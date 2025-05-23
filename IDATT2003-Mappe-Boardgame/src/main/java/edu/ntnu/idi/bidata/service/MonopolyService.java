package edu.ntnu.idi.bidata.service;

import edu.ntnu.idi.bidata.exception.InvalidParameterException;
import edu.ntnu.idi.bidata.model.BoardGame;
import edu.ntnu.idi.bidata.model.Card;
import edu.ntnu.idi.bidata.model.Player;
import edu.ntnu.idi.bidata.model.actions.monopoly.PropertyAction;
import edu.ntnu.idi.bidata.model.actions.monopoly.RailroadAction;
import edu.ntnu.idi.bidata.model.actions.monopoly.UtilityAction;
import edu.ntnu.idi.bidata.util.Logger;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Monopoly-specific game logic implementing the {@link GameService} interface.
 * Manages player turns, jail status, property ownership, card drawing, and game state for Monopoly.
 */
public class MonopolyService implements GameService {
    private int currentPlayerIndex = -1;
    private final Map<Player, Integer> jailedPlayers = new HashMap<>();
    private final Map<Player, List<PropertyAction>> playerProperties = new HashMap<>();
    private CardService cardService;
    private final Map<Player, Integer> getOutOfJailFreeCards = new HashMap<>();
    private BoardGame game; // Storing game reference from setup

    /**
     * Sets up the Monopoly game.
     * Initializes player money, starting positions, and clears any existing game state data.
     * Sets the current player to the first player in the list if players exist.
     *
     * @param game The {@link BoardGame} instance to set up.
     * @throws InvalidParameterException if the game is null.
     */
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

    /**
     * Gets the current player whose turn it is.
     *
     * @param game The {@link BoardGame} instance (parameter consistent with interface, though internal state is used).
     * @return The current {@link Player}, or null if no current player is set or players list is out of bounds.
     */
    @Override
    public Player getCurrentPlayer(BoardGame game) { // game param consistent with interface
        if (this.currentPlayerIndex >= 0 && this.currentPlayerIndex < this.game.getPlayers().size()) {
            return this.game.getPlayers().get(this.currentPlayerIndex);
        }
        return null;
    }

    /**
     * Plays a turn for the given player.
     * Handles dice rolling, player movement, jail logic (including using Get Out of Jail Free cards),
     * and advancing to the next player.
     *
     * @param game The {@link BoardGame} instance (parameter consistent with interface, though internal state is used).
     * @param player The {@link Player} whose turn it is.
     * @return The total dice roll for the turn. Returns 0 if the player is in jail and cannot move.
     * @throws IllegalArgumentException if the specified player is not in the game or it's not their turn according to this service.
     */
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


        // Roll the dice
        int totalRoll = this.game.getDice().rollDie();
        Logger.debug("Total roll: " + totalRoll);
        // We do not include doubles checking

        if (isInJail(player)) {
            // Check if player has Get Out of Jail Free card
            if (hasGetOutOfJailFreeCard(player)) {
                Logger.info(player.getName() + " uses Get Out of Jail Free card to get out of jail!");
                // Use the card (decrease count by 1)
                getOutOfJailFreeCards.put(player, getOutOfJailFreeCards.get(player) - 1);
                // Remove player from jail
                jailedPlayers.remove(player);
                // Player now gets to roll and move normally
            } else {
                Logger.info(player.getName() + " is in jail and cannot roll.");
                handleJailTurn(player);

                // If still in jail after handling, skip turn
                if (isInJail(player)) {
                    currentPlayerIndex = (currentPlayerIndex + 1) % this.game.getPlayers().size();
                    return 0; // Player cannot move
                }
            }
        }

        player.move(totalRoll); // This should trigger tile actions
        Logger.info(player.getName() + " (Money: $" + player.getMoney() + ") rolled " + totalRoll);
        currentPlayerIndex = (currentPlayerIndex + 1) % this.game.getPlayers().size();

