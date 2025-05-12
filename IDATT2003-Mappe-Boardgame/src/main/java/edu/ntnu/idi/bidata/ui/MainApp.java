package edu.ntnu.idi.bidata.ui;

import edu.ntnu.idi.bidata.controller.GameController;
import javafx.application.Application;
import javafx.stage.Stage;
// import javafx.scene.Scene; // No longer needed for registration with updated SceneManager
import edu.ntnu.idi.bidata.app.GameVariant;
import edu.ntnu.idi.bidata.factory.GameFactory;
import edu.ntnu.idi.bidata.model.BoardGame;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer; // For S&L setup theme handling
import java.util.function.Consumer;  // For Monopoly setup (no theme)
// java.lang.Runnable is implicitly available

/**
 * Entry point for the board-game application.
 * Handles scene transitions and game flow via SceneManager.
 */
public class MainApp extends Application {
    private Stage primaryStage;
    private GameVariant selectedVariant;
    private List<String> lastPlayerNames = new ArrayList<>();
    private SceneManager sceneManager;
    // Store the selected theme for Snakes & Ladders to pass it to GameFactory and GameScene
    private SnakeLadderPlayerSetupScene.Theme currentSnakesLaddersTheme = SnakeLadderPlayerSetupScene.Theme.EGYPT;

    @Override
    public void start(Stage primaryStage) {
        this.primaryStage = primaryStage;
        this.sceneManager = SceneManager.getInstance();
        this.sceneManager.initialize(primaryStage);

        // Define New Game action (called from within a game scene)
        Runnable newGameAction = () -> {
            if (selectedVariant == null || lastPlayerNames.isEmpty()) {
                System.out.println("New Game requested but variant or players not set. Returning to selection.");
                sceneManager.show("selection");
                return;
            }
            String gameSceneKey = (selectedVariant == GameVariant.SNAKES_LADDERS) ? "slGame" : "monoGame";
            sceneManager.clear(gameSceneKey); // Clear cached game scene
            sceneManager.show(gameSceneKey); // Re-create and show
        };

        // Define Home action (called from player setup or game scenes)
        Runnable homeAction = () -> {
            sceneManager.clear("slGame");
            sceneManager.clear("monoGame");
            selectedVariant = null;
            lastPlayerNames.clear();
            currentSnakesLaddersTheme = SnakeLadderPlayerSetupScene.Theme.EGYPT; // Reset theme
            sceneManager.show("selection");
        };

        // 1) Selection scene registration
        // Assumes SelectionScene implements SceneManager.ControlledScene
        sceneManager.register("selection", () ->
            new SelectionScene(
                primaryStage,
                variant -> {
                    this.selectedVariant = variant;
                    sceneManager.show(getSetupKey(variant));
                }
            ) // Returns the ControlledScene instance
        );

        // 2) Snake & Ladder setup scene registration
        // SnakeLadderPlayerSetupScene constructor now takes BiConsumer for onStart
        sceneManager.register("slSetup", () -> {
            BiConsumer<List<String>, SnakeLadderPlayerSetupScene.Theme> onStartSl = (names, theme) -> {
                this.lastPlayerNames = names;
                this.currentSnakesLaddersTheme = theme; // Store the selected theme
                System.out.println("Starting S&L game. Players: " + names + ". Selected Theme: " + theme);
                sceneManager.show("slGame"); // Navigate to the S&L game scene
            };
            return new SnakeLadderPlayerSetupScene(
                primaryStage,
                onStartSl, // Pass the BiConsumer
                homeAction
            ); // Returns the ControlledScene instance
        });


        // 3) Monopoly setup scene registration
        // Assumes MonopolyPlayerSetupScene implements SceneManager.ControlledScene
        sceneManager.register("monoSetup", () -> {
            Consumer<List<String>> onStartMono = names -> { // Monopoly setup doesn't involve themes here
                this.lastPlayerNames = names;
                sceneManager.show("monoGame");
            };
            return new MonopolyPlayerSetupScene(
                primaryStage,
                onStartMono,
                homeAction
            ); // Returns the ControlledScene instance
        });


        // 4a) Snakes & Ladders game scene registration
        // Assumes GameScene implements SceneManager.ControlledScene
        sceneManager.register("slGame", () -> {
            if (lastPlayerNames == null || lastPlayerNames.isEmpty()) {
                System.err.println("Error: Attempting to start S&L game without player names.");
                sceneManager.show("selection"); // Or slSetup
                throw new IllegalStateException("Cannot create S&L game scene without player names. Redirecting.");
            }
            // Pass the stored theme to GameFactory
            BoardGame gameModel = GameFactory.createGame(lastPlayerNames, GameVariant.SNAKES_LADDERS, this.currentSnakesLaddersTheme);
            GameController controller = new GameController(gameModel);

            // Pass the theme to GameScene constructor
            GameScene gameScene = new GameScene(
                primaryStage,
                controller,
                gameModel,
                newGameAction,
                homeAction,
                this.currentSnakesLaddersTheme // Pass theme to GameScene
            );
            controller.setActiveView(gameScene); // Use the correct method name
            gameScene.initializeView();
            controller.startGame();
            return gameScene; // Returns the ControlledScene instance
        });

        // 4b) Monopoly game scene registration
        // Assumes MonopolyGameScene implements SceneManager.ControlledScene
        sceneManager.register("monoGame", () -> {
            if (lastPlayerNames == null || lastPlayerNames.isEmpty()) {
                System.err.println("Error: Attempting to start Monopoly game without player names.");
                sceneManager.show("selection"); // Or monoSetup
                throw new IllegalStateException("Cannot create Monopoly game scene without player names. Redirecting.");
            }
            // Monopoly currently doesn't use themes in this setup
            BoardGame gameModel = GameFactory.createGame(lastPlayerNames, GameVariant.MINI_MONOPOLY); // Uses overloaded GameFactory method
            GameController controller = new GameController(gameModel);

            MonopolyGameScene monopolyGameScene = new MonopolyGameScene(
                primaryStage,
                controller,
                gameModel,
                newGameAction,
                homeAction
                // No theme passed to MonopolyGameScene constructor
            );
            controller.setActiveView(monopolyGameScene); // Use the correct method name
            monopolyGameScene.initializeView();
            controller.startGame();
            return monopolyGameScene; // Returns the ControlledScene instance
        });

        // Kick off initial scene
        sceneManager.show("selection");
        primaryStage.setTitle("Board Game Suite");
        primaryStage.show();
    }

    /** Maps variant identifiers to their respective setup scene keys. */
    private String getSetupKey(GameVariant variant) {
      return switch (variant) {
        case SNAKES_LADDERS -> "slSetup";
        case MINI_MONOPOLY -> "monoSetup";
        default -> throw new IllegalArgumentException("Unknown variant: " + variant);
      };
    }

    public static void main(String[] args) {
        launch(args);
    }
}