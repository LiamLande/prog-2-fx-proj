package edu.ntnu.idi.bidata.model.actions.monopoly;

import edu.ntnu.idi.bidata.model.Player;
import edu.ntnu.idi.bidata.model.actions.TileAction;
import edu.ntnu.idi.bidata.util.Logger;

/**
 * Represents an own able property on the board: name, cost, rent, and owner.
 */
public class PropertyAction implements TileAction {
    private final String name;
    private final int cost;
    private final int rent;
    private final String colorGroup; // Optional property group
    private Player owner;

    public PropertyAction(String name, int cost, int rent) {
        this.name = name;
        this.cost = cost;
        this.rent = rent;
        this.colorGroup = null; // Default to null if no color group is provided
    }
    
    public PropertyAction(String name, int cost, int rent, String colorGroup) {
        this.name = name;
        this.cost = cost;
        this.rent = rent;
        this.colorGroup = colorGroup;
    }

    @Override
    public void perform(Player player) {
        // Here we do the property action in our service,
        // so we have access to the UI level.
        Logger.info("Performing property action " + name);
    }
    
    /**
     * Calculate the rent amount based on property details.
     * This can be overridden by subclasses to implement different rent calculations.
     */
    protected int calculateRent() {
        return rent;
    }

    public String getName() {
        return name;
    }

    public int getCost() {
        return cost;
    }

    public int getRent() {
        return rent;
    }
    
    public String getColorGroup() {
        return colorGroup;
    }

    public Player getOwner() {
        return owner;
    }

    public void setOwner(Player owner) {
        this.owner = owner;
    }
}


