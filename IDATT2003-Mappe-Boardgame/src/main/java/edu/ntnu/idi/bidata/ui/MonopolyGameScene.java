package edu.ntnu.idi.bidata.ui;

import edu.ntnu.idi.bidata.controller.GameController;
import edu.ntnu.idi.bidata.model.BoardGame;
import edu.ntnu.idi.bidata.model.Player;
import edu.ntnu.idi.bidata.model.Tile;
import edu.ntnu.idi.bidata.model.actions.monopoly.PropertyAction;
import edu.ntnu.idi.bidata.model.actions.TileAction;
import edu.ntnu.idi.bidata.service.MonopolyService;
import edu.ntnu.idi.bidata.service.ServiceLocator;
import javafx.beans.value.ChangeListener;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.Point2D;
import javafx.scene.Group;
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
import javafx.stage.Stage;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * MonopolyGameScene displays a simple Monopoly board game interface.
 * This follows a similar pattern to GameScene but with Monopoly-specific elements.
 */
public class MonopolyGameScene implements SceneManager.ControlledScene {
    private final GameController controller;
    private final MonopolyBoardView boardView;
    private VBox playerStatusPane;
    private final Map<Player, Label> playerPositionLabels = new HashMap<>();
    private final Map<Player, Circle> playerTokens = new HashMap<>();
    private final Scene scene;
    private Player currentPlayer;
    private Label diceLabel;
    private Button rollButton;
    private int lastRoll;

    public MonopolyGameScene(Stage primaryStage, BoardGame game, Runnable newGameAction, Runnable homeAction) {
        // Initialize controller and first player
        controller = new GameController(game);
        currentPlayer = game.getPlayers().getFirst();

        // Root layout with Monopoly-style background
        BorderPane root = new BorderPane();
        root.setStyle("-fx-background-color: #335c33;"); // Monopoly green

        // Custom board view for Monopoly
        MonopolyBoardView bv = new MonopolyBoardView(game);
        this.boardView = bv;
        StackPane boardContainer = createBoardContainer(bv);

        // Side panel for title, status, controls
        VBox sidePanel = createSidePanel(game.getPlayers(), newGameAction, homeAction);

        // Assemble layout
        root.setCenter(boardContainer);
        root.setRight(sidePanel);
        root.setPadding(new Insets(20));

        // Create scene
        scene = new Scene(root, 1100, 800);
        // Add Monopoly-specific CSS
        scene.getStylesheets().add(
            Objects.requireNonNull(getClass().getClassLoader().getResource("css/monopoly.css")).toExternalForm()
        );

        // Listen to game events
        controller.addListener(new GameController.GameListener() {
            @Override
            public void onGameStart(List<Player> players) {
                currentPlayer = players.getFirst();
                updateDiceLabel("⚄");
                updatePlayerPositions();
                highlightCurrentPlayer();
                rollButton.setDisable(false);
            }

            @Override
            public void onRoundPlayed(List<Integer> rolls, List<Player> players) {
                if (!rolls.isEmpty()) {
                    lastRoll = rolls.getFirst();
                    updateDiceLabel(String.valueOf(lastRoll));
                }
                boardView.refresh();
                updatePlayerPositions();
                
                // Check if current player landed on a property that can be purchased
                checkPropertyPurchase(currentPlayer);
                
                if (controller.getGame().isFinished()) return;
                int idx = players.indexOf(currentPlayer);
                currentPlayer = players.get((idx + 1) % players.size());
                highlightCurrentPlayer();
            }

            @Override
            public void onGameOver(Player winner) {
                updateDiceLabel("💰");
                updatePlayerPositions();
                rollButton.setDisable(true);
                
                // Reset styles
                for (Player p : controller.getGame().getPlayers()) {
                    HBox box = (HBox) playerStatusPane.getChildren()
                            .get(controller.getGame().getPlayers().indexOf(p));
                    box.setStyle("-fx-background-color: #D2B48C; -fx-background-radius: 10px; -fx-padding: 10px;");
                }
                
                // Highlight winner
                HBox winBox = (HBox) playerStatusPane.getChildren()
                        .get(controller.getGame().getPlayers().indexOf(winner));
                winBox.setStyle("-fx-background-color: #FFD700; -fx-background-radius: 10px; -fx-padding: 10px;");
                Circle token = playerTokens.get(winner);
                if (token != null) {
                    token.setStroke(Color.GOLD);
                    token.setStrokeWidth(3);
                }
            }
        });
    }

