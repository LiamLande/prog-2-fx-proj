package edu.ntnu.idi.bidata.app;

import edu.ntnu.idi.bidata.factory.GameFactory;
import edu.ntnu.idi.bidata.model.BoardGame;
import edu.ntnu.idi.bidata.ui.GameScene;
import edu.ntnu.idi.bidata.ui.MonopolyPlayerSetupScene;
import edu.ntnu.idi.bidata.ui.SnakeLadderPlayerSetupScene;
import edu.ntnu.idi.bidata.ui.SelectionScene;
import javafx.application.Application;
import javafx.stage.Stage;

import java.util.ArrayList;
import java.util.List;

/**
 * Main application that launches selection, player setup, and the unified game scene.
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

  private void showPlayerSetup() {
    // reuse a common PlayerSetupScene or themed Monopoly version
    if (selectedVariant == GameVariant.MINI_MONOPOLY) {
      MonopolyPlayerSetupScene ms = new MonopolyPlayerSetupScene(
          primaryStage,
          this::startGame,
          this::showSelection
      );
      primaryStage.setScene(ms.getScene());
    } else {
      SnakeLadderPlayerSetupScene ps = new SnakeLadderPlayerSetupScene(
          primaryStage,
          this::startGame,
          this::showSelection
      );
      primaryStage.setScene(ps.getScene());
    }
    primaryStage.setTitle(selectedVariant + " – Enter Names");
  }

  private void startGame(List<String> playerNames) {
    // remember for resets
    lastPlayerNames = new ArrayList<>(playerNames);

    // 1) build the board & logic based on variant
    BoardGame game = GameFactory.createGame(playerNames, selectedVariant);

    // 2) create the unified GameScene
    GameScene gameScene = new GameScene(
        primaryStage,
        game,
        this::resetGame,   // New Game callback
        this::showSelection // Home callback
    );

    // 3) show & start
    primaryStage.setScene(gameScene.getScene());
    primaryStage.setTitle(selectedVariant + " – Game");
    gameScene.start();
  }

  private void resetGame() {
    if (lastPlayerNames != null) {
      startGame(lastPlayerNames);
    }
  }

  public static void main(String[] args) {
    launch(args);
  }
}
