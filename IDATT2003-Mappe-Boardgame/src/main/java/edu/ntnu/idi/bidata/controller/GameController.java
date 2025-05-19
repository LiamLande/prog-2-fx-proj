package edu.ntnu.idi.bidata.controller;

import edu.ntnu.idi.bidata.model.Board;
import edu.ntnu.idi.bidata.model.BoardGame;
import edu.ntnu.idi.bidata.model.BoardGameObserver;
import edu.ntnu.idi.bidata.model.Player;
import edu.ntnu.idi.bidata.model.Tile;
import edu.ntnu.idi.bidata.model.actions.monopoly.PropertyAction; // Assuming this exists for Monopoly
import edu.ntnu.idi.bidata.model.actions.snakes.SchrodingerBoxAction; // Import new action
import edu.ntnu.idi.bidata.service.MonopolyService; // Assuming this exists for Monopoly
import edu.ntnu.idi.bidata.service.ServiceLocator;   // Assuming this exists for Monopoly
import edu.ntnu.idi.bidata.ui.GameScene;
import edu.ntnu.idi.bidata.ui.MonopolyGameScene; // Assuming this exists for Monopoly
import edu.ntnu.idi.bidata.ui.SceneManager;

import javafx.scene.control.Alert; // For Monopoly alerts, if used
import java.util.List;
import java.util.Random;

public class GameController implements BoardGameObserver {
    private final BoardGame gameModel;
    private SceneManager.ControlledScene activeView;
    private Player currentPlayer; // Player whose turn it is currently or about to be
    private MonopolyService monopolyService; // Assuming this is for Monopoly

    // State for Schrödinger's Box
    private boolean awaitingSchrodingerChoice = false;
    private Player playerMakingSchrodingerChoice = null; // The player who landed on the box
    private final Random random = new Random(); // For Schrödinger's outcome

    public GameController(BoardGame game) {
        this.gameModel = game;
        this.gameModel.addObserver(this);

        // Assuming ServiceLocator and MonopolyService are part of your Monopoly implementation
        // If not, you can remove these lines if they cause errors and are not needed for S&L
        if (ServiceLocator.getMonopolyService() != null) { // Check if service is registered
            this.monopolyService = ServiceLocator.getMonopolyService();
        }
    }

    public void setActiveView(SceneManager.ControlledScene view) {
        this.activeView = view;
        if (this.activeView != null && this.gameModel.isGameStarted()) {
            initializeOrRefreshViewForCurrentState();
        }
    }

    private void initializeOrRefreshViewForCurrentState() {
        if (activeView == null || !gameModel.isGameStarted()) return;

        this.currentPlayer = gameModel.getCurrentPlayer();
        awaitingSchrodingerChoice = false; // Reset on view refresh
        playerMakingSchrodingerChoice = null;


        if (activeView instanceof GameScene scene) {
            scene.initializeView(); // This should internally call createPlayerStatusBoxes, updatePlayerStatusDisplay
            scene.getBoardView().refresh(); // Refresh board visuals
            if (this.currentPlayer != null) {
                scene.highlightCurrentPlayer(this.currentPlayer);
            }
            scene.setRollButtonEnabled(!gameModel.isFinished() && this.currentPlayer != null && !awaitingSchrodingerChoice);
            scene.hideSchrodingerChoice(); // Ensure hidden
        } else if (activeView instanceof MonopolyGameScene scene) { // Adapt for Monopoly if it also needs this
            scene.initializeView();
            scene.updatePlayerStatusDisplay();
            // scene.getBoardView().refresh(); // Assuming MonopolyGameScene has getBoardView()
            if (this.currentPlayer != null) {
                scene.highlightCurrentPlayer(this.currentPlayer);
            }
            scene.setRollButtonEnabled(!gameModel.isFinished() && this.currentPlayer != null);
            // scene.hideSchrodingerChoice(); // If Monopoly ever gets this
        }
    }

    public void startGame() {
        gameModel.init(); // Model initializes and fires onGameStart
    }

    @Override
    public void onGameStart(List<Player> players) {
        if (activeView == null) return;

        this.currentPlayer = gameModel.getCurrentPlayer();
        awaitingSchrodingerChoice = false;
        playerMakingSchrodingerChoice = null;

        if (activeView instanceof GameScene scene) {
            scene.initializeView(); // This will set up dice, player statuses etc.
            scene.hideSchrodingerChoice(); // Crucial to hide at game start
            if (this.currentPlayer != null) {
                scene.highlightCurrentPlayer(this.currentPlayer);
            }
            scene.setRollButtonEnabled(true); // For the first player
            if (scene.getBoardView() != null) scene.getBoardView().refresh(); // Initial board draw
        } else if (activeView instanceof MonopolyGameScene scene) {
            scene.initializeView();
            if (this.currentPlayer != null) {
                scene.highlightCurrentPlayer(this.currentPlayer);
            }
            scene.setRollButtonEnabled(true);
            // if (scene.getBoardView() != null) scene.getBoardView().refresh();
        }
    }

