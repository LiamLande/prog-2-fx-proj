package edu.ntnu.idi.bidata.model.actions.monopoly;

import edu.ntnu.idi.bidata.model.Player;
import edu.ntnu.idi.bidata.model.actions.TileAction;
import edu.ntnu.idi.bidata.util.Logger;

/**
 * Represents an ownable property on the Monopoly board.
 * This class implements {@link TileAction} and defines the behavior when a player lands on a property tile.
 * It holds information about the property's name, cost, rent, color group, and current owner.
 * The actual purchase and rent payment logic is typically handled by a service or UI layer
 * that interacts with this action.
 */
public class PropertyAction implements TileAction {
    private final String name;
    private final int cost;
    private final int rent;
    private final String colorGroup; // Optional property group
    private Player owner;

    /**
     * Constructs a new PropertyAction without a specified color group.
     *
     * @param name The name of the property.
     * @param cost The cost to purchase the property.
     * @param rent The base rent amount for the property.
     */
    public PropertyAction(String name, int cost, int rent) {
        this.name = name;
        this.cost = cost;
        this.rent = rent;
        this.colorGroup = null; // Default to null if no color group is provided
    }
    
    /**
     * Constructs a new PropertyAction with a specified color group.
     *
     * @param name The name of the property.
     * @param cost The cost to purchase the property.
     * @param rent The base rent amount for the property.
     * @param colorGroup The color group to which this property belongs (e.g., "Blue", "Railroad").
     */
    public PropertyAction(String name, int cost, int rent, String colorGroup) {
        this.name = name;
        this.cost = cost;
        this.rent = rent;
        this.colorGroup = colorGroup;
    }

    /**
     * Performs the action when a player lands on this property tile.
     * Currently, this method logs that the action is being performed.
     * The detailed logic for property purchase or rent payment is expected to be handled
     * by a higher-level service (e.g., {@link edu.ntnu.idi.bidata.service.MonopolyService})
     * or the UI, which would interact with this PropertyAction's state (owner, cost, rent).
     *
     * @param player The {@link Player} who landed on the property tile.
     */
    @Override
    public void perform(Player player) {
        // The core logic for property interaction (buy/rent) is typically handled
        // by the MonopolyService or the UI layer (e.g., MonopolyGameScene)
        // which checks the owner, prompts for purchase, or collects rent.
        Logger.info("Player " + player.getName() + " landed on property: " + name);
    }
    
    /**
     * Calculates the rent amount for this property.
     * This base implementation returns the fixed rent value.
     * Subclasses (e.g., for utilities or railroads with different rent calculation rules)
     * can override this method to implement custom rent logic.
     *
     * @return The calculated rent amount.
     */
    protected int calculateRent() {
        return rent;
    }

    /**
     * Gets the name of the property.
     *
     * @return The name of the property.
     */
    public String getName() {
        return name;
    }

    /**
     * Gets the cost to purchase the property.
     *
     * @return The purchase cost of the property.
     */
    public int getCost() {
        return cost;
    }

    /**
     * Gets the base rent amount for the property.
     * To get the actual rent due, {@link #calculateRent()} should typically be used,
     * especially if there are rules for rent modification (e.g., based on monopolies or houses).
     *
     * @return The base rent amount.
     */
    public int getRent() {
        return rent;
    }
    
    /**
     * Gets the color group of the property.
     *
     * @return The color group string (e.g., "Brown", "Light Blue"), or {@code null} if not part of a color group.
     */
    public String getColorGroup() {
        return colorGroup;
    }

    /**
     * Gets the current owner of the property.
     *
     * @return The {@link Player} who owns the property, or {@code null} if the property is unowned.
     */
    public Player getOwner() {
        return owner;
    }

    /**
     * Sets the owner of the property.
     *
     * @param owner The {@link Player} to set as the new owner. Can be {@code null} to indicate the property is unowned.
     */
    public void setOwner(Player owner) {
        this.owner = owner;
    }
}


