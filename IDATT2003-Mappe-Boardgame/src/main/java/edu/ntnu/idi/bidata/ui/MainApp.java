package edu.ntnu.idi.bidata.ui;

import edu.ntnu.idi.bidata.controller.GameController;
import javafx.application.Application;
import javafx.stage.Stage;
// import javafx.scene.Scene; // No longer needed for registration
import edu.ntnu.idi.bidata.app.GameVariant;
import edu.ntnu.idi.bidata.factory.GameFactory;
import edu.ntnu.idi.bidata.model.BoardGame;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer; // For S&L setup
import java.util.function.Consumer;  // For Monopoly setup

/**
 * Entry point for the board-game application.
 * Handles scene transitions and game flow via SceneManager (updated).
 */
public class MainApp extends Application {
    private Stage primaryStage;
    private GameVariant selectedVariant;
    private List<String> lastPlayerNames = new ArrayList<>();
    private SceneManager sceneManager;

    @Override
    public void start(Stage primaryStage) {
        this.primaryStage = primaryStage;
        this.sceneManager = SceneManager.getInstance();
        this.sceneManager.initialize(primaryStage);

        Runnable newGameAction = () -> {
            if (selectedVariant == null || lastPlayerNames.isEmpty()) {
                System.out.println("New Game requested but variant or players not set. Returning to selection.");
                sceneManager.show("selection");
                return;
            }
            String gameSceneKey = (selectedVariant == GameVariant.SNAKES_LADDERS) ? "slGame" : "monoGame";
            sceneManager.clear(gameSceneKey);
            sceneManager.show(gameSceneKey);
        };

        Runnable homeAction = () -> {
            sceneManager.clear("slGame");
            sceneManager.clear("monoGame");
            selectedVariant = null;
            lastPlayerNames.clear();
            sceneManager.show("selection");
        };

        // 1) Selection scene registration
        // MODIFIED: Register Supplier<ControlledScene>
        // Make sure SelectionScene implements SceneManager.ControlledScene
        sceneManager.register("selection", () ->
            new SelectionScene(
                primaryStage,
                variant -> {
                    this.selectedVariant = variant;
                    sceneManager.show(getSetupKey(variant));
                }
            ) // No .getScene() here
        );

        // 2) Snake & Ladder setup scene
        // MODIFIED: Register Supplier<ControlledScene> and use BiConsumer
        sceneManager.register("slSetup", () -> {
            // Define the BiConsumer for onStart
            BiConsumer<List<String>, SnakeLadderPlayerSetupScene.Theme> onStartSl = (names, theme) -> {
                this.lastPlayerNames = names;
                System.out.println("Starting S&L game. Players: " + names + ". Selected Theme: " + theme);
                // TODO: Potentially use 'theme' to influence GameFactory or board loading for slGame
                // For now, the theme is just logged. GameFactory.createGame is called without theme.
                sceneManager.show("slGame");
            };
            return new SnakeLadderPlayerSetupScene(
                primaryStage,
                onStartSl, // Pass the BiConsumer
                homeAction
            ); // No .getScene() here
        });


        // 3) Monopoly setup scene
        // MODIFIED: Register Supplier<ControlledScene>
        // Make sure MonopolyPlayerSetupScene implements SceneManager.ControlledScene
        sceneManager.register("monoSetup", () -> {
            Consumer<List<String>> onStartMono = names -> {
                this.lastPlayerNames = names;
                sceneManager.show("monoGame");
            };
            return new MonopolyPlayerSetupScene(
                primaryStage,
                onStartMono,
                homeAction
            ); // No .getScene() here
        });


        // 4a) Snakes & Ladders game scene
        // MODIFIED: Register Supplier<ControlledScene>
        // Make sure GameScene implements SceneManager.ControlledScene
        sceneManager.register("slGame", () -> {
            if (lastPlayerNames == null || lastPlayerNames.isEmpty()) {
                System.err.println("Error: Attempting to start S&L game without player names.");
                // Redirect instead of returning null, which SceneManager now disallows from supplier
                sceneManager.show("selection"); // Or slSetup
                throw new IllegalStateException("Cannot create S&L game scene without player names. Redirecting.");
            }
            // GameFactory.createGame does not currently accept a theme.
            // If it did, you'd pass the theme from the 'slSetup' onStart here.
            BoardGame gameModel = GameFactory.createGame(lastPlayerNames, GameVariant.SNAKES_LADDERS);
            GameController controller = new GameController(gameModel);

            GameScene gameScene = new GameScene(
                primaryStage,
                controller,
                gameModel,
                newGameAction,
                homeAction
            );
            controller.setActiveView(gameScene); // Assumes GameController has this method
            gameScene.initializeView(); // Initialize the view components
            controller.startGame();
            return gameScene; // No .getScene() here
        });

        // 4b) Monopoly game scene
        // MODIFIED: Register Supplier<ControlledScene>
        // Make sure MonopolyGameScene implements SceneManager.ControlledScene
        sceneManager.register("monoGame", () -> {
            if (lastPlayerNames == null || lastPlayerNames.isEmpty()) {
                System.err.println("Error: Attempting to start Monopoly game without player names.");
                sceneManager.show("selection"); // Or monoSetup
                throw new IllegalStateException("Cannot create Monopoly game scene without player names. Redirecting.");
            }
            BoardGame gameModel = GameFactory.createGame(lastPlayerNames, GameVariant.MINI_MONOPOLY);
            GameController controller = new GameController(gameModel);

            MonopolyGameScene monopolyGameScene = new MonopolyGameScene(
                primaryStage,
                controller,
                gameModel,
                newGameAction,
                homeAction
            );
            controller.setActiveView(monopolyGameScene); // Assumes GameController has this method
            monopolyGameScene.initializeView(); // Initialize the view components
            controller.startGame();
            return monopolyGameScene; // No .getScene() here
        });

        sceneManager.show("selection");
        primaryStage.setTitle("Board Game Suite");
        primaryStage.show();
    }

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