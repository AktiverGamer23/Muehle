package de.htwg.se.muehle.model

import org.scalatest.wordspec.AnyWordSpec
import org.scalatest.matchers.should.Matchers

class MessageSpec extends AnyWordSpec with Matchers {

  "SuccessMessage" should {

    "have correct toString for PlaceStone" in {
      SuccessMessage.PlaceStone.toString.shouldBe("Bitte Stein setzen(0-23)")
    }

    "have correct toString for MoveStone" in {
      SuccessMessage.MoveStone.toString.shouldBe("0-23")
    }

    "have correct toString for RemoveStone" in {
      SuccessMessage.RemoveStone.toString.shouldBe("Bitte Stein Bewegen")
    }
  }

  "ErrorMessage" should {

    "have correct toString for InvalidMove" in {
      ErrorMessage.InvalidMove.toString.shouldBe("Ungültiger Zug!")
    }

    "have correct toString for NotYourStone" in {
      ErrorMessage.NotYourStone.toString.shouldBe("Du hast diesen Stein nicht!")
    }

    "have correct toString for NoNeighbour" in {
      ErrorMessage.NoNeighbour.toString.shouldBe("Zielfeld ist kein Nachbar!")
    }

    "have correct toString for NeighbourOccupied" in {
      ErrorMessage.NeighbourOccupied.toString.shouldBe("Nachbarfeld besetzt!")
    }

    "have correct toString for OwnStoneChosen" in {
      ErrorMessage.OwnStoneChosen.toString.shouldBe("Du kannst deinen eigenen Stein nicht entfernen!")
    }

    "have correct toString for InvalidMoveRemove" in {
      ErrorMessage.InvalidMoveRemove.toString.shouldBe("Du kannst diesen Stein nicht entfernen")
    }
  }
}
