package edu.ntnu.idi.bidata.controller;

import edu.ntnu.idi.bidata.model.BoardGame;
import edu.ntnu.idi.bidata.model.BoardGameObserver;
import edu.ntnu.idi.bidata.model.Player;
import edu.ntnu.idi.bidata.model.Tile;
import edu.ntnu.idi.bidata.model.actions.monopoly.PropertyAction;
import edu.ntnu.idi.bidata.service.MonopolyService;
import edu.ntnu.idi.bidata.service.ServiceLocator;
import edu.ntnu.idi.bidata.ui.GameScene; // Assuming GameScene is in ui
import edu.ntnu.idi.bidata.ui.MonopolyGameScene; // Assuming MonopolyGameScene is in ui
import edu.ntnu.idi.bidata.ui.SceneManager; // For ControlledScene interface

import javafx.scene.control.Alert; // For Monopoly alerts

import java.util.List;

/**
 * Orchestrates game flow:
 * - Listens to model events from BoardGame.
 * - Receives user input requests from the active View.
 * - Uses Services for game-specific business logic.
 * - Updates the active View based on model changes and game events.
 */
public class GameController implements BoardGameObserver {
    private final BoardGame gameModel;
    private SceneManager.ControlledScene activeView; // The current active game scene (GameScene or MonopolyGameScene)
    private Player currentPlayer;

    // Services - could be injected or fetched via ServiceLocator
    private MonopolyService monopolyService;
    // private SnakesLaddersService snakesLaddersService; // If needed for other complex S&L logic

    public GameController(BoardGame game) {
        this.gameModel = game;
        this.gameModel.addObserver(this); // Controller observes the model

        // Initialize services based on game type or always if they are light
        // Assuming ServiceLocator is set up and provides these services
        this.monopolyService = ServiceLocator.getMonopolyService();
        // this.snakesLaddersService = ServiceLocator.getSnakesLaddersService();
    }

    /**
     * Sets the currently active game view for the controller to interact with.
     * This should be called by the MainApp or SceneManager when a game scene is displayed.
     * @param view The active game scene (must implement SceneManager.ControlledScene).
     */
    public void setActiveView(SceneManager.ControlledScene view) {
        this.activeView = view;
        // If the view is set and the game has already started (e.g., loading a saved game),
        // you might want to immediately refresh the view with the current game state.
        if (this.activeView != null && this.gameModel.isGameStarted()) {
            initializeOrRefreshViewForCurrentState();
        }
    }

    private void initializeOrRefreshViewForCurrentState() {
        if (activeView == null || !gameModel.isGameStarted()) return;

        this.currentPlayer = gameModel.getCurrentPlayer(); // Get current player from model

        if (activeView instanceof GameScene scene) {
            scene.initializeView(); // Sets up initial UI elements
            scene.updatePlayerStatusDisplay();
            scene.getBoardView().refresh();
            if (this.currentPlayer != null) {
                scene.highlightCurrentPlayer(this.currentPlayer);
            }
            scene.setRollButtonEnabled(!gameModel.isFinished() && this.currentPlayer != null);
        } else if (activeView instanceof MonopolyGameScene scene) {
            scene.initializeView();
            scene.updatePlayerStatusDisplay();
            scene.getBoardView().refresh();
            if (this.currentPlayer != null) {
                scene.highlightCurrentPlayer(this.currentPlayer);
            }
            scene.setRollButtonEnabled(!gameModel.isFinished() && this.currentPlayer != null);
            // Potentially check for pending actions if game was loaded mid-action
        }
    }


    /**
     * Initiates the game. The model will call back onGameStart via the observer pattern.
     */
    public void startGame() {
        gameModel.init(); // Model initializes and fires onGameStart
    }

    // ---------------------------------------------------
    // BoardGameObserver callbacks (fired by the model when its state changes):
    // ---------------------------------------------------

    @Override
    public void onGameStart(List<Player> players) {
        if (activeView == null) return;

        this.currentPlayer = gameModel.getCurrentPlayer(); // Model should determine the starting player

        if (activeView instanceof GameScene scene) {
            scene.initializeView(); // This will set up dice, player statuses etc.
            scene.highlightCurrentPlayer(this.currentPlayer);
        } else if (activeView instanceof MonopolyGameScene scene) {
            scene.initializeView();
            scene.highlightCurrentPlayer(this.currentPlayer);
        }
        // Common for both: ensure board is drawn
        if (activeView instanceof GameScene) ((GameScene)activeView).getBoardView().refresh();
        if (activeView instanceof MonopolyGameScene) ((MonopolyGameScene)activeView).getBoardView().refresh();

    }

    @Override
    public void onRoundPlayed(List<Integer> rolls, List<Player> players) {
        if (activeView == null) return;

        Player playerWhoMoved = this.currentPlayer; // The player whose turn just ended
        this.currentPlayer = gameModel.getCurrentPlayer(); // Get the next player from the model

        String diceRollDisplay = rolls.isEmpty() ? "" : String.valueOf(rolls.getFirst()); // Adapt if multiple dice

        // Common UI updates
        if (activeView instanceof GameScene scene) {
            scene.updateDiceLabel(diceRollDisplay);
            scene.getBoardView().refresh();
            scene.updatePlayerStatusDisplay();
            if (!gameModel.isFinished()) {
                scene.highlightCurrentPlayer(this.currentPlayer);
                scene.setRollButtonEnabled(true); // Enable for next player
            }
        } else if (activeView instanceof MonopolyGameScene scene) {
            scene.updateDiceLabel(diceRollDisplay);
            // boardView.refresh() and updatePlayerStatusDisplay() will be called after Monopoly actions

            // Monopoly-specific post-move logic
            if (playerWhoMoved != null && playerWhoMoved.getCurrentTile() != null) {
                Tile landedTile = playerWhoMoved.getCurrentTile();
                if (landedTile.getAction() instanceof PropertyAction pa) {
                    handleLandedOnProperty(playerWhoMoved, pa, scene);
                }
                // TODO: Handle other Monopoly actions like Chance, Community Chest, Go To Jail, Tax etc.
                // This might involve:
                // 1. Checking landedTile.getAction().
                // 2. Calling appropriate methods in MonopolyService.
                // 3. MonopolyService updates the model.
                // 4. Model might fire more events if necessary, or controller updates view directly after service call.
            }

            // Refresh view after any potential Monopoly actions
            scene.getBoardView().refresh();
            scene.updatePlayerStatusDisplay();

            if (!gameModel.isFinished()) {
                scene.highlightCurrentPlayer(this.currentPlayer);
                scene.setRollButtonEnabled(true);
            }
        }
    }

