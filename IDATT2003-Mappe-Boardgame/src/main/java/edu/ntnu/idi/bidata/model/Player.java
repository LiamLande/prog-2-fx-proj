package edu.ntnu.idi.bidata.model;

public class Player {
  private String name;
  private Tile currentTile;

  public Player(String name, BoardGame game) {
    this.name = name;
    this.currentTile = game.getBoard().getTile(0); // Start at the first tile
  }

  public void placeOnTile(Tile tile) {
    this.currentTile = tile;
  }

  public void move(int steps) {

  }
}
