package de.htwg.se.muehle.controller
import de.htwg.se.muehle.model.*
import scala.io.StdIn
import de.htwg.se.muehle.util.*

class Gamecontroller(var game: GameState) extends Observable{
  def handle(pos1: Int, pos2: Int= -1): Unit = {
    game = GameStateContext.handle(pos1, pos2, game)
    notifyObservers
  }
}