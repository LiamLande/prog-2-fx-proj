package edu.ntnu.idi.bidata.ui;

import edu.ntnu.idi.bidata.model.BoardGame;
import edu.ntnu.idi.bidata.model.Player;
import edu.ntnu.idi.bidata.model.Tile;
import edu.ntnu.idi.bidata.model.actions.snakes.LadderAction;
import edu.ntnu.idi.bidata.model.actions.snakes.SnakeAction;
import java.util.Objects;
import javafx.geometry.Point2D;
import javafx.scene.Group;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.paint.ImagePattern;
import javafx.scene.shape.Circle;
import javafx.scene.shape.CubicCurveTo;
import javafx.scene.shape.Line;
import javafx.scene.shape.MoveTo;
import javafx.scene.shape.Path;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.StrokeLineCap;
import javafx.scene.shape.StrokeLineJoin;
import javafx.scene.text.Font;
import javafx.scene.text.Text;

import java.util.HashMap;
import java.util.Map;

/**
 * Renders any BoardGame loaded from JSON as a square grid of tiles
 * in a serpentine pattern, including snakes & ladders and player tokens.
 * The refresh() method should be called by an external controller or parent view
 * when player positions or other dynamic elements need updating.
 */
public class BoardView extends Pane {
  private static final double SIZE = 600; // Assuming this was defined elsewhere

  private final BoardGame game;
  private final Map<Integer, Point2D> tilePositions = new HashMap<>();
  private final Map<Player, Circle> playerTokens = new HashMap<>();

  private final Image lightTileImg;
  private final Image darkTileImg;

  public BoardView(BoardGame game) {
    this.game = game;
    setPrefSize(SIZE, SIZE);

    lightTileImg = new Image(Objects.requireNonNull(
            getClass().getClassLoader().getResourceAsStream("images/sl-tile-light.png"),
            "Could not find images/sl-tile-light.png in classloader"));

    darkTileImg  = new Image(Objects.requireNonNull(
            getClass().getClassLoader().getResourceAsStream("images/sl-tile-dark.png"),
            "Could not find images/sl-tile-dark.png in classloader"));

    initializeBoardVisuals();
    drawSnakesAndLadders();
    initializePlayerTokenVisuals();
    refresh(); // Initial draw of dynamic elements
  }

  private void initializeBoardVisuals() {
    int tileCount = game.getBoard().getTiles().size();
    int boardSize = (int) Math.sqrt(tileCount);
    double cellSize = SIZE / boardSize;

    for (Tile tile : game.getBoard().getTiles().values()) {
      int id = tile.getId();
      int row = id / boardSize;
      int offset = id % boardSize;
      int col = (row % 2 == 0 ? offset : (boardSize - 1 - offset));

      Point2D pos = calculateTilePosition(id, boardSize, cellSize);
      tilePositions.put(id, pos);

      boolean isLightTile = (row + col) % 2 == 0;
      Rectangle bg = createTileBackground(pos.getX(), pos.getY(), cellSize, isLightTile);
      Text num = createTileNumber(id, pos.getX(), pos.getY());

      getChildren().addAll(bg, num);
    }
  }


  private Point2D calculateTilePosition(int id, int boardSize, double cellSize) {
    int row = id / boardSize;
    int offset = id % boardSize;
    int col = (row % 2 == 0) ? offset : (boardSize - 1 - offset);
    int uiRow = boardSize - 1 - row;

    double x = col * cellSize;
    double y = uiRow * cellSize;
    double centerX = x + cellSize / 2;
    double centerY = y + cellSize / 2;

    return new Point2D(centerX, centerY);
  }

  private Rectangle createTileBackground(double x, double y, double size, boolean isLight) {
    Rectangle bg = new Rectangle(x - size/2, y - size/2, size, size);
    Image img = isLight ? lightTileImg : darkTileImg;
    ImagePattern pat = new ImagePattern(img, 0, 0, size, size, false);
    bg.setFill(pat);
    bg.setStroke(Color.DARKGRAY);
    bg.setStrokeWidth(1);
    return bg;
  }

  private Text createTileNumber(int id, double x, double y) {
    Text num = new Text(String.valueOf(id));
    num.setFont(Font.font(14));
    num.setFill(Color.DARKSLATEGRAY);
    num.setX(x - 12);
    num.setY(y + 5);
    return num;
  }

  private void drawSnakesAndLadders() {
    game.getBoard().getTiles().values().stream()
            .filter(tile -> tile.getAction() != null)
            .forEach(tile -> {
              int fromTileId = tile.getId();
              if (tile.getAction() instanceof LadderAction action) {
                int toTileId = fromTileId + action.getSteps();
                drawLadder(fromTileId, toTileId);
              } else if (tile.getAction() instanceof SnakeAction action) {
                int toTileId = fromTileId - action.getSteps();
                drawSnake(fromTileId, toTileId);
              }
            });
  }

  private void drawLadder(int fromTileId, int toTileId) {
    if (!tilePositions.containsKey(fromTileId) || !tilePositions.containsKey(toTileId)) {
      return;
    }
    Point2D start = tilePositions.get(fromTileId);
    Point2D end = tilePositions.get(toTileId);
    Group ladder = createLadderVisual(start, end);
    getChildren().add(ladder);
  }

