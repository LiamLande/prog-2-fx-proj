// service/SnakesLaddersService.java
package edu.ntnu.idi.bidata.service;

import edu.ntnu.idi.bidata.model.BoardGame;
import edu.ntnu.idi.bidata.model.Player;
import edu.ntnu.idi.bidata.model.Tile;
import java.util.ArrayList;
import java.util.List;

/**
 * Snakes & Ladders game logic (Level 1).
 */
public class SnakesLaddersService implements GameService {

  @Override
  public void setup(BoardGame game) {
    // put every player on the “start” tile
    Tile start = game.getBoard().getStart();
    for (Player p : game.getPlayers()) {
      p.setCurrent(start);
    }
  }

  @Override
  public List<Integer> playOneRound(BoardGame game) {
    List<Integer> rolls = new ArrayList<>();
    for (Player player : game.getPlayers()) {
      int roll = game.getDice().roll();
      player.move(roll);          // assumes Player.move applies snakes/ladders
      rolls.add(roll);
      if (isFinished(game)) {
        break;
      }
    }
    return rolls;
  }

  @Override
  public int playTurn(BoardGame game, Player player) {
    int roll = game.getDice().roll();
    player.move(roll);
    return roll;
  }

  @Override
  public boolean isFinished(BoardGame game) {
    return game.getPlayers().stream()
        .anyMatch(p -> p.getCurrent().getNext() == null);
  }

  @Override
  public Player getWinner(BoardGame game) {
    return game.getPlayers().stream()
        .filter(p -> p.getCurrent().getNext() == null)
        .findFirst()
        .orElse(null);
  }
}
