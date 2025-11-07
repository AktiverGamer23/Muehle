package scala
import scala.io.StdIn
import model.*
import View.*

import Controller.GameController



object MuehleApp:
  @main def runMuehle(): Unit =
    def readStones(): Int =
      println("Wie viele Steine hat jeder Spieler (0-9)?")
      val input = StdIn.readLine()
      try
        val n = input.toInt
        if n >= 0 && n <= 9 then n
        else
          println("Ungültige Eingabe. Bitte geben Sie eine Zahl von 0 bis 9 ein.")
          readStones()
      catch
        case _: NumberFormatException =>
          println("Ungültige Eingabe. Bitte geben Sie eine Zahl von 0 bis 9 ein.")
          readStones()

    val steine = readStones()
    val initialState = GameState(emptyBoard, Player.White, steine, steine, steine, steine)
    GameController.placingPhase(initialState)
