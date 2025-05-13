package edu.ntnu.idi.bidata.ui;

import edu.ntnu.idi.bidata.controller.GameController;
import edu.ntnu.idi.bidata.model.BoardGame;
import edu.ntnu.idi.bidata.model.Player;
import javafx.beans.value.ChangeListener;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import javafx.stage.Stage;

public class GameScene implements SceneManager.ControlledScene {
  private final GameController controller;
  private final BoardGame gameModel;
  private BoardView boardView;
  private VBox playerStatusPane;
  private final Map<Player, Label> playerPositionLabels = new HashMap<>();
  private final Map<Player, ImageView> sidePanelPieceViews = new HashMap<>();
  private final Scene scene;
  private Label diceLabel;
  private Button rollButton;
  private final Runnable onNewGameCallback, onHomeCallback;
  private final SnakeLadderPlayerSetupScene.Theme theme;

  private static final String EGYPT_GAME_BG = "/images/sl_game_background.jpg";
  private static final String JUNGLE_GAME_BG = "/images/jungle_game_background.png";
  private static final double SIDE_PANEL_PIECE_SIZE = 24;


  public GameScene(Stage stage, GameController gameController, BoardGame gameModel,
      Runnable onNewGame, Runnable onHome, SnakeLadderPlayerSetupScene.Theme gameTheme) {
    this.controller = gameController;
    this.gameModel = gameModel;
    this.onNewGameCallback = onNewGame;
    this.onHomeCallback = onHome;
    this.theme = gameTheme;

    BorderPane root = new BorderPane();
    String bgPath = (this.theme == SnakeLadderPlayerSetupScene.Theme.JUNGLE) ? JUNGLE_GAME_BG : EGYPT_GAME_BG;
    try {
      Image bgImg = new Image(Objects.requireNonNull(getClass().getResourceAsStream(bgPath), "Background image not found: " + bgPath));
      BackgroundImage bgImage = new BackgroundImage(bgImg, BackgroundRepeat.NO_REPEAT, BackgroundRepeat.NO_REPEAT, BackgroundPosition.CENTER, new BackgroundSize(1.0, 1.0, true, true, false, true));
      root.setBackground(new Background(bgImage));
    } catch (Exception e) {
      System.err.println("Failed to load background image for GameScene: " + bgPath + ". " + e.getMessage());
      root.setStyle("-fx-background-color: #DEB887;"); // Fallback color
    }


    this.boardView = new BoardView(this.gameModel, this.theme);
    StackPane boardContainer = createBoardContainer(this.boardView);
    VBox sidePanel = createSidePanel(); // Players will be passed via model during refresh

    root.setCenter(boardContainer);
    root.setRight(sidePanel);
    root.setPadding(new Insets(20));

    scene = new Scene(root, 1100, 800);
  }

  public void initializeView() {
    updateDiceLabel("⚄");
    createPlayerStatusBoxes(gameModel.getPlayers()); // Create status boxes with current players
    updatePlayerStatusDisplay(); // Update positions
    if (gameModel.getCurrentPlayer() != null) {
      highlightCurrentPlayer(gameModel.getCurrentPlayer());
    } else if (!gameModel.getPlayers().isEmpty()){
      highlightCurrentPlayer(gameModel.getPlayers().getFirst());
    }
    setRollButtonEnabled(true);
    boardView.initializePlayerTokenVisuals(); // Crucial: Tell BoardView to create its tokens
    boardView.refresh();
  }

  public Scene getScene() { return scene; }
  @Override public void onShow() {}
  @Override public void onHide() {}
  public BoardView getBoardView() { return boardView; }

  private StackPane createBoardContainer(BoardView board) {
    Group boardGroup = new Group(board);
    Region border = new Region(); // Used for padding/styling effect
    border.setStyle("-fx-background-color: transparent; -fx-background-radius: 15px; -fx-padding: 15px;"); // Example style

    StackPane container = new StackPane(border, boardGroup);
    StackPane.setMargin(boardGroup, new Insets(10)); // Margin for the board itself within the border
    container.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE); // Allow container to grow

    // Resizing logic for the board
    ChangeListener<Number> resizer = (obs, oldVal, newVal) -> {
      double availableWidth = container.getWidth() - (2 * StackPane.getMargin(boardGroup).getLeft()) - (border.getInsets().getLeft() + border.getInsets().getRight());
      double availableHeight = container.getHeight() - (2 * StackPane.getMargin(boardGroup).getTop()) - (border.getInsets().getTop() + border.getInsets().getBottom());

      if (availableWidth <=0 || availableHeight <= 0) return;

      double scale = Math.min(availableWidth / board.getPrefWidth(), availableHeight / board.getPrefHeight());
      boardGroup.setScaleX(scale);
      boardGroup.setScaleY(scale);
    };

