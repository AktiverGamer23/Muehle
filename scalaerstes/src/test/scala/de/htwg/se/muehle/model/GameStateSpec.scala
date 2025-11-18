package de.htwg.se.muehle.model

import org.scalatest.wordspec.AnyWordSpec
import org.scalatest.matchers.should.Matchers

class GameStateSpec extends AnyWordSpec with Matchers {

  val emptyB = emptyBoard

  def baseState: GameState =
    GameState(
      board = emptyB,
      currentPlayer = Player.White,
      whiteStonesToPlace = 9,
      blackStonesToPlace = 9,
      whiteStones = 9,
      blackStones = 9
    )



  "GameState – placing stones" should {

    "allow placing a stone on an empty field" in {
      val state = baseState
      val next = state.placeStone(0)

      next.board(0) shouldBe Occupied(Player.White)
      next.whiteStonesToPlace shouldBe 8
      next.currentPlayer shouldBe Player.Black
    }

    "reject placing a stone on an occupied field" in {
      val state = baseState.placeStone(0)
      val next = state.placeStone(0)

      next.message shouldBe Some(ErrorMessage.InvalidMove)
    }

    "detect a mill after placing" in {
      val state =
        baseState
          .copy(whiteStonesToPlace = 3)
          .placeStone(0)  // W
          .switchPlayer.copy(currentPlayer = Player.White)
          .placeStone(1)  // W
          .switchPlayer.copy(currentPlayer = Player.White)

      val finalState = state.placeStone(2) // Mühle

      finalState.isMillFormed(2) shouldBe true
      finalState.message shouldBe Some(SuccessMessage.RemoveStone)
    }
  }


  "GameState – movement phase" should {

    "reject moving an opponent's stone" in {
      val state = baseState.copy(board = emptyB.updated(0, Occupied(Player.Black)))
      val next = state.moveStone(0, 1)

      next.message shouldBe Some(ErrorMessage.NotYourStone)
    }

    "reject moving to a non-neighbour if flying is not allowed" in {
      val state = baseState.copy(board = emptyB.updated(0, Occupied(Player.White)))
      val next = state.moveStone(0, 10)

      next.message shouldBe Some(ErrorMessage.NoNeighbour)
    }

    "allow moving to a neighbour" in {
      val board = emptyB.updated(0, Occupied(Player.White))
      val state = baseState.copy(board = board, whiteStonesToPlace = 0, blackStonesToPlace = 0)

      val next = state.moveStone(0, 1)

      next.board(0) shouldBe Empty
      next.board(1) shouldBe Occupied(Player.White)
      next.currentPlayer shouldBe Player.Black
    }

    "allow flying when player has only 3 stones" in {
      val board = emptyB.updated(0, Occupied(Player.White))
      val state = baseState.copy(
        board = board,
        whiteStones = 3,
        whiteStonesToPlace = 0,
        blackStonesToPlace = 0
      )

      val next = state.moveStone(0, 10)

      next.board(10) shouldBe Occupied(Player.White)
      next.message shouldBe Some(SuccessMessage.MoveStone)
    }

    "detect mill after moving a stone" in {
      val board =
        emptyB
          .updated(0, Occupied(Player.White))
          .updated(1, Occupied(Player.White))
          .updated(3, Occupied(Player.White))

      val state = baseState.copy(
        board = board,
        whiteStonesToPlace = 0,
        blackStonesToPlace = 0
      )

      val next = state.moveStone(3, 2)

      next.isMillFormed(2) shouldBe true
      next.message shouldBe Some(SuccessMessage.RemoveStone)
    }
  }


  "GameState – removing stones" should {

    "allow removing an opponent stone" in {
      val board =
        emptyB.updated(5, Occupied(Player.Black))

      val state = baseState.copy(board = board)
      val next = state.removeStone(5)

      next.board(5) shouldBe Empty
      next.blackStones shouldBe 8
      next.currentPlayer shouldBe Player.Black
    }

    "reject removing own stone" in {
      val board =
        emptyB.updated(5, Occupied(Player.White))

      val state = baseState.copy(board = board)
      val next = state.removeStone(5)

      next.message shouldBe Some(ErrorMessage.OwnStoneChosen)
    }

    "reject removing from an empty field" in {
      val state = baseState
      val next = state.removeStone(10)

      next.message shouldBe Some(ErrorMessage.InvalidMoveRemove)
    }
  }


