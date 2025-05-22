package edu.ntnu.idi.bidata.service;

import edu.ntnu.idi.bidata.model.BoardGame;
import edu.ntnu.idi.bidata.model.Player;
import edu.ntnu.idi.bidata.model.Tile;

/**
 * Implements the {@link GameService} interface for a Snakes and Ladders game.
 * Manages player turns, game setup, and determines the game's finished state and winner.
 */
public class SnakesLaddersService implements GameService {
  private int currentPlayerIndex = -1; // Index in the game.getPlayers() list

  /**
   * Sets up the Snakes and Ladders game.
   * Places all players on the starting tile and sets the first player as the current player.
   *
   * @param game The {@link BoardGame} instance to set up.
   */
  @Override
  public void setup(BoardGame game) {
    Tile start = game.getBoard().getStart();
    for (Player p : game.getPlayers()) {
      p.setCurrentTile(start); // Use setCurrentTile for clarity if Player has it
    }
    if (!game.getPlayers().isEmpty()) {
      this.currentPlayerIndex = 0; // First player starts
    } else {
      this.currentPlayerIndex = -1;
    }
  }

  /**
   * Gets the current player whose turn it is.
   *
   * @param game The {@link BoardGame} instance.
   * @return The current {@link Player}, or null if no current player is set or the player list is out of bounds.
   */
  @Override
  public Player getCurrentPlayer(BoardGame game) {
    if (this.currentPlayerIndex >= 0 && this.currentPlayerIndex < game.getPlayers().size()) {
      return game.getPlayers().get(this.currentPlayerIndex);
    }
    return null;
  }

  /**
   * Plays a turn for the given player in the Snakes and Ladders game.
   * The player rolls the dice and moves on the board. Tile actions (snakes, ladders) are handled by the Player.move() method.
   * Advances to the next player if the game is not finished.
   *
   * @param game The {@link BoardGame} instance.
   * @param player The {@link Player} whose turn it is.
   * @return The result of the dice roll.
   * @throws IllegalArgumentException if the specified player is not in the game or it is not their turn.
   */
  @Override
  public int playTurn(BoardGame game, Player player) {
    // Ensure the player passed IS the current player according to our index
    if (game.getPlayers().isEmpty() || !player.equals(game.getPlayers().get(this.currentPlayerIndex))) {
      // Or, if you trust the controller, find the index of 'player' and set it.
      int newIndex = game.getPlayers().indexOf(player);
      if (newIndex == -1) {
        throw new IllegalArgumentException("Player " + player.getName() + " is not in the game or not their turn.");
      }
      this.currentPlayerIndex = newIndex;
    }

    int roll = game.getDice().rollDie();
    player.move(roll); // Player.move handles tile actions internally

    // Advance to the next player for the *next* turn
    if (!isFinished(game) && !game.getPlayers().isEmpty()) {
      this.currentPlayerIndex = (this.currentPlayerIndex + 1) % game.getPlayers().size();
    }
    return roll;
  }

  /**
   * Checks if the Snakes and Ladders game has finished.
   * The game is finished if any player has reached the final tile (a tile where getNext() is null).
   *
   * @param game The {@link BoardGame} instance.
   * @return true if the game has finished, false otherwise.
   */
  @Override
  public boolean isFinished(BoardGame game) {
    return game.getPlayers().stream()
            .anyMatch(p -> p.getCurrentTile().getNext() == null); // Assuming getNext being null means end
  }

  /**
   * Gets the winner of the Snakes and Ladders game.
   * The winner is the first player to reach the final tile.
   *
   * @param game The {@link BoardGame} instance.
   * @return The winning {@link Player}, or null if there is no winner yet.
   */
  @Override
  public Player getWinner(BoardGame game) {
    return game.getPlayers().stream()
            .filter(p -> p.getCurrentTile().getNext() == null)
            .findFirst()
            .orElse(null);
  }
}