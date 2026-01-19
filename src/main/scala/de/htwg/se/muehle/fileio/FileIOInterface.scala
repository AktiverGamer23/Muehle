package de.htwg.se.muehle.fileio

import de.htwg.se.muehle.gamestate.GameStateInterface
import scala.util.Try

trait FileIOInterface:
  def save(gameState: GameStateInterface, filePath: String): Try[Unit]
  def load(filePath: String): Try[GameStateInterface]
