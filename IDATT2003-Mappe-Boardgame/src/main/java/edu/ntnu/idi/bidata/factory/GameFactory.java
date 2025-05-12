package edu.ntnu.idi.bidata.factory;

import edu.ntnu.idi.bidata.app.GameVariant;
// import edu.ntnu.idi.bidata.file.CardJsonReaderWriter; // Not used in this snippet
import edu.ntnu.idi.bidata.model.BoardGame;
// import edu.ntnu.idi.bidata.model.Card; // Not used in this snippet
import edu.ntnu.idi.bidata.model.Dice;
import edu.ntnu.idi.bidata.model.Player;
import edu.ntnu.idi.bidata.service.CardService;
import edu.ntnu.idi.bidata.service.MonopolyService;
import edu.ntnu.idi.bidata.service.ServiceLocator;
import edu.ntnu.idi.bidata.service.SnakesLaddersService;
import edu.ntnu.idi.bidata.ui.SnakeLadderPlayerSetupScene; // Import Theme enum

// import java.io.IOException; // Not used in this snippet
// import java.io.InputStreamReader; // Not used in this snippet
// import java.io.Reader; // Not used in this snippet
import java.util.List;
// import java.util.Map; // Not used in this snippet


/**
 * Factory to create a configured BoardGame instance from dynamic inputs.
 */
public final class GameFactory {
  private GameFactory() { }

  // Overload createGame for non-themed games or default theme
  public static BoardGame createGame(List<String> playerNames, GameVariant variant) {
    // For S&L, default to EGYPT theme if no theme is specified
    SnakeLadderPlayerSetupScene.Theme defaultTheme = SnakeLadderPlayerSetupScene.Theme.EGYPT;
    if (variant == GameVariant.SNAKES_LADDERS) {
      return createGame(playerNames, variant, defaultTheme);
    }
    // For Monopoly, theme is not currently used, so proceed as before
    return createThemedGameInternal(playerNames, variant, null); // Pass null for theme if not S&L
  }


  /**
   * Builds a game using a JSON-configured board and user-supplied players, considering theme for S&L.
   * @param playerNames list of player names
   * @param variant the game variant
   * @param theme the theme for Snakes & Ladders (can be null for other variants)
   * @return initialized BoardGame (need to call init() before play)
   */
  public static BoardGame createGame(List<String> playerNames, GameVariant variant, SnakeLadderPlayerSetupScene.Theme theme) {
    return createThemedGameInternal(playerNames, variant, theme);
  }

  private static BoardGame createThemedGameInternal(List<String> playerNames, GameVariant variant, SnakeLadderPlayerSetupScene.Theme theme) {
    BoardGame game = new BoardGame();
    String boardJsonPath;

    switch (variant) {
      case SNAKES_LADDERS:
        if (theme == SnakeLadderPlayerSetupScene.Theme.JUNGLE) {
          boardJsonPath = "/data/boards/snakes_and_ladders_JUNGLE.json";
        } else { // Default to EGYPT or if theme is null
          boardJsonPath = "/data/boards/snakes_and_ladders.json";
        }
        game.setBoard(BoardFactory.createFromJson(boardJsonPath, variant, theme)); // Pass theme to BoardFactory
        game.setGameService(new SnakesLaddersService());
        game.setDice(new Dice(2));
        break;
      case MINI_MONOPOLY:
        boardJsonPath = "/data/boards/mini_monopoly.json";
        // Monopoly currently doesn't use theme for board creation in this setup
        game.setBoard(BoardFactory.createFromJson(boardJsonPath, variant, null)); // Pass null theme
        MonopolyService monopolyService = new MonopolyService();
        game.setGameService(monopolyService);
        ServiceLocator.setMonopolyService(monopolyService);
        game.setDice(new Dice(2));
        CardService cardService = CardFactory.createCardServiceFromJson("/data/cards/cards.json");
        monopolyService.setCardService(cardService);
        break;
      default:
        throw new IllegalArgumentException("Unsupported game variant: " + variant);
    }

    for (String name : playerNames) {
      // Ensure getTile(0) exists on the loaded board
      if (game.getBoard() != null && game.getBoard().getTile(0) != null) {
        game.addPlayer(new Player(name, game.getBoard().getTile(0)));
      } else {
        System.err.println("Error: Board or starting tile 0 not found for player " + name);
        // Handle error, perhaps throw exception or skip player
      }
    }
    game.init(); // Initialize game (e.g., set current player)
    return game;
  }
}