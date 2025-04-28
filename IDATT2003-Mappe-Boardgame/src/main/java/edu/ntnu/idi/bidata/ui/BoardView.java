package edu.ntnu.idi.bidata.ui;

import edu.ntnu.idi.bidata.model.BoardGame;
import edu.ntnu.idi.bidata.model.Player;
import edu.ntnu.idi.bidata.model.Tile;
import edu.ntnu.idi.bidata.model.actions.snakes.LadderAction;
import javafx.scene.Group;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.CubicCurve;
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

          double x1 = tileCenterX.get(from), y1 = tileCenterY.get(from);
          double x2 = tileCenterX.get(to),   y2 = tileCenterY.get(to);
          boolean isLadder = t.getAction() instanceof LadderAction;

          if (isLadder) {
            // compute unit‐perp vector for offsetting the rails
            double dx = x2 - x1, dy = y2 - y1;
            double len = Math.hypot(dx, dy);
            double ux = -dy/len * 8, uy = dx/len * 8;  // 8px offset

            // left and right rails
            Line rail1 = new Line(x1, y1, x2, y2);
            Line rail2 = new Line(x1+ux, y1+uy, x2+ux, y2+uy);
            rail1.setStroke(Color.FORESTGREEN);
            rail2.setStroke(Color.FORESTGREEN);
            rail1.setStrokeWidth(4); rail2.setStrokeWidth(4);

            // rungs
            Group ladder = new Group(rail1, rail2);
            int rungCount = 5;
            for (int i = 1; i < rungCount; i++) {
              double tFrac = i/(double)rungCount;
              double rx1 = x1 + dx*tFrac + ux;
              double ry1 = y1 + dy*tFrac + uy;
              double rx2 = x1 + dx*tFrac - ux;
              double ry2 = y1 + dy*tFrac - uy;
              Line rung = new Line(rx1, ry1, rx2, ry2);
              rung.setStroke(Color.FORESTGREEN);
              rung.setStrokeWidth(3);
              ladder.getChildren().add(rung);
            }
            getChildren().add(ladder);

          } else {
            // draw a wiggly snake with a cubic Bézier curve
            double midX = (x1 + x2)/2, midY = (y1 + y2)/2;
            CubicCurve snake = new CubicCurve(
                x1, y1,
                midX, midY + 40,     // control point 1
                midX, midY - 40,     // control point 2
                x2, y2
            );
            snake.setStroke(Color.CRIMSON);
            snake.setStrokeWidth(4);
            snake.setFill(Color.TRANSPARENT);

            // optionally add a little “head” circle at the start
            Circle head = new Circle(x1, y1, 6, Color.CRIMSON);
            head.setStroke(Color.DARKRED);
            head.setStrokeWidth(2);

            getChildren().addAll(snake, head);
          }
        });

    // 3) place initial tokens
    Color[] colors = { Color.RED, Color.BLUE, Color.GREEN, Color.PURPLE };
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
