package edu.ntnu.idi.bidata.model;

import edu.ntnu.idi.bidata.model.actions.TileAction;

public class Tile {
  private final int tileId;
  private Tile nextTile;
  private TileAction landAction;
  private Tile previousTile;

  public Tile(int tileId) {
    this.tileId = tileId;
  }

  public void landPlayer(Player player) {
    if (landAction != null) {
      landAction.perform(player);
    }
  }

  public void setLandAction(TileAction landAction) {
    this.landAction = landAction;
  }

  public void leavePlayer(Player player) {
    // Implement logic for when a player leaves the tile (if needed)
  }

  public void setPreviousTile(Tile previousTile) {
    this.previousTile = previousTile;
  }

  public Tile getPreviousTile() {
    return previousTile;
  }

  public void setNextTile(Tile nextTile) {
    this.nextTile = nextTile;
  }

  public Tile getNextTile() {
    return nextTile;
  }

  public int getTileId() {
    return tileId;
  }
}