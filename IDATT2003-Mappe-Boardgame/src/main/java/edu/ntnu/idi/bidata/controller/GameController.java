package edu.ntnu.idi.bidata.controller;

import edu.ntnu.idi.bidata.model.Board;
import edu.ntnu.idi.bidata.model.BoardGame;
import edu.ntnu.idi.bidata.model.BoardGameObserver;
import edu.ntnu.idi.bidata.model.Player;
import edu.ntnu.idi.bidata.model.Tile;
import edu.ntnu.idi.bidata.model.actions.monopoly.*;
import edu.ntnu.idi.bidata.model.actions.snakes.SchrodingerBoxAction;
import edu.ntnu.idi.bidata.service.MonopolyService;
import edu.ntnu.idi.bidata.service.ServiceLocator;
import edu.ntnu.idi.bidata.ui.sl.SnakeLadderGameScene;
import edu.ntnu.idi.bidata.ui.monopoly.MonopolyGameScene;
import edu.ntnu.idi.bidata.ui.SceneManager;
import edu.ntnu.idi.bidata.util.Logger;

import javafx.scene.control.Alert;
import java.util.List;

/**
 * The GameController class is responsible for managing the game flow,
 * coordinating between the game model (BoardGame) and the user interface (SceneManager.ControlledScene).
 * It implements BoardGameObserver to react to changes in the game state.
 */
public class GameController implements BoardGameObserver {
    private final BoardGame gameModel;
    private SceneManager.ControlledScene activeView;
    private Player currentPlayer;
    private MonopolyService monopolyService;

    private boolean awaitingSchrodingerChoice = false;
    private Player playerMakingSchrodingerChoice = null;
    private SchrodingerBoxAction currentSchrodingerAction = null; // Store the action instance

    /**
     * Constructs a GameController with the specified game model.
     * It initializes the game model, adds itself as an observer, and attempts to locate the MonopolyService.
     *
     * @param game The BoardGame model instance.
     */
    public GameController(BoardGame game) {
        this.gameModel = game;
        this.gameModel.addObserver(this);
        Logger.info("GameController initialized.");

        if (ServiceLocator.getMonopolyService() != null) {
            this.monopolyService = ServiceLocator.getMonopolyService();
            Logger.debug("MonopolyService located and assigned.");
        } else {
            Logger.warning("MonopolyService not found via ServiceLocator. Monopoly features might be unavailable.");
        }
    }

    /**
     * Sets the active view (scene) for the game.
     * If the game has already started, it initializes or refreshes the view to reflect the current game state.
     *
     * @param view The SceneManager.ControlledScene to be set as active.
     */
    public void setActiveView(SceneManager.ControlledScene view) {
        this.activeView = view;
        Logger.info("Active view set to: " + (view != null ? view.getClass().getSimpleName() : "null"));
        if (this.activeView != null && this.gameModel.isGameStarted()) {
            Logger.debug("Active view set and game started, initializing/refreshing view.");
            initializeOrRefreshViewForCurrentState();
        } else {
            Logger.debug("Skipping view initialization/refresh: Active view is null or game not started.");
        }
    }

    /**
     * Initializes or refreshes the active view based on the current game state.
     * This method is called when the active view is set or when the game state changes significantly.
     * It configures the view (e.g., SnakeLadderGameScene or MonopolyGameScene) with the current player,
     * board state, and enables/disables UI elements as appropriate.
     */
    private void initializeOrRefreshViewForCurrentState() {
        if (activeView == null) {
            Logger.warning("Attempted to initialize/refresh view, but activeView is null.");
            return;
        }
        if (!gameModel.isGameStarted()){
            Logger.debug("Attempted to initialize/refresh view, but gameModel is not started.");
            return;
        }
        Logger.debug("Initializing/refreshing view for current game state. View type: " + activeView.getClass().getSimpleName());

        this.currentPlayer = gameModel.getCurrentPlayer();


        if (activeView instanceof SnakeLadderGameScene scene) {
            awaitingSchrodingerChoice = false;
            playerMakingSchrodingerChoice = null;
            currentSchrodingerAction = null; // Reset current action
            Logger.debug("Schrödinger choice state reset. Current player for view: " + (this.currentPlayer != null ? this.currentPlayer.getName() : "None"));


            scene.initializeView();
            scene.getBoardView().refresh();
            if (this.currentPlayer != null) {
                scene.highlightCurrentPlayer(this.currentPlayer);
            }
            scene.setRollButtonEnabled(!gameModel.isFinished() && this.currentPlayer != null && !awaitingSchrodingerChoice);
            scene.hideSchrodingerChoice();
            Logger.debug("SnakeLadderGameScene refreshed for current state.");
        } else if (activeView instanceof MonopolyGameScene scene) {
            scene.initializeView();
            scene.updatePlayerStatusDisplay();
            if (this.currentPlayer != null) {
                scene.highlightCurrentPlayer(this.currentPlayer);
            }
            scene.setRollButtonEnabled(!gameModel.isFinished() && this.currentPlayer != null);
            Logger.debug("MonopolyGameScene refreshed for current state.");
        }
    }

