package edu.ntnu.idi.bidata.model.actions;


import edu.ntnu.idi.bidata.model.Player;

/**
 * Strategy interface for actions triggered when a player lands on a tile.
 */
public interface TileAction {
  /**
   * Perform this action on the given player.
   * @param player the player on which to perform the action
   */
  void perform(Player player);
}

