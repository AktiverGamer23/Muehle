package de.htwg.se.muehle.ai

import de.htwg.se.muehle.gamestate.GameStateInterface


trait BotSelectorInterface:
  def setStrategy(strategy: BotStrategy): Unit
  def getStrategy: Option[BotStrategy]
  def clearStrategy(): Unit
  def isActive: Boolean
  def calculateMove(gameState: GameStateInterface): Option[(Int, Int)]