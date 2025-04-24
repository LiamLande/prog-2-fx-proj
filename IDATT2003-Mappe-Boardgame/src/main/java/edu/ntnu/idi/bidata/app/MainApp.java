// File: MainApp.java
package edu.ntnu.idi.bidata.app;

import edu.ntnu.idi.bidata.factory.GameFactory;
import edu.ntnu.idi.bidata.model.BoardGame;
import edu.ntnu.idi.bidata.ui.GameScene;
import edu.ntnu.idi.bidata.ui.PlayerSetupScene;
import edu.ntnu.idi.bidata.ui.SelectionScene;
import javafx.application.Application;
import javafx.stage.Stage;

import java.util.ArrayList;
import java.util.List;

/**
 * Main application that launches selection, player setup, and game scenes.
 */
public class MainApp extends Application {
  private Stage primaryStage;
  private GameVariant selectedVariant;
  private List<String> lastPlayerNames;

  @Override
  public void start(Stage stage) {
    this.primaryStage = stage;
    showSelection();
    stage.show();
  }

  /**
   * Shows the variant selection screen.
   */
  private void showSelection() {
    SelectionScene selection = new SelectionScene(
        primaryStage,
        variant -> {
          this.selectedVariant = variant;
          showPlayerSetup();
        }
    );
    primaryStage.setScene(selection.getScene());
    primaryStage.setTitle("Select Game Variant");
  }

  /**
   * Shows the styled player-setup screen.
   */
  private void showPlayerSetup() {
    PlayerSetupScene setup = new PlayerSetupScene(
        primaryStage,
        names -> startGame(names),
        this::showSelection
    );
    primaryStage.setScene(setup.getScene());
    primaryStage.setTitle(selectedVariant + " - Enter Names");
  }

  /**
   * Starts or restarts the game with the given player names.
   */
  private void startGame(List<String> playerNames) {
    // Save names for possible reset
    lastPlayerNames = new ArrayList<>(playerNames);

    BoardGame game = GameFactory.createGame(playerNames);
    GameScene gameScene = new GameScene(
        primaryStage,
        game,
        this::resetGame,
        this::showSelection
    );
    primaryStage.setScene(gameScene.getScene());
    primaryStage.setTitle(selectedVariant + " - Game");
    gameScene.start();
  }

  /**
   * Resets the current game using the last entered player names.
   */
  private void resetGame() {
    if (lastPlayerNames != null) {
      startGame(lastPlayerNames);
    }
  }

  public static void main(String[] args) {
    launch(args);
  }
}
