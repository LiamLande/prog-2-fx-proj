package edu.ntnu.idi.bidata.factory;

import edu.ntnu.idi.bidata.model.BoardGame;
import edu.ntnu.idi.bidata.model.Dice;
import edu.ntnu.idi.bidata.model.Player;
import java.util.List;

/**
 * Factory to create a configured BoardGame instance from dynamic inputs.
 */
public final class GameFactory {
  private GameFactory() { }

  /**
   * Builds a game using a JSON-configured board and user-supplied players.
   * @param playerNames list of player names
   * @return initialized BoardGame (need to call init() before play)
   */
  public static BoardGame createGame(List<String> playerNames) {
    BoardGame game = new BoardGame();
    // Load board from JSON
    game.setBoard(BoardFactory.createDefaultBoard());
    // Configure dice
    game.setDice(new Dice(1));
    // Setup facade (delegates to service)
    game.init();
    // Add players
    for (String name : playerNames) {
      game.addPlayer(new Player(name, game.getBoard().getTile(0)));
    }
    return game;
  }
}
