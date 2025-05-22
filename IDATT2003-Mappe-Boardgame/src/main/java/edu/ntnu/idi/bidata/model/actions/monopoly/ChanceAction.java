package edu.ntnu.idi.bidata.model.actions.monopoly;

import edu.ntnu.idi.bidata.model.Player;
import edu.ntnu.idi.bidata.model.actions.TileAction;
import edu.ntnu.idi.bidata.service.MonopolyService;
import edu.ntnu.idi.bidata.service.ServiceLocator;

/**
 * Implements the {@link TileAction} for a "Chance" space in Monopoly.
 * When a player lands on this tile, this action triggers the drawing of a Chance card
 * by interacting with the {@link MonopolyService}.
 */
public class ChanceAction implements TileAction {
    private final String description;

    /**
     * Constructs a new ChanceAction.
     *
     * @param description A description of this action (e.g., "Landed on Chance").
     */
    public ChanceAction(String description) {
        this.description = description;
    }

    /**
     * Gets the description of this Chance action.
     *
     * @return The description string.
     */
    public String getDescription() {
        return description;
    }

    /**
     * Performs the Chance action for the given player.
     * This involves printing the action's description to the console and then
     * instructing the {@link MonopolyService} to handle the drawing of a Chance card for the player.
     * If the {@link MonopolyService} cannot be located, no card action will occur beyond the print.
     *
     * @param player The {@link Player} who landed on the Chance tile.
     */
    @Override
    public void perform(Player player) {
        System.out.println(description);
        MonopolyService service = ServiceLocator.getMonopolyService();
        if (service != null) {
            service.drawChanceCard(player);
        }
    }
}