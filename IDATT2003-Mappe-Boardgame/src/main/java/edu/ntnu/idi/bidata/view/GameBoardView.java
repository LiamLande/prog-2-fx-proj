//package edu.ntnu.idi.bidata.view;
//
//import edu.ntnu.idi.bidata.controller.GameController;
//import edu.ntnu.idi.bidata.model.Player;
//import javafx.animation.KeyFrame;
//import javafx.animation.KeyValue;
//import javafx.animation.PathTransition;
//import javafx.animation.Timeline;
//import javafx.application.Platform;
//import javafx.geometry.Insets;
//import javafx.geometry.Pos;
//import javafx.scene.Group;
//import javafx.scene.Node;
//import javafx.scene.control.Alert;
//import javafx.scene.control.Button;
//import javafx.scene.control.Label;
//import javafx.scene.effect.DropShadow;
//import javafx.scene.effect.Glow;
//import javafx.scene.image.ImageView;
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
// * Improved visual representation of the Snakes and Ladders game board
// * with enhanced graphics and animations
// */
//public class GameBoardView extends BorderPane implements GameController.GameListener {
//
//    private final GameController gameController;
//    private final int BOARD_SIZE = 10; // 10x10 grid for 100 tiles
//    private final int TILE_SIZE = 60;
//    private final Map<Integer, StackPane> tileViews = new HashMap<>();
//    private final Map<Player, Circle> playerTokens = new HashMap<>();
//    private final Map<Player, Text> playerLabels = new HashMap<>();
//    private final Map<String, Path> specialPaths = new HashMap<>();
//
//    private Group tokensGroup;
//    private Label statusLabel;
//    private Button rollButton;
//    private Label diceLabel;
//    private VBox playerInfoPanel;
//
//    /**
//     * Constructor
//     *
//     * @param gameController The game controller
//     */
//    public GameBoardView(GameController gameController) {
//        this.gameController = gameController;
//        gameController.addGameListener(this);
//        setupUI();
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
//     * Create the game board with tiles, snakes, and ladders
//     *
//     * @return Pane containing the game board
//     */
//    private Pane createGameBoard() {
//        StackPane boardStack = new StackPane();
//        boardStack.setPrefSize(BOARD_SIZE * TILE_SIZE, BOARD_SIZE * TILE_SIZE);
//
//        // Background image or color
//        Rectangle background = new Rectangle(
//                BOARD_SIZE * TILE_SIZE,
//                BOARD_SIZE * TILE_SIZE,
//                Color.web("#f5f5dc") // Beige background
//        );
//
//        // Create grid for tiles
//        GridPane tileGrid = new GridPane();
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
//        // Create a group for paths (snakes and ladders)
//        Group pathsGroup = new Group();
//
//        // Add snakes and ladders
//        addSnakesAndLadders(pathsGroup);
//
//        // Create a group for player tokens
//        tokensGroup = new Group();
//
//        // Add player tokens
//        createPlayerTokens(tokensGroup);
//
//        // Stack all layers
//        boardStack.getChildren().addAll(background, tileGrid, pathsGroup, tokensGroup);
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
//        tileView.getChildren().add(tileNumberText);
//
//        return tileView;
//    }
//
//    /**
//     * Add visual representations of snakes and ladders to the board
//     *
//     * @param group The group to add snakes and ladders to
//     */
//    private void addSnakesAndLadders(Group group) {
//        // Ladders
//        addLadder(group, 3, 22, Color.FORESTGREEN);
//        addLadder(group, 8, 30, Color.FORESTGREEN);
//        addLadder(group, 28, 84, Color.FORESTGREEN);
//        addLadder(group, 58, 77, Color.FORESTGREEN);
//
//        // Snakes
//        addSnake(group, 16, 6, Color.web("#FF4500"));  // OrangeRed
//        addSnake(group, 47, 26, Color.web("#8B0000")); // DarkRed
//        addSnake(group, 62, 18, Color.web("#FF4500")); // OrangeRed
//        addSnake(group, 87, 24, Color.web("#8B0000")); // DarkRed
//    }
//
//    /**
//     * Add a visual representation of a ladder to the board
//     *
//     * @param group The group to add the ladder to
//     * @param fromTile The starting tile
//     * @param toTile The ending tile
//     * @param color The color of the ladder
//     */
//    private void addLadder(Group group, int fromTile, int toTile, Color color) {
//        StackPane startTileView = tileViews.get(fromTile);
//        StackPane endTileView = tileViews.get(toTile);
//
//        if (startTileView == null || endTileView == null) {
//            return;
//        }
//
//        // Get center coordinates of tiles
//        double startX = startTileView.getBoundsInParent().getCenterX();
//        double startY = startTileView.getBoundsInParent().getCenterY();
//        double endX = endTileView.getBoundsInParent().getCenterX();
//        double endY = endTileView.getBoundsInParent().getCenterY();
//
//        // Draw a nicer ladder with parallel lines and rungs
//        Group ladder = new Group();
//
//        // Calculate ladder width and angle
//        double ladderWidth = TILE_SIZE * 0.35;
//        double ladderLength = Math.sqrt(Math.pow(endX - startX, 2) + Math.pow(endY - startY, 2));
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
//        // Create path for animation
//        Path path = new Path();
//        path.getElements().add(new MoveTo(startX, startY));
//        path.getElements().add(new LineTo(endX, endY));
//        path.setOpacity(0); // Make path invisible
//
//        // Store the path for future animation
//        specialPaths.put("ladder_" + fromTile + "_" + toTile, path);
//
//        ladder.getChildren().add(path);
//        group.getChildren().add(ladder);
//    }
//
//    /**
//     * Add a visual representation of a snake to the board
//     *
//     * @param group The group to add the snake to
//     * @param fromTile The starting tile
//     * @param toTile The ending tile
//     * @param color The color of the snake
//     */
//    private void addSnake(Group group, int fromTile, int toTile, Color color) {
//        StackPane startTileView = tileViews.get(fromTile);
//        StackPane endTileView = tileViews.get(toTile);
//
//        if (startTileView == null || endTileView == null) {
//            return;
//        }
//
//        // Get center coordinates of tiles
//        double startX = startTileView.getBoundsInParent().getCenterX();
//        double startY = startTileView.getBoundsInParent().getCenterY();
//        double endX = endTileView.getBoundsInParent().getCenterX();
//        double endY = endTileView.getBoundsInParent().getCenterY();
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
//        // Create a copy of path for animation (with proper path elements for token movement)
//        Path animPath = new Path();
//        animPath.getElements().addAll(
//                new MoveTo(startX, startY),
//                new QuadCurveTo(
//                        midX + perpX * distance * 0.3,
//                        midY + perpY * distance * 0.3,
//                        midX, midY
//                ),
//                new QuadCurveTo(
//                        midX - perpX * distance * 0.3,
//                        midY - perpY * distance * 0.3,
//                        endX, endY
//                )
//        );
//        animPath.setOpacity(0); // Hide the path
//
//        // Store the path for future animation
//        specialPaths.put("snake_" + fromTile + "_" + toTile, animPath);
//
//        group.getChildren().addAll(snakePath, head, leftEye, leftPupil, rightEye, rightPupil, animPath);
//    }
//
//    /**
//     * Create player tokens
//     *
//     * @param group The group to add tokens to
//     */
//    private void createPlayerTokens(Group group) {
//        List<Player> players = gameController.getPlayers();
//        Color[] tokenColors = {
//                Color.web("#1E90FF"), // DodgerBlue
//                Color.web("#FF4500"), // OrangeRed
//                Color.web("#32CD32"), // LimeGreen
//                Color.web("#FFD700")  // Gold
//        };
//
//        for (int i = 0; i < players.size(); i++) {
//            Player player = players.get(i);
//            Color tokenColor = tokenColors[i % tokenColors.length];
//
//            // Position at starting tile
//            StackPane startTile = tileViews.get(0);
//            double startX = startTile.getBoundsInParent().getCenterX();
//            double startY = startTile.getBoundsInParent().getCenterY();
//
//            // Offset tokens so they don't overlap
//            double offsetX = (i % 2) * TILE_SIZE * 0.3 - TILE_SIZE * 0.15;
//            double offsetY = (i / 2) * TILE_SIZE * 0.3 - TILE_SIZE * 0.15;
//
//            // Create player token
//            Circle token = new Circle(TILE_SIZE * 0.2);
//            token.setFill(tokenColor);
//            token.setStroke(Color.BLACK);
//            token.setStrokeWidth(2);
//            token.setCenterX(startX + offsetX);
//            token.setCenterY(startY + offsetY);
//            token.setEffect(new Glow(0.5));
//
//            // Add a Text with player's initial for identification
//            Text playerInitial = new Text(player.getName().substring(0, 1));
//            playerInitial.setFont(Font.font("Arial", FontWeight.BOLD, 14));
//            playerInitial.setFill(Color.WHITE);
//            playerInitial.setX(startX + offsetX - 5);
//            playerInitial.setY(startY + offsetY + 5);
//
//            group.getChildren().addAll(token, playerInitial);
//
//            // Store token references
//            playerTokens.put(player, token);
//            playerLabels.put(player, playerInitial);
//        }
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
//    }
//
//    /**
//     * Animate dice roll
//     *
//     * @param roll The final dice roll result
//     */
//    private void animateDiceRoll(int roll) {
//        // Create a simple animation for the dice roll
//        Random random = new Random();
//        Timeline timeline = new Timeline();
//
//        // Add several keyframes to show random dice faces
//        for (int i = 0; i < 10; i++) {
//            final int randomRoll = random.nextInt(6) + 1;
//            KeyFrame kf = new KeyFrame(Duration.millis(i * 50),
//                    e -> diceLabel.setText(Integer.toString(randomRoll)));
//            timeline.getKeyFrames().add(kf);
//        }
//
//        // Add the final keyframe to show the actual roll
//        KeyFrame finalFrame = new KeyFrame(Duration.millis(500),
//                e -> diceLabel.setText(Integer.toString(roll)));
//        timeline.getKeyFrames().add(finalFrame);
//
//        // Play the animation
//        timeline.play();
//    }
//
//    /**
//     * Animate player movement on the board
//     *
//     * @param player The player to animate
//     * @param fromTile The starting tile
//     * @param toTile The ending tile
//     */
//    private void animatePlayerMovement(Player player, int fromTile, int toTile) {
//        // Get player token
//        Circle token = playerTokens.get(player);
//        Text label = playerLabels.get(player);
//
//        if (token == null || label == null) return;
//
//        // If it's a direct move (no snake or ladder)
//        StackPane destTile = tileViews.get(toTile);
//        if (destTile == null) return;
//
//        double destX = destTile.getBoundsInParent().getCenterX();
//        double destY = destTile.getBoundsInParent().getCenterY();
//
//        // Apply offset based on player index to prevent overlap
//        List<Player> players = gameController.getPlayers();
//        int playerIndex = players.indexOf(player);
//        double offsetX = (playerIndex % 2) * TILE_SIZE * 0.3 - TILE_SIZE * 0.15;
//        double offsetY = (playerIndex / 2) * TILE_SIZE * 0.3 - TILE_SIZE * 0.15;
//
//        // Create movement animation for token
//        Timeline timeline = new Timeline();
//        timeline.getKeyFrames().add(new KeyFrame(Duration.seconds(0.5),
//                new KeyValue(token.centerXProperty(), destX + offsetX),
//                new KeyValue(token.centerYProperty(), destY + offsetY)
//        ));
//
//        // Create animation for player label
//        Timeline labelTimeline = new Timeline();
//        labelTimeline.getKeyFrames().add(new KeyFrame(Duration.seconds(0.5),
//                new KeyValue(label.xProperty(), destX + offsetX - 5),
//                new KeyValue(label.yProperty(), destY + offsetY + 5)
//        ));
//
//        // Play the animations
//        timeline.play();
//        labelTimeline.play();
//    }
//
//    /**
//     * Animate special action (snake or ladder)
//     *
//     * @param player The player
//     * @param actionType The action type ("snake" or "ladder")
//     * @param fromTile The tile before action
//     * @param toTile The tile after action
//     */
//    private void animateSpecialAction(Player player, String actionType, int fromTile, int toTile) {
//        // Get player token
//        Circle token = playerTokens.get(player);
//        Text label = playerLabels.get(player);
//
//        if (token == null || label == null) return;
//
//        // Calculate current and destination coordinates
//        StackPane startTile = tileViews.get(fromTile);
//        StackPane endTile = tileViews.get(toTile);
//
//        if (startTile == null || endTile == null) return;
//
//        double startX = startTile.getBoundsInParent().getCenterX();
//        double startY = startTile.getBoundsInParent().getCenterY();
//        double endX = endTile.getBoundsInParent().getCenterX();
//        double endY = endTile.getBoundsInParent().getCenterY();
//
//        // Apply offset based on player index
//        List<Player> players = gameController.getPlayers();
//        int playerIndex = players.indexOf(player);
//        double offsetX = (playerIndex % 2) * TILE_SIZE * 0.3 - TILE_SIZE * 0.15;
//        double offsetY = (playerIndex / 2) * TILE_SIZE * 0.3 - TILE_SIZE * 0.15;
//
//        // Create a path for the animation
//        Path path = new Path();
//
//        if (actionType.equals("ladder")) {
//            // Straight line for ladder
//            path.getElements().add(new MoveTo(token.getCenterX(), token.getCenterY()));
//            path.getElements().add(new LineTo(endX + offsetX, endY + offsetY));
//        } else {
//            // Curved path for snake
//            double midX = (startX + endX) / 2;
//            double midY = (startY + endY) / 2;
//            double distance = Math.sqrt(Math.pow(endX - startX, 2) + Math.pow(endY - startY, 2));
//            double perpX = -(endY - startY) / distance * (distance * 0.2);
//            double perpY = (endX - startX) / distance * (distance * 0.2);
//
//            path.getElements().add(new MoveTo(token.getCenterX(), token.getCenterY()));
//            path.getElements().add(new QuadCurveTo(
//                    midX + perpX, midY + perpY,
//                    endX + offsetX, endY + offsetY
//            ));
//        }
//
//        // Create path transitions for token and label
//        PathTransition tokenTransition = new PathTransition();
//        tokenTransition.setDuration(Duration.seconds(1));
//        tokenTransition.setPath(path);
//        tokenTransition.setNode(token);
//
//        Path labelPath = new Path();
//        for (PathElement element : path.getElements()) {
//            if (element instanceof MoveTo) {
//                MoveTo move = (MoveTo) element;
//                labelPath.getElements().add(new MoveTo(label.getX(), label.getY()));
//            } else if (element instanceof LineTo) {
//                LineTo line = (LineTo) element;
//                labelPath.getElements().add(new LineTo(line.getX() - 5, line.getY() + 5));
//            } else if (element instanceof QuadCurveTo) {
//                QuadCurveTo curve = (QuadCurveTo) element;
//                labelPath.getElements().add(new QuadCurveTo(
//                        curve.getControlX() - 5, curve.getControlY() + 5,
//                        curve.getX() - 5, curve.getY() + 5
//                ));
//            }
//        }
//
//        PathTransition labelTransition = new PathTransition();
//        labelTransition.setDuration(Duration.seconds(1));
//        labelTransition.setPath(labelPath);
//        labelTransition.setNode(label);
//
//        // Play the animations
//        tokenTransition.play();
//        labelTransition.play();
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
//        animateDiceRoll(roll);
//        statusLabel.setText(player.getName() + " rolled a " + roll);
//    }
//
//    @Override
//    public void onPlayerMove(Player player, int fromTile, int toTile) {
//        animatePlayerMovement(player, fromTile, toTile);
//        updatePlayerPosition(player, toTile);
//    }
//
//    @Override
//    public void onSpecialAction(Player player, String actionType, int fromTile, int toTile) {
//        animateSpecialAction(player, actionType, fromTile, toTile);
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