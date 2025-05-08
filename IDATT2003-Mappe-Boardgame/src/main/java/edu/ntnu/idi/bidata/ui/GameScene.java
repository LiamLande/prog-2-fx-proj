package edu.ntnu.idi.bidata.ui;

import edu.ntnu.idi.bidata.controller.GameController; // Assuming GameController is in this package
import edu.ntnu.idi.bidata.model.BoardGame;
import edu.ntnu.idi.bidata.model.Player;
import javafx.beans.value.ChangeListener;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage; // Stage is kept for context if needed by SceneManager
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Main game view for Snakes and Ladders: board + side panel.
 * Interacts with a GameController to handle game logic and updates.
 */
public class GameScene implements SceneManager.ControlledScene {
  private final GameController controller;
  private final BoardGame gameModel; // Store game model for UI updates reading player data
  private final BoardView boardView;
  private VBox playerStatusPane;
  private final Map<Player, Label> playerPositionLabels = new HashMap<>();
  private final Map<Player, Circle> playerTokenUIs = new HashMap<>(); // Renamed to avoid confusion
  private final Scene scene;
  private Label diceLabel;
  private Button rollButton;

  // Runnables for external navigation, handled by a higher-level coordinator
  private final Runnable onNewGameCallback;
  private final Runnable onHomeCallback;

  public GameScene(Stage stage, // Kept for SceneManager compatibility
                   GameController gameController,
                   BoardGame gameModel,
                   Runnable onNewGame,
                   Runnable onHome) {
    this.controller = gameController;
    this.gameModel = gameModel; // Keep a reference to the model for reading display data
    this.onNewGameCallback = onNewGame;
    this.onHomeCallback = onHome;

    BorderPane root = new BorderPane();
    Image bgImg = new Image(
            getClass().getClassLoader().getResourceAsStream("images/sl_game_background.jpg")
    );
    BackgroundSize bgSize = new BackgroundSize(1.0, 1.0, true, true, false, true);
    BackgroundImage bgImage = new BackgroundImage(bgImg,
            BackgroundRepeat.NO_REPEAT, BackgroundRepeat.NO_REPEAT,
            BackgroundPosition.CENTER, bgSize);
    root.setBackground(new Background(bgImage));

    this.boardView = new BoardView(this.gameModel);
    StackPane boardContainer = createBoardContainer(this.boardView);
    VBox sidePanel = createSidePanel(this.gameModel.getPlayers());

    root.setCenter(boardContainer);
    root.setRight(sidePanel);
    root.setPadding(new Insets(20));

    scene = new Scene(root, 1100, 800);

    // The controller will call public methods on this GameScene to update UI.
    // No GameListener implementation here anymore.
  }

  /**
   * Called by the controller to initiate the game view setup.
   */
  public void initializeView() {
    updateDiceLabel("⚄"); // Initial dice face
    updatePlayerStatusDisplay(); // Update based on initial model state
    if (!gameModel.getPlayers().isEmpty()) {
      highlightCurrentPlayer(gameModel.getPlayers().getFirst()); // Highlight first player
    }
    setRollButtonEnabled(true); // Enable roll button
    boardView.refresh(); // Ensure board is drawn correctly
  }


  public Parent getRoot() { return scene.getRoot(); }
  public Scene getScene() { return scene; }

  @Override public void onShow() { /* Managed by SceneManager or App Controller */ }
  @Override public void onHide() { /* Managed by SceneManager or App Controller */ }

  public BoardView getBoardView() {
    return boardView;
  }

  private StackPane createBoardContainer(BoardView board) {
    Group boardGroup = new Group(board);
    Region border = new Region();
    border.setStyle("-fx-background-color: transparent; -fx-background-radius: 15px; -fx-padding: 15px;");

    StackPane container = new StackPane(border, boardGroup);
    StackPane.setMargin(boardGroup, new Insets(10));
    container.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);

