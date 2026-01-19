package de.htwg.se.muehle.model.gamestate

import org.scalatest.wordspec.AnyWordSpec
import org.scalatest.matchers.should.Matchers
import de.htwg.se.muehle.gamestate.{GameState, GameStateInterface}
import de.htwg.se.muehle.board.{Board, BoardInterface}
import de.htwg.se.muehle.player.Player
import de.htwg.se.muehle.model.*

class GameStateSpec extends AnyWordSpec with Matchers {

  "A GameState" when {
    "created with create method" should {
      val gameState = GameState.create(
        board = Board.empty,
        whiteStonesToPlace = 9,
        blackStonesToPlace = 9,
        whiteStones = 9,
        blackStones = 9
      )

      "start with White player" in {
        gameState.currentPlayer should be(Player.White)
      }

      "start in PlacingPhase" in {
        gameState.phase should be(GameStateInterface.PlacingPhase)
      }

      "have correct initial stones to place" in {
        gameState.whiteStonesToPlace should be(9)
        gameState.blackStonesToPlace should be(9)
      }

      "have correct total stones" in {
        gameState.whiteStones should be(9)
        gameState.blackStones should be(9)
      }

      "have PlaceStoneMessage.Success as initial message" in {
        gameState.message should be(Some(PlaceStoneMessage.Success))
      }

      "have empty board" in {
        (0 until 24).foreach { pos =>
          gameState.getPlayer(pos) should be(None)
        }
      }
    }

    "using getPlayer" should {
      val board = Board.empty
        .set(0, Some(Player.White))
        .set(8, Some(Player.Black))

      val gameState = GameState(
        board = board,
        currentPlayer = Player.White,
        phase = GameStateInterface.PlacingPhase,
        whiteStonesToPlace = 8,
        blackStonesToPlace = 8,
        whiteStones = 9,
        blackStones = 9,
        message = None
      )

      "return correct player at position" in {
        gameState.getPlayer(0) should be(Some(Player.White))
        gameState.getPlayer(8) should be(Some(Player.Black))
      }

      "return None for empty position" in {
        gameState.getPlayer(5) should be(None)
      }
    }

    "checking placing phase" should {
      "return false when stones still to place" in {
        val gameState = GameState.create(
          board = Board.empty,
          whiteStonesToPlace = 5,
          blackStonesToPlace = 3,
          whiteStones = 9,
          blackStones = 9
        )

        gameState.isPlacingPhaseOver should be(false)
      }

      "return true when all stones placed" in {
        val gameState = GameState.create(
          board = Board.empty,
          whiteStonesToPlace = 0,
          blackStonesToPlace = 0,
          whiteStones = 9,
          blackStones = 9
        )

        gameState.isPlacingPhaseOver should be(true)
      }

      "return false when only white has stones to place" in {
        val gameState = GameState.create(
          board = Board.empty,
          whiteStonesToPlace = 1,
          blackStonesToPlace = 0,
          whiteStones = 9,
          blackStones = 9
        )

        gameState.isPlacingPhaseOver should be(false)
      }

      "return false when only black has stones to place" in {
        val gameState = GameState.create(
          board = Board.empty,
          whiteStonesToPlace = 0,
          blackStonesToPlace = 2,
          whiteStones = 9,
          blackStones = 9
        )

        gameState.isPlacingPhaseOver should be(false)
      }
    }

    "created directly" should {
      val board = Board.empty.set(5, Some(Player.Black))
      val gameState = GameState(
        board = board,
        currentPlayer = Player.Black,
        phase = GameStateInterface.MovingPhase,
        whiteStonesToPlace = 0,
        blackStonesToPlace = 0,
        whiteStones = 7,
        blackStones = 6,
        message = Some(MoveStoneMessage.Success)
      )

      "have correct properties" in {
        gameState.currentPlayer should be(Player.Black)
        gameState.phase should be(GameStateInterface.MovingPhase)
        gameState.whiteStonesToPlace should be(0)
        gameState.blackStonesToPlace should be(0)
        gameState.whiteStones should be(7)
        gameState.blackStones should be(6)
        gameState.message should be(Some(MoveStoneMessage.Success))
        gameState.getPlayer(5) should be(Some(Player.Black))
      }
    }

    "with different phases" should {
      "work in MillRemovePhase" in {
        val gameState = GameState(
          board = Board.empty,
          currentPlayer = Player.White,
          phase = GameStateInterface.MillRemovePhase,
          whiteStonesToPlace = 0,
          blackStonesToPlace = 0,
          whiteStones = 9,
          blackStones = 9,
          message = Some(RemoveStoneMessage.Success)
        )

        gameState.phase should be(GameStateInterface.MillRemovePhase)
        gameState.message should be(Some(RemoveStoneMessage.Success))
      }
    }

    "immutability" should {
      "be a case class" in {
        val gs1 = GameState.create(Board.empty, 9, 9, 9, 9)
        val gs2 = gs1.asInstanceOf[GameState].copy(currentPlayer = Player.Black)

        gs1.currentPlayer should be(Player.White)
        gs2.currentPlayer should be(Player.Black)
      }
    }
  }
}
