package edu.ntnu.idi.bidata.model.actions;

import edu.ntnu.idi.bidata.model.Player;

public class SnakeAction implements TileAction {
  private int positionDecrement;
  private String description;

  public SnakeAction(String description, int positionDecrement) {
    this.description = description;
    this.positionDecrement = positionDecrement;
  }

  @Override
  public void perform(Player player) {
    System.out.println(description);
    player.move(-positionDecrement); // Move backward
  }
}
