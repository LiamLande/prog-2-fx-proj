package edu.ntnu.idi.bidata.ui;

import edu.ntnu.idi.bidata.controller.GameController;
import edu.ntnu.idi.bidata.model.BoardGame;
import edu.ntnu.idi.bidata.model.Player;
import javafx.beans.value.ChangeListener;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Group;
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
import javafx.stage.Stage;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Main game view: board + side panel, responsive to window size and custom background.
 * No longer sets the stage directly; managed by SceneManager.
 */
public class GameScene implements SceneManager.ControlledScene {
  private final GameController controller;
  private final BoardView boardView;
  private VBox playerStatusPane;
  private final Map<Player, Label> playerPositionLabels = new HashMap<>();
  private final Map<Player, Circle> playerTokens = new HashMap<>();
  private final Scene scene;
  private Player currentPlayer;
  private Label diceLabel;
  private Button rollButton;
  private int lastRoll;

  public GameScene(Stage stage,
      BoardGame game,
      Runnable onNewGame,
      Runnable onHome) {
    // Initialize controller and first player
    controller = new GameController(game);
    currentPlayer = game.getPlayers().getFirst();

    // Root layout with custom background
    BorderPane root = new BorderPane();
    Image bgImg = new Image(
        getClass().getClassLoader().getResourceAsStream("images/sl_game_background.jpg")
    );
    BackgroundSize bgSize = new BackgroundSize(1.0, 1.0, true, true, false, true);
    BackgroundImage bgImage = new BackgroundImage(bgImg,
        BackgroundRepeat.NO_REPEAT, BackgroundRepeat.NO_REPEAT,
        BackgroundPosition.CENTER, bgSize);
    root.setBackground(new Background(bgImage));

    // Board view and container
    BoardView bv = new BoardView(game);
    this.boardView = bv;
    StackPane boardContainer = createBoardContainer(bv);

    // Side panel for title, status, controls
    VBox sidePanel = createSidePanel(game.getPlayers(), onNewGame, onHome);

    // Assemble layout
    root.setCenter(boardContainer);
    root.setRight(sidePanel);
    root.setPadding(new Insets(20));

    // Create scene (but do not set on stage)
    scene = new Scene(root, 1100, 800);

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
        if (controller.getGame().isFinished()) return;
        int idx = players.indexOf(currentPlayer);
        currentPlayer = players.get((idx + 1) % players.size());
        highlightCurrentPlayer();
      }

      @Override
      public void onGameOver(Player winner) {
        updateDiceLabel("🏆");
        updatePlayerPositions();
        rollButton.setDisable(true);
        // Reset styles
        for (Player p : controller.getGame().getPlayers()) {
          HBox box = (HBox) playerStatusPane.getChildren()
              .get(controller.getGame().getPlayers().indexOf(p));
          box.setStyle("-fx-background-color: #F5EBDA; -fx-background-radius: 10px; -fx-padding: 10px;");
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

  /** Returns the root Parent for SceneManager swapping. */
  public Parent getRoot() { return scene.getRoot(); }

  /** Returns the Scene instance for sizing and styling. */
  public Scene getScene() { return scene; }

  /** Starts the game by initializing model and firing first event. */
  public void start() { controller.startGame(); }

  @Override public void onShow() { }
  @Override public void onHide() { }

  /** Wraps the BoardView in a responsive StackPane with border. */
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
    titleBox.setStyle("-fx-background-color: #F5EBDA; -fx-background-radius: 10px;");
    Label titleLabel = new Label("Ancient Journey");
    titleLabel.setFont(Font.font("Serif", FontWeight.BOLD, 32));
    titleLabel.setTextFill(Color.web("#5A3A22"));
    Label subtitleLabel = new Label("A Game of Destiny and Fortune");
    subtitleLabel.setFont(Font.font("Serif", FontWeight.NORMAL, 16));
    subtitleLabel.setTextFill(Color.web("#5A3A22"));
    titleBox.getChildren().addAll(titleLabel, subtitleLabel);

    // Player status
    playerStatusPane = new VBox(10);
    playerStatusPane.setAlignment(Pos.CENTER);
    createPlayerStatusBoxes(players);
    VBox playerSection = new VBox(15, playerStatusPane);
    playerSection.setAlignment(Pos.TOP_CENTER);
    playerSection.setPadding(new Insets(20));
    playerSection.setStyle("-fx-background-color: #F5EBDA; -fx-background-radius: 10px;");

    // Dice controls
    diceLabel = new Label("⚄");
    diceLabel.setFont(Font.font("Serif", FontWeight.BOLD, 48));
    diceLabel.setTextFill(Color.web("#5A3A22"));
    rollButton = new Button("Roll Dice");
    styleButton(rollButton);
    rollButton.setOnAction(e -> controller.playTurn(currentPlayer));
    rollButton.setPrefWidth(150);
    VBox diceBox = new VBox(10, diceLabel, rollButton);
    diceBox.setAlignment(Pos.CENTER);
    diceBox.setPadding(new Insets(20));
    diceBox.setStyle("-fx-background-color: #F5EBDA; -fx-background-radius: 10px;");

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
    Color[] colors = { Color.RED, Color.BLUE, Color.GREEN, Color.PURPLE };
    for (int i = 0; i < players.size(); i++) {
      Player p = players.get(i);
      HBox box = new HBox(15);
      box.setAlignment(Pos.CENTER_LEFT);
      box.setPadding(new Insets(10));
      box.setStyle("-fx-background-color: #F5EBDA; -fx-background-radius: 10px;");
      Circle tok = new Circle(15, colors[i % colors.length]);
      tok.setStroke(Color.BLACK);
      playerTokens.put(p, tok);
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

  /** Applies consistent styling and hover effects to buttons. */
  private void styleButton(Button btn) {
    btn.setFont(Font.font("Serif", FontWeight.BOLD, 16));
    btn.setStyle("-fx-background-color: #E5B85C; -fx-text-fill: #5A3A22; -fx-background-radius: 5px; -fx-cursor: hand;");
    btn.setOnMouseEntered(e -> btn.setStyle("-fx-background-color: #D4A74A; -fx-text-fill: #5A3A22; -fx-background-radius: 5px; -fx-cursor: hand;"));
    btn.setOnMouseExited(e -> btn.setStyle("-fx-background-color: #E5B85C; -fx-text-fill: #5A3A22; -fx-background-radius: 5px; -fx-cursor: hand;"));
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
      box.setStyle("-fx-background-color: #F5EBDA; -fx-background-radius: 10px; -fx-padding: 10px;");
    }
    int idx = players.indexOf(currentPlayer);
    HBox curr = (HBox) playerStatusPane.getChildren().get(idx);
    curr.setStyle("-fx-background-color: #FFE7B3; -fx-background-radius: 10px; -fx-border-color: #E5B85C; -fx-border-width: 2px; -fx-border-radius: 10px; -fx-padding: 10px;");
  }
}
