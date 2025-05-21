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

  // --- Observer registration ---
  public void addObserver(BoardGameObserver observer) {
    if (observer == null) {
      throw new InvalidParameterException("Observer cannot be null");
    }
    observers.add(observer);
  }

  public void removeObserver(BoardGameObserver observer) {
    observers.remove(observer);
  }

  // New method to notify UI of generic game events/messages (optional but useful)
  // If you decide to use this, GameController would call it, and BoardGameObserver would need a new method.
  // For now, GameController updates SnakeLadderGameScene directly for messages.
  public void notifyGameEvent(String eventMessage, Player involvedPlayer) {
    for (BoardGameObserver obs : observers) {
      // Example: if (obs instanceof ExtendedBoardGameObserver extObs) {
      // extObs.onGameMessage(eventMessage, involvedPlayer);
      // }
    }
  }


  // --- Initialization ---
  public void init() {
    if (board == null) throw new IllegalStateException("Board must be set before init()");
    if (dice == null) throw new IllegalStateException("Dice must be set before init()");
    if (players.isEmpty()) throw new IllegalStateException("At least one player must be added before init()");
    if (service == null) throw new IllegalStateException("GameService must be set before init()");

    service.setup(this); // Let the service do its setup (e.g., set initial player positions, current player index)
    this.gameInitialized = true;
    notifyGameStart(); // Notify observers that the game is ready
  }

  // --- Gameplay ---
  /**
   * Plays exactly one roll/move for the given player.
   * The service will handle moving the player and calling tile.land(player).
   * After this, notifyRoundPlayed is called.
   */
  public void playTurn(Player player) {
    requireInitialized();
    if (!players.contains(player)) {
      throw new IllegalArgumentException("Player is not part of this game");
    }

    int roll = service.playTurn(this, player); // Service handles dice, move, and tile.land()

    // Notify that a dice roll part of the turn happened.
    // The GameController's onRoundPlayed will handle complex UI updates,
    // including checks for special actions like Schrödinger's Box.
    notifyRoundPlayed(List.of(roll));

    // The GameController will now handle the game over check in its onRoundPlayed
    // or after a choice is made (if a choice was pending).
    // This avoids a premature game over notification if a choice could change the outcome.
    // if (isFinished()) { // This check is now primarily driven by GameController after actions
    //     notifyGameOver(getWinner());
    // }
  }

  // --- Queries delegated to the service ---
  public boolean isFinished() {
    requireInitialized();
    return service.isFinished(this);
  }

  public boolean isGameStarted() {
    return this.gameInitialized;
  }

  public Player getWinner() {
    requireInitialized();
    return service.getWinner(this);
  }

  // --- Wiring ---
  public void setBoard(Board board) {
    if (board == null) throw new InvalidParameterException("Board cannot be null");
    this.board = board;
  }

  public void setDice(Dice dice) {
    if (dice == null) throw new InvalidParameterException("Dice cannot be null");
    this.dice = dice;
  }

  public void addPlayer(Player player) {
    if (player == null) throw new InvalidParameterException("Player cannot be null");
    players.add(player);
  }

  public void setGameService(GameService service) {
    if (service == null) throw new InvalidParameterException("GameService cannot be null");
    this.service = service;
  }

  // --- Simple getters ---
  public Board getBoard() { return board; }
  public Dice getDice()   { return dice; }
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
      // If game not started or service not set, behavior might depend on your GameService.
      // Returning null is a safe default.
      return null;
    }
    return service.getCurrentPlayer(this);
  }

  // --- Internals & notification helpers ---
  private void requireInitialized() {
    if (!gameInitialized || service == null) {
      throw new IllegalStateException("Game not fully initialized; call init() first and ensure service is set.");
    }
  }

  private void notifyGameStart() {
    for (var obs : observers) {
      obs.onGameStart(getPlayers());
    }
  }

  private void notifyRoundPlayed(List<Integer> rolls) {
    for (var obs : observers) {
      obs.onRoundPlayed(rolls, getPlayers());
    }
  }

  private void notifyGameOver(Player winner) {
    for (var obs : observers) {
      obs.onGameOver(winner);
    }
  }
}