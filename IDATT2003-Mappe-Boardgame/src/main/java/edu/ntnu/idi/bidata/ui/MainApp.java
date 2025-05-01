package edu.ntnu.idi.bidata.ui;

import javafx.application.Application;
import javafx.stage.Stage;
import edu.ntnu.idi.bidata.app.GameVariant;
import edu.ntnu.idi.bidata.factory.GameFactory;
import edu.ntnu.idi.bidata.model.BoardGame;
import javafx.scene.Scene;

import java.util.List;

/**
 * Entry point for the board-game application.
 * Handles scene transitions and game flow via SceneManager.
 */
public class MainApp extends Application {
  private Stage primaryStage;
  private GameVariant selectedVariant;
  private List<String> lastPlayerNames;

  @Override
  public void start(Stage primaryStage) {
    this.primaryStage = primaryStage;

    // Initialize SceneManager
    SceneManager mgr = SceneManager.getInstance();
    mgr.initialize(primaryStage);

    // Define New Game action to clear and rebuild the game scene
    Runnable newGameAction = () -> {
      mgr.clear("game");        // Remove old cached GameScene
      mgr.show("game");         // Rebuild and show a fresh GameScene
    };

    // Define Home action to reset game state and return to selection screen
    Runnable homeAction = () -> {
      // Clear game scene cache
      mgr.clear("game");
      // Reset selected variant and player list
      selectedVariant = null;
      lastPlayerNames = null;
      // Show selection scene
      mgr.show("selection");
    };

    // 1) Selection scene registration
    mgr.register("selection", () ->
        new SelectionScene(
            primaryStage,
            variant -> {
              this.selectedVariant = variant;
              mgr.show(getSetupKey(variant));
            }
        ).getScene()
    );

    // 2) Snake & Ladder setup scene
    mgr.register("slSetup", () ->
        new SnakeLadderPlayerSetupScene(
            primaryStage,
            names -> {
              this.lastPlayerNames = names;
              mgr.show("game");
            },
            homeAction
        ).getScene()
    );

    // 3) Monopoly setup scene
    mgr.register("monoSetup", () ->
        new MonopolyPlayerSetupScene(
            primaryStage,
            names -> {
              this.lastPlayerNames = names;
              mgr.show("game");
            },
            homeAction
        ).getScene()
    );

    // 4) Game scene registration with newGameAction and homeAction
    mgr.register("game", () -> {
      BoardGame game = GameFactory.createGame(lastPlayerNames, selectedVariant);
      GameScene gs = new GameScene(
          primaryStage,
          game,
          newGameAction,          // Restart current game
          homeAction              // Reset to home and clear state
      );
      gs.start();
      return gs.getScene();
    });

    // Show initial scene
    mgr.show("selection");
    primaryStage.setTitle("Board Game Suite");
    primaryStage.show();
  }

  /** Maps variant identifiers to their respective setup scene keys. */
  private String getSetupKey(GameVariant variant) {
    switch (variant) {
      case SNAKES_LADDERS: return "slSetup";
      case MINI_MONOPOLY:  return "monoSetup";
      default: throw new IllegalArgumentException("Unknown variant: " + variant);
    }
  }

  public static void main(String[] args) {
    launch(args);
  }
}
