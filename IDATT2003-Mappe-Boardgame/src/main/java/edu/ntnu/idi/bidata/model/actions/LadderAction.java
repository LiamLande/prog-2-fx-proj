package edu.ntnu.idi.bidata.model.actions;

import edu.ntnu.idi.bidata.model.Player;

public class LadderAction implements TileAction{
  private int destinationTileId;
  private String description;

  public LadderAction(int destinationTileId, String description) {
    this.destinationTileId = destinationTileId;
    this.description = description;
  }

  @Override
  public void perform(Player player) {

  }
}