  private Group createLadderVisual(Point2D start, Point2D end) {
    Group ladder = new Group();
    double x1 = start.getX(), y1 = start.getY();
    double x2 = end.getX(), y2 = end.getY();
    double dx = x2 - x1, dy = y2 - y1;
    double length = Math.hypot(dx, dy);
    double railOffset = 10;
    double ux = -dy/length * railOffset;
    double uy = dx/length * railOffset;

    Line leftRail = new Line(x1 - ux, y1 - uy, x2 - ux, y2 - uy);
    Line rightRail = new Line(x1 + ux, y1 + uy, x2 + ux, y2 + uy);
    leftRail.setStroke(Color.GOLD);
    rightRail.setStroke(Color.GOLD);
    leftRail.setStrokeWidth(5);
    rightRail.setStrokeWidth(5);
    ladder.getChildren().addAll(leftRail, rightRail);

    int rungCount = (int)(length / 40) + 3;
    for (int i = 0; i <= rungCount; i++) {
      double fraction = i / (double)rungCount;
      double rx = x1 + dx * fraction;
      double ry = y1 + dy * fraction;
      Line rung = new Line(rx - ux, ry - uy, rx + ux, ry + uy);
      rung.setStroke(Color.GOLDENROD);
      rung.setStrokeWidth(4);
      ladder.getChildren().add(rung);
    }
    return ladder;
  }

  private void drawSnake(int fromTileId, int toTileId) {
    if (!tilePositions.containsKey(fromTileId) || !tilePositions.containsKey(toTileId)) {
      return;
    }
    Point2D start = tilePositions.get(fromTileId);
    Point2D end = tilePositions.get(toTileId);
    Group snake = createSnakeVisual(start, end);
    getChildren().add(snake);
  }

  private Group createSnakeVisual(Point2D start, Point2D end) {
    Group snakeGroup = new Group();
    double x1 = start.getX(), y1 = start.getY();
    double x2 = end.getX(), y2 = end.getY();
    double midX = (x1 + x2) / 2;
    double controlDistance = Math.hypot(x2 - x1, y2 - y1) * 0.3;

    Path snakePath = new Path();
    snakePath.setStrokeWidth(8);
    snakePath.setStroke(Color.SADDLEBROWN);
    snakePath.setStrokeLineCap(StrokeLineCap.ROUND);
    snakePath.setStrokeLineJoin(StrokeLineJoin.ROUND);
    snakePath.setFill(null);
    snakePath.getElements().add(new MoveTo(x1, y1));
    double quarter = (y2 - y1) / 4;
    snakePath.getElements().add(new CubicCurveTo(midX + controlDistance, y1, midX - controlDistance, y1 + quarter, midX, y1 + quarter * 2));
    snakePath.getElements().add(new CubicCurveTo(midX + controlDistance, y1 + quarter * 3, midX - controlDistance, y2, x2, y2));

    Circle head = new Circle(x1, y1, 12, Color.GOLDENROD);
    head.setStroke(Color.SADDLEBROWN);
    head.setStrokeWidth(3);
    double eyeOffset = 4;
    Circle leftEye = new Circle(x1 - eyeOffset, y1 - eyeOffset, 3, Color.WHITE);
    Circle rightEye = new Circle(x1 + eyeOffset, y1 - eyeOffset, 3, Color.WHITE);
    Circle leftPupil = new Circle(x1 - eyeOffset, y1 - eyeOffset, 1.5, Color.BLACK);
    Circle rightPupil = new Circle(x1 + eyeOffset, y1 - eyeOffset, 1.5, Color.BLACK);
    Circle tail = new Circle(x2, y2, 4, Color.SADDLEBROWN);
    snakeGroup.getChildren().addAll(snakePath, tail, head, leftEye, rightEye, leftPupil, rightPupil);
    return snakeGroup;
  }


  private void initializePlayerTokenVisuals() {
    Color[] colors = { Color.RED, Color.BLUE, Color.GREEN, Color.PURPLE };
    int playerIndex = 0;

    for (Player player : game.getPlayers()) {
      Circle token = new Circle(12, colors[playerIndex++ % colors.length]);
      token.setStroke(Color.BLACK);
      token.setStrokeWidth(2);
      DropShadow shadow = new DropShadow();
      shadow.setRadius(5);
      shadow.setOffsetX(2);
      shadow.setOffsetY(2);
      shadow.setColor(Color.color(0, 0, 0, 0.5));
      token.setEffect(shadow);
      playerTokens.put(player, token);
      getChildren().add(token);
    }
  }

  /**
   * Refreshes the positions of dynamic elements on the board, primarily player tokens.
   * This method should be called when the game state changes (e.g., after a player moves).
   */
  public void refresh() {
    for (Player player : game.getPlayers()) {
      int tileId = player.getCurrentTile().getId();
      Point2D position = tilePositions.get(tileId);

      if (position != null && playerTokens.containsKey(player)) {
        Circle token = playerTokens.get(player);
        token.setCenterX(position.getX());
        token.setCenterY(position.getY());
      }
    }
  }
}