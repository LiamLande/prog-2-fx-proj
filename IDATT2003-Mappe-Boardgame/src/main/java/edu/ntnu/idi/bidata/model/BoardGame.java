package edu.ntnu.idi.bidata.model;

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

  }

  public void createDice() {
    dice = new Dice(1);
  }

  public void play() {

  }

  public Player getWinner() {
    return null;
  }

  public Board getBoard() {
    return board;
  }

}