    ChangeListener<Number> resizer = (obs, old, nw) -> {
      double availW = container.getWidth() - 30;
      double availH = container.getHeight() - 30;
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
    titleBox.setStyle("-fx-background-color: #F5EBDA; -fx-background-radius: 10px;");
    Label titleLabel = new Label("Ancient Journey");
    titleLabel.setFont(Font.font("Serif", FontWeight.BOLD, 32));
    titleLabel.setTextFill(Color.web("#5A3A22"));
    Label subtitleLabel = new Label("A Game of Destiny and Fortune");
    subtitleLabel.setFont(Font.font("Serif", FontWeight.NORMAL, 16));
    subtitleLabel.setTextFill(Color.web("#5A3A22"));
    titleBox.getChildren().addAll(titleLabel, subtitleLabel);

    playerStatusPane = new VBox(10);
    playerStatusPane.setAlignment(Pos.CENTER);
    createPlayerStatusBoxes(players); // Initial setup
    VBox playerSection = new VBox(15, playerStatusPane);
    playerSection.setAlignment(Pos.TOP_CENTER);
    playerSection.setPadding(new Insets(20));
    playerSection.setStyle("-fx-background-color: #F5EBDA; -fx-background-radius: 10px;");

    diceLabel = new Label("⚄");
    diceLabel.setFont(Font.font("Serif", FontWeight.BOLD, 48));
    diceLabel.setTextFill(Color.web("#5A3A22"));
    rollButton = new Button("Roll Dice");
    styleButton(rollButton);
    // Controller handles the action. Assumes a method like handleRollDiceRequest exists.
    rollButton.setOnAction(e -> controller.handleRollDiceRequest());
    rollButton.setPrefWidth(150);
    VBox diceBox = new VBox(10, diceLabel, rollButton);
    diceBox.setAlignment(Pos.CENTER);
    diceBox.setPadding(new Insets(20));
    diceBox.setStyle("-fx-background-color: #F5EBDA; -fx-background-radius: 10px;");

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
    playerTokenUIs.clear();
    Color[] colors = { Color.RED, Color.BLUE, Color.GREEN, Color.PURPLE };
    for (int i = 0; i < players.size(); i++) {
      Player p = players.get(i);
      HBox box = new HBox(15);
      box.setAlignment(Pos.CENTER_LEFT);
      box.setPadding(new Insets(10));
      box.setStyle("-fx-background-color: #F5EBDA; -fx-background-radius: 10px;");
      Circle tok = new Circle(15, colors[i % colors.length]);
      tok.setStroke(Color.BLACK);
      playerTokenUIs.put(p, tok);
      Label name = new Label(p.getName());
      name.setFont(Font.font("Serif", FontWeight.BOLD, 16));
      name.setTextFill(Color.web("#5A3A22"));
      Label pos = new Label(String.valueOf(p.getCurrentTile().getId()));
      pos.setFont(Font.font("Serif", FontWeight.NORMAL, 24));
      pos.setTextFill(Color.web("#5A3A22"));
      playerPositionLabels.put(p, pos);
      VBox info = new VBox(5, name, pos);
      info.setAlignment(Pos.CENTER_LEFT);
      box.getChildren().addAll(tok, info);
      playerStatusPane.getChildren().add(box);
    }
  }

  private void styleButton(Button btn) {
    btn.setFont(Font.font("Serif", FontWeight.BOLD, 16));
    btn.setStyle("-fx-background-color: #E5B85C; -fx-text-fill: #5A3A22; -fx-background-radius: 5px; -fx-cursor: hand;");
    btn.setOnMouseEntered(e -> btn.setStyle("-fx-background-color: #D4A74A; -fx-text-fill: #5A3A22; -fx-background-radius: 5px; -fx-cursor: hand;"));
    btn.setOnMouseExited(e -> btn.setStyle("-fx-background-color: #E5B85C; -fx-text-fill: #5A3A22; -fx-background-radius: 5px; -fx-cursor: hand;"));
  }

  // --- Public methods to be called by the Controller ---

