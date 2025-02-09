package edu.ntnu.idi.bidata.model.actions;

import edu.ntnu.idi.bidata.model.Player;

public class LadderAction implements TileAction{
  private String description;
  private int positionIncrement;

  public LadderAction(String description, int positionIncrement) {
    this.description = description;
    this.positionIncrement = positionIncrement;
  }

  @Override
  public void perform(Player player) {
    player.move(positionIncrement);
  }
}
