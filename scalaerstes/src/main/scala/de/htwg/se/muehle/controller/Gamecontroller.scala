package de.htwg.se.muehle.controller
import de.htwg.se.muehle.view.*
import de.htwg.se.muehle.model.*
import scala.io.StdIn
import de.htwg.se.muehle.util.*

class Gamecontroller(var gameState: GameState) extends Observable:

  def placeStone(pos: Int): Unit =
    gameState = gameState.placeStone(pos)
    notifyObservers

  def moveStone(from: Int, to: Int): Unit =
    gameState = gameState.moveStone(from, to)
    notifyObservers

  def removeStone(pos: Int): Unit =
    gameState = gameState.removeStone(pos)
    notifyObservers
  
