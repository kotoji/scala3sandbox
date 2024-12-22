import cats.effect.IOApp
import cats.effect.IO
import cats.effect.std.Console

object SnsAppEntry extends IOApp.Simple {
  def run: IO[Unit] = SnsApp.impl[IO].run
}

trait SnsApp[F[_]] {
  def run: F[Unit]
}

object SnsApp {
  def impl[F[_]: Console]: SnsApp[F] = new SnsApp[F] {
    def run: F[Unit] =
      Console[F].println("Hello, world!")
  }
}
