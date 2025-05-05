package edu.ntnu.idi.bidata.model.actions.monopoly;

import edu.ntnu.idi.bidata.model.Player;
import edu.ntnu.idi.bidata.model.actions.TileAction;

public class TaxAction implements TileAction {
    private String description;
    private int taxAmount;

    public TaxAction(String description, int taxAmount) {
        this.description = description;
        this.taxAmount = taxAmount;
    }

    @Override
    public void perform(Player player) {
        // Deduct the tax amount from the player's balance
        player.decreaseMoney(taxAmount);
        System.out.println(description + " You paid " + taxAmount + " in taxes.");
        //TODO:IMPLEMENT ALERT
    }

    public String getDescription() {
        return description;
    }

    public int getTaxAmount() {
        return taxAmount;
    }

}
