package edu.ntnu.idi.bidata.model.actions.monopoly;

import edu.ntnu.idi.bidata.model.Player;
import edu.ntnu.idi.bidata.model.actions.TileAction;

/**
 * Implements the {@link TileAction} for a "Jail" space in Monopoly (specifically, the "Just Visiting" part
 * or when a player is in jail but it's not their turn to act on it).
 * Landing on this space while not being sent to jail typically has no direct effect.
 * The actual mechanics of being in jail (e.g., skipping turns, paying to get out) are usually handled
 * by the {@link edu.ntnu.idi.bidata.service.MonopolyService} or game controller logic.
 */
public class JailAction implements TileAction {
    private final String description;

    /**
     * Constructs a new JailAction.
     *
     * @param description A description of this action (e.g., "Landed on Jail / Just Visiting").
     */
    public JailAction(String description) {
        this.description = description;
    }

    /**
     * Performs the action when a player lands on the Jail tile.
     * In its current implementation, this action is passive if the player is "Just Visiting".
     * It simply prints the description of the action (e.g., indicating the player is at the jail tile).
     * The logic for being incarcerated, bail, etc., is handled elsewhere (e.g., {@link MonopolyService}).
     *
     * @param player The {@link Player} who landed on the Jail tile.
     */
    @Override
    public void perform(Player player) {
        // Jail does not have any specific action to perform if just visiting.
        // Actual jail mechanics (bail, turns in jail) are handled by MonopolyService.
        System.out.println(description);
    }

}

