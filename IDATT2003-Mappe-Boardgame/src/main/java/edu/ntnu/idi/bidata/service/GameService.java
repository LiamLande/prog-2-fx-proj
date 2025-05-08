// service/GameService.java
package edu.ntnu.idi.bidata.service;

import edu.ntnu.idi.bidata.model.BoardGame;
import edu.ntnu.idi.bidata.model.Player;
import java.util.List;

public interface GameService {

  /** Called once before any moves; e.g. reset all players to the start tile. */
  void setup(BoardGame game);

  /** Roll & move each player once, returning the list of dice‐rolls in order. */
  List<Integer> playOneRound(BoardGame game);

  /** Roll & move exactly one player; returns that player’s roll. */
  int playTurn(BoardGame game, Player player);

  /** True as soon as someone has reached the end. */
  boolean isFinished(BoardGame game);

  /** The first player whose current tile is the “finish”; or null. */
  Player getWinner(BoardGame game);

  /**
   * Gets the player whose turn it is currently.
   * @param game The BoardGame instance.
   * @return The current Player.
   */
  Player getCurrentPlayer(BoardGame game); // <<< ADD THIS LINE
}