        return totalRoll; // Or return individual rolls if controller needs them
    }

    /**
     * Checks if the Monopoly game has finished.
     * The game is considered finished if there is one or zero active (non-bankrupt) players.
     *
     * @param game The {@link BoardGame} instance.
     * @return true if the game has finished, false otherwise.
     */
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

    /**
     * Gets the winner of the Monopoly game.
     * The winner is the last remaining player with money.
     *
     * @param game The {@link BoardGame} instance.
     * @return The winning {@link Player}, or null if there is no winner yet (e.g., multiple players still active or game not finished).
     */
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

    /**
     * Sends a player to jail.
     * The player is typically jailed for 3 turns.
     *
     * @param player The {@link Player} to send to jail.
     */
    public void sendToJail(Player player) {
        jailedPlayers.put(player, 3); // Standard is 3 turns in jail
    }

    /**
     * Checks if a player is currently in jail.
     *
     * @param player The {@link Player} to check.
     * @return true if the player is in jail, false otherwise.
     */
    public boolean isInJail(Player player) {
        return jailedPlayers.containsKey(player);
    }

    /**
     * Handles a turn for a player who is in jail.
     * Decrements the remaining jail turns. If turns run out, the player is released.
     * Note: This method currently only handles turn decrement; full jail logic (rolling doubles, paying fine) is a TODO.
     *
     * @param player The {@link Player} in jail.
     */
    public void handleJailTurn(Player player) {
        if (isInJail(player)) {
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
     * @return true if the rent was successfully paid, false if the payer could not afford it.
     * @throws InvalidParameterException if payer, owner are null or amount is negative.
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
     * @return true if the purchase was successful, false otherwise (e.g., not enough money or property already owned).
     * @throws InvalidParameterException if player or property is null.
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

    /**
     * Adds a property to a player's list of owned properties.
     *
     * @param player The {@link Player} who now owns the property.
     * @param property The {@link PropertyAction} representing the property.
     */
    public void addProperty(Player player, PropertyAction property) {
        playerProperties.computeIfAbsent(player, p -> new ArrayList<>()).add(property);
    }

    /**
     * Gets the number of railroads owned by a specific player.
     *
     * @param player The {@link Player} to check.
     * @return The count of railroads owned by the player.
     */
    public int getRailroadsOwnedCount(Player player) {
        return (int) playerProperties.getOrDefault(player, List.of())
                .stream()
                .filter(p -> p instanceof RailroadAction)
                .count();
    }

    /**
     * Gets the number of utilities owned by a specific player.
     *
     * @param player The {@link Player} to check.
     * @return The count of utilities owned by the player.
     */
    public int getUtilitiesOwnedCount(Player player) {
        return (int) playerProperties.getOrDefault(player, List.of())
                .stream()
                .filter(p -> p instanceof UtilityAction)
                .count();
    }


    /**
     * Sets the {@link CardService} to be used for drawing Chance and Community Chest cards.
     *
     * @param cardService The {@link CardService} instance.
     */
    public void setCardService(CardService cardService) {
        this.cardService = cardService;
    }

    /**
     * Draws a Chance card for the player and executes its action.
     *
     * @param player The {@link Player} drawing the card.
     * @return The drawn {@link Card}.
     */
    public Card drawChanceCard(Player player) {
        Card card = cardService.drawCard("chance");
        executeCardAction(card, player);
        return card;
    }

    /**
     * Draws a Community Chest card for the player and executes its action.
     *
     * @param player The {@link Player} drawing the card.
     * @return The drawn {@link Card}.
     */
    public Card drawCommunityChestCard(Player player) {
        Card card = cardService.drawCard("communityChest");
        executeCardAction(card, player);
        return card;
    }

    /**
     * Gives a "Get Out of Jail Free" card to the specified player.
     *
     * @param player The {@link Player} receiving the card.
     */
    public void giveGetOutOfJailFreeCard(Player player) {
        getOutOfJailFreeCards.put(player, getOutOfJailFreeCards.getOrDefault(player, 0) + 1);
    }

    /**
     * Checks if a player has a "Get Out of Jail Free" card.
     *
     * @param player The {@link Player} to check.
     * @return true if the player has one or more cards, false otherwise.
     */
    public boolean hasGetOutOfJailFreeCard(Player player) {
        return getOutOfJailFreeCards.getOrDefault(player, 0) > 0;
    }

    /**
     * Executes the action associated with a drawn card.
     * Handles various card types like movement, payments, receiving money, and jail-related actions.
     *
     * @param card The {@link Card} whose action is to be executed.
     * @param player The {@link Player} affected by the card.
     */
    private void executeCardAction(Card card, Player player) {
        Logger.info(player.getName() + " drew: " + card.getDescription());

        switch (card.getType()) {
            // Movement cards
            case "AdvanceToGo":
                player.setCurrentTile(game.getBoard().getTile(0));
                player.increaseMoney(200); // Collect $200 for passing GO
                Logger.info(player.getName() + " advances to GO and collects $200");
                break;
            case "AdvanceToIllinoisAve":
                // Find Illinois Avenue position
               game.getBoard().getTiles().forEach((key,tile) -> {
                   if (tile.getAction() instanceof PropertyAction pa) {
                       if (pa.getName().equals("Illinois Avenue")) {
                           player.setCurrentTile(tile);
                           Logger.info(player.getName() + " advanced to Illinois Avenue");
                       }
                   }
               });
                break;
            case "GoBack":
                int spaces = card.getIntProperty("spaces", 3);
                player.move(-spaces);
                Logger.info(player.getName() + " moved back " + spaces + " spaces");
                break;

            // Special cards
            case "GetOutOfJailFree":
                giveGetOutOfJailFreeCard(player);
                Logger.info(player.getName() + " received a Get Out of Jail Free card");
                break;
            case "GoToJail":
                sendToJail(player);
                Logger.info(player.getName() + " was sent to Jail");
                break;

            // Money-related cards (player pays)
            case "PayTax":
            case "PayPoorTax":
            case "HospitalFees":
            case "SchoolFees":
            case "DoctorFees":
                int amount = card.getIntProperty("amount", 0);
                if (player.getMoney() >= amount) {
                    player.decreaseMoney(amount);
                    Logger.info(player.getName() + " paid $" + amount);
                } else {
                    Logger.info(player.getName() + " cannot afford to pay $" + amount);
                    // Handle player bankruptcy
                }
                break;

            // Money-related cards (player receives)
            case "BankPaysYou":
            case "BankErrorInYourFavor":
            case "BuildingLoanMatures":
            case "CrosswordCompetition":
            case "SaleOfStock":
            case "HolidayFundMatures":
            case "IncomeTaxRefund":
            case "LifeInsuranceMatures":
            case "ReceiveConsultancyFee":
            case "BeautyContest":
                int receiveAmount = card.getIntProperty("amount", 0);
                player.increaseMoney(receiveAmount);
                Logger.info(player.getName() + " received $" + receiveAmount);
                break;

            // Money-related cards (player & other players)
            case "ChairmanOfBoard":
                int payAmount = card.getIntProperty("amount", 50);
                int totalPaid = 0;
                totalPaid = handleCardRentActionSpecialCase(player, payAmount, totalPaid);
                player.increaseMoney(totalPaid);
                Logger.info(player.getName() + " paid $" + payAmount + " to each player as Chairman of the Board");
                break;
            case "GrandOperaNight":
            case "ItsYourBirthday":
                int collectAmount = card.getIntProperty("amount", 10);
                int totalCollected = 0;
                totalCollected = handleCardRentActionSpecialCase(player, collectAmount, totalCollected);
                player.increaseMoney(totalCollected);
                Logger.info(player.getName() + " collected $" + collectAmount + " from each player");
                break;

            default:
                Logger.warning("Card type not implemented: " + card.getType());

        }

    }

    /**
     * Handles special card actions where a player pays or collects money from all other players.
     * For example, "Chairman of the Board" or "Grand Opera Night".
     *
     * @param player The {@link Player} initiating the action (either paying to others or collecting from others).
     * @param amountPerPlayer The amount each other player pays or receives.
     * @param totalAccumulated The initial accumulated amount (usually 0, used for recursive or iterative summing).
     * @return The total amount paid by or collected from other players.
     */
    private int handleCardRentActionSpecialCase(Player player, int amountPerPlayer, int totalAccumulated) {
        for (Player otherPlayer : game.getPlayers()) {
            if (!otherPlayer.equals(player)) {
                if (otherPlayer.getMoney() >= amountPerPlayer) {
                    otherPlayer.decreaseMoney(amountPerPlayer);
                    totalAccumulated += amountPerPlayer;
                } else {
                    totalAccumulated += otherPlayer.getMoney();
                    otherPlayer.decreaseMoney(otherPlayer.getMoney());
                }
            }
        }
        return totalAccumulated;
    }

}
