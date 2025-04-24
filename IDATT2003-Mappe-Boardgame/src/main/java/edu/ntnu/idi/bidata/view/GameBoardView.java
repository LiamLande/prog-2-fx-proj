//package edu.ntnu.idi.bidata.view;
//
//import edu.ntnu.idi.bidata.controller.GameController;
//import edu.ntnu.idi.bidata.model.Board;
//import edu.ntnu.idi.bidata.model.Player;
//import edu.ntnu.idi.bidata.model.Tile;
//import edu.ntnu.idi.bidata.model.actions.LadderAction;
//import edu.ntnu.idi.bidata.model.actions.SnakeAction;
//import edu.ntnu.idi.bidata.model.actions.TileAction;
//import javafx.animation.PauseTransition;
//import javafx.application.Platform;
//import javafx.geometry.Insets;
//import javafx.geometry.Pos;
//import javafx.scene.Group;
//import javafx.scene.Node;
//import javafx.scene.control.Alert;
//import javafx.scene.control.Button;
//import javafx.scene.control.Label;
//import javafx.scene.effect.DropShadow;
//import javafx.scene.layout.*;
//import javafx.scene.paint.Color;
//import javafx.scene.shape.*;
//import javafx.scene.text.Font;
//import javafx.scene.text.FontWeight;
//import javafx.scene.text.Text;
//import javafx.util.Duration;
//
//import java.util.*;
//
///**
// * Tile-based visual representation of the Snakes and Ladders game board
// * with direct placement of snakes and ladders
// */
//public class DirectSnakesLaddersView extends BorderPane implements GameController.GameListener {
//
//    private final GameController gameController;
//    private final int BOARD_SIZE = 10; // 10x10 grid for 100 tiles
//    private final int TILE_SIZE = 60;
//    private final Map<Integer, StackPane> tileViews = new HashMap<>();
//
//    // Maps players to their current tile IDs
//    private final Map<Player, Integer> playerPositions = new HashMap<>();
//
//    private GridPane tileGrid;
//    private Label statusLabel;
//    private Button rollButton;
//    private Label diceLabel;
//    private VBox playerInfoPanel;
//    private Pane pathsLayer;
//
//    /**
//     * Constructor
//     *
//     * @param gameController The game controller
//     */
//    public DirectSnakesLaddersView(GameController gameController) {
//        this.gameController = gameController;
//        gameController.addGameListener(this);
//        setupUI();
//
//        // Use a slight delay to ensure layout is complete
//        PauseTransition delay = new PauseTransition(Duration.millis(100));
//        delay.setOnFinished(event -> initializeAfterLayout());
//        delay.play();
//    }
//
//    /**
//     * Initialize components that require the layout to be complete
//     */
//    private void initializeAfterLayout() {
//        // Debug check to make sure pathsLayer exists
//        if (pathsLayer == null) {
//            System.err.println("pathsLayer is null during initialization");
//            return;
//        }
//
//        // Clear any existing content
//        pathsLayer.getChildren().clear();
//
//        // Create snakes and ladders directly
//        createDirectSnakesAndLadders();
//
//        // Initialize all players at position 0
//        initializePlayerPositions();
//
//        // Debug to verify the paths were added
//        System.out.println("Number of elements in pathsLayer: " + pathsLayer.getChildren().size());
//    }
//
//    /**
//     * Initialize all players at the starting position
//     */
//    private void initializePlayerPositions() {
//        List<Player> players = gameController.getPlayers();
//        for (Player player : players) {
//            playerPositions.put(player, 0);
//        }
//
//        // Render all players on their tiles
//        refreshPlayerPositions();
//    }
//
//    /**
//     * Set up the UI components
//     */
//    private void setupUI() {
//        // Create the main game board
//        Pane boardPane = createGameBoard();
//
//        // Create control panel
//        VBox controlPanel = createControlPanel();
//
//        // Create player info panel
//        playerInfoPanel = createPlayerInfoPanel();
//
//        // Add components to the border pane
//        this.setCenter(boardPane);
//        this.setBottom(controlPanel);
//        this.setRight(playerInfoPanel);
//
//        // Style the board
//        this.setPadding(new Insets(20));
//        this.setStyle("-fx-background-color: linear-gradient(to bottom, #87CEFA, #E0F7FA);");
//    }
//
//    /**
//     * Create the game board with tiles and paths layer
//     *
//     * @return Pane containing the game board
//     */
//    private Pane createGameBoard() {
//        StackPane boardStack = new StackPane();
//        boardStack.setPrefSize(BOARD_SIZE * TILE_SIZE, BOARD_SIZE * TILE_SIZE);
//        boardStack.setMinSize(BOARD_SIZE * TILE_SIZE, BOARD_SIZE * TILE_SIZE);
//        boardStack.setMaxSize(BOARD_SIZE * TILE_SIZE, BOARD_SIZE * TILE_SIZE);
//
//        // Background image or color
//        Rectangle background = new Rectangle(
//                BOARD_SIZE * TILE_SIZE,
//                BOARD_SIZE * TILE_SIZE,
//                Color.web("#f5f5dc") // Beige background
//        );
//
//        // Create paths layer for snakes and ladders
//        pathsLayer = new Pane();
//        pathsLayer.setPrefSize(BOARD_SIZE * TILE_SIZE, BOARD_SIZE * TILE_SIZE);
//        pathsLayer.setMinSize(BOARD_SIZE * TILE_SIZE, BOARD_SIZE * TILE_SIZE);
//        pathsLayer.setMaxSize(BOARD_SIZE * TILE_SIZE, BOARD_SIZE * TILE_SIZE);
//
//        // Create grid for tiles
//        tileGrid = new GridPane();
//        tileGrid.setAlignment(Pos.CENTER);
//
//        // Create tiles in a snake pattern (from bottom to top, alternating left-to-right and right-to-left)
//        for (int row = 0; row < BOARD_SIZE; row++) {
//            for (int col = 0; col < BOARD_SIZE; col++) {
//                int tileNumber;
//
//                // Calculate tile number based on row and column
//                if (row % 2 == 0) {
//                    // Even rows go left to right
//                    tileNumber = (BOARD_SIZE - 1 - row) * BOARD_SIZE + col;
//                } else {
//                    // Odd rows go right to left
//                    tileNumber = (BOARD_SIZE - 1 - row) * BOARD_SIZE + (BOARD_SIZE - 1 - col);
//                }
//
//                // Create tile view
//                StackPane tileView = createTileView(tileNumber);
//                tileViews.put(tileNumber, tileView);
//
//                // Add tile to grid
//                tileGrid.add(tileView, col, BOARD_SIZE - 1 - row);
//            }
//        }
//
//        // Stack all layers
//        boardStack.getChildren().addAll(background, tileGrid, pathsLayer);
//
//        // Add a border to the board
//        boardStack.setStyle("-fx-border-color: #8B4513; -fx-border-width: 8px; -fx-border-radius: 10px;");
//        boardStack.setEffect(new DropShadow(15, Color.GRAY));
//
//        return boardStack;
//    }
//
//    /**
//     * Create a visual representation of a tile
//     *
//     * @param tileNumber The tile number
//     * @return StackPane representing the tile
//     */
//    private StackPane createTileView(int tileNumber) {
//        StackPane tileView = new StackPane();
//        tileView.setPrefSize(TILE_SIZE, TILE_SIZE);
//        tileView.setMinSize(TILE_SIZE, TILE_SIZE);
//        tileView.setMaxSize(TILE_SIZE, TILE_SIZE);
//
//        // Set the background color alternating between light and dark
//        if ((tileNumber / BOARD_SIZE) % 2 == 0) {
//            tileView.setStyle(tileNumber % 2 == 0 ?
//                    "-fx-background-color: #FFF8DC; -fx-border-color: #8B4513; -fx-border-width: 1px;" :
//                    "-fx-background-color: #F5DEB3; -fx-border-color: #8B4513; -fx-border-width: 1px;");
//        } else {
//            tileView.setStyle(tileNumber % 2 == 0 ?
//                    "-fx-background-color: #F5DEB3; -fx-border-color: #8B4513; -fx-border-width: 1px;" :
//                    "-fx-background-color: #FFF8DC; -fx-border-color: #8B4513; -fx-border-width: 1px;");
//        }
//
//        // Special styling for start and finish tiles
//        if (tileNumber == 0) {
//            tileView.setStyle("-fx-background-color: #98FB98; -fx-border-color: #006400; -fx-border-width: 2px;");
//        } else if (tileNumber == 99) {
//            tileView.setStyle("-fx-background-color: #FFD700; -fx-border-color: #B8860B; -fx-border-width: 2px;");
//        }
//
//        // Add tile number
//        Text tileNumberText = new Text(Integer.toString(tileNumber));
//        tileNumberText.setFont(Font.font("Arial", FontWeight.BOLD, 14));
//        tileNumberText.setFill(Color.web("#8B4513"));
//
//        // Add the tile number at the top-left corner
//        StackPane.setAlignment(tileNumberText, Pos.TOP_LEFT);
//        StackPane.setMargin(tileNumberText, new Insets(3, 0, 0, 3));
//
//        tileView.getChildren().add(tileNumberText);
//        tileView.setId("tile_" + tileNumber);
//
//        return tileView;
//    }
//
//    /**
//     * Create snakes and ladders using the game model configuration
//     */
//    private void createDirectSnakesAndLadders() {
//        if (pathsLayer == null) {
//            return;
//        }
//
//        // Clear any existing content
//        pathsLayer.getChildren().clear();
//
//        // Get the game board from the controller
//        Board board = gameController.getGame().getBoard();
//        if (board == null) {
//            System.err.println("Game board is null when trying to create snakes and ladders");
//            return;
//        }
//
//        // Track drawn elements to avoid duplicates
//        Set<Integer> processedTiles = new HashSet<>();
//
//        // Loop through all tiles to find snakes and ladders
//        for (int i = 0; i < 100; i++) {
//            if (processedTiles.contains(i)) {
//                continue;
//            }
//
//            Tile tile = board.getTile(i);
//            if (tile == null) {
//                continue;
//            }
//
//            TileAction action = tile.getLandAction();
//
//            if (action instanceof LadderAction) {
//                // For ladders, calculate the destination tile
//                int fromTile = tile.getTileId();
//
//                // Move a player temporarily to calculate the destination
//                Player tempPlayer = new Player("temp", gameController.getGame());
//                tempPlayer.setCurrentTile(tile);
//
//                // Store current tile before performing action
//                Tile currentTile = tempPlayer.getCurrentTile();
//
//                // Perform the ladder action
//                action.perform(tempPlayer);
//
//                // Get the destination tile
//                int toTile = tempPlayer.getCurrentTile().getTileId();
//
//                // Only draw if there's actually movement
//                if (toTile > fromTile) {
//                    drawLadder(fromTile, toTile, Color.FORESTGREEN);
//                    processedTiles.add(fromTile);
//                    System.out.println("Drawing ladder from " + fromTile + " to " + toTile);
//                }
//
//                // Reset the temp player's position
//                tempPlayer.setCurrentTile(currentTile);
//
//            } else if (action instanceof SnakeAction) {
//                // For snakes, calculate the destination tile
//                int fromTile = tile.getTileId();
//
//                // Move a player temporarily to calculate the destination
//                Player tempPlayer = new Player("temp", gameController.getGame());
//                tempPlayer.setCurrentTile(tile);
//
//                // Store current tile before performing action
//                Tile currentTile = tempPlayer.getCurrentTile();
//
//                // Perform the snake action
//                action.perform(tempPlayer);
//
//                // Get the destination tile
//                int toTile = tempPlayer.getCurrentTile().getTileId();
//
//                // Only draw if there's actually movement
//                if (toTile < fromTile) {
//                    drawSnake(fromTile, toTile, fromTile % 2 == 0 ? Color.web("#FF4500") : Color.web("#8B0000"));
//                    processedTiles.add(fromTile);
//                    System.out.println("Drawing snake from " + fromTile + " to " + toTile);
//                }
//
//                // Reset the temp player's position
//                tempPlayer.setCurrentTile(currentTile);
//            }
//        }
//    }
//
//    /**
//     * Draw a ladder directly on the paths layer
//     *
//     * @param fromTile Starting tile number
//     * @param toTile Ending tile number
//     * @param color Color of the ladder
//     */
//    private void drawLadder(int fromTile, int toTile, Color color) {
//        // Board pattern:
//        // 9  8  7  6  5  4  3  2  1  0   (top row, right to left)
//        // 10 11 12 13 14 15 16 17 18 19  (second row, left to right)
//        // 29 28 27 26 25 24 23 22 21 20  (third row, right to left)
//        // And so on...
//
//        int from = fromTile;
//        int to = toTile;
//
//        // From tile: Calculate row and column
//        int fromRow = from / BOARD_SIZE;  // Integer division gives row index from top (0-based)
//        int fromCol;
//
//        if (fromRow % 2 == 0) {
//            // Even rows (0, 2, 4...) go right to left
//            fromCol = BOARD_SIZE - 1 - (from % BOARD_SIZE);
//        } else {
//            // Odd rows (1, 3, 5...) go left to right
//            fromCol = from % BOARD_SIZE;
//        }
//
//        // To tile: Calculate row and column
//        int toRow = to / BOARD_SIZE;
//        int toCol;
//
//        if (toRow % 2 == 0) {
//            // Even rows (0, 2, 4...) go right to left
//            toCol = BOARD_SIZE - 1 - (to % BOARD_SIZE);
//        } else {
//            // Odd rows (1, 3, 5...) go left to right
//            toCol = to % BOARD_SIZE;
//        }
//
//        // Calculate center coordinates
//        double startX = fromCol * TILE_SIZE + TILE_SIZE / 2;
//        double startY = fromRow * TILE_SIZE + TILE_SIZE / 2;
//        double endX = toCol * TILE_SIZE + TILE_SIZE / 2;
//        double endY = toRow * TILE_SIZE + TILE_SIZE / 2;
//
//        // Debug output
//        System.out.println("Drawing ladder from tile " + from + " to " + to);
//        System.out.println("  From: row=" + fromRow + ", col=" + fromCol + " (" + startX + "," + startY + ")");
//        System.out.println("  To: row=" + toRow + ", col=" + toCol + " (" + endX + "," + endY + ")");
//
//        // Create the ladder
//        Group ladder = new Group();
//
//        // Calculate ladder width and angle
//        double ladderWidth = TILE_SIZE * 0.35;
//        double angle = Math.atan2(endY - startY, endX - startX);
//
//        // Rails
//        Line leftRail = new Line(
//                startX - Math.cos(angle + Math.PI/2) * ladderWidth/2,
//                startY - Math.sin(angle + Math.PI/2) * ladderWidth/2,
//                endX - Math.cos(angle + Math.PI/2) * ladderWidth/2,
//                endY - Math.sin(angle + Math.PI/2) * ladderWidth/2
//        );
//        leftRail.setStroke(color);
//        leftRail.setStrokeWidth(5);
//        leftRail.setStrokeLineCap(StrokeLineCap.ROUND);
//
//        Line rightRail = new Line(
//                startX + Math.cos(angle + Math.PI/2) * ladderWidth/2,
//                startY + Math.sin(angle + Math.PI/2) * ladderWidth/2,
//                endX + Math.cos(angle + Math.PI/2) * ladderWidth/2,
//                endY + Math.sin(angle + Math.PI/2) * ladderWidth/2
//        );
//        rightRail.setStroke(color);
//        rightRail.setStrokeWidth(5);
//        rightRail.setStrokeLineCap(StrokeLineCap.ROUND);
//
//        ladder.getChildren().addAll(leftRail, rightRail);
//
//        // Add rungs
//        double ladderLength = Math.sqrt(Math.pow(endX - startX, 2) + Math.pow(endY - startY, 2));
//        int numRungs = (int) (ladderLength / (TILE_SIZE * 0.4)) + 1;
//
//        for (int i = 0; i <= numRungs; i++) {
//            double ratio = i / (double) numRungs;
//            double rungX = startX + ratio * (endX - startX);
//            double rungY = startY + ratio * (endY - startY);
//
//            Line rung = new Line(
//                    rungX - Math.cos(angle + Math.PI/2) * ladderWidth/2,
//                    rungY - Math.sin(angle + Math.PI/2) * ladderWidth/2,
//                    rungX + Math.cos(angle + Math.PI/2) * ladderWidth/2,
//                    rungY + Math.sin(angle + Math.PI/2) * ladderWidth/2
//            );
//            rung.setStroke(color);
//            rung.setStrokeWidth(3);
//            rung.setStrokeLineCap(StrokeLineCap.ROUND);
//
//            ladder.getChildren().add(rung);
//        }
//
//        // Add the ladder to the paths layer
//        pathsLayer.getChildren().add(ladder);
//    }
//
//    /**
//     * Draw a snake directly on the paths layer
//     *
//     * @param fromTile Starting tile number
//     * @param toTile Ending tile number
//     * @param color Color of the snake
//     */
//    private void drawSnake(int fromTile, int toTile, Color color) {
//        // Board pattern:
//        // 9  8  7  6  5  4  3  2  1  0   (top row, right to left)
//        // 10 11 12 13 14 15 16 17 18 19  (second row, left to right)
//        // 29 28 27 26 25 24 23 22 21 20  (third row, right to left)
//
//        int from = fromTile;
//        int to = toTile;
//
//        // From tile: Calculate row and column
//        int fromRow = from / BOARD_SIZE;
//        int fromCol;
//
//        if (fromRow % 2 == 0) {
//            // Even rows (0, 2, 4...) go right to left
//            fromCol = BOARD_SIZE - 1 - (from % BOARD_SIZE);
//        } else {
//            // Odd rows (1, 3, 5...) go left to right
//            fromCol = from % BOARD_SIZE;
//        }
//
//        // To tile: Calculate row and column
//        int toRow = to / BOARD_SIZE;
//        int toCol;
//
//        if (toRow % 2 == 0) {
//            // Even rows (0, 2, 4...) go right to left
//            toCol = BOARD_SIZE - 1 - (to % BOARD_SIZE);
//        } else {
//            // Odd rows (1, 3, 5...) go left to right
//            toCol = to % BOARD_SIZE;
//        }
//
//        // Calculate center coordinates
//        double startX = fromCol * TILE_SIZE + TILE_SIZE / 2;
//        double startY = fromRow * TILE_SIZE + TILE_SIZE / 2;
//        double endX = toCol * TILE_SIZE + TILE_SIZE / 2;
//        double endY = toRow * TILE_SIZE + TILE_SIZE / 2;
//
//        // Debug output
//        System.out.println("Drawing snake from tile " + from + " to " + to);
//        System.out.println("  From: row=" + fromRow + ", col=" + fromCol + " (" + startX + "," + startY + ")");
//        System.out.println("  To: row=" + toRow + ", col=" + toCol + " (" + endX + "," + endY + ")");
//
//        // Create the snake
//        Group snake = new Group();
//
//        // Calculate control points for a wavy snake shape
//        double distance = Math.sqrt(Math.pow(endX - startX, 2) + Math.pow(endY - startY, 2));
//        double midX = (startX + endX) / 2;
//        double midY = (startY + endY) / 2;
//
//        // Calculate perpendicular offset for control points
//        double dx = endX - startX;
//        double dy = endY - startY;
//        double perpX = -dy / distance;
//        double perpY = dx / distance;
//
//        // Create a more realistic snake path
//        Path snakePath = new Path();
//
//        // Start point
//        MoveTo moveTo = new MoveTo(startX, startY);
//
//        // First curve (head to middle)
//        QuadCurveTo curve1 = new QuadCurveTo(
//                midX + perpX * distance * 0.3,
//                midY + perpY * distance * 0.3,
//                midX, midY
//        );
//
//        // Second curve (middle to tail)
//        QuadCurveTo curve2 = new QuadCurveTo(
//                midX - perpX * distance * 0.3,
//                midY - perpY * distance * 0.3,
//                endX, endY
//        );
//
//        snakePath.getElements().addAll(moveTo, curve1, curve2);
//
//        // Style the snake
//        snakePath.setStroke(color);
//        snakePath.setStrokeWidth(10);
//        snakePath.setStrokeLineCap(StrokeLineCap.ROUND);
//        snakePath.setStrokeLineJoin(StrokeLineJoin.ROUND);
//        snakePath.setFill(null);
//
//        // Add snake head
//        Circle head = new Circle(startX, startY, TILE_SIZE * 0.2);
//        head.setFill(color);
//
//        // Add eyes
//        double angle = Math.atan2(midY - startY, midX - startX);
//        double eyeOffsetX = Math.cos(angle + Math.PI/4) * TILE_SIZE * 0.1;
//        double eyeOffsetY = Math.sin(angle + Math.PI/4) * TILE_SIZE * 0.1;
//
//        Circle leftEye = new Circle(startX + eyeOffsetX, startY + eyeOffsetY, TILE_SIZE * 0.05);
//        leftEye.setFill(Color.WHITE);
//
//        Circle leftPupil = new Circle(startX + eyeOffsetX, startY + eyeOffsetY, TILE_SIZE * 0.025);
//        leftPupil.setFill(Color.BLACK);
//
//        eyeOffsetX = Math.cos(angle - Math.PI/4) * TILE_SIZE * 0.1;
//        eyeOffsetY = Math.sin(angle - Math.PI/4) * TILE_SIZE * 0.1;
//
//        Circle rightEye = new Circle(startX + eyeOffsetX, startY + eyeOffsetY, TILE_SIZE * 0.05);
//        rightEye.setFill(Color.WHITE);
//
//        Circle rightPupil = new Circle(startX + eyeOffsetX, startY + eyeOffsetY, TILE_SIZE * 0.025);
//        rightPupil.setFill(Color.BLACK);
//
//        // Add all snake elements to the group
//        snake.getChildren().addAll(snakePath, head, leftEye, leftPupil, rightEye, rightPupil);
//
//        // Add the snake to the paths layer
//        pathsLayer.getChildren().add(snake);
//    }
//
//    /**
//     * Create control panel with roll button and status
//     *
//     * @return VBox containing the control panel
//     */
//    private VBox createControlPanel() {
//        VBox panel = new VBox(15);
//        panel.setPadding(new Insets(20));
//        panel.setAlignment(Pos.CENTER);
//        panel.setStyle("-fx-background-color: #8B4513; -fx-border-radius: 10px;");
//
//        // Status label
//        statusLabel = new Label("Game Ready! Roll the dice to start.");
//        statusLabel.setFont(Font.font("Arial", FontWeight.BOLD, 16));
//        statusLabel.setTextFill(Color.WHITE);
//        statusLabel.setWrapText(true);
//        statusLabel.setMaxWidth(600);
//
//        // Dice result
//        HBox diceBox = new HBox(10);
//        diceBox.setAlignment(Pos.CENTER);
//
//        diceLabel = new Label("?");
//        diceLabel.setFont(Font.font("Arial", FontWeight.BOLD, 24));
//        diceLabel.setTextFill(Color.WHITE);
//        diceLabel.setStyle("-fx-background-color: #B8860B; -fx-padding: 10px 20px; -fx-background-radius: 5px;");
//
//        diceBox.getChildren().add(diceLabel);
//
//        // Roll button
//        rollButton = new Button("Roll Dice");
//        rollButton.setFont(Font.font("Arial", FontWeight.BOLD, 16));
//        rollButton.setPrefSize(150, 40);
//        rollButton.setStyle("-fx-background-color: #F5DEB3; -fx-text-fill: #8B4513;");
//
//        rollButton.setOnAction(e -> {
//            if (gameController.isGameRunning()) {
//                rollButton.setDisable(true);
//                gameController.rollDiceAndMove();
//            }
//        });
//
//        panel.getChildren().addAll(statusLabel, diceBox, rollButton);
//        return panel;
//    }
//
//    /**
//     * Create player info panel
//     *
//     * @return VBox containing player information
//     */
//    private VBox createPlayerInfoPanel() {
//        VBox panel = new VBox(15);
//        panel.setPadding(new Insets(20));
//        panel.setPrefWidth(200);
//        panel.setStyle("-fx-background-color: #8B4513; -fx-border-radius: 10px;");
//
//        // Title
//        Label titleLabel = new Label("Players");
//        titleLabel.setFont(Font.font("Arial", FontWeight.BOLD, 18));
//        titleLabel.setTextFill(Color.WHITE);
//
//        panel.getChildren().add(titleLabel);
//
//        // Player info
//        List<Player> players = gameController.getPlayers();
//        String[] colors = {"#1E90FF", "#FF4500", "#32CD32", "#FFD700"};
//
//        for (int i = 0; i < players.size(); i++) {
//            Player player = players.get(i);
//
//            HBox playerBox = new HBox(10);
//            playerBox.setAlignment(Pos.CENTER_LEFT);
//
//            // Player token indicator
//            Circle indicator = new Circle(10);
//            indicator.setFill(Color.web(colors[i % colors.length]));
//            indicator.setStroke(Color.WHITE);
//            indicator.setStrokeWidth(1);
//
//            // Player name
//            Label nameLabel = new Label(player.getName());
//            nameLabel.setFont(Font.font("Arial", 14));
//            nameLabel.setTextFill(Color.WHITE);
//
//            // Current position
//            Label positionLabel = new Label("Position: 0");
//            positionLabel.setFont(Font.font("Arial", 12));
//            positionLabel.setTextFill(Color.LIGHTGRAY);
//            positionLabel.setId("position_" + player.getName());
//
//            VBox playerInfo = new VBox(5);
//            playerInfo.getChildren().addAll(nameLabel, positionLabel);
//
//            playerBox.getChildren().addAll(indicator, playerInfo);
//
//            // Highlight current player
//            if (player == gameController.getCurrentPlayer()) {
//                playerBox.setStyle("-fx-background-color: rgba(255,255,255,0.2); -fx-background-radius: 5px; -fx-padding: 5px;");
//            } else {
//                playerBox.setStyle("-fx-padding: 5px;");
//            }
//
//            playerBox.setId("player_" + player.getName());
//
//            panel.getChildren().add(playerBox);
//        }
//
//        return panel;
//    }
//
//    /**
//     * Update UI to highlight current player
//     *
//     * @param currentPlayer The current player
//     */
//    private void updateCurrentPlayerHighlight(Player currentPlayer) {
//        for (Node node : playerInfoPanel.getChildren()) {
//            if (node instanceof HBox && node.getId() != null && node.getId().startsWith("player_")) {
//                String playerName = node.getId().substring("player_".length());
//
//                if (playerName.equals(currentPlayer.getName())) {
//                    node.setStyle("-fx-background-color: rgba(255,255,255,0.2); -fx-background-radius: 5px; -fx-padding: 5px;");
//                } else {
//                    node.setStyle("-fx-padding: 5px;");
//                }
//            }
//        }
//    }
//
//    /**
//     * Update the position label for a player
//     *
//     * @param player The player
//     * @param position The new position
//     */
//    private void updatePlayerPosition(Player player, int position) {
//        // Update position in side panel
//        for (Node node : playerInfoPanel.getChildren()) {
//            if (node instanceof HBox && node.getId() != null && node.getId().equals("player_" + player.getName())) {
//                HBox playerBox = (HBox) node;
//                for (Node infoNode : playerBox.getChildren()) {
//                    if (infoNode instanceof VBox) {
//                        VBox infoBox = (VBox) infoNode;
//                        for (Node label : infoBox.getChildren()) {
//                            if (label instanceof Label && label.getId() != null &&
//                                    label.getId().equals("position_" + player.getName())) {
//                                ((Label) label).setText("Position: " + position);
//                                break;
//                            }
//                        }
//                        break;
//                    }
//                }
//                break;
//            }
//        }
//
//        // Update internal position tracking
//        playerPositions.put(player, position);
//
//        // Refresh all player positions on the board
//        refreshPlayerPositions();
//    }
//
//    /**
//     * Refresh the display of all player positions on the board
//     */
//    private void refreshPlayerPositions() {
//        // First, clear all player tokens from tiles
//        for (StackPane tile : tileViews.values()) {
//            // Remove all player tokens while keeping the tile number
//            tile.getChildren().removeIf(node -> node instanceof FlowPane && "player_tokens".equals(node.getId()));
//        }
//
//        // Create a map to track which players are on which tiles
//        Map<Integer, List<Player>> playersByTile = new HashMap<>();
//
//        // Group players by tile
//        for (Map.Entry<Player, Integer> entry : playerPositions.entrySet()) {
//            Player player = entry.getKey();
//            Integer tileId = entry.getValue();
//
//            if (!playersByTile.containsKey(tileId)) {
//                playersByTile.put(tileId, new ArrayList<>());
//            }
//            playersByTile.get(tileId).add(player);
//        }
//
//        // Add player tokens to tiles
//        for (Map.Entry<Integer, List<Player>> entry : playersByTile.entrySet()) {
//            Integer tileId = entry.getKey();
//            List<Player> playersOnTile = entry.getValue();
//
//            StackPane tile = tileViews.get(tileId);
//            if (tile != null) {
//                // Container for all player tokens on this tile
//                FlowPane playerTokensPane = new FlowPane();
//                playerTokensPane.setId("player_tokens");
//                playerTokensPane.setAlignment(Pos.CENTER);
//                playerTokensPane.setHgap(5);
//                playerTokensPane.setVgap(5);
//                playerTokensPane.setPrefWrapLength(TILE_SIZE - 10);
//
//                // Add each player token
//                String[] colors = {"#1E90FF", "#FF4500", "#32CD32", "#FFD700"};
//                List<Player> allPlayers = gameController.getPlayers();
//
//                for (Player player : playersOnTile) {
//                    int playerIndex = allPlayers.indexOf(player);
//                    Color tokenColor = Color.web(colors[playerIndex % colors.length]);
//
//                    // Create a simple circle with player's initial
//                    StackPane playerToken = new StackPane();
//                    Circle circle = new Circle(10);
//                    circle.setFill(tokenColor);
//                    circle.setStroke(Color.BLACK);
//                    circle.setStrokeWidth(1.5);
//
//                    Text initial = new Text(player.getName().substring(0, 1));
//                    initial.setFont(Font.font("Arial", FontWeight.BOLD, 10));
//                    initial.setFill(Color.WHITE);
//
//                    playerToken.getChildren().addAll(circle, initial);
//                    playerTokensPane.getChildren().add(playerToken);
//                }
//
//                // Position the player tokens in the center of the tile
//                StackPane.setAlignment(playerTokensPane, Pos.CENTER);
//                tile.getChildren().add(playerTokensPane);
//            }
//        }
//    }
//
//    /**
//     * Show a game over dialog
//     *
//     * @param winner The winning player
//     */
//    private void showGameOverDialog(Player winner) {
//        Platform.runLater(() -> {
//            Alert alert = new Alert(Alert.AlertType.INFORMATION);
//            alert.setTitle("Game Over");
//            alert.setHeaderText("We have a winner!");
//            alert.setContentText(winner.getName() + " has won the game!");
//            alert.showAndWait();
//        });
//    }
//
//    //-----------------------------------------------------------------------------------
//    // GameController.GameListener Implementation
//    //-----------------------------------------------------------------------------------
//
//    @Override
//    public void onRollDice(Player player, int roll) {
//        diceLabel.setText(Integer.toString(roll));
//        statusLabel.setText(player.getName() + " rolled a " + roll);
//    }
//
//    @Override
//    public void onPlayerMove(Player player, int fromTile, int toTile) {
//        updatePlayerPosition(player, toTile);
//    }
//
//    @Override
//    public void onSpecialAction(Player player, String actionType, int fromTile, int toTile) {
//        // Update player position
//        updatePlayerPosition(player, toTile);
//
//        if (actionType.equals("ladder")) {
//            statusLabel.setText(player.getName() + " climbed a ladder from " + fromTile + " to " + toTile + "!");
//        } else {
//            statusLabel.setText(player.getName() + " slid down a snake from " + fromTile + " to " + toTile + "!");
//        }
//    }
//
//    @Override
//    public void onGameOver(Player winner) {
//        statusLabel.setText("Game Over! " + winner.getName() + " has won the game!");
//        rollButton.setDisable(true);
//        showGameOverDialog(winner);
//    }
//
//    @Override
//    public void onNextPlayer(Player player) {
//        updateCurrentPlayerHighlight(player);
//        statusLabel.setText(player.getName() + "'s turn. Roll the dice!");
//        rollButton.setDisable(false);
//    }
//}