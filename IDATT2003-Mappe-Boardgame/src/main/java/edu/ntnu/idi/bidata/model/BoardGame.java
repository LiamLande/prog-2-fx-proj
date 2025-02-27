package edu.ntnu.idi.bidata.model;

import edu.ntnu.idi.bidata.model.actions.LadderAction;
import edu.ntnu.idi.bidata.model.actions.SnakeAction;
import java.util.ArrayList;
import java.util.List;

public class BoardGame {
  private Board board;
  private Player currentPlayer;
  private List<Player> players = new ArrayList<>();
  private Dice dice;

  public void addPlayer(Player player) {
    players.add(player);
  }

  public void createBoard() {
    board = new Board();
    // Create 100 tiles for the board
    for (int i = 0; i < 100; i++) {
      board.addTile(new Tile(i));
    }

    setupLaddersAndSnakes();
  }

  private void setupLaddersAndSnakes() {
    // Ensure all tiles are linked correctly
    for (int i = 0; i < 100; i++) {
      board.getTile(i).setNextTile(board.getTile(i + 1));
      if (i < 99) {
        board.getTile(i + 1).setPreviousTile(board.getTile(i));
      }
    }

    // Ladders (Move Forward)
    board.getTile(3).setLandAction(new LadderAction("Ladder to 22!", 19)); // 3 -> 22
    board.getTile(8).setLandAction(new LadderAction("Ladder to 30!", 22)); // 8 -> 30
    board.getTile(28).setLandAction(new LadderAction("Ladder to 84!", 56)); // 28 -> 84
    board.getTile(58).setLandAction(new LadderAction("Ladder to 77!", 19)); // 58 -> 77

    // Snakes (Move Backward)
    board.getTile(16).setLandAction(new SnakeAction("Snake down to 6!", 10)); // 16 -> 6
    board.getTile(47).setLandAction(new SnakeAction("Snake down to 26!", 21)); // 47 -> 26
    board.getTile(62).setLandAction(new SnakeAction("Snake down to 18!", 44)); // 62 -> 18
    board.getTile(87).setLandAction(new SnakeAction("Snake down to 24!", 63)); // 87 -> 24
  }


  public void createDice() {
    dice = new Dice(1);
  }

  public void play() {
    while (!gameOver()) {
      for (Player player : players) {
        currentPlayer = player;
        int steps = dice.roll();
        System.out.println(player.getName() + " rolled a " + steps);
        player.move(steps);
        System.out.println(player.getName() + " is now on tile " + player.getCurrentTile().getTileId());
        if (player.getCurrentTile().getTileId() >= 99) {
          System.out.println(player.getName() + " wins!");
          return;
        }
      }
    }
  }

  public boolean gameOver() {
    for (Player player : players) {
      if (player.getCurrentTile().getTileId() >= 99) {
        return true;
      }
    }
    return false;
  }

  public Player getWinner() {
    for (Player player : players) {
      if (player.getCurrentTile().getTileId() == 99) {
        return player;
      }
    }
    return null;
  }

    public static String[] generateExampleGame(){
        return new String[]{"Round 1: John Doe rolled 5 and 6", "Round 2: Jane Doe rolled 2 and 3", "Round 3: John Doe rolled 1 and 4", "Round 4: Jane Doe rolled 2 and 2", "Round 5: John Doe rolled 3 and 6", "Round 6: Jane Doe rolled 1 and 5", "Round 7: John Doe rolled 4 and 6", "Round 8: Jane Doe rolled 1 and 3", "Round 9: John Doe rolled 2 and 5", "Round 10: Jane Doe rolled 1 and 6"};
    }


  public Board getBoard() {
    return board;
  }
}