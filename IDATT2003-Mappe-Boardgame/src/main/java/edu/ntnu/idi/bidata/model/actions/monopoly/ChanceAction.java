package edu.ntnu.idi.bidata.model.actions.monopoly;

import edu.ntnu.idi.bidata.model.Player;
import edu.ntnu.idi.bidata.model.actions.TileAction;

public class ChanceAction implements TileAction {
    private final String description;

    public ChanceAction(String description) {
        this.description = description;
    }

    @Override
    public void perform(Player player) {
        // TODO: Implement drawing a Chance card
        // This would likely trigger a UI event or callback
    }
}