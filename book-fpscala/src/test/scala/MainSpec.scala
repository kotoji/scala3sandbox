package fpscala

import org.scalatest.funspec.AnyFunSpec
import org.scalatest.matchers.*
import org.scalatest.prop.TableDrivenPropertyChecks
import org.scalatest.prop.TableDrivenPropertyChecks.Table
import org.scalacheck.Prop.*

class MainSpec
    extends AnyFunSpec
    with TableDrivenPropertyChecks
    with should.Matchers {

  describe("exercise 3.10") {
    it("case1") {
      val obtained = Main.solve3_10(List(1, 2, 3, 4))
      obtained should be(4)
    }
  }
  describe("exercise 3.11") {
    it("length") {
      val table = Table(
        ("list", "length"),
        (List(1), 1),
        (List(1, 2), 2),
        (List(1, 2, 3, 4), 4)
      )
      forAll(table) { (a: List[Int], expected: Int) =>
        Main.solve3_10(a) == expected
      }
    }
  }
  describe("exercise 3.13") {
    it("foldRight with foldLeft") {
      val obtained = List.foldRight(List(1, 2, 3), 0)(_ + _)
      obtained should be(6)
    }
  }
  describe("exercise 3.14") {
    it("append") {
      val obtained = List.append(List(1, 2, 3), List(4, 5, 6))
      obtained should be(List(1, 2, 3, 4, 5, 6))
    }
  }
  describe("exercise 3.15") {
    it("append2") {
      val obtained = List.concat(List(List(1, 2), List(4, 5), List(7, 8)))
      obtained should be(List(1, 2, 4, 5, 7, 8))
    }
  }
  describe("exercise 3.16") {
    it("increment") {
      val obtained = Main.increment(List(1, 3, 5, 7, 11))
      obtained should be(List(2, 4, 6, 8, 12))
    }
  }
  describe("exercise 3.17") {
    it("doubleToString") {
      val obtained = Main.doubleToString(List(1.1, 3.3, 5.5))
      obtained should be(List("1.1", "3.3", "5.5"))
    }
  }
  describe("exercise 3.19") {
    it("filter") {
      val obtained = List.filter(List(1, 2, 3, 4, 5, 7))(_ % 2 == 1)
      obtained should be(List(1, 3, 5, 7))
    }
  }
  describe("exercise 3.20") {
    it("flatMap") {
      val obtained = List.flatMap(List(1, 2, 3))(i => List(i, i))
      obtained should be(List(1, 1, 2, 2, 3, 3))
    }
  }
  describe("exercise 3.22") {
    it("addPairwise") {
      val obtained = Main.addPairwise(List(1, 2, 3), List(4, 5, 6))
      obtained should be(List(5, 7, 9))
    }
  }
  describe("exercise 3.33") {
    it("zipWith") {
      val obtained = List.zipWith(List(1, 2, 3), List(4, 5, 6))(_ + _)
      obtained should be(List(5, 7, 9))
    }
  }
  describe("exercise 3.3x") {
    it("case 1") {
      val obtained = List.hasSubsequence(List(1, 2, 3, 4), List(2, 3))
      obtained should be(true)
    }
    it("case 2") {
      val obtained = List.hasSubsequence(List(1, 2, 3, 4), List(2, 4))
      obtained should be(false)
    }
    it("case 3") {
      val obtained = List.hasSubsequence(Nil, Nil)
      obtained should be(false)
    }
  }

  describe("exercise 3.25") {
    it("xxx") {
      val obtained =
        Tree.size(Branch(Leaf(1), Branch(Leaf(2), Leaf(3))))
      obtained should be(5)
    }
  }
}
