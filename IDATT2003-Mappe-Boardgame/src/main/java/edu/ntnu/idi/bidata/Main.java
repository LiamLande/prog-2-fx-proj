package edu.ntnu.idi.bidata;

import edu.ntnu.idi.bidata.model.BoardGame;
import edu.ntnu.idi.bidata.model.Player;
import javafx.application.Application;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class Main extends Application {

  private BoardGame game;
  private Label gameStatus;
  private TextField playerNameInput;

  @Override
  public void start(Stage primaryStage) {
    game = new BoardGame();
    game.createBoard();
    game.createDice();

    // UI Elements
    playerNameInput = new TextField();
    playerNameInput.setPromptText("Enter player name");

    Button addPlayerButton = new Button("Add Player");
    addPlayerButton.setOnAction(e -> addPlayer());

    gameStatus = new Label("Game not started. Add players.");

    Button nextRoundButton = new Button("Next Round");
    nextRoundButton.setOnAction(e -> playNextRound());

    // Layout
    VBox root = new VBox(10, playerNameInput, addPlayerButton, gameStatus, nextRoundButton);
    root.setAlignment(Pos.CENTER);

    // Scene
    Scene scene = new Scene(root, 400, 300);
    primaryStage.setTitle("Snakes and Ladders");
    primaryStage.setScene(scene);
    primaryStage.show();
  }
  private void addPlayer() {
    String name = playerNameInput.getText().trim();
    if (!name.isEmpty()) {
      game.addPlayer(new Player(name, game));

      // Build the player list display
      StringBuilder playerList = new StringBuilder("Players: ");
      for (Player p : game.getPlayers()) {
        playerList.append(p.getName()).append(", ");
      }

      // Remove trailing comma and space
      if (!game.getPlayers().isEmpty()) {
        playerList.setLength(playerList.length() - 2);
      }

      gameStatus.setText(playerList.toString());
      playerNameInput.clear();
    } else {
      gameStatus.setText("Enter a valid name.");
    }
  }


  private void playNextRound() {
    game.play();
    gameStatus.setText("Round played! Check console for details.");

    if (game.gameOver()) {
      gameStatus.setText("Game Over! Winner: " + game.getWinner().getName());
    }
  }

  public static void main(String[] args) {
    launch(args);
  }
}
