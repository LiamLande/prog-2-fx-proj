package edu.ntnu.idi.bidata.model.actions.snakes;

import edu.ntnu.idi.bidata.model.Board;
import edu.ntnu.idi.bidata.model.Player;
import edu.ntnu.idi.bidata.model.Tile;
import edu.ntnu.idi.bidata.model.actions.TileAction;
import edu.ntnu.idi.bidata.util.Logger; // Import the Logger

import java.util.Random;

/**
 * Represents a tile action for a "Schrödinger's Box".
 * When a player lands on this tile, they are presented with a choice
 * by the UI to "observe" the box (resulting in a random outcome) or "move on".
 * The perform() method signals that this special action should occur.
 * The actual choice and outcome logic are implemented in executeObserve and executeIgnore.
 */
public class SchrodingerBoxAction implements TileAction {

  private final String description;
  private final Random random = new Random(); // For determining observe outcome

  /**
   * Constructs a SchrodingerBoxAction with a default description.
   */
  public SchrodingerBoxAction() {
    this.description = "A mysterious Schrödinger's Box! Observe its contents or move on?";
  }

  /**
   * Constructs a SchrodingerBoxAction with a custom description.
   * If the provided description is null or empty, a default description is used.
   *
   * @param description The description of the Schrödinger's Box action.
   */
  public SchrodingerBoxAction(String description) {
    this.description = (description != null && !description.trim().isEmpty()) ?
        description.trim() :
        "A mysterious Schrödinger's Box! Observe its contents or move on?";
  }

  /**
   * Gets the description of this Schrödinger's Box action.
   *
   * @return The description string.
   */
  public String getDescription() {
    return description;
  }

  /**
   * Called when a player lands on a tile with this action.
   * This method signals that the player has landed on such a box.
   * The GameController will then prompt the UI for player choice and subsequently
   * call either executeObserve() or executeIgnore() on this action instance.
   *
   * @param player the player on which to perform the action (in this case, the one who landed)
   */
  @Override
  public void perform(Player player) {
    // This method signals that the player has landed on this special tile.
    // The GameController is responsible for detecting this and initiating the UI choice.
    Logger.info(player.getName() + " landed on: \"" + description + "\". Awaiting decision via UI.");
    // No direct game state change here related to the choice outcome itself;
    // that's handled by executeObserve/executeIgnore.
  }

  /**
   * Executes the "Observe" outcome of the Schrödinger's Box.
   * Moves the player to the start or finish tile randomly.
   *
   * @param player The player making the choice.
   * @param board The game board, needed to find start/finish tiles.
   * @return A message describing the outcome.
   */
  public String executeObserve(Player player, Board board) {
    Logger.info("Player " + player.getName() + " chose to OBSERVE the Schrödinger Box: \"" + description + "\"");
    boolean goToStart = random.nextBoolean();
    Tile targetTile;
    String outcomeMessage;

    if (board == null) {
      Logger.error("Schrödinger (Observe) error: Board is null for player " + player.getName());
      return player.getName() + " opened the box, but the fabric of reality seems broken (board missing)!";
    }
    if (board.getTiles().isEmpty()) {
      Logger.error("Schrödinger (Observe) error: Board has no tiles for player " + player.getName());
      return player.getName() + " opened the box, but the universe is empty (no tiles on board)!";
    }

    Tile startTile = board.getTile(0); // Assuming tile 0 is the start tile
    Tile endTile = board.getTile(board.getTiles().size() - 1); // Assuming last tile is the finish tile

    if (startTile == null) {
      Logger.error("Schrödinger (Observe) error: Start tile (tile 0) is null.");
      // Fallback: player stays, but this is an error state
      return player.getName() + " opened the box, but the start point is missing from reality!";
    }
    if (endTile == null) {
      Logger.error("Schrödinger (Observe) error: End tile (tile " + (board.getTiles().size() - 1) +") is null.");
      // Fallback: player stays, but this is an error state
      return player.getName() + " opened the box, but the end point is missing from reality!";
    }

    if (goToStart) {
      targetTile = startTile;
      outcomeMessage = player.getName() + " opened the box... Oh no! Sent back to the start (Tile " + targetTile.getId() + ")!";
      Logger.info("Schrödinger outcome (Observe): " + player.getName() + " sent to tile " + targetTile.getId() + " (START).");
    } else {
      targetTile = endTile;
      outcomeMessage = player.getName() + " opened the box... Unbelievable! Sent straight to the finish line (Tile " + targetTile.getId() + ")!";
      Logger.info("Schrödinger outcome (Observe): " + player.getName() + " sent to tile " + targetTile.getId() + " (FINISH).");
    }

    player.setCurrentTile(targetTile); // Move the player
    Logger.debug("Player " + player.getName() + " moved to tile " + targetTile.getId() + " due to Schrödinger Box (Observe).");

    return outcomeMessage;
  }

  /**
   * Executes the "Ignore" outcome of the Schrödinger's Box.
   * The player stays on the current tile.
   *
   * @param player The player making the choice.
   * @return A message describing the outcome.
   */
  public String executeIgnore(Player player) {
    String message = player.getName() + " cautiously decided to ignore: \"" + description + "\"";
    Logger.info("Player " + player.getName() + " chose to IGNORE the Schrödinger Box. No change in position.");
    // No player movement or game state change other than progressing the turn.
    return message;
  }
}