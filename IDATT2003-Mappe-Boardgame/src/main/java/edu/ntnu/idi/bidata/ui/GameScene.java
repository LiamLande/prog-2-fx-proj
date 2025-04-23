package edu.ntnu.idi.bidata.ui;

import edu.ntnu.idi.bidata.controller.GameController;
import edu.ntnu.idi.bidata.model.BoardGame;
import edu.ntnu.idi.bidata.model.Player;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

import java.util.List;

/**
 * Assembles BoardView and ControlPanel into the main Game scene.
 */
public class GameScene {
  private final GameController controller;
  private final BoardView boardView;
  private final ControlPanel controlPanel;
  private final Scene scene;

  public GameScene(Stage stage, BoardGame game) {
    // Initialize controller and views
    this.controller = new GameController(game);
    this.boardView = new BoardView(game);
    this.controlPanel = new ControlPanel();

    // Register listener with correctly-typed methods
    controller.addListener(new GameController.GameListener() {
      @Override
      public void onGameStart(List<Player> players) {
        controlPanel.setStatus("Game started. " + players.getFirst().getName() + "'s turn.");
      }

      @Override
      public void onRoundPlayed(List<Integer> rolls, List<Player> players) {
        boardView.refresh();
        controlPanel.setStatus("Rolls: " + rolls + " - Next: " + players.getFirst().getName());
      }

      @Override
      public void onGameOver(Player winner) {
        controlPanel.setStatus("Game Over! Winner: " + winner.getName());
        controlPanel.getRollButton().setDisable(true);
      }
    });

    // Wire button to controller
    controlPanel.getRollButton().setOnAction(e -> controller.playOneRound());

    // Layout
    BorderPane root = new BorderPane();
    root.setCenter(boardView);
    root.setRight(controlPanel);

    // Build scene
    this.scene = new Scene(root, 800, 600);
    UiStyles.apply(scene);
    stage.setScene(scene);
    stage.setTitle("Snakes & Ladders");
  }

  /**
   * Starts the game logic.
   */
  public void start() {
    controller.startGame();
  }

  /**
   * Returns the JavaFX Scene for display.
   */
  public Scene getScene() {
    return scene;
  }
}
