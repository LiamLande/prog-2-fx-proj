package edu.ntnu.idi.bidata.model.actions.monopoly;

import edu.ntnu.idi.bidata.model.Player;
import edu.ntnu.idi.bidata.service.MonopolyService;
import edu.ntnu.idi.bidata.service.ServiceLocator;
import edu.ntnu.idi.bidata.util.Logger;

/**
 * Utility action: rent is 4x or 10x the dice roll, depending on utilities owned.
 */
public class UtilityAction extends PropertyAction {
    public UtilityAction(String name, int cost) {
        super(name, cost, 0);
    }

    @Override
    public void perform(Player player) {
        if (getOwner() == null) {
        } else if (!getOwner().equals(player)) {
            // Retrieve last roll from game context or Player
            int roll = player.getLastDiceRoll(); // implement in Player or via context
            MonopolyService service = ServiceLocator.getMonopolyService();
            int owned = service.getUtilitiesOwnedCount(getOwner());
            int multiplier = (owned == 1 ? 4 : 10);
            int rentToPay = roll * multiplier;
            player.decreaseMoney(rentToPay);
            Logger.info("Utility action performed " + owned + " owned by " + getOwner().getName());
            Logger.info("Rent to pay: " + rentToPay);
            getOwner().increaseMoney(rentToPay);
        }
    }
}