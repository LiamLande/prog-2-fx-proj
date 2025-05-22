package edu.ntnu.idi.bidata.ui.sl;

import edu.ntnu.idi.bidata.controller.GameController;
import edu.ntnu.idi.bidata.model.BoardGame;
import edu.ntnu.idi.bidata.model.Player;
import edu.ntnu.idi.bidata.model.actions.snakes.SchrodingerBoxAction;
import edu.ntnu.idi.bidata.ui.PieceUIData;
import edu.ntnu.idi.bidata.ui.SceneManager.ControlledScene;
import javafx.beans.value.ChangeListener;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
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

public class SnakeLadderGameScene implements ControlledScene {
  private final GameController controller;
  private final BoardGame gameModel;
  private final SnakeLadderBoardView snakeLadderBoardView;
  private VBox playerStatusPane;
  private final Map<Player, Label> playerPositionLabels = new HashMap<>();
  private final Map<Player, ImageView> sidePanelPieceViews = new HashMap<>();
  private final Scene scene;
  private Label diceLabel;
  private Button rollButton;
  private final Runnable onNewGameCallback, onHomeCallback;
  private final SnakeLadderPlayerSetupScene.Theme theme;

  // Schrödinger UI elements
  private Button schrodingerObserveButton;
  private Button schrodingerIgnoreButton;
  private VBox schrodingerChoiceBox;
  private Label gameMessageLabel; // For general game messages

  private static final String EGYPT_GAME_BG = "/images/sl_game_background.jpg";
  private static final String JUNGLE_GAME_BG = "/images/jungle_game_background.png";
  private static final double SIDE_PANEL_PIECE_SIZE = 24;

  public SnakeLadderGameScene(Stage stage, GameController gameController, BoardGame gameModel,
      Runnable onNewGame, Runnable onHome, SnakeLadderPlayerSetupScene.Theme gameTheme) {
    this.controller = gameController;
    this.gameModel = gameModel;
    this.onNewGameCallback = onNewGame;
    this.onHomeCallback = onHome;
    this.theme = gameTheme;

    BorderPane root = new BorderPane();
    String bgPath = (this.theme == SnakeLadderPlayerSetupScene.Theme.JUNGLE) ? JUNGLE_GAME_BG : EGYPT_GAME_BG;
    try {
      Image bgImg = new Image(Objects.requireNonNull(getClass().getResourceAsStream(bgPath),"Bg image not found"));
      BackgroundImage bg = new BackgroundImage(bgImg, BackgroundRepeat.NO_REPEAT, BackgroundRepeat.NO_REPEAT,
          BackgroundPosition.CENTER, new BackgroundSize(1.0,1.0,true,true,false,true));
      root.setBackground(new Background(bg));
    } catch (Exception e) {
      System.err.println("Failed to load SnakeLadderGameScene background: " + bgPath + ". " + e.getMessage());
      root.setStyle("-fx-background-color: #D2B48C;"); // Tan fallback
    }

    this.snakeLadderBoardView = new SnakeLadderBoardView(this.gameModel, this.theme);
    StackPane boardContainer = createBoardContainer(this.snakeLadderBoardView);
    VBox sidePanel = createSidePanel();

    root.setCenter(boardContainer);
    root.setRight(sidePanel);
    root.setPadding(new Insets(20));
    scene = new Scene(root, 1100, 800);
  }

  public void initializeView() {
    updateDiceLabel("⚀"); // Initial dice face
    createPlayerStatusBoxes(gameModel.getPlayers()); // Create/recreate based on current model
    updatePlayerStatusDisplay();
    hideSchrodingerChoice(); // Ensure hidden on fresh view init

    Player current = gameModel.getCurrentPlayer();
    if (current != null) {
      highlightCurrentPlayer(current);
    } else if (!gameModel.getPlayers().isEmpty()) {
      highlightCurrentPlayer(gameModel.getPlayers().getFirst()); // Highlight first if no current (e.g. pre-game)
    }
    setRollButtonEnabled(!gameModel.isFinished()); // Enable roll if game not over
    snakeLadderBoardView.initializePlayerTokenVisuals();
    snakeLadderBoardView.refresh();
  }

  public Scene getScene() { return scene; }

