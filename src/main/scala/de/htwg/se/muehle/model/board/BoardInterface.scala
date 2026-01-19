package de.htwg.se.muehle.board

import de.htwg.se.muehle.player.Player

/**
 * Interface representing the game board for Muehle (Nine Men's Morris).
 *
 * The board consists of 24 positions arranged in three concentric squares,
 * connected by lines. Positions are numbered 0-23.
 *
 * {{{
 * Board Layout:
 * 16---------17---------18
 *  |         |          |
 *  |  8------9------10  |
 *  |  |      |      |   |
 *  |  |  0---1---2  |   |
 *  |  |  |       |  |   |
 * 23--15-7       3-11--19
 *  |  |  |       |  |   |
 *  |  |  6---5---4  |   |
 *  |  |      |      |   |
 *  |  14-----13-----12  |
 *  |         |          |
 * 22---------21---------20
 * }}}
 *
 * This interface is immutable - all modification methods return a new BoardInterface.
 */
trait BoardInterface:

  /**
   * Returns the player who has a stone at the given position.
   *
   * @param pos The position to check (0-23)
   * @return Some(Player) if a stone is present, None if the position is empty
   */
  def stoneAt(pos: Int): Option[Player]

  /**
   * Creates a new board with the specified position set to the given player.
   *
   * @param pos The position to modify (0-23)
   * @param player Some(Player) to place a stone, None to remove a stone
   * @return A new BoardInterface with the modification applied
   */
  def set(pos: Int, player: Option[Player]): BoardInterface

  /**
   * Checks if two positions are adjacent (connected by a line).
   *
   * @param from The source position (0-23)
   * @param to The target position (0-23)
   * @return true if the positions are neighbours, false otherwise
   */
  def isNeighbour(from: Int, to: Int): Boolean

  /**
   * Checks if the stone at the given position is part of a mill (three in a row).
   *
   * A mill is formed when three stones of the same player are aligned
   * horizontally or vertically on connected positions.
   *
   * @param pos The position to check (0-23)
   * @return true if the stone at pos forms a mill, false otherwise
   */
  def isMill(pos: Int): Boolean
