package edu.ntnu.idi.bidata.model;

import edu.ntnu.idi.bidata.model.actions.LadderAction;
import edu.ntnu.idi.bidata.model.actions.SnakeAction;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class PlayerTest {
  private Player player;
  private Board board;
  private BoardGame game;

  @BeforeEach
  void setUp() {
    game = new BoardGame();
    game.createBoard(); // Ensure board is initialized

    board = game.getBoard(); // Get initialized board

    player = new Player("John Doe", game); // Pass board, constructor handles starting tile
  }

  @Test
  void testPlayerStartsAtInitialTile() {
    assertEquals(0, player.getCurrentTile().getTileId());
  }

  @Test
  void testPlayerMovesCorrectly() {
    player.move(5);
    assertEquals(5, player.getCurrentTile().getTileId());
  }

  @Test
  void testPlayerDoesNotMoveBeyondLastTile() {
    player.move(105);
    assertEquals(99, player.getCurrentTile().getTileId()); // Last tile is 99
  }

  @Test
  void testPlayerEncountersLadder() {
    board.getTile(3).setLandAction(new LadderAction("Ladder to 22!", 19));
    player.move(3);
    assertEquals(22, player.getCurrentTile().getTileId());
  }

  @Test
  void testPlayerEncountersSnake() {
    board.getTile(16).setLandAction(new SnakeAction("Snake down to 6!", 10));
    player.move(16);
    assertEquals(6, player.getCurrentTile().getTileId());
  }
}
