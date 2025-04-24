package edu.ntnu.idi.bidata.controller;

import edu.ntnu.idi.bidata.model.BoardGame;
import edu.ntnu.idi.bidata.model.BoardGameObserver;
import edu.ntnu.idi.bidata.model.Player;

import java.util.ArrayList;
import java.util.List;

/**
 * Orchestrates game flow for UI: subscribes to model events and forwards them to UI listeners.
 */
public class GameController implements BoardGameObserver {
    private final BoardGame game;
    private final List<GameListener> listeners = new ArrayList<>();

    /**
     * UI-level listener interface (your existing callbacks).
     */
    public interface GameListener {
        void onGameStart(List<Player> players);
        void onRoundPlayed(List<Integer> rolls, List<Player> players);
        void onGameOver(Player winner);
    }

    public GameController(BoardGame game) {
        this.game = game;
        // Register as an observer on the model
        game.addObserver(this);
    }

    /** UI registers here to get callbacks. */
    public void addListener(GameListener listener) {
        listeners.add(listener);
    }

    /** (Optional) UI can unregister too. */
    public void removeListener(GameListener listener) {
        listeners.remove(listener);
    }

    // ---------------------------------------------------
    // BoardGameObserver callbacks (fired by the model):
    // ---------------------------------------------------

    @Override
    public void onGameStart(List<Player> players) {
        listeners.forEach(l -> l.onGameStart(players));
    }

    @Override
    public void onRoundPlayed(List<Integer> rolls, List<Player> players) {
        listeners.forEach(l -> l.onRoundPlayed(rolls, players));
    }

    @Override
    public void onGameOver(Player winner) {
        listeners.forEach(l -> l.onGameOver(winner));
    }

    /** Kick off the game; the model will call back onGameStart(...) */
    public void startGame() {
        game.init();
    }

    /**
     * Plays a turn for a specific player
     *
     * @param player The player who will take their turn
     */
    public void playTurn(Player player) {
        game.playTurn(player);
    }

    public BoardGame getGame() {
        return game;
    }
}