  public SnakeLadderBoardView getBoardView() { return snakeLadderBoardView; }

  private StackPane createBoardContainer(SnakeLadderBoardView board) {
    Group boardGroup = new Group(board);
    StackPane container = new StackPane(boardGroup);
    StackPane.setMargin(boardGroup, new Insets(10));
    container.setMaxSize(Region.USE_PREF_SIZE, Region.USE_PREF_SIZE); // Let board dictate size initially

    ChangeListener<Number> resizer = (obs, oldVal, newVal) -> {
      double W = container.getWidth() - 20; // -20 for margins
      double H = container.getHeight() - 20;
      if (W <= 0 || H <= 0 || board.getPrefWidth() <=0 || board.getPrefHeight() <=0) return;
      double scale = Math.min(W / board.getPrefWidth(), H / board.getPrefHeight());
      boardGroup.setScaleX(scale);
      boardGroup.setScaleY(scale);
    };
    container.widthProperty().addListener(resizer);
    container.heightProperty().addListener(resizer);
    return container;
  }

  private VBox createSidePanel() {
    VBox sidePanel = new VBox(15); // Adjusted spacing
    sidePanel.setAlignment(Pos.TOP_CENTER);
    sidePanel.setPadding(new Insets(10, 10, 10, 20)); // Top, Right, Bottom, Left
    sidePanel.setPrefWidth(320);
    sidePanel.setStyle("-fx-background-color: rgba(245, 235, 218, 0.85); -fx-background-radius: 10px;");

    // Title
    Label titleLabel = new Label("Ancient Journey");
    titleLabel.setFont(Font.font("Serif", FontWeight.BOLD, 30));
    titleLabel.setTextFill(Color.web("#5A3A22"));
    VBox titleBox = new VBox(titleLabel);
    titleBox.setAlignment(Pos.CENTER);
    titleBox.setPadding(new Insets(10,0,5,0));

    // Player Status Pane
    playerStatusPane = new VBox(8); // Spacing between player entries
    playerStatusPane.setAlignment(Pos.TOP_LEFT); // Align player entries to top-left
    playerStatusPane.setPadding(new Insets(5));

    // Dice Display and Roll Button
    diceLabel = new Label("⚀");
    diceLabel.setFont(Font.font("Serif", FontWeight.BOLD, 48));
    diceLabel.setTextFill(Color.web("#5A3A22"));
    rollButton = new Button("Roll Dice");
    styleButton(rollButton);
    rollButton.setOnAction(e -> controller.handleRollDiceRequest());
    rollButton.setPrefWidth(160);
    VBox diceBox = new VBox(10, diceLabel, rollButton);
    diceBox.setAlignment(Pos.CENTER);
    diceBox.setPadding(new Insets(10,0,10,0));

    // Schrödinger Controls
    schrodingerObserveButton = new Button("Open the Box!");
    styleButton(schrodingerObserveButton, Color.web("#FF6347"), Color.web("#E0523A")); // Tomato color
    schrodingerObserveButton.setTooltip(new Tooltip("Reveal the box's mysterious content!"));
    schrodingerObserveButton.setOnAction(e -> controller.handleObserveSchrodingerBoxRequest());

    schrodingerIgnoreButton = new Button("Ignore & Move On");
    styleButton(schrodingerIgnoreButton, Color.web("#708090"), Color.web("#596773")); // Slate gray color
    schrodingerIgnoreButton.setTooltip(new Tooltip("Leave the box untouched and end your special action."));
    schrodingerIgnoreButton.setOnAction(e -> controller.handleIgnoreSchrodingerBoxRequest());

    schrodingerChoiceBox = new VBox(10, new Label("A Choice Awaits!"), schrodingerObserveButton, schrodingerIgnoreButton);
    schrodingerChoiceBox.setAlignment(Pos.CENTER);
    schrodingerChoiceBox.setPadding(new Insets(10));
    schrodingerChoiceBox.setStyle("-fx-background-color: rgba(211, 211, 211, 0.9); -fx-background-radius: 8px; -fx-border-color: #808080; -fx-border-width: 1px;");
    hideSchrodingerChoice(); // Initially hidden

    // Game Message Label
    gameMessageLabel = new Label("Welcome to the game!");
    gameMessageLabel.setWrapText(true);
    gameMessageLabel.setFont(Font.font("Serif", FontWeight.NORMAL, 14));
    gameMessageLabel.setTextFill(Color.web("#4A4A4A"));
    gameMessageLabel.setPadding(new Insets(5));
    gameMessageLabel.setStyle("-fx-background-color: rgba(255, 250, 240, 0.9); -fx-background-radius: 5px;"); // FloralWhite
    gameMessageLabel.setMinHeight(40); // Ensure space for messages

    // Game Controls
    Button newGameBtn = new Button("New Game");
    styleButton(newGameBtn);
    newGameBtn.setOnAction(e -> onNewGameCallback.run());
    Button homeBtn = new Button("Return Home");
    styleButton(homeBtn);
    homeBtn.setOnAction(e -> onHomeCallback.run());
    HBox gameControlsBox = new HBox(15, newGameBtn, homeBtn);
    gameControlsBox.setAlignment(Pos.CENTER);
    gameControlsBox.setPadding(new Insets(10,0,10,0));

    sidePanel.getChildren().addAll(titleBox, playerStatusPane, diceBox, schrodingerChoiceBox, gameMessageLabel, gameControlsBox);
    return sidePanel;
  }