  "GameState – global logic" should {

    "switch player correctly" in {
      val next = baseState.switchPlayer
      next.currentPlayer shouldBe Player.Black
    }

    "detect losing condition when white has < 3 stones" in {
      val state = baseState.copy(whiteStones = 2)
      state.validate shouldBe Some(Player.Black)
    }

    "detect losing condition when black has < 3 stones" in {
      val state = baseState.copy(blackStones = 2)
      state.validate shouldBe Some(Player.White)
    }

    "detect placing phase over" in {
      val state = baseState.copy(whiteStonesToPlace = 0, blackStonesToPlace = 0)
      state.isPlacingPhaseOver shouldBe true
    }
  }
    "GameState – toString / board rendering" should {

    "include current player, stone counts and board layout" in {
      val state = baseState
      val text = state.toString

      text should include ("Current Player: White")
      text should include ("White Stones: 9 (to place: 9)")
      text should include ("Black Stones: 9 (to place: 9)")

      // Check that empty board coordinates appear
      text should include ("16---------17---------18")
      text should include ("22---------21---------20")
    }
  }


  "GameState – message correctness" should {

  "use correct error messages" in {

    // 1) NotYourStone: Feld ist leer
    val e1 = baseState.moveStone(0, 10)
    e1.message shouldBe Some(ErrorMessage.NotYourStone)

    // 2) NoNeighbour: Spieler hat eigenen Stein, aber Ziel ist kein Nachbar
    val boardOwn = emptyB.updated(0, Occupied(Player.White))
    val e2 = baseState.copy(
      board = boardOwn,
      whiteStonesToPlace = 0,
      blackStonesToPlace = 0
    ).moveStone(0, 10)

    e2.message shouldBe Some(ErrorMessage.NoNeighbour)

    // 3) InvalidMoveRemove: Feld ist leer
    val e3 = baseState.removeStone(5)
    e3.message shouldBe Some(ErrorMessage.InvalidMoveRemove)

    // 4) OwnStoneChosen: versucht eigenen Stein zu entfernen
    val e4 = baseState.copy(
      board = emptyB.updated(0, Occupied(Player.White))
    ).removeStone(0)

    e4.message shouldBe Some(ErrorMessage.OwnStoneChosen)
  }


  "use correct success messages" in {

    // Place stone
    val place = baseState.placeStone(0)
    place.message shouldBe Some(SuccessMessage.PlaceStone)

    // Move stone (requires no placing phase)
    val moveState = baseState.copy(
      board = emptyB.updated(0, Occupied(Player.White)),
      whiteStonesToPlace = 0,
      blackStonesToPlace = 0
    )

    val moved = moveState.moveStone(0, 1)
    moved.message shouldBe Some(SuccessMessage.MoveStone)
  }
}
  "GameState – canFly" should {

    "return true if White has exactly 3 stones" in {
      val state = baseState.copy(whiteStones = 3)
      state.canFly(Player.White) shouldBe true
    }

    "return false if White has more than 3 stones" in {
      val state = baseState.copy(whiteStones = 4)
      state.canFly(Player.White) shouldBe false
    }

    "return true if Black has exactly 3 stones" in {
      val state = baseState.copy(blackStones = 3)
      state.canFly(Player.Black) shouldBe true
    }

    "return false if Black has more than 3 stones" in {
      val state = baseState.copy(blackStones = 5)
      state.canFly(Player.Black) shouldBe false
    }
  }
    "GameState – validate" should {

    "return None if both players have 3 or more stones" in {
      val state = baseState.copy(whiteStones = 3, blackStones = 3)
      state.validate.shouldBe(None)
    }

    "return Some(Player.Black) if White has less than 3 stones" in {
      val state = baseState.copy(whiteStones = 2, blackStones = 5)
      state.validate.shouldBe(Some(Player.Black))
    }

    "return Some(Player.White) if Black has less than 3 stones" in {
      val state = baseState.copy(whiteStones = 5, blackStones = 2)
      state.validate.shouldBe(Some(Player.White))
    }
  }




