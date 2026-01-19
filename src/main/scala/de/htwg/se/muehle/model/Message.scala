package de.htwg.se.muehle.model

trait Message
trait PlaceStoneMessage extends Message
trait RemoveStoneMessage extends Message
trait MoveStoneMessage extends Message
trait Winner extends Message
object PlaceStoneMessage:
  case object Success extends PlaceStoneMessage:
    override def toString: String = "Bitte Stein setzen(0-23)"
  case object InvalidMove extends PlaceStoneMessage:
    override def toString: String = "Bitte Stein setzen(0-23)"
  case object Occupied extends PlaceStoneMessage:
    override def toString: String = "Feld ist besetzt"


object RemoveStoneMessage:
  case object InvalidMove extends RemoveStoneMessage:
    override def toString: String = "Du kannst diesen Stein nicht entfernen"
  case object Success extends RemoveStoneMessage:
    override def toString: String = "Wähle einen Stein zum entfernen"
  case object OwnStoneChosen extends RemoveStoneMessage:
    override def toString: String = "Du kannst deinen eigenen Stein nicht entfernen!"
object MoveStoneMessage:
  case object NoNeighbour extends MoveStoneMessage:
    override def toString: String = "Zielfeld ist kein Nachbar"
  case object NeighbourOccupied extends MoveStoneMessage:
    override def toString: String = "Nachbarfeld ist besetzt"
  case object NotYourStone extends MoveStoneMessage:
    override def toString: String = "Du hast diesen Stein nicht"
  case object Success extends MoveStoneMessage:
    override def toString: String = "Bewege einen Stein"

object Winner:
  case object White extends Winner:
    override def toString: String = "Gewinner: WEISS"
  case object Black extends Winner:
    override def toString: String = "Gewinner: SCHWARZ"