package edu.ntnu.idi.bidata.ui;

import edu.ntnu.idi.bidata.controller.GameController;
import edu.ntnu.idi.bidata.model.BoardGame;
import edu.ntnu.idi.bidata.model.Player;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GameScene {
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

  public GameScene(Stage stage, BoardGame game, Runnable onNewGame, Runnable onHome) {
    controller = new GameController(game);
    currentPlayer = game.getPlayers().getFirst();

    // Create main layout with soft teal background
    BorderPane root = new BorderPane();
    root.setStyle("-fx-background-color: linear-gradient(to bottom right, #8BBFB3, #C3D9B2);");

    // Create the board with a golden frame
    boardView = createStyledBoardView(game);

    // Create the side panel
    VBox sidePanel = createSidePanel(game.getPlayers(), onNewGame, onHome);

    // Add components to root
    root.setCenter(boardView);
    root.setRight(sidePanel);

    // Set padding around the entire UI
    root.setPadding(new Insets(20));

    // Create scene
    scene = new Scene(root, 1100, 800);
    stage.setScene(scene);
    stage.setTitle("Ancient Journey");

    // Observer wiring
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
        // Get the last roll
        if (!rolls.isEmpty()) {
          lastRoll = rolls.getFirst();
          updateDiceLabel(String.valueOf(lastRoll));
        }

        boardView.refresh();
        updatePlayerPositions();

        // Check if game is over before moving to next player
        if (controller.getGame().isFinished()) {
          return;
        }

        // Move to next player
        int currentIndex = players.indexOf(currentPlayer);
        currentIndex = (currentIndex + 1) % players.size();
        currentPlayer = players.get(currentIndex);

        highlightCurrentPlayer();
      }

      @Override
      public void onGameOver(Player winner) {
        updateDiceLabel("🏆");
        updatePlayerPositions();
        rollButton.setDisable(true);

        // Remove highlight from all players
        for (Player p : controller.getGame().getPlayers()) {
          HBox playerBox = (HBox) playerStatusPane.getChildren().get(controller.getGame().getPlayers().indexOf(p));
          playerBox.setStyle("-fx-background-color: #F5EBDA; -fx-background-radius: 10px; -fx-padding: 10px;");
        }

        // Highlight winner
        HBox winnerBox = (HBox) playerStatusPane.getChildren().get(controller.getGame().getPlayers().indexOf(winner));
        winnerBox.setStyle("-fx-background-color: #FFD700; -fx-background-radius: 10px; -fx-padding: 10px;");

        // Also highlight the winner's token
        Circle winnerToken = playerTokens.get(winner);
        if (winnerToken != null) {
          winnerToken.setStroke(Color.GOLD);
          winnerToken.setStrokeWidth(3);
        }
      }
    });
  }

  private BoardView createStyledBoardView(BoardGame game) {
    // Create board view with customized styling
    BoardView board = new BoardView(game);

    // Apply styling to the board
    board.setStyle("-fx-background-color: #F5EBDA; -fx-background-radius: 10px; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.3), 10, 0, 0, 0);");

    // Create container with gold border
    StackPane boardContainer = new StackPane();

    // Gold border
    Region border = new Region();
    border.setStyle("-fx-background-color: #E5B85C; -fx-background-radius: 15px; -fx-padding: 15px;");

    boardContainer.getChildren().addAll(border, board);

    // Add some padding between border and board
    StackPane.setMargin(board, new Insets(10));

    return board;
  }

  private VBox createSidePanel(List<Player> players, Runnable onNewGame, Runnable onHome) {
    VBox sidePanel = new VBox(20);
    sidePanel.setAlignment(Pos.TOP_CENTER);
    sidePanel.setPadding(new Insets(0, 0, 0, 20));
    sidePanel.setPrefWidth(300);

    // Game title
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

    // Player status section
    VBox playerSection = new VBox(15);
    playerSection.setAlignment(Pos.TOP_CENTER);
    playerSection.setPadding(new Insets(20));
    playerSection.setStyle("-fx-background-color: #F5EBDA; -fx-background-radius: 10px;");

    playerStatusPane = new VBox(10);
    playerStatusPane.setAlignment(Pos.CENTER);

    // Create player info displays
    createPlayerStatusBoxes(players);

    playerSection.getChildren().add(playerStatusPane);

    // Dice display
    VBox diceBox = new VBox(10);
    diceBox.setAlignment(Pos.CENTER);
    diceBox.setPadding(new Insets(20));
    diceBox.setStyle("-fx-background-color: #F5EBDA; -fx-background-radius: 10px;");

    diceLabel = new Label("⚄");
    diceLabel.setFont(Font.font("Serif", FontWeight.BOLD, 48));
    diceLabel.setTextFill(Color.web("#5A3A22"));

    rollButton = new Button("Roll Dice");
    styleButton(rollButton);
    rollButton.setOnAction(e -> {
      // Move only the current player
      controller.playTurn(currentPlayer);
    });
    rollButton.setPrefWidth(150);

    diceBox.getChildren().addAll(diceLabel, rollButton);

    // Game controls
    VBox controlsBox = new VBox(15);
    controlsBox.setAlignment(Pos.CENTER);

    // New Game button
    Button newGameBtn = new Button("New Game");
    styleButton(newGameBtn);
    newGameBtn.setOnAction(e -> onNewGame.run());
    newGameBtn.setPrefWidth(200);

    // Home button
    Button homeBtn = new Button("Return Home");
    styleButton(homeBtn);
    homeBtn.setOnAction(e -> onHome.run());
    homeBtn.setPrefWidth(200);

    controlsBox.getChildren().addAll(newGameBtn, homeBtn);

    sidePanel.getChildren().addAll(titleBox, playerSection, diceBox, controlsBox);

    return sidePanel;
  }

  private void createPlayerStatusBoxes(List<Player> players) {
    playerStatusPane.getChildren().clear();
    playerPositionLabels.clear();
    playerTokens.clear();

    // Define player colors - can add more if needed
    Color[] playerColors = {
        Color.RED, Color.BLUE, Color.GREEN, Color.PURPLE
    };

    for (int i = 0; i < players.size(); i++) {
      Player player = players.get(i);

      HBox playerBox = new HBox(15);
      playerBox.setAlignment(Pos.CENTER_LEFT);
      playerBox.setPadding(new Insets(10));
      playerBox.setStyle("-fx-background-color: #F5EBDA; -fx-background-radius: 10px;");

      // Player token/icon
      Circle playerToken = new Circle(15);
      playerToken.setFill(playerColors[i % playerColors.length]);
      playerToken.setStroke(Color.BLACK);
      playerToken.setStrokeWidth(1);
      playerTokens.put(player, playerToken);

      // Player info
      VBox playerInfo = new VBox(5);
      playerInfo.setAlignment(Pos.CENTER_LEFT);

      Label nameLabel = new Label(player.getName());
      nameLabel.setFont(Font.font("Serif", FontWeight.BOLD, 16));
      nameLabel.setTextFill(Color.web("#5A3A22"));

      Label positionLabel = new Label(String.valueOf(player.getCurrent().getId()));
      positionLabel.setFont(Font.font("Serif", FontWeight.NORMAL, 24));
      positionLabel.setTextFill(Color.web("#5A3A22"));
      playerPositionLabels.put(player, positionLabel);

      playerInfo.getChildren().addAll(nameLabel, positionLabel);
      playerBox.getChildren().addAll(playerToken, playerInfo);

      playerStatusPane.getChildren().add(playerBox);
    }
  }

  private void styleButton(Button button) {
    button.setFont(Font.font("Serif", FontWeight.BOLD, 16));
    button.setStyle("-fx-background-color: #E5B85C; -fx-text-fill: #5A3A22; " +
        "-fx-background-radius: 5px; -fx-cursor: hand;");

    // Add hover effect
    button.setOnMouseEntered(e ->
        button.setStyle("-fx-background-color: #D4A74A; -fx-text-fill: #5A3A22; " +
            "-fx-background-radius: 5px; -fx-cursor: hand;"));

    button.setOnMouseExited(e ->
        button.setStyle("-fx-background-color: #E5B85C; -fx-text-fill: #5A3A22; " +
            "-fx-background-radius: 5px; -fx-cursor: hand;"));
  }

  private void updatePlayerPositions() {
    for (Map.Entry<Player, Label> entry : playerPositionLabels.entrySet()) {
      Player player = entry.getKey();
      Label positionLabel = entry.getValue();
      positionLabel.setText(String.valueOf(player.getCurrent().getId()));
    }
  }

  private void updateDiceLabel(String text) {
    diceLabel.setText(text);
  }

  private void highlightCurrentPlayer() {
    // Reset all player box styles
    for (Player p : controller.getGame().getPlayers()) {
      int index = controller.getGame().getPlayers().indexOf(p);
      if (index < playerStatusPane.getChildren().size()) {
        HBox playerBox = (HBox) playerStatusPane.getChildren().get(index);
        playerBox.setStyle("-fx-background-color: #F5EBDA; -fx-background-radius: 10px; -fx-padding: 10px;");
      }
    }

    // Highlight current player
    int currentIndex = controller.getGame().getPlayers().indexOf(currentPlayer);
    if (currentIndex >= 0 && currentIndex < playerStatusPane.getChildren().size()) {
      HBox currentPlayerBox = (HBox) playerStatusPane.getChildren().get(currentIndex);
      currentPlayerBox.setStyle("-fx-background-color: #FFE7B3; -fx-background-radius: 10px; -fx-border-color: #E5B85C; -fx-border-width: 2px; -fx-border-radius: 10px; -fx-padding: 10px;");
    }
  }

  public void start() {
    controller.startGame();
  }

  public Scene getScene() {
    return scene;
  }
}