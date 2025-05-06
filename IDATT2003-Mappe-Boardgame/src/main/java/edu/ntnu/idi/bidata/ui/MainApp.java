package edu.ntnu.idi.bidata.ui;

import javafx.application.Application;
import javafx.stage.Stage;
import javafx.scene.Scene;
import edu.ntnu.idi.bidata.app.GameVariant;
import edu.ntnu.idi.bidata.factory.GameFactory;
import edu.ntnu.idi.bidata.model.BoardGame;

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

        // Define New Game action to clear and rebuild the appropriate game scene
        Runnable newGameAction = () -> {
            String key = (selectedVariant == GameVariant.SNAKES_LADDERS) ? "slGame" : "monoGame";
            mgr.clear(key);
            mgr.show(key);
        };

        // Define Home action to reset state and return to selection screen
        Runnable homeAction = () -> {
            // Clear both possible game scenes
            mgr.clear("slGame");
            mgr.clear("monoGame");
            selectedVariant = null;
            lastPlayerNames = null;
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
                            mgr.show("slGame");
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
                            mgr.show("monoGame");
                        },
                        homeAction
                ).getScene()
        );

        // 4a) Snakes & Ladders game scene
        mgr.register("slGame", () -> {
            BoardGame game = GameFactory.createGame(lastPlayerNames, GameVariant.SNAKES_LADDERS);
            GameScene gs = new GameScene(
                    primaryStage,
                    game,
                    newGameAction,
                    homeAction
            );
            gs.start();
            return gs.getScene();
        });

        // 4b) Monopoly game scene
        mgr.register("monoGame", () -> {
            BoardGame game = GameFactory.createGame(lastPlayerNames, GameVariant.MINI_MONOPOLY);
            MonopolyGameScene mgs = new MonopolyGameScene(
                    primaryStage,
                    game,
                    newGameAction,
                    homeAction
            );
            mgs.start();
            return mgs.getScene();
        });

        // Kick off initial scene
        mgr.show("selection");
        primaryStage.setTitle("Board Game Suite");
        primaryStage.show();
    }

    /** Maps variant identifiers to their respective setup scene keys. */
    private String getSetupKey(GameVariant variant) {
        switch (variant) {
            case SNAKES_LADDERS: return "slSetup";
            case MINI_MONOPOLY:  return "monoSetup";
            default:
                throw new IllegalArgumentException("Unknown variant: " + variant);
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
