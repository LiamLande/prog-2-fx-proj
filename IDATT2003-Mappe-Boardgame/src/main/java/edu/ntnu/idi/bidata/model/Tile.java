package edu.ntnu.idi.bidata.model;


import edu.ntnu.idi.bidata.exception.InvalidParameterException;
import edu.ntnu.idi.bidata.model.actions.TileAction;

/**
 * A board tile linked to next/previous and optional action on landing.
 */
public class Tile {
  private final int id;
  private Tile next;
  private Tile previous;
  private TileAction action;

  public Tile(int id) {
    if (id < 0) {
      throw new InvalidParameterException("Tile id must be non‐negative");
    }
    this.id = id;
  }

  public int getId() {
    return id;
  }

  public Tile getNext() {
    return next;
  }

  public void setNext(Tile next) {
    this.next = next;
  }

  public Tile getPrevious() {
    return previous;
  }

  public void setPrevious(Tile previous) {
    this.previous = previous;
  }

  public void setAction(TileAction action) {
    this.action = action;
  }

  /**
   * Called when a player lands here: triggers the action if present.
   */
  public void land(Player player) {
    if (action != null) {
      action.perform(player);
    }
  }
}