    /**
     * Starts the game by initializing the game model.
     * The model will then typically fire an onGameStart event.
     */
    public void startGame() {
        Logger.info("startGame() called. Initializing game model.");
        gameModel.init(); // Model initializes and fires onGameStart
    }

    /**
     * Called when the game starts.
     * It initializes the UI for the game start, sets the current player, and enables appropriate UI controls.
     *
     * @param players The list of players participating in the game.
     */
    @Override
    public void onGameStart(List<Player> players) {
        Logger.info("Game Started. Number of players: " + players.size());
        if (activeView == null) {
            Logger.warning("onGameStart: activeView is null, UI updates will be skipped.");
            return;
        }

        this.currentPlayer = gameModel.getCurrentPlayer();
        awaitingSchrodingerChoice = false;
        playerMakingSchrodingerChoice = null;
        currentSchrodingerAction = null; // Reset current action
        Logger.info("First player's turn: " + (this.currentPlayer != null ? this.currentPlayer.getName() : "None"));


        if (activeView instanceof SnakeLadderGameScene scene) {
            Logger.debug("onGameStart: Schrödinger state reset.");
            scene.initializeView();
            scene.hideSchrodingerChoice();
            if (this.currentPlayer != null) {
                scene.highlightCurrentPlayer(this.currentPlayer);
            }
            scene.setRollButtonEnabled(true);
            if (scene.getBoardView() != null) scene.getBoardView().refresh();
            Logger.debug("SnakeLadderGameScene initialized for game start.");
        } else if (activeView instanceof MonopolyGameScene scene) {
            scene.initializeView();
            if (this.currentPlayer != null) {
                scene.highlightCurrentPlayer(this.currentPlayer);
            }
            scene.setRollButtonEnabled(true);
            Logger.debug("MonopolyGameScene initialized for game start.");
        }
    }

