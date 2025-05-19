package edu.ntnu.idi.bidata.model.actions.snakes;

import edu.ntnu.idi.bidata.model.Player;
import edu.ntnu.idi.bidata.model.actions.TileAction; // Your existing interface

/**
 * Represents a tile action for a "Schrödinger's Box".
 * When a player lands on this tile, they are presented with a choice
 * by the UI to "observe" the box (resulting in a random outcome) or "move on".
 * The perform() method primarily serves to identify that this special action should occur.
 * The actual choice and outcome are handled by the GameController and UI.
 */
public class SchrodingerBoxAction implements TileAction {

  private final String description;

  public SchrodingerBoxAction() {
    this.description = "A mysterious Schrödinger's Box! Observe its contents or move on?";
  }

  // Constructor for custom description from JSON, though not strictly needed if using default
  public SchrodingerBoxAction(String description) {
    this.description = (description != null && !description.trim().isEmpty()) ?
        description.trim() :
        "A mysterious Schrödinger's Box! Observe its contents or move on?";
  }

  /**
   * Gets the description of this action.
   * If your TileAction interface doesn't have getDescription(),
   * this method is standalone or you should add it to the interface.
   * @return The description string.
   */
  public String getDescription() {
    return description;
  }

  /**
   * Called when a player lands on a tile with this action.
   * For Schrödinger's Box, this method doesn't directly change game state based on randomness.
   * Instead, it signals that the player has landed on such a box.
   * The GameController will then prompt the UI for player choice.
   *
   * @param player the player on which to perform the action (in this case, the one who landed)
   */
  @Override
  public void perform(Player player) {
    // The primary effect is that the GameController, upon seeing this action type
    // on the player's current tile after a move, will initiate the UI choice.
    // So, this perform() method itself might not need to do much directly.
    // It could log or set a temporary flag on the player if needed, but the
    // controller will likely check tile.getAction() instanceof SchrodingerBoxAction.
    System.out.println(player.getName() + " has landed on a Schrödinger's Box tile. Awaiting decision.");
    // No direct game state change here; that's deferred to player choice via UI & Controller.
  }
}