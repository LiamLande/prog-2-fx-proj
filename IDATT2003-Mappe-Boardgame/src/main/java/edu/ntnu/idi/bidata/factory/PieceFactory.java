package edu.ntnu.idi.bidata.factory;

import edu.ntnu.idi.bidata.model.Player;
import edu.ntnu.idi.bidata.model.Board;
import edu.ntnu.idi.bidata.model.Tile;

import java.util.ArrayList;
import java.util.List;

/**
 * Factory to create Player instances placed on the board.
 */
public final class PieceFactory {
  private PieceFactory() { }

  /**
   * Creates a list of Player objects at the start tile (id=0).
   */
  public static List<Player> createPlayers(List<String> names, Board board) {
    Tile start = board.getTile(0);
    List<Player> players = new ArrayList<>(names.size());
    for (String name : names) {
      players.add(new Player(name, start));
    }
    return players;
  }
}