package de.htwg.se.muehle.util

import de.htwg.se.muehle.gamestate.GameStateInterface

class UndoManager extends UndoManagerInterface:
  
  private var undoStack: List[Command] = Nil
  private var redoStack: List[Command] = Nil
  
  override def doStep(command: Command): GameStateInterface =
    val newState = command.doStep
    undoStack = command :: undoStack
    redoStack = Nil
    newState
  
  override def undoStep(current: GameStateInterface): GameStateInterface =
    undoStack match
      case Nil => current
      case head :: stack =>
        val newState = head.undoStep
        undoStack = stack
        redoStack = head :: redoStack
        newState
  
  override def redoStep(current: GameStateInterface): GameStateInterface =
    redoStack match
      case Nil => current
      case head :: stack =>
        val newState = head.redoStep
        redoStack = stack
        undoStack = head :: undoStack
        newState

  override def clear(): Unit =
    undoStack = Nil
    redoStack = Nil

