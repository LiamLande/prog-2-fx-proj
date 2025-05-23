package edu.ntnu.idi.bidata.model.actions.monopoly;

import edu.ntnu.idi.bidata.model.Player;
import edu.ntnu.idi.bidata.service.MonopolyService;
import edu.ntnu.idi.bidata.service.ServiceLocator;
import edu.ntnu.idi.bidata.util.Logger;

/**
 * Represents a Utility property action in a Monopoly-like game (e.g., Water Works, Electric Company).
 * This class extends {@link PropertyAction} and customizes the behavior for utilities.
 * The rent for a utility is calculated based on the player's dice roll and the number of utilities
 * owned by the proprietor: 4 times the dice roll if one utility is owned, and 10 times the dice roll
 * if multiple (typically two) utilities are owned by the same player.
 * The purchase logic is generally handled by a higher-level service or UI component.
 */
public class UtilityAction extends PropertyAction {
    /**
     * Constructs a new UtilityAction.
     * The base rent in the superclass constructor is set to 0, as utility rent is dynamic and
     * calculated based on dice rolls and number of utilities owned.
     *
     * @param name The name of the utility (e.g., "Electric Company").
     * @param cost The cost to purchase the utility.
     */
    public UtilityAction(String name, int cost) {
        super(name, cost, 0); // Base rent is 0, actual rent is dynamic.
    }

    /**
     * Performs the action when a player lands on this utility tile.
     * <ul>
     *   <li>If the utility is unowned, no direct action is taken by this method.
     *       The game's UI or service layer would typically handle offering the property for purchase.</li>
     *   <li>If the utility is owned by the current player, no action is taken.</li>
     *   <li>If the utility is owned by another player, rent is calculated and paid:
     *     <ol>
     *       <li>The current player's last dice roll is retrieved using {@link Player#getLastDiceRoll()}. 
     *           It is assumed this method is implemented and returns the correct sum of the dice for the turn.</li>
     *       <li>The {@link MonopolyService} is used to determine the number of utilities the owner possesses.</li>
     *       <li>A multiplier is determined: 4x if the owner has one utility, 10x if the owner has more than one (typically two).</li>
     *       <li>The rent is calculated as {@code diceRoll * multiplier}.</li>
     *       <li>The calculated rent is deducted from the current player's money and added to the owner's money.</li>
     *       <li>Information about the transaction is logged.</li>
     *     </ol>
     *   </li>
     * </ul>
     * This method directly handles the money transfer for rent.
     *
     * @param player The {@link Player} who landed on the utility tile.
     */
    @Override
    public void perform(Player player) {
        if (getOwner() == null) {
            // Property is unowned. UI/Service layer would typically handle purchase option.
            // Logger.info("Player " + player.getName() + " landed on unowned utility: " + getName());
        } else if (!getOwner().equals(player)) {
            // Property is owned by another player. Rent is due.
            // Retrieve last roll from game context or Player
            int roll = player.getLastDiceRoll(); // implement in Player or via context
            MonopolyService service = ServiceLocator.getMonopolyService();
            if (service != null) {
                int owned = service.getUtilitiesOwnedCount(getOwner());
                int multiplier = (owned == 1 ? 4 : 10); // If 1 utility owned, 4x roll. If >1 (i.e., 2), 10x roll.
                int rentToPay = roll * multiplier;
                player.decreaseMoney(rentToPay);
                Logger.info("Utility action performed: " + getName() + ". " + owned + " utility/utilities owned by " + getOwner().getName());
                Logger.info("Player " + player.getName() + " rolled " + roll + ". Rent to pay: " + rentToPay);
                getOwner().increaseMoney(rentToPay);
                Logger.info(player.getName() + " paid " + rentToPay + " to " + getOwner().getName());
            } else {
                Logger.warning("MonopolyService not available for UtilityAction on " + getName());
            }
        } else {
            // Player landed on their own property.
            // Logger.info("Player " + player.getName() + " landed on their own utility: " + getName());
        }
    }
}