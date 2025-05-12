package edu.ntnu.idi.bidata.ui;

import edu.ntnu.idi.bidata.controller.GameController; // Import GameController
import javafx.application.Application;
import javafx.stage.Stage;
import javafx.scene.Scene;
import edu.ntnu.idi.bidata.app.GameVariant;
import edu.ntnu.idi.bidata.factory.GameFactory;
import edu.ntnu.idi.bidata.model.BoardGame;

import java.util.ArrayList; // For initial player names if needed
import java.util.List;

/**
 * Entry point for the board-game application.
 * Handles scene transitions and game flow via SceneManager.
 */
public class MainApp extends Application {
    private Stage primaryStage;
    private GameVariant selectedVariant;
    private List<String> lastPlayerNames = new ArrayList<>(); // Initialize to avoid null
    private SceneManager sceneManager; // Store SceneManager instance

    // Store controllers to keep them alive with the game session if needed,
    // or re-create them each time a game scene is shown (as done below).
    // For simplicity, we re-create them here.
    // private GameController activeGameController;

    @Override
    public void start(Stage primaryStage) {
        this.primaryStage = primaryStage;
        this.sceneManager = SceneManager.getInstance(); // Get instance
        this.sceneManager.initialize(primaryStage); // Initialize

        // Define New Game action
        Runnable newGameAction = () -> {
            if (selectedVariant == null || lastPlayerNames.isEmpty()) {
                // Not enough info to start a new game, go to selection or setup
                System.out.println("New Game requested but variant or players not set. Returning to selection.");
                sceneManager.show("selection");
                return;
            }
            String gameSceneKey = (selectedVariant == GameVariant.SNAKES_LADDERS) ? "slGame" : "monoGame";
            // Clearing and showing will re-trigger the scene supplier, creating new game model & controller
            sceneManager.clear(gameSceneKey);
            sceneManager.show(gameSceneKey);
        };

        // Define Home action
        Runnable homeAction = () -> {
            sceneManager.clear("slGame");
            sceneManager.clear("monoGame");
            selectedVariant = null;
            lastPlayerNames.clear();
            // activeGameController = null; // If storing controller instance
            sceneManager.show("selection");
        };

        // 1) Selection scene registration
        sceneManager.register("selection", () ->
                new SelectionScene(
                        primaryStage,
                        variant -> {
                            this.selectedVariant = variant;
                            sceneManager.show(getSetupKey(variant));
                        }
                ).getScene()
        );

        // 2) Snake & Ladder setup scene
        sceneManager.register("slSetup", () ->
                new SnakeLadderPlayerSetupScene(
                        primaryStage,
                        names -> {
                            this.lastPlayerNames = names;
                            sceneManager.show("slGame"); // This will now create controller & model
                        },
                        homeAction
                ).getScene()
        );

        // 3) Monopoly setup scene
        sceneManager.register("monoSetup", () ->
                new MonopolyPlayerSetupScene(
                        primaryStage,
                        names -> {
                            this.lastPlayerNames = names;
                            sceneManager.show("monoGame"); // This will now create controller & model
                        },
                        homeAction
                ).getScene()
        );

        // 4a) Snakes & Ladders game scene
        sceneManager.register("slGame", () -> {
            if (lastPlayerNames == null || lastPlayerNames.isEmpty()) {
                System.err.println("Error: Attempting to start S&L game without player names. Returning to setup.");
                // Fallback: redirect to setup or selection if player names are missing
                // This might happen if the app state is inconsistent.
                // For a robust app, you'd handle this more gracefully, e.g., disable "start game" buttons
                // until player names are set.
                return null;
//                return sceneManager.getCachedOrLoad("slSetup"); // Try to get setup scene or load it.
                // `getCachedOrLoad` would be a new method in SceneManager
                // or simply redirect to selection.
                // For now, let's assume this path is guarded by UI flow.
            }
            BoardGame gameModel = GameFactory.createGame(lastPlayerNames, GameVariant.SNAKES_LADDERS);
            GameController controller = new GameController(gameModel); // Create Controller

            GameScene gameScene = new GameScene(
                    primaryStage,
                    controller,    // Pass controller
                    gameModel,     // Pass model
                    newGameAction,
                    homeAction
            );
            // activeGameController = controller; // If you need to store it
            controller.setActiveView(gameScene); // IMPORTANT: Link controller to its view
            // gameScene.initializeView(); // Controller's startGame will trigger onGameStart which calls initializeView.
            controller.startGame(); // Controller tells model to init, which fires onGameStart
            return gameScene.getScene();
        });

        // 4b) Monopoly game scene
        sceneManager.register("monoGame", () -> {
            if (lastPlayerNames == null || lastPlayerNames.isEmpty()) {
                System.err.println("Error: Attempting to start Monopoly game without player names.");
                // Similar fallback as above
//                return sceneManager.getCachedOrLoad("monoSetup");
                return null; // or redirect to setup
            }
            BoardGame gameModel = GameFactory.createGame(lastPlayerNames, GameVariant.MINI_MONOPOLY);
            GameController controller = new GameController(gameModel); // Create Controller

            MonopolyGameScene monopolyGameScene = new MonopolyGameScene(
                    primaryStage,
                    controller,    // Pass controller
                    gameModel,     // Pass model
                    newGameAction,
                    homeAction
            );
            // activeGameController = controller; // If storing
            controller.setActiveView(monopolyGameScene); // IMPORTANT
            // monopolyGameScene.initializeView(); // Controller's startGame will trigger onGameStart
            controller.startGame(); // Controller tells model to init
            return monopolyGameScene.getScene();
        });

        // Kick off initial scene
        sceneManager.show("selection");
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
        // Initialize ServiceLocator if it needs explicit setup and isn't self-initializing
        // e.g., ServiceLocator.configure(new DefaultMonopolyService(), new DefaultSnakesLaddersService());
        launch(args);
    }
}