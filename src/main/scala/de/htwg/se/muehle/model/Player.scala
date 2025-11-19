package de.htwg.se.muehle.model

enum Player:
  case White, Black
  def next: Player = this match
      case White => Black
      case Black => White
