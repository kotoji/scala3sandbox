package fpscala

import cats.effect.IO
import cats.effect.unsafe.implicits.global
import cats.syntax.all._
import scala.concurrent.duration.*

object Main {
  def main(args: Array[String]): Unit =
    val run: IO[Unit] = for
      _ <- IO.println(solve3_10(List(1, 2, 3, 4)))
      _ <- IO.println(solve3_11(List(1, 2, 3, 4)))
      _ <- IO.println(solve3_12(List(1, 2, 3, 4)))
    yield ()

    run.unsafeRunSync()

  def solve3_10[A](as: List[A]): Int =
    List.foldLeft(as, 0)((acc, _) => acc + 1)
  def solve3_11(xs: List[Int]): (Int, Int, Int) =
    (sum(xs), product(xs), length(xs))
  def solve3_12[A](as: List[A]): List[A] = List.reverse(as)

  def sum(xs: List[Int]): Int = List.foldLeft(xs, 0)(_ + _)
  def product(xs: List[Int]): Int = List.foldLeft(xs, 1)(_ * _)
  def length[A](as: List[A]): Int = List.foldLeft(as, 0)((acc, _) => acc + 1)
  def increment(ns: List[Int]): List[Int] = List.map(ns)(_ + 1)
  def doubleToString(ds: List[Double]): List[String] = List.map(ds)(_.toString)
  def addPairwise(xs: List[Int], ys: List[Int]): List[Int] = (xs, ys) match
    case (Nil, _)                     => Nil
    case (_, Nil)                     => Nil
    case (Cons(h1, t1), Cons(h2, t2)) => Cons(h1 + h2, addPairwise(t1, t2))
}
