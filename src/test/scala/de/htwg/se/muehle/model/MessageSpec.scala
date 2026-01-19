package de.htwg.se.muehle.model

import org.scalatest.wordspec.AnyWordSpec
import org.scalatest.matchers.should.Matchers

class MessageSpec extends AnyWordSpec with Matchers {

  "PlaceStoneMessage" should {
    "have Success message" in {
      PlaceStoneMessage.Success.toString should be("Bitte Stein setzen(0-23)")
    }
    "have InvalidMove message" in {
      PlaceStoneMessage.InvalidMove.toString should be("Bitte Stein setzen(0-23)")
    }
    "have Occupied message" in {
      PlaceStoneMessage.Occupied.toString should be("Feld ist besetzt")
    }
    "implement PlaceStoneMessage trait" in {
      PlaceStoneMessage.Success shouldBe a[PlaceStoneMessage]
      PlaceStoneMessage.InvalidMove shouldBe a[PlaceStoneMessage]
      PlaceStoneMessage.Occupied shouldBe a[PlaceStoneMessage]
    }
    "implement Message trait" in {
      PlaceStoneMessage.Success shouldBe a[Message]
      PlaceStoneMessage.InvalidMove shouldBe a[Message]
      PlaceStoneMessage.Occupied shouldBe a[Message]
    }
  }

  "RemoveStoneMessage" should {
    "have InvalidMove message" in {
      RemoveStoneMessage.InvalidMove.toString should be("Du kannst diesen Stein nicht entfernen")
    }
    "have Success message" in {
      RemoveStoneMessage.Success.toString should be("Wähle einen Stein zum entfernen")
    }
    "have OwnStoneChosen message" in {
      RemoveStoneMessage.OwnStoneChosen.toString should be("Du kannst deinen eigenen Stein nicht entfernen!")
    }
    "implement RemoveStoneMessage trait" in {
      RemoveStoneMessage.InvalidMove shouldBe a[RemoveStoneMessage]
      RemoveStoneMessage.Success shouldBe a[RemoveStoneMessage]
      RemoveStoneMessage.OwnStoneChosen shouldBe a[RemoveStoneMessage]
    }
    "implement Message trait" in {
      RemoveStoneMessage.InvalidMove shouldBe a[Message]
      RemoveStoneMessage.Success shouldBe a[Message]
      RemoveStoneMessage.OwnStoneChosen shouldBe a[Message]
    }
  }

  "MoveStoneMessage" should {
    "have NoNeighbour message" in {
      MoveStoneMessage.NoNeighbour.toString should be("Zielfeld ist kein Nachbar")
    }
    "have NeighbourOccupied message" in {
      MoveStoneMessage.NeighbourOccupied.toString should be("Nachbarfeld ist besetzt")
    }
    "have NotYourStone message" in {
      MoveStoneMessage.NotYourStone.toString should be("Du hast diesen Stein nicht")
    }
    "have Success message" in {
      MoveStoneMessage.Success.toString should be("Bewege einen Stein")
    }
    "implement MoveStoneMessage trait" in {
      MoveStoneMessage.NoNeighbour shouldBe a[MoveStoneMessage]
      MoveStoneMessage.NeighbourOccupied shouldBe a[MoveStoneMessage]
      MoveStoneMessage.NotYourStone shouldBe a[MoveStoneMessage]
      MoveStoneMessage.Success shouldBe a[MoveStoneMessage]
    }
    "implement Message trait" in {
      MoveStoneMessage.NoNeighbour shouldBe a[Message]
      MoveStoneMessage.NeighbourOccupied shouldBe a[Message]
      MoveStoneMessage.NotYourStone shouldBe a[Message]
      MoveStoneMessage.Success shouldBe a[Message]
    }
  }

  "Winner" should {
    "have White winner message" in {
      Winner.White.toString should be("Gewinner: WEISS")
    }
    "have Black winner message" in {
      Winner.Black.toString should be("Gewinner: SCHWARZ")
    }
    "implement Winner trait" in {
      Winner.White shouldBe a[Winner]
      Winner.Black shouldBe a[Winner]
    }
    "implement Message trait" in {
      Winner.White shouldBe a[Message]
      Winner.Black shouldBe a[Message]
    }
  }
}