    /** Returns the Scene instance for sizing and styling. */
    public Scene getScene() { return scene; }
    
    /** Starts the game by initializing model and firing first event. */
    public void start() { controller.startGame(); }

    @Override public void onShow() { }
    @Override public void onHide() { }

    /** Wraps the MonopolyBoardView in a responsive StackPane with border. */
    private StackPane createBoardContainer(MonopolyBoardView board) {
        Group boardGroup = new Group(board);
        
        // Create a border with Monopoly-style design
        Region border = new Region();
        border.setStyle("-fx-background-color: #F0E68C; -fx-background-radius: 15px; -fx-border-color: #8B4513; -fx-border-width: 8px; -fx-border-radius: 15px;");

        StackPane container = new StackPane(border, boardGroup);
        StackPane.setMargin(boardGroup, new Insets(15));
        container.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);

        // Scale board based on available space
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

    /** Builds the side panel with title, player statuses, dice and controls. */
    private VBox createSidePanel(List<Player> players, 
                                Runnable onNewGame, 
                                Runnable onHome) {
        VBox sidePanel = new VBox(20);
        sidePanel.setAlignment(Pos.TOP_CENTER);
        sidePanel.setPadding(new Insets(0, 0, 0, 20));
        sidePanel.setPrefWidth(300);

        // Title section
        VBox titleBox = new VBox(5);
        titleBox.setAlignment(Pos.CENTER);
        titleBox.setPadding(new Insets(20));
        titleBox.setStyle("-fx-background-color: #D2B48C; -fx-background-radius: 10px;");
        Label titleLabel = new Label("Mini Monopoly");
        titleLabel.getStyleClass().add("scene-title");
        titleLabel.setFont(Font.font("Kabel", FontWeight.BOLD, 32));
        
        Label subtitleLabel = new Label("The Market of Fortune");
        subtitleLabel.setFont(Font.font("Kabel", FontWeight.NORMAL, 16));
        subtitleLabel.setTextFill(Color.web("#8B4513"));
        
        titleBox.getChildren().addAll(titleLabel, subtitleLabel);

        // Player status
        playerStatusPane = new VBox(10);
        playerStatusPane.setAlignment(Pos.CENTER);
        createPlayerStatusBoxes(players);
        
        VBox playerSection = new VBox(15, new Label("Players"), playerStatusPane);
        playerSection.setAlignment(Pos.TOP_CENTER);
        playerSection.setPadding(new Insets(20));
        playerSection.setStyle("-fx-background-color: #D2B48C; -fx-background-radius: 10px;");

        // Dice controls with Monopoly styling
        diceLabel = new Label("⚄");
        diceLabel.setFont(Font.font("Kabel", FontWeight.BOLD, 48));
        diceLabel.setTextFill(Color.web("#8B4513"));
        
        rollButton = new Button("Roll Dice");
        styleButton(rollButton);
        rollButton.setOnAction(e -> controller.playTurn(currentPlayer));
        rollButton.setPrefWidth(150);
        
        VBox diceBox = new VBox(10, diceLabel, rollButton);
        diceBox.setAlignment(Pos.CENTER);
        diceBox.setPadding(new Insets(20));
        diceBox.setStyle("-fx-background-color: #D2B48C; -fx-background-radius: 10px;");

        // New Game / Home buttons
        Button newGameBtn = new Button("New Game");
        styleButton(newGameBtn);
        newGameBtn.setOnAction(e -> onNewGame.run());
        newGameBtn.setPrefWidth(200);
        
        Button homeBtn = new Button("Return Home");
        styleButton(homeBtn);
        homeBtn.setOnAction(e -> onHome.run());
        homeBtn.setPrefWidth(200);
        
        VBox controlsBox = new VBox(15, newGameBtn, homeBtn);
        controlsBox.setAlignment(Pos.CENTER);

        sidePanel.getChildren().addAll(titleBox, playerSection, diceBox, controlsBox);
        return sidePanel;
    }

