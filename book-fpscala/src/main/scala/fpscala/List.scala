package fpscala

sealed trait List[+A]
case class Cons[+A](head: A, tail: List[A]) extends List[A]
case object Nil extends List[Nothing]

object List:
  def apply[A](as: A*): List[A] =
    if (as.isEmpty) Nil
    else Cons(as.head, apply(as.tail*))

  def foldLeft[A, B](as: List[A], z: B)(f: (B, A) => B): B = as match
    case Nil         => z
    case Cons(x, xs) => foldLeft(xs, f(z, x))(f)

  def reverse[A](as: List[A]) =
    foldLeft(as, Nil)((acc: List[A], head: A) => Cons(head, acc))

  def foldRight[A, B](as: List[A], z: B)(f: (A, B) => B): B =
    foldLeft(reverse(as), z)((b, a) => f(a, b))

  def append[A](xs: List[A], ys: List[A]): List[A] =
    foldRight(xs, ys)((x, acc) => Cons(x, acc))

  def concat[A](xss: List[List[A]]): List[A] =
    foldLeft(xss, Nil: List[A])(append)

  def map[A, B](as: List[A])(f: A => B): List[B] = as match
    case Nil         => Nil
    case Cons(x, xs) => Cons(f(x), map(xs)(f))

  def filter[A](as: List[A])(f: A => Boolean): List[A] =
    flatMap(as)(a => if f(a) then List(a) else Nil)

  def flatMap[A, B](as: List[A])(f: A => List[B]): List[B] = concat(map(as)(f))

  def zipWith[A, B, C](as: List[A], bs: List[B])(f: (A, B) => C): List[C] =
    (as, bs) match
      case (Nil, _)                     => Nil
      case (_, Nil)                     => Nil
      case (Cons(h1, t1), Cons(h2, t2)) => Cons(f(h1, h2), zipWith(t1, t2)(f))

  def takeN[A](as: List[A], n: Int): List[A] =
    def impl(as: List[A], n: Int, acc: List[A]): List[A] =
      if n == 0 then acc
      else
        as match
          case Nil         => acc
          case Cons(x, xs) => impl(xs, n - 1, Cons(x, acc))
    reverse(impl(as, n, Nil: List[A]))

  def startWith[A](sup: List[A], sub: List[A]): Boolean =
    foldLeft(zipWith(takeN(sup, Main.length(sub)), sub)(_ == _), true)(_ && _)

  def hasSubsequence[A](sup: List[A], sub: List[A]): Boolean =
    if startWith(sup, sub) then true
    else
      sup match
        case Nil         => false
        case Cons(_, xs) => startWith(xs, sub)
