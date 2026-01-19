// Main.scala
package de.htwg.se.muehle

import com.google.inject.Guice
import de.htwg.se.muehle.controller.ControllerInterface
import de.htwg.se.muehle.view.{GameView, Gui}
import de.htwg.se.muehle.ai.{BotSelectorInterface, RandomBot, SimpleBot}
import scala.io.StdIn

object Main:

  def main(args: Array[String]): Unit =
    val useJson = chooseFileIOFormat()
    val injector = Guice.createInjector(new MuehleModule(stoneswhite = 9, stonesblack = 9, useJson = useJson, useMemento = true))

    val controller = injector.getInstance(classOf[ControllerInterface])
    val botSelector = injector.getInstance(classOf[BotSelectorInterface])

    chooseBlackBot(botSelector)

    val gui = new Gui(controller)
    val tui = new GameView(controller)

    gui.start()
    tui.start()

  private def chooseFileIOFormat(): Boolean =
    println("=" * 50)
    println("Wähle das Dateiformat für Save/Load:")
    println("  0 = XML")
    println("  1 = JSON")
    println("=" * 50)
    print("Deine Wahl: ")

    try
      val choice = StdIn.readInt()
      choice match
        case 0 =>
          println("✓ XML-Format wird verwendet")
          false
        case 1 =>
          println("✓ JSON-Format wird verwendet")
          true
        case _ =>
          println("⚠ Ungültige Wahl! XML-Format wird verwendet")
          false
    catch
      case _: Throwable =>
        println("⚠ Ungültige Eingabe! XML-Format wird verwendet")
        false
    finally
      println("=" * 50)
      println()
  

  private def chooseBlackBot(botSelector: BotSelectorInterface): Unit =
    println("=" * 50)
    println("Wähle den Spieler für Schwarz:")
    println("  0 = Mensch")
    println("  1 = RandomBot (zufällige Züge)")
    println("  2 = SimpleBot (versucht Mühlen zu bilden)")
    println("=" * 50)
    print("Deine Wahl: ")
    
    try
      val choice = StdIn.readInt()
      choice match
        case 0 => 
          botSelector.clearStrategy()
          println("✓ Schwarz wird von einem Menschen gespielt")
        case 1 => 
          botSelector.setStrategy(new RandomBot())
          println("✓ Schwarz wird vom RandomBot gespielt")
        case 2 => 
          botSelector.setStrategy(new SimpleBot())
          println("✓ Schwarz wird vom SimpleBot gespielt")
        case _ => 
          println("⚠ Ungültige Wahl! Schwarz wird von einem Menschen gespielt")
          botSelector.clearStrategy()
    catch
      case _: Throwable =>
        println("⚠ Ungültige Eingabe! Schwarz wird von einem Menschen gespielt")
        botSelector.clearStrategy()
    
    println("=" * 50)
    println()