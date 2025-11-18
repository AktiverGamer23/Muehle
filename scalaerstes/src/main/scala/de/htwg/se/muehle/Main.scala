package de.htwg.se.muehle

import de.htwg.se.muehle.model._
import de.htwg.se.muehle.view.GameView
import de.htwg.se.muehle.controller.Gamecontroller
import de.htwg.se.muehle.util.*

object Main:
  def main(args: Array[String]): Unit =
    // Initialisierung von GameState, Controller, View
    val initialState = GameState(
      board = Vector.fill(24)(Empty),
      currentPlayer = Player.White,
      whiteStonesToPlace = 4,
      blackStonesToPlace = 4,
      whiteStones = 4,
      blackStones = 4,
      message = None
    )
    val controller = new Gamecontroller(initialState)
    val view = new GameView(controller)
    view.start()