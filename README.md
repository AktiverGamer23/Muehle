# Muehle (Nine Men's Morris)

A Scala 3 implementation of the classic board game Muehle (Nine Men's Morris) with both GUI and TUI interfaces.

## Features

- **Dual Interface**: Play via graphical UI (ScalaFX) or text-based terminal UI
- **AI Opponents**: Choose between RandomBot or SimpleBot for computer-controlled players
- **Save/Load**: Persist game state in XML or JSON format
- **Undo/Redo**: Full undo/redo support using Command or Memento pattern
- **Docker Support**: Run the game in a containerized environment

## Quick Start

### Prerequisites

- Java 21 or higher
- sbt 1.10+
- (Optional) Docker for containerized execution

### Running Locally

```bash
# Clone the repository
git clone <repository-url>
cd MuehleNeu

# Run the game
sbt run
```

### Running with Docker

```bash
# Allow X11 access for GUI
xhost +local:docker

# Build and run
docker build -t muehle-game .
docker run -it --rm -e DISPLAY=$DISPLAY -v /tmp/.X11-unix:/tmp/.X11-unix --network host muehle-game
```

## Game Rules

Muehle (Nine Men's Morris) is a strategy board game for two players:

1. **Placing Phase**: Players alternate placing 9 stones each on empty positions
2. **Moving Phase**: Players move stones to adjacent empty positions
3. **Mill Formation**: Three stones in a row form a "mill", allowing removal of an opponent's stone
4. **Winning**: A player wins when the opponent has only 2 stones or cannot move

## Architecture

See [ARCHITECTURE.md](ARCHITECTURE.md) for detailed architecture documentation.

### Project Structure

```
src/main/scala/de/htwg/se/muehle/
├── Main.scala                 # Application entry point
├── MuehleModule.scala         # Dependency injection configuration
├── controller/                # MVC Controller layer
│   ├── ControllerInterface.scala
│   ├── GameController.scala   # Command Pattern implementation
│   └── GameControllerWithMemento.scala  # Memento Pattern implementation
├── model/
│   ├── board/                 # Game board representation
│   ├── gamestate/             # Game state management
│   ├── player/                # Player definitions
│   ├── state/                 # State Pattern for game phases
│   └── ai/                    # AI bot strategies
├── view/                      # UI implementations
│   ├── GameView.scala         # Text UI
│   └── Gui.scala              # Graphical UI
├── fileio/                    # Persistence layer
│   ├── FileIOInterface.scala
│   ├── JsonFileIO.scala
│   └── XmlFileIO.scala
└── util/                      # Utilities and patterns
    ├── Observable.scala       # Observer pattern
    ├── Command.scala          # Command pattern
    └── MementoCaretaker.scala # Memento pattern
```

## Design Patterns

| Pattern | Usage |
|---------|-------|
| **MVC** | Separates Model, View, and Controller |
| **Observer** | Views observe controller for state changes |
| **Strategy** | Interchangeable AI bot strategies |
| **State** | Different behavior per game phase |
| **Command** | Undo/redo via command objects |
| **Memento** | Undo/redo via state snapshots |
| **Dependency Injection** | Google Guice for component wiring |

## Controls

### TUI Commands

| Key | Action |
|-----|--------|
| `0-23` | Place/select position |
| `u` | Undo last move |
| `r` | Redo move |
| `s` | Save game |
| `l` | Load game |
| `q` | Quit |

### GUI

Click on board positions to place or move stones. Use menu options for save/load/undo/redo.

## Testing

```bash
# Run all tests
sbt test

# Run with coverage
sbt coverage test coverageReport
```

## License

This project is developed for educational purposes at HTWG Konstanz.
