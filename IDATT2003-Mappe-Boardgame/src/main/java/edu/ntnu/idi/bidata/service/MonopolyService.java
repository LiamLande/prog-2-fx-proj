package edu.ntnu.idi.bidata.service;

import edu.ntnu.idi.bidata.model.BoardGame;
import edu.ntnu.idi.bidata.model.Player;

import java.util.ArrayList;
import java.util.List;

public class MonopolyService implements GameService {
    @Override
    public void setup(BoardGame game) {
        // Monopoly-specific setup
        // Place players at starting position, initialize money, etc.

        // Example: Set all players to start with $1500

    }

    @Override
    public List<Integer> playOneRound(BoardGame game) {
        // Players roll dice, move and perform Monopoly-specific actions (buying properties, paying rent, etc.)
        return null;   // Placeholder for actual implementation
    }

    @Override
    public boolean isFinished(BoardGame game) {
        // Define ending condition (e.g., when a player is bankrupt or after N rounds)
        return false;
    }

    @Override
    public Player getWinner(BoardGame game) {
        // Define winner (e.g., player with most properties/money)
        return null;
    }
}