    @Override
    public void onRoundPlayed(List<Integer> rolls, List<Player> players) {
        if (activeView == null) return;

        // 'this.currentPlayer' at this point should be the player who JUST finished their dice roll and initial move.
        // The GameService within gameModel.playTurn() is responsible for:
        // 1. Rolling dice.
        // 2. Calling player.move(diceResult).
        // 3. Calling landedTile.land(playerWhoMoved).
        // This onRoundPlayed is the *consequence* of that.

        Player playerWhoActed = this.currentPlayer; // The player whose turn activities we are processing

        // Clear any previous choice state for safety, though it should be cleared by completeSchrodingerActionSequence
        awaitingSchrodingerChoice = false;
        playerMakingSchrodingerChoice = null;
        if(activeView instanceof GameScene scene) scene.hideSchrodingerChoice(); // Ensure UI is reset


        if (playerWhoActed != null && playerWhoActed.getCurrentTile() != null) {
            Tile landedTile = playerWhoActed.getCurrentTile();

            // The tile.land(player) method was called by the service.
            // Now, we check the *type* of action on that tile to see if it's a Schrödinger box.
            if (landedTile.getAction() instanceof SchrodingerBoxAction schrodingerAction) {
                awaitingSchrodingerChoice = true;
                playerMakingSchrodingerChoice = playerWhoActed; // Store who needs to make the choice
                if (activeView instanceof GameScene scene) {
                    scene.showSchrodingerChoice(playerWhoActed, schrodingerAction);
                    scene.setRollButtonEnabled(false); // Disable main roll button
                    // Update UI to reflect current state before choice
                    scene.updateDiceLabel(rolls.isEmpty() ? "" : String.valueOf(rolls.getFirst()));
                    scene.getBoardView().refresh(); // Show player on the Schrödinger tile
                    scene.updatePlayerStatusDisplay();
                }
                // IMPORTANT: Return here. Do not proceed to finalizeTurnAndSetupNext.
                // The turn is now paused, waiting for player input via handleObserve/IgnoreSchrodingerBoxRequest.
                return;
            }

            // Handle other game-specific actions if not a Schrödinger box (e.g., Monopoly)
            if (activeView instanceof MonopolyGameScene mScene) {
                if (monopolyService != null && landedTile.getAction() instanceof PropertyAction pa) {
                    handleLandedOnProperty(playerWhoActed, pa, mScene);
                }
                // ... other Monopoly action checks ...
            }
        }

        // If no choice is pending (i.e., not a Schrödinger box or other choice-based action),
        // proceed to finalize the turn and set up for the next player.
        finalizeTurnAndSetupNext(rolls);
    }

    /**
     * Finalizes the current turn's UI updates and prepares for the next player.
     * Called when no special player choice is pending.
     */
    private void finalizeTurnAndSetupNext(List<Integer> rollsIfApplicable) {
        // At this point, any direct consequences of the dice roll and landing (like standard snakes/ladders)
        // have already occurred in the model via tile.land().

        // Check for game over first, as the last move might have ended the game.
        if (gameModel.isFinished()) {
            onGameOver(gameModel.getWinner()); // This will disable roll button etc.
            // Ensure dice and board reflect the final state.
            if (activeView instanceof GameScene scene) {
                if (rollsIfApplicable != null && !rollsIfApplicable.isEmpty()) scene.updateDiceLabel(String.valueOf(rollsIfApplicable.getFirst()));
                scene.getBoardView().refresh();
                scene.updatePlayerStatusDisplay();
            }
            return;
        }

        // Get the *next* player whose turn it is from the game model.
        // The GameService should have advanced its internal current player pointer.
        this.currentPlayer = gameModel.getCurrentPlayer();

        // Update UI for the new current player.
        if (activeView instanceof GameScene scene) {
            if (rollsIfApplicable != null && !rollsIfApplicable.isEmpty()) scene.updateDiceLabel(String.valueOf(rollsIfApplicable.getFirst()));
            scene.getBoardView().refresh(); // Show any position changes
            scene.updatePlayerStatusDisplay(); // Update tile numbers in status
            scene.hideSchrodingerChoice(); // Ensure choice UI is hidden
            if (this.currentPlayer != null) scene.highlightCurrentPlayer(this.currentPlayer);
            scene.setRollButtonEnabled(!gameModel.isFinished()); // Enable if game not over
        } else if (activeView instanceof MonopolyGameScene mScene) {
            // ... similar updates for MonopolyGameScene ...
            // mScene.updateDiceLabel(...);
            // mScene.getBoardView().refresh();
            // mScene.updatePlayerStatusDisplay();
            // mScene.hideSchrodingerChoice(); // If applicable
            // if (this.currentPlayer != null) mScene.highlightCurrentPlayer(this.currentPlayer);
            // mScene.setRollButtonEnabled(!gameModel.isFinished());
        }
    }

