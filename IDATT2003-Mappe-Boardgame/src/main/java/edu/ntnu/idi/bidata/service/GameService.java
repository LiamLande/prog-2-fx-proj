// service/GameService.java
package edu.ntnu.idi.bidata.service;

import edu.ntnu.idi.bidata.model.BoardGame;
import edu.ntnu.idi.bidata.model.Player;
import java.util.List;

/**
 * Defines core game operations: setup, play a round, and finish detection.
 */
public interface GameService {
  /**
   * Prepares the game for play (e.g., initialize state).
   */
  void setup(BoardGame game);

  /**
   * Plays one full round: each player rolls and moves.
   * @return list of dice rolls in player order
   */
  List<Integer> playOneRound(BoardGame game);

  /**
   * Checks if the game has reached its end condition.
   */
  boolean isFinished(BoardGame game);

  /**
   * Returns the winning player if the game is finished, else null.
   */
  Player getWinner(BoardGame game);
}