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
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
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
        // Set up the background image
        Image bgImg = new Image(Objects.requireNonNull(getClass().getClassLoader().getResourceAsStream("images/monopoly_bg.jpg")));
        ImageView bgView = new ImageView(bgImg);
        bgView.setPreserveRatio(true);
        root.getChildren().add(bgView);

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


}