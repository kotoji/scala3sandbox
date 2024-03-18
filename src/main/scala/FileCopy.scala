import cats.effect.IOApp
import cats.effect.IO
import cats.effect.ExitCode
import cats.effect.Resource
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.InputStream
import java.io.OutputStream
import cats.effect.kernel.Sync
import cats.effect.std.Console
import cats.ApplicativeError
import cats.syntax.all._

object FileCopy {
  def run[F[_]: Sync: Console](
      args: List[String]
  )(using F: ApplicativeError[F, Throwable]): F[ExitCode] =
    for {
      _ <-
        if (args.length < 2)
          F.raiseError(new IllegalArgumentException("Need origin and destination files")).void
        else
          F.unit
      orig = new File(args(0))
      dest = new File(args(1))
      count <- copy(orig, dest)
      _ <- Console[F].println(s"$count bytes copied from ${orig.getPath} to ${dest.getPath}")
    } yield ExitCode.Success

  def inputStream[F[_]: Sync](
      f: File
  )(using F: ApplicativeError[F, Throwable]): Resource[F, FileInputStream] =
    Resource.make {
      Sync[F].blocking(new FileInputStream(f))
    } { inStream =>
      val close = Sync[F].blocking(inStream.close())
      F.handleErrorWith(close)(_ => F.unit)
    }

  def outputStream[F[_]: Sync](
      f: File
  )(using F: ApplicativeError[F, Throwable]): Resource[F, FileOutputStream] =
    Resource.make {
      Sync[F].blocking(new FileOutputStream(f))
    } { outStream =>
      val close = Sync[F].blocking(outStream.close())
      F.handleErrorWith(close)(_ => F.unit)
    }

  def inputOutputStreams[F[_]: Sync](
      in: File,
      out: File
  ): Resource[F, (FileInputStream, FileOutputStream)] =
    for {
      inStream <- inputStream(in)
      outStream <- outputStream(out)
    } yield (inStream, outStream)

  def transfer[F[_]: Sync](origin: InputStream, destination: OutputStream): F[Long] = {
    def go[F[_]: Sync](org: InputStream, dst: OutputStream, buf: Array[Byte], acc: Long): F[Long] =
      for {
        amount <- Sync[F].blocking(org.read(buf, 0, buf.length))
        count <-
          if (amount > -1) {
            Sync[F].blocking(dst.write(buf, 0, amount)) >> go(org, dst, buf, acc + amount)
          } else {
            Sync[F].pure(acc)
          }
      } yield count

    go(origin, destination, new Array[Byte](1024 * 10), 0L)
  }

  def copy[F[_]: Sync](origin: File, destination: File): F[Long] =
    inputOutputStreams(origin, destination).use { case (in, out) =>
      transfer(in, out)
    }

}
