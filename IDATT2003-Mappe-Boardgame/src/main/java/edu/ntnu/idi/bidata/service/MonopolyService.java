package edu.ntnu.idi.bidata.service;

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

public class MonopolyService implements GameService {

    private Map<Player, Integer> jailedPlayers = new HashMap<>(); // Player -> remaining turns in jail

    private Map<Player, List<PropertyAction>> playerProperties = new HashMap<>(); // Player -> list of properties owned
    // Add to MonopolyService.java

    private CardService cardService;
    private Map<Player, Integer> getOutOfJailFreeCards = new HashMap<>();

    private BoardGame game;

    @Override
    public void setup(BoardGame game) {
        // Monopoly-specific setup
        // Place players at starting position, initialize money, etc.

        // Example: Set all players to start with $1500

        for (Player player : game.getPlayers()) {
            player.setMoney(1500);
            player.setTile(game.getBoard().getTile(0));
        }

        this.game = game;
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
    public int playTurn(BoardGame game, Player player) {
        // Handle jail logic first
        if (isInJail(player)) {
            // Check if player has Get Out of Jail Free card
            if (hasGetOutOfJailFreeCard(player)) {
                // Use the card and remove it
                getOutOfJailFreeCards.put(player, getOutOfJailFreeCards.get(player) - 1);
                jailedPlayers.remove(player);
            } else {
                // Handle jail turn (try to roll doubles, pay fine, etc.)
                handleJailTurn(player);

                // If still in jail after handling, skip movement
                if (isInJail(player)) {
                    return 0;
                }
            }
        }

        // Roll the dice
        int roll = game.getDice().roll();

        // Move the player (this triggers land() which executes tile actions)
        player.move(roll);

        return roll;
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
        // If game isn't finished yet
        if (!isFinished(game)) {
            return null;
        }

        // Find the non-bankrupt player
        for (Player player : game.getPlayers()) {
            if (player.getMoney() > 0) {
                return player;
            }
        }

        // Fallback - if everyone is bankrupt, return player with most assets
        // (shouldn't happen in normal gameplay)
        Player wealthiest = null;
        int maxWealth = Integer.MIN_VALUE;

        for (Player player : game.getPlayers()) {
            int totalAssets = calculateTotalAssets(player);
            if (totalAssets > maxWealth) {
                maxWealth = totalAssets;
                wealthiest = player;
            }
        }

        return wealthiest;
    }

    // Helper method to calculate a player's total assets
    private int calculateTotalAssets(Player player) {
        int total = player.getMoney();

        // Add property values
        List<PropertyAction> properties = playerProperties.getOrDefault(player, List.of());
        for (PropertyAction property : properties) {
            total += property.getCost(); // Use purchase price as approximation
        }

        return total;
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
                player.setCurrent(game.getBoard().getTile(0)); // Go tile
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
