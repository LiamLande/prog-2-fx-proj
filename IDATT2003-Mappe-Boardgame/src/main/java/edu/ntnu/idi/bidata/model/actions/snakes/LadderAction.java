package edu.ntnu.idi.bidata.model.actions.snakes;


import edu.ntnu.idi.bidata.exception.InvalidParameterException;
import edu.ntnu.idi.bidata.model.Player;
import edu.ntnu.idi.bidata.model.actions.TileAction;

/**
 * Represents a "Ladder" action in a Snakes and Ladders game.
 * When a player lands on a tile with this action, they are moved forward a specified number of steps.
 * This class implements the {@link TileAction} interface.
 */
public class LadderAction implements TileAction {
  private final int steps;
  private final String description;

  /**
   * Constructs a new LadderAction.
   *
   * @param description Text describing the ladder (e.g., "Climbed a long ladder!"). Must not be null or blank.
   * @param steps The positive number of tiles the player will advance. Must be 1 or greater.
   * @throws InvalidParameterException if the description is null/blank or if steps is less than 1.
   */
  public LadderAction(String description, int steps) {
    if (description == null || description.isBlank()) {
      throw new InvalidParameterException("LadderAction description must not be empty");
    }
    if (steps < 1) {
      throw new InvalidParameterException("LadderAction steps must be positive");
    }
    this.description = description;
    this.steps = steps;
  }

  
  /**
   * Performs the ladder action for the given player.
   * This involves printing the ladder's description to the console and then moving the player
   * forward by the number of steps defined for this ladder.
   *
   * @param player The {@link Player} who landed on the ladder tile.
   */
  @Override
  public void perform(Player player) {
    // optionally log the description
    System.out.println(description);
    player.move(steps);
  }

  /**
   * Gets the number of steps this ladder will move a player forward.
   *
   * @return The positive integer number of steps.
   */
  public int getSteps() {
    return steps;
  }
}
