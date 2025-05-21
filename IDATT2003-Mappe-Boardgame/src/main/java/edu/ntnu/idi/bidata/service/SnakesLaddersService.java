package edu.ntnu.idi.bidata.service;

import edu.ntnu.idi.bidata.model.BoardGame;
import edu.ntnu.idi.bidata.model.Player;
import edu.ntnu.idi.bidata.model.Tile;
import java.util.ArrayList;
import java.util.List;

public class SnakesLaddersService implements GameService {
  private int currentPlayerIndex = -1; // Index in the game.getPlayers() list

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

  @Override
  public Player getCurrentPlayer(BoardGame game) {
    if (this.currentPlayerIndex >= 0 && this.currentPlayerIndex < game.getPlayers().size()) {
      return game.getPlayers().get(this.currentPlayerIndex);
    }
    return null;
  }

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

  @Override
  public boolean isFinished(BoardGame game) {
    return game.getPlayers().stream()
            .anyMatch(p -> p.getCurrentTile().getNext() == null); // Assuming getNext being null means end
  }

  @Override
  public Player getWinner(BoardGame game) {
    return game.getPlayers().stream()
            .filter(p -> p.getCurrentTile().getNext() == null)
            .findFirst()
            .orElse(null);
  }
}