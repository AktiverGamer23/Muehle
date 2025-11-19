package de.htwg.se.muehle.model

import org.scalatest.wordspec.AnyWordSpec
import org.scalatest.matchers.should.Matchers

class BoardNeighborsSpec extends AnyWordSpec with Matchers {

  "The neighbors map" should {

    "contain 24 fields (0–23)" in {
      neighbors.size shouldBe 24
      neighbors.keys should contain allElementsOf (0 to 23)
    }

    "have correct neighbor assignments" in {
      neighbors(0) should contain theSameElementsAs List(1, 7)
      neighbors(1) should contain theSameElementsAs List(2, 0, 9)
      neighbors(15) should contain theSameElementsAs List(14, 7, 8, 23)
      neighbors(23) should contain theSameElementsAs List(15, 22, 16)
    }

    "be symmetric (if A is neighbor of B, B must be neighbor of A)" in {
      for {
        (pos, neighList) <- neighbors
        n <- neighList
      } neighbors(n) should contain (pos)
    }
  }

  "The mill definitions" should {
    "contain only valid board positions" in {
      mills.flatten foreach { pos =>
        pos should (be >= 0 and be <= 23)
      }
    }

    "define 16 mills" in {
      mills.size shouldBe 16
    }

    "have exactly 3 positions per mill" in {
      all (mills.map(_.size)) shouldBe 3
    }
  }
}
