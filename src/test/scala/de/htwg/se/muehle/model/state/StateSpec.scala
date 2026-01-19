package de.htwg.se.muehle.model.state

import org.scalatest.wordspec.AnyWordSpec
import org.scalatest.matchers.should.Matchers
import de.htwg.se.muehle.state.{StateP, StateInterface}
import de.htwg.se.muehle.gamestate.{GameState, GameStateInterface}
import de.htwg.se.muehle.board.Board
import de.htwg.se.muehle.player.Player
import de.htwg.se.muehle.model.*

class StateSpec extends AnyWordSpec with Matchers {

  "A StateP" when {
    val state = new StateP()

    "in PlacingPhase" should {
      val gameState = GameState.create(
        board = Board.empty,
        whiteStonesToPlace = 9,
        blackStonesToPlace = 9,
        whiteStones = 9,
        blackStones = 9
      )

      "place a white stone successfully" in {
        val newState = state.handle(gameState, 0)
        newState.getPlayer(0) should be(Some(Player.White))
        newState.whiteStonesToPlace should be(8)
        newState.currentPlayer should be(Player.Black)
        newState.message should be(Some(PlaceStoneMessage.Success))
      }

      "reject placement on occupied field" in {
        val withStone = gameState.asInstanceOf[GameState].copy(
          board = Board.empty.set(5, Some(Player.Black))
        )
        val newState = state.handle(withStone, 5)
        newState.message should be(Some(PlaceStoneMessage.Occupied))
        newState.whiteStonesToPlace should be(9)
      }

      "reject invalid position (negative)" in {
        val newState = state.handle(gameState, -1)
        newState.message should be(Some(PlaceStoneMessage.InvalidMove))
      }

      "reject invalid position (too high)" in {
        val newState = state.handle(gameState, 24)
        newState.message should be(Some(PlaceStoneMessage.InvalidMove))
      }

      "switch to MillRemovePhase when mill is formed" in {
        val boardWithTwoStones = Board.empty
          .set(0, Some(Player.White))
          .set(1, Some(Player.White))

        val gsBeforeMill = gameState.asInstanceOf[GameState].copy(
          board = boardWithTwoStones,
          whiteStonesToPlace = 8
        )

        val newState = state.handle(gsBeforeMill, 2)
        newState.phase should be(GameStateInterface.MillRemovePhase)
        newState.currentPlayer should be(Player.White)
        newState.message should be(Some(RemoveStoneMessage.Success))
      }

      "switch to MovingPhase when all stones are placed" in {
        val almostDone = gameState.asInstanceOf[GameState].copy(
          whiteStonesToPlace = 1,
          blackStonesToPlace = 0,
          board = Board.empty
        )

        val newState = state.handle(almostDone, 0)
        newState.phase should be(GameStateInterface.MovingPhase)
        newState.message should be(Some(MoveStoneMessage.Success))
      }

      "alternate players correctly" in {
        val afterWhite = state.handle(gameState, 0)
        afterWhite.currentPlayer should be(Player.Black)

        val afterBlack = state.handle(afterWhite, 8)
        afterBlack.currentPlayer should be(Player.White)
      }
    }

    "in MovingPhase" should {
      val movingGameState = GameState(
        board = Board.empty.set(0, Some(Player.White)),
        currentPlayer = Player.White,
        phase = GameStateInterface.MovingPhase,
        whiteStonesToPlace = 0,
        blackStonesToPlace = 0,
        whiteStones = 9,
        blackStones = 9,
        message = Some(MoveStoneMessage.Success)
      )

      "move a stone to neighbour successfully" in {
        val newState = state.handle(movingGameState, 0, 1)
        newState.getPlayer(0) should be(None)
        newState.getPlayer(1) should be(Some(Player.White))
        newState.currentPlayer should be(Player.Black)
        newState.message should be(Some(MoveStoneMessage.Success))
      }

      "reject move to non-neighbour" in {
        val newState = state.handle(movingGameState, 0, 5)
        newState.message should be(Some(MoveStoneMessage.NoNeighbour))
        newState.getPlayer(0) should be(Some(Player.White))
      }

      "reject move to occupied neighbour" in {
        val boardWithTwo = Board.empty
          .set(0, Some(Player.White))
          .set(1, Some(Player.Black))

        val gsWithOccupied = movingGameState.asInstanceOf[GameState].copy(
          board = boardWithTwo
        )

        val newState = state.handle(gsWithOccupied, 0, 1)
        newState.message should be(Some(MoveStoneMessage.NeighbourOccupied))
      }

      "reject move of opponent's stone" in {
        val boardWithBlack = Board.empty.set(8, Some(Player.Black))
        val gsWhiteTurn = movingGameState.asInstanceOf[GameState].copy(
          board = boardWithBlack,
          currentPlayer = Player.White
        )

        val newState = state.handle(gsWhiteTurn, 8, 9)
        newState.message should be(Some(MoveStoneMessage.NoNeighbour))
      }

      "reject move with invalid from position" in {
        val newState = state.handle(movingGameState, -1, 5)
        newState.message should be(Some(MoveStoneMessage.NoNeighbour))
      }

      "reject move with invalid to position" in {
        val newState = state.handle(movingGameState, 0, 24)
        newState.message should be(Some(MoveStoneMessage.NoNeighbour))
      }

      "switch to MillRemovePhase when mill is formed" in {
        val boardBeforeMill = Board.empty
          .set(0, Some(Player.White))
          .set(1, Some(Player.White))
          .set(3, Some(Player.White))

        val gsBeforeMill = movingGameState.asInstanceOf[GameState].copy(
          board = boardBeforeMill
        )

        val newState = state.handle(gsBeforeMill, 3, 2)
        newState.phase should be(GameStateInterface.MillRemovePhase)
        newState.currentPlayer should be(Player.White)
        newState.message should be(Some(RemoveStoneMessage.Success))
      }

      "reject move from empty position" in {
        val newState = state.handle(movingGameState, 5, 6)
        newState.message should be(Some(MoveStoneMessage.NoNeighbour))
      }
    }

    "in MillRemovePhase" should {
      val millRemoveState = GameState(
        board = Board.empty
          .set(0, Some(Player.White))
          .set(8, Some(Player.Black)),
        currentPlayer = Player.White,
        phase = GameStateInterface.MillRemovePhase,
        whiteStonesToPlace = 5,
        blackStonesToPlace = 5,
        whiteStones = 9,
        blackStones = 9,
        message = Some(RemoveStoneMessage.Success)
      )

      "remove opponent's stone successfully" in {
        val newState = state.handle(millRemoveState, 8)
        newState.getPlayer(8) should be(None)
        newState.blackStones should be(8)
        newState.currentPlayer should be(Player.Black)
        newState.phase should be(GameStateInterface.PlacingPhase)
        newState.message should be(Some(PlaceStoneMessage.Success))
      }

      "reject removing own stone" in {
        val newState = state.handle(millRemoveState, 0)
        newState.message should be(Some(RemoveStoneMessage.OwnStoneChosen))
        newState.getPlayer(0) should be(Some(Player.White))
      }

      "reject removing from empty position" in {
        val newState = state.handle(millRemoveState, 5)
        newState.message should be(Some(RemoveStoneMessage.InvalidMove))
      }

      "reject invalid position (negative)" in {
        val newState = state.handle(millRemoveState, -1)
        newState.message should be(Some(RemoveStoneMessage.InvalidMove))
      }

      "reject invalid position (too high)" in {
        val newState = state.handle(millRemoveState, 24)
        newState.message should be(Some(RemoveStoneMessage.InvalidMove))
      }

      "declare Black winner when White has 2 stones left" in {
        val almostWon = millRemoveState.asInstanceOf[GameState].copy(
          board = millRemoveState.board.set(8, None).set(16, Some(Player.White)),
          whiteStones = 3,
          currentPlayer = Player.Black
        )

        val newState = state.handle(almostWon, 16)
        newState.whiteStones should be(2)
        newState.message should be(Some(Winner.Black))
      }

      "declare White winner when Black has 2 stones left" in {
        val almostWon = millRemoveState.asInstanceOf[GameState].copy(
          currentPlayer = Player.White,
          blackStones = 3
        )

        val newState = state.handle(almostWon, 8)
        newState.blackStones should be(2)
        newState.message should be(Some(Winner.White))
      }

      "switch back to PlacingPhase when placing not done" in {
        val newState = state.handle(millRemoveState, 8)
        newState.phase should be(GameStateInterface.PlacingPhase)
        newState.message should be(Some(PlaceStoneMessage.Success))
      }

      "switch to MovingPhase when placing is done" in {
        val afterPlacing = millRemoveState.asInstanceOf[GameState].copy(
          whiteStonesToPlace = 0,
          blackStonesToPlace = 0
        )

        val newState = state.handle(afterPlacing, 8)
        newState.phase should be(GameStateInterface.MovingPhase)
        newState.message should be(Some(MoveStoneMessage.Success))
      }
    }

    "complex scenarios" should {
      "handle full game flow from placing to moving" in {
        var gs = GameState.create(Board.empty, 9, 9, 9, 9)

        val positions = List(0, 8, 3, 9, 5, 10, 12, 13, 14, 16, 17, 18, 19, 20, 21, 22, 23, 4)

        positions.foreach { pos =>
          if (!gs.isPlacingPhaseOver && gs.phase != GameStateInterface.MillRemovePhase) {
            gs = state.handle(gs, pos)
          }
          if (gs.phase == GameStateInterface.MillRemovePhase) {
            val opponentStones = (0 until 24).filter { p =>
              gs.getPlayer(p).exists(_ != gs.currentPlayer)
            }
            if (opponentStones.nonEmpty) {
              gs = state.handle(gs, opponentStones.head)
            }
          }
        }

        gs.isPlacingPhaseOver should be(true)
      }

      "maintain stone counts correctly" in {
        val gs = GameState.create(Board.empty, 9, 9, 9, 9)
        val afterPlace = state.handle(gs, 0)

        afterPlace.whiteStonesToPlace should be(8)
        afterPlace.whiteStones should be(9)
      }
    }
  }
}
