package de.htwg.se.muehle.view

import de.htwg.se.muehle.model.*
import de.htwg.se.muehle.controller.*
import scala.io.StdIn
import de.htwg.se.muehle.util.Observer

class GameView(controller: Gamecontroller) extends Observer:

  // Observer registrieren
  controller.add(this)

  /** Board-Anzeige bei jedem update */
  override def update: Unit =
    println(controller.gameState)

  /** Spiel starten */
  def start(): Unit =
    println("Willkommen zu Mühle!")
    runGameLoop()

  /** Spielschleife */
  private var lastAction: SuccessMessage = SuccessMessage.PlaceStone

  private def runGameLoop(): Unit =
    var continue = true
    while continue do
      val msg = controller.gameState.message.getOrElse(lastAction)

      msg match
        case s: SuccessMessage =>
          lastAction = s // merken, welche Phase aktuell ist
          s match
            case SuccessMessage.PlaceStone =>
              val pos = readValidInt("Bitte Position zum Setzen eingeben (0-23): ")
              controller.placeStone(pos)

            case SuccessMessage.MoveStone =>
              val from = readValidInt(s"${controller.gameState.currentPlayer}, wähle Stein zum Bewegen (0-23): ")
              val to   = readValidInt("Wähle Zielfeld (0-23): ")
              controller.moveStone(from, to)

            case SuccessMessage.RemoveStone =>
              val pos = readValidInt("Mühle! Wähle gegnerischen Stein zum Entfernen (0-23): ")
              controller.removeStone(pos)

        case err: ErrorMessage =>
          println("Fehler: " + err)
        // Wiederhole die letzte Aktion
          lastAction match
            case SuccessMessage.PlaceStone =>
              val pos = readValidInt("Bitte Position erneut eingeben (0-23): ")
              controller.placeStone(pos)
            case SuccessMessage.MoveStone =>
              val from = readValidInt(s"${controller.gameState.currentPlayer}, wähle Stein zum Bewegen (0-23): ")
              val to   = readValidInt("Wähle Zielfeld (0-23): ")
              controller.moveStone(from, to)
            case SuccessMessage.RemoveStone =>
              val pos = readValidInt("Mühle! Wähle gegnerischen Stein erneut zum Entfernen (0-23): ")
              controller.removeStone(pos)

    // Spielende prüfen



  /** Liest gültige Zahl zwischen 0-23 ein */
  def readValidInt(prompt: String): Int =
    var numOpt: Option[Int] = None
    while numOpt.isEmpty do
      try
        print(prompt)
        val num = StdIn.readInt()
        if num >= 0 && num <= 23 then numOpt = Some(num)
        else println("Ungültige Zahl! Bitte 0-23 eingeben.")
      catch
        case _: NumberFormatException =>
          println("Ungültige Eingabe! Bitte eine Zahl eingeben.")
    numOpt.get