    /** Initializes player status boxes with tokens and position labels. */
    private void createPlayerStatusBoxes(List<Player> players) {
        playerStatusPane.getChildren().clear();
        playerPositionLabels.clear();
        playerTokens.clear();
        
        // Monopoly token colors
        Color[] colors = { 
            Color.RED, Color.BLUE, 
            Color.GREEN, Color.ORANGE, 
            Color.PURPLE, Color.BLACK 
        };
        
        for (int i = 0; i < players.size(); i++) {
            Player p = players.get(i);
            HBox box = new HBox(15);
            box.setAlignment(Pos.CENTER_LEFT);
            box.setPadding(new Insets(10));
            box.setStyle("-fx-background-color: #D2B48C; -fx-background-radius: 10px;");
            
            Circle tok = new Circle(15, colors[i % colors.length]);
            tok.setStroke(Color.BLACK);
            playerTokens.put(p, tok);
            
            Label name = new Label(p.getName());
            name.setFont(Font.font("Kabel", FontWeight.BOLD, 16));
            name.setTextFill(Color.web("#8B4513"));
            
            Label pos = new Label(String.valueOf(p.getCurrentTile().getId()));
            pos.setFont(Font.font("Kabel", FontWeight.NORMAL, 24));
            pos.setTextFill(Color.web("#8B4513"));
            playerPositionLabels.put(p, pos);
            
            VBox info = new VBox(5, name, pos);
            info.setAlignment(Pos.CENTER_LEFT);
            box.getChildren().addAll(tok, info);
            playerStatusPane.getChildren().add(box);
        }
    }

    /** Applies Monopoly-themed styling and hover effects to buttons. */
    private void styleButton(Button btn) {
        btn.setFont(Font.font("Kabel", FontWeight.BOLD, 16));
        btn.setStyle("-fx-background-color: #FF5722; -fx-text-fill: white; -fx-background-radius: 5px; -fx-cursor: hand;");
        btn.setOnMouseEntered(e -> btn.setStyle("-fx-background-color: #E64A19; -fx-text-fill: white; -fx-background-radius: 5px; -fx-cursor: hand;"));
        btn.setOnMouseExited(e -> btn.setStyle("-fx-background-color: #FF5722; -fx-text-fill: white; -fx-background-radius: 5px; -fx-cursor: hand;"));
    }

    /** Refreshes all player position labels from the model. */
    private void updatePlayerPositions() {
        playerPositionLabels.forEach((p, lbl) -> lbl.setText(String.valueOf(p.getCurrentTile().getId())));
    }

    /** Updates the dice face label text. */
    private void updateDiceLabel(String txt) {
        diceLabel.setText(txt);
    }

    /** Highlights the current player and resets others. */
    private void highlightCurrentPlayer() {
        List<Player> players = controller.getGame().getPlayers();
        for (int i = 0; i < players.size(); i++) {
            HBox box = (HBox) playerStatusPane.getChildren().get(i);
            box.setStyle("-fx-background-color: #D2B48C; -fx-background-radius: 10px; -fx-padding: 10px;");
        }
        
        int idx = players.indexOf(currentPlayer);
        HBox curr = (HBox) playerStatusPane.getChildren().get(idx);
        curr.setStyle("-fx-background-color: #FFFACD; -fx-background-radius: 10px; -fx-border-color: #FF5722; -fx-border-width: 2px; -fx-border-radius: 10px; -fx-padding: 10px;");
    }
    
