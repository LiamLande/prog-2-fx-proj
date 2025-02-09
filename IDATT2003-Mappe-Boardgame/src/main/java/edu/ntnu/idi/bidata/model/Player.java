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
    if (steps > 0) { // Move forward
      for (int i = 0; i < steps; i++) {
        if (currentTile.getNextTile() != null) {
          currentTile = currentTile.getNextTile();
        } else {
          break;
        }
      }
    } else if (steps < 0) { // Move backward
      for (int i = 0; i < Math.abs(steps); i++) {
        if (currentTile.getPreviousTile() != null) {
          currentTile = currentTile.getPreviousTile();
        } else {
          break;
        }
      }
    }

    currentTile.landPlayer(this); // Trigger tile action (ladder/snake)
  }


  public Tile getCurrentTile() {
    return currentTile;
  }

  public String getName() {
    return name;
  }
}