    /**
     * Called after a round is played (a player has taken their turn).
     * It updates the UI based on the outcome of the turn, such as player movement,
     * tile actions (e.g., landing on a property, Schrödinger box), and prepares for the next player's turn.
     *
     * @param rolls   The list of dice rolls made during the turn.
     * @param players The list of all players in the game.
     */
    @Override
    public void onRoundPlayed(List<Integer> rolls, List<Player> players) {
        String rollsStr = rolls != null && !rolls.isEmpty() ? rolls.toString() : "N/A";
        Logger.info("Round played. Player who acted: " + (this.currentPlayer != null ? this.currentPlayer.getName() : "Unknown") + ". Rolls: " + rollsStr);

        if (activeView == null) {
            Logger.warning("onRoundPlayed: activeView is null, UI updates will be skipped.");
            return;
        }

        Player playerWhoActed = this.currentPlayer;

        if(activeView instanceof SnakeLadderGameScene scene) {
            awaitingSchrodingerChoice = false;
            playerMakingSchrodingerChoice = null;
            currentSchrodingerAction = null; // Reset before checking new actions
            scene.hideSchrodingerChoice();
            Logger.debug("onRoundPlayed: Schrödinger choice state reset before tile action check.");

        }



        if (playerWhoActed != null && playerWhoActed.getCurrentTile() != null) {
            Tile landedTile = playerWhoActed.getCurrentTile();
            Logger.debug("Player " + playerWhoActed.getName() + " landed on tile " + landedTile.getId() +
                ". Action: " +
                (landedTile.getAction() != null ? landedTile.getAction().getClass().getSimpleName() : "None"));

            if(activeView instanceof SnakeLadderGameScene scene) {
                if (landedTile.getAction() instanceof SchrodingerBoxAction schrodingerActionInstance) {
                    this.currentSchrodingerAction = schrodingerActionInstance; // Store the action
                    awaitingSchrodingerChoice = true;
                    playerMakingSchrodingerChoice = playerWhoActed;
                    // The action's perform() method was already called by gameModel.playTurn -> player.move -> tile.landOn -> action.perform
                    // Logger.info from action's perform() method should have already fired.
                    // Here, we set up the controller state to await UI input.
                    Logger.info("Controller detected landing on Schrödinger Box. Setting up for player choice.");
                    // Pass the action instance so UI can get description, etc.
                    scene.showSchrodingerChoice(playerWhoActed, this.currentSchrodingerAction);
                    scene.setRollButtonEnabled(false);
                    assert rolls != null;
                    scene.updateDiceLabel(rolls.isEmpty() ? "" : String.valueOf(rolls.getFirst()));
                    scene.getBoardView().refresh();
                    scene.updatePlayerStatusDisplay();
                    Logger.debug("Schrödinger choice UI shown for " + playerWhoActed.getName());
                    return; // Await player choice
                }
            }
            if (activeView instanceof MonopolyGameScene mScene) {
                mScene.updatePlayerStatusDisplay();
                mScene.getBoardView().refresh();
                if (monopolyService != null && landedTile.getAction() instanceof PropertyAction pa) {
                    Logger.debug("Player " + playerWhoActed.getName() + " landed on Monopoly property: " + pa.getName() + ". Handling property action.");
                    handleLandedOnProperty(playerWhoActed, pa, mScene);
                }
                if (landedTile.getAction() instanceof ChanceAction chanceAction) {
                    Logger.info("Player " + playerWhoActed.getName() + " landed on a Chance tile. Action would already be executed by model, showing alert.");
                    mScene.showAlert("Chance!", "Chance Tile",
                        "You landed on a Chance tile! " + chanceAction.getDescription(),
                        Alert.AlertType.INFORMATION);

                } else if (landedTile.getAction() instanceof CommunityChestAction ca) {
                    mScene.showAlert("Community Chest!", "Community Chest Tile",
                        "You landed on a Community Chest tile! " + ca.getDescription(),
                        Alert.AlertType.INFORMATION);
                } else if (landedTile.getAction() instanceof TaxAction ta) {
                    mScene.showAlert("Taxes!", "Death, Taxes, and Taxes",
                        "You must pay " + ta.getTaxAmount() + "$ in taxes!",
                        Alert.AlertType.INFORMATION);
                } else if (landedTile.getAction() instanceof GoToJailAction) {
                    mScene.showAlert("Go to jail!", "Community Chest Tile",
                        "You must go to jail! For you are poor!",
                        Alert.AlertType.WARNING);
                }

                mScene.updatePlayerStatusDisplay();
                mScene.getBoardView().refresh();
                Logger.debug("MonopolyGameScene UI updated after property action handling.");

            }
        } else {
            Logger.warning("onRoundPlayed: playerWhoActed or their current tile is null. Player: " +
                (playerWhoActed != null ? playerWhoActed.getName() : "null"));
        }

        Logger.debug("No special choice pending from landing. Finalizing turn and setting up for next player.");
        finalizeTurnAndSetupNext(rolls);
    }


