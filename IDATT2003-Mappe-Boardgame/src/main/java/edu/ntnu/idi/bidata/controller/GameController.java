package edu.ntnu.idi.bidata.controller;

import edu.ntnu.idi.bidata.model.BoardGame;
import edu.ntnu.idi.bidata.model.Player;
import edu.ntnu.idi.bidata.view.GameView;
import java.util.List;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;

public class GameController {
  private final BoardGame game;
  private final GameView view;
  private int round = 1;

  public GameController() {
    game = new BoardGame();
    game.createBoard();
    game.createDice();

    view = new GameView();

    // Handle player addition
    view.getAddPlayerButton().setOnAction(event -> addPlayer());

    // Handle turn progression
    view.getNextRoundButton().setOnAction(event -> nextRound());
  }

  private void addPlayer() {
    String name = view.getPlayerNameInput().getText().trim();
    if (!name.isEmpty()) {
      game.addPlayer(new Player(name, game));
      view.updatePlayerList(game.getPlayers());
      view.getPlayerNameInput().clear();
    } else {
      view.updateStatus("Enter a valid name.");
    }
  }

  private void nextRound() {
    if (game.getPlayers().size() < 2) {
      view.updateStatus("Need at least 2 players to play.");
      return;
    }
    List<Player> players = game.getPlayers();
    for (Player player : players) {
      if (player.getCurrentTile().getTileId() >= 99) {
        view.updateStatus(player.getName() + " has won the game!");
        view.getNextRoundButton().setDisable(true);
        return;
      }
    }
    game.play();
    view.updatePlayerList(game.getPlayers());
    view.updateStatus("Round: " + round + " complete!");
    round += 1;
  }

  public GameView getView() {
    return view;
  }
}
