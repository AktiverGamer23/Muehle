package de.htwg.se.muehle.controller

import de.htwg.se.muehle.util.Observable
import de.htwg.se.muehle.gamestate.GameStateInterface
import scala.util.Try

trait ControllerInterface extends Observable:
  def handle(pos1: Int, pos2: Int = -1): Unit
  def undo(): Unit
  def redo(): Unit
  def getGameState: GameStateInterface
  def save(filePath: String): Try[Unit]
  def load(filePath: String): Try[Unit]
  def restart(): Unit