    /**
     * Finalizes the current player's turn and sets up the UI for the next player.
     * This includes updating dice labels, refreshing the board view, highlighting the next current player,
     * and enabling/disabling the roll button. It also checks if the game is over.
     *
     * @param rollsIfApplicable A list of integers representing the dice rolls from the completed turn,
     *                          or null/empty if not applicable (e.g., after a Schrödinger box choice).
     *                          Used to update the dice display.
     */
    private void finalizeTurnAndSetupNext(List<Integer> rollsIfApplicable) {
        Logger.debug("Entering finalizeTurnAndSetupNext.");
        String rollsStr = rollsIfApplicable != null && !rollsIfApplicable.isEmpty() ? rollsIfApplicable.toString() : "N/A";
        Logger.debug("Rolls from this turn (if applicable): " + rollsStr);

        // Check for game over after potential player position changes (e.g., from Schrödinger box)
        if (gameModel.isFinished()) {
            Logger.info("Game is finished (detected in finalizeTurnAndSetupNext). Calling onGameOver.");
            onGameOver(gameModel.getWinner());
            if (activeView instanceof SnakeLadderGameScene scene) {
                if (rollsIfApplicable != null && !rollsIfApplicable.isEmpty()) scene.updateDiceLabel(String.valueOf(rollsIfApplicable.getFirst()));
                scene.getBoardView().refresh();
                scene.updatePlayerStatusDisplay();
                Logger.debug("SnakeLadderGameScene UI updated for final game state.");
            }
            if (activeView instanceof MonopolyGameScene scene) {
                if (rollsIfApplicable != null && !rollsIfApplicable.isEmpty()) scene.updateDiceLabel(String.valueOf(rollsIfApplicable.getFirst()));
                scene.getBoardView().refresh();
                scene.updatePlayerStatusDisplay();
                Logger.debug("MonopolyGameScene UI updated for final game state.");
            }
            return;
        }

        this.currentPlayer = gameModel.getCurrentPlayer(); // This is now the *next* player
        Logger.info("Next player's turn: " + (this.currentPlayer != null ? this.currentPlayer.getName() : "None"));

        if (activeView instanceof SnakeLadderGameScene scene) {
            if (rollsIfApplicable != null && !rollsIfApplicable.isEmpty()) scene.updateDiceLabel(String.valueOf(rollsIfApplicable.getFirst()));
            scene.getBoardView().refresh();
            scene.updatePlayerStatusDisplay();
            scene.hideSchrodingerChoice();
            if (this.currentPlayer != null) scene.highlightCurrentPlayer(this.currentPlayer);
            scene.setRollButtonEnabled(!gameModel.isFinished());
            Logger.debug("SnakeLadderGameScene UI updated for the next turn.");
        } else if (activeView instanceof MonopolyGameScene mScene) {
            // ... (Monopoly UI updates)
            if (rollsIfApplicable != null && !rollsIfApplicable.isEmpty()) mScene.updateDiceLabel(String.valueOf(rollsIfApplicable.getFirst()));
            Logger.debug("MonopolyGameScene UI updated for the next turn (placeholder).");
        }
    }

    /**
     * Called when the game is over.
     * It updates the UI to display the game over message, shows the winner, and disables game controls.
     *
     * @param winner The player who won the game, or null if it's a draw or error.
     */
    @Override
    public void onGameOver(Player winner) {
        String winnerName = winner != null ? winner.getName() : "No one (Draw or Error)";
        Logger.info("Game Over. Winner: " + winnerName);
        if (activeView == null) {
            Logger.warning("onGameOver: activeView is null, UI updates will be skipped.");
            return;
        }
        awaitingSchrodingerChoice = false;
        playerMakingSchrodingerChoice = null;
        currentSchrodingerAction = null; // Reset current action
        Logger.debug("Schrödinger choice state reset on game over.");

        if (activeView instanceof SnakeLadderGameScene scene) {
            scene.displayGameOver(winner);
            scene.hideSchrodingerChoice();
            scene.setRollButtonEnabled(false);
            Logger.debug("SnakeLadderGameScene displayed game over message and disabled roll button.");
        } else if (activeView instanceof MonopolyGameScene scene) {
            scene.displayGameOver(winner);
            scene.setRollButtonEnabled(false);
            Logger.debug("MonopolyGameScene displayed game over message and disabled roll button.");
        }
    }

    /**
     * Handles a request from the UI to roll the dice for the current player.
     * It checks if the game is ongoing, if there's a current player, and if the game is not awaiting
     * a special choice (like a Schrödinger box decision). If valid, it instructs the game model
     * to play the turn for the current player.
     */
    public void handleRollDiceRequest() {
        Logger.debug("Entering handleRollDiceRequest for player: " + (this.currentPlayer != null ? this.currentPlayer.getName() : "Unknown/None"));
        if (gameModel.isFinished()) {
            Logger.warning("Roll dice request ignored: Game is finished.");
            return;
        }
        if (this.currentPlayer == null) {
            Logger.warning("Roll dice request ignored: Current player is null.");
            return;
        }
        if (awaitingSchrodingerChoice) {
            Logger.warning("Roll dice request ignored: Awaiting Schrödinger choice from " +
                (playerMakingSchrodingerChoice != null ? playerMakingSchrodingerChoice.getName() : "Unknown"));
            if (activeView instanceof SnakeLadderGameScene scene) scene.setRollButtonEnabled(false);
            return;
        }

        Logger.info("Player " + this.currentPlayer.getName() + " initiated a dice roll.");

        if (activeView instanceof SnakeLadderGameScene scene) {
            scene.setRollButtonEnabled(false);
            scene.showGameMessage(this.currentPlayer.getName() + " is rolling...");
            Logger.debug("Roll button disabled for SnakeLadderGameScene during roll.");
        }

        Logger.debug("Executing gameModel.playTurn() for player: " + this.currentPlayer.getName());
        gameModel.playTurn(this.currentPlayer);
    }

