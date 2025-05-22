package edu.ntnu.idi.bidata.model.actions.monopoly;

import edu.ntnu.idi.bidata.model.Player;
import edu.ntnu.idi.bidata.model.actions.TileAction;
import edu.ntnu.idi.bidata.service.MonopolyService;
import edu.ntnu.idi.bidata.service.ServiceLocator;

public class ChanceAction implements TileAction {
    private final String description;

    public ChanceAction(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

    // In ChanceAction.java
    @Override
    public void perform(Player player) {
        System.out.println(description);
        MonopolyService service = ServiceLocator.getMonopolyService();
        if (service != null) {
            service.drawChanceCard(player);
        }
    }
}