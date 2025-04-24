package edu.ntnu.idi.bidata.model.actions.monopoly;

import edu.ntnu.idi.bidata.model.Player;
import edu.ntnu.idi.bidata.model.actions.TileAction;

public class PropertyAction implements TileAction {
    private final String propertyName;
    private final int propertyId;

    public PropertyAction(String propertyName, int propertyId) {
        this.propertyName = propertyName;
        this.propertyId = propertyId;
    }

    public String getPropertyName() {
        return propertyName;
    }

    public int getPropertyId() {
        return propertyId;
    }

    @Override
    public String toString() {
        return "PropertyAction{" +
                "propertyName='" + propertyName + '\'' +
                ", propertyId=" + propertyId +
                '}';
    }

    @Override
    public void perform(Player player) {
        // Perform the action on the player
        // This could involve buying, selling, or interacting with the property in some way
        System.out.println(player.getName() + " is interacting with property: " + propertyName);
        // TODO: Add logic for what happens when a player interacts with a property
    }
}
