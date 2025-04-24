package edu.ntnu.idi.bidata.factory;

import edu.ntnu.idi.bidata.app.GameVariant;
import edu.ntnu.idi.bidata.model.BoardGame;
import edu.ntnu.idi.bidata.model.Dice;
import edu.ntnu.idi.bidata.model.Player;
import edu.ntnu.idi.bidata.service.MonopolyService;
import edu.ntnu.idi.bidata.service.SnakesLaddersService;

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
  public static BoardGame createGame(List<String> playerNames, GameVariant variant) {
    BoardGame game = new BoardGame();

    switch (variant) {
      case SNAKES_LADDERS:
        game.setBoard(BoardFactory.createFromJson("/data/boards/snakes_and_ladders.json"));
        game.setGameService(new SnakesLaddersService());
        game.setDice(new Dice(1));
        break;
      case MINI_MONOPOLY:
        game.setBoard(BoardFactory.createFromJson("/data/boards/mini_monopoly.json"));
        game.setGameService(new MonopolyService());
        game.setDice(new Dice(2)); // Monopoly typically uses two dice
        break;
    }

    game.init();
    for (String name : playerNames) {
      game.addPlayer(new Player(name, game.getBoard().getTile(0)));
    }
    return game;
  }
}
