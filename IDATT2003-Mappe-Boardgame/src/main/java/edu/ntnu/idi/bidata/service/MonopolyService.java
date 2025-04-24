package edu.ntnu.idi.bidata.service;

import edu.ntnu.idi.bidata.model.BoardGame;
import edu.ntnu.idi.bidata.model.Player;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MonopolyService implements GameService {
    // Add to MonopolyService.java
    private Map<Player, Integer> jailedPlayers = new HashMap<>(); // Player -> remaining turns in jail


    @Override
    public void setup(BoardGame game) {
        // Monopoly-specific setup
        // Place players at starting position, initialize money, etc.

        // Example: Set all players to start with $1500

        for (Player player : game.getPlayers()) {
            player.setMoney(1500);
            player.setPosition(0);
        }

    }

    @Override
    public List<Integer> playOneRound(BoardGame game) {
        // Players roll dice, move and perform Monopoly-specific actions (buying properties, paying rent, etc.)
        List<Integer> rolls = new ArrayList<>();
        for (Player player : game.getPlayers()) {
            int roll = game.getDice().roll();
            rolls.add(roll);
            player.move(roll);

            // TODO: Perform actions based on the new position
            // Example: Check if the player lands on a property, chance, community chest, etc.
            // PropertyAction.perform(player);  // Placeholder for actual action handling
        }


        return null;   // Placeholder for actual implementation
    }

    @Override
    public boolean isFinished(BoardGame game) {
        // TODO: Define ending condition (e.g., when a player is bankrupt or after N rounds)
        return false;
    }

    @Override
    public Player getWinner(BoardGame game) {
        // TODO: Define winner (e.g., player with most properties/money)
        return null;
    }


    public void sendToJail(Player player) {
        jailedPlayers.put(player, 3); // Standard is 3 turns in jail
    }

    public boolean isInJail(Player player) {
        return jailedPlayers.containsKey(player);
    }

    public void handleJailTurn(Player player) {
        if (isInJail(player)) {
            // TODO: Handle jail turn logic (rolling doubles, paying fine, etc.)
            int remainingTurns = jailedPlayers.get(player);
            if (remainingTurns <= 1) {
                jailedPlayers.remove(player);
            } else {
                jailedPlayers.put(player, remainingTurns - 1);
            }
        }
    }
}
