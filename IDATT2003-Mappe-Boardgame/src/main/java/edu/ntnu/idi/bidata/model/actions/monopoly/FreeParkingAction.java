package edu.ntnu.idi.bidata.model.actions.monopoly;

import edu.ntnu.idi.bidata.model.Player;
import edu.ntnu.idi.bidata.model.actions.TileAction;

/**
 * Implements the {@link TileAction} for a "Free Parking" space in Monopoly.
 * In standard rules, landing on Free Parking has no direct effect on the player.
 * This action simply logs that the player has landed on the space.
 */
public class FreeParkingAction implements TileAction {
    private final String description;

    /**
     * Constructs a new FreeParkingAction.
     *
     * @param description A description of this action (e.g., "Landed on Free Parking").
     */
    public FreeParkingAction(String description) {
        this.description = description;
    }

    /**
     * Performs the Free Parking action for the given player.
     * In the standard Monopoly rules, Free Parking is just a resting place
     * and does not have any specific action to perform on the player.
     * This method prints the description of the action to the console.
     *
     * @param player The {@link Player} who landed on the Free Parking tile.
     */
    @Override
    public void perform(Player player) {
        // Free parking does not have any specific action to perform
        // It is just a resting place for players
        System.out.println(description);
    }

}
