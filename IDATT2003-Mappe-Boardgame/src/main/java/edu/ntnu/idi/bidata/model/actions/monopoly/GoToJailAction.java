package edu.ntnu.idi.bidata.model.actions.monopoly;

import edu.ntnu.idi.bidata.exception.InvalidParameterException;
import edu.ntnu.idi.bidata.model.Player;
import edu.ntnu.idi.bidata.model.Tile;
import edu.ntnu.idi.bidata.model.actions.TileAction;
import edu.ntnu.idi.bidata.service.MonopolyService;
import edu.ntnu.idi.bidata.service.ServiceLocator;

public class GoToJailAction implements TileAction {
    private final String description;
    private final int targetId;


    /**
     * @param description text describing the jail action
     * @param targetId the ID of the jail tile to move to
     */
    public GoToJailAction(String description, int targetId) {
        if (description == null || description.isBlank()) {
            throw new InvalidParameterException("GoToJailAction description must not be empty");
        }

        this.description = description;
        this.targetId = targetId;
    }

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
            MonopolyService monopolyService = getMonopolyService(); // You'll need to implement this
            if (monopolyService != null) {
                monopolyService.sendToJail(player);
            }
        }
    }

    /**
     * Calculate steps between current tile and target jail tile
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

    public MonopolyService getMonopolyService() {
        return ServiceLocator.getMonopolyService();
    }
}