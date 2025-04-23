package edu.ntnu.idi.bidata.ui;

import edu.ntnu.idi.bidata.model.BoardGame;
import javafx.scene.layout.Pane;

/**
 * Renders the game board and player tokens.
 */
public class BoardView extends Pane {
  public BoardView(BoardGame game) {
    setPrefSize(600, 600);
    // TODO: draw grid, tokens, snakes/ladders
  }

  /**
   * Updates the view to reflect current game state.
   */
  public void refresh() {
    // TODO: reposition tokens according to player.getCurrent()
  }
}
