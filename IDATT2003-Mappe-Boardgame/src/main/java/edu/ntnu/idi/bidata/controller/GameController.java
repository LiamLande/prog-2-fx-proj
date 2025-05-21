package edu.ntnu.idi.bidata.controller;

import edu.ntnu.idi.bidata.model.Board;
import edu.ntnu.idi.bidata.model.BoardGame;
import edu.ntnu.idi.bidata.model.BoardGameObserver;
import edu.ntnu.idi.bidata.model.Player;
import edu.ntnu.idi.bidata.model.Tile;
import edu.ntnu.idi.bidata.model.actions.monopoly.PropertyAction;
import edu.ntnu.idi.bidata.model.actions.snakes.SchrodingerBoxAction;
import edu.ntnu.idi.bidata.service.MonopolyService;
import edu.ntnu.idi.bidata.service.ServiceLocator;
import edu.ntnu.idi.bidata.ui.sl.SnakeLadderGameScene;
import edu.ntnu.idi.bidata.ui.monopoly.MonopolyGameScene;
import edu.ntnu.idi.bidata.ui.SceneManager;
import edu.ntnu.idi.bidata.util.Logger;

import javafx.scene.control.Alert;
import java.util.List;
import java.util.Random;

public class GameController implements BoardGameObserver {
    private final BoardGame gameModel;
    private SceneManager.ControlledScene activeView;
    private Player currentPlayer;
    private MonopolyService monopolyService;

    private boolean awaitingSchrodingerChoice = false;
    private Player playerMakingSchrodingerChoice = null;
    private final Random random = new Random();

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
        awaitingSchrodingerChoice = false;
        playerMakingSchrodingerChoice = null;
        Logger.debug("Schrödinger choice state reset. Current player for view: " + (this.currentPlayer != null ? this.currentPlayer.getName() : "None"));


        if (activeView instanceof SnakeLadderGameScene scene) {
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

    public void startGame() {
        Logger.info("startGame() called. Initializing game model.");
        gameModel.init(); // Model initializes and fires onGameStart
    }

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
        Logger.info("First player's turn: " + (this.currentPlayer != null ? this.currentPlayer.getName() : "None"));
        Logger.debug("onGameStart: Schrödinger state reset.");


        if (activeView instanceof SnakeLadderGameScene scene) {
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

    @Override
    public void onRoundPlayed(List<Integer> rolls, List<Player> players) {
        String rollsStr = rolls != null && !rolls.isEmpty() ? rolls.toString() : "N/A";
        // This could be INFO if you want a log per turn, or DEBUG if that's too much.
        // Let's keep it INFO for now as it's a significant game progression event.
        Logger.info("Round played. Player who acted: " + (this.currentPlayer != null ? this.currentPlayer.getName() : "Unknown") + ". Rolls: " + rollsStr);

        if (activeView == null) {
            Logger.warning("onRoundPlayed: activeView is null, UI updates will be skipped.");
            return;
        }

        Player playerWhoActed = this.currentPlayer; // This is the player who just finished their dice roll & initial move

        // Reset choice state before checking for new choices
        awaitingSchrodingerChoice = false;
        playerMakingSchrodingerChoice = null;
        if(activeView instanceof SnakeLadderGameScene scene) scene.hideSchrodingerChoice();
        Logger.debug("onRoundPlayed: Schrödinger choice state reset before tile action check.");


        if (playerWhoActed != null && playerWhoActed.getCurrentTile() != null) {
            Tile landedTile = playerWhoActed.getCurrentTile();
            Logger.debug("Player " + playerWhoActed.getName() + " landed on tile " + landedTile.getId() +
                ". Action: " +
                (landedTile.getAction() != null ? landedTile.getAction().getClass().getSimpleName() : "None"));

            if (landedTile.getAction() instanceof SchrodingerBoxAction schrodingerAction) {
                awaitingSchrodingerChoice = true;
                playerMakingSchrodingerChoice = playerWhoActed;
                Logger.info("Player " + playerWhoActed.getName() + " landed on a Schrödinger Box. Awaiting choice.");
                if (activeView instanceof SnakeLadderGameScene scene) {
                    scene.showSchrodingerChoice(playerWhoActed, schrodingerAction);
                    scene.setRollButtonEnabled(false);
                    assert rolls != null;
                    scene.updateDiceLabel(rolls.isEmpty() ? "" : String.valueOf(rolls.getFirst()));
                    scene.getBoardView().refresh();
                    scene.updatePlayerStatusDisplay();
                    Logger.debug("Schrödinger choice UI shown for " + playerWhoActed.getName());
                }
                return; // Await player choice
            }

            if (activeView instanceof MonopolyGameScene mScene) {
                mScene.updatePlayerStatusDisplay();
                mScene.getBoardView().refresh();
                if (monopolyService != null && landedTile.getAction() instanceof PropertyAction pa) {
                    Logger.debug("Player " + playerWhoActed.getName() + " landed on Monopoly property: " + pa.getName() + ". Handling property action.");
                    handleLandedOnProperty(playerWhoActed, pa, mScene);
                }
                //TODO: FIX ALERTS FOR OTHER SHIT
            }
        } else {
            Logger.warning("onRoundPlayed: playerWhoActed or their current tile is null. Player: " +
                (playerWhoActed != null ? playerWhoActed.getName() : "null"));
        }

        Logger.debug("No special choice pending from landing. Finalizing turn and setting up for next player.");
        finalizeTurnAndSetupNext(rolls);
    }


    private void finalizeTurnAndSetupNext(List<Integer> rollsIfApplicable) {
        Logger.debug("Entering finalizeTurnAndSetupNext.");
        String rollsStr = rollsIfApplicable != null && !rollsIfApplicable.isEmpty() ? rollsIfApplicable.toString() : "N/A";
        Logger.debug("Rolls from this turn (if applicable): " + rollsStr);

        if (gameModel.isFinished()) {
            Logger.info("Game is finished (detected in finalizeTurnAndSetupNext). Calling onGameOver.");
            onGameOver(gameModel.getWinner()); // This will log its own message
            if (activeView instanceof SnakeLadderGameScene scene) {
                if (rollsIfApplicable != null && !rollsIfApplicable.isEmpty()) scene.updateDiceLabel(String.valueOf(rollsIfApplicable.getFirst()));
                scene.getBoardView().refresh();
                scene.updatePlayerStatusDisplay();
                Logger.debug("SnakeLadderGameScene UI updated for final game state.");
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
            // Add similar DEBUG logging for Monopoly if UI updates are extensive
            // mScene.updateDiceLabel(...);
            // mScene.getBoardView().refresh();
            // mScene.updatePlayerStatusDisplay();
            // if (this.currentPlayer != null) mScene.highlightCurrentPlayer(this.currentPlayer);
            // mScene.setRollButtonEnabled(!gameModel.isFinished());
            Logger.debug("MonopolyGameScene UI updated for the next turn (placeholder).");
        }
    }

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

        // This is a key user action, so INFO seems appropriate
        Logger.info("Player " + this.currentPlayer.getName() + " initiated a dice roll.");

        if (activeView instanceof SnakeLadderGameScene scene) {
            scene.setRollButtonEnabled(false);
            scene.showGameMessage(this.currentPlayer.getName() + " is rolling...");
            Logger.debug("Roll button disabled for SnakeLadderGameScene during roll.");
        } else if (activeView instanceof MonopolyGameScene scene) {
            // scene.setRollButtonEnabled(false);
            Logger.debug("Roll button (Monopoly) disabled during roll (placeholder).");
        }

        Logger.debug("Executing gameModel.playTurn() for player: " + this.currentPlayer.getName());
        gameModel.playTurn(this.currentPlayer); // This call eventually triggers onRoundPlayed
    }

    public void handleObserveSchrodingerBoxRequest() {
        Logger.debug("Entering handleObserveSchrodingerBoxRequest.");
        if (!awaitingSchrodingerChoice || playerMakingSchrodingerChoice == null) {
            Logger.warning("ObserveSchrodingerBoxRequest called inappropriately. State: awaiting=" + awaitingSchrodingerChoice +
                ", player=" + (playerMakingSchrodingerChoice != null ? playerMakingSchrodingerChoice.getName() : "null"));
            return;
        }
        Logger.info("Player " + playerMakingSchrodingerChoice.getName() + " chose to OBSERVE the Schrödinger Box.");

        boolean goToStart = random.nextBoolean();
        Tile targetTile;
        String outcomeMessage;
        Board board = gameModel.getBoard();

        if (board == null || board.getTile(0) == null || board.getTiles().isEmpty()) {
            Logger.error("Schrödinger outcome error: Board or critical tiles (start/end) not available.");
            if (activeView instanceof SnakeLadderGameScene scene) {
                scene.showGameMessage("Error: Game board is in an invalid state for Schrödinger's Box!");
            }
            completeSchrodingerActionSequence(); // Attempt to gracefully exit
            return;
        }

        if (goToStart) {
            targetTile = board.getTile(0);
            outcomeMessage = playerMakingSchrodingerChoice.getName() + " opened the box... Oh no! Sent back to the start!";
            Logger.info("Schrödinger outcome (Observe): " + playerMakingSchrodingerChoice.getName() + " sent to START.");
        } else {
            int lastTileId = board.getTiles().size() - 1;
            targetTile = board.getTile(lastTileId);
            outcomeMessage = playerMakingSchrodingerChoice.getName() + " opened the box... Unbelievable! Sent straight to the finish line!";
            Logger.info("Schrödinger outcome (Observe): " + playerMakingSchrodingerChoice.getName() + " sent to FINISH.");
        }

        if (targetTile != null) {
            playerMakingSchrodingerChoice.setCurrentTile(targetTile);
            Logger.debug("Player " + playerMakingSchrodingerChoice.getName() + " moved to tile " + targetTile.getId() + " due to Schrödinger Box.");
        } else {
            outcomeMessage = playerMakingSchrodingerChoice.getName() + " opened the box... but the destination was unclear!";
            Logger.error("Schrödinger outcome error: Target tile for observe outcome was null. GoToStart: " + goToStart);
        }

        if (activeView instanceof SnakeLadderGameScene scene) {
            scene.showGameMessage(outcomeMessage);
        }

        completeSchrodingerActionSequence();
    }

    public void handleIgnoreSchrodingerBoxRequest() {
        Logger.debug("Entering handleIgnoreSchrodingerBoxRequest.");
        if (!awaitingSchrodingerChoice || playerMakingSchrodingerChoice == null) {
            Logger.warning("IgnoreSchrodingerBoxRequest called inappropriately. State: awaiting=" + awaitingSchrodingerChoice +
                ", player=" + (playerMakingSchrodingerChoice != null ? playerMakingSchrodingerChoice.getName() : "null"));
            return;
        }
        String message = playerMakingSchrodingerChoice.getName() + " cautiously decided to ignore the mysterious box.";
        Logger.info("Player " + playerMakingSchrodingerChoice.getName() + " chose to IGNORE the Schrödinger Box.");


        if (activeView instanceof SnakeLadderGameScene scene) {
            scene.showGameMessage(message);
        }
        completeSchrodingerActionSequence();
    }

    private void completeSchrodingerActionSequence() {
        Logger.debug("Completing Schrödinger action sequence for player: " +
            (playerMakingSchrodingerChoice != null ? playerMakingSchrodingerChoice.getName() : "Unknown"));
        awaitingSchrodingerChoice = false;

        finalizeTurnAndSetupNext(null); // Rolls not directly relevant here for dice display

        playerMakingSchrodingerChoice = null;
        Logger.debug("Schrödinger choice state fully reset after action.");
    }


    private void handleLandedOnProperty(Player player, PropertyAction propertyAction, MonopolyGameScene monopolyView) {
        Logger.info("Player " + player.getName() + " landed on property: " + propertyAction.getName() + ". Handling action.");

        if (monopolyService == null) {
            Logger.error("MonopolyService not available! Cannot handle property action for " + propertyAction.getName());
            monopolyView.showAlert("Error", "Service Unavailable", "Monopoly features are currently unavailable.", Alert.AlertType.ERROR);
            return;
        }

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
                                //If you are reading this you shall give us an A+ for this code
                                //ヾ(๑╹◡╹)ﾉ🔪
                                //or else...
                                //(PS: DETTE ER HUMOR - IKKE TRUE)
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
            }
        } else {
            Logger.debug("Player " + player.getName() + " landed on their own property: " + propertyAction.getName() + ". No rent/purchase action.");
        }
        monopolyView.updatePlayerStatusDisplay();
        Logger.debug("Monopoly player status display updated after property action handling.");
    }

    public BoardGame getGameModel() {
        // No logging needed for a simple getter usually
        return gameModel;
    }
}