package edu.ntnu.idi.bidata;

import edu.ntnu.idi.bidata.controller.GameController;
import edu.ntnu.idi.bidata.model.BoardGame;
import edu.ntnu.idi.bidata.model.Player;
import edu.ntnu.idi.bidata.view.GameBoardView;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;

import java.util.ArrayList;
import java.util.List;

/**
 * Main application class for the Snakes and Ladders game
 */
public class Main extends Application {

  private Stage primaryStage;
  private BoardGame game;
  private GameController gameController;
  private List<String> playerNames = new ArrayList<>();

  @Override
  public void start(Stage primaryStage) {
    this.primaryStage = primaryStage;
    primaryStage.setTitle("Snakes and Ladders");

    // Show welcome screen
    showWelcomeScreen();
  }

  /**
   * Show the welcome screen
   */
  private void showWelcomeScreen() {
    BorderPane root = new BorderPane();
    root.setPadding(new Insets(20));
    root.setStyle("-fx-background-color: linear-gradient(to bottom, #87CEFA, #E0F7FA);");

    // Game title
    Label titleLabel = new Label("Snakes & Ladders");
    titleLabel.setFont(Font.font("Arial", FontWeight.BOLD, 36));
    titleLabel.setTextFill(Color.web("#8B4513"));
    titleLabel.setStyle("-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.6), 5, 0, 0, 1);");

    // Center content
    VBox centerContent = new VBox(30);
    centerContent.setAlignment(Pos.CENTER);
    centerContent.getChildren().add(titleLabel);

    // Game description
    Label descLabel = new Label("A classic board game of luck and adventure!");
    descLabel.setFont(Font.font("Arial", 18));
    descLabel.setTextFill(Color.web("#8B4513"));

    // Start button
    Button startButton = new Button("Start New Game");
    startButton.setFont(Font.font("Arial", FontWeight.BOLD, 16));
    startButton.setPrefSize(200, 50);
    startButton.setStyle("-fx-background-color: #8B4513; -fx-text-fill: white;");

    startButton.setOnAction(e -> showPlayerSetupScreen());

    centerContent.getChildren().addAll(descLabel, startButton);

    root.setCenter(centerContent);

    // Create scene
    Scene scene = new Scene(root, 800, 600);
    primaryStage.setScene(scene);
    primaryStage.show();
  }

  /**
   * Show the player setup screen
   */
  private void showPlayerSetupScreen() {
    BorderPane root = new BorderPane();
    root.setPadding(new Insets(20));
    root.setStyle("-fx-background-color: linear-gradient(to bottom, #87CEFA, #E0F7FA);");

    // Title
    Label titleLabel = new Label("Player Setup");
    titleLabel.setFont(Font.font("Arial", FontWeight.BOLD, 24));
    titleLabel.setTextFill(Color.web("#8B4513"));

    // Player input fields
    VBox playerInputs = new VBox(15);
    playerInputs.setAlignment(Pos.CENTER);
    playerInputs.setMaxWidth(400);

    // Instructions
    Label instructionsLabel = new Label("Enter player names (2-4 players)");
    instructionsLabel.setFont(Font.font("Arial", 16));
    instructionsLabel.setTextFill(Color.web("#8B4513"));

    playerInputs.getChildren().add(instructionsLabel);

    // Player input fields
    List<TextField> playerFields = new ArrayList<>();

    for (int i = 0; i < 4; i++) {
      HBox playerRow = new HBox(10);
      playerRow.setAlignment(Pos.CENTER);

      Label playerLabel = new Label("Player " + (i + 1) + ":");
      playerLabel.setFont(Font.font("Arial", 14));
      playerLabel.setTextFill(Color.web("#8B4513"));
      playerLabel.setPrefWidth(80);

      TextField playerField = new TextField();
      playerField.setPromptText("Enter name");
      playerField.setPrefWidth(200);

      if (i < 2) {
        // First two players are required
        playerField.setStyle("-fx-border-color: #8B4513;");
      } else {
        // Other players are optional
        playerLabel.setText(playerLabel.getText() + " (optional)");
      }

      playerRow.getChildren().addAll(playerLabel, playerField);
      playerInputs.getChildren().add(playerRow);
      playerFields.add(playerField);
    }

    // Buttons
    HBox buttons = new HBox(20);
    buttons.setAlignment(Pos.CENTER);

    Button backButton = new Button("Back");
    backButton.setFont(Font.font("Arial", 14));
    backButton.setPrefSize(100, 40);
    backButton.setStyle("-fx-background-color: #B8860B; -fx-text-fill: white;");

    Button startButton = new Button("Start Game");
    startButton.setFont(Font.font("Arial", FontWeight.BOLD, 14));
    startButton.setPrefSize(100, 40);
    startButton.setStyle("-fx-background-color: #8B4513; -fx-text-fill: white;");

    buttons.getChildren().addAll(backButton, startButton);

    // Event handlers
    backButton.setOnAction(e -> showWelcomeScreen());

    startButton.setOnAction(e -> {
      // Validate player names
      playerNames.clear();

      for (int i = 0; i < playerFields.size(); i++) {
        String name = playerFields.get(i).getText().trim();

        if (!name.isEmpty()) {
          playerNames.add(name);
        }
      }

      if (playerNames.size() < 2) {
        // Show error
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Invalid Players");
        alert.setHeaderText("Not enough players");
        alert.setContentText("Please enter at least 2 player names.");
        alert.showAndWait();
        return;
      }

      // Start game
      startGame();
    });

    // Layout
    VBox mainContent = new VBox(30);
    mainContent.setAlignment(Pos.CENTER);
    mainContent.getChildren().addAll(titleLabel, playerInputs, buttons);

    root.setCenter(mainContent);

    // Create scene
    Scene scene = new Scene(root, 800, 600);
    primaryStage.setScene(scene);
  }

  /**
   * Start the game with the entered player names
   */
  private void startGame() {
    // Create game model
    game = new BoardGame();
    game.createBoard();
    game.createDice();

    // Add players
    for (String name : playerNames) {
      game.addPlayer(new Player(name, game));
    }

    // Create game controller
    gameController = new GameController(game);

    // Create game view
    GameBoardView gameView = new GameBoardView(gameController);

    // Create scene with appropriate size for the game board
    Scene scene = new Scene(gameView, 950, 750);
    primaryStage.setScene(scene);
    primaryStage.centerOnScreen();

    // Make sure the window is large enough
    primaryStage.setMinWidth(950);
    primaryStage.setMinHeight(750);

    // Start the game
    gameController.startGame();
  }

  /**
   * Main method
   *
   * @param args Command line arguments
   */
  public static void main(String[] args) {
    launch(args);
  }
}