  "GameState – complete game flow" should {

    "play through placing → mill → remove → move phase" in {
      // Start: Placing White stones
      val s0 = baseState

      val s1 = s0.placeStone(0)                           // W places at 0
      s1.currentPlayer shouldBe Player.Black

      val s2 = s1.placeStone(10)                          // B places
      s2.currentPlayer shouldBe Player.White

      val s3 = s2.placeStone(1)                           // W places at 1
      s3.currentPlayer shouldBe Player.Black

      val s4 = s3.placeStone(11)                          // B places
      s4.currentPlayer shouldBe Player.White

      val s5 = s4.placeStone(2)                           // W forms mill (0,1,2)
      s5.isMillFormed(2) shouldBe true
      s5.message shouldBe Some(SuccessMessage.RemoveStone)

      // Remove a Black stone
      val s6 = s5.removeStone(10)
      s6.currentPlayer shouldBe Player.Black
      s6.blackStones shouldBe 8

      // Skip ahead: Move phase (force finishing placing phase)
      val moveReady =
        s6.copy(whiteStonesToPlace = 0, blackStonesToPlace = 0)

      val s7 =
        moveReady.copy(currentPlayer = Player.Black)
          .moveStone(11, 10)                  // B moves 11 → 9

      s7.board(11) shouldBe Empty
      s7.board(10) shouldBe Occupied(Player.Black)
      s7.message shouldBe Some(SuccessMessage.MoveStone)
      s7.currentPlayer shouldBe Player.White
    }
  }
  "GameState – boardString" should {

  "display empty, white and black stones correctly" in {
    val board = emptyB
      .updated(0, Occupied(Player.White))
      .updated(1, Occupied(Player.Black))
      .updated(2, Empty) 

    val state = baseState.copy(board = board)
    val str = state.toString

    str should include (" W")
    str should include (" B")
    str should include (" 2")
  }

  "handle full board without crashing" in {
    val board = Vector.tabulate(24)(i => if i % 2 == 0 then Occupied(Player.White) else Occupied(Player.Black))
    val state = baseState.copy(board = board)
    val str = state.toString

    str.count(_ == 'W') should be >= 3
    str.count(_ == 'B') should be >= 3
  }
}
"GameState – removeStone" should {

  "decrease white stones and update board when removing a black stone" in {
    // White currentPlayer, Black Stein wird entfernt
    val board = emptyB.updated(5, Occupied(Player.Black))
    val state = baseState.copy(board = board)

    val result = state.removeStone(5)

    result.board(5) shouldBe Empty
    result.blackStones shouldBe 8
    result.message.get shouldBe SuccessMessage.PlaceStone  // Placing-Phase noch nicht vorbei
  }

  "set SuccessMessage.MoveStone if placing phase is over" in {
    // White currentPlayer, Black Stein entfernen, Placing-Phase vorbei
    val board = emptyB.updated(5, Occupied(Player.Black))
    val state = baseState.copy(
      board = board,
      whiteStonesToPlace = 0,
      blackStonesToPlace = 0
    )

    val result = state.removeStone(5)

    result.board(5) shouldBe Empty
    result.blackStones shouldBe 8
    result.message.get shouldBe SuccessMessage.MoveStone
  }

  "decrease black stones and update board when removing a white stone" in {
    // Black currentPlayer, White Stein wird entfernt
    val board = emptyB.updated(5, Occupied(Player.White))
    val state = baseState.copy(board = board, currentPlayer = Player.Black)

    val result = state.removeStone(5)

    result.board(5) shouldBe Empty
    result.whiteStones shouldBe 8
    result.message.get shouldBe SuccessMessage.PlaceStone
  }
}
"GameState – moveStone" should {

  "return ErrorMessage.NeighbourOccupied when target field is already occupied" in {
    // Setup: currentPlayer White, von Feld 0 nach Feld 1, aber Feld 1 ist schon besetzt
    val board = emptyB
      .updated(0, Occupied(Player.White))
      .updated(1, Occupied(Player.Black))  // Zielfeld besetzt

    val state = baseState.copy(board = board)

    val result = state.moveStone(0, 1)

    result.board(0) shouldBe Occupied(Player.White)   // Stein bleibt auf altem Feld
    result.board(1) shouldBe Occupied(Player.Black)   // Zielfeld unverändert
    result.message.get shouldBe ErrorMessage.NeighbourOccupied
    result.currentPlayer shouldBe Player.White       // Spieler wechselt nicht
  }
}
"GameState – placeStone" should {

  "switch automatically to Move-Phase when placing phase is over" in {
    // Beide Spieler haben nur noch einen Stein zu setzen
    val state = baseState.copy(
      whiteStonesToPlace = 1,
      blackStonesToPlace = 1,
      currentPlayer = Player.White
    )

    // Weiß setzt letzten Stein
    val s = state.placeStone(0)
    val s1 = s.placeStone(1)

    // Jetzt sollte die Nachricht auf MoveStone stehen
    s1.message.get.shouldBe(SuccessMessage.MoveStone)
    s1.whiteStonesToPlace.shouldBe(0)
    s1.blackStonesToPlace.shouldBe(0) // Black hat noch einen Stein
    s1.currentPlayer.shouldBe(Player.White) // Spielerwechsel
  }

  "continue placing phase if not all stones are placed" in {
    val state = baseState.copy(
      whiteStonesToPlace = 2,
      blackStonesToPlace = 2,
      currentPlayer = Player.White
    )

    val s1 = state.placeStone(0)

    s1.message.get.shouldBe(SuccessMessage.PlaceStone)
    s1.whiteStonesToPlace.shouldBe(1)
    s1.currentPlayer.shouldBe(Player.Black)
  }
}




}
