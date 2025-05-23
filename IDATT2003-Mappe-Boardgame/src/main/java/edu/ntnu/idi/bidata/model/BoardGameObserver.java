package edu.ntnu.idi.bidata.model;

import java.util.List;

/**
 * Observer interface for BoardGame events.
 */
public interface BoardGameObserver {
  /** Called when the game is initialized and ready to start. */
  void onGameStart(List<Player> players);

  /** Called after each round is played. */
  void onRoundPlayed(List<Integer> rolls, List<Player> players);

  /** Called when the game ends and a winner is determined. */
  void onGameOver(Player winner);
}