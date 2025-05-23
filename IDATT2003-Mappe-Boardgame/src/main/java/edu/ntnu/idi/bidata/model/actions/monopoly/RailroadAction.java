package edu.ntnu.idi.bidata.model.actions.monopoly;

import edu.ntnu.idi.bidata.model.Player;
import edu.ntnu.idi.bidata.service.MonopolyService;
import edu.ntnu.idi.bidata.service.ServiceLocator;

/**
 * Represents a Railroad property action in a Monopoly-like game.
 * This class extends {@link PropertyAction} and customizes the behavior for railroads,
 * particularly how rent is calculated based on the number of railroads owned by the same player.
 * The purchase and detailed rent transaction (e.g., UI dialogs, transferring money to owner)
 * are typically handled by a higher-level service or UI component that interacts with this action.
 */
public class RailroadAction extends PropertyAction {
    /**
     * The base rent for owning one railroad. The actual rent increases
     * based on the total number of railroads owned by the proprietor.
     * For example, if a player owns 2 railroads, the rent might be 2 * BASE_RENT.
     */
    private static final int BASE_RENT = 25;

    /**
     * Constructs a new RailroadAction.
     *
     * @param name The name of the railroad (e.g., "Reading Railroad").
     * @param cost The cost to purchase the railroad.
     * @param rent This parameter is currently ignored. The base rent for the superclass
     *             is initialized using the static {@link #BASE_RENT} (25).
     *             The actual rent calculation is dynamic and handled by the {@code perform} or
     *             an overridden {@code calculateRent} method if implemented differently.
     */
    public RailroadAction(String name, int cost, int rent) {
        super(name, cost, BASE_RENT); // The 'rent' parameter is effectively unused, BASE_RENT is passed.
    }

    /**
     * Performs the action when a player lands on this railroad tile.
     * This method contains logic that was intended to calculate and apply rent if the railroad is owned by another player.
     * However, the primary interaction logic (purchase dialogs, full rent transaction) is generally expected
     * to be handled by the {@link edu.ntnu.idi.bidata.ui.MonopolyGameScene} or {@link MonopolyService}.
     *
     * <p>Current behavior of this method if the railroad is owned by another player:
     * <ol>
     *   <li>Retrieves the {@link MonopolyService}.</li>
     *   <li>Gets the count of railroads owned by the proprietor using {@code service.getRailroadsOwnedCount(getOwner())}.</li>
     *   <li>Calculates {@code rentToPay = BASE_RENT * ownedCount}.</li>
     * </ol>
     * If the railroad is unowned, this method currently does nothing.
     * If the railroad is owned by the current player, this method also does nothing.
     *
     * @param player The {@link Player} who landed on the railroad tile.
     */
    @Override
    public void perform(Player player) {

        if (getOwner() == null) {
        } else if (!getOwner().equals(player)) {
            // Property is owned by another player. Rent should be paid.
            MonopolyService service = ServiceLocator.getMonopolyService();
            if (service != null) {
                int ownedCount = service.getRailroadsOwnedCount(getOwner());
                int rentToPay = BASE_RENT * ownedCount; 

                player.decreaseMoney(rentToPay - getRent()); // getRent() here is BASE_RENT (25)
            } 
        } 
    }
}

