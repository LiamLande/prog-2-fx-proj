package edu.ntnu.idi.bidata.model.actions.monopoly;

import edu.ntnu.idi.bidata.exception.InvalidParameterException;
import edu.ntnu.idi.bidata.model.Player;
import edu.ntnu.idi.bidata.model.Tile;
import edu.ntnu.idi.bidata.model.actions.TileAction;
import edu.ntnu.idi.bidata.service.MonopolyService;
import edu.ntnu.idi.bidata.service.ServiceLocator;

/**
 * Implements the {@link TileAction} for a "Go To Jail" space in Monopoly.
 * When a player lands on this tile, this action moves the player to the designated Jail tile
 * and updates their status via the {@link MonopolyService}.
 */
public class GoToJailAction implements TileAction {
    private final String description;
    private final int targetId;


    /**
     * Constructs a new GoToJailAction.
     *
     * @param description Text describing the jail action (e.g., "Go To Jail"). Must not be null or blank.
     * @param targetId The ID of the jail tile to which the player will be moved.
     * @throws InvalidParameterException if the description is null or blank.
     */
    public GoToJailAction(String description, int targetId) {
        if (description == null || description.isBlank()) {
            throw new InvalidParameterException("GoToJailAction description must not be empty");
        }

        this.description = description;
        this.targetId = targetId;
    }

    /**
     * Performs the Go To Jail action for the given player.
     * This involves printing the action's description, finding the Jail tile on the board,
     * moving the player to that Jail tile, and then notifying the {@link MonopolyService}
     * that the player has been sent to jail.
     * The method searches for the jail tile first by moving forward from the player's current position,
     * and if not found, then by moving backward.
     *
     * @param player The {@link Player} who landed on the Go To Jail tile.
     */
    @Override
    public void perform(Player player) {
        System.out.println(description);

        // Find jail tile by going backward/forward until we find the target ID
        Tile current = player.getCurrentTile();
        // First try to find jail by going forward
        while (current != null && current.getId() != targetId) {
            current = current.getNext();
        }

        // If not found, try backward
        if (current == null || current.getId() != targetId) {
            current = player.getCurrentTile();
            while (current != null && current.getId() != targetId) {
                current = current.getPrevious();
            }
        }

        // If we found the jail tile, set player's position directly
        if (current != null && current.getId() == targetId) {
            // We need to use the move method to handle any potential jail actions
            int stepsToJail = calculateSteps(player.getCurrentTile(), current);
            player.move(stepsToJail);
            MonopolyService monopolyService = getMonopolyService();
            if (monopolyService != null) {
                monopolyService.sendToJail(player);
            }
        }
    }

    /**
     * Calculates the number of steps (and direction) from a current tile to a target tile.
     * It first attempts to find the target by moving forwards. If the target is found in the
     * forward direction, a positive number of steps is returned.
     * If not found by moving forwards (or if a safety limit of 40 steps is exceeded),
     * it then calculates the steps by moving backwards (via {@link #calculateReverseSteps(Tile, Tile)})
     * and returns this as a negative number.
     *
     * @param current The starting {@link Tile}.
     * @param target The target {@link Tile} to reach.
     * @return The number of steps to reach the target. Positive for forward, negative for backward.
     *         Returns a large number of steps (positive or negative) if the target is not found within 40 steps
     *         in either direction, effectively indicating the target is not reasonably reachable by this simple traversal.
     */
    private int calculateSteps(Tile current, Tile target) {
        // This is a simplified calculation to determine direction
        // Check if we need to go forward or backward
        Tile temp = current;
        int forwardSteps = 0;
        while (temp != null && temp.getId() != target.getId()) {
            temp = temp.getNext();
            forwardSteps++;
            if (forwardSteps > 40) break; // Safety to prevent infinite loop
        }

        if (temp != null && temp.getId() == target.getId()) {
            return forwardSteps;
        } else {
            return -calculateReverseSteps(current, target);
        }
    }

    /**
     * Calculates the number of steps required to reach a target tile by moving backwards from a current tile.
     * This method iterates backwards from the {@code current} tile using {@code getPrevious()} until the
     * {@code target} tile is found or a maximum of 40 steps is taken (to prevent infinite loops on malformed boards).
     *
     * @param current The starting {@link Tile}.
     * @param target The target {@link Tile} to reach by moving backwards.
     * @return The number of backward steps to reach the target. Returns a number greater than 40 if the target
     *         is not found within 40 backward steps.
     */
    private int calculateReverseSteps(Tile current, Tile target) {
        Tile temp = current;
        int backwardSteps = 0;
        while (temp != null && temp.getId() != target.getId()) {
            temp = temp.getPrevious();
            backwardSteps++;
            if (backwardSteps > 40) break; // Safety to prevent infinite loop
        }
        return backwardSteps;
    }

    /**
     * Retrieves the {@link MonopolyService} instance from the {@link ServiceLocator}.
     *
     * @return The {@link MonopolyService} instance, or {@code null} if it has not been registered.
     */
    public MonopolyService getMonopolyService() {
        return ServiceLocator.getMonopolyService();
    }
}