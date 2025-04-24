package edu.ntnu.idi.bidata.model.actions.monopoly;

import edu.ntnu.idi.bidata.model.Board;
import edu.ntnu.idi.bidata.model.Player;
import edu.ntnu.idi.bidata.model.actions.TileAction;

public class FreeParkingAction implements TileAction {
    private String description;

    public FreeParkingAction(String description) {
        this.description = description;
    }

    @Override
    public void perform(Player player) {
        // Free parking does not have any specific action to perform
        // It is just a resting place for players
        System.out.println(description);
    }

}
