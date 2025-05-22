package edu.ntnu.idi.bidata.model.actions.monopoly;

import edu.ntnu.idi.bidata.model.Player;
import edu.ntnu.idi.bidata.model.actions.TileAction;

/**
 * Implements the {@link TileAction} for a tax collection space in Monopoly.
 * When a player lands on this tile, a fixed tax amount is deducted from their money.
 */
public class TaxAction implements TileAction {
    private final String description;
    private final int taxAmount;

    /**
     * Constructs a new TaxAction.
     *
     * @param description A description of this tax action (e.g., "Income Tax", "Luxury Tax").
     * @param taxAmount The amount of tax the player must pay.
     */
    public TaxAction(String description, int taxAmount) {
        this.description = description;
        this.taxAmount = taxAmount;
    }

    /**
     * Performs the tax collection action for the given player.
     * This action deducts the specified {@code taxAmount} from the player's money
     * and prints a message to the console indicating the tax payment.
     *
     * @param player The {@link Player} who landed on the tax tile.
     */
    @Override
    public void perform(Player player) {
        // Deduct the tax amount from the player's balance
        player.decreaseMoney(taxAmount);
        System.out.println(description + " You paid " + taxAmount + " in taxes.");
    }

    /**
     * Gets the description of this tax action.
     *
     * @return The description string (e.g., "Income Tax").
     */
    public String getDescription() {
        return description;
    }

    /**
     * Gets the amount of tax to be collected.
     *
     * @return The tax amount.
     */
    public int getTaxAmount() {
        return taxAmount;
    }

}
