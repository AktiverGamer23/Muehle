package de.htwg.se.muehle.util

import de.htwg.se.muehle.gamestate.GameStateInterface

trait Command {
  def doStep: GameStateInterface
  def undoStep: GameStateInterface
  def redoStep: GameStateInterface
}

class GameStateCommand(
    oldState: GameStateInterface,
    action: GameStateInterface => GameStateInterface
) extends Command:

  private var newState: Option[GameStateInterface] = None

  override def doStep: GameStateInterface =
    val res = action(oldState)
    newState = Some(res)
    res

  override def undoStep: GameStateInterface =
    oldState

  override def redoStep: GameStateInterface =
    newState.getOrElse(oldState)
