package de.htwg.se.muehle.state

import de.htwg.se.muehle.gamestate.GameStateInterface

/**
 * Interface for the State Pattern implementation in the Muehle game.
 *
 * This interface defines how game actions are processed based on the current
 * game phase. It implements the State Pattern, where behavior changes depending
 * on the game's current state (phase).
 *
 * The State Pattern allows the game logic to be cleanly separated into:
 * - PlacingPhase: Handle stone placement
 * - MovingPhase: Handle stone movement
 * - MillRemovePhase: Handle opponent stone removal
 *
 * @see [[de.htwg.se.muehle.gamestate.GameStateInterface.Phase]] for phase definitions
 */
trait StateInterface:

  /**
   * Handles a game action and returns the resulting game state.
   *
   * This method processes player actions based on the current game phase:
   *
   * '''PlacingPhase:'''
   * - pos1: Position where to place the stone
   * - pos2: Ignored
   * - Returns: New state with stone placed, possibly transitioning to MillRemovePhase
   *
   * '''MovingPhase:'''
   * - pos1: Position of the stone to move
   * - pos2: Target position for the stone
   * - Returns: New state with stone moved, possibly transitioning to MillRemovePhase
   *
   * '''MillRemovePhase:'''
   * - pos1: Position of opponent's stone to remove
   * - pos2: Ignored
   * - Returns: New state with stone removed, transitioning back to previous phase
   *
   * @param gs The current game state
   * @param pos1 Primary position for the action (0-23)
   * @param pos2 Secondary position for move actions (0-23), default -1 for non-move actions
   * @return A new GameStateInterface reflecting the action's result
   */
  def handle(gs: GameStateInterface, pos1: Int, pos2: Int = -1): GameStateInterface
