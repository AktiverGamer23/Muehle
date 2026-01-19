package de.htwg.se.muehle.util

import org.scalatest.wordspec.AnyWordSpec
import org.scalatest.matchers.should.Matchers
import de.htwg.se.muehle.gamestate.{GameState, GameStateInterface}
import de.htwg.se.muehle.board.Board
import de.htwg.se.muehle.player.Player
import de.htwg.se.muehle.model.*

class GameStateMementoSpec extends AnyWordSpec with Matchers {

  "A GameStateMemento" when {
    "created from a game state" should {
      val originalState = GameState.create(Board.empty, 9, 9, 9, 9)
      val memento = GameStateMemento.create(originalState)

      "restore the same state" in {
        val restored = memento.restore()

        restored.currentPlayer should be(originalState.currentPlayer)
        restored.phase should be(originalState.phase)
        restored.whiteStonesToPlace should be(originalState.whiteStonesToPlace)
        restored.blackStonesToPlace should be(originalState.blackStonesToPlace)
        restored.whiteStones should be(originalState.whiteStones)
        restored.blackStones should be(originalState.blackStones)
        restored.message should be(originalState.message)
      }

      "restore board state" in {
        val restored = memento.restore()
        (0 until 24).foreach { pos =>
          restored.getPlayer(pos) should be(originalState.getPlayer(pos))
        }
      }
    }

    "created from complex game state" should {
      val board = Board.empty
        .set(0, Some(Player.White))
        .set(1, Some(Player.White))
        .set(2, Some(Player.White))
        .set(8, Some(Player.Black))
        .set(9, Some(Player.Black))

      val complexState = GameState(
        board = board,
        currentPlayer = Player.Black,
        phase = GameStateInterface.MovingPhase,
        whiteStonesToPlace = 0,
        blackStonesToPlace = 0,
        whiteStones = 7,
        blackStones = 6,
        message = Some(MoveStoneMessage.Success)
      )

      val memento = GameStateMemento.create(complexState)

      "preserve all properties" in {
        val restored = memento.restore()

        restored.currentPlayer should be(Player.Black)
        restored.phase should be(GameStateInterface.MovingPhase)
        restored.whiteStonesToPlace should be(0)
        restored.blackStonesToPlace should be(0)
        restored.whiteStones should be(7)
        restored.blackStones should be(6)
        restored.message should be(Some(MoveStoneMessage.Success))
      }

      "preserve board state exactly" in {
        val restored = memento.restore()

        restored.getPlayer(0) should be(Some(Player.White))
        restored.getPlayer(1) should be(Some(Player.White))
        restored.getPlayer(2) should be(Some(Player.White))
        restored.getPlayer(8) should be(Some(Player.Black))
        restored.getPlayer(9) should be(Some(Player.Black))
        restored.getPlayer(5) should be(None)
      }
    }

    "with different phases" should {
      "preserve PlacingPhase" in {
        val state = GameState.create(Board.empty, 5, 4, 9, 9)
        val memento = GameStateMemento.create(state)
        val restored = memento.restore()

        restored.phase should be(GameStateInterface.PlacingPhase)
        restored.whiteStonesToPlace should be(5)
        restored.blackStonesToPlace should be(4)
      }

      "preserve MovingPhase" in {
        val state = GameState(
          board = Board.empty,
          currentPlayer = Player.White,
          phase = GameStateInterface.MovingPhase,
          whiteStonesToPlace = 0,
          blackStonesToPlace = 0,
          whiteStones = 9,
          blackStones = 9,
          message = Some(MoveStoneMessage.Success)
        )

        val memento = GameStateMemento.create(state)
        val restored = memento.restore()

        restored.phase should be(GameStateInterface.MovingPhase)
      }

      "preserve MillRemovePhase" in {
        val state = GameState(
          board = Board.empty,
          currentPlayer = Player.White,
          phase = GameStateInterface.MillRemovePhase,
          whiteStonesToPlace = 3,
          blackStonesToPlace = 2,
          whiteStones = 9,
          blackStones = 9,
          message = Some(RemoveStoneMessage.Success)
        )

        val memento = GameStateMemento.create(state)
        val restored = memento.restore()

        restored.phase should be(GameStateInterface.MillRemovePhase)
      }
    }

    "with different messages" should {
      "preserve PlaceStoneMessage" in {
        val messages = List(
          PlaceStoneMessage.Success,
          PlaceStoneMessage.InvalidMove,
          PlaceStoneMessage.Occupied
        )

        messages.foreach { msg =>
          val state = GameState.create(Board.empty, 9, 9, 9, 9)
            .asInstanceOf[GameState].copy(message = Some(msg))
          val memento = GameStateMemento.create(state)
          val restored = memento.restore()

          restored.message should be(Some(msg))
        }
      }

      "preserve RemoveStoneMessage" in {
        val messages = List(
          RemoveStoneMessage.Success,
          RemoveStoneMessage.InvalidMove,
          RemoveStoneMessage.OwnStoneChosen
        )

        messages.foreach { msg =>
          val state = GameState.create(Board.empty, 9, 9, 9, 9)
            .asInstanceOf[GameState].copy(message = Some(msg))
          val memento = GameStateMemento.create(state)
          val restored = memento.restore()

          restored.message should be(Some(msg))
        }
      }

      "preserve MoveStoneMessage" in {
        val messages = List(
          MoveStoneMessage.Success,
          MoveStoneMessage.NoNeighbour,
          MoveStoneMessage.NeighbourOccupied,
          MoveStoneMessage.NotYourStone
        )

        messages.foreach { msg =>
          val state = GameState.create(Board.empty, 9, 9, 9, 9)
            .asInstanceOf[GameState].copy(message = Some(msg))
          val memento = GameStateMemento.create(state)
          val restored = memento.restore()

          restored.message should be(Some(msg))
        }
      }

      "preserve Winner message" in {
        val messages = List(Winner.White, Winner.Black)

        messages.foreach { msg =>
          val state = GameState.create(Board.empty, 9, 9, 9, 9)
            .asInstanceOf[GameState].copy(message = Some(msg))
          val memento = GameStateMemento.create(state)
          val restored = memento.restore()

          restored.message should be(Some(msg))
        }
      }

      "preserve None message" in {
        val state = GameState.create(Board.empty, 9, 9, 9, 9)
          .asInstanceOf[GameState].copy(message = None)
        val memento = GameStateMemento.create(state)
        val restored = memento.restore()

        restored.message should be(None)
      }
    }

    "with both players" should {
      "preserve White player" in {
        val state = GameState.create(Board.empty, 9, 9, 9, 9)
        val memento = GameStateMemento.create(state)
        val restored = memento.restore()

        restored.currentPlayer should be(Player.White)
      }

      "preserve Black player" in {
        val state = GameState.create(Board.empty, 9, 9, 9, 9)
          .asInstanceOf[GameState].copy(currentPlayer = Player.Black)
        val memento = GameStateMemento.create(state)
        val restored = memento.restore()

        restored.currentPlayer should be(Player.Black)
      }
    }

    "with different stone counts" should {
      "preserve stone counts" in {
        val state = GameState(
          board = Board.empty,
          currentPlayer = Player.White,
          phase = GameStateInterface.MovingPhase,
          whiteStonesToPlace = 0,
          blackStonesToPlace = 0,
          whiteStones = 5,
          blackStones = 3,
          message = None
        )

        val memento = GameStateMemento.create(state)
        val restored = memento.restore()

        restored.whiteStones should be(5)
        restored.blackStones should be(3)
      }

      "preserve stones to place" in {
        val state = GameState.create(Board.empty, 3, 7, 9, 9)

        val memento = GameStateMemento.create(state)
        val restored = memento.restore()

        restored.whiteStonesToPlace should be(3)
        restored.blackStonesToPlace should be(7)
      }
    }

    "immutability" should {
      "not affect original state when restored state is modified" in {
        val originalState = GameState.create(Board.empty, 9, 9, 9, 9)
        val memento = GameStateMemento.create(originalState)
        val restored = memento.restore()

        val modified = restored.asInstanceOf[GameState].copy(
          currentPlayer = Player.Black
        )

        val restoredAgain = memento.restore()
        restoredAgain.currentPlayer should be(Player.White)
      }
    }

    "multiple mementos" should {
      "be independent" in {
        val state1 = GameState.create(Board.empty, 9, 9, 9, 9)
        val state2 = state1.asInstanceOf[GameState].copy(
          board = state1.board.set(0, Some(Player.White))
        )

        val memento1 = GameStateMemento.create(state1)
        val memento2 = GameStateMemento.create(state2)

        val restored1 = memento1.restore()
        val restored2 = memento2.restore()

        restored1.getPlayer(0) should be(None)
        restored2.getPlayer(0) should be(Some(Player.White))
      }
    }

    "full board state" should {
      "preserve all positions" in {
        var board = Board.empty
        (0 until 24 by 2).foreach { i =>
          board = board.set(i, Some(Player.White))
        }
        (1 until 24 by 2).foreach { i =>
          board = board.set(i, Some(Player.Black))
        }

        val state = GameState(
          board = board,
          currentPlayer = Player.White,
          phase = GameStateInterface.MovingPhase,
          whiteStonesToPlace = 0,
          blackStonesToPlace = 0,
          whiteStones = 12,
          blackStones = 12,
          message = None
        )

        val memento = GameStateMemento.create(state)
        val restored = memento.restore()

        (0 until 24).foreach { pos =>
          restored.getPlayer(pos) should be(state.getPlayer(pos))
        }
      }
    }
  }
}
