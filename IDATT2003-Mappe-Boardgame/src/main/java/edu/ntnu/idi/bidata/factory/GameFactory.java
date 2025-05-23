package edu.ntnu.idi.bidata.factory;

import edu.ntnu.idi.bidata.app.GameVariant;
import edu.ntnu.idi.bidata.model.BoardGame;
import edu.ntnu.idi.bidata.model.Dice;
import edu.ntnu.idi.bidata.model.Player;
import edu.ntnu.idi.bidata.model.Tile;
import edu.ntnu.idi.bidata.service.CardService;
import edu.ntnu.idi.bidata.service.MonopolyService;
import edu.ntnu.idi.bidata.service.ServiceLocator;
import edu.ntnu.idi.bidata.service.SnakesLaddersService;
import edu.ntnu.idi.bidata.ui.sl.SnakeLadderPlayerSetupScene;
import edu.ntnu.idi.bidata.ui.PlayerSetupData;

import edu.ntnu.idi.bidata.util.Logger;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Factory to create a configured BoardGame instance from dynamic inputs.
 */
public final class GameFactory {
  private GameFactory() { }

  /**
   * Creates a Snakes & Ladders game using detailed player setup including piece identifiers and theme.
   *
   * @param playerSetupDetails List of PlayerSetupData containing names and piece identifiers.
   * @param theme              The theme for Snakes & Ladders.
   * @return An initialized BoardGame.
   */
  public static BoardGame createSnakesLaddersGameWithDetails(
      List<PlayerSetupData> playerSetupDetails,
      SnakeLadderPlayerSetupScene.Theme theme) {
    return createThemedGameInternal(GameVariant.SNAKES_LADDERS, theme, playerSetupDetails, null);
  }

  /**
   * Creates a game (typically Monopoly or S&L with default pieces) using only player names.
   * This is the general purpose creator.
   *
   * @param playerNames List of player names.
   * @param variant     The game variant.
   * @return An initialized BoardGame.
   */
  public static BoardGame createGame(List<String> playerNames, GameVariant variant) {
    if (variant == GameVariant.SNAKES_LADDERS) {
      // For S&L, if only names are provided, use default theme and default pieces
      SnakeLadderPlayerSetupScene.Theme defaultTheme = SnakeLadderPlayerSetupScene.Theme.EGYPT;
      List<PlayerSetupData> details = playerNames.stream()
          .map(name -> new PlayerSetupData(name, Player.DEFAULT_PIECE_IDENTIFIER))
          .collect(Collectors.toList());
      return createSnakesLaddersGameWithDetails(details, defaultTheme);
    } else {
      return createThemedGameInternal(variant, null, null, playerNames);
    }
  }

  /**
   * Creates a game with the specified variant, theme, and player setup details.
   * This method is private and should not be called directly.
   *
   * @param variant             The game variant (e.g., SNAKES_LADDERS or MINI_MONOPOLY).
   * @param theme               The theme for Snakes & Ladders (can be null for other games).
   * @param slPlayerSetupDetails List of PlayerSetupData for Snakes & Ladders (can be null for Monopoly).
   * @param monopolyPlayerNames  List of player names for Monopoly (can be null for S&L).
   * @return An initialized BoardGame.
   */
  private static BoardGame createThemedGameInternal(
      GameVariant variant,
      SnakeLadderPlayerSetupScene.Theme theme,       // Can be null if not S&L
      List<PlayerSetupData> slPlayerSetupDetails,    // For S&L, can be null if monopolyPlayerNames is used
      List<String> monopolyPlayerNames) {            // For Monopoly, can be null if slPlayerSetupDetails is used

    BoardGame game = new BoardGame();
    String boardJsonPath;

    switch (variant) {
      case SNAKES_LADDERS:
        if (slPlayerSetupDetails == null || slPlayerSetupDetails.isEmpty()) {
          throw new IllegalArgumentException("PlayerSetupData must be provided for Snakes & Ladders.");
        }
        if (theme == SnakeLadderPlayerSetupScene.Theme.JUNGLE) {
          boardJsonPath = "/data/boards/snakes_and_ladders_JUNGLE.json";
        } else {
          boardJsonPath = "/data/boards/snakes_and_ladders.json";
        }

        game.setBoard(BoardFactory.createFromJson(boardJsonPath, variant, theme));
        game.setGameService(new SnakesLaddersService());
        game.setDice(new Dice(2));
        break;
      case MINI_MONOPOLY:
        if (monopolyPlayerNames == null || monopolyPlayerNames.isEmpty()) {
          throw new IllegalArgumentException("Player names must be provided for Mini Monopoly.");
        }

        boardJsonPath = "/data/boards/mini_monopoly.json";
        game.setBoard(BoardFactory.createFromJson(boardJsonPath, variant, null));

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

    Tile startTile = null;
    if (game.getBoard() != null) {
      startTile = game.getBoard().getTile(0);
    }

    if (startTile == null) {
      Logger.error("Error: Board or starting tile 0 not found for game variant: " + variant + ". Cannot add players.");
    } else {
      if (variant == GameVariant.SNAKES_LADDERS) {
        for (PlayerSetupData detail : slPlayerSetupDetails) {
          game.addPlayer(new Player(detail.name(), startTile, detail.pieceIdentifier()));
        }
      } else {
        for (String name : monopolyPlayerNames) {
          game.addPlayer(new Player(name, startTile, Player.DEFAULT_PIECE_IDENTIFIER, 1500));
        }
      }
    }

    game.init();
    return game;
  }
}