# FileIO Implementation - Mühle Game

## Übersicht

Diese Implementierung fügt dem Mühle-Spiel die Möglichkeit hinzu, Spielstände zu speichern und zu laden. Es werden zwei Formate unterstützt: **XML** und **JSON**. Die Implementierung nutzt Dependency Injection, um zur Laufzeit zwischen den Formaten zu wechseln.

## Architektur

### 1. Interface-basiertes Design

**FileIOInterface** ([src/main/scala/de/htwg/se/muehle/fileio/FileIOInterface.scala](src/main/scala/de/htwg/se/muehle/fileio/FileIOInterface.scala))
```scala
trait FileIOInterface:
  def save(gameState: GameStateInterface, filePath: String): Try[Unit]
  def load(filePath: String): Try[GameStateInterface]
```

### 2. Implementierungen

#### XML-Implementierung
**XmlFileIO** ([src/main/scala/de/htwg/se/muehle/fileio/XmlFileIO.scala](src/main/scala/de/htwg/se/muehle/fileio/XmlFileIO.scala))
- Verwendet `scala.xml` Bibliothek
- Speichert GameState als strukturiertes XML
- Pretty-printing für lesbare Ausgabe

#### JSON-Implementierung
**JsonFileIO** ([src/main/scala/de/htwg/se/muehle/fileio/JsonFileIO.scala](src/main/scala/de/htwg/se/muehle/fileio/JsonFileIO.scala))
- Verwendet Play JSON Bibliothek
- Custom Format-Implementierungen für Player, Phase, Message
- Kompaktes JSON-Format

### 3. Dependency Injection

**MuehleModule** ([src/main/scala/de/htwg/se/muehle/MuehleModule.scala](src/main/scala/de/htwg/se/muehle/MuehleModule.scala))
```scala
class MuehleModule(stoneswhite: Int = 9, stonesblack: Int = 9, useJson: Boolean = false)
```

Der `useJson` Parameter bestimmt, welche Implementierung verwendet wird:
- `useJson = false` → XmlFileIO wird gebunden
- `useJson = true` → JsonFileIO wird gebunden

### 4. Controller-Integration

**GameController** ([src/main/scala/de/htwg/se/muehle/controller/Gamecontroller.scala](src/main/scala/de/htwg/se/muehle/controller/Gamecontroller.scala))
- FileIOInterface wird per Constructor Injection eingebunden
- `save(filePath: String): Try[Unit]` - Speichert aktuellen Spielstand
- `load(filePath: String): Try[Unit]` - Lädt Spielstand und setzt Undo-Stack zurück

### 5. View-Integration

#### Text UI (TUI)
**GameView** ([src/main/scala/de/htwg/se/muehle/view/Gameview.scala](src/main/scala/de/htwg/se/muehle/view/Gameview.scala))
- Kommando `s` - Save (fragt nach Dateiname)
- Kommando `l` - Load (fragt nach Dateiname)
- Feedback mit ✓/✗ Symbolen

#### Grafisches UI (GUI)
**Gui** ([src/main/scala/de/htwg/se/muehle/view/Gui.scala](src/main/scala/de/htwg/se/muehle/view/Gui.scala))
- "Save" Button mit FileChooser Dialog
- "Load" Button mit FileChooser Dialog
- Feedback im Message Label

## Verwendung

### 1. Auswahl des Dateiformats beim Start

Beim Start des Programms ([Main.scala](src/main/scala/de/htwg/se/muehle/Main.scala)) wird gefragt:
```
==================================================
Wähle das Dateiformat für Save/Load:
  0 = XML
  1 = JSON
==================================================
Deine Wahl:
```

### 2. Speichern und Laden im TUI

```
> s
Dateiname zum Speichern: mygame.xml
✓ Spiel erfolgreich gespeichert in mygame.xml

> l
Dateiname zum Laden: mygame.xml
✓ Spiel erfolgreich geladen aus mygame.xml
```

### 3. Speichern und Laden in der GUI

- Klick auf "Save" Button → FileChooser öffnet sich
- Dateiname eingeben und speichern
- Klick auf "Load" Button → FileChooser öffnet sich
- Datei auswählen und laden

## Beispiel XML-Format

```xml
<gameState>
  <board>
    <position index="0">White</position>
    <position index="1">Empty</position>
    ...
    <position index="23">Empty</position>
  </board>
  <currentPlayer>White</currentPlayer>
  <phase>PlacingPhase</phase>
  <whiteStonesToPlace>9</whiteStonesToPlace>
  <blackStonesToPlace>9</blackStonesToPlace>
  <whiteStones>9</whiteStones>
  <blackStones>9</blackStones>
  <message>PlaceStoneSuccess</message>
</gameState>
```

## Beispiel JSON-Format

```json
{
  "board": [
    {"index": 0, "player": "White"},
    {"index": 1, "player": null},
    ...
    {"index": 23, "player": null}
  ],
  "currentPlayer": "White",
  "phase": "PlacingPhase",
  "whiteStonesToPlace": 9,
  "blackStonesToPlace": 9,
  "whiteStones": 9,
  "blackStones": 9,
  "message": "PlaceStoneSuccess"
}
```

## Tests

**XmlFileIOSpec** ([src/test/scala/de/htwg/se/muehle/fileio/XmlFileIOSpec.scala](src/test/scala/de/htwg/se/muehle/fileio/XmlFileIOSpec.scala))
- Test: Save und Load eines GameState
- Test: Fehlerbehandlung bei ungültigen Pfaden
- Test: Fehlerbehandlung bei nicht existierenden Dateien

**JsonFileIOSpec** ([src/test/scala/de/htwg/se/muehle/fileio/JsonFileIOSpec.scala](src/test/scala/de/htwg/se/muehle/fileio/JsonFileIOSpec.scala))
- Test: Save und Load eines GameState
- Test: Fehlerbehandlung bei ungültigen Pfaden
- Test: Fehlerbehandlung bei nicht existierenden Dateien

## Dependencies

In [build.sbt](build.sbt) wurden folgende Abhängigkeiten hinzugefügt:

```scala
"org.scala-lang.modules" %% "scala-xml" % "2.1.0",  // XML support
"com.typesafe.play" %% "play-json" % "2.10.1"       // JSON support
```

## Design Patterns

1. **Strategy Pattern**: FileIOInterface mit zwei austauschbaren Implementierungen
2. **Dependency Injection**: Guice bindet die gewählte Implementierung
3. **Try Monad**: Fehlerbehandlung mit Try[Unit] und Try[GameStateInterface]
4. **Builder Pattern**: GameState.modify für immutable State-Modifikation

## Vorteile dieser Implementierung

1. **Austauschbarkeit**: Einfacher Wechsel zwischen XML und JSON
2. **Erweiterbarkeit**: Neue Formate können einfach hinzugefügt werden (z.B. Binary, YAML)
3. **Testbarkeit**: Mock-Implementierungen können für Tests injiziert werden
4. **Fehlerbehandlung**: Try-Monade verhindert Abstürze bei I/O-Fehlern
5. **Clean Architecture**: Klare Trennung zwischen Interface und Implementierung
6. **Single Responsibility**: Jede Klasse hat genau eine Aufgabe
7. **Open/Closed Principle**: Offen für Erweiterung, geschlossen für Modifikation
