package de.htwg.se.muehle.fileio

import org.scalatest.wordspec.AnyWordSpec
import org.scalatest.matchers.should.Matchers
import de.htwg.se.muehle.fileio.JsonFileIO
import de.htwg.se.muehle.gamestate.{GameState, GameStateInterface}
import de.htwg.se.muehle.board.Board
import de.htwg.se.muehle.player.Player
import de.htwg.se.muehle.model.*
import java.io.File
import scala.util.{Success, Failure}

class JsonFileIOSpec extends AnyWordSpec with Matchers {

  "A JsonFileIO" when {
    val fileIO = new JsonFileIO()

    "saving a game state" should {
      val gameState = GameState.create(Board.empty, 9, 9, 9, 9)
      val tempFile = File.createTempFile("muehle_json_test", ".json")
      tempFile.deleteOnExit()
      val filePath = tempFile.getAbsolutePath

      "save successfully" in {
        val result = fileIO.save(gameState, filePath)
        result.isSuccess should be(true)
      }

      "create a file" in {
        fileIO.save(gameState, filePath)
        tempFile.exists() should be(true)
      }

      "create valid JSON content" in {
        fileIO.save(gameState, filePath)
        val content = scala.io.Source.fromFile(tempFile).mkString
        content should include("board")
        content should include("currentPlayer")
        content should include("phase")
      }
    }

    "loading a game state" should {
      val originalState = GameState(
        board = Board.empty.set(0, Some(Player.White)).set(8, Some(Player.Black)),
        currentPlayer = Player.Black,
        phase = GameStateInterface.MovingPhase,
        whiteStonesToPlace = 0,
        blackStonesToPlace = 0,
        whiteStones = 7,
        blackStones = 6,
        message = Some(MoveStoneMessage.Success)
      )

      val tempFile = File.createTempFile("muehle_json_load_test", ".json")
      tempFile.deleteOnExit()
      val filePath = tempFile.getAbsolutePath

      fileIO.save(originalState, filePath)

      "load successfully" in {
        val result = fileIO.load(filePath)
        result.isSuccess should be(true)
      }

      "restore correct game state" in {
        val loadedState = fileIO.load(filePath).get

        loadedState.currentPlayer should be(Player.Black)
        loadedState.phase should be(GameStateInterface.MovingPhase)
        loadedState.whiteStonesToPlace should be(0)
        loadedState.blackStonesToPlace should be(0)
        loadedState.whiteStones should be(7)
        loadedState.blackStones should be(6)
      }

      "restore board state" in {
        val loadedState = fileIO.load(filePath).get

        loadedState.getPlayer(0) should be(Some(Player.White))
        loadedState.getPlayer(8) should be(Some(Player.Black))
        loadedState.getPlayer(5) should be(None)
      }

      "restore message" in {
        val loadedState = fileIO.load(filePath).get
        loadedState.message should be(Some(MoveStoneMessage.Success))
      }
    }

    "with different phases" should {
      "save and load PlacingPhase" in {
        val state = GameState.create(Board.empty, 5, 4, 9, 9)
        val tempFile = File.createTempFile("muehle_placing", ".json")
        tempFile.deleteOnExit()

        fileIO.save(state, tempFile.getAbsolutePath)
        val loaded = fileIO.load(tempFile.getAbsolutePath).get

        loaded.phase should be(GameStateInterface.PlacingPhase)
      }

      "save and load MillRemovePhase" in {
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

        val tempFile = File.createTempFile("muehle_mill", ".json")
        tempFile.deleteOnExit()

        fileIO.save(state, tempFile.getAbsolutePath)
        val loaded = fileIO.load(tempFile.getAbsolutePath).get

        loaded.phase should be(GameStateInterface.MillRemovePhase)
      }
    }

    "with different messages" should {
      "save and load PlaceStoneMessage.Success" in {
        val state = GameState.create(Board.empty, 9, 9, 9, 9)
        val tempFile = File.createTempFile("muehle_msg1", ".json")
        tempFile.deleteOnExit()

        fileIO.save(state, tempFile.getAbsolutePath)
        val loaded = fileIO.load(tempFile.getAbsolutePath).get

        loaded.message should be(Some(PlaceStoneMessage.Success))
      }

      "save and load PlaceStoneMessage.Occupied" in {
        val state = GameState(
          board = Board.empty,
          currentPlayer = Player.White,
          phase = GameStateInterface.PlacingPhase,
          whiteStonesToPlace = 9,
          blackStonesToPlace = 9,
          whiteStones = 9,
          blackStones = 9,
          message = Some(PlaceStoneMessage.Occupied)
        )

        val tempFile = File.createTempFile("muehle_msg2", ".json")
        tempFile.deleteOnExit()

        fileIO.save(state, tempFile.getAbsolutePath)
        val loaded = fileIO.load(tempFile.getAbsolutePath).get

        loaded.message should be(Some(PlaceStoneMessage.Occupied))
      }

      "save and load Winner.White" in {
        val state = GameState(
          board = Board.empty,
          currentPlayer = Player.Black,
          phase = GameStateInterface.MovingPhase,
          whiteStonesToPlace = 0,
          blackStonesToPlace = 0,
          whiteStones = 5,
          blackStones = 2,
          message = Some(Winner.White)
        )

        val tempFile = File.createTempFile("muehle_winner", ".json")
        tempFile.deleteOnExit()

        fileIO.save(state, tempFile.getAbsolutePath)
        val loaded = fileIO.load(tempFile.getAbsolutePath).get

        loaded.message should be(Some(Winner.White))
      }

      "save and load all RemoveStoneMessages" in {
        val messages = List(
          RemoveStoneMessage.Success,
          RemoveStoneMessage.InvalidMove,
          RemoveStoneMessage.OwnStoneChosen
        )

        messages.foreach { msg =>
          val state = GameState(
            board = Board.empty,
            currentPlayer = Player.White,
            phase = GameStateInterface.MillRemovePhase,
            whiteStonesToPlace = 0,
            blackStonesToPlace = 0,
            whiteStones = 9,
            blackStones = 9,
            message = Some(msg)
          )

          val tempFile = File.createTempFile("muehle_remove_msg", ".json")
          tempFile.deleteOnExit()

          fileIO.save(state, tempFile.getAbsolutePath)
          val loaded = fileIO.load(tempFile.getAbsolutePath).get

          loaded.message should be(Some(msg))
        }
      }

      "save and load all MoveStoneMessages" in {
        val messages = List(
          MoveStoneMessage.Success,
          MoveStoneMessage.NoNeighbour,
          MoveStoneMessage.NeighbourOccupied,
          MoveStoneMessage.NotYourStone
        )

        messages.foreach { msg =>
          val state = GameState(
            board = Board.empty,
            currentPlayer = Player.White,
            phase = GameStateInterface.MovingPhase,
            whiteStonesToPlace = 0,
            blackStonesToPlace = 0,
            whiteStones = 9,
            blackStones = 9,
            message = Some(msg)
          )

          val tempFile = File.createTempFile("muehle_move_msg", ".json")
          tempFile.deleteOnExit()

          fileIO.save(state, tempFile.getAbsolutePath)
          val loaded = fileIO.load(tempFile.getAbsolutePath).get

          loaded.message should be(Some(msg))
        }
      }
    }

    "with complex board states" should {
      "save and load full board" in {
        var board = Board.empty
        (0 until 24).foreach { i =>
          val player = if (i % 2 == 0) Some(Player.White) else Some(Player.Black)
          board = board.set(i, player)
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

        val tempFile = File.createTempFile("muehle_full_board", ".json")
        tempFile.deleteOnExit()

        fileIO.save(state, tempFile.getAbsolutePath)
        val loaded = fileIO.load(tempFile.getAbsolutePath).get

        (0 until 24).foreach { i =>
          val expected = if (i % 2 == 0) Some(Player.White) else Some(Player.Black)
          loaded.getPlayer(i) should be(expected)
        }
      }

      "save and load sparse board" in {
        val board = Board.empty
          .set(0, Some(Player.White))
          .set(5, Some(Player.Black))
          .set(23, Some(Player.White))

        val state = GameState(
          board = board,
          currentPlayer = Player.Black,
          phase = GameStateInterface.MovingPhase,
          whiteStonesToPlace = 0,
          blackStonesToPlace = 0,
          whiteStones = 2,
          blackStones = 1,
          message = None
        )

        val tempFile = File.createTempFile("muehle_sparse_board", ".json")
        tempFile.deleteOnExit()

        fileIO.save(state, tempFile.getAbsolutePath)
        val loaded = fileIO.load(tempFile.getAbsolutePath).get

        loaded.getPlayer(0) should be(Some(Player.White))
        loaded.getPlayer(5) should be(Some(Player.Black))
        loaded.getPlayer(23) should be(Some(Player.White))
        loaded.getPlayer(10) should be(None)
      }
    }

    "with invalid files" should {
      "fail to load non-existent file" in {
        val result = fileIO.load("/non/existent/file.json")
        result.isFailure should be(true)
      }

      "fail to save to invalid path" in {
        val state = GameState.create(Board.empty, 9, 9, 9, 9)
        val result = fileIO.save(state, "/invalid/path/file.json")
        result.isFailure should be(true)
      }
    }

    "round-trip test" should {
      "preserve all state through save and load" in {
        val board = Board.empty
          .set(0, Some(Player.White))
          .set(1, Some(Player.White))
          .set(2, Some(Player.White))
          .set(8, Some(Player.Black))

        val originalState = GameState(
          board = board,
          currentPlayer = Player.Black,
          phase = GameStateInterface.MillRemovePhase,
          whiteStonesToPlace = 5,
          blackStonesToPlace = 7,
          whiteStones = 8,
          blackStones = 6,
          message = Some(RemoveStoneMessage.Success)
        )

        val tempFile = File.createTempFile("muehle_roundtrip", ".json")
        tempFile.deleteOnExit()

        fileIO.save(originalState, tempFile.getAbsolutePath)
        val loadedState = fileIO.load(tempFile.getAbsolutePath).get

        loadedState.currentPlayer should be(originalState.currentPlayer)
        loadedState.phase should be(originalState.phase)
        loadedState.whiteStonesToPlace should be(originalState.whiteStonesToPlace)
        loadedState.blackStonesToPlace should be(originalState.blackStonesToPlace)
        loadedState.whiteStones should be(originalState.whiteStones)
        loadedState.blackStones should be(originalState.blackStones)
        loadedState.message should be(originalState.message)

        (0 until 24).foreach { pos =>
          loadedState.getPlayer(pos) should be(originalState.getPlayer(pos))
        }
      }
    }
  }
}
