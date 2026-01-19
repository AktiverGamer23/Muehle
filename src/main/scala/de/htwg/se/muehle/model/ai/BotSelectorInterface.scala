package de.htwg.se.muehle.ai

import de.htwg.se.muehle.gamestate.GameStateInterface

/**
 * Interface for AI bot selection and management using the Strategy Pattern.
 *
 * This interface allows dynamic selection and switching of AI strategies
 * for computer-controlled players. It implements the Strategy Pattern,
 * enabling different bot behaviors to be swapped at runtime.
 *
 * Available strategies typically include:
 * - RandomBot: Makes random valid moves
 * - SimpleBot: Basic strategy trying to form mills
 *
 * @example
 * {{{
 * val botSelector: BotSelectorInterface = new BotSelector()
 * botSelector.setStrategy(new RandomBot())
 *
 * // During game loop
 * botSelector.calculateMove(gameState) match {
 *   case Some((pos1, pos2)) => controller.handle(pos1, pos2)
 *   case None => // Human player's turn or no valid move
 * }
 * }}}
 *
 * @see [[BotStrategy]] for implementing custom bot strategies
 */
trait BotSelectorInterface:

  /**
   * Sets the active bot strategy.
   *
   * @param strategy The BotStrategy implementation to use for AI moves
   */
  def setStrategy(strategy: BotStrategy): Unit

  /**
   * Returns the currently active bot strategy, if any.
   *
   * @return Some(BotStrategy) if a strategy is set, None otherwise
   */
  def getStrategy: Option[BotStrategy]

  /**
   * Removes the current bot strategy, disabling AI control.
   *
   * After calling this method, isActive will return false and
   * calculateMove will return None.
   */
  def clearStrategy(): Unit

  /**
   * Checks if a bot strategy is currently active.
   *
   * @return true if a strategy is set, false otherwise
   */
  def isActive: Boolean

  /**
   * Calculates the next move for the AI player.
   *
   * This method delegates to the current strategy to determine the best move.
   * The returned tuple represents:
   * - In PlacingPhase: (position to place, -1)
   * - In MovingPhase: (from position, to position)
   * - In MillRemovePhase: (position to remove, -1)
   *
   * @param gameState The current game state to analyze
   * @return Some((pos1, pos2)) if a move is calculated, None if no strategy is set
   *         or no valid move exists
   */
  def calculateMove(gameState: GameStateInterface): Option[(Int, Int)]