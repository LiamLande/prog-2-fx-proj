package edu.ntnu.idi.bidata.model.actions.monopoly;

import edu.ntnu.idi.bidata.model.Player;
import edu.ntnu.idi.bidata.service.MonopolyService;
import edu.ntnu.idi.bidata.service.ServiceLocator;

/**
 * Railroad action: rent depends on the number of railroads the owner holds.
 */
public class RailroadAction extends PropertyAction {
    private static final int BASE_RENT = 25;

    public RailroadAction(String name, int cost, int rent) {
        super(name, cost, BASE_RENT);
    }

    @Override
    public void perform(Player player) {
        // In RailroadAction.perform()
        if (getOwner() == null) {
            // TODO: offer purchase
        } else if (!getOwner().equals(player)) {
            MonopolyService service = ServiceLocator.getMonopolyService();
            int owned = service.getRailroadsOwnedCount(getOwner());
            int rentToPay = BASE_RENT * owned;
            // Payment logic
        }
    }
}

