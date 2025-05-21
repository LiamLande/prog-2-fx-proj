package edu.ntnu.idi.bidata.service;

import edu.ntnu.idi.bidata.exception.InvalidParameterException;
import edu.ntnu.idi.bidata.model.BoardGame;
import edu.ntnu.idi.bidata.model.Card;
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
    private int currentPlayerIndex = -1;
    private Map<Player, Integer> jailedPlayers = new HashMap<>();
    private Map<Player, List<PropertyAction>> playerProperties = new HashMap<>();
    private CardService cardService;
    private Map<Player, Integer> getOutOfJailFreeCards = new HashMap<>();
    private BoardGame game; // Storing game reference from setup

    @Override
    public void setup(BoardGame game) {
        this.game = game; // Store the game instance
        if (game == null) {
            throw new InvalidParameterException("Game cannot be null in MonopolyService.setup");
        }
        for (Player player : game.getPlayers()) {
            player.setMoney(1500);
            player.setCurrentTile(game.getBoard().getTile(0)); // Use setCurrentTile
        }
        playerProperties.clear();
        jailedPlayers.clear();
        getOutOfJailFreeCards.clear();

        if (!game.getPlayers().isEmpty()) {
            this.currentPlayerIndex = 0;
        } else {
            this.currentPlayerIndex = -1;
        }
    }

    @Override
    public Player getCurrentPlayer(BoardGame game) { // game param consistent with interface
        if (this.currentPlayerIndex >= 0 && this.currentPlayerIndex < this.game.getPlayers().size()) {
            return this.game.getPlayers().get(this.currentPlayerIndex);
        }
        return null;
    }

    // playOneRound for Monopoly might be complex (doubles, multiple turns)
    // For now, let's assume playTurn is the primary mode of advancement.
    @Override
    public List<Integer> playOneRound(BoardGame game) {
        // This might need careful thought for Monopoly due to rules like rolling doubles.
        // Often, Monopoly games proceed one 'playTurn' at a time.
        // If you implement this, ensure currentPlayerIndex is managed correctly.
        throw new UnsupportedOperationException("playOneRound for Monopoly needs specific rule implementation (e.g., doubles). Use playTurn.");
    }


    @Override
    public int playTurn(BoardGame game, Player player) { // `game` param from interface
        // Ensure the 'player' passed is indeed the current one
        if (this.game.getPlayers().isEmpty() || !player.equals(this.game.getPlayers().get(this.currentPlayerIndex))) {
            int newIndex = this.game.getPlayers().indexOf(player);
            if (newIndex == -1) {
                throw new IllegalArgumentException("Player " + player.getName() + " is not in the game or not their turn according to MonopolyService.");
            }
            this.currentPlayerIndex = newIndex; // Sync if controller is authoritative on who is passed
        }

        // ... (existing jail logic, dice roll, player.move()) ...
        // Roll the dice
        int roll1 = this.game.getDice().rollDie(); // Assuming Dice has rollDie() for single die
        int roll2 = this.game.getDice().rollDie(); // And Monopoly dice has 2
        int totalRoll = roll1 + roll2;
        boolean rolledDoubles = (roll1 == roll2);

        // TODO: Handle doubles logic (another turn, or go to jail on 3rd double)

        player.move(totalRoll); // This should trigger tile actions
        System.out.println(player.getName() + " (Money: $" + player.getMoney() + ") rolled " + roll1 + "+" + roll2 + "=" + totalRoll + (rolledDoubles ? " (Doubles!)" : ""));


        // Advance to the next player for the *next* turn
        // UNLESS player rolled doubles and isn't jailed for it (Monopoly rule)
        if (!rolledDoubles || /* condition for going to jail on 3rd double */ false) {
            if (!isFinished(this.game) && !this.game.getPlayers().isEmpty()) {
                this.currentPlayerIndex = (this.currentPlayerIndex + 1) % this.game.getPlayers().size();
            }
        } else {
            // Player rolled doubles, gets another turn (currentPlayerIndex remains the same)
            System.out.println(player.getName() + " rolled doubles, gets another turn!");
        }

        return totalRoll; // Or return individual rolls if controller needs them
    }

    @Override
    public boolean isFinished(BoardGame game) {
        // Count active (non-bankrupt) players
        int activePlayers = 0;

        for (Player player : game.getPlayers()) {
            if (player.getMoney() > 0) {
                activePlayers++;
            }
        }

        // Game is finished when only one player remains solvent
        // or after a maximum number of rounds (optional)
        return activePlayers <= 1;
    }

    @Override
    public Player getWinner(BoardGame game) {
        // Find the non-bankrupt player
        for (Player player : game.getPlayers()) {
            if (player.getMoney() > 0) {
                return player;
            }
        }
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

    /**
     * Handles the payment of rent from one player to another.
     * @param payer The player who needs to pay rent.
     * @param owner The owner of the property receiving rent.
     * @param amount The amount of rent to be paid.
     * @return true if the rent was successfully paid, false if the payer could not afford it (and is potentially bankrupt or needs to mortgage).
     */
    public boolean payRent(Player payer, Player owner, int amount) {
        if (payer == null || owner == null) {
            throw new InvalidParameterException("Payer and Owner cannot be null for paying rent.");
        }
        if (amount < 0) {
            throw new InvalidParameterException("Rent amount cannot be negative.");
        }

        if (payer.getMoney() >= amount) {
            try {
                payer.decreaseMoney(amount);
                owner.increaseMoney(amount);
                System.out.println(payer.getName() + " paid $" + amount + " rent to " + owner.getName());
                return true;
            } catch (InvalidParameterException e) {
                // This should ideally not happen if getMoney() check passed, but good for safety
                System.err.println("Error during rent payment (unexpected): " + e.getMessage());
                return false; // Or rethrow as a runtime exception if this indicates a logic flaw
            }
        } else {
            System.out.println(payer.getName() + " cannot afford to pay $" + amount + " rent.");
            //player is bankrupt game is over
            payer.decreaseMoney(payer.getMoney());
            return false;
        }
    }

    /**
     * Handles the purchase of a property by a player.
     * @param player The player purchasing the property.
     * @param property The property being purchased.
     * @return true if the purchase was successful, false otherwise (e.g., not enough money).
     */
    public boolean purchaseProperty(Player player, PropertyAction property) {
        if (player == null || property == null) {
            throw new InvalidParameterException("Player and Property cannot be null for purchase.");
        }
        if (property.getOwner() != null) {
            System.err.println("Attempt to purchase already owned property: " + property.getName());
            return false; // Property already owned
        }

        if (player.getMoney() >= property.getCost()) {
            try {
                player.decreaseMoney(property.getCost());
                property.setOwner(player);
                addProperty(player, property); // Your existing method to track player properties
                System.out.println(player.getName() + " purchased " + property.getName() + " for $" + property.getCost());
                return true;
            } catch (InvalidParameterException e) {
                System.err.println("Error during property purchase (unexpected): " + e.getMessage());
                return false;
            }
        } else {
            System.out.println(player.getName() + " cannot afford to purchase " + property.getName());
            return false;
        }
    }

    // You might also need a method for when a player cannot pay rent and becomes bankrupt
    public boolean handleBankruptcy(Player player) {
        if (player.getMoney() < 0) { // Or if they couldn't pay a required fee
            System.out.println(player.getName() + " is bankrupt!");
            // TODO: Logic to remove player from game, return their properties to the bank, etc.
            // For now, just a marker.
            // game.getPlayers().remove(player); // This would need BoardGame to have a removePlayer method
            // And this would also need to be careful about modifying list while iterating.
            return true;
        }
        return false;
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


    public void setCardService(CardService cardService) {
        this.cardService = cardService;
    }

    public Card drawChanceCard(Player player) {
        Card card = cardService.drawCard("chance");
        executeCardAction(card, player);
        return card;
    }

    public Card drawCommunityChestCard(Player player) {
        Card card = cardService.drawCard("communityChest");
        executeCardAction(card, player);
        return card;
    }

    public void giveGetOutOfJailFreeCard(Player player) {
        getOutOfJailFreeCards.put(player, getOutOfJailFreeCards.getOrDefault(player, 0) + 1);
    }

    public boolean hasGetOutOfJailFreeCard(Player player) {
        return getOutOfJailFreeCards.getOrDefault(player, 0) > 0;
    }

    private void executeCardAction(Card card, Player player) {
        System.out.println("Card: " + card.getDescription());

        switch (card.getType()) {
            case "AdvanceToGo":
                player.setCurrentTile(game.getBoard().getTile(0)); // Go tile
                break;
            case "GetOutOfJailFree":
                giveGetOutOfJailFreeCard(player);
                break;
            case "GoToJail":
                sendToJail(player);
                break;
            // Implement other card types
            default:
                System.out.println("Card type not implemented: " + card.getType());
        }
    }
}
