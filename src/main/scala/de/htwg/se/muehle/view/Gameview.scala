package de.htwg.se.muehle.view

import de.htwg.se.muehle.model.*
import de.htwg.se.muehle.controller.*
import de.htwg.se.muehle.util.Observer
import scala.io.StdIn.readLine

class GameView(controller: Gamecontroller) extends Observer:

  controller.add(this)

  def run(): Unit =
    println("Willkommen bei Mühle!")
    printBoard(controller.game)
    loop()

  private def loop(): Unit =
    while true do
      val game: GameState = controller.game

      GameStateContext.phase match
        case Placing =>
          val pos = readPosition("Position zum Platzieren (0-23): ")
          controller.handle(pos)
        case Removing =>
          val pos = readPosition("Position zum Entfernen eines gegnerischen Steins (0-23): ")
          controller.handle(pos)
        case Moving =>
          val from = readPosition("Von Position (0-23): ")
          val to   = readPosition("Nach Position (0-23): ")
          controller.handle(from, to)
  override def update: Unit =
    val game = controller.game
    println("\n--- Spiel aktualisiert ---")
    printBoard(game)
    game.message.foreach(msg => println(s"Nachricht: $msg"))
    println(s"Aktueller Spieler: ${game.currentPlayer}")

  private def printBoard(game: GameState): Unit =
    val board = game.board
    println(
    s"""
      |${cell(board.vec3(0))}---------${cell(board.vec3(1))}---------${cell(board.vec3(2))}
      | |          |          |
      | |  ${cell(board.vec2(0))}-----${cell(board.vec2(1))}-----${cell(board.vec2(2))}   |
      | |   |      |      |   |
      | |   |  ${cell(board.vec1(0))}-${cell(board.vec1(1))}-${cell(board.vec1(2))}   |   |
      | |   |   |     |   |   |
      |${cell(board.vec3(7))}--${cell(board.vec2(7))}--${cell(board.vec1(7))}  ${cell(board.vec1(3))}  ${cell(board.vec2(3))}--${cell(board.vec3(3))}
      | |   |   |     |   |   |
      | |   |  ${cell(board.vec1(6))}-${cell(board.vec1(5))}-${cell(board.vec1(4))}   |   |
      | |   |      |      |   |
      | |  ${cell(board.vec2(6))}-----${cell(board.vec2(5))}-----${cell(board.vec2(4))}   |
      | |          |          |
      |${cell(board.vec3(6))}---------${cell(board.vec3(5))}---------${cell(board.vec3(4))}
      |"""
    )

  private def cell(option: Option[Player]): String =
    option match
      case Some(player) => player.toString.head.toString
      case None => "."



  private def readPosition(prompt: String): Int =
    print(prompt + " ")
    val input = readLine()
    try input.toInt
    catch case _: Exception =>
      println("Ungültige Eingabe, bitte Zahl zwischen 0 und 23.")
      readPosition(prompt)
