package de.htwg.se.muehle.model
sealed trait Message
sealed trait SuccessMessage extends Message
sealed trait ErrorMessage extends Message

object SuccessMessage:
  case object PlaceStone extends SuccessMessage:
    override def toString: String = "Bitte Stein setzen(0-23)"
  case object MoveStone extends SuccessMessage:
    override def toString: String = "0-23"
  case object RemoveStone extends SuccessMessage:
    override def toString: String = "Bitte Stein Bewegen"

object ErrorMessage:
  case object InvalidMove extends ErrorMessage:
    override def toString: String = "Ungültiger Zug!"
  case object NotYourStone extends ErrorMessage:
    override def toString: String = "Du hast diesen Stein nicht!"
  case object NoNeighbour extends ErrorMessage:
    override def toString: String = "Zielfeld ist kein Nachbar!"
  case object NeighbourOccupied extends ErrorMessage:
    override def toString: String = "Nachbarfeld besetzt!"
  case object OwnStoneChosen extends ErrorMessage:
    override def toString: String = "Du kannst deinen eigenen Stein nicht entfernen!"
  case object InvalidMoveRemove extends ErrorMessage:
    override def toString: String = "Du kannst diesen Stein nicht entfernen"