    @Override
    public void onGameOver(Player winner) {
        if (activeView == null) return;
        awaitingSchrodingerChoice = false; // Reset state
        playerMakingSchrodingerChoice = null;

        if (activeView instanceof GameScene scene) {
            scene.displayGameOver(winner); // This method should handle dice label, button state etc.
            scene.hideSchrodingerChoice(); // Ensure hidden
            scene.setRollButtonEnabled(false);
        } else if (activeView instanceof MonopolyGameScene scene) {
            scene.displayGameOver(winner);
            // scene.hideSchrodingerChoice(); // If applicable
            scene.setRollButtonEnabled(false);
        }
    }

    public void handleRollDiceRequest() {
        if (gameModel.isFinished() || this.currentPlayer == null || awaitingSchrodingerChoice) {
            // If awaiting choice, the main roll button should already be disabled by showSchrodingerChoice.
            // This is an extra safeguard.
            if (activeView instanceof GameScene scene) {
                scene.setRollButtonEnabled(false);
            } else if (activeView instanceof MonopolyGameScene scene) {
                // scene.setRollButtonEnabled(false);
            }
            return;
        }

        // Disable roll button immediately to prevent multiple clicks for the same turn
        if (activeView instanceof GameScene scene) {
            scene.setRollButtonEnabled(false);
            scene.showGameMessage(this.currentPlayer.getName() + " is rolling..."); // Brief message
        } else if (activeView instanceof MonopolyGameScene scene) {
            // scene.setRollButtonEnabled(false);
        }

        gameModel.playTurn(this.currentPlayer); // This call eventually triggers onRoundPlayed
    }

    /**
     * Called by the UI when the player chooses to "observe" the Schrödinger's Box.
     */
    public void handleObserveSchrodingerBoxRequest() {
        if (!awaitingSchrodingerChoice || playerMakingSchrodingerChoice == null) {
            System.err.println("Controller: ObserveSchrodingerBoxRequest called inappropriately.");
            return;
        }

        boolean goToStart = random.nextBoolean(); // 50/50 chance
        Tile targetTile;
        String outcomeMessage;
        Board board = gameModel.getBoard();

        if (board == null || board.getTile(0) == null || board.getTiles().isEmpty()) {
            System.err.println("Controller: Board or critical tiles not available for Schrödinger outcome.");
            completeSchrodingerActionSequence(); // Attempt to gracefully exit the choice state
            return;
        }

        if (goToStart) {
            targetTile = board.getTile(0); // Tile 0 is the start
            outcomeMessage = playerMakingSchrodingerChoice.getName() + " opened the box... Oh no! Sent back to the start!";
        } else {
            int lastTileId = board.getTiles().size() - 1; // Assuming tile IDs are 0 to N-1
            targetTile = board.getTile(lastTileId);
            outcomeMessage = playerMakingSchrodingerChoice.getName() + " opened the box... Unbelievable! Sent straight to the finish line!";
        }

        if (targetTile != null) {
            playerMakingSchrodingerChoice.setCurrentTile(targetTile);
            // After moving the player, we could trigger the land() method of the new tile
            // if we want actions on the destination tile (start/finish) to also execute.
            // For S&L, start/finish usually don't have actions, but if they did:
            // targetTile.land(playerMakingSchrodingerChoice); // This would call its action.perform()
            // This might lead to nested actions, so handle with care or decide against it.
            // For now, direct placement is simpler.
        } else {
            outcomeMessage = playerMakingSchrodingerChoice.getName() + " opened the box... but the destination was unclear!";
            System.err.println("Controller: Target tile for Schrödinger outcome was null.");
        }

        if (activeView instanceof GameScene scene) {
            scene.showGameMessage(outcomeMessage);
        }

        completeSchrodingerActionSequence();
    }

    /**
     * Called by the UI when the player chooses to "move on" (ignore) the Schrödinger's Box.
     */
    public void handleIgnoreSchrodingerBoxRequest() {
        if (!awaitingSchrodingerChoice || playerMakingSchrodingerChoice == null) {
            System.err.println("Controller: IgnoreSchrodingerBoxRequest called inappropriately.");
            return;
        }
        String message = playerMakingSchrodingerChoice.getName() + " cautiously decided to ignore the mysterious box.";

        if (activeView instanceof GameScene scene) {
            scene.showGameMessage(message);
        }
        completeSchrodingerActionSequence();
    }

    /**
     * Common logic after a Schrödinger choice is made (observe or ignore).
     * This method resets the choice state and finalizes the turn by preparing for the next player.
     */
    private void completeSchrodingerActionSequence() {
        awaitingSchrodingerChoice = false;
        // The playerMakingSchrodingerChoice is the one whose turn effectively just had its action phase.

        // The actual dice rolls (List<Integer> rolls) that led to the Schrödinger tile
        // were processed in onRoundPlayed before it returned early.
        // We don't have 'rolls' here directly, so pass null or an empty list.
        // The dice label in UI should already be updated from the initial part of onRoundPlayed.
        finalizeTurnAndSetupNext(null);

        playerMakingSchrodingerChoice = null; // Clear the player who was making the choice
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