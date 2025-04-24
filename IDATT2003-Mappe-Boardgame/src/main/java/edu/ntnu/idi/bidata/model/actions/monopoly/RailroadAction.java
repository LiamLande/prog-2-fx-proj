
// --------------------------------------------------
// Specialized ownable spaces
// --------------------------------------------------

import edu.ntnu.idi.bidata.model.Player;
import edu.ntnu.idi.bidata.model.actions.monopoly.PropertyAction;

/**
 * Railroad action: rent depends on the number of railroads the owner holds.
 */
public class RailroadAction extends PropertyAction {
    private static final int BASE_RENT = 25;

    public RailroadAction(String name, int cost) {
        super(name, cost, BASE_RENT);
    }

    @Override
    public void perform(Player player) {
        if (getOwner() == null) {
            // TODO: offer purchase
        } else if (!getOwner().equals(player)) {
            // Determine number of railroads owned
            int owned = getOwner().getRailroadsOwnedCount(); // implement in Player
            int rentToPay = BASE_RENT * owned;
            // player.decreaseBalance(rentToPay);
            // getOwner().increaseBalance(rentToPay);
        }
    }
}

