package de.htwg.se.muehle

import org.scalatest.wordspec.AnyWordSpec
import org.scalatest.matchers.should.Matchers
import com.google.inject.{Guice, Injector}
import de.htwg.se.muehle.board.{Board, BoardInterface}
import de.htwg.se.muehle.state.{StateP, StateInterface}
import de.htwg.se.muehle.ai.{BotSelector, BotSelectorInterface}
import de.htwg.se.muehle.gamestate.{GameState, GameStateInterface}
import de.htwg.se.muehle.fileio.{FileIOInterface, XmlFileIO, JsonFileIO}
import de.htwg.se.muehle.controller.{ControllerInterface, GameControllerWithMemento}
import de.htwg.se.muehle.player.Player

class MuehleModuleSpec extends AnyWordSpec with Matchers {

  "A MuehleModule" when {
    "created with default parameters" should {
      val module = new MuehleModule()
      val injector = Guice.createInjector(module)

      "bind StateInterface to StateP" in {
        val stateInterface = injector.getInstance(classOf[StateInterface])
        stateInterface.isInstanceOf[StateP] should be(true)
      }

      "bind BotSelectorInterface to BotSelector" in {
        val botSelector = injector.getInstance(classOf[BotSelectorInterface])
        botSelector.isInstanceOf[BotSelector] should be(true)
      }

      "bind ControllerInterface not to GameControllerwithMemento" in {
        val controller = injector.getInstance(classOf[ControllerInterface])
        controller.isInstanceOf[GameControllerWithMemento] should be (false)
      }

      "bind FileIOInterface to XmlFileIO by default" in {
        val fileIO = injector.getInstance(classOf[FileIOInterface])
        fileIO.isInstanceOf[XmlFileIO] should be(true)
      }

      "provide BoardInterface as singleton" in {
        val board1 = injector.getInstance(classOf[BoardInterface])
        val board2 = injector.getInstance(classOf[BoardInterface])

        board1 should be theSameInstanceAs board2
      }

      "provide empty Board" in {
        val board = injector.getInstance(classOf[BoardInterface])

        (0 until 24).foreach { pos =>
          board.stoneAt(pos) should be(None)
        }
      }

      "provide GameStateInterface as singleton" in {
        val gameState1 = injector.getInstance(classOf[GameStateInterface])
        val gameState2 = injector.getInstance(classOf[GameStateInterface])

        gameState1 should be theSameInstanceAs gameState2
      }

      "provide GameState with default stone counts (9, 9)" in {
        val gameState = injector.getInstance(classOf[GameStateInterface])

        gameState.whiteStonesToPlace should be(9)
        gameState.blackStonesToPlace should be(9)
        gameState.whiteStones should be(9)
        gameState.blackStones should be(9)
      }

      "provide GameState with White as starting player" in {
        val gameState = injector.getInstance(classOf[GameStateInterface])
        gameState.currentPlayer should be(Player.White)
      }

      "provide GameState in PlacingPhase" in {
        val gameState = injector.getInstance(classOf[GameStateInterface])
        gameState.phase should be(GameStateInterface.PlacingPhase)
      }
    }

    "created with custom stone counts" should {
      "use provided white stones count" in {
        val module = new MuehleModule(stoneswhite = 5, stonesblack = 9)
        val injector = Guice.createInjector(module)
        val gameState = injector.getInstance(classOf[GameStateInterface])

        gameState.whiteStonesToPlace should be(5)
        gameState.whiteStones should be(5)
      }

      "use provided black stones count" in {
        val module = new MuehleModule(stoneswhite = 9, stonesblack = 7)
        val injector = Guice.createInjector(module)
        val gameState = injector.getInstance(classOf[GameStateInterface])

        gameState.blackStonesToPlace should be(7)
        gameState.blackStones should be(7)
      }

      "use both custom stone counts" in {
        val module = new MuehleModule(stoneswhite = 6, stonesblack = 8)
        val injector = Guice.createInjector(module)
        val gameState = injector.getInstance(classOf[GameStateInterface])

        gameState.whiteStonesToPlace should be(6)
        gameState.blackStonesToPlace should be(8)
        gameState.whiteStones should be(6)
        gameState.blackStones should be(8)
      }

      "handle minimum stone count (3, 3)" in {
        val module = new MuehleModule(stoneswhite = 3, stonesblack = 3)
        val injector = Guice.createInjector(module)
        val gameState = injector.getInstance(classOf[GameStateInterface])

        gameState.whiteStonesToPlace should be(3)
        gameState.blackStonesToPlace should be(3)
      }

      "handle maximum stone count (12, 12)" in {
        val module = new MuehleModule(stoneswhite = 12, stonesblack = 12)
        val injector = Guice.createInjector(module)
        val gameState = injector.getInstance(classOf[GameStateInterface])

        gameState.whiteStonesToPlace should be(12)
        gameState.blackStonesToPlace should be(12)
      }

      "handle asymmetric stone counts" in {
        val module = new MuehleModule(stoneswhite = 3, stonesblack = 12)
        val injector = Guice.createInjector(module)
        val gameState = injector.getInstance(classOf[GameStateInterface])

        gameState.whiteStonesToPlace should be(3)
        gameState.blackStonesToPlace should be(12)
      }
    }

    "created with useJson = true" should {
      val module = new MuehleModule(useJson = true)
      val injector = Guice.createInjector(module)

      "bind FileIOInterface to JsonFileIO" in {
        val fileIO = injector.getInstance(classOf[FileIOInterface])
        fileIO.isInstanceOf[JsonFileIO] should be(true)
      }

      "not bind FileIOInterface to XmlFileIO" in {
        val fileIO = injector.getInstance(classOf[FileIOInterface])
        fileIO.isInstanceOf[XmlFileIO] should be(false)
      }
    }

    "created with useJson = false" should {
      val module = new MuehleModule(useJson = false)
      val injector = Guice.createInjector(module)

      "bind FileIOInterface to XmlFileIO" in {
        val fileIO = injector.getInstance(classOf[FileIOInterface])
        fileIO.isInstanceOf[XmlFileIO] should be(true)
      }

      "not bind FileIOInterface to JsonFileIO" in {
        val fileIO = injector.getInstance(classOf[FileIOInterface])
        fileIO.isInstanceOf[JsonFileIO] should be(false)
      }
    }

    "created with all custom parameters" should {
      val module = new MuehleModule(stoneswhite = 7, stonesblack = 8, useJson = true)
      val injector = Guice.createInjector(module)

      "use custom stone counts" in {
        val gameState = injector.getInstance(classOf[GameStateInterface])
        gameState.whiteStonesToPlace should be(7)
        gameState.blackStonesToPlace should be(8)
      }

      "use JsonFileIO" in {
        val fileIO = injector.getInstance(classOf[FileIOInterface])
        fileIO.isInstanceOf[JsonFileIO] should be(true)
      }

      "still provide all other bindings" in {
        val stateInterface = injector.getInstance(classOf[StateInterface])
        val botSelector = injector.getInstance(classOf[BotSelectorInterface])
        val controller = injector.getInstance(classOf[ControllerInterface])

        stateInterface should not be null
        botSelector should not be null
        controller should not be null
      }
    }

    "dependency injection" should {
      val module = new MuehleModule()
      val injector = Guice.createInjector(module)

      "inject BoardInterface into GameState provider" in {
        val gameState = injector.getInstance(classOf[GameStateInterface])
        gameState.board should not be null
      }

      "provide consistent Board instance" in {
        val board = injector.getInstance(classOf[BoardInterface])
        val gameState = injector.getInstance(classOf[GameStateInterface])

        board should be theSameInstanceAs gameState.board
      }

      "create multiple independent injectors" in {
        val injector1 = Guice.createInjector(new MuehleModule(stoneswhite = 5))
        val injector2 = Guice.createInjector(new MuehleModule(stoneswhite = 7))

        val gameState1 = injector1.getInstance(classOf[GameStateInterface])
        val gameState2 = injector2.getInstance(classOf[GameStateInterface])

        gameState1.whiteStonesToPlace should be(5)
        gameState2.whiteStonesToPlace should be(7)
        gameState1 should not be theSameInstanceAs(gameState2)
      }

      "provide singleton instances within same injector" in {
        val state1 = injector.getInstance(classOf[StateInterface])
        val state2 = injector.getInstance(classOf[StateInterface])

        state1 should be theSameInstanceAs state2
      }

      "provide singleton BotSelector" in {
        val bot1 = injector.getInstance(classOf[BotSelectorInterface])
        val bot2 = injector.getInstance(classOf[BotSelectorInterface])

        bot1 should be theSameInstanceAs bot2
      }
    }

    "edge cases" should {
      "handle zero stones for white" in {
        val module = new MuehleModule(stoneswhite = 0, stonesblack = 9)
        val injector = Guice.createInjector(module)
        val gameState = injector.getInstance(classOf[GameStateInterface])

        gameState.whiteStonesToPlace should be(0)
        gameState.whiteStones should be(0)
      }

      "handle zero stones for black" in {
        val module = new MuehleModule(stoneswhite = 9, stonesblack = 0)
        val injector = Guice.createInjector(module)
        val gameState = injector.getInstance(classOf[GameStateInterface])

        gameState.blackStonesToPlace should be(0)
        gameState.blackStones should be(0)
      }

      "handle zero stones for both players" in {
        val module = new MuehleModule(stoneswhite = 0, stonesblack = 0)
        val injector = Guice.createInjector(module)
        val gameState = injector.getInstance(classOf[GameStateInterface])

        gameState.whiteStonesToPlace should be(0)
        gameState.blackStonesToPlace should be(0)
      }

      "handle very large stone counts" in {
        val module = new MuehleModule(stoneswhite = 100, stonesblack = 100)
        val injector = Guice.createInjector(module)
        val gameState = injector.getInstance(classOf[GameStateInterface])

        gameState.whiteStonesToPlace should be(100)
        gameState.blackStonesToPlace should be(100)
      }
    }

    "module configuration" should {
      "not throw exception during module creation" in {
        noException should be thrownBy new MuehleModule()
      }

      "not throw exception with custom parameters" in {
        noException should be thrownBy new MuehleModule(5, 7, true)
      }

      "not throw exception during injector creation" in {
        val module = new MuehleModule()
        noException should be thrownBy Guice.createInjector(module)
      }

      "allow multiple modules with different configurations" in {
        val modules = List(
          new MuehleModule(),
          new MuehleModule(stoneswhite = 5),
          new MuehleModule(stonesblack = 7),
          new MuehleModule(useJson = true),
          new MuehleModule(3, 3, false)
        )

        modules.foreach { module =>
          noException should be thrownBy Guice.createInjector(module)
        }
      }
    }

    "default vs custom configurations" should {
      "default configuration uses 9 stones each" in {
        val defaultModule = new MuehleModule()
        val injector = Guice.createInjector(defaultModule)
        val gameState = injector.getInstance(classOf[GameStateInterface])

        gameState.whiteStonesToPlace should be(9)
        gameState.blackStonesToPlace should be(9)
      }

      "default configuration uses XmlFileIO" in {
        val defaultModule = new MuehleModule()
        val injector = Guice.createInjector(defaultModule)
        val fileIO = injector.getInstance(classOf[FileIOInterface])

        fileIO.isInstanceOf[XmlFileIO] should be(true)
      }

      "custom configuration overrides defaults" in {
        val customModule = new MuehleModule(stoneswhite = 5, stonesblack = 6, useJson = true)
        val injector = Guice.createInjector(customModule)
        val gameState = injector.getInstance(classOf[GameStateInterface])
        val fileIO = injector.getInstance(classOf[FileIOInterface])

        gameState.whiteStonesToPlace should be(5)
        gameState.blackStonesToPlace should be(6)
        fileIO.isInstanceOf[JsonFileIO] should be(true)
      }
    }

    "with different FileIO implementations" should {
      "switch between XmlFileIO and JsonFileIO" in {
        val xmlModule = new MuehleModule(useJson = false)
        val jsonModule = new MuehleModule(useJson = true)

        val xmlInjector = Guice.createInjector(xmlModule)
        val jsonInjector = Guice.createInjector(jsonModule)

        val xmlFileIO = xmlInjector.getInstance(classOf[FileIOInterface])
        val jsonFileIO = jsonInjector.getInstance(classOf[FileIOInterface])

        xmlFileIO.isInstanceOf[XmlFileIO] should be(true)
        jsonFileIO.isInstanceOf[JsonFileIO] should be(true)
      }

      "FileIO instances are independent across injectors" in {
        val injector1 = Guice.createInjector(new MuehleModule(useJson = true))
        val injector2 = Guice.createInjector(new MuehleModule(useJson = true))

        val fileIO1 = injector1.getInstance(classOf[FileIOInterface])
        val fileIO2 = injector2.getInstance(classOf[FileIOInterface])

        fileIO1 should not be theSameInstanceAs(fileIO2)
      }
    }

    "GameState provider" should {
      "create GameState with provided board" in {
        val module = new MuehleModule()
        val injector = Guice.createInjector(module)
        val gameState = injector.getInstance(classOf[GameStateInterface])

        gameState.board should not be null
        gameState.board.isInstanceOf[BoardInterface] should be(true)
      }

      "GameState has empty board initially" in {
        val module = new MuehleModule()
        val injector = Guice.createInjector(module)
        val gameState = injector.getInstance(classOf[GameStateInterface])

        (0 until 24).foreach { pos =>
          gameState.getPlayer(pos) should be(None)
        }
      }

      "GameState initial message is PlaceStoneMessage.Success" in {
        val module = new MuehleModule()
        val injector = Guice.createInjector(module)
        val gameState = injector.getInstance(classOf[GameStateInterface])

        gameState.message should be(Some(de.htwg.se.muehle.model.PlaceStoneMessage.Success))
      }
    }
  }
}
