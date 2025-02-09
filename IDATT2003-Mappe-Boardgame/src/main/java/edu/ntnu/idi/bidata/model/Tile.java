package edu.ntnu.idi.bidata.model;

import edu.ntnu.idi.bidata.model.actions.TileAction;

public class Tile {
  private final int tileId;
  private Tile nextTile;
  private TileAction landAction;

  public Tile(int tileId) {
    this.tileId = tileId;
  }

  public void landPlayer(Player player) {
    if (landAction != null) {
      landAction.perform(player);
    }
  }

  public void leavePlayer(Player player) {

  }

  public void setNextTile(Tile nextTile) {
    this.nextTile = nextTile;
  }

  public int getTileId() {
    return tileId;
  }

}
