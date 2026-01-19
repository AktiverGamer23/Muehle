package de.htwg.se.muehle.controller

import org.scalatest.wordspec.AnyWordSpec
import org.scalatest.matchers.should.Matchers
import de.htwg.se.muehle.controller.GameController
import de.htwg.se.muehle.gamestate.{GameState, GameStateInterface}
import de.htwg.se.muehle.board.Board
import de.htwg.se.muehle.state.StateP
import de.htwg.se.muehle.ai.{BotSelector, RandomBot}
import de.htwg.se.muehle.fileio.JsonFileIO
import de.htwg.se.muehle.player.Player
import de.htwg.se.muehle.util.Observer
import java.io.File
import scala.util.{Try, Success, Failure}

class GameControllerSpec extends AnyWordSpec with Matchers {

  "A GameController (Command Pattern)" when {
    val initialState = GameState.create(Board.empty, 9, 9, 9, 9)
    val state = new StateP()
    val fileIO = new JsonFileIO()

    "created" should {
      val botSelector = new BotSelector()
      val controller = new GameController(initialState, state, botSelector, fileIO)

      "have initial game state" in {
        controller.getGameState.currentPlayer should be(Player.White)
        controller.getGameState.phase should be(GameStateInterface.PlacingPhase)
      }
    }

    "handling a move" should {
      val botSelector = new BotSelector()
      val controller = new GameController(initialState, state, botSelector, fileIO)

      "update game state" in {
        controller.handle(0)
        controller.getGameState.getPlayer(0) should be(Some(Player.White))
      }
    }

    "undoing a move" should {
      val botSelector = new BotSelector()
      val controller = new GameController(initialState, state, botSelector, fileIO)
      controller.handle(0)

      controller.undo()

      "restore previous state" in {
        controller.getGameState.currentPlayer should be(Player.White)
        controller.getGameState.getPlayer(0) should be(None)
      }
    }

    "redoing a move" should {
      val botSelector = new BotSelector()
      val controller = new GameController(initialState, state, botSelector, fileIO)
      controller.handle(0)
      controller.undo()

      controller.redo()

      "restore the undone move" in {
        controller.getGameState.getPlayer(0) should be(Some(Player.White))
      }
    }

    "handling multiple moves" should {
      val botSelector = new BotSelector()
      val controller = new GameController(initialState, state, botSelector, fileIO)

      "track all moves" in {
        controller.handle(0)
        controller.handle(8)
        controller.handle(1)

        controller.getGameState.getPlayer(0) should be(Some(Player.White))
        controller.getGameState.getPlayer(8) should be(Some(Player.Black))
        controller.getGameState.getPlayer(1) should be(Some(Player.White))
      }

      "undo multiple times" in {
        controller.undo()
        controller.getGameState.getPlayer(1) should be(None)

        controller.undo()
        controller.getGameState.getPlayer(8) should be(None)

        controller.undo()
        controller.getGameState.getPlayer(0) should be(None)
      }
    }

    "undo without history" should {
      val botSelector = new BotSelector()
      val controller = new GameController(initialState, state, botSelector, fileIO)

      "not change state" in {
        val stateBefore = controller.getGameState
        controller.undo()
        controller.getGameState should be(stateBefore)
      }
    }

    "redo without redo stack" should {
      val botSelector = new BotSelector()
      val controller = new GameController(initialState, state, botSelector, fileIO)

      "not change state" in {
        val stateBefore = controller.getGameState
        controller.redo()
        controller.getGameState should be(stateBefore)
      }
    }

    "restarting game" should {
      val botSelector = new BotSelector()
      val controller = new GameController(initialState, state, botSelector, fileIO)
      controller.handle(0)
      controller.handle(8)

      controller.restart()

      "reset to initial state" in {
        controller.getGameState.currentPlayer should be(Player.White)
        controller.getGameState.phase should be(GameStateInterface.PlacingPhase)
        (0 until 24).foreach { pos =>
          controller.getGameState.getPlayer(pos) should be(None)
        }
      }
    }

    "saving and loading" should {
      val botSelector = new BotSelector()
      val controller = new GameController(initialState, state, botSelector, fileIO)
      controller.handle(0)
      controller.handle(8)

      val tempFile = File.createTempFile("muehle_test_cmd", ".json")
      tempFile.deleteOnExit()
      val filePath = tempFile.getAbsolutePath

      "save successfully" in {
        val result = controller.save(filePath)
        result.isSuccess should be(true)
      }

      "load successfully" in {
        val newController = new GameController(initialState, state, botSelector, fileIO)
        val result = newController.load(filePath)

        result.isSuccess should be(true)
        newController.getGameState.getPlayer(0) should be(Some(Player.White))
        newController.getGameState.getPlayer(8) should be(Some(Player.Black))
      }
    }

    "with bot enabled" should {
      val botSelector = new BotSelector()
      val controller = new GameController(initialState, state, botSelector, fileIO)
      botSelector.setStrategy(new RandomBot())

      "trigger bot move after human move" in {
        controller.handle(0)
        controller.getGameState.currentPlayer should be(Player.White)
      }
    }

    "observing changes" should {
      val botSelector = new BotSelector()
      val controller = new GameController(initialState, state, botSelector, fileIO)
      var updateCount = 0

      val observer = new Observer {
        override def update: Unit = updateCount += 1
      }

      controller.add(observer)

      "notify observers on handle" in {
        controller.handle(0)
        updateCount should be > 0
      }

      "notify observers on undo" in {
        val countBefore = updateCount
        controller.undo()
        updateCount should be > countBefore
      }

      "notify observers on redo" in {
        val countBefore = updateCount
        controller.redo()
        updateCount should be > countBefore
      }

      "notify observers on restart" in {
        val countBefore = updateCount
        controller.restart()
        updateCount should be > countBefore
      }

      "notify observers on load" in {
        val tempFile = File.createTempFile("muehle_test_cmd", ".json")
        tempFile.deleteOnExit()
        controller.save(tempFile.getAbsolutePath)

        val countBefore = updateCount
        controller.load(tempFile.getAbsolutePath)
        updateCount should be > countBefore
      }

      "stop notifying after removal" in {
        controller.remove(observer)
        val countBefore = updateCount
        controller.handle(1)
        updateCount should be(countBefore)
      }
    }

    "loading non-existent file" should {
      val botSelector = new BotSelector()
      val controller = new GameController(initialState, state, botSelector, fileIO)

      "return failure" in {
        val result = controller.load("/non/existent/path.json")
        result.isFailure should be(true)
      }
    }

    "complex undo/redo scenario" should {
      val botSelector = new BotSelector()
      val controller = new GameController(initialState, state, botSelector, fileIO)

      "clear redo stack on new move after undo" in {
        controller.handle(0)
        controller.undo()
        controller.handle(9)

        controller.redo()
        controller.getGameState.getPlayer(0) should be(None)
      }
    }
  }
}