    container.widthProperty().addListener(resizer);
    container.heightProperty().addListener(resizer);
    return container;
  }

  private VBox createSidePanel() {
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
    playerStatusPane.setAlignment(Pos.CENTER_LEFT);
    playerStatusPane.setPadding(new Insets(10));

    VBox playerSection = new VBox(15, new Label("Players:"), playerStatusPane);
    playerSection.setAlignment(Pos.TOP_CENTER);
    playerSection.setPadding(new Insets(20));
    playerSection.setStyle("-fx-background-color: #F5EBDA; -fx-background-radius: 10px;");


    diceLabel = new Label("⚄");
    diceLabel.setFont(Font.font("Serif", FontWeight.BOLD, 48));
    diceLabel.setTextFill(Color.web("#5A3A22"));
    rollButton = new Button("Roll Dice");
    styleButton(rollButton);
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
    sidePanelPieceViews.clear();

    if (players == null) return;

    for (Player p : players) {
      HBox box = new HBox(10); // Spacing between elements in the player row
      box.setAlignment(Pos.CENTER_LEFT);
      box.setPadding(new Insets(8)); // Padding within each player's box
      box.setStyle("-fx-background-color: #FFF9E0; -fx-background-radius: 8px;");

      ImageView pieceView = new ImageView();
      pieceView.setFitWidth(SIDE_PANEL_PIECE_SIZE);
      pieceView.setFitHeight(SIDE_PANEL_PIECE_SIZE);
      pieceView.setPreserveRatio(true);

      Optional<PieceUIData> pieceDataOpt = SnakeLadderPlayerSetupScene.AVAILABLE_PIECES.stream()
          .filter(pd -> pd.getIdentifier().equals(p.getPieceIdentifier()))
          .findFirst();

      if (pieceDataOpt.isPresent()) {
        Image pieceImage = pieceDataOpt.get().getImage(SIDE_PANEL_PIECE_SIZE);
        if (pieceImage != null) {
          pieceView.setImage(pieceImage);
        } else { /* TODO: Handle missing image, e.g. placeholder */ }
      } else { /* TODO: Handle missing PieceUIData */ }
      sidePanelPieceViews.put(p, pieceView);

      Label nameLabel = new Label(p.getName());
      nameLabel.setFont(Font.font("Serif", FontWeight.BOLD, 16));
      nameLabel.setTextFill(Color.web("#5A3A22"));

      Label posLabel = new Label("Tile: " + (p.getCurrentTile() != null ? p.getCurrentTile().getId() + 1 : "N/A")); // Display 1-indexed
      posLabel.setFont(Font.font("Serif", FontWeight.NORMAL, 14));
      posLabel.setTextFill(Color.web("#5A3A22"));
      playerPositionLabels.put(p, posLabel);

      VBox playerInfo = new VBox(3, nameLabel, posLabel); // Text info
      box.getChildren().addAll(pieceView, playerInfo);
      playerStatusPane.getChildren().add(box);
    }
  }

  private void styleButton(Button btn) {
    btn.setFont(Font.font("Serif", FontWeight.BOLD, 16));
    btn.setStyle("-fx-background-color: #E5B85C; -fx-text-fill: #5A3A22; -fx-background-radius: 5px; -fx-cursor: hand;");
    btn.setOnMouseEntered(e -> btn.setStyle("-fx-background-color: #D4A74A; -fx-text-fill: #5A3A22; -fx-background-radius: 5px; -fx-cursor: hand;"));
    btn.setOnMouseExited(e -> btn.setStyle("-fx-background-color: #E5B85C; -fx-text-fill: #5A3A22; -fx-background-radius: 5px; -fx-cursor: hand;"));
  }

  public void updatePlayerStatusDisplay() {
    if (gameModel == null || gameModel.getPlayers() == null) return;
    // If the number of players displayed doesn't match the model, recreate the boxes
    if (playerStatusPane.getChildren().size() != gameModel.getPlayers().size()) {
      createPlayerStatusBoxes(gameModel.getPlayers());
    }

    gameModel.getPlayers().forEach(p -> {
      Label posLabel = playerPositionLabels.get(p);
      if (posLabel != null && p.getCurrentTile() != null) {
        posLabel.setText("Tile: " + (p.getCurrentTile().getId() + 1)); // Update position (1-indexed)
      }
    });
  }

  public void updateDiceLabel(String text) { if (diceLabel != null) diceLabel.setText(text); }

  public void highlightCurrentPlayer(Player playerToHighlight) {
    if (playerToHighlight == null || gameModel.getPlayers() == null) return;
    for (Node node : playerStatusPane.getChildren()) {
      if (node instanceof HBox) { // Each player's status is an HBox
        node.setStyle("-fx-background-color: #FFF9E0; -fx-background-radius: 8px; -fx-border-color: transparent;"); // Reset style
      }
    }
    int playerIndex = gameModel.getPlayers().indexOf(playerToHighlight);
    if (playerIndex != -1 && playerIndex < playerStatusPane.getChildren().size()) {
      Node currentPlayerNode = playerStatusPane.getChildren().get(playerIndex);
      currentPlayerNode.setStyle("-fx-background-color: #FFE7B3; -fx-background-radius: 8px; -fx-border-color: #E5B85C; -fx-border-width: 2px; -fx-border-radius: 8px;");
    }
  }

  public void setRollButtonEnabled(boolean enabled) { if (rollButton != null) rollButton.setDisable(!enabled); }

  public void displayGameOver(Player winner) {
    updateDiceLabel("🏆");
    setRollButtonEnabled(false);
    for (Node node : playerStatusPane.getChildren()) { // Reset all highlights
      if (node instanceof HBox) {
        node.setStyle("-fx-background-color: #FFF9E0; -fx-background-radius: 8px; -fx-border-color: transparent;");
      }
    }
    if (winner != null) {
      int winnerIndex = gameModel.getPlayers().indexOf(winner);
      if (winnerIndex != -1 && winnerIndex < playerStatusPane.getChildren().size()) {
        Node winnerNode = playerStatusPane.getChildren().get(winnerIndex);
        winnerNode.setStyle("-fx-background-color: #FFD700; -fx-background-radius: 8px; -fx-padding: 8px; -fx-border-color: #DAA520; -fx-border-width: 2px;"); // Gold highlight
      }
    }
    updatePlayerStatusDisplay();
  }
}