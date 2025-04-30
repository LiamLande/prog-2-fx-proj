package edu.ntnu.idi.bidata.service;

import edu.ntnu.idi.bidata.exception.InvalidParameterException;
import edu.ntnu.idi.bidata.model.BoardGame;
import edu.ntnu.idi.bidata.model.Player;
import edu.ntnu.idi.bidata.model.actions.monopoly.PropertyAction;
import edu.ntnu.idi.bidata.model.actions.monopoly.RailroadAction;
import edu.ntnu.idi.bidata.model.actions.monopoly.UtilityAction;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Monopoly-specific game logic.
 * <p>
 * Currently a stub: all methods throw UnsupportedOperationException
 * until you fill in actual Monopoly rules (rolling doubles, buying
 * houses, jail, auction, bankruptcies, etc.).
 */
public class MonopolyService implements GameService {

    private Map<Player, Integer> jailedPlayers = new HashMap<>(); // Player -> remaining turns in jail

    private Map<Player, List<PropertyAction>> playerProperties = new HashMap<>(); // Player -> list of properties owned

    @Override
    public void setup(BoardGame game) {
        // Monopoly-specific setup
        // Place players at starting position, initialize money, etc.

        // Example: Set all players to start with $1500

        for (Player player : game.getPlayers()) {
            player.setMoney(1500);
            player.setTile(game.getBoard().getTile(0));
        }

        if (game == null) {
            throw new InvalidParameterException("Game cannot be null");
        }
        // TODO: Place players on GO, give $1500, clear properties, etc.
        throw new UnsupportedOperationException("MonopolyService.setup() not implemented");
    }

    @Override
    public List<Integer> playOneRound(BoardGame game) {
        if (game == null) {
            throw new InvalidParameterException("Game cannot be null");
        }
        List<Integer> rolls = new ArrayList<>();
        // TODO: Loop through players, handle doubles, moves, property logic...
        throw new UnsupportedOperationException("MonopolyService.playOneRound() not implemented");
        // return rolls;
        // Players roll dice, move and perform Monopoly-specific actions (buying properties, paying rent, etc.)
        for (Player player : game.getPlayers()) {
            int roll = game.getDice().roll();
            rolls.add(roll);
            player.move(roll);

            // TODO: Perform actions based on the new position
            // Example: Check if the player lands on a property, chance, community chest, etc.
            // PropertyAction.perform(player);  // Placeholder for actual action handling
        }
    }

    @Override
    public int playTurn(BoardGame game, Player player) {
        if (game == null || player == null) {
            throw new InvalidParameterException("Game and player must not be null");
        }
        // TODO: Roll two dice, move, handle landing logic, doubles/jail...
        throw new UnsupportedOperationException("MonopolyService.playTurn() not implemented");
        // return rollValue;
    }

    @Override
    public int playTurn(BoardGame game, Player player) {
        return 0;
    }

    @Override
    public boolean isFinished(BoardGame game) {
        // TODO: Define ending condition (e.g., when a player is bankrupt or after N rounds)
        if (game == null) {
            throw new InvalidParameterException("Game cannot be null");
        }
        // TODO: Return true when only one player remains solvent, or after N rounds
        throw new UnsupportedOperationException("MonopolyService.isFinished() not implemented");
    }

    @Override
    public Player getWinner(BoardGame game) {
        // TODO: Define winner (e.g., player with most properties/money)
        if (game == null) {
            throw new InvalidParameterException("Game cannot be null");
        }
        // TODO: Identify the richest player or last player standing
        throw new UnsupportedOperationException("MonopolyService.getWinner() not implemented");
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

    public void addProperty(Player player, PropertyAction property) {
        playerProperties.computeIfAbsent(player, p -> new ArrayList<>()).add(property);
    }

    public int getRailroadsOwnedCount(Player player) {
        return (int) playerProperties.getOrDefault(player, List.of())
                .stream()
                .filter(p -> p instanceof RailroadAction)
                .count();
    }

    public int getUtilitiesOwnedCount(Player player) {
        return (int) playerProperties.getOrDefault(player, List.of())
                .stream()
                .filter(p -> p instanceof UtilityAction)
                .count();
    }
}