  private void createPlayerStatusBoxes(List<Player> players) {
    playerStatusPane.getChildren().clear();
    playerPositionLabels.clear();
    sidePanelPieceViews.clear();
    if (players == null) return;

    for (Player p : players) {
      HBox playerBox = new HBox(8);
      playerBox.setAlignment(Pos.CENTER_LEFT);
      playerBox.setPadding(new Insets(6));
      playerBox.setStyle("-fx-background-color: rgba(255, 249, 224, 0.8); -fx-background-radius: 6px;"); // Light beige

      ImageView pieceView = new ImageView();
      pieceView.setFitWidth(SIDE_PANEL_PIECE_SIZE);
      pieceView.setFitHeight(SIDE_PANEL_PIECE_SIZE);
      pieceView.setPreserveRatio(true);
      Optional<PieceUIData> pieceDataOpt = SnakeLadderPlayerSetupScene.AVAILABLE_PIECES.stream()
          .filter(pd -> pd.getIdentifier().equals(p.getPieceIdentifier()))
          .findFirst();
      if (pieceDataOpt.isPresent() && pieceDataOpt.get().getImage(SIDE_PANEL_PIECE_SIZE) != null) {
        pieceView.setImage(pieceDataOpt.get().getImage(SIDE_PANEL_PIECE_SIZE));
      }

      sidePanelPieceViews.put(p, pieceView);

      Label nameLbl = new Label(p.getName());
      nameLbl.setFont(Font.font("Serif", FontWeight.BOLD, 15));
      nameLbl.setTextFill(Color.web("#5A3A22"));
      nameLbl.setMinWidth(80); // Give some min width for names

      Label posLbl = new Label("Tile: " + (p.getCurrentTile() != null ? (p.getCurrentTile().getId() + 1) : "N/A"));
      posLbl.setFont(Font.font("Serif", FontWeight.NORMAL, 14));
      posLbl.setTextFill(Color.web("#5A3A22"));
      playerPositionLabels.put(p, posLbl);

      VBox textInfo = new VBox(2, nameLbl, posLbl);
      playerBox.getChildren().addAll(pieceView, textInfo);
      playerStatusPane.getChildren().add(playerBox);
    }
  }

