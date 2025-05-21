package edu.ntnu.idi.bidata.ui;

import edu.ntnu.idi.bidata.controller.GameController;
import edu.ntnu.idi.bidata.ui.monopoly.MonopolyGameScene;
import edu.ntnu.idi.bidata.ui.monopoly.MonopolyPlayerSetupScene;
import edu.ntnu.idi.bidata.ui.sl.SnakeLadderGameScene;
import edu.ntnu.idi.bidata.ui.sl.SnakeLadderPlayerSetupScene;
import javafx.application.Application;
import javafx.stage.Stage;
import edu.ntnu.idi.bidata.app.GameVariant;
import edu.ntnu.idi.bidata.factory.GameFactory;
import edu.ntnu.idi.bidata.model.BoardGame;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

/**
 * Entry point for the board-game application.
 * Handles scene transitions and game flow via SceneManager.
 */
public class MainApp extends Application {
    private Stage primaryStage;
    private GameVariant selectedVariant;
    // Store the selected theme for Snakes & Ladders
    private SnakeLadderPlayerSetupScene.Theme currentSnakesLaddersTheme = SnakeLadderPlayerSetupScene.Theme.EGYPT;

    // Store detailed setup for S&L players
    private List<PlayerSetupData> lastSlPlayerSetupDetails = new ArrayList<>();
    // Store names for Monopoly (as it doesn't have piece selection yet)
    private List<String> lastMonopolyPlayerNames = new ArrayList<>();
    private SceneManager sceneManager;


    @Override
    public void start(Stage primaryStage) {
        this.primaryStage = primaryStage;
        this.sceneManager = SceneManager.getInstance();
        this.sceneManager.initialize(primaryStage);

        Runnable newGameAction = () -> {
            if (selectedVariant == null) {
                System.out.println("New Game requested but variant not set. Returning to selection.");
                sceneManager.show("selection");
                return;
            }
            // Check if player data is available for the selected variant
            boolean canRestart = switch (selectedVariant) {
                case SNAKES_LADDERS -> !lastSlPlayerSetupDetails.isEmpty();
                case MINI_MONOPOLY -> !lastMonopolyPlayerNames.isEmpty();
                // default -> false; // Or handle other variants
            };

            if (!canRestart) {
                System.out.println("New Game requested but player data for " + selectedVariant + " not set. Returning to setup.");
                sceneManager.show(getSetupKey(selectedVariant)); // Go to setup for that variant
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
            lastSlPlayerSetupDetails.clear();
            lastMonopolyPlayerNames.clear();
            currentSnakesLaddersTheme = SnakeLadderPlayerSetupScene.Theme.EGYPT; // Reset theme
            sceneManager.show("selection");
        };

        // 1) Selection scene registration (Assuming SelectionScene exists)
        sceneManager.register("selection", () ->
            new SelectionScene( // This scene is not provided, but its registration is kept
                primaryStage,
                variant -> {
                    this.selectedVariant = variant;
                    sceneManager.show(getSetupKey(variant));
                }
            )
        );


        // 2) Snake & Ladder setup scene registration
        sceneManager.register("slSetup", () -> {
            // Updated BiConsumer to take List<PlayerSetupData>
            BiConsumer<List<PlayerSetupData>, SnakeLadderPlayerSetupScene.Theme> onStartSl = (details, theme) -> {
                this.lastSlPlayerSetupDetails = details;
                this.currentSnakesLaddersTheme = theme;
                this.selectedVariant = GameVariant.SNAKES_LADDERS; // Ensure variant is set
                System.out.println("Starting S&L game. Player Details: " + details + ". Selected Theme: " + theme);
                sceneManager.show("slGame");
            };
            return new SnakeLadderPlayerSetupScene(
                primaryStage,
                onStartSl,
                homeAction
            );
        });


        // 3) Monopoly setup scene registration (Assuming MonopolyPlayerSetupScene exists)
        sceneManager.register("monoSetup", () -> {
            Consumer<List<String>> onStartMono = names -> {
                this.lastMonopolyPlayerNames = names;
                this.selectedVariant = GameVariant.MINI_MONOPOLY; // Ensure variant is set
                sceneManager.show("monoGame");
            };
            // Assuming MonopolyPlayerSetupScene exists and takes these params
            return new MonopolyPlayerSetupScene( // This scene is not provided
                primaryStage,
                onStartMono,
                homeAction
            );
        });


        // 4a) Snakes & Ladders game scene registration
        sceneManager.register("slGame", () -> {
            if (lastSlPlayerSetupDetails == null || lastSlPlayerSetupDetails.isEmpty()) {
                System.err.println("Error: Attempting to start S&L game without player details.");
                sceneManager.show("slSetup"); // Or "selection"
                throw new IllegalStateException("Cannot create S&L game scene without player details. Redirecting.");
            }
            // Call the renamed factory method for S&L
            BoardGame gameModel = GameFactory.createSnakesLaddersGameWithDetails(
                lastSlPlayerSetupDetails,
                this.currentSnakesLaddersTheme
            );
            GameController controller = new GameController(gameModel);

            SnakeLadderGameScene snakeLadderGameScene = new SnakeLadderGameScene(
                primaryStage,
                controller,
                gameModel,
                newGameAction,
                homeAction,
                this.currentSnakesLaddersTheme
            );
            controller.setActiveView(snakeLadderGameScene);
            snakeLadderGameScene.initializeView();
            controller.startGame();
            return snakeLadderGameScene;
        });

        // 4b) Monopoly game scene registration (Assuming MonopolyGameScene exists)
        sceneManager.register("monoGame", () -> {
            if (lastMonopolyPlayerNames == null || lastMonopolyPlayerNames.isEmpty()) {
                System.err.println("Error: Attempting to start Monopoly game without player names.");
                sceneManager.show("monoSetup");
                throw new IllegalStateException("Cannot create Monopoly game scene without player names. Redirecting.");
            }
            BoardGame gameModel = GameFactory.createGame(lastMonopolyPlayerNames, GameVariant.MINI_MONOPOLY);
            GameController controller = new GameController(gameModel);

            MonopolyGameScene monopolyGameScene = new MonopolyGameScene( // This scene is not provided
                primaryStage,
                controller,
                gameModel,
                newGameAction,
                homeAction
            );
            controller.setActiveView(monopolyGameScene);
            monopolyGameScene.initializeView(); // Should be called by controller or ensure it's safe here
            controller.startGame();
            return monopolyGameScene;
        });

        sceneManager.show("selection");
        primaryStage.setTitle("Board Game Suite");
        primaryStage.show();
    }

    private String getSetupKey(GameVariant variant) {
        return switch (variant) {
            case SNAKES_LADDERS -> "slSetup";
            case MINI_MONOPOLY -> "monoSetup";
            // default -> throw new IllegalArgumentException("Unknown variant: " + variant); // Handle if more variants
        };
    }

    public static void main(String[] args) {
        launch(args);
    }
}