    /**
     * Handles a request from the UI to "observe" the Schrödinger box.
     * This is called when a player lands on a Schrödinger box tile and chooses the "observe" option.
     * It executes the observation logic of the SchrödingerBoxAction and updates the game state.
     */
    public void handleObserveSchrodingerBoxRequest() {
        Logger.debug("Entering handleObserveSchrodingerBoxRequest.");
        if (!awaitingSchrodingerChoice || playerMakingSchrodingerChoice == null || currentSchrodingerAction == null) {
            Logger.warning("ObserveSchrodingerBoxRequest called inappropriately. State: awaiting=" + awaitingSchrodingerChoice +
                ", player=" + (playerMakingSchrodingerChoice != null ? playerMakingSchrodingerChoice.getName() : "null") +
                ", action=" + (currentSchrodingerAction != null));
            // If UI is out of sync, hide choice and enable roll for current player to recover state
            if (activeView instanceof SnakeLadderGameScene scene) {
                scene.hideSchrodingerChoice();
                scene.setRollButtonEnabled(!gameModel.isFinished() && this.currentPlayer != null);
            }
            return;
        }
        // Logging of choice is now within currentSchrodingerAction.executeObserve()

        Board board = gameModel.getBoard(); // Get the board instance
        // The player (playerMakingSchrodingerChoice) and board are passed to the action method
        String outcomeMessage = currentSchrodingerAction.executeObserve(playerMakingSchrodingerChoice, board);

        if (activeView instanceof SnakeLadderGameScene scene) {
            scene.showGameMessage(outcomeMessage);
            // Board view and player status will be refreshed by completeSchrodingerActionSequence -> finalizeTurnAndSetupNext
        }

        completeSchrodingerActionSequence();
    }

    /**
     * Handles a request from the UI to "ignore" the Schrödinger box.
     * This is called when a player lands on a Schrödinger box tile and chooses the "ignore" option.
     * It executes the ignore logic of the SchrödingerBoxAction and updates the game state.
     */
    public void handleIgnoreSchrodingerBoxRequest() {
        Logger.debug("Entering handleIgnoreSchrodingerBoxRequest.");
        if (!awaitingSchrodingerChoice || playerMakingSchrodingerChoice == null || currentSchrodingerAction == null) {
            Logger.warning("IgnoreSchrodingerBoxRequest called inappropriately. State: awaiting=" + awaitingSchrodingerChoice +
                ", player=" + (playerMakingSchrodingerChoice != null ? playerMakingSchrodingerChoice.getName() : "null") +
                ", action=" + (currentSchrodingerAction != null));
            if (activeView instanceof SnakeLadderGameScene scene) {
                scene.hideSchrodingerChoice();
                scene.setRollButtonEnabled(!gameModel.isFinished() && this.currentPlayer != null);
            }
            return;
        }
        // Logging of choice is now within currentSchrodingerAction.executeIgnore()

        String outcomeMessage = currentSchrodingerAction.executeIgnore(playerMakingSchrodingerChoice);

        if (activeView instanceof SnakeLadderGameScene scene) {
            scene.showGameMessage(outcomeMessage);
        }
        completeSchrodingerActionSequence();
    }

    /**
     * Completes the sequence of actions related to a Schrödinger box event.
     * It resets the state flags for awaiting a choice and finalizes the turn,
     * setting up for the next player.
     */
    private void completeSchrodingerActionSequence() {
        Logger.debug("Completing Schrödinger action sequence for player: " +
            (playerMakingSchrodingerChoice != null ? playerMakingSchrodingerChoice.getName() : "Unknown"));
        awaitingSchrodingerChoice = false;
        finalizeTurnAndSetupNext(null);
        playerMakingSchrodingerChoice = null;
        currentSchrodingerAction = null;
        Logger.debug("Schrödinger choice state fully reset after action completion and turn finalization.");
    }