  /** Refreshes all player position labels and other status indicators from the model. */
  public void updatePlayerStatusDisplay() {
    // Re-create or update status boxes if player list changes dynamically (not typical for this game)
    // For now, assume player list is static once game starts, just update positions.
    if (gameModel == null || gameModel.getPlayers().isEmpty()) return;

    // If player status boxes aren't matching gameModel.getPlayers(), recreate them
    if (playerStatusPane.getChildren().size() != gameModel.getPlayers().size() ||
            !playerTokenUIs.keySet().containsAll(gameModel.getPlayers())) {
      createPlayerStatusBoxes(gameModel.getPlayers());
    }

    playerPositionLabels.forEach((p, lbl) -> {
      if (p.getCurrentTile() != null) { // Ensure player has a tile
        lbl.setText(String.valueOf(p.getCurrentTile().getId()));
      }
    });
    // Potentially update other player-specific UI elements here
  }

  /** Updates the dice face label text. */
  public void updateDiceLabel(String text) {
    if (diceLabel != null) {
      diceLabel.setText(text);
    }
  }

  /** Highlights the current player and resets others. */
  public void highlightCurrentPlayer(Player playerToHighlight) {
    if (gameModel == null || gameModel.getPlayers().isEmpty() || playerToHighlight == null) return;

    List<Player> players = gameModel.getPlayers();
    for (int i = 0; i < players.size(); i++) {
      Node node = playerStatusPane.getChildren().get(i);
      if (node instanceof HBox) {
        HBox box = (HBox) node;
        box.setStyle("-fx-background-color: #F5EBDA; -fx-background-radius: 10px; -fx-padding: 10px;");
      }
    }
    if (players.contains(playerToHighlight)) {
      int idx = players.indexOf(playerToHighlight);
      if (idx >= 0 && idx < playerStatusPane.getChildren().size()) {
        Node node = playerStatusPane.getChildren().get(idx);
        if (node instanceof HBox) {
          HBox curr = (HBox) node;
          curr.setStyle("-fx-background-color: #FFE7B3; -fx-background-radius: 10px; -fx-border-color: #E5B85C; -fx-border-width: 2px; -fx-border-radius: 10px; -fx-padding: 10px;");
        }
      }
    }
  }

  /** Enables or disables the roll dice button. */
  public void setRollButtonEnabled(boolean enabled) {
    if (rollButton != null) {
      rollButton.setDisable(!enabled);
    }
  }

  /** Displays the game over state. */
  public void displayGameOver(Player winner) {
    updateDiceLabel("🏆"); // Winner symbol
    setRollButtonEnabled(false);

    // Reset styles for all player boxes
    for (Player p : gameModel.getPlayers()) {
      int playerIndex = gameModel.getPlayers().indexOf(p);
      if (playerIndex >= 0 && playerIndex < playerStatusPane.getChildren().size()) {
        Node node = playerStatusPane.getChildren().get(playerIndex);
        if (node instanceof HBox) {
          HBox box = (HBox) node;
          box.setStyle("-fx-background-color: #F5EBDA; -fx-background-radius: 10px; -fx-padding: 10px;");
        }
      }
    }

    // Highlight winner
    if (winner != null && gameModel.getPlayers().contains(winner)) {
      int winnerIndex = gameModel.getPlayers().indexOf(winner);
      if (winnerIndex >= 0 && winnerIndex < playerStatusPane.getChildren().size()) {
        Node node = playerStatusPane.getChildren().get(winnerIndex);
        if (node instanceof HBox) {
          HBox winBox = (HBox) node;
          winBox.setStyle("-fx-background-color: #FFD700; -fx-background-radius: 10px; -fx-padding: 10px;");
        }
      }
      Circle tokenUI = playerTokenUIs.get(winner);
      if (tokenUI != null) {
        tokenUI.setStroke(Color.GOLD);
        tokenUI.setStrokeWidth(3);
      }
    }
    // boardView.refresh(); // Already done by controller or after player move
    updatePlayerStatusDisplay(); // Ensure final positions are shown
  }
}