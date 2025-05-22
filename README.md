# Board Game Project (IDATx2003 - Programmering 2)

## Overview

This project is a board game application developed as part of the IDATx2003 Programmering 2 course at NTNU. The primary goal is to implement a functional Snakes & Ladders game with additional features, and optionally extend it to support other board game types like a simplified Monopoly (Mini-Monopoly). The application features a JavaFX graphical user interface and demonstrates concepts such as object-oriented design, file handling, unit testing, and the use of design patterns.

The project is developed in three main parts, iteratively adding features:
*   **Del 1:** Core class design, game logic, version control, and basic unit testing.
*   **Del 2:** File handling (saving/loading games and players), design patterns (e.g., Factory, Observer, Facade).
*   **Del 3:** Completion of the GUI, comprehensive unit testing, and potential custom extensions.

## Features

*   **Game Variants:**
    *   **Snakes & Ladders (Stigespill):**
        *   Classic gameplay on a configurable board.
        *   Dice rolling to determine movement.
        *   Snakes (move backward) and Ladders (move forward).
        *   Special "Schrödinger's Box" tiles with random outcomes (move to start/finish or no effect).
    *   **Mini-Monopoly (Implied by actions):**
        *   Ownable properties (standard properties, railroads, utilities).
        *   Collection of rent.
        *   Special tiles: Go (collect salary), Jail (visiting), Go To Jail, Free Parking, Tax, Chance, Community Chest.
*   **Player Management:**
    *   Support for multiple players (1-4 players for Snakes & Ladders).
    *   Player piece selection/identification.
    *   Saving and loading player configurations from/to CSV files.
*   **Board Customization:**
    *   Loading board layouts and tile actions from JSON configuration files.
*   **User Interface:**
    *   Graphical User Interface built with JavaFX (programmatic, without FXML).
    *   Separate scenes for different game variants (Snakes & Ladders, Monopoly).
    *   Visual representation of the game board and player pieces.
    *   Interactive elements for player actions (e.g., rolling dice, making choices).
*   **Controller Logic:**
    *   `GameController` manages game flow, player turns, and UI updates via the Observer pattern.
    *   `PlayerSetupController` handles loading and saving player configurations.

## Project Requirements (Summary)

(Based on the "Mappeoppgave: Boardgame" document)

