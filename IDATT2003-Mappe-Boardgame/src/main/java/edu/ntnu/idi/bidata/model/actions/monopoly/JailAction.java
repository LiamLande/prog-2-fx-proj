package edu.ntnu.idi.bidata.model.actions.monopoly;

import edu.ntnu.idi.bidata.model.Player;
import edu.ntnu.idi.bidata.model.actions.TileAction;

public class JailAction implements TileAction {
    private final String description;

    public JailAction(String description) {
        this.description = description;
    }

    @Override
    public void perform(Player player) {
        // Jail does not have any specific action to perform
        // It is just a resting place for players
        System.out.println(description);
    }

}

