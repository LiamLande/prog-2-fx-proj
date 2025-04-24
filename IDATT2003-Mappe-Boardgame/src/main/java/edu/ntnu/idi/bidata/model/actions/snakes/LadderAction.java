package edu.ntnu.idi.bidata.model.actions.snakes;


import edu.ntnu.idi.bidata.exception.InvalidParameterException;
import edu.ntnu.idi.bidata.model.Player;
import edu.ntnu.idi.bidata.model.actions.TileAction;

/**
 * Action to move the player forward by a positive number of tiles (ladder).
 */
public class LadderAction implements TileAction {
  private final int steps;
  private final String description;

  /**
   * @param description text describing the ladder
   * @param steps positive number of tiles to advance
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

  @Override
  public void perform(Player player) {
    // optionally log the description
    System.out.println(description);
    player.move(steps);
  }
}