    @Override
    public void onGameOver(Player winner) {
        if (activeView == null) return;

        if (activeView instanceof GameScene scene) {
            scene.displayGameOver(winner);
        } else if (activeView instanceof MonopolyGameScene scene) {
            scene.displayGameOver(winner);
        }
    }

    // ---------------------------------------------------
    // Methods called BY the View (user actions)
    // ---------------------------------------------------

    /**
     * Handles the request from the UI to roll the dice for the current player.
     */
    public void handleRollDiceRequest() {
        if (gameModel.isFinished() || currentPlayer == null) {
            // Should not happen if button is properly disabled, but good for safety
            if (activeView instanceof GameScene) ((GameScene)activeView).setRollButtonEnabled(false);
            if (activeView instanceof MonopolyGameScene) ((MonopolyGameScene)activeView).setRollButtonEnabled(false);
            return;
        }

        // Disable roll button immediately to prevent multiple rolls for the same turn
        if (activeView instanceof GameScene) {
            ((GameScene) activeView).setRollButtonEnabled(false);
        } else if (activeView instanceof MonopolyGameScene) {
            ((MonopolyGameScene) activeView).setRollButtonEnabled(false);
        }

        gameModel.playTurn(currentPlayer); // The model handles dice rolling, player movement, and tile actions.
        // It will then fire onRoundPlayed().
    }


    // ---------------------------------------------------
    // Monopoly-Specific Helper Logic (could be in a dedicated MonopolyGameController)
    // ---------------------------------------------------
    private void handleLandedOnProperty(Player player, PropertyAction propertyAction, MonopolyGameScene monopolyView) {
        if (monopolyService == null) {
            System.err.println("MonopolyService not available!"); // Or show an error in UI
            monopolyView.showAlert("Error", "Service Unavailable", "Monopoly features are currently unavailable.", Alert.AlertType.ERROR);
            return;
        }

        if (propertyAction.getOwner() == null) { // Property is unowned
            if (player.getMoney() >= propertyAction.getCost()) {
                // Ask player if they want to buy
                boolean wantsToBuy = monopolyView.showPropertyPurchaseDialog(player, propertyAction);
                if (wantsToBuy) {
                    boolean purchased = monopolyService.purchaseProperty(player, propertyAction);
                    if (purchased) {
                        monopolyView.showAlert("Property Purchased", "Congratulations!",
                                "You now own " + propertyAction.getName() + ".\n" +
                                        "Your remaining balance: $" + player.getMoney(), Alert.AlertType.INFORMATION);
                    } else {
                        // This case (can afford but purchase fails) should ideally be rare if service logic is robust
                        monopolyView.showAlert("Purchase Failed", "Error",
                                "Could not complete the purchase of " + propertyAction.getName() + ".", Alert.AlertType.ERROR);
                    }
                }
            } else {
                // Cannot afford to buy
                monopolyView.showAlert("Property Available", propertyAction.getName(),
                        "You landed on " + propertyAction.getName() + " (Cost: $" + propertyAction.getCost() +
                                "), but you don't have enough money to buy it.", Alert.AlertType.INFORMATION);
            }
        } else if (!propertyAction.getOwner().equals(player)) { // Property owned by another player
            int rentAmount = propertyAction.getRent(); // Or monopolyService.calculateRent(propertyAction) if complex

            monopolyView.showAlert("Rent Due!",
                    "Landed on " + propertyAction.getName(),
                    player.getName() + ", you landed on " + propertyAction.getOwner().getName() +
                            "'s property. You owe $" + rentAmount + " in rent.",
                    Alert.AlertType.INFORMATION);

            boolean paid = monopolyService.payRent(player, propertyAction.getOwner(), rentAmount);
            if (paid) {
                // Alert already shown, or add a small confirmation if needed
                // monopolyView.showAlert("Rent Paid", "Transaction Complete",
                //        "$" + rentAmount + " paid to " + propertyAction.getOwner().getName(), Alert.AlertType.INFORMATION);
            } else {
                // Player could not afford rent - handle bankruptcy or other consequences
                // This is a more complex scenario that might involve mortgaging properties,
                // or declaring bankruptcy and being removed from the game.
                monopolyView.showAlert("Rent Payment Failed", "Insufficient Funds",
                        player.getName() + " could not afford to pay $" + rentAmount + " rent.", Alert.AlertType.WARNING);
                // TODO: Implement further bankruptcy logic via MonopolyService
                // e.g., boolean isBankrupt = monopolyService.handleBankruptcy(player);
                // if (isBankrupt) gameModel.removePlayer(player); -> triggers onGameOver if conditions met
            }
        }
        // No action if player lands on their own property (usually)
    }


    /**
     * Provides access to the game model, primarily for the views to read data for display.
     * Views should not modify the model directly.
     * @return The current BoardGame model.
     */
    public BoardGame getGameModel() {
        return gameModel;
    }
}