  private void styleButton(Button btn) {
    styleButton(btn, Color.web("#E5B85C"), Color.web("#D4A74A")); // Default colors
  }
  private void styleButton(Button btn, Color baseColor, Color hoverColor) {
    String baseRgb = String.format("rgba(%d, %d, %d, 0.9)", (int)(baseColor.getRed()*255), (int)(baseColor.getGreen()*255), (int)(baseColor.getBlue()*255));
    String hoverRgb = String.format("rgba(%d, %d, %d, 1.0)", (int)(hoverColor.getRed()*255), (int)(hoverColor.getGreen()*255), (int)(hoverColor.getBlue()*255));

    btn.setFont(Font.font("Serif", FontWeight.BOLD, 15));
    btn.setStyle("-fx-background-color: " + baseRgb + "; -fx-text-fill: #5A3A22; -fx-background-radius: 5px; -fx-cursor: hand; -fx-padding: 6 12 6 12;");
    btn.setOnMouseEntered(e -> btn.setStyle("-fx-background-color: " + hoverRgb + "; -fx-text-fill: #5A3A22; -fx-background-radius: 5px; -fx-cursor: hand; -fx-padding: 6 12 6 12;"));
    btn.setOnMouseExited(e -> btn.setStyle("-fx-background-color: " + baseRgb + "; -fx-text-fill: #5A3A22; -fx-background-radius: 5px; -fx-cursor: hand; -fx-padding: 6 12 6 12;"));
  }


  public void updatePlayerStatusDisplay() {
    if (gameModel == null || gameModel.getPlayers() == null) return;
    if (playerStatusPane.getChildren().size() != gameModel.getPlayers().size()) {
      createPlayerStatusBoxes(gameModel.getPlayers()); // Recreate if player count changed
    } else { // Just update existing labels
      gameModel.getPlayers().forEach(p -> {
        Label posLabel = playerPositionLabels.get(p);
        if (posLabel != null && p.getCurrentTile() != null) {
          posLabel.setText("Tile: " + (p.getCurrentTile().getId() + 1));
        }
      });
    }
  }

  public void updateDiceLabel(String text) { if (diceLabel != null) diceLabel.setText(text); }

  public void highlightCurrentPlayer(Player playerToHighlight) {
    if (playerToHighlight == null || gameModel.getPlayers() == null) return;
    for (int i = 0; i < gameModel.getPlayers().size(); i++) {
      Node node = playerStatusPane.getChildren().get(i); // Assumes order matches
      if (gameModel.getPlayers().get(i).equals(playerToHighlight)) {
        node.setStyle("-fx-background-color: #FFE7B3; -fx-background-radius: 6px; -fx-border-color: #E5B85C; -fx-border-width: 2px; -fx-border-radius: 6px; -fx-padding: 5px;"); // Adjusted padding
      } else {
        node.setStyle("-fx-background-color: rgba(255, 249, 224, 0.8); -fx-background-radius: 6px; -fx-padding: 6px;"); // Reset to default player box style
      }
    }
  }

  public void setRollButtonEnabled(boolean enabled) {
    if (rollButton != null) rollButton.setDisable(!enabled);
  }

  public void displayGameOver(Player winner) {
    updateDiceLabel(winner != null ? "🏆" : "🏁");
    setRollButtonEnabled(false);
    hideSchrodingerChoice();
    String M = winner != null ? winner.getName() + " wins the journey!" : "The game has ended!";
    showGameMessage(M);
    if (winner != null) highlightCurrentPlayer(winner); // Highlight the winner
  }

  //Schrödinger UI Methods
  public void showSchrodingerChoice(Player player, SchrodingerBoxAction action) {
    if (schrodingerChoiceBox != null) {
      // Find the label within schrodingerChoiceBox to update text, or add one if needed
      Node firstChild = schrodingerChoiceBox.getChildren().getFirst();
      if (firstChild instanceof Label choiceLabel) {
        choiceLabel.setText(player.getName() + ", " + action.getDescription());
      } else {
        Label tempLabel = new Label(player.getName() + ", " + action.getDescription());
        tempLabel.setWrapText(true);
        tempLabel.setTextAlignment(javafx.scene.text.TextAlignment.CENTER);
        schrodingerChoiceBox.getChildren().addFirst(tempLabel);
      }

      schrodingerChoiceBox.setManaged(true);
      schrodingerChoiceBox.setVisible(true);
      rollButton.setDisable(true); // Make sure roll is disabled
      showGameMessage(player.getName() + " faces a choice..."); // Update main message too
    }
  }

  public void hideSchrodingerChoice() {
    if (schrodingerChoiceBox != null) {
      schrodingerChoiceBox.setManaged(false);
      schrodingerChoiceBox.setVisible(false);
    }
  }

  public void showGameMessage(String message) {
    if (gameMessageLabel != null) {
      gameMessageLabel.setText(message);
    }
  }
}