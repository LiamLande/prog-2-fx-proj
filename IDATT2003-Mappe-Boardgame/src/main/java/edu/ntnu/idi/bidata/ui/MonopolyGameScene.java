package edu.ntnu.idi.bidata.ui;

import edu.ntnu.idi.bidata.controller.GameController; // Assuming GameController
import edu.ntnu.idi.bidata.model.BoardGame;
import edu.ntnu.idi.bidata.model.Player;
import edu.ntnu.idi.bidata.model.Tile;
import edu.ntnu.idi.bidata.model.actions.monopoly.PropertyAction;
// import edu.ntnu.idi.bidata.model.actions.TileAction; // Not directly used in this class after review
// MonopolyService and ServiceLocator are used by the Controller, not directly by the View here.
// import edu.ntnu.idi.bidata.service.MonopolyService;
// import edu.ntnu.idi.bidata.service.ServiceLocator;
import javafx.beans.value.ChangeListener;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.Point2D;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.stage.Stage; // Kept for SceneManager compatibility
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * MonopolyGameScene displays a Monopoly board game interface.
 * Interacts with a GameController for game logic and updates.
 */
public class MonopolyGameScene implements SceneManager.ControlledScene {
    private final GameController controller;
    private final BoardGame gameModel;
    private final MonopolyBoardView boardView;
    private VBox playerStatusPane;
    private final Map<Player, Label> playerPositionLabels = new HashMap<>();
    private final Map<Player, Label> playerMoneyLabels = new HashMap<>(); // Added for Monopoly
    private final Map<Player, Circle> playerTokenUIs = new HashMap<>(); // Tokens in the side panel
    private final Scene scene;
    private Label diceLabel;
    private Button rollButton;

    private final Runnable onNewGameCallback;
    private final Runnable onHomeCallback;

    public MonopolyGameScene(Stage primaryStage, // Kept for SceneManager
                             GameController gameController,
                             BoardGame gameModel,
                             Runnable newGameAction,
                             Runnable homeAction) {
        this.controller = gameController;
        this.gameModel = gameModel;
        this.onNewGameCallback = newGameAction;
        this.onHomeCallback = homeAction;

        BorderPane root = new BorderPane();
        root.setStyle("-fx-background-color: #335c33;"); // Dark green background

        // Initialize playerStatusPane here as it's needed to populate playerTokenUIs
        playerStatusPane = new VBox(10);
        playerStatusPane.setAlignment(Pos.CENTER);
        // Populate playerTokenUIs map by creating status boxes. This map is needed by MonopolyBoardView
        // for consistent player colors.
        createPlayerStatusBoxes(this.gameModel.getPlayers());

        this.boardView = new MonopolyBoardView(this.gameModel, this.playerTokenUIs); // Now playerTokenUIs is populated
        StackPane boardContainer = createBoardContainer(this.boardView);
        VBox sidePanel = createSidePanel(); // Uses the already initialized playerStatusPane

        root.setCenter(boardContainer);
        root.setRight(sidePanel);
        root.setPadding(new Insets(20));

        scene = new Scene(root, 1250, 950); // Increased size slightly
        scene.getStylesheets().add(
                Objects.requireNonNull(getClass().getClassLoader().getResource("css/monopoly.css")).toExternalForm()
        );
    }

    /**
     * Called by the controller to initiate the game view setup.
     */
    public void initializeView() {
        updateDiceLabel("⚀"); // Start with 1
        updatePlayerStatusDisplay(); // This will call createPlayerStatusBoxes again if needed due to player changes
        if (gameModel != null && !gameModel.getPlayers().isEmpty()) {
            highlightCurrentPlayer(gameModel.getPlayers().getFirst());
        }
        setRollButtonEnabled(true);
        boardView.refresh(); // Refresh board visuals
    }


    public Scene getScene() { return scene; }
    public Parent getRoot() { return scene.getRoot(); } // For SceneManager

    @Override public void onShow() { }
    @Override public void onHide() { }

    public MonopolyBoardView getBoardView() {
        return boardView;
    }

