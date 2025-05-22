package edu.ntnu.idi.bidata.model.actions.monopoly;

import edu.ntnu.idi.bidata.model.Player;
import edu.ntnu.idi.bidata.model.actions.TileAction;

/**
 * Implements the {@link TileAction} for the "GO" space in Monopoly.
 * When a player lands on this tile, they receive a specified reward (money).
 * The logic for receiving money when *passing* GO is typically handled elsewhere
 * (e.g., in the game controller or player movement logic).
 */
public class GoAction implements TileAction {
    private final String description;
    private final int reward;

    /**
     * Constructs a new GoAction.
     *
     * @param description A description of this action (e.g., "Landed on GO, collect salary").
     * @param reward The amount of money the player receives for landing on GO.
     */
    public GoAction(String description, int reward) {
        this.description = description;
        this.reward = reward;
    }

    /**
     * Performs the GO action for the given player.
     * This action increases the player's money by the specified reward amount.
     * Note: The current implementation gives the reward for *landing* on GO.
     * The logic for collecting salary when *passing* GO is handled separately.
     *
     * @param player The {@link Player} who landed on the GO tile.
     */
    @Override
    public void perform(Player player) {
        player.increaseMoney(reward);
    }
}