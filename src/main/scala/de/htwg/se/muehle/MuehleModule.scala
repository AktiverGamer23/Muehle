
package de.htwg.se.muehle

import com.google.inject.{AbstractModule, Provides, Singleton}
import de.htwg.se.muehle.board.{Board, BoardInterface}
import de.htwg.se.muehle.state.{StateP, StateInterface}
import de.htwg.se.muehle.ai.{BotSelector, BotSelectorInterface}
import de.htwg.se.muehle.gamestate.{GameState, GameStateInterface}
import de.htwg.se.muehle.fileio.{FileIOInterface, XmlFileIO, JsonFileIO}
import de.htwg.se.muehle.controller.{ControllerInterface, GameController, GameControllerWithMemento}

/**
 * Dependency Injection Module for Muehle Game
 *
 * @param stoneswhite Number of white stones (default: 9)
 * @param stonesblack Number of black stones (default: 9)
 * @param useJson Use JSON file format instead of XML (default: false)
 * @param useMemento Use Memento Pattern for undo/redo (default: false = Command Pattern)
 */
class MuehleModule(
  stoneswhite: Int = 9,
  stonesblack: Int = 9,
  useJson: Boolean = false,
  useMemento: Boolean = false
) extends AbstractModule:

  override def configure(): Unit =
    bind(classOf[StateInterface]).to(classOf[StateP])

    bind(classOf[BotSelectorInterface]).to(classOf[BotSelector])

    // Choose Controller implementation based on useMemento flag
    if useMemento then
      bind(classOf[ControllerInterface]).to(classOf[GameControllerWithMemento])
    else
      bind(classOf[ControllerInterface]).to(classOf[GameController])

    if useJson then
      bind(classOf[FileIOInterface]).to(classOf[JsonFileIO])
    else
      bind(classOf[FileIOInterface]).to(classOf[XmlFileIO])

  @Provides
  @Singleton
  def provideBoard(): BoardInterface =
    Board.empty

  @Provides
  @Singleton
  def provideGameState(board: BoardInterface): GameStateInterface =
    GameState.create(
      board = board,
      whiteStonesToPlace = stoneswhite,
      blackStonesToPlace = stonesblack,
      whiteStones = stoneswhite,
      blackStones = stonesblack
    )