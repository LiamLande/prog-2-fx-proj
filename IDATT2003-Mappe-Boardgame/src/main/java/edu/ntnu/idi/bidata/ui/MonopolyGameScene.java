package edu.ntnu.idi.bidata.ui;

import edu.ntnu.idi.bidata.controller.GameController; // Assuming GameController
import edu.ntnu.idi.bidata.model.BoardGame;
import edu.ntnu.idi.bidata.model.Player;
import edu.ntnu.idi.bidata.model.Tile;
import edu.ntnu.idi.bidata.model.actions.monopoly.PropertyAction;
import edu.ntnu.idi.bidata.model.actions.TileAction;
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
    private final Map<Player, Circle> playerTokenUIs = new HashMap<>();
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
        root.setStyle("-fx-background-color: #335c33;");

        this.boardView = new MonopolyBoardView(this.gameModel, this.playerTokenUIs); // Pass playerTokenUIs for color lookup
        StackPane boardContainer = createBoardContainer(this.boardView);
        VBox sidePanel = createSidePanel(this.gameModel.getPlayers());

        root.setCenter(boardContainer);
        root.setRight(sidePanel);
        root.setPadding(new Insets(20));

        scene = new Scene(root, 1100, 800);
        scene.getStylesheets().add(
                Objects.requireNonNull(getClass().getClassLoader().getResource("css/monopoly.css")).toExternalForm()
        );
    }

    /**
     * Called by the controller to initiate the game view setup.
     */
    public void initializeView() {
        updateDiceLabel("⚄");
        updatePlayerStatusDisplay();
        if (!gameModel.getPlayers().isEmpty()) {
            highlightCurrentPlayer(gameModel.getPlayers().getFirst());
        }
        setRollButtonEnabled(true);
        boardView.refresh();
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
        Region border = new Region();
        border.setStyle("-fx-background-color: #F0E68C; -fx-background-radius: 15px; -fx-border-color: #8B4513; -fx-border-width: 8px; -fx-border-radius: 15px;");

        StackPane container = new StackPane(border, boardGroup);
        StackPane.setMargin(boardGroup, new Insets(15));
        container.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);

        ChangeListener<Number> resizer = (obs, old, nw) -> {
            double availW = container.getWidth() - 40;
            double availH = container.getHeight() - 40;
            double scale = Math.min(availW, availH) / 600;
            boardGroup.setScaleX(scale);
            boardGroup.setScaleY(scale);
        };
        container.widthProperty().addListener(resizer);
        container.heightProperty().addListener(resizer);
        return container;
    }

    private VBox createSidePanel(List<Player> players) {
        VBox sidePanel = new VBox(20);
        sidePanel.setAlignment(Pos.TOP_CENTER);
        sidePanel.setPadding(new Insets(0, 0, 0, 20));
        sidePanel.setPrefWidth(300);

        VBox titleBox = new VBox(5);
        titleBox.setAlignment(Pos.CENTER);
        titleBox.setPadding(new Insets(20));
        titleBox.setStyle("-fx-background-color: #D2B48C; -fx-background-radius: 10px;");
        Label titleLabel = new Label("Mini Monopoly");
        titleLabel.getStyleClass().add("scene-title"); // From CSS
        titleLabel.setFont(Font.font("Kabel", FontWeight.BOLD, 32));
        Label subtitleLabel = new Label("The Market of Fortune");
        subtitleLabel.setFont(Font.font("Kabel", FontWeight.NORMAL, 16));
        subtitleLabel.setTextFill(Color.web("#8B4513"));
        titleBox.getChildren().addAll(titleLabel, subtitleLabel);

        playerStatusPane = new VBox(10);
        playerStatusPane.setAlignment(Pos.CENTER);
        createPlayerStatusBoxes(players);
        VBox playerSection = new VBox(15, new Label("Players"), playerStatusPane);
        playerSection.setAlignment(Pos.TOP_CENTER);
        playerSection.setPadding(new Insets(20));
        playerSection.setStyle("-fx-background-color: #D2B48C; -fx-background-radius: 10px;");

        diceLabel = new Label("⚄");
        diceLabel.setFont(Font.font("Kabel", FontWeight.BOLD, 48));
        diceLabel.setTextFill(Color.web("#8B4513"));
        rollButton = new Button("Roll Dice");
        styleButton(rollButton);
        rollButton.setOnAction(e -> controller.handleRollDiceRequest()); // Controller handles this
        rollButton.setPrefWidth(150);
        VBox diceBox = new VBox(10, diceLabel, rollButton);
        diceBox.setAlignment(Pos.CENTER);
        diceBox.setPadding(new Insets(20));
        diceBox.setStyle("-fx-background-color: #D2B48C; -fx-background-radius: 10px;");

        Button newGameBtn = new Button("New Game");
        styleButton(newGameBtn);
        newGameBtn.setOnAction(e -> onNewGameCallback.run());
        newGameBtn.setPrefWidth(200);
        Button homeBtn = new Button("Return Home");
        styleButton(homeBtn);
        homeBtn.setOnAction(e -> onHomeCallback.run());
        homeBtn.setPrefWidth(200);
        VBox controlsBox = new VBox(15, newGameBtn, homeBtn);
        controlsBox.setAlignment(Pos.CENTER);

        sidePanel.getChildren().addAll(titleBox, playerSection, diceBox, controlsBox);
        return sidePanel;
    }

    private void createPlayerStatusBoxes(List<Player> players) {
        playerStatusPane.getChildren().clear();
        playerPositionLabels.clear();
        playerMoneyLabels.clear();
        playerTokenUIs.clear();
        Color[] colors = { Color.RED, Color.BLUE, Color.GREEN, Color.ORANGE, Color.PURPLE, Color.BLACK };
        for (int i = 0; i < players.size(); i++) {
            Player p = players.get(i);
            HBox box = new HBox(15);
            box.setAlignment(Pos.CENTER_LEFT);
            box.setPadding(new Insets(10));
            box.setStyle("-fx-background-color: #D2B48C; -fx-background-radius: 10px;");
            Circle tok = new Circle(15, colors[i % colors.length]);
            tok.setStroke(Color.BLACK);
            playerTokenUIs.put(p, tok);
            Label name = new Label(p.getName());
            name.setFont(Font.font("Kabel", FontWeight.BOLD, 16));
            name.setTextFill(Color.web("#8B4513"));

            Label posLbl = new Label("Tile: " + (p.getCurrentTile() != null ? String.valueOf(p.getCurrentTile().getId()) : "N/A"));
            posLbl.setFont(Font.font("Kabel", FontWeight.NORMAL, 14));
            posLbl.setTextFill(Color.web("#8B4513"));
            playerPositionLabels.put(p, posLbl);

            Label moneyLbl = new Label("Money: $" + p.getMoney());
            moneyLbl.setFont(Font.font("Kabel", FontWeight.NORMAL, 14));
            moneyLbl.setTextFill(Color.web("#8B4513"));
            playerMoneyLabels.put(p, moneyLbl);

            VBox info = new VBox(5, name, posLbl, moneyLbl);
            info.setAlignment(Pos.CENTER_LEFT);
            box.getChildren().addAll(tok, info);
            playerStatusPane.getChildren().add(box);
        }
    }

    private void styleButton(Button btn) {
        btn.setFont(Font.font("Kabel", FontWeight.BOLD, 16));
        btn.setStyle("-fx-background-color: #FF5722; -fx-text-fill: white; -fx-background-radius: 5px; -fx-cursor: hand;");
        btn.setOnMouseEntered(e -> btn.setStyle("-fx-background-color: #E64A19; -fx-text-fill: white; -fx-background-radius: 5px; -fx-cursor: hand;"));
        btn.setOnMouseExited(e -> btn.setStyle("-fx-background-color: #FF5722; -fx-text-fill: white; -fx-background-radius: 5px; -fx-cursor: hand;"));
    }

    // --- Public methods to be called by the Controller ---

    public void updatePlayerStatusDisplay() {
        if (gameModel == null || gameModel.getPlayers().isEmpty()) return;

        if (playerStatusPane.getChildren().size() != gameModel.getPlayers().size() ||
                !playerTokenUIs.keySet().containsAll(gameModel.getPlayers())) {
            createPlayerStatusBoxes(gameModel.getPlayers());
        }

        playerPositionLabels.forEach((p, lbl) -> {
            if (p.getCurrentTile() != null) {
                lbl.setText("Tile: " + String.valueOf(p.getCurrentTile().getId()));
            } else {
                lbl.setText("Tile: N/A");
            }
        });
        playerMoneyLabels.forEach((p, lbl) -> lbl.setText("Money: $" + p.getMoney()));
    }

    public void updateDiceLabel(String text) {
        if (diceLabel != null) {
            diceLabel.setText(text);
        }
    }

    public void highlightCurrentPlayer(Player playerToHighlight) {
        if (gameModel == null || gameModel.getPlayers().isEmpty() || playerToHighlight == null) return;
        List<Player> players = gameModel.getPlayers();
        for (int i = 0; i < players.size(); i++) {
            Node node = playerStatusPane.getChildren().get(i);
            if (node instanceof HBox) {
                ((HBox) node).setStyle("-fx-background-color: #D2B48C; -fx-background-radius: 10px; -fx-padding: 10px;");
            }
        }
        if (players.contains(playerToHighlight)) {
            int idx = players.indexOf(playerToHighlight);
            if (idx >= 0 && idx < playerStatusPane.getChildren().size()) {
                Node node = playerStatusPane.getChildren().get(idx);
                if (node instanceof HBox) {
                    ((HBox) node).setStyle("-fx-background-color: #FFFACD; -fx-background-radius: 10px; -fx-border-color: #FF5722; -fx-border-width: 2px; -fx-border-radius: 10px; -fx-padding: 10px;");
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
        updateDiceLabel("💰"); // Winner symbol for Monopoly
        setRollButtonEnabled(false);

        for (Player p : gameModel.getPlayers()) {
            int playerIndex = gameModel.getPlayers().indexOf(p);
            if (playerIndex >= 0 && playerIndex < playerStatusPane.getChildren().size()) {
                Node node = playerStatusPane.getChildren().get(playerIndex);
                if (node instanceof HBox) {
                    ((HBox) node).setStyle("-fx-background-color: #D2B48C; -fx-background-radius: 10px; -fx-padding: 10px;");
                }
            }
        }

        if (winner != null && gameModel.getPlayers().contains(winner)) {
            int winnerIndex = gameModel.getPlayers().indexOf(winner);
            if (winnerIndex >= 0 && winnerIndex < playerStatusPane.getChildren().size()) {
                Node node = playerStatusPane.getChildren().get(winnerIndex);
                if (node instanceof HBox) {
                    ((HBox) node).setStyle("-fx-background-color: #FFD700; -fx-background-radius: 10px; -fx-padding: 10px;");
                }
            }
            Circle tokenUI = playerTokenUIs.get(winner);
            if (tokenUI != null) {
                tokenUI.setStroke(Color.GOLD);
                tokenUI.setStrokeWidth(3);
            }
        }
        updatePlayerStatusDisplay(); // Show final money and positions
    }

    /**
     * Shows a dialog offering the player the option to purchase a property.
     * Returns true if the player confirms purchase, false otherwise.
     */
    public boolean showPropertyPurchaseDialog(Player player, PropertyAction propertyAction) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Property Available");
        alert.setHeaderText("Buy Property: " + propertyAction.getName());
        alert.setContentText("Would you like to buy " + propertyAction.getName() +
                " for $" + propertyAction.getCost() + "?\n\n" +
                "Rent: $" + propertyAction.getRent() + "\n" +
                "Your money: $" + player.getMoney());

        Optional<ButtonType> result = alert.showAndWait();
        return result.isPresent() && result.get() == ButtonType.OK;
    }

    /**
     * Shows a generic alert dialog.
     */
    public void showAlert(String title, String header, String content, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.showAndWait();
    }

    /**
     * Inner class for rendering the Monopoly board.
     * Assumes its refresh() method will be called by MonopolyGameScene after controller updates.
     */
    public static class MonopolyBoardView extends Pane {
        private static final double SIZE = 600;
        private final BoardGame game;
        private final Map<Integer, Point2D> tilePositions = new HashMap<>();
        private final Map<Player, Circle> playerTokensOnBoard = new HashMap<>(); // Tokens on this board
        private final Map<Player, Circle> playerTokenUIsFromSidePanel; // Reference for colors

        private final Color[] propertyColors = {
                Color.BROWN, Color.LIGHTBLUE, Color.PINK, Color.ORANGE,
                Color.RED, Color.YELLOW, Color.GREEN, Color.BLUE
        };

        public MonopolyBoardView(BoardGame game, Map<Player, Circle> playerTokenUIsFromSidePanel) {
            this.game = game;
            this.playerTokenUIsFromSidePanel = playerTokenUIsFromSidePanel;
            setPrefSize(SIZE, SIZE);
            initializeBoardVisuals();
            initializePlayerTokenVisuals();
            refresh();
        }

        private void initializeBoardVisuals() {
            int tileCount = game.getBoard().getTiles().size();
            if (tileCount == 0) return;

            int tilesPerSide = (tileCount / 4) + (tileCount % 4 == 0 ? 0 : 1); // Simplified, assumes roughly square
            if (tilesPerSide <= 1) tilesPerSide = (int)Math.ceil(Math.sqrt(tileCount)); // fallback for small boards
            if (tilesPerSide == 0) tilesPerSide = 1;
            double cellSize = SIZE / tilesPerSide;

            Rectangle center = new Rectangle(cellSize, cellSize, SIZE - 2 * cellSize, SIZE - 2 * cellSize);
            center.setFill(Color.LIGHTYELLOW);
            center.setStroke(Color.BLACK);
            getChildren().add(center);
            Label centerLabel = new Label("MONOPOLY");
            centerLabel.setFont(Font.font("Kabel", FontWeight.BOLD, 36));
            centerLabel.setTextFill(Color.RED);
            centerLabel.setLayoutX(SIZE / 2 - centerLabel.prefWidth(-1) / 2); // Centering text
            centerLabel.setLayoutY(SIZE / 2 - centerLabel.prefHeight(-1) / 2);
            getChildren().add(centerLabel);

            for (int i = 0; i < tileCount; i++) {
                int tileId = i;
                Tile tile = game.getBoard().getTile(tileId);
                if (tile == null) continue;

                Point2D position = calculateTilePosition(tileId, tilesPerSide, cellSize, tileCount);
                tilePositions.put(tileId, position);

                // Determine color group index for properties
                int colorGroupIndex = 0;
                if (tile.getAction() instanceof PropertyAction) {
                    // This is a simplification. Real Monopoly has fixed color groups.
                    // Here, we'll just cycle through colors for different properties.
                    colorGroupIndex = tileId % propertyColors.length;
                }


                Rectangle tileRect = createTileVisual(position.getX(), position.getY(), cellSize, colorGroupIndex, tile, tileId, tilesPerSide);
                getChildren().add(tileRect);

                Text tileIdText = new Text(String.valueOf(tileId));
                tileIdText.setX(position.getX() - cellSize*0.4); // Adjust for visibility
                tileIdText.setY(position.getY() - cellSize*0.35);
                tileIdText.setFont(Font.font("Arial", FontWeight.BOLD, 10));
                // getChildren().add(tileIdText); // Optional: display tile ID

                if (tile.getAction() instanceof PropertyAction propertyAction) {
                    String propertyName = propertyAction.getName();
                    Text nameText = new Text(propertyName);
                    nameText.setFont(Font.font("Arial", 8));
                    nameText.setTextAlignment(TextAlignment.CENTER);
                    nameText.setWrappingWidth(cellSize - 4);

                    int side = tileId / (tilesPerSide -1); // Simple side calculation
                    if (tilesPerSide <=1) side = 0; // Handle single row/col case


                    // Position text based on tile orientation
                    // This needs to be more robust if tile orientation varies
                    if (side == 0) { // Bottom
                        nameText.setX(position.getX() - (cellSize/2) + 2);
                        nameText.setY(position.getY() + cellSize*0.1);
                    } else if (side == 1) { // Left
                        nameText.setX(position.getX() - cellSize*0.4);
                        nameText.setY(position.getY() - (cellSize/2) + 10);
                        nameText.setRotate(-90);
                    } else if (side == 2) { // Top
                        nameText.setX(position.getX() - (cellSize/2) + 2);
                        nameText.setY(position.getY() - cellSize*0.3);
                        // nameText.setRotate(180); // If text needs to be upside down
                    } else { // Right
                        nameText.setX(position.getX() + cellSize*0.1);
                        nameText.setY(position.getY() - (cellSize/2) + 10);
                        nameText.setRotate(90);
                    }
                    getChildren().add(nameText);


                    Text priceText = new Text("$" + propertyAction.getCost());
                    priceText.setFont(Font.font("Arial", FontWeight.BOLD, 8));
                    priceText.setFill(Color.BLACK);
                    if (side == 0) { // Bottom
                        priceText.setX(position.getX() - priceText.getLayoutBounds().getWidth()/2 );
                        priceText.setY(position.getY() + cellSize*0.35);
                    } else if (side == 1) { // Left
                        priceText.setX(position.getX() - cellSize*0.3);
                        priceText.setY(position.getY() + priceText.getLayoutBounds().getWidth()/2);
                        priceText.setRotate(-90);
                    } else if (side == 2) { // Top
                        priceText.setX(position.getX()- priceText.getLayoutBounds().getWidth()/2);
                        priceText.setY(position.getY() - cellSize*0.1);
                    } else { // Right
                        priceText.setX(position.getX() + cellSize*0.2);
                        priceText.setY(position.getY() - priceText.getLayoutBounds().getWidth()/2);
                        priceText.setRotate(90);
                    }
                    getChildren().add(priceText);
                }
            }
        }

        private Point2D calculateTilePosition(int tileId, int tilesPerSide, double cellSize, int totalTiles) {
            double x, y;
            // Standard Monopoly layout: 0-9 bottom, 10-19 left, 20-29 top, 30-39 right (for 40 tiles)
            // This is a simplified calculation for a generic square board
            int effectiveTilesPerSide = tilesPerSide -1;
            if (effectiveTilesPerSide == 0 && totalTiles > 1) effectiveTilesPerSide = 1; // Avoid division by zero for 1xN boards
            else if (effectiveTilesPerSide == 0 && totalTiles == 1) { // Single tile case
                return new Point2D(SIZE/2, SIZE/2);
            }


            int side = tileId / effectiveTilesPerSide;
            int posOnSide = tileId % effectiveTilesPerSide;


            if (side == 0) { // Bottom row (right to left)
                x = SIZE - (cellSize / 2) - posOnSide * cellSize;
                y = SIZE - (cellSize / 2);
            } else if (side == 1) { // Left column (bottom to top)
                x = cellSize / 2;
                y = SIZE - (cellSize / 2) - posOnSide * cellSize;
            } else if (side == 2) { // Top row (left to right)
                x = (cellSize / 2) + posOnSide * cellSize;
                y = cellSize / 2;
            } else if (side == 3 && totalTiles > 3 * effectiveTilesPerSide) { // Right column (top to bottom)
                x = SIZE - (cellSize / 2);
                y = (cellSize / 2) + posOnSide * cellSize;
            } else { // Fallback for boards not fitting 4 sides perfectly or few tiles
                // Distribute remaining tiles, e.g. on the last started side or center.
                // This part needs robust logic for non-standard board sizes.
                // For simplicity, let's assume it fits the pattern or put it in the center.
                int col = tileId % tilesPerSide;
                int row = tileId / tilesPerSide;
                x = col * cellSize + cellSize / 2;
                y = row * cellSize + cellSize / 2;
                // If it's the last side and not full, adjust:
                if (side == 3) { // Typically right column
                    x = SIZE - (cellSize / 2);
                    y = (cellSize / 2) + posOnSide * cellSize;
                } else if (totalTiles <= tilesPerSide) { // Single row
                    x = (cellSize/2) + tileId * cellSize;
                    y = SIZE - (cellSize/2);
                } else { // Fallback
                    x = SIZE / 2; y = SIZE / 2;
                }

            }
            return new Point2D(x, y);
        }

        private Rectangle createTileVisual(double x, double y, double size, int colorGroupIndex, Tile tile, int tileId, int tilesPerSide) {
            Rectangle tileRect = new Rectangle(x - size / 2, y - size / 2, size, size);
            // Determine if it's a corner or special tile by ID
            boolean isCorner = tileId == 0 || tileId == (tilesPerSide -1) || tileId == 2*(tilesPerSide-1) || tileId == 3*(tilesPerSide-1);
            if (game.getBoard().getTiles().size() <= 4) isCorner = false; // No corners for very small boards


            if (isCorner) { // Corner tiles
                tileRect.setFill(Color.LIGHTGRAY); // Generic corner color
                // Could add specific text like "GO", "Jail", "Free Parking", "Go to Jail"
                // For "GO"
                if (tileId == 0) {
                    tileRect.setFill(Color.rgb(200,225,170)); // Monopoly GO Green
                }
            } else if (tile.getAction() instanceof PropertyAction propertyAction) {
                tileRect.setFill(propertyColors[colorGroupIndex % propertyColors.length]);
                if (propertyAction.getOwner() != null) {
                    tileRect.setStroke(getPlayerColorFromSidePanel(propertyAction.getOwner()));
                    tileRect.setStrokeWidth(3); // Indicate ownership
                } else {
                    tileRect.setStroke(Color.BLACK);
                    tileRect.setStrokeWidth(1);
                }
            } else { // Other non-property tiles (Chance, Community Chest, Tax)
                tileRect.setFill(Color.LIGHTGRAY); // Default for other special tiles
                tileRect.setStroke(Color.BLACK);
                tileRect.setStrokeWidth(1);
            }
            if (! (tile.getAction() instanceof PropertyAction propertyAction && propertyAction.getOwner() != null)) {
                tileRect.setStroke(Color.BLACK);
                tileRect.setStrokeWidth(1);
            }

            return tileRect;
        }

        private Color getPlayerColorFromSidePanel(Player player) {
            Circle tokenUI = playerTokenUIsFromSidePanel.get(player);
            if (tokenUI != null && tokenUI.getFill() instanceof Color) {
                return (Color) tokenUI.getFill();
            }
            return Color.BLACK; // Default
        }

        private void initializePlayerTokenVisuals() {
            // Tokens are created using colors from side panel for consistency
            // This map playerTokensOnBoard stores the visual Circle objects for this board
            for (Player player : game.getPlayers()) {
                Color playerColor = getPlayerColorFromSidePanel(player);
                Circle token = new Circle(10, playerColor);
                token.setStroke(Color.BLACK);
                token.setStrokeWidth(2);
                playerTokensOnBoard.put(player, token);
                getChildren().add(token);
            }
        }

        public void refresh() {
            // Update tile visuals (especially property ownership borders)
            // This requires re-iterating tiles or finding the specific Rectangle nodes
            // For simplicity, we'll clear and redraw property-related parts or rely on finding them
            // This part can be optimized by storing references to the tile Rectangles.
            for (javafx.scene.Node node : getChildren()) {
                if (node instanceof Rectangle && node.getUserData() instanceof Tile) {
                    Tile tile = (Tile) node.getUserData(); // Assume userData was set
                    Rectangle tileRect = (Rectangle) node;
                    if (tile.getAction() instanceof PropertyAction pa) {
                        if (pa.getOwner() != null) {
                            tileRect.setStroke(getPlayerColorFromSidePanel(pa.getOwner()));
                            tileRect.setStrokeWidth(3);
                        } else {
                            tileRect.setStroke(Color.BLACK);
                            tileRect.setStrokeWidth(1);
                        }
                    }
                }
            }


            // Update token positions
            for (Player player : game.getPlayers()) {
                if (player.getCurrentTile() == null) continue;
                int tileId = player.getCurrentTile().getId();
                Point2D position = tilePositions.get(tileId);
                Circle token = playerTokensOnBoard.get(player);

                if (position != null && token != null) {
                    int playerIndex = game.getPlayers().indexOf(player);
                    // Simple offset to avoid tokens perfectly overlapping
                    double offsetX = (playerIndex % 2 == 0 ? -5 : 5) * (playerIndex/2 + 1);
                    double offsetY = (playerIndex % 3 == 0 ? -5 : (playerIndex % 3 == 1 ? 0 : 5)) ;

                    token.setCenterX(position.getX() + offsetX);
                    token.setCenterY(position.getY() + offsetY);
                }
            }
        }
    }
}