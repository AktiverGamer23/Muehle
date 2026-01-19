package de.htwg.se.muehle.fileio

import de.htwg.se.muehle.gamestate.GameStateInterface
import scala.util.Try

/**
 * Interface for game state persistence (save/load functionality).
 *
 * This interface defines the contract for saving and loading game states
 * to/from files. Implementations can use different file formats:
 * - XmlFileIO: Saves/loads in XML format
 * - JsonFileIO: Saves/loads in JSON format
 *
 * The interface uses Scala's Try monad for error handling, allowing
 * callers to handle success and failure cases gracefully.
 *
 * @example
 * {{{
 * val fileIO: FileIOInterface = new JsonFileIO()
 *
 * // Save a game
 * fileIO.save(gameState, "savegame.json") match {
 *   case Success(_) => println("Game saved!")
 *   case Failure(e) => println(s"Save failed: ${e.getMessage}")
 * }
 *
 * // Load a game
 * fileIO.load("savegame.json") match {
 *   case Success(state) => // Use loaded state
 *   case Failure(e) => println(s"Load failed: ${e.getMessage}")
 * }
 * }}}
 *
 * @see [[de.htwg.se.muehle.gamestate.GameStateInterface]] for the state being persisted
 */
trait FileIOInterface:

  /**
   * Saves the game state to a file.
   *
   * The implementation serializes the complete game state including:
   * - Board configuration (all stone positions)
   * - Current player
   * - Game phase
   * - Stone counts for both players
   * - Current message (if any)
   *
   * @param gameState The game state to save
   * @param filePath The path where the file should be created/overwritten
   * @return Success(()) if saved successfully, Failure with the exception otherwise
   */
  def save(gameState: GameStateInterface, filePath: String): Try[Unit]

  /**
   * Loads a game state from a file.
   *
   * The implementation deserializes the file and reconstructs the complete
   * game state. The file must have been created by a compatible save() call.
   *
   * @param filePath The path to the save file
   * @return Success(GameStateInterface) with the loaded state, or Failure with
   *         the exception (e.g., FileNotFoundException, parse error)
   */
  def load(filePath: String): Try[GameStateInterface]
