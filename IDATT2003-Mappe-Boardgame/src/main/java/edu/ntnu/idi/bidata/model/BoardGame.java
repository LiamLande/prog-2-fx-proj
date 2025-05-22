package edu.ntnu.idi.bidata.model;

import edu.ntnu.idi.bidata.exception.InvalidParameterException;
import edu.ntnu.idi.bidata.service.GameService;
import java.util.ArrayList;
import java.util.List;

/**
 * Facade for game setup and play.
 * Delegates to a GameService implementation and notifies observers.
 */
public class BoardGame {
  private Board board;
  private Dice dice;
  private final List<Player> players = new ArrayList<>();
  private GameService service;
  private final List<BoardGameObserver> observers = new ArrayList<>();
  private boolean gameInitialized = false;

  /**
   * Adds an observer to be notified of game events.
   *
   * @param observer The observer to add.
   * @throws InvalidParameterException if the observer is null.
   */
  public void addObserver(BoardGameObserver observer) {
    if (observer == null) {
      throw new InvalidParameterException("Observer cannot be null");
    }
    observers.add(observer);
  }

  /**
   * Initializes the game. This method must be called after setting the board, dice, players, and game service.
   * It sets up the game through the game service and notifies observers that the game has started.
   *
   * @throws IllegalStateException if the board, dice, players, or game service are not set before initialization.
   */
  public void init() {
    if (board == null) throw new IllegalStateException("Board must be set before init()");
    if (dice == null) throw new IllegalStateException("Dice must be set before init()");
    if (players.isEmpty()) throw new IllegalStateException("At least one player must be added before init()");
    if (service == null) throw new IllegalStateException("GameService must be set before init()");

    service.setup(this);
    this.gameInitialized = true;
    notifyGameStart(); // Notify observers that the game is ready
  }

  /**
   * Plays exactly one roll/move for the given player.
   * The service will handle moving the player and calling tile.land(player).
   * After this, notifyRoundPlayed is called.
   *
   * @param player The player whose turn it is.
   * @throws IllegalStateException if the game is not initialized.
   * @throws IllegalArgumentException if the player is not part of this game.
   */
  public void playTurn(Player player) {
    requireInitialized();
    if (!players.contains(player)) {
      throw new IllegalArgumentException("Player is not part of this game");
    }

    int roll = service.playTurn(this, player); // Service handles dice, move, and tile.land()

    notifyRoundPlayed(List.of(roll));
  }

  /**
   * Checks if the game has finished.
   * Delegated to the GameService.
   *
   * @return true if the game has finished, false otherwise.
   * @throws IllegalStateException if the game is not initialized.
   */
  public boolean isFinished() {
    requireInitialized();
    return service.isFinished(this);
  }

  /**
   * Checks if the game has been initialized and started.
   *
   * @return true if the game has started, false otherwise.
   */
  public boolean isGameStarted() {
    return this.gameInitialized;
  }

  /**
   * Gets the winner of the game.
   * Delegated to the GameService.
   *
   * @return The winning Player, or null if there is no winner yet.
   * @throws IllegalStateException if the game is not initialized.
   */
  public Player getWinner() {
    requireInitialized();
    return service.getWinner(this);
  }

  /**
   * Sets the game board.
   *
   * @param board The game board.
   * @throws InvalidParameterException if the board is null.
   */
  public void setBoard(Board board) {
    if (board == null) throw new InvalidParameterException("Board cannot be null");
    this.board = board;
  }

  /**
   * Sets the dice for the game.
   *
   * @param dice The dice to be used.
   * @throws InvalidParameterException if the dice are null.
   */
  public void setDice(Dice dice) {
    if (dice == null) throw new InvalidParameterException("Dice cannot be null");
    this.dice = dice;
  }

  /**
   * Adds a player to the game.
   *
   * @param player The player to add.
   * @throws InvalidParameterException if the player is null.
   */
  public void addPlayer(Player player) {
    if (player == null) throw new InvalidParameterException("Player cannot be null");
    players.add(player);
  }

  /**
   * Sets the game service that will manage the game logic.
   *
   * @param service The game service.
   * @throws InvalidParameterException if the service is null.
   */
  public void setGameService(GameService service) {
    if (service == null) throw new InvalidParameterException("GameService cannot be null");
    this.service = service;
  }

  /**
   * Gets the game board.
   *
   * @return The game board.
   */
  public Board getBoard() { return board; }

  /**
   * Gets the dice used in the game.
   *
   * @return The dice.
   */
  public Dice getDice()   { return dice; }

  /**
   * Gets a list of players in the game.
   * Returns a defensive copy of the list.
   *
   * @return A list of players.
   */
  public List<Player> getPlayers() {
    return new ArrayList<>(players); // Defensive copy
  }

  /**
   * Gets the current player whose turn it is.
   * Delegated to the GameService.
   * @return The current Player, or null if not applicable or game not started.
   */
  public Player getCurrentPlayer() {
    if (!isGameStarted() || service == null) {
      return null;
    }
    return service.getCurrentPlayer(this);
  }

  /**
   * Ensures that the game has been initialized before certain operations are performed.
   *
   * @throws IllegalStateException if the game is not initialized or the service is not set.
   */
  private void requireInitialized() {
    if (!gameInitialized || service == null) {
      throw new IllegalStateException("Game not fully initialized; call init() first and ensure service is set.");
    }
  }

  /**
   * Notifies all registered observers that the game has started.
   */
  private void notifyGameStart() {
    for (var obs : observers) {
      obs.onGameStart(getPlayers());
    }
  }

  /**
   * Notifies all registered observers that a round has been played.
   *
   * @param rolls The list of dice rolls in the round.
   */
  private void notifyRoundPlayed(List<Integer> rolls) {
    for (var obs : observers) {
      obs.onRoundPlayed(rolls, getPlayers());
    }
  }
}