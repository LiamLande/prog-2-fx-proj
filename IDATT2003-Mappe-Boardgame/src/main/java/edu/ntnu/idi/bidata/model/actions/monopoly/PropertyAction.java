// In package edu.ntnu.idi.bidata.model.actions.monopoly

package edu.ntnu.idi.bidata.model.actions.monopoly;

import edu.ntnu.idi.bidata.model.Player;
import edu.ntnu.idi.bidata.model.actions.TileAction;

/**
 * Represents an ownable property on the board: name, cost, rent, and owner.
 */
public class PropertyAction implements TileAction {
    private final String name;
    private final int cost;
    private final int rent;
    private Player owner;

    public PropertyAction(String name, int cost, int rent) {
        this.name = name;
        this.cost = cost;
        this.rent = rent;
    }

    @Override
    public void perform(Player player) {
        if (owner == null) {
            // TODO: offer purchase opportunity to `player`, e.g. via UI prompt
        } else if (!owner.equals(player)) {
            // pay rent
            // player.decreaseBalance(rent);
            // owner.increaseBalance(rent);
        }
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

    public Player getOwner() {
        return owner;
    }

    public void setOwner(Player owner) {
        this.owner = owner;
    }
}


