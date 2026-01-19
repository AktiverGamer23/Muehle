package de.htwg.se.muehle.util

import de.htwg.se.muehle.gamestate.GameStateInterface

trait UndoManagerInterface:
  def doStep(command: Command): GameStateInterface
  def undoStep(current: GameStateInterface): GameStateInterface
  def redoStep(current: GameStateInterface): GameStateInterface
  def clear(): Unit
