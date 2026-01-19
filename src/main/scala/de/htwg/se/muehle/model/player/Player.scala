package de.htwg.se.muehle.player

enum Player:
  case White, Black

  def next: Player = this match
    case White => Black
    case Black => White