*   **Core Gameplay:** Implement a Snakes & Ladders game as the Minimum Viable Product (MVP).
    *   Board with numbered tiles
    *   Players roll two dice.
    *   Snakes and Ladders functionality.
    *   "Noe ekstra": Special tiles like "return to start," "skip a turn," or similar (Schrödinger's Box implemented).
*   **Level 2 (Optional Extension):** Implement other types of board games (e.g., a simplified Monopoly).
*   **Technical Requirements:**
    *   **Java:** Use Java 21 LTS.
    *   **Build Tool:** Maven.
    *   **GUI:** JavaFX, implemented programmatically (no FXML).
    *   **Version Control:** Git (e.g., GitHub/GitLab).
    *   **File Handling:**
        *   Load board configurations from JSON files.
        *   Save/load player lists from CSV files.
    *   **Code Quality:**
        *   Use CheckStyle (Google rules) and SonarLint.
        *   High-quality code with good structure, Javadoc documentation.
        *   Robustness (exception handling, input validation).
    *   **Unit Testing:** JUnit 5 for business-critical logic. Avoid tests that read/write to actual files during regular test runs (use mocks or in-memory streams).
    *   **Design Patterns:** Apply relevant design patterns (e.g., Observer for UI updates, Factory for object creation, Facade for simplifying interfaces).
    *   **AI Tool Usage:** Permitted as a sparring partner/advisor, but code generation for direct use is restricted. Usage must be documented.
*   **Deliverables:**
    *   Source code.
    *   Project report (approx. 2500-3000 words).
    *   Final solution as a ZIP file submitted via Inspera.

## Core Domain Model

The project's model is centered around the following key classes:

*   `BoardGame`: Facade for the game, manages overall game state, players, board, dice, and observers.
*   `Board`: Represents the game board, containing a collection of `Tile` objects.
*   `Tile`: Represents a single square on the board, can have an associated `TileAction`. Tiles are linked (next/previous).
*   `Player`: Represents a player in the game, with a name, current tile, piece identifier, and money (for Monopoly).
*   `Dice` & `Die`: Represent the game dice (a collection of individual dice).
*   `TileAction` (Interface): Defines actions triggered when a player lands on a tile.
    *   **Snakes & Ladders Actions:** `LadderAction`, `SnakeAction`, `SchrodingerBoxAction`.
    *   **Monopoly Actions:** `PropertyAction` (and subclasses `RailroadAction`, `UtilityAction`), `GoAction`, `JailAction`, `GoToJailAction`, `TaxAction`, `ChanceAction`, `CommunityChestAction`, `FreeParkingAction`.
*   `Card` (Model): Represents cards (e.g., Chance/Community Chest) with properties defined in JSON.
*   `BoardGameObserver` (Interface): Used by UI components to observe and react to game state changes.

## Technologies Used

*   **Language:** Java 21 LTS
*   **Build Tool:** Apache Maven
*   **GUI Framework:** OpenJFX (JavaFX) 23.0.1
*   **Testing:** JUnit 5 (Jupiter)
*   **Mocking (for tests):** Mockito
*   **JSON Processing:** Google Gson (for `Card` class and potentially `BoardJsonReaderWriter`)
*   **Version Control:** Git

## Prerequisites

To build and run this project, you will need:
*   Java Development Kit (JDK) 21 or later (configured for Java 21 LTS).
*   Apache Maven 3.6.x or later.
*   An IDE that supports Maven projects (e.g., IntelliJ IDEA, Eclipse, VS Code).

## Getting Started

```bash
git clone https://github.com/LiamLande/prog-2-fx-proj
cd IDATT2003-Mappe-Boardgame
```


### Building the Project

Use Maven to compile the project and download dependencies:
```bash
mvn clean install
```

Alternatively, to just compile:
```bash
mvn clean compile
```


### Running the Application

The application is a JavaFX application. It can be run using the JavaFX Maven plugin:
```bash
mvn javafx:run
```

The main class that starts the application is likely `edu.ntnu.idi.bidata.ui.MainApp`

## Running Tests

To run the JUnit unit tests for the project:
```bash
mvn test
```

Test reports can usually be found in `target/surefire-reports/`.

## Project Structure

The project follows a standard Maven directory layout. Key source code packages under `src/main/java/edu/ntnu/idi/bidata/` include:

*   `app/`: Contains application-level configurations like `GameVariant`.
*   `controller/`: Contains controller classes (`GameController`, `PlayerSetupController`) that mediate between the model and the UI.
*   `exception/`: Custom exception classes.
*   `file/`: Classes for reading and writing game data (`BoardJsonReaderWriter`, `PlayerCsvReaderWriter`).
*   `model/`: Core domain logic, including entities (`Player`, `Board`, `Tile`, etc.) and actions (`model/actions/`).
*   `service/`: Service interfaces and locators (`MonopolyService`, `ServiceLocator`).
*   `ui/`: User interface classes built with JavaFX, organized into sub-packages for different game scenes (e.g., `ui/sl/`, `ui/monopoly/`).
*   `util/`: Utility classes (`CsvUtils`, `JsonUtils`, `Logger`).

## Configuration

### Board Definitions (JSON)

The game board layouts, tile connections, and special actions for tiles are defined in JSON files. `BoardJsonReaderWriter` is responsible for parsing these files. An example structure might be:

```json
{
  "tiles": [
    {"id": 0, "nextId": 1},
    {"id": 1, "nextId": 2, "action": {"type": "LadderAction", "description": "Climb!", "steps": 5}},
    {"id": 2, "nextId": 3, "action": {"type": "PropertyAction", "name": "Old Kent Road", "cost": 60, "rent": 2, "colorGroup": "Brown"}}
    // ... more tiles
  ]
}
```

These files are typically loaded by the application to set up a specific game board. (src/main/resources/data/boards/).

### Player Setups (CSV)

Player names and their chosen piece identifiers can be saved and loaded from CSV files. `PlayerSetupController` and `PlayerCsvReaderWriter` handle this. The format used by `PlayerCsvReaderWriter` is:
`PlayerName,TileID,PieceIdentifier`
(TileID is often a placeholder like 0 during setup saving/loading, as the actual starting tile is determined by the board).

Users can choose where they want to save this file and keep it stores.

Example `players.csv`:
```csv
Alice,0,tokenA
Bob,0,tokenB
```


## Authors

*   Alexander Owren Elton (alexanoe@stud.ntnu.no)
*   Liam Schreiner Lande (liamsl@stud.ntnu.no

## License
This project is licensed under the MIT License - see the `LICENSE.md` file for details





