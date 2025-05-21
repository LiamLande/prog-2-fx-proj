// In package edu.ntnu.idi.bidata.model.actions.monopoly

package edu.ntnu.idi.bidata.model.actions.monopoly;

import edu.ntnu.idi.bidata.model.Player;
import edu.ntnu.idi.bidata.model.actions.TileAction;
import edu.ntnu.idi.bidata.service.MonopolyService;
import edu.ntnu.idi.bidata.service.ServiceLocator;

/**
 * Represents an ownable property on the board: name, cost, rent, and owner.
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
        // The property purchase dialog is now handled by MonopolyGameScene
        // This method will be called when a player lands on the property
        // The UI will handle showing purchase dialog or paying rent
        
        // If we want to add automatic handling without UI, uncomment this:
        /*
        if (owner == null) {
            // Automatic purchase if player can afford it
            if (player.getMoney() >= cost) {
                player.decreaseMoney(cost);
                setOwner(player);
                
                MonopolyService monopolyService = ServiceLocator.getMonopolyService();
                if (monopolyService != null) {
                    monopolyService.addProperty(player, this);
                }
                
                System.out.println(player.getName() + " purchased " + name + " for $" + cost);
            }
        } else if (!owner.equals(player)) {
            // Pay rent automatically
            int rentToPay = calculateRent();
            player.decreaseMoney(rentToPay);
            owner.increaseMoney(rentToPay);
            System.out.println(player.getName() + " paid $" + rentToPay + " rent to " + owner.getName() + " for " + name);
        }
        */
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


