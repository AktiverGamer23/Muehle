package de.htwg.se.muehle.util

import org.scalatest.wordspec.AnyWordSpec
import org.scalatest.matchers.should.Matchers
import de.htwg.se.muehle.gamestate.{GameState, GameStateInterface}
import de.htwg.se.muehle.board.Board
import de.htwg.se.muehle.player.Player

class CommandSpec extends AnyWordSpec with Matchers {

  "A GameStateCommand" when {
    val initialState = GameState.create(Board.empty, 9, 9, 9, 9)

    "created with an action" should {
      val action: GameStateInterface => GameStateInterface = { gs =>
        gs.asInstanceOf[GameState].copy(
          board = gs.board.set(0, Some(Player.White)),
          whiteStonesToPlace = gs.whiteStonesToPlace - 1
        )
      }

      val command = new GameStateCommand(initialState, action)

      "execute the action on doStep" in {
        val newState = command.doStep
        newState.getPlayer(0) should be(Some(Player.White))
        newState.whiteStonesToPlace should be(8)
      }

      "return old state on undoStep" in {
        command.doStep
        val undoneState = command.undoStep

        undoneState.getPlayer(0) should be(None)
        undoneState.whiteStonesToPlace should be(9)
        undoneState should be(initialState)
      }

      "return new state on redoStep after doStep" in {
        val newState = command.doStep
        val redoneState = command.redoStep

        redoneState.getPlayer(0) should be(Some(Player.White))
        redoneState.whiteStonesToPlace should be(8)
        redoneState should be(newState)
      }

      "return old state on redoStep before doStep" in {
        val cmd = new GameStateCommand(initialState, action)
        val redoneState = cmd.redoStep

        redoneState should be(initialState)
      }
    }

    "executing multiple times" should {
      val action: GameStateInterface => GameStateInterface = { gs =>
        gs.asInstanceOf[GameState].copy(currentPlayer = gs.currentPlayer.next)
      }

      val command = new GameStateCommand(initialState, action)

      "store the result" in {
        val result1 = command.doStep
        result1.currentPlayer should be(Player.Black)

        val result2 = command.doStep
        result2.currentPlayer should be(Player.Black)
      }

      "always return same undo state" in {
        command.doStep
        val undo1 = command.undoStep
        val undo2 = command.undoStep

        undo1 should be(initialState)
        undo2 should be(initialState)
      }
    }

    "with complex state changes" should {
      val complexAction: GameStateInterface => GameStateInterface = { gs =>
        val newBoard = gs.board
          .set(0, Some(Player.White))
          .set(1, Some(Player.White))
          .set(2, Some(Player.White))

        gs.asInstanceOf[GameState].copy(
          board = newBoard,
          whiteStonesToPlace = gs.whiteStonesToPlace - 3,
          currentPlayer = Player.Black
        )
      }

      val command = new GameStateCommand(initialState, complexAction)

      "execute complex action correctly" in {
        val newState = command.doStep

        newState.getPlayer(0) should be(Some(Player.White))
        newState.getPlayer(1) should be(Some(Player.White))
        newState.getPlayer(2) should be(Some(Player.White))
        newState.whiteStonesToPlace should be(6)
        newState.currentPlayer should be(Player.Black)
      }

      "undo complex action correctly" in {
        command.doStep
        val undoneState = command.undoStep

        undoneState.getPlayer(0) should be(None)
        undoneState.getPlayer(1) should be(None)
        undoneState.getPlayer(2) should be(None)
        undoneState.whiteStonesToPlace should be(9)
        undoneState.currentPlayer should be(Player.White)
      }

      "redo complex action correctly" in {
        val newState = command.doStep
        val redoneState = command.redoStep

        redoneState.getPlayer(0) should be(Some(Player.White))
        redoneState.getPlayer(1) should be(Some(Player.White))
        redoneState.getPlayer(2) should be(Some(Player.White))
        redoneState.whiteStonesToPlace should be(6)
        redoneState.currentPlayer should be(Player.Black)
      }
    }

    "with different initial states" should {
      val stateWithStones = GameState(
        board = Board.empty.set(5, Some(Player.Black)),
        currentPlayer = Player.Black,
        phase = GameStateInterface.PlacingPhase,
        whiteStonesToPlace = 7,
        blackStonesToPlace = 8,
        whiteStones = 9,
        blackStones = 9,
        message = None
      )

      val action: GameStateInterface => GameStateInterface = { gs =>
        gs.asInstanceOf[GameState].copy(
          board = gs.board.set(8, Some(Player.Black)),
          blackStonesToPlace = gs.blackStonesToPlace - 1
        )
      }

      val command = new GameStateCommand(stateWithStones, action)

      "work from non-initial state" in {
        val newState = command.doStep
        newState.getPlayer(5) should be(Some(Player.Black))
        newState.getPlayer(8) should be(Some(Player.Black))
        newState.blackStonesToPlace should be(7)
      }

      "undo to correct previous state" in {
        command.doStep
        val undoneState = command.undoStep

        undoneState.getPlayer(5) should be(Some(Player.Black))
        undoneState.getPlayer(8) should be(None)
        undoneState.blackStonesToPlace should be(8)
      }
    }

    "with no-op action" should {
      val noOpAction: GameStateInterface => GameStateInterface = identity

      val command = new GameStateCommand(initialState, noOpAction)

      "return same state on doStep" in {
        val newState = command.doStep
        newState should be(initialState)
      }

      "return same state on undoStep" in {
        command.doStep
        val undoneState = command.undoStep
        undoneState should be(initialState)
      }

      "return same state on redoStep" in {
        command.doStep
        val redoneState = command.redoStep
        redoneState should be(initialState)
      }
    }

    "Command trait implementation" should {
      val action: GameStateInterface => GameStateInterface = { gs =>
        gs.asInstanceOf[GameState].copy(currentPlayer = Player.Black)
      }

      val command: Command = new GameStateCommand(initialState, action)

      "implement doStep" in {
        val result = command.doStep
        result.isInstanceOf[GameStateInterface] should be(true)
      }

      "implement undoStep" in {
        command.doStep
        val result = command.undoStep
        result.isInstanceOf[GameStateInterface] should be(true)
      }

      "implement redoStep" in {
        command.doStep
        val result = command.redoStep
        result.isInstanceOf[GameStateInterface] should be(true)
      }
    }
  }
}
