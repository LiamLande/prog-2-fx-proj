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

import java.util.List;
import java.util.stream.Collectors;

/**
 * Factory to create a configured BoardGame instance from dynamic inputs.
 */
public final class GameFactory {
  private GameFactory() { }

  // --- Public Factory Methods ---

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
    // Directly call the internal method that handles PlayerSetupData for S&L
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
      // Call the specific S&L method, which then calls the internal one
      return createSnakesLaddersGameWithDetails(details, defaultTheme);
    } else {
      // For Monopoly or other variants not using themes or detailed setup here
      return createThemedGameInternal(variant, null, null, playerNames);
    }
  }

  /**
   * Creates a Snakes and Ladders game using player names and a theme, assigning default pieces.
   * This method can be kept if there's a specific use case for it, but ensure its name or
   * parameters differ enough from the one taking PlayerSetupData if it also had a List parameter.
   * To avoid the original clash, this method could also be renamed or one of its parameters changed.
   * Given the solution above (renaming the one with PlayerSetupData), this one *might* be okay
   * if it's only called with List<String>. However, to be safe, let's make it also more specific
   * or rely on the general createGame(List<String>, GameVariant) to handle S&L with defaults.
   *
   * Let's make this method also call the specific S&L creator to centralize logic.
   */
  public static BoardGame createGame(List<String> playerNames, GameVariant variant, SnakeLadderPlayerSetupScene.Theme theme) {
    if (variant != GameVariant.SNAKES_LADDERS) {
      // If it's not S&L, but theme is provided, it's a bit ambiguous.
      // This path should ideally go through createGame(playerNames, variant) which handles non-S&L
      // or S&L with default theme. If a specific theme is given for non-S&L, that's a design question.
      // For now, assuming this is primarily for S&L.
      throw new IllegalArgumentException("Themed game creation with List<String> is primarily for S&L. Use createGame(playerNames, variant) for other types.");
    }
    List<PlayerSetupData> details = playerNames.stream()
        .map(name -> new PlayerSetupData(name, Player.DEFAULT_PIECE_IDENTIFIER))
        .collect(Collectors.toList());
    return createSnakesLaddersGameWithDetails(details, theme);
  }


  // --- Internal Game Creation Logic ---
  // This internal method is now called by the more specific public methods.
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
      System.err.println("Error: Board or starting tile 0 not found for game variant: " + variant + ". Cannot add players.");
    } else {
      if (variant == GameVariant.SNAKES_LADDERS && slPlayerSetupDetails != null) {
        for (PlayerSetupData detail : slPlayerSetupDetails) {
          game.addPlayer(new Player(detail.name(), startTile, detail.pieceIdentifier()));
        }
      } else if (variant == GameVariant.MINI_MONOPOLY && monopolyPlayerNames != null) {
        for (String name : monopolyPlayerNames) {
          game.addPlayer(new Player(name, startTile, Player.DEFAULT_PIECE_IDENTIFIER, 1500));
        }
      }
    }

    game.init();
    return game;
  }
}