package de.htwg.se.muehle.model

import org.scalatest.wordspec.AnyWordSpec
import org.scalatest.matchers.should.Matchers
import de.htwg.se.muehle.player.Player

class PlayerSpec extends AnyWordSpec with Matchers {

  "A Player" when {
    "White" should {
      "have next player as Black" in {
        Player.White.next should be(Player.Black)
      }
      "have correct toString" in {
        Player.White.toString should be("White")
      }
    }

    "Black" should {
      "have next player as White" in {
        Player.Black.next should be(Player.White)
      }
      "have correct toString" in {
        Player.Black.toString should be("Black")
      }
    }

    "alternating" should {
      "cycle correctly" in {
        val p1 = Player.White
        val p2 = p1.next
        val p3 = p2.next
        val p4 = p3.next

        p2 should be(Player.Black)
        p3 should be(Player.White)
        p4 should be(Player.Black)
      }
    }
  }
}
