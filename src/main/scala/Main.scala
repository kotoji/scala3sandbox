import cats.effect.IOApp
import cats.effect.IO
import cats.effect.ExitCode
import cats.effect.Resource
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.InputStream
import java.io.OutputStream

object Main extends IOApp {
  override def run(args: List[String]): IO[ExitCode] = FileCopy.run(args)
}
