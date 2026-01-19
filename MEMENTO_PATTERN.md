# Memento Pattern Implementierung

## Übersicht

Das Memento Pattern ermöglicht es, den Zustand eines Objekts zu speichern und später wiederherzustellen, ohne die Kapselung zu verletzen.

## Komponenten

### 1. Memento - `GameStateMemento`

Speichert einen vollständigen Snapshot des Spielzustands.

```scala
class GameStateMemento private[util] (
  private val board: BoardInterface,
  private val currentPlayer: Player,
  private val phase: GameStateInterface.Phase,
  // ... weitere Felder
)
```

**Wichtig:** Der Konstruktor ist `private[util]`, sodass nur das `util` Package Mementos erstellen kann.

### 2. Originator - `GameStateInterface`

Der Originator (hier `GameState`) wird über Factory-Methoden im `GameStateMemento` unterstützt:

```scala
object GameStateMemento:
  def create(gameState: GameStateInterface): GameStateMemento
```

### 3. Caretaker - `MementoCaretaker`

Verwaltet die Historie der Mementos:

```scala
class MementoCaretaker:
  private var history: List[GameStateMemento] = Nil
  private var redoStack: List[GameStateMemento] = Nil
```

## Verwendung

### Standard Controller (Command Pattern)

Der aktuelle `GameController` verwendet das Command Pattern:

```scala
class GameController @Inject() (
  private var gameState: GameStateInterface,
  // ...
) extends ControllerInterface:
  private val undoManager = new UndoManager()

  def handle(pos1: Int, pos2: Int = -1): Unit =
    val cmd = new GameStateCommand(gameState, old => state.handle(old, pos1, pos2))
    gameState = undoManager.doStep(cmd)
```

### Alternative: Memento Pattern

Der neue `GameControllerWithMemento` verwendet das Memento Pattern:

```scala
class GameControllerWithMemento @Inject() (
  private var gameState: GameStateInterface,
  // ...
) extends ControllerInterface:
  private val caretaker = new MementoCaretaker()

  def handle(pos1: Int, pos2: Int = -1): Unit =
    caretaker.saveState(gameState)
    gameState = state.handle(gameState, pos1, pos2)
```

## Umschalten zwischen den Implementierungen

In `MuehleModule.scala`:

```scala
// Command Pattern (Standard)
bind(classOf[ControllerInterface]).to(classOf[GameController])

// ODER

// Memento Pattern
bind(classOf[ControllerInterface]).to(classOf[GameControllerWithMemento])
```

## Vorteile des Memento Patterns

1. **Vollständige Snapshots**: Speichert den kompletten Zustand
2. **Einfaches Debugging**: Kann gesamte Historie inspizieren
3. **Komplexe Operationen**: Kann beliebig komplexe Zustandsänderungen rückgängig machen
4. **Zusätzliche Features**: `canUndo()`, `canRedo()`, `undoCount()`, `redoCount()`

## Nachteile

1. **Speicherverbrauch**: Jeder Snapshot speichert den kompletten Zustand
2. **Performance**: Mehr Speicherallokation als Command Pattern

## Vergleich: Command vs Memento

| Aspekt | Command Pattern | Memento Pattern |
|--------|----------------|-----------------|
| Speicher | Weniger (nur Änderungen) | Mehr (volle Snapshots) |
| Komplexität | Höher (Commands schreiben) | Niedriger (automatisch) |
| Flexibilität | Sehr hoch | Mittel |
| Debugging | Schwieriger | Einfacher |
| Use Case | Viele kleine Operationen | Komplexe Zustandsänderungen |

## Beispielcode

```scala
val caretaker = new MementoCaretaker()

// Spielzustand 1: Initiales Brett
val state1 = GameState.create(Board.empty, 9, 9, 9, 9)
caretaker.saveState(state1)

// Spielzustand 2: Stein platziert
val state2 = state1.copy(
  board = state1.board.set(0, Some(Player.White)),
  whiteStonesToPlace = 8
)
caretaker.saveState(state2)

// Spielzustand 3: Weiterer Stein
val state3 = state2.copy(
  board = state2.board.set(1, Some(Player.Black)),
  blackStonesToPlace = 8
)
caretaker.saveState(state3)

// Undo: state3 -> state2
val previousState = caretaker.undo(state3)
println(previousState.get.whiteStonesToPlace) // 8

// Redo: state2 -> state3
val nextState = caretaker.redo()
println(nextState.get.blackStonesToPlace) // 8

// Check availability
if (caretaker.canUndo) {
  println(s"Can undo ${caretaker.undoCount} times")
}
if (caretaker.canRedo) {
  println(s"Can redo ${caretaker.redoCount} times")
}
```

## Tests ausführen

```bash
sbt "testOnly *MementoCaretakerSpec"
```

## Integration in die Anwendung

Um das Memento Pattern zu verwenden, ändere in `MuehleModule.scala`:

```scala
@Singleton
class MuehleModule(
  stoneswhite: Int = 9,
  stonesblack: Int = 9,
  useJson: Boolean = false,
  useMemento: Boolean = false  // Neuer Parameter
) extends AbstractModule:

  override def configure(): Unit =
    // ...

    if useMemento then
      bind(classOf[ControllerInterface]).to(classOf[GameControllerWithMemento])
    else
      bind(classOf[ControllerInterface]).to(classOf[GameController])
```

Dann in `Main.scala`:

```scala
val useMemento = true // oder false
val injector = Guice.createInjector(
  new MuehleModule(
    stoneswhite = 9,
    stonesblack = 9,
    useJson = useJson,
    useMemento = useMemento
  )
)
```

## Fazit

Das Memento Pattern ist eine hervorragende Alternative zum Command Pattern für Undo/Redo-Funktionalität. Es bietet:
- Einfachere Implementierung
- Besseres Debugging
- Vollständige Zustandshistorie

Der Preis ist höherer Speicherverbrauch, was für ein Mühle-Spiel aber vernachlässigbar ist.
