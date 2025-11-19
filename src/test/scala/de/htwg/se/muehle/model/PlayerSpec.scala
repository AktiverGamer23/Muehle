package de.htwg.se.muehle.model

import org.scalatest.wordspec.AnyWordSpec
import org.scalatest.matchers.should.Matchers

class PlayerSpec extends AnyWordSpec with Matchers {

  "Player.next" should {

    "return Black when current player is White" in {
      Player.White.next shouldBe Player.Black
    }

    "return White when current player is Black" in {
      Player.Black.next shouldBe Player.White
    }

  }

}
