package View
import model.*
import scala.io.StdIn

object Gameview {

    def Feld(pos: Int, board: Board): String = board(pos) match
        case Empty => f"$pos%2d "           
        case Occupied(Player.White) => " W "
        case Occupied(Player.Black) => " B "

    def renderBoard(board: Board, currentPlayer: Player): String =
    s"""
      |${Feld(16, board)}---------${Feld(17, board)}---------${Feld(18, board)}
      | |           |           |
      | |  ${Feld(8, board)}-----${Feld(9, board)}-----${Feld(10, board)}  |
      | |   |       |       |   |
      | |   |  ${Feld(0, board)}-${Feld(1, board)}-${Feld(2, board)}  |   |
      | |   |   |       |   |   |
      |${Feld(23, board)}-${Feld(15, board)}-${Feld(7, board)}     ${Feld(3, board)}-${Feld(11, board)}-${Feld(19, board)}
      | |   |   |       |   |   |
      | |   |  ${Feld(6, board)}-${Feld(5, board)}-${Feld(4, board)}  |   |
      | |   |       |       |   |
      | |  ${Feld(14, board)}-----${Feld(13, board)}-----${Feld(12, board)}  |
      | |           |           |
      |${Feld(22, board)}---------${Feld(21, board)}---------${Feld(20, board)}
      |""".stripMargin

    def showBoard(state: GameState): Unit =
        println(renderBoard(state.board, state.currentPlayer))

    def readPos(player: Player): Int =
        println(s"$player ist am Zug. Wähle ein Feld (0-23):")
        try
            val i = StdIn.readLine().toInt
            i
        catch
            case _: NumberFormatException =>
                println("Ungültige Eingabe. Bitte eine Zahl eingeben.")
                readPos(player)
    
    

}