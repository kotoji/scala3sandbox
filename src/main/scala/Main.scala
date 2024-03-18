import cats.effect.IOApp
import cats.effect.IO
import cats.effect.ExitCode
import cats.syntax.all._

object Main extends IOApp {
  override def run(args: List[String]): IO[ExitCode] = FileCopy.run(args)
}
