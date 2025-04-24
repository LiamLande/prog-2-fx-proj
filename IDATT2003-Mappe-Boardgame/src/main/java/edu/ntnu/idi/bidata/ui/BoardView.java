package edu.ntnu.idi.bidata.ui;

import edu.ntnu.idi.bidata.model.BoardGame;
import edu.ntnu.idi.bidata.model.Player;
import edu.ntnu.idi.bidata.model.Tile;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.Text;

import java.util.HashMap;
import java.util.Map;

/**
 * Renders any BoardGame loaded from JSON as a square grid of tiles
 * in a serpentine pattern, including snakes & ladders and player tokens.
 */
public class BoardView extends Pane {
  private final BoardGame game;
  private final Map<Integer, Double> tileCenterX = new HashMap<>();
  private final Map<Integer, Double> tileCenterY = new HashMap<>();
  private final Map<Player, Circle> tokenMap = new HashMap<>();

  // size in pixels (square)
  private static final double SIZE = 600;

  public BoardView(BoardGame game) {
    this.game = game;
    int tileCount = game.getBoard().getTiles().size();
    int dim = (int) Math.sqrt(tileCount);
    double cell = SIZE / dim;

    setPrefSize(SIZE, SIZE);

    // 1) draw each cell
    for (Tile t : game.getBoard().getTiles().values()) {
      int id = t.getId();
      int row = id / dim;
      int offs = id % dim;
      // serpentine pattern
      int col = (row % 2 == 0 ? offs : (dim - 1 - offs));
      // flip Y so id=0 is bottom-left
      int uiRow = dim - 1 - row;

      double x = col * cell;
      double y = uiRow * cell;
      double cx = x + cell/2, cy = y + cell/2;
      tileCenterX.put(id, cx);
      tileCenterY.put(id, cy);

      // background
      Rectangle bg = new Rectangle(x, y, cell, cell);
      bg.setFill((row+col)%2==0 ? Color.BEIGE : Color.BURLYWOOD);
      bg.setStroke(Color.DARKGRAY);
      bg.setStrokeWidth(1);

      // tile number
      Text num = new Text(String.valueOf(id));
      num.setFont(Font.font(14));
      num.setFill(Color.DARKSLATEGRAY);
      num.setX(x+4);
      num.setY(y+16);

      getChildren().addAll(bg, num);
    }

    // 2) draw snakes & ladders
    game.getBoard().getTiles().values().stream()
        .filter(t -> t.getAction() != null)
        .forEach(t -> {
          int from = t.getId();
          int to = t.getAction() instanceof edu.ntnu.idi.bidata.model.actions.snakes.LadderAction
              ? from + ((edu.ntnu.idi.bidata.model.actions.snakes.LadderAction)t.getAction()).getSteps()
              : from - ((edu.ntnu.idi.bidata.model.actions.snakes.SnakeAction)t.getAction()).getSteps();

          // safe‐guard
          if (!tileCenterX.containsKey(to)) return;

          Line line = new Line(
              tileCenterX.get(from), tileCenterY.get(from),
              tileCenterX.get(to),   tileCenterY.get(to)
          );
          line.setStroke(t.getAction() instanceof edu.ntnu.idi.bidata.model.actions.snakes.LadderAction
              ? Color.FORESTGREEN
              : Color.CRIMSON);
          line.setStrokeWidth(4);
          getChildren().add(line);
        });

    // 3) place initial tokens
    Color[] colors = { Color.CORNFLOWERBLUE, Color.CRIMSON, Color.FORESTGREEN, Color.GOLDENROD };
    int i = 0;
    for (Player p : game.getPlayers()) {
      Circle tok = new Circle(12, colors[i++ % colors.length]);
      tok.setStroke(Color.BLACK);
      tokenMap.put(p, tok);
      getChildren().add(tok);
    }

    // initial refresh
    refresh();
  }

  /**
   * Repositions each token at its player's current tile center.
   */
  public void refresh() {
    for (Map.Entry<Player, Circle> e : tokenMap.entrySet()) {
      int id = e.getKey().getCurrent().getId();
      Circle tok = e.getValue();
      tok.setCenterX(tileCenterX.get(id));
      tok.setCenterY(tileCenterY.get(id));
    }
  }
}