    /**
     * Handles the logic when a player lands on a property tile in a Monopoly game.
     * It checks if the property is owned, unowned, or owned by another player,
     * and interacts with the MonopolyService and the MonopolyGameScene to manage
     * property purchase, rent payment, and display relevant dialogs to the user.
     *
     * @param player         The player who landed on the property.
     * @param propertyAction The PropertyAction associated with the tile.
     * @param monopolyView   The MonopolyGameScene instance for UI interactions.
     */
    private void handleLandedOnProperty(Player player, PropertyAction propertyAction, MonopolyGameScene monopolyView) {
        Logger.info("Player " + player.getName() + " landed on property: " + propertyAction.getName() + ". Handling action.");

        if (monopolyService == null) {
            Logger.error("MonopolyService not available! Cannot handle property action for " + propertyAction.getName());
            monopolyView.showAlert("Error", "Service Unavailable", "Monopoly features are currently unavailable.", Alert.AlertType.ERROR);
            return;
        }

        //This is the worst possible way to do this, but it works for now. -Liam
        if (propertyAction.getOwner() == null) {
            Logger.debug("Property " + propertyAction.getName() + " is unowned. Cost: " + propertyAction.getCost() + ". Player money: " + player.getMoney());
            if (player.getMoney() >= propertyAction.getCost()) {
                Logger.debug("Player " + player.getName() + " can afford " + propertyAction.getName() + ". Showing purchase dialog.");
                boolean wantsToBuy = monopolyView.showPropertyPurchaseDialog(player, propertyAction);
                if (wantsToBuy) {
                    Logger.info("Player " + player.getName() + " attempts to buy " + propertyAction.getName());
                    boolean purchased = monopolyService.purchaseProperty(player, propertyAction);
                    if (purchased) {
                        Logger.info("Property " + propertyAction.getName() + " purchased by " + player.getName() + ". New balance: $" + player.getMoney());
                        monopolyView.showAlert("Property Purchased", "Congratulations!",
                            "You now own " + propertyAction.getName() + ".\n" +
                                "Your remaining balance: $" + player.getMoney(), Alert.AlertType.INFORMATION);
                    } else {
                        Logger.error("Purchase of " + propertyAction.getName() + " by " + player.getName() + " failed despite affording it (service layer issue?).");
                        monopolyView.showAlert("Purchase Failed", "Error",
                            "Could not complete the purchase of " + propertyAction.getName() + ".", Alert.AlertType.ERROR);
                    }
                } else {
                    Logger.info("Player " + player.getName() + " chose not to buy unowned property " + propertyAction.getName());
                }
            } else {
                Logger.info("Player " + player.getName() + " cannot afford unowned property " + propertyAction.getName());
                monopolyView.showAlert("Property Available", propertyAction.getName(),
                    "You landed on " + propertyAction.getName() + " (Cost: $" + propertyAction.getCost() +
                        "), but you don't have enough money to buy it.", Alert.AlertType.INFORMATION);
            }
        } else if (!propertyAction.getOwner().equals(player)) {
            int rentAmount = propertyAction.getRent();
            Logger.info("Player " + player.getName() + " landed on " + propertyAction.getName() + " owned by " +
                propertyAction.getOwner().getName() + ". Rent due: $" + rentAmount);

            monopolyView.showAlert("Rent Due!",
                "Landed on " + propertyAction.getName(),
                player.getName() + ", you landed on " + propertyAction.getOwner().getName() +
                    "'s property. You owe $" + rentAmount + " in rent.",
                Alert.AlertType.INFORMATION);

            boolean paid = monopolyService.payRent(player, propertyAction.getOwner(), rentAmount);
            if (paid) {
                Logger.info("Player " + player.getName() + " paid $" + rentAmount + " rent to " + propertyAction.getOwner().getName() + " for " + propertyAction.getName());
            } else {
                Logger.warning("Player " + player.getName() + " could not afford to pay $" + rentAmount + " rent for " + propertyAction.getName() + ". Potential bankruptcy.");
                monopolyView.showAlert("Rent Payment Failed", "Insufficient Funds",
                    player.getName() + " could not afford to pay $" + rentAmount + " rent.", Alert.AlertType.WARNING);
                player.decreaseMoney(player.getMoney()); //Lose condition
            }
        } else {
            Logger.debug("Player " + player.getName() + " landed on their own property: " + propertyAction.getName() + ". No rent/purchase action.");
        }
        monopolyView.updatePlayerStatusDisplay();
        Logger.debug("Monopoly player status display updated after property action handling.");
    }

    /**
     * Gets the game model.
     *
     * @return The BoardGame instance.
     */
    public BoardGame getGameModel() {
        return gameModel;
    }
}