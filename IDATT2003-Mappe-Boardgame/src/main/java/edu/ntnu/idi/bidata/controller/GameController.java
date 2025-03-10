package edu.ntnu.idi.bidata.controller;

import edu.ntnu.idi.bidata.model.BoardGame;
import edu.ntnu.idi.bidata.model.Player;
import edu.ntnu.idi.bidata.model.Tile;

import java.util.ArrayList;
import java.util.List;

/**
 * Controller for the Snakes and Ladders game
 */
public class GameController {

    private BoardGame game;
    private List<Player> players;
    private int currentPlayerIndex;
    private boolean gameRunning;

    private List<GameListener> listeners = new ArrayList<>();

    /**
     * Constructor
     *
     * @param game The board game instance
     */
    public GameController(BoardGame game) {
        this.game = game;
        this.currentPlayerIndex = 0;
        this.gameRunning = false;

        // Get players from the game
        this.players = getPlayersFromGame();
    }

    /**
     * Interface for game event listeners
     */
    public interface GameListener {
        void onRollDice(Player player, int roll);
        void onPlayerMove(Player player, int fromTile, int toTile);
        void onSpecialAction(Player player, String actionType, int fromTile, int toTile);
        void onGameOver(Player winner);
        void onNextPlayer(Player player);
    }

    /**
     * Add a listener for game events
     *
     * @param listener The listener to add
     */
    public void addGameListener(GameListener listener) {
        listeners.add(listener);
    }

    /**
     * Start the game
     */
    public void startGame() {
        if (players.size() < 2) {
            throw new IllegalStateException("Game needs at least 2 players");
        }

        gameRunning = true;
        currentPlayerIndex = 0;

        // Notify listeners that the first player is now active
        notifyNextPlayer();
    }

    /**
     * Roll the dice and move the current player
     *
     * @return The dice roll result
     */
    public int rollDiceAndMove() {
        if (!gameRunning) {
            throw new IllegalStateException("Game is not running");
        }

        // Get current player
        Player player = players.get(currentPlayerIndex);

        // Store current tile before moving
        int fromTile = player.getCurrentTile().getTileId();

        // Roll dice
        int roll = game.getDice().roll();

        // Notify listeners about dice roll
        notifyRollDice(player, roll);

        // Calculate expected destination (before any snake/ladder)
        int expectedDestination = Math.min(fromTile + roll, 99);

        // Move player
        player.move(roll);

        // Get new tile after moving
        int toTile = player.getCurrentTile().getTileId();

        // Notify listeners about player movement
        notifyPlayerMove(player, fromTile, expectedDestination);

        // Check if a special action (snake or ladder) occurred
        if (toTile != expectedDestination) {
            String actionType = toTile > expectedDestination ? "ladder" : "snake";
            notifySpecialAction(player, actionType, expectedDestination, toTile);
        }

        // Check if game is over
        if (toTile >= 99) {
            gameRunning = false;
            notifyGameOver(player);
        } else {
            // Move to next player
            currentPlayerIndex = (currentPlayerIndex + 1) % players.size();
            notifyNextPlayer();
        }

        return roll;
    }

    /**
     * Notify listeners that a player has rolled the dice
     *
     * @param player The player who rolled
     * @param roll The dice roll result
     */
    private void notifyRollDice(Player player, int roll) {
        for (GameListener listener : listeners) {
            listener.onRollDice(player, roll);
        }
    }

    /**
     * Notify listeners that a player has moved
     *
     * @param player The player who moved
     * @param fromTile The starting tile
     * @param toTile The ending tile
     */
    private void notifyPlayerMove(Player player, int fromTile, int toTile) {
        for (GameListener listener : listeners) {
            listener.onPlayerMove(player, fromTile, toTile);
        }
    }

    /**
     * Notify listeners that a special action occurred
     *
     * @param player The player affected
     * @param actionType The type of action ("snake" or "ladder")
     * @param fromTile The starting tile
     * @param toTile The ending tile
     */
    private void notifySpecialAction(Player player, String actionType, int fromTile, int toTile) {
        for (GameListener listener : listeners) {
            listener.onSpecialAction(player, actionType, fromTile, toTile);
        }
    }

    /**
     * Notify listeners that the game is over
     *
     * @param winner The winning player
     */
    private void notifyGameOver(Player winner) {
        for (GameListener listener : listeners) {
            listener.onGameOver(winner);
        }
    }

    /**
     * Notify listeners that it's the next player's turn
     */
    private void notifyNextPlayer() {
        for (GameListener listener : listeners) {
            listener.onNextPlayer(players.get(currentPlayerIndex));
        }
    }

    /**
     * Get the current player
     *
     * @return The current player
     */
    public Player getCurrentPlayer() {
        return players.get(currentPlayerIndex);
    }

    /**
     * Get all players in the game
     *
     * @return List of players
     */
    public List<Player> getPlayers() {
        return new ArrayList<>(players);
    }

    /**
     * Get the game board
     *
     * @return The board game instance
     */
    public BoardGame getGame() {
        return game;
    }

    /**
     * Check if the game is running
     *
     * @return True if the game is running, false otherwise
     */
    public boolean isGameRunning() {
        return gameRunning;
    }

    /**
     * Get players from the game instance
     * This uses reflection since the BoardGame class doesn't expose the players list directly
     *
     * @return List of players
     */
    private List<Player> getPlayersFromGame() {
        try {
            java.lang.reflect.Field playersField = BoardGame.class.getDeclaredField("players");
            playersField.setAccessible(true);
            return (List<Player>) playersField.get(game);
        } catch (Exception e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }
}