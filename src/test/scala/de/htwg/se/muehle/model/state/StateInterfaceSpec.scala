package de.htwg.se.muehle.state

import org.scalatest.wordspec.AnyWordSpec
import org.scalatest.matchers.should.Matchers
import de.htwg.se.muehle.gamestate.{GameState, GameStateInterface}
import de.htwg.se.muehle.board.Board
import de.htwg.se.muehle.player.Player
import de.htwg.se.muehle.model.*

class StateInterfaceSpec extends AnyWordSpec with Matchers {

  "A StateInterface" when {
    "used polymorphically" should {
      val stateHandler: StateInterface = new StateP

      "handle PlacingPhase correctly" in {
        val initialState = GameState.create(Board.empty, 9, 9, 9, 9)
        val result = stateHandler.handle(initialState, 0)

        result.getPlayer(0) should be(Some(Player.White))
        result.currentPlayer should be(Player.Black)
        result.whiteStonesToPlace should be(8)
      }

      "handle MovingPhase correctly" in {
        val board = Board.empty
          .set(0, Some(Player.White))
          .set(1, Some(Player.Black))

        val movingState = GameState(
          board = board,
          currentPlayer = Player.White,
          phase = GameStateInterface.MovingPhase,
          whiteStonesToPlace = 0,
          blackStonesToPlace = 0,
          whiteStones = 9,
          blackStones = 9,
          message = None
        )

        val result = stateHandler.handle(movingState, 0, 7)

        result.getPlayer(0) should be(None)
        result.getPlayer(7) should be(Some(Player.White))
        result.currentPlayer should be(Player.Black)
      }

      "handle MillRemovePhase correctly" in {
        val board = Board.empty
          .set(0, Some(Player.White))
          .set(8, Some(Player.Black))

        val removeState = GameState(
          board = board,
          currentPlayer = Player.White,
          phase = GameStateInterface.MillRemovePhase,
          whiteStonesToPlace = 0,
          blackStonesToPlace = 0,
          whiteStones = 9,
          blackStones = 9,
          message = Some(RemoveStoneMessage.Success)
        )

        val result = stateHandler.handle(removeState, 8)

        result.getPlayer(8) should be(None)
        result.blackStones should be(8)
        result.currentPlayer should be(Player.Black)
      }
    }

    "using default parameter" should {
      val stateHandler: StateInterface = new StateP

      "work with single position in PlacingPhase" in {
        val state = GameState.create(Board.empty, 9, 9, 9, 9)
        val result = stateHandler.handle(state, 5)

        result.getPlayer(5) should be(Some(Player.White))
        result.whiteStonesToPlace should be(8)
      }

      "work with single position in MillRemovePhase" in {
        val board = Board.empty.set(10, Some(Player.Black))
        val removeState = GameState(
          board = board,
          currentPlayer = Player.White,
          phase = GameStateInterface.MillRemovePhase,
          whiteStonesToPlace = 5,
          blackStonesToPlace = 4,
          whiteStones = 9,
          blackStones = 9,
          message = Some(RemoveStoneMessage.Success)
        )

        val result = stateHandler.handle(removeState, 10)

        result.getPlayer(10) should be(None)
        result.blackStones should be(8)
      }

      "use -1 as default for pos2" in {
        val state = GameState.create(Board.empty, 9, 9, 9, 9)
        val result1 = stateHandler.handle(state, 3)
        val result2 = stateHandler.handle(state, 3, -1)

        result1.getPlayer(3) should be(result2.getPlayer(3))
        result1.whiteStonesToPlace should be(result2.whiteStonesToPlace)
      }
    }

    "through interface contract" should {
      val stateHandler: StateInterface = new StateP

      "return GameStateInterface type" in {
        val state = GameState.create(Board.empty, 9, 9, 9, 9)
        val result = stateHandler.handle(state, 0)

        result.isInstanceOf[GameStateInterface] should be(true)
      }

      "accept GameStateInterface as input" in {
        val state: GameStateInterface = GameState.create(Board.empty, 9, 9, 9, 9)
        val result = stateHandler.handle(state, 0)

        result.getPlayer(0) should be(Some(Player.White))
      }

      "handle all valid positions 0-23" in {
        (0 until 24).foreach { pos =>
          val state = GameState.create(Board.empty, 9, 9, 9, 9)
          val result = stateHandler.handle(state, pos)

          result.getPlayer(pos) should be(Some(Player.White))
        }
      }
    }

    "validating PlacingPhase transitions" should {
      val stateHandler: StateInterface = new StateP

      "transition to PlacingPhase after successful placement" in {
        val state = GameState.create(Board.empty, 9, 9, 9, 9)
        val result = stateHandler.handle(state, 0)

        result.phase should be(GameStateInterface.PlacingPhase)
        result.message should be(Some(PlaceStoneMessage.Success))
      }

      "transition to MillRemovePhase on mill formation" in {
        val board = Board.empty
          .set(0, Some(Player.White))
          .set(1, Some(Player.White))

        val state = GameState(
          board = board,
          currentPlayer = Player.White,
          phase = GameStateInterface.PlacingPhase,
          whiteStonesToPlace = 7,
          blackStonesToPlace = 9,
          whiteStones = 9,
          blackStones = 9,
          message = None
        )

        val result = stateHandler.handle(state, 2)

        result.phase should be(GameStateInterface.MillRemovePhase)
        result.message should be(Some(RemoveStoneMessage.Success))
        result.currentPlayer should be(Player.White)
      }

      "transition to MovingPhase when all stones placed" in {
        val board = Board.empty
          .set(0, Some(Player.White))
          .set(1, Some(Player.Black))

        val state = GameState(
          board = board,
          currentPlayer = Player.White,
          phase = GameStateInterface.PlacingPhase,
          whiteStonesToPlace = 1,
          blackStonesToPlace = 0,
          whiteStones = 9,
          blackStones = 9,
          message = None
        )

        val result = stateHandler.handle(state, 7)

        result.phase should be(GameStateInterface.MovingPhase)
        result.message should be(Some(MoveStoneMessage.Success))
      }

      "reject invalid position < 0" in {
        val state = GameState.create(Board.empty, 9, 9, 9, 9)
        val result = stateHandler.handle(state, -1)

        result.message should be(Some(PlaceStoneMessage.InvalidMove))
        result.phase should be(GameStateInterface.PlacingPhase)
      }

      "reject invalid position >= 24" in {
        val state = GameState.create(Board.empty, 9, 9, 9, 9)
        val result = stateHandler.handle(state, 24)

        result.message should be(Some(PlaceStoneMessage.InvalidMove))
        result.phase should be(GameStateInterface.PlacingPhase)
      }

      "reject occupied position" in {
        val board = Board.empty.set(5, Some(Player.Black))
        val state = GameState(
          board = board,
          currentPlayer = Player.White,
          phase = GameStateInterface.PlacingPhase,
          whiteStonesToPlace = 9,
          blackStonesToPlace = 8,
          whiteStones = 9,
          blackStones = 9,
          message = None
        )

        val result = stateHandler.handle(state, 5)

        result.message should be(Some(PlaceStoneMessage.Occupied))
        result.getPlayer(5) should be(Some(Player.Black))
      }
    }

    "validating MovingPhase transitions" should {
      val stateHandler: StateInterface = new StateP

      "successfully move stone to neighbour" in {
        val board = Board.empty.set(0, Some(Player.White))
        val state = GameState(
          board = board,
          currentPlayer = Player.White,
          phase = GameStateInterface.MovingPhase,
          whiteStonesToPlace = 0,
          blackStonesToPlace = 0,
          whiteStones = 9,
          blackStones = 9,
          message = None
        )

        val result = stateHandler.handle(state, 0, 1)

        result.getPlayer(0) should be(None)
        result.getPlayer(1) should be(Some(Player.White))
        result.message should be(Some(MoveStoneMessage.Success))
        result.currentPlayer should be(Player.Black)
      }

      "transition to MillRemovePhase on mill formation" in {
        val board = Board.empty
          .set(0, Some(Player.White))
          .set(1, Some(Player.White))
          .set(3, Some(Player.White))

        val state = GameState(
          board = board,
          currentPlayer = Player.White,
          phase = GameStateInterface.MovingPhase,
          whiteStonesToPlace = 0,
          blackStonesToPlace = 0,
          whiteStones = 9,
          blackStones = 9,
          message = None
        )

        val result = stateHandler.handle(state, 3, 2)

        result.phase should be(GameStateInterface.MillRemovePhase)
        result.message should be(Some(RemoveStoneMessage.Success))
        result.currentPlayer should be(Player.White)
      }

      "reject move with invalid from position" in {
        val state = GameState(
          board = Board.empty,
          currentPlayer = Player.White,
          phase = GameStateInterface.MovingPhase,
          whiteStonesToPlace = 0,
          blackStonesToPlace = 0,
          whiteStones = 9,
          blackStones = 9,
          message = None
        )

        val result = stateHandler.handle(state, -1, 5)

        result.message should be(Some(MoveStoneMessage.NoNeighbour))
      }

      "reject move with invalid to position" in {
        val board = Board.empty.set(0, Some(Player.White))
        val state = GameState(
          board = board,
          currentPlayer = Player.White,
          phase = GameStateInterface.MovingPhase,
          whiteStonesToPlace = 0,
          blackStonesToPlace = 0,
          whiteStones = 9,
          blackStones = 9,
          message = None
        )

        val result = stateHandler.handle(state, 0, 25)

        result.message should be(Some(MoveStoneMessage.NoNeighbour))
      }

      "reject move to non-neighbour position" in {
        val board = Board.empty.set(0, Some(Player.White))
        val state = GameState(
          board = board,
          currentPlayer = Player.White,
          phase = GameStateInterface.MovingPhase,
          whiteStonesToPlace = 0,
          blackStonesToPlace = 0,
          whiteStones = 9,
          blackStones = 9,
          message = None
        )

        val result = stateHandler.handle(state, 0, 3)

        result.message should be(Some(MoveStoneMessage.NoNeighbour))
        result.getPlayer(0) should be(Some(Player.White))
      }

      "reject move to occupied position" in {
        val board = Board.empty
          .set(0, Some(Player.White))
          .set(1, Some(Player.Black))

        val state = GameState(
          board = board,
          currentPlayer = Player.White,
          phase = GameStateInterface.MovingPhase,
          whiteStonesToPlace = 0,
          blackStonesToPlace = 0,
          whiteStones = 9,
          blackStones = 9,
          message = None
        )

        val result = stateHandler.handle(state, 0, 1)

        result.message should be(Some(MoveStoneMessage.NeighbourOccupied))
        result.getPlayer(0) should be(Some(Player.White))
        result.getPlayer(1) should be(Some(Player.Black))
      }

      "reject move from empty position" in {
        val state = GameState(
          board = Board.empty,
          currentPlayer = Player.White,
          phase = GameStateInterface.MovingPhase,
          whiteStonesToPlace = 0,
          blackStonesToPlace = 0,
          whiteStones = 9,
          blackStones = 9,
          message = None
        )

        val result = stateHandler.handle(state, 0, 1)

        result.message should be(Some(MoveStoneMessage.NoNeighbour))
      }
    }

    "validating MillRemovePhase transitions" should {
      val stateHandler: StateInterface = new StateP

      "successfully remove opponent stone" in {
        val board = Board.empty.set(8, Some(Player.Black))
        val state = GameState(
          board = board,
          currentPlayer = Player.White,
          phase = GameStateInterface.MillRemovePhase,
          whiteStonesToPlace = 5,
          blackStonesToPlace = 4,
          whiteStones = 9,
          blackStones = 9,
          message = Some(RemoveStoneMessage.Success)
        )

        val result = stateHandler.handle(state, 8)

        result.getPlayer(8) should be(None)
        result.blackStones should be(8)
        result.currentPlayer should be(Player.Black)
      }

      "transition to PlacingPhase after remove during placing" in {
        val board = Board.empty.set(10, Some(Player.Black))
        val state = GameState(
          board = board,
          currentPlayer = Player.White,
          phase = GameStateInterface.MillRemovePhase,
          whiteStonesToPlace = 5,
          blackStonesToPlace = 4,
          whiteStones = 9,
          blackStones = 9,
          message = Some(RemoveStoneMessage.Success)
        )

        val result = stateHandler.handle(state, 10)

        result.phase should be(GameStateInterface.PlacingPhase)
        result.message should be(Some(PlaceStoneMessage.Success))
      }

      "transition to MovingPhase after remove during moving" in {
        val board = Board.empty.set(15, Some(Player.Black))
        val state = GameState(
          board = board,
          currentPlayer = Player.White,
          phase = GameStateInterface.MillRemovePhase,
          whiteStonesToPlace = 0,
          blackStonesToPlace = 0,
          whiteStones = 9,
          blackStones = 9,
          message = Some(RemoveStoneMessage.Success)
        )

        val result = stateHandler.handle(state, 15)

        result.phase should be(GameStateInterface.MovingPhase)
        result.message should be(Some(MoveStoneMessage.Success))
      }

      "detect Black winner when White has 2 stones" in {
        val board = Board.empty
          .set(0, Some(Player.White))
          .set(1, Some(Player.White))
          .set(2, Some(Player.White))

        val state = GameState(
          board = board,
          currentPlayer = Player.Black,
          phase = GameStateInterface.MillRemovePhase,
          whiteStonesToPlace = 0,
          blackStonesToPlace = 0,
          whiteStones = 3,
          blackStones = 9,
          message = Some(RemoveStoneMessage.Success)
        )

        val result = stateHandler.handle(state, 0)

        result.whiteStones should be(2)
        result.message should be(Some(Winner.Black))
      }

      "detect White winner when Black has 2 stones" in {
        val board = Board.empty
          .set(8, Some(Player.Black))
          .set(9, Some(Player.Black))
          .set(10, Some(Player.Black))

        val state = GameState(
          board = board,
          currentPlayer = Player.White,
          phase = GameStateInterface.MillRemovePhase,
          whiteStonesToPlace = 0,
          blackStonesToPlace = 0,
          whiteStones = 9,
          blackStones = 3,
          message = Some(RemoveStoneMessage.Success)
        )

        val result = stateHandler.handle(state, 8)

        result.blackStones should be(2)
        result.message should be(Some(Winner.White))
      }

      "reject removing own stone" in {
        val board = Board.empty.set(5, Some(Player.White))
        val state = GameState(
          board = board,
          currentPlayer = Player.White,
          phase = GameStateInterface.MillRemovePhase,
          whiteStonesToPlace = 5,
          blackStonesToPlace = 4,
          whiteStones = 9,
          blackStones = 9,
          message = Some(RemoveStoneMessage.Success)
        )

        val result = stateHandler.handle(state, 5)

        result.message should be(Some(RemoveStoneMessage.OwnStoneChosen))
        result.getPlayer(5) should be(Some(Player.White))
      }

      "reject removing from empty position" in {
        val state = GameState(
          board = Board.empty,
          currentPlayer = Player.White,
          phase = GameStateInterface.MillRemovePhase,
          whiteStonesToPlace = 5,
          blackStonesToPlace = 4,
          whiteStones = 9,
          blackStones = 9,
          message = Some(RemoveStoneMessage.Success)
        )

        val result = stateHandler.handle(state, 7)

        result.message should be(Some(RemoveStoneMessage.InvalidMove))
      }

      "reject invalid position < 0" in {
        val state = GameState(
          board = Board.empty,
          currentPlayer = Player.White,
          phase = GameStateInterface.MillRemovePhase,
          whiteStonesToPlace = 5,
          blackStonesToPlace = 4,
          whiteStones = 9,
          blackStones = 9,
          message = Some(RemoveStoneMessage.Success)
        )

        val result = stateHandler.handle(state, -1)

        result.message should be(Some(RemoveStoneMessage.InvalidMove))
      }

      "reject invalid position >= 24" in {
        val state = GameState(
          board = Board.empty,
          currentPlayer = Player.White,
          phase = GameStateInterface.MillRemovePhase,
          whiteStonesToPlace = 5,
          blackStonesToPlace = 4,
          whiteStones = 9,
          blackStones = 9,
          message = Some(RemoveStoneMessage.Success)
        )

        val result = stateHandler.handle(state, 30)

        result.message should be(Some(RemoveStoneMessage.InvalidMove))
      }
    }

    "with different implementations" should {
      "allow multiple implementations of StateInterface" in {
        class CustomState extends StateInterface {
          override def handle(gs: GameStateInterface, pos1: Int, pos2: Int = -1): GameStateInterface = {
            gs
          }
        }

        val customHandler: StateInterface = new CustomState
        val state = GameState.create(Board.empty, 9, 9, 9, 9)
        val result = customHandler.handle(state, 0)

        result should be(state)
      }

      "support polymorphic usage" in {
        val handlers: List[StateInterface] = List(new StateP, new StateP)

        handlers.foreach { handler =>
          val state = GameState.create(Board.empty, 9, 9, 9, 9)
          val result = handler.handle(state, 0)

          result.getPlayer(0) should be(Some(Player.White))
        }
      }
    }

    "edge cases" should {
      val stateHandler: StateInterface = new StateP

      "handle position 0 correctly" in {
        val state = GameState.create(Board.empty, 9, 9, 9, 9)
        val result = stateHandler.handle(state, 0)

        result.getPlayer(0) should be(Some(Player.White))
      }

      "handle position 23 correctly" in {
        val state = GameState.create(Board.empty, 9, 9, 9, 9)
        val result = stateHandler.handle(state, 23)

        result.getPlayer(23) should be(Some(Player.White))
      }

      "handle alternating players correctly" in {
        val state = GameState.create(Board.empty, 9, 9, 9, 9)

        val after1 = stateHandler.handle(state, 0)
        after1.currentPlayer should be(Player.Black)

        val after2 = stateHandler.handle(after1, 1)
        after2.currentPlayer should be(Player.White)

        val after3 = stateHandler.handle(after2, 2)
        after3.currentPlayer should be(Player.Black)
      }

      "decrement stones to place correctly" in {
        val state = GameState.create(Board.empty, 9, 9, 9, 9)

        val after1 = stateHandler.handle(state, 0)
        after1.whiteStonesToPlace should be(8)
        after1.blackStonesToPlace should be(9)

        val after2 = stateHandler.handle(after1, 1)
        after2.whiteStonesToPlace should be(8)
        after2.blackStonesToPlace should be(8)
      }
    }

    "stress testing" should {
      val stateHandler: StateInterface = new StateP

      "handle rapid consecutive calls" in {
        var state = GameState.create(Board.empty, 9, 9, 9, 9)

        (0 until 10).foreach { i =>
          state = stateHandler.handle(state, i)
          state.getPlayer(i) should not be None
        }
      }

      "handle all board positions sequentially" in {
        var state = GameState.create(Board.empty, 18, 18, 18, 18)

        (0 until 18).foreach { i =>
          state = stateHandler.handle(state, i)
        }

        (0 until 18).foreach { i =>
          state.getPlayer(i) should not be None
        }
      }
    }
  }
}
