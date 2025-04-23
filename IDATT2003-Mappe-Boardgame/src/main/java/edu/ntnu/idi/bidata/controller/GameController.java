package edu.ntnu.idi.bidata.controller;

import edu.ntnu.idi.bidata.model.BoardGame;
import edu.ntnu.idi.bidata.model.Player;
import java.util.List;

/**
 * Orchestrates game flow for UI: handles user actions and notifies listeners of game events.
 */
public class GameController {
    private final BoardGame game;
    private final List<GameListener> listeners;

    public GameController(BoardGame game) {
        this.game = game;
        this.listeners = new java.util.ArrayList<>();
    }

    public interface GameListener {
        void onGameStart(List<Player> players);
        void onRoundPlayed(List<Integer> rolls, List<Player> players);
        void onGameOver(Player winner);
    }

    public void addListener(GameListener listener) {
        listeners.add(listener);
    }

    /**
     * Starts the game: notifies UI and plays until finished.
     */
    public void startGame() {
        game.init();
        listeners.forEach(l -> l.onGameStart(game.getPlayers()));
        if (game.isFinished()) {
            listeners.forEach(l -> l.onGameOver(game.getWinner()));
        }
    }

    /**
     * Plays a single round and notifies listeners.
     */
    public void playOneRound() {
        List<Integer> rolls = game.playOneRound();
        listeners.forEach(l -> l.onRoundPlayed(rolls, game.getPlayers()));
        if (game.isFinished()) {
            listeners.forEach(l -> l.onGameOver(game.getWinner()));
        }
    }
}