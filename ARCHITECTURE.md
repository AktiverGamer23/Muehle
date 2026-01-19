# Architecture Documentation

This document describes the software architecture of the Muehle (Nine Men's Morris) game implementation.

## Overview

The application follows a layered architecture based on the **Model-View-Controller (MVC)** pattern, enhanced with several design patterns for flexibility and maintainability.

```
┌─────────────────────────────────────────────────────────────────┐
│                           View Layer                            │
│                  (GameView, Gui - Observer Pattern)             │
└─────────────────────────────┬───────────────────────────────────┘
                              │ observes
                              ▼
┌─────────────────────────────────────────────────────────────────┐
│                       Controller Layer                          │
│            (ControllerInterface - Command/Memento Pattern)      │
└─────────────────────────────┬───────────────────────────────────┘
                              │ manipulates
                              ▼
┌─────────────────────────────────────────────────────────────────┐
│                         Model Layer                             │
│    (Board, GameState, Player, State Pattern, Strategy Pattern)  │
└─────────────────────────────────────────────────────────────────┘
```

## Component Diagram

```
                    ┌──────────────┐
                    │     Main     │
                    └──────┬───────┘
                           │
                    ┌──────▼───────┐
                    │ MuehleModule │ (Guice DI)
                    └──────┬───────┘
                           │
        ┌──────────────────┼──────────────────┐
        │                  │                  │
        ▼                  ▼                  ▼
┌───────────────┐  ┌───────────────┐  ┌───────────────┐
│   GameView    │  │     Gui       │  │  Controller   │
│    (TUI)      │  │  (ScalaFX)    │  │  Interface    │
└───────┬───────┘  └───────┬───────┘  └───────┬───────┘
        │                  │                  │
        └──────────────────┼──────────────────┘
                           │ Observer Pattern
                           ▼
              ┌────────────────────────┐
              │  GameController or     │
              │  GameControllerMemento │
              └────────────┬───────────┘
                           │
        ┌──────────────────┼──────────────────┐
        │                  │                  │
        ▼                  ▼                  ▼
┌───────────────┐  ┌───────────────┐  ┌───────────────┐
│  GameState    │  │  BotSelector  │  │    FileIO     │
│  Interface    │  │  (Strategy)   │  │  (XML/JSON)   │
└───────┬───────┘  └───────────────┘  └───────────────┘
        │
        ▼
┌───────────────┐
│    Board      │
│  Interface    │
└───────────────┘
```

## Design Patterns

### 1. Model-View-Controller (MVC)

The application is structured into three main layers:

- **Model**: Contains game logic, board representation, and game state
- **View**: Handles user interface (TUI and GUI)
- **Controller**: Mediates between Model and View, processes user input

### 2. Observer Pattern

Views register as observers with the controller. When the game state changes, all registered observers are notified automatically.

```scala
trait Observable:
  private var observers: List[Observer] = Nil

  def add(observer: Observer): Unit
  def remove(observer: Observer): Unit
  def notifyObservers: Unit

trait Observer:
  def update: Unit
```

**Usage:**
```scala
class GameView(controller: ControllerInterface) extends Observer:
  controller.add(this)

  override def update: Unit =
    // Refresh display with current game state
```

### 3. State Pattern

Game behavior changes based on the current phase. The `StateInterface` handles actions differently depending on whether it's the placing, moving, or mill-remove phase.

```
┌─────────────────┐      ┌─────────────────┐      ┌─────────────────┐
│  PlacingPhase   │ ───► │  MovingPhase    │ ───► │ MillRemovePhase │
│                 │      │                 │ ◄─── │                 │
│ Place stones    │      │ Move stones     │      │ Remove opponent │
└─────────────────┘      └─────────────────┘      └─────────────────┘
```

### 4. Strategy Pattern

AI opponents use the Strategy Pattern, allowing different bot algorithms to be swapped at runtime.

```scala
trait BotStrategy:
  def calculateMove(gameState: GameStateInterface): Option[(Int, Int)]

class RandomBot extends BotStrategy
class SimpleBot extends BotStrategy
```

### 5. Command Pattern (Undo/Redo Option 1)

Actions are encapsulated as Command objects, enabling undo/redo functionality.

```scala
trait Command:
  def doStep: GameStateInterface
  def undoStep: GameStateInterface
  def redoStep: GameStateInterface

class GameStateCommand(oldState, action) extends Command
```

### 6. Memento Pattern (Undo/Redo Option 2)

Alternative undo/redo implementation using state snapshots.

```scala
class GameStateMemento:
  // Stores complete state snapshot
  def restore(): GameStateInterface

class MementoCaretaker:
  // Manages history stack
  def saveState(state): Unit
  def undo(): Option[GameStateInterface]
  def redo(): Option[GameStateInterface]
```

**Comparison:**

| Aspect | Command Pattern | Memento Pattern |
|--------|-----------------|-----------------|
| Memory | Lower (stores deltas) | Higher (stores full state) |
| Complexity | Higher (commands need undo logic) | Lower (automatic) |
| Debugging | Harder | Easier (can inspect states) |

### 7. Dependency Injection (Google Guice)

Components are wired together using Google Guice, allowing easy configuration and testing.

```scala
class MuehleModule(
  stoneswhite: Int = 9,
  stonesblack: Int = 9,
  useJson: Boolean = false,
  useMemento: Boolean = false
) extends AbstractModule:

  override def configure(): Unit =
    bind(classOf[ControllerInterface])
      .to(if useMemento then classOf[GameControllerWithMemento]
          else classOf[GameController])
```

## Data Flow

### Placing a Stone

```
1. User clicks position 5 in GUI
           │
           ▼
2. Gui calls controller.handle(5)
           │
           ▼
3. Controller saves state for undo
           │
           ▼
4. Controller calls state.handle(gameState, 5, -1)
           │
           ▼
5. StateP (PlacingPhase) processes:
   - Validates position is empty
   - Places stone on board
   - Checks for mill
   - Switches player or enters MillRemovePhase
           │
           ▼
6. Controller updates gameState
           │
           ▼
7. Controller calls notifyObservers
           │
           ▼
8. All views receive update notification
           │
           ▼
9. Views refresh display with new state
```

### Undo Operation

```
Command Pattern:                    Memento Pattern:

1. User presses 'u'                1. User presses 'u'
        │                                  │
        ▼                                  ▼
2. controller.undo()               2. controller.undo()
        │                                  │
        ▼                                  ▼
3. undoManager.undoStep()          3. caretaker.undo()
        │                                  │
        ▼                                  ▼
4. Pop command from stack          4. Pop memento from history
   Call command.undoStep()            Push to redo stack
   Push to redo stack                 Return memento.restore()
        │                                  │
        ▼                                  ▼
5. Return previous state           5. Return previous state
        │                                  │
        ▼                                  ▼
6. notifyObservers                 6. notifyObservers
```

## Board Representation

The board consists of 24 positions arranged in three concentric squares:

```
Position Numbers:
 0---------1---------2
 |         |         |
 |  3------4------5  |
 |  |      |      |  |
 |  |  6---7---8  |  |
 |  |  |       |  |  |
 9--10-11     12-13--14
 |  |  |       |  |  |
 |  |  15--16--17 |  |
 |  |      |      |  |
 |  18-----19-----20 |
 |         |         |
 21--------22--------23
```

**Mill Combinations:**
- Horizontal: [0,1,2], [3,4,5], [6,7,8], [9,10,11], [12,13,14], [15,16,17], [18,19,20], [21,22,23]
- Vertical: [0,9,21], [3,10,18], [6,11,15], [1,4,7], [16,19,22], [8,12,17], [5,13,20], [2,14,23]

## File I/O

Game states can be persisted in two formats:

### JSON Format
```json
{
  "board": [null, "White", null, ...],
  "currentPlayer": "White",
  "phase": "PlacingPhase",
  "whiteStonesToPlace": 7,
  "blackStonesToPlace": 7,
  "whiteStones": 9,
  "blackStones": 9
}
```

### XML Format
```xml
<gameState>
  <board>
    <position index="0">empty</position>
    <position index="1">White</position>
    ...
  </board>
  <currentPlayer>White</currentPlayer>
  <phase>PlacingPhase</phase>
  ...
</gameState>
```

## Testing Strategy

The application uses ScalaTest for unit testing:

- **Model Tests**: Test board logic, mill detection, game state transitions
- **Controller Tests**: Test handle, undo/redo, save/load operations
- **Integration Tests**: Test full game scenarios

```bash
sbt test                    # Run all tests
sbt coverage test           # Run with coverage
sbt coverageReport          # Generate coverage report
```

## Extensibility

The architecture supports easy extension:

1. **New AI Strategies**: Implement `BotStrategy` trait
2. **New File Formats**: Implement `FileIOInterface`
3. **New UI**: Implement `Observer` and use `ControllerInterface`
4. **Game Variants**: Extend `GameStateInterface` with new rules