    /**
     * Checks if a player landed on a property that can be purchased and displays a purchase dialog.
     */
    private void checkPropertyPurchase(Player player) {
        TileAction action = player.getCurrentTile().getAction();
        if (action instanceof PropertyAction propertyAction) {
            if (propertyAction.getOwner() == null) {
                showPropertyPurchaseDialog(player, propertyAction);
            } else if (!propertyAction.getOwner().equals(player)) {
                // Player landed on someone else's property - pay rent
                int rentAmount = propertyAction.getRent();
                player.decreaseMoney(rentAmount);
                propertyAction.getOwner().increaseMoney(rentAmount);
                
                // Show info dialog about rent payment
                Alert rentAlert = new Alert(Alert.AlertType.INFORMATION);
                rentAlert.setTitle("Rent Paid");
                rentAlert.setHeaderText("Rent Paid");
                rentAlert.setContentText(player.getName() + " paid $" + rentAmount + 
                                        " rent to " + propertyAction.getOwner().getName() +
                                        " for landing on " + propertyAction.getName());
                rentAlert.showAndWait();
            }
        }
    }
    
    /**
     * Shows a dialog offering the player the option to purchase a property.
     */
    private void showPropertyPurchaseDialog(Player player, PropertyAction propertyAction) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Property Available");
        alert.setHeaderText("Buy Property: " + propertyAction.getName());
        alert.setContentText("Would you like to buy " + propertyAction.getName() + 
                            " for $" + propertyAction.getCost() + "?\n\n" +
                            "Rent: $" + propertyAction.getRent() + "\n" +
                            "Your money: $" + player.getMoney());

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            // Player chose to buy the property
            if (player.getMoney() >= propertyAction.getCost()) {
                player.decreaseMoney(propertyAction.getCost());
                propertyAction.setOwner(player);
                
                // Add property to the player's portfolio in MonopolyService
                MonopolyService monopolyService = ServiceLocator.getMonopolyService();
                if (monopolyService != null) {
                    monopolyService.addProperty(player, propertyAction);
                }
                
                Alert successAlert = new Alert(Alert.AlertType.INFORMATION);
                successAlert.setTitle("Property Purchased");
                successAlert.setHeaderText("Congratulations!");
                successAlert.setContentText("You now own " + propertyAction.getName() + ".\n" +
                                           "Your remaining balance: $" + player.getMoney());
                successAlert.showAndWait();
            } else {
                // Not enough money
                Alert errorAlert = new Alert(Alert.AlertType.ERROR);
                errorAlert.setTitle("Insufficient Funds");
                errorAlert.setHeaderText("You don't have enough money!");
                errorAlert.setContentText("You need $" + propertyAction.getCost() + 
                                         " to buy this property, but you only have $" + player.getMoney() + ".");
                errorAlert.showAndWait();
            }
        }
    }
    
    /** 
     * Inner class for rendering the Monopoly board.
     * This is a simplified version of a Monopoly board with property tiles.
     */
    private static class MonopolyBoardView extends Pane {
        private static final double SIZE = 600;
        private final BoardGame game;
        private final Map<Integer, Point2D> tilePositions = new HashMap<>();
        private final Map<Player, Circle> playerTokens = new HashMap<>();
        
        // Monopoly property colors
        private final Color[] propertyColors = {
            Color.BROWN, Color.LIGHTBLUE, Color.PINK, Color.ORANGE,
            Color.RED, Color.YELLOW, Color.GREEN, Color.BLUE
        };

        public MonopolyBoardView(BoardGame game) {
            this.game = game;
            setPrefSize(SIZE, SIZE);
            
            initializeBoard();
            initializePlayerTokens();
            refresh();
        }

        private void initializeBoard() {
            int tileCount = game.getBoard().getTiles().size();
            
            // Calculate how many tiles per side (assuming 4 sides)
            int tilesPerSide = (tileCount / 4) + 1;
            double cellSize = SIZE / tilesPerSide;

            // Center space (where players will start)
            Rectangle center = new Rectangle(cellSize, cellSize, SIZE - 2 * cellSize, SIZE - 2 * cellSize);
            center.setFill(Color.LIGHTYELLOW);
            center.setStroke(Color.BLACK);
            getChildren().add(center);
            
            // Add "MONOPOLY" text in center
            Label centerLabel = new Label("MONOPOLY");
            centerLabel.setFont(Font.font("Kabel", FontWeight.BOLD, 36));
            centerLabel.setTextFill(Color.RED);
            centerLabel.setLayoutX(SIZE / 2 - 100);
            centerLabel.setLayoutY(SIZE / 2 - 20);
            getChildren().add(centerLabel);

            // Create tiles around the board in a square pattern
            for (int i = 0; i < tileCount; i++) {
                int tileId = i;
                Tile tile = game.getBoard().getTile(tileId);
                
                // Calculate position based on which side of the board we're on
                Point2D position = calculateTilePosition(tileId, tilesPerSide, cellSize);
                tilePositions.put(tileId, position);
                
                // Create tile with property color if applicable
                Rectangle tileRect = createTile(position.getX(), position.getY(), cellSize, i % 8, tile);
                getChildren().add(tileRect);
                
                // Add tile number
                Text tileText = new Text(String.valueOf(tileId));
                tileText.setX(position.getX() - 5);
                tileText.setY(position.getY() - 15);
                tileText.setFont(Font.font("Arial", FontWeight.BOLD, 10));
                getChildren().add(tileText);
                
                // Add property name if it's a property
                if (tile.getAction() instanceof PropertyAction propertyAction) {
                    String propertyName = propertyAction.getName();
                    Text nameText = new Text(propertyName);
                    nameText.setFont(Font.font("Arial", 8));
                    nameText.setTextAlignment(TextAlignment.CENTER);
                    nameText.setWrappingWidth(cellSize - 4);
                    
                    // Position the name based on the side of the board
                    int side = tileId / (tilesPerSide - 1);
                    switch (side) {
                        case 0: // Bottom row
                            nameText.setX(position.getX() - cellSize/2 + 2);
                            nameText.setY(position.getY() + 5);
                            break;
                        case 1: // Left column
                            nameText.setX(position.getX() - cellSize/2 + 2);
                            nameText.setY(position.getY() + 5);
                            nameText.setRotate(-90);
                            break;
                        case 2: // Top row
                            nameText.setX(position.getX() - cellSize/2 + 2);
                            nameText.setY(position.getY() + 5);
                            nameText.setRotate(180);
                            break;
                        case 3: // Right column
                            nameText.setX(position.getX() - cellSize/2 + 2);
                            nameText.setY(position.getY() + 5);
                            nameText.setRotate(90);
                            break;
                    }
                    getChildren().add(nameText);
                    
                    // Add price below property name
                    Text priceText = new Text("$" + propertyAction.getCost());
                    priceText.setFont(Font.font("Arial", FontWeight.BOLD, 8));
                    priceText.setFill(Color.BLACK);
                    
                    // Position price based on the side of the board
                    switch (side) {
                        case 0: // Bottom row
                            priceText.setX(position.getX() - 10);
                            priceText.setY(position.getY() + 15);
                            break;
                        case 1: // Left column
                            priceText.setX(position.getX() - 15);
                            priceText.setY(position.getY() + 10);
                            priceText.setRotate(-90);
                            break;
                        case 2: // Top row
                            priceText.setX(position.getX() - 10);
                            priceText.setY(position.getY() - 10);
                            break;
                        case 3: // Right column
                            priceText.setX(position.getX() + 10);
                            priceText.setY(position.getY() - 10);
                            priceText.setRotate(90);
                            break;
                    }
                    getChildren().add(priceText);
                }
            }
        }

        private Point2D calculateTilePosition(int tileId, int tilesPerSide, double cellSize) {
            double x, y;
            int side = tileId / (tilesPerSide - 1);
            int position = tileId % (tilesPerSide - 1);
            
            switch (side) {
                case 0: // Bottom row
                    x = SIZE - cellSize / 2 - position * cellSize;
                    y = SIZE - cellSize / 2;
                    break;
                case 1: // Left column
                    x = cellSize / 2;
                    y = SIZE - cellSize / 2 - position * cellSize;
                    break;
                case 2: // Top row
                    x = cellSize / 2 + position * cellSize;
                    y = cellSize / 2;
                    break;
                case 3: // Right column
                    x = SIZE - cellSize / 2;
                    y = cellSize / 2 + position * cellSize;
                    break;
                default:
                    x = SIZE / 2;
                    y = SIZE / 2;
            }
            
            return new Point2D(x, y);
        }

        private Rectangle createTile(double x, double y, double size, int colorIndex, Tile tile) {
            Rectangle tileRect = new Rectangle(x - size / 2, y - size / 2, size, size);
            
            // Special tiles
            if (tile.getId() == 0) {
                tileRect.setFill(Color.GREEN); // GO
                tileRect.setStroke(Color.BLACK);
                tileRect.setStrokeWidth(2);
                return tileRect;
            } else if (tile.getId() % 10 == 0) {
                tileRect.setFill(Color.GRAY); // Special corners
                tileRect.setStroke(Color.BLACK);
                tileRect.setStrokeWidth(2);
                return tileRect;
            }
            
            // Property tiles
            if (tile.getAction() instanceof PropertyAction propertyAction) {
                // Check if the property is owned and change the appearance accordingly
                Player owner = propertyAction.getOwner();
                if (owner != null) {
                    // Add a border in the player's color to indicate ownership
                    tileRect.setStroke(getPlayerColor(owner));
                    tileRect.setStrokeWidth(3);
                }
                
                Color mainColor = propertyColors[colorIndex % propertyColors.length];
                tileRect.setFill(mainColor);
            } else {
                // Non-property tiles
                tileRect.setFill(Color.LIGHTGRAY);
            }
            
            tileRect.setStroke(Color.BLACK);
            tileRect.setStrokeWidth(1);
            
            return tileRect;
        }
        
        private Color getPlayerColor(Player player) {
            // Find the color of the player's token
            for (Map.Entry<Player, Circle> entry : playerTokens.entrySet()) {
                if (entry.getKey().equals(player)) {
                    return (Color) entry.getValue().getFill();
                }
            }
            return Color.BLACK; // Default
        }

        private void initializePlayerTokens() {
            Color[] colors = { 
                Color.RED, Color.BLUE, 
                Color.GREEN, Color.ORANGE, 
                Color.PURPLE, Color.BLACK 
            };
            int playerIndex = 0;

            for (Player player : game.getPlayers()) {
                Circle token = new Circle(10, colors[playerIndex++ % colors.length]);
                token.setStroke(Color.BLACK);
                token.setStrokeWidth(2);
                playerTokens.put(player, token);
                getChildren().add(token);
            }
        }

        public void refresh() {
            // Update token positions based on player locations
            for (Player player : game.getPlayers()) {
                int tileId = player.getCurrentTile().getId();
                Point2D position = tilePositions.get(tileId);

                if (position != null && playerTokens.containsKey(player)) {
                    Circle token = playerTokens.get(player);
                    // Add a slight offset for each player to prevent overlap
                    int index = game.getPlayers().indexOf(player);
                    double offsetX = (index % 2) * 10;
                    double offsetY = (index / 2) * 10;
                    
                    token.setCenterX(position.getX() + offsetX - 10);
                    token.setCenterY(position.getY() + offsetY - 10);
                }
            }
            
            // Update property ownership indicators
            for (int i = 0; i < game.getBoard().getTiles().size(); i++) {
                Tile tile = game.getBoard().getTile(i);
                if (tile.getAction() instanceof PropertyAction propertyAction) {
                    Point2D position = tilePositions.get(i);
                    Player owner = propertyAction.getOwner();
                    
                    // Find the tile rectangle
                    for (javafx.scene.Node node : getChildren()) {
                        if (node instanceof Rectangle rectangle) {
                            double centerX = rectangle.getX() + rectangle.getWidth() / 2;
                            double centerY = rectangle.getY() + rectangle.getHeight() / 2;
                            
                            if (Math.abs(centerX - position.getX()) < 1 && 
                                Math.abs(centerY - position.getY()) < 1) {
                                
                                // Update the rectangle's appearance based on ownership
                                if (owner != null) {
                                    rectangle.setStroke(getPlayerColor(owner));
                                    rectangle.setStrokeWidth(3);
                                } else {
                                    rectangle.setStroke(Color.BLACK);
                                    rectangle.setStrokeWidth(1);
                                }
                                break;
                            }
                        }
                    }
                }
            }
        }
    }
}
