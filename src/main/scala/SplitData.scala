import cats.effect.IOApp
import cats.effect.IO
import cats.effect.std.Queue
import cats.effect.Async
import cats.effect.Sync
import cats.effect.Concurrent
import cats.effect.Spawn
import cats.implicits._
import cats.syntax.all._
import fs2.Stream
import scala.concurrent.duration._
import cats.effect.std.Console
import cats.effect.Temporal
import fs2.io.file.Files
import fs2.io.file.Path
import cats.MonadThrow
import cats.Monad
import fs2.Pipe
import fs2.Pull
import fs2.Chunk
import cats.effect.std.Random
import fs2.io.file.{Path, Files}
import fs2.io.readInputStream

import java.net.{URI, URL}
import java.io.InputStream
import scala.util.Try
import cats.effect.Ref
import scala.collection.immutable.SortedMap

object SplitData extends IOApp.Simple {
  def run: IO[Unit] = SplitData[IO].run2
}

class SplitData[F[_]: Async: Monad: Concurrent: MonadThrow: Console] {
  val filePath = "data/kaggle_santander_product_recommendation/train_ver2.csv"
  val outDir   = "data/output"
  val N        = 9

  // A naive implementation for comparison
  // This is much faster than `run`...
  def run2: F[Unit] = {
    val writeAppend = (path: java.nio.file.Path, s: String) =>
      (java.nio.file.Files
        .writeString(
          path,
          s,
          java.nio.charset.StandardCharsets.UTF_8,
          java.nio.file.StandardOpenOption.CREATE,
          java.nio.file.StandardOpenOption.APPEND,
        ))
        .pure[F]
        .void
    val writeCreate = (path: java.nio.file.Path, s: String) =>
      (java.nio.file.Files
        .writeString(
          path,
          s,
          java.nio.charset.StandardCharsets.UTF_8,
          java.nio.file.StandardOpenOption.CREATE,
          java.nio.file.StandardOpenOption.TRUNCATE_EXISTING,
        ))
        .pure[F]
        .void

    for {
      pathsRef <- Ref.of[F, Map[String, java.nio.file.Path]](Map.empty)
      _ <- Files[F]
        // NOTE: It's not going well with readUtf8Lines
        .readAll(Path(filePath))
        .dropThrough(p => p != '\n'.toByte)
        .dropLast
        .through(fs2.text.utf8Decode)
        // It's smarter to use `groupAdjacentByLimit` instead of the long lines with `repartition`.
        // .repartition(s => fs2.Chunk.array(s.split("\n")))
        // .groupAdjacentByLimit(1000)(s => s.take("2006-01".length))
        // .map(_._2.toList.mkString("\n"))
        // .map(_ + "\n")
        .repartition(s => {
          var chunks: Array[Array[String]] = Array.empty
          var chunk: Array[String]         = Array.empty
          var currentKey                   = ""
          for (line <- s.split("\n")) {
            val k = line.take("2006-01".length)
            if (k != currentKey || chunk.size >= 100) {
              if (chunk.nonEmpty) {
                chunks :+= chunk
              }
              chunk = Array(line)
              currentKey = k
            } else {
              chunk :+= line
            }
          }
          if (chunk.nonEmpty) {
            chunks :+= chunk
          }
          fs2.Chunk.array(chunks.map(_.mkString("\n")))
        })
        .map(_ + "\n")
        .evalTap(s => {
          val k = s.take("2006-01".length)
          for {
            paths <- pathsRef.get
            _ <- paths.get(k) match {
              case Some(path) => writeAppend(path, s)
              case None =>
                for {
                  path <- (java.nio.file.Path
                    .of(s"${outDir}/${k}.csv"))
                    .pure[F]
                  _ <- pathsRef.set(paths + (k -> path))
                  _ <- writeCreate(path, s)
                } yield ()
            }
          } yield ()
        })
        .compile
        .drain
    } yield ()
  }

  def parWrite(m: SortedMap[String, (Long, Long)]): F[Unit] = {
    val streams = m.keys.toList
      .map(k => (k, s"${outDir}/${k}.csv"))
      .map((k, s) => (k, Files[F].writeAll(Path(s))))
      .map((k, p) => {
        Files[F]
          .readUtf8Lines(Path(filePath))
          .drop(1)
          .drop(m(k)._1)
          .take(m(k)._2 - m(k)._1 + 1)
          .takeThrough(s => s.startsWith(k)) // TODO: Enable to change the condition
          .map(_ + "\n")
          .through(fs2.text.utf8Encode)
          .through(p)
      })
    Stream(streams*).parJoin(streams.size).compile.drain
  }

  def collectFileInfo(infoRef: Ref[F, SortedMap[String, (Long, Long)]]): F[Unit] =
    Files[F]
      .readUtf8Lines(Path(filePath))
      .drop(1)
      .dropLast
      .zipWithIndex
      .evalTap((s, i) => {
        val k = s.take("2006-01".length)
        infoRef.get.flatMap(m =>
          m.get(k) match {
            case Some((s, t)) => infoRef.set(m.updated(k, (s, i)))
            case None         => infoRef.set(m + (k -> (i, i)))
          }
        )
      })
      .compile
      .drain

  // It's assumed that the file is almost sorted by the timestamp
  def run: F[Unit] =
    for {
      infoRef <- Ref.of[F, SortedMap[String, (Long, Long)]](SortedMap.empty)
      _       <- collectFileInfo(infoRef)
      // TODO: make output directory if it doesn't exist
      _ <- infoRef.get.flatMap { m =>
        val tasks = for {
          i <- (0 to m.size / N).toList
        } yield parWrite(m.drop(i * N).take(N))
        tasks.sequence
      }
    } yield ()
}