    private StackPane createBoardContainer(MonopolyBoardView board) {
        Group boardGroup = new Group(board);
        Region borderRegion = new Region();
        borderRegion.setStyle("-fx-background-color: #F0E68C; -fx-background-radius: 15px; -fx-border-color: #8B4513; -fx-border-width: 8px; -fx-border-radius: 15px;");

        StackPane container = new StackPane(borderRegion, boardGroup);
        StackPane.setMargin(boardGroup, new Insets(15));
        // Set container size explicitly to hold the board and its border/padding
        double containerSize = MonopolyBoardView.SIZE + 2 * 15 + 2 * 8; // Board + margin + border
        container.setPrefSize(containerSize, containerSize);
        container.setMaxSize(containerSize, containerSize);


        // Resizing logic: If the container were allowed to resize, this would scale the board.
        // Since container size is fixed, this primarily centers the board if its intrinsic size changes (it shouldn't).
        ChangeListener<Number> resizer = (obs, oldVal, newVal) -> {
            double availW = container.getWidth() - 2 * 15; // Margin for boardGroup inside borderRegion
            double availH = container.getHeight() - 2 * 15;
            double scale = Math.min(availW / MonopolyBoardView.SIZE, availH / MonopolyBoardView.SIZE);
            boardGroup.setScaleX(scale);
            boardGroup.setScaleY(scale);
            // Center the scaled group if StackPane alignment doesn't handle it (it usually does)
            boardGroup.setLayoutX((container.getWidth() - (MonopolyBoardView.SIZE * scale)) / 2.0 - StackPane.getMargin(boardGroup).getLeft());
            boardGroup.setLayoutY((container.getHeight() - (MonopolyBoardView.SIZE * scale)) / 2.0 - StackPane.getMargin(boardGroup).getTop());
        };
        container.widthProperty().addListener(resizer);
        container.heightProperty().addListener(resizer);
        // Initial call to position/scale correctly might be needed if dynamic resizing happens
        // Platform.runLater(() -> resizer.changed(null,0,0));
        return container;
    }

    private VBox createSidePanel() {
        VBox sidePanel = new VBox(20);
        sidePanel.setAlignment(Pos.TOP_CENTER);
        sidePanel.setPadding(new Insets(0, 0, 0, 20));
        sidePanel.setPrefWidth(350);

        VBox titleBox = new VBox(5);
        titleBox.setAlignment(Pos.CENTER);
        titleBox.setPadding(new Insets(20));
        titleBox.setStyle("-fx-background-color: #D2B48C; -fx-background-radius: 10px;");
        Label titleLabel = new Label("Mini Monopoly");
        titleLabel.getStyleClass().add("scene-title");
        titleLabel.setFont(Font.font("Kabel", FontWeight.BOLD, 36));
        Label subtitleLabel = new Label("The Market of Fortune");
        subtitleLabel.setFont(Font.font("Kabel", FontWeight.NORMAL, 18));
        subtitleLabel.setTextFill(Color.web("#8B4513"));
        titleBox.getChildren().addAll(titleLabel, subtitleLabel);

        // playerStatusPane is already initialized and populated in constructor or by updatePlayerStatusDisplay
        VBox playerSection = new VBox(15, new Label("Players"), playerStatusPane);
        playerSection.setAlignment(Pos.TOP_CENTER);
        playerSection.setPadding(new Insets(20));
        playerSection.setStyle("-fx-background-color: #D2B48C; -fx-background-radius: 10px;");

        diceLabel = new Label("⚀");
        diceLabel.setFont(Font.font("Kabel", FontWeight.BOLD, 60));
        diceLabel.setTextFill(Color.web("#8B4513"));
        rollButton = new Button("Roll Dice");
        styleButton(rollButton);
        rollButton.setOnAction(e -> controller.handleRollDiceRequest());
        rollButton.setPrefWidth(180);
        VBox diceBox = new VBox(10, diceLabel, rollButton);
        diceBox.setAlignment(Pos.CENTER);
        diceBox.setPadding(new Insets(20));
        diceBox.setStyle("-fx-background-color: #D2B48C; -fx-background-radius: 10px;");

        Button newGameBtn = new Button("New Game");
        styleButton(newGameBtn);
        newGameBtn.setOnAction(e -> onNewGameCallback.run());
        newGameBtn.setPrefWidth(220);
        Button homeBtn = new Button("Return Home");
        styleButton(homeBtn);
        homeBtn.setOnAction(e -> onHomeCallback.run());
        homeBtn.setPrefWidth(220);
        VBox controlsBox = new VBox(15, newGameBtn, homeBtn);
        controlsBox.setAlignment(Pos.CENTER);

        sidePanel.getChildren().addAll(titleBox, playerSection, diceBox, controlsBox);
        return sidePanel;
    }

