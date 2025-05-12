package edu.ntnu.idi.bidata.model.actions.monopoly;

import edu.ntnu.idi.bidata.model.Player;
import edu.ntnu.idi.bidata.model.actions.TileAction;

public class GoAction implements TileAction {
    private final String description;
    private final int reward;

    public GoAction(String description, int reward) {
        this.description = description;
        this.reward = reward;
    }

    @Override
    public void perform(Player player) {
        // TODO: Add money to player when they pass GO
        // player.increaseMoney(reward);
        // This might be better to have as a check in the Player or service class, therefore go could just be double money if you land on it?
        player.increaseMoney(reward);
    }
}