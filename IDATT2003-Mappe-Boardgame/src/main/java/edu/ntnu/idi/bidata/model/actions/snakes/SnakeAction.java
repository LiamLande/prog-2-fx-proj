package edu.ntnu.idi.bidata.model.actions.snakes;

import edu.ntnu.idi.bidata.exception.InvalidParameterException;
import edu.ntnu.idi.bidata.model.Player;
import edu.ntnu.idi.bidata.model.actions.TileAction;

/**
 * Action to move the player backward by a positive number of tiles (snake).
 */
public class SnakeAction implements TileAction {
  private final int steps;
  private final String description;

  /**
   * @param description text describing the snake
   * @param steps positive number of tiles to retreat
   */
  public SnakeAction(String description, int steps) {
    if (description == null || description.isBlank()) {
      throw new InvalidParameterException("SnakeAction description must not be empty");
    }
    if (steps < 1) {
      throw new InvalidParameterException("SnakeAction steps must be positive");
    }
    this.description = description;
    this.steps = steps;
  }

  /**
   * Performs the snake action for the given player.
   * This involves printing the snake's description to the console and then moving the player
   * backward by the number of steps defined for this snake.
   *
   * @param player The {@link Player} who landed on the snake tile.
   */
  @Override
  public void perform(Player player) {
    // optionally log the description
    System.out.println(description);
    player.move(-steps);
  }

  public int getSteps() {
    return steps;
  }
}