    private void createPlayerStatusBoxes(List<Player> players) {
        playerStatusPane.getChildren().clear();
        // These maps are tied to specific Label instances. Clearing and re-adding is correct.
        playerPositionLabels.clear();
        playerMoneyLabels.clear();

        // playerTokenUIs stores the Circle objects for the side panel.
        // We want to preserve existing Circle objects for existing players if possible,
        // or update their colors. New players get new Circles.
        Map<Player, Circle> updatedPlayerTokenUIs = new HashMap<>();
        Color[] colors = { Color.RED, Color.BLUE, Color.rgb(0,128,0), Color.GOLD, Color.PURPLE, Color.DARKCYAN }; // Standard Green, Gold

        for (int i = 0; i < players.size(); i++) {
            Player p = players.get(i);
            HBox box = new HBox(15);
            box.setAlignment(Pos.CENTER_LEFT);
            box.setPadding(new Insets(10));
            box.setStyle("-fx-background-color: #F5DEB3; -fx-background-radius: 8px;");
            box.setUserData(p); // Store player object for easier identification

            Circle tok = playerTokenUIs.get(p); // Try to get existing token Circle
            if (tok == null) { // If player is new or their token wasn't there
                tok = new Circle(15, colors[i % colors.length]);
            } else { // Existing player, ensure color is up-to-date (e.g. if colors array changed)
                tok.setFill(colors[i % colors.length]);
            }
            tok.setStroke(Color.BLACK);
            tok.setStrokeWidth(1.5);
            updatedPlayerTokenUIs.put(p, tok);

            Label name = new Label(p.getName());
            name.setFont(Font.font("Kabel", FontWeight.BOLD, 18));
            name.setTextFill(Color.web("#654321"));

            Label posLbl = new Label("Tile: " + (p.getCurrentTile() != null ? String.valueOf(p.getCurrentTile().getId()) : "N/A"));
            posLbl.setFont(Font.font("Kabel", FontWeight.NORMAL, 15));
            posLbl.setTextFill(Color.web("#654321"));
            playerPositionLabels.put(p, posLbl);

            Label moneyLbl = new Label("Money: $" + p.getMoney());
            moneyLbl.setFont(Font.font("Kabel", FontWeight.NORMAL, 15));
            moneyLbl.setTextFill(Color.web("#654321"));
            playerMoneyLabels.put(p, moneyLbl);

            VBox info = new VBox(5, name, posLbl, moneyLbl);
            info.setAlignment(Pos.CENTER_LEFT);
            box.getChildren().addAll(tok, info);
            playerStatusPane.getChildren().add(box);
        }
        // Update the main playerTokenUIs map with the latest set of tokens
        playerTokenUIs.clear();
        playerTokenUIs.putAll(updatedPlayerTokenUIs);

        if (boardView != null) {
            boardView.updatePlayerTokenColors(); // Notify board view to update its token colors
            boardView.refresh(); // Full refresh to reposition tokens and update ownership
        }
    }

    private void styleButton(Button btn) {
        btn.setFont(Font.font("Kabel", FontWeight.BOLD, 16));
        btn.setStyle("-fx-background-color: #FF7043; -fx-text-fill: white; -fx-background-radius: 5px; -fx-cursor: hand; -fx-padding: 8px 15px;");
        btn.setOnMouseEntered(e -> btn.setStyle("-fx-background-color: #FF8A65; -fx-text-fill: white; -fx-background-radius: 5px; -fx-cursor: hand; -fx-padding: 8px 15px;"));
        btn.setOnMouseExited(e -> btn.setStyle("-fx-background-color: #FF7043; -fx-text-fill: white; -fx-background-radius: 5px; -fx-cursor: hand; -fx-padding: 8px 15px;"));
    }

    public void updatePlayerStatusDisplay() {
        if (gameModel == null) return;

        boolean playersChanged = playerTokenUIs.size() != gameModel.getPlayers().size() ||
                !playerTokenUIs.keySet().containsAll(gameModel.getPlayers()) ||
                !gameModel.getPlayers().containsAll(playerTokenUIs.keySet());

        if (playersChanged) {
            createPlayerStatusBoxes(gameModel.getPlayers());
        } else {
            gameModel.getPlayers().forEach(p -> {
                Label posLabel = playerPositionLabels.get(p);
                if (posLabel != null) {
                    posLabel.setText("Tile: " + (p.getCurrentTile() != null ? String.valueOf(p.getCurrentTile().getId()) : "N/A"));
                }
                Label moneyLabel = playerMoneyLabels.get(p);
                if (moneyLabel != null) {
                    moneyLabel.setText("Money: $" + p.getMoney());
                }
            });
        }
    }

    public void updateDiceLabel(String text) {
        if (diceLabel != null) {
            diceLabel.setText(text);
        }
    }

    public void highlightCurrentPlayer(Player playerToHighlight) {
        if (playerToHighlight == null) return;

        for (Node node : playerStatusPane.getChildren()) {
            if (node instanceof HBox) {
                Player p = (Player) node.getUserData(); // Retrieve player from HBox
                if (p != null) {
                    if (p.equals(playerToHighlight)) {
                        node.setStyle("-fx-background-color: #FFFACD; -fx-background-radius: 8px; -fx-border-color: #FF5722; -fx-border-width: 2.5px; -fx-border-radius: 8px; -fx-padding: 10px;");
                    } else {
                        node.setStyle("-fx-background-color: #F5DEB3; -fx-background-radius: 8px; -fx-padding: 10px; -fx-border-color: transparent;");
                    }
                }
            }
        }
    }

    public void setRollButtonEnabled(boolean enabled) {
        if (rollButton != null) {
            rollButton.setDisable(!enabled);
        }
    }

    public void displayGameOver(Player winner) {
        updateDiceLabel(winner != null ? "👑" : "🏁");
        setRollButtonEnabled(false);

        for (Node node : playerStatusPane.getChildren()) {
            if (node instanceof HBox) {
                Player p = (Player) node.getUserData();
                if (p != null && p.equals(winner)) {
                    node.setStyle("-fx-background-color: #FFD700; -fx-background-radius: 8px; -fx-border-color: #B8860B; -fx-border-width: 3px; -fx-padding: 10px;");
                    Circle tokenUI = playerTokenUIs.get(p);
                    if (tokenUI != null) {
                        tokenUI.setStroke(Color.GOLD);
                        tokenUI.setStrokeWidth(3);
                    }
                } else {
                    node.setStyle("-fx-background-color: #F5DEB3; -fx-background-radius: 8px; -fx-padding: 10px;");
                }
            }
        }
        updatePlayerStatusDisplay();
    }

    public boolean showPropertyPurchaseDialog(Player player, PropertyAction propertyAction) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Property Available");
        alert.setHeaderText("Buy Property: " + propertyAction.getName() + " (" + propertyAction.getColorGroup() + ")");
        alert.setContentText("Would you like to buy " + propertyAction.getName() +
                " for $" + propertyAction.getCost() + "?\n\n" +
                "Rent: $" + propertyAction.getRent() + "\n" +
                "Your current money: $" + player.getMoney());

        ButtonType buyButton = new ButtonType("Buy for $" + propertyAction.getCost());
        ButtonType passButton = ButtonType.CANCEL;
        alert.getButtonTypes().setAll(buyButton, passButton);

        Optional<ButtonType> result = alert.showAndWait();
        return result.isPresent() && result.get() == buyButton;
    }

    public void showAlert(String title, String header, String content, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.showAndWait();
    }

    /**
     * Inner class for rendering the Monopoly board.
     */
    public static class MonopolyBoardView extends Pane {
        public static final double SIZE = 700;
        private static final double TOKEN_RADIUS = 8;

        private final BoardGame game;
        private final Map<Integer, Point2D> tileCenterPositions = new HashMap<>();
        private final Map<Player, Circle> playerTokensOnBoard = new HashMap<>();
        private final Map<Player, Circle> playerTokenUIsFromSidePanel;

        private final Map<Integer, Rectangle> tileRects = new HashMap<>();
        private final Map<Integer, Group> tileTextGroups = new HashMap<>(); // To manage text updates/removal

        private final Color[] propertyColors = {
                Color.rgb(165, 42, 42),    // Brown
                Color.LIGHTSKYBLUE,        // Light Blue
                Color.rgb(255,105,180),    // Pink (HotPink)
                Color.ORANGE,              // Orange
                Color.RED,                 // Red
                Color.YELLOW,              // Yellow
                Color.LIMEGREEN,           // Green
                Color.ROYALBLUE            // Dark Blue
        };
        private int cellsPerSideGrid;
        private double cellSize;

        public MonopolyBoardView(BoardGame game, Map<Player, Circle> playerTokenUIsFromSidePanel) {
            this.game = game;
            this.playerTokenUIsFromSidePanel = playerTokenUIsFromSidePanel;
            setPrefSize(SIZE, SIZE);
            setMinSize(SIZE, SIZE);
            setMaxSize(SIZE, SIZE);
            setStyle("-fx-background-color: #C9E2C9;"); // Light green board background, e.g. Monopoly classic green

            initializeBoardLayout();
            initializeBoardVisuals();
            initializePlayerTokenVisuals(); // Create player tokens on board
            refresh();
        }

        private void initializeBoardLayout() {
            int tileCount = (game.getBoard() != null && game.getBoard().getTiles() != null) ? game.getBoard().getTiles().size() : 0;
            if (tileCount == 0) {
                this.cellsPerSideGrid = 1; // Default to a single cell if no tiles
                this.cellSize = SIZE;
                System.err.println("MonopolyBoardView: No tiles found in board model. Displaying single cell.");
                return;
            }

            if (tileCount > 0 && tileCount % 4 == 0) {
                this.cellsPerSideGrid = (tileCount / 4) + 1;
            } else {
                this.cellsPerSideGrid = (int) Math.ceil(Math.sqrt(tileCount));
                if (this.cellsPerSideGrid <= 0) this.cellsPerSideGrid = 1; // Ensure at least 1
                System.err.println("MonopolyBoardView: Non-standard tile count " + tileCount +
                        ". Using grid of " + this.cellsPerSideGrid + "x" + this.cellsPerSideGrid);
            }
            this.cellSize = SIZE / this.cellsPerSideGrid;
        }

        private void initializeBoardVisuals() {
            getChildren().clear();
            tileRects.clear();
            tileCenterPositions.clear();
            tileTextGroups.clear();

            int tileCount = (game.getBoard() != null && game.getBoard().getTiles() != null) ? game.getBoard().getTiles().size() : 0;
            if (tileCount == 0) return;

            double innerAreaSize = Math.max(0, (cellsPerSideGrid - 2) * cellSize);
            Rectangle centerRect = new Rectangle(cellSize, cellSize, innerAreaSize, innerAreaSize);
            centerRect.setFill(Color.rgb(210, 228, 210)); // Slightly different green for center
            centerRect.setStroke(Color.DARKSLATEGRAY);
            centerRect.setStrokeWidth(1);
            getChildren().add(centerRect);

            Label centerLabel = new Label("Mini Monopoly");
            centerLabel.setFont(Font.font("Kabel", FontWeight.BOLD, Math.max(10, innerAreaSize * 0.15)));
            centerLabel.setTextFill(Color.DARKRED);
            centerLabel.setPrefSize(innerAreaSize, innerAreaSize);
            centerLabel.setAlignment(Pos.CENTER);
            centerLabel.setLayoutX(cellSize);
            centerLabel.setLayoutY(cellSize);
            getChildren().add(centerLabel);

            for (int tileId = 0; tileId < tileCount; tileId++) {
                Tile tile = game.getBoard().getTile(tileId);
                if (tile == null) continue;

                Point2D centerPos = calculateTileCenterPosition(tileId);
                tileCenterPositions.put(tileId, centerPos);

                Rectangle tileRect = createTileRectangle(centerPos, tile, tileId);
                tileRect.setUserData(tile);
                getChildren().add(tileRect);
                tileRects.put(tileId, tileRect);

                Group textGroup = createTileTextGroup(tile, centerPos, tileId);
                getChildren().add(textGroup);
                tileTextGroups.put(tileId, textGroup);
            }
        }

        private Point2D calculateTileCenterPosition(int tileId) {
            double x, y;
            int tilesOnEdge = cellsPerSideGrid - 1;

            if (tilesOnEdge <= 0) return new Point2D(SIZE / 2.0, SIZE / 2.0);

            int side = tileId / tilesOnEdge;
            int posOnSide = tileId % tilesOnEdge;

            switch (side) {
                case 0: // Bottom row
                    x = SIZE - (cellSize / 2.0) - (posOnSide * cellSize);
                    y = SIZE - (cellSize / 2.0);
                    break;
                case 1: // Left column
                    x = cellSize / 2.0;
                    y = SIZE - (cellSize / 2.0) - (posOnSide * cellSize);
                    break;
                case 2: // Top row
                    x = (cellSize / 2.0) + (posOnSide * cellSize);
                    y = cellSize / 2.0;
                    break;
                case 3: // Right column
                    x = SIZE - (cellSize / 2.0);
                    y = (cellSize / 2.0) + (posOnSide * cellSize);
                    break;
                default:
                    System.err.println("MonopolyBoardView: Tile " + tileId + " on unexpected side " + side + ". Placing at default.");
                    x = cellSize / 2.0; y = cellSize / 2.0;
            }
            return new Point2D(x, y);
        }

        private Rectangle createTileRectangle(Point2D centerPos, Tile tile, int tileId) {
            Rectangle rect = new Rectangle(centerPos.getX() - cellSize / 2.0, centerPos.getY() - cellSize / 2.0, cellSize, cellSize);

            int tilesOnEdge = cellsPerSideGrid - 1;
            boolean isCorner = tilesOnEdge > 0 && (tileId % tilesOnEdge == 0);

            rect.setStroke(Color.DARKSLATEGRAY);
            rect.setStrokeWidth(1);

            if (isCorner) {
                rect.setFill(Color.LIGHTGRAY.deriveColor(0,1,1,0.8)); // Semi-transparent light gray
                if (tileId == 0) rect.setFill(Color.LIGHTGREEN.deriveColor(0,1,1,0.8));
            } else if (tile.getAction() instanceof PropertyAction pa) {
                rect.setFill(Color.IVORY.deriveColor(0,1,1,0.7)); // Base for property, mostly covered by color bar + text
                // Add color bar separately, rect acts as border/background
                Rectangle colorBar = createColorBar(pa, centerPos, tileId);
                getChildren().add(colorBar); // Add before text group potentially
                // Ownership stroke handled in refresh
            } else { // Chance, Community Chest, Tax
                rect.setFill(Color.WHITESMOKE.deriveColor(0,1,1,0.7));
            }
            return rect;
        }

        private Rectangle createColorBar(PropertyAction pa, Point2D tileCenterPos, int tileId) {
            int colorGroupIndex = getColorGroupIndex(pa.getColorGroup());
            Color propertyColor = propertyColors[colorGroupIndex % propertyColors.length];

            double barThicknessRatio = 0.22; // Bar takes ~22% of cell's dimension perpendicular to bar
            double barX = 0, barY = 0, barWidth = 0, barHeight = 0;

            int tilesOnEdge = cellsPerSideGrid - 1;
            int side = tilesOnEdge > 0 ? tileId / tilesOnEdge : 0;

            switch (side) {
                case 0: // Bottom row, bar at top of cell
                    barX = tileCenterPos.getX() - cellSize / 2.0;
                    barY = tileCenterPos.getY() - cellSize / 2.0;
                    barWidth = cellSize; barHeight = cellSize * barThicknessRatio;
                    break;
                case 1: // Left row, bar at right of cell
                    barX = tileCenterPos.getX() + cellSize / 2.0 - (cellSize * barThicknessRatio);
                    barY = tileCenterPos.getY() - cellSize / 2.0;
                    barWidth = cellSize * barThicknessRatio; barHeight = cellSize;
                    break;
                case 2: // Top row, bar at bottom of cell
                    barX = tileCenterPos.getX() - cellSize / 2.0;
                    barY = tileCenterPos.getY() + cellSize / 2.0 - (cellSize * barThicknessRatio);
                    barWidth = cellSize; barHeight = cellSize * barThicknessRatio;
                    break;
                case 3: // Right row, bar at left of cell
                    barX = tileCenterPos.getX() - cellSize / 2.0;
                    barY = tileCenterPos.getY() - cellSize / 2.0;
                    barWidth = cellSize * barThicknessRatio; barHeight = cellSize;
                    break;
            }

            Rectangle colorBarRect = new Rectangle(barX, barY, barWidth, barHeight);
            colorBarRect.setFill(propertyColor);
            colorBarRect.setStroke(Color.BLACK);
            colorBarRect.setStrokeWidth(0.5);
            return colorBarRect;
        }

        private Group createTileTextGroup(Tile tile, Point2D centerPos, int tileId) {
            String nameStr = "Tile " + tileId;
            String priceStr = "";

            int tilesOnEdge = cellsPerSideGrid - 1;
            boolean isCorner = tilesOnEdge > 0 && (tileId % tilesOnEdge == 0);

            if (isCorner) {
                if (tileId == 0) nameStr = "GO";
                else if (tileId == tilesOnEdge) nameStr = "JAIL";
                else if (tileId == 2 * tilesOnEdge) nameStr = "FREE PARKING";
                else if (tileId == 3 * tilesOnEdge) nameStr = "GO TO JAIL";
            } else if (tile.getAction() instanceof PropertyAction pa) {
                nameStr = pa.getName();
                priceStr = "$" + pa.getCost();
            }

            Text nameTextNode = new Text(nameStr);
            nameTextNode.setFont(Font.font("Arial", FontWeight.BOLD, Math.max(6, cellSize * 0.09)));
            nameTextNode.setTextAlignment(TextAlignment.CENTER);
            nameTextNode.setWrappingWidth(cellSize * 0.80); // Allow text to wrap

            Group textGroup = new Group(nameTextNode);
            double totalTextHeight = nameTextNode.getLayoutBounds().getHeight();

            if (!priceStr.isEmpty()) {
                Text priceTextNode = new Text(priceStr);
                priceTextNode.setFont(Font.font("Arial", FontWeight.NORMAL, Math.max(5, cellSize * 0.08)));
                priceTextNode.setTextAlignment(TextAlignment.CENTER);
                priceTextNode.setWrappingWidth(cellSize * 0.80);
                textGroup.getChildren().add(priceTextNode);
                totalTextHeight += priceTextNode.getLayoutBounds().getHeight() + 2; // Small spacing
                // Position price below name
                priceTextNode.setY(nameTextNode.getLayoutBounds().getHeight() + 2);
            }

            // Center the collective text block (name & price) within the available space
            // Available space is cell minus color bar areas.
            double textBlockYOffset = (cellSize - totalTextHeight) / 2.0; // Initial vertical center
            // If it's a property, color bar takes up space, adjust textBlockYOffset
            if (!isCorner && tile.getAction() instanceof PropertyAction) {
                textBlockYOffset = cellSize * 0.22 + (cellSize * (1-0.22) - totalTextHeight) / 2.0; // Place below color bar
            }
            nameTextNode.setY(textBlockYOffset);
            if (textGroup.getChildren().size() > 1 && textGroup.getChildren().get(1) instanceof Text priceText) {
                priceText.setY(textBlockYOffset + nameTextNode.getLayoutBounds().getHeight() + 2);
            }
            // Horizontal centering for each text node
            nameTextNode.setX((cellSize - nameTextNode.getLayoutBounds().getWidth()) / 2.0);
            if (textGroup.getChildren().size() > 1 && textGroup.getChildren().get(1) instanceof Text priceText) {
                priceText.setX((cellSize - priceText.getLayoutBounds().getWidth()) / 2.0);
            }


            // Position and rotate the entire textGroup
            textGroup.setLayoutX(centerPos.getX() - cellSize / 2.0); // Group origin to cell top-left
            textGroup.setLayoutY(centerPos.getY() - cellSize / 2.0);

            int side = tilesOnEdge > 0 ? tileId / tilesOnEdge : 0;
            switch (side) {
                case 1: // Left row
                    textGroup.setRotate(-90);
                    textGroup.setLayoutX(centerPos.getX() + cellSize / 2.0); // Adjust pivot due to rotation
                    textGroup.setLayoutY(centerPos.getY() - cellSize / 2.0);
                    break;
                case 2: // Top row
                    textGroup.setRotate(180);
                    textGroup.setLayoutX(centerPos.getX() + cellSize / 2.0);
                    textGroup.setLayoutY(centerPos.getY() + cellSize / 2.0);
                    break;
                case 3: // Right row
                    textGroup.setRotate(90);
                    textGroup.setLayoutX(centerPos.getX() - cellSize / 2.0);
                    textGroup.setLayoutY(centerPos.getY() + cellSize / 2.0);
                    break;
                // case 0 (Bottom row) needs no rotation or special layout adjustment for the group
            }
            return textGroup;
        }

        private int getColorGroupIndex(String colorGroup) {
            if (colorGroup == null) return 0;
            return switch (colorGroup.toLowerCase()) { // Use toLowerCase for robustness
                case "brown" -> 0;
                case "light blue" -> 1;
                case "pink", "magenta" -> 2; // Added magenta as common alias
                case "orange" -> 3;
                case "red" -> 4;
                case "yellow" -> 5;
                case "green" -> 6;
                case "dark blue", "blue" -> 7; // Added blue
                default -> {
                    System.err.println("MonopolyBoardView: Unknown color group '" + colorGroup + "'. Defaulting to Brown.");
                    yield 0;
                }
            };
        }

        private Color getPlayerColorFromSidePanel(Player player) {
            Circle tokenUI = playerTokenUIsFromSidePanel.get(player);
            if (tokenUI != null && tokenUI.getFill() instanceof Color) {
                return (Color) tokenUI.getFill();
            }
            int playerIndex = game.getPlayers().indexOf(player);
            Color[] defaultColors = { Color.RED, Color.BLUE, Color.rgb(0,128,0), Color.GOLD, Color.PURPLE, Color.DARKCYAN };
            return playerIndex >=0 ? defaultColors[playerIndex % defaultColors.length] : Color.BLACK;
        }

        public void updatePlayerTokenColors() {
            if (game == null || game.getPlayers() == null) return;
            for(Player player : game.getPlayers()){
                Circle tokenOnBoard = playerTokensOnBoard.get(player);
                if(tokenOnBoard != null){
                    tokenOnBoard.setFill(getPlayerColorFromSidePanel(player));
                } else { // Player token not on board, might be new player
                    Circle newToken = createTokenForPlayer(player);
                    playerTokensOnBoard.put(player, newToken);
                    getChildren().add(newToken);
                    // Position will be updated in refresh()
                }
            }
        }

        private Circle createTokenForPlayer(Player player) {
            Color playerColor = getPlayerColorFromSidePanel(player);
            Circle token = new Circle(TOKEN_RADIUS, playerColor);
            token.setStroke(Color.BLACK);
            token.setStrokeWidth(1.0); // Thinner stroke for smaller tokens
            token.setEffect(new javafx.scene.effect.DropShadow(2, Color.rgb(0,0,0,0.4)));
            token.setUserData(player); // To identify this node as a player token
            return token;
        }

        private void initializePlayerTokenVisuals() {
            // Remove old player token Circle nodes from the scene graph
            getChildren().removeIf(node -> node.getUserData() instanceof Player && node instanceof Circle);
            playerTokensOnBoard.clear(); // Clear the map

            if (game == null || game.getPlayers() == null) return;
            for (Player player : game.getPlayers()) {
                Circle token = createTokenForPlayer(player);
                playerTokensOnBoard.put(player, token);
                getChildren().add(token);
            }
        }

        public void refresh() {
            // Update tile ownership visuals
            for (int tileId = 0; tileId < game.getBoard().getTiles().size(); tileId++) {
                Tile tile = game.getBoard().getTile(tileId);
                Rectangle tileRect = tileRects.get(tileId);
                if (tileRect == null || tile == null) continue;

                if (tile.getAction() instanceof PropertyAction pa) {
                    if (pa.getOwner() != null) {
                        tileRect.setStroke(getPlayerColorFromSidePanel(pa.getOwner()));
                        tileRect.setStrokeWidth(2.5); // Thicker stroke for owned
                    } else {
                        tileRect.setStroke(Color.DARKSLATEGRAY);
                        tileRect.setStrokeWidth(1);
                    }
                }
            }

            // Update player token positions
            Map<Integer, Integer> playersOnTileCount = new HashMap<>();
            for (Player player : game.getPlayers()) {
                if (player.getCurrentTile() != null) {
                    int tileId = player.getCurrentTile().getId();
                    playersOnTileCount.put(tileId, playersOnTileCount.getOrDefault(tileId, 0) + 1);
                }
            }

            Map<Integer, Integer> currentPlayerIndexOnTile = new HashMap<>();
            for (Player player : game.getPlayers()) {
                if (player.getCurrentTile() == null) continue;
                int tileId = player.getCurrentTile().getId();
                Point2D tileCenterPos = tileCenterPositions.get(tileId);
                Circle token = playerTokensOnBoard.get(player);

                if (tileCenterPos != null && token != null) {
                    int totalOnThisTile = playersOnTileCount.get(tileId);
                    int playerIdxOnTile = currentPlayerIndexOnTile.getOrDefault(tileId, 0);
                    currentPlayerIndexOnTile.put(tileId, playerIdxOnTile + 1);

                    double offsetX = 0;
                    double offsetY = 0;
                    double offsetStep = TOKEN_RADIUS * 1.5;

                    if (totalOnThisTile > 1) {
                        // Simple grid layout (2xN)
                        int col = playerIdxOnTile % 2; // 0 or 1
                        int row = playerIdxOnTile / 2; // 0, 1, 2...
                        offsetX = (col - 0.5) * offsetStep;
                        offsetY = (row - ( (double)(totalOnThisTile-1)/2.0 ) /2.0 ) * offsetStep ; // Center rows vertically
                        if (totalOnThisTile > 6) { // Jitter for many players
                            offsetX = (Math.random() - 0.5) * cellSize * 0.15;
                            offsetY = (Math.random() - 0.5) * cellSize * 0.15;
                        }
                    }
                    token.setCenterX(tileCenterPos.getX() + offsetX);
                    token.setCenterY(tileCenterPos.getY() + offsetY);
                    token.toFront();
                } else if (token == null) {
                    System.err.println("MonopolyBoardView: Token for player " + player.getName() + " not found during refresh.");
                }
            }
        }
    }
}