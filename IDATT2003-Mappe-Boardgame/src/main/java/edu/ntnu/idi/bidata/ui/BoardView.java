package edu.ntnu.idi.bidata.ui;

import edu.ntnu.idi.bidata.model.BoardGame;
import edu.ntnu.idi.bidata.model.Player;
import edu.ntnu.idi.bidata.model.Tile;
import edu.ntnu.idi.bidata.model.actions.snakes.LadderAction;
import edu.ntnu.idi.bidata.model.actions.snakes.SnakeAction;
import javafx.geometry.Point2D;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView; // Added
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.paint.ImagePattern;
import javafx.scene.shape.Circle; // Can be kept for fallback or removed
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
import java.util.Objects;
import java.util.Optional;

public class BoardView extends Pane {
  private static final double SIZE = 600; // Board size
  private static final double TOKEN_SIZE = 30; // Desired player token image size on board

  private final BoardGame game;
  private final Map<Integer, Point2D> tilePositions = new HashMap<>();
  private final Map<Player, ImageView> playerTokenViews = new HashMap<>(); // Changed from Circle
  private final SnakeLadderPlayerSetupScene.Theme theme;

  private static final String EGYPT_TILE_LIGHT = "/images/sl-tile-light.png";
  private static final String EGYPT_TILE_DARK = "/images/sl-tile-dark.png";
  private static final String JUNGLE_TILE_LIGHT = "/images/jungle-tile-light.png";
  private static final String JUNGLE_TILE_DARK = "/images/jungle-tile-dark.png";

  private Image lightTileImg, darkTileImg;

  public BoardView(BoardGame game, SnakeLadderPlayerSetupScene.Theme boardTheme) {
    this.game = game;
    this.theme = boardTheme;
    setPrefSize(SIZE, SIZE);
    loadThemeSpecificTileImages();
    initializeBoardVisuals(); // Tiles and numbers
    drawSnakesAndLadders();   // Snakes and ladders
    // Player tokens will be initialized/refreshed by initializePlayerTokenVisuals() or refresh()
    // which should be called after players are added to the game model.
  }

  private void loadThemeSpecificTileImages() { /* ... Same as before ... */
    String lightPath, darkPath;
    if (this.theme == SnakeLadderPlayerSetupScene.Theme.JUNGLE) {
      lightPath = JUNGLE_TILE_LIGHT;
      darkPath = JUNGLE_TILE_DARK;
    } else { // Default to EGYPT
      lightPath = EGYPT_TILE_LIGHT;
      darkPath = EGYPT_TILE_DARK;
    }
    try {
      lightTileImg = new Image(Objects.requireNonNull(getClass().getResourceAsStream(lightPath),"Could not find image: " + lightPath));
      darkTileImg  = new Image(Objects.requireNonNull(getClass().getResourceAsStream(darkPath),"Could not find image: " + darkPath));
    } catch (Exception e) {
      System.err.println("Error loading tile images for theme: " + theme);
      // Consider fallback solid colors if images fail
      lightTileImg = null; // Or a default placeholder image
      darkTileImg = null;  // Or a default placeholder image
    }
  }
  private void initializeBoardVisuals() { /* ... Same as before ... */
    int tileCount = game.getBoard().getTiles().size();
    int boardSize = (int) Math.sqrt(tileCount);
    double cellSize = SIZE / boardSize;

    for (Tile tile : game.getBoard().getTiles().values()) {
      int id = tile.getId();
      Point2D pos = calculateTilePosition(id, boardSize, cellSize);
      tilePositions.put(id, pos);

      int row = id / boardSize; // Mathematical row
      int offset = id % boardSize;
      int col = (row % 2 == 0 ? offset : (boardSize - 1 - offset)); // Mathematical col
      boolean isLightTile = (row + col) % 2 == 0; // Use mathematical row/col for pattern

      Rectangle bg = createTileBackground(pos.getX() - cellSize / 2, pos.getY() - cellSize / 2, cellSize, isLightTile); // Use calculated X,Y for top-left
      Text num = createTileNumber(id, pos.getX(), pos.getY()); // Use center X,Y for number positioning
      getChildren().addAll(bg, num);
    }
  }
  private Point2D calculateTilePosition(int id, int boardSize, double cellSize) { /* ... Same as before ... */
    int row = id / boardSize; // 0-indexed logical row from bottom
    int offset = id % boardSize; // 0-indexed offset in the logical row
    int col; // 0-indexed logical column from left
    if (row % 2 == 0) { // Even rows (0, 2, ...) go left to right
      col = offset;
    } else { // Odd rows (1, 3, ...) go right to left
      col = boardSize - 1 - offset;
    }
    // Convert to UI coordinates (0,0 at top-left)
    int uiRow = boardSize - 1 - row;
    double x = col * cellSize + cellSize / 2; // Center of the tile
    double y = uiRow * cellSize + cellSize / 2; // Center of the tile
    return new Point2D(x, y);
  }
  private Rectangle createTileBackground(double x, double y, double size, boolean isLight) { /* ... Same ... */
    Rectangle bg = new Rectangle(x, y, size, size); // x, y is top-left
    Image imgToUse = isLight ? lightTileImg : darkTileImg;
    if (imgToUse != null) {
      ImagePattern pat = new ImagePattern(imgToUse, 0, 0, 1, 1, true); // Use relative coords for pattern
      bg.setFill(pat);
    } else {
      bg.setFill(isLight ? Color.BEIGE : Color.BURLYWOOD); // Fallback colors
    }
    bg.setStroke(Color.DARKGRAY);
    bg.setStrokeWidth(1);
    return bg;
  }
  private Text createTileNumber(int id, double centerX, double centerY) { /* ... Same ... */
    Text num = new Text(String.valueOf(id + 1)); // Display 1-indexed numbers
    num.setFont(Font.font(14));
    num.setFill(Color.DARKSLATEGRAY);
    // Adjust to center text within the tile based on its bounds
    num.setX(centerX - num.getLayoutBounds().getWidth() / 2);
    num.setY(centerY + num.getLayoutBounds().getHeight() / 4); // Approximation for baseline
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

  /**
   * Initializes or re-initializes player token visuals (ImageViews).
   * Should be called after players are known and game starts.
   */
  public void initializePlayerTokenVisuals() {
    // Remove existing token ImageViews from the pane
    playerTokenViews.values().forEach(getChildren()::remove);
    playerTokenViews.clear();

    if (game.getPlayers() == null) return;

    for (Player player : game.getPlayers()) {
      ImageView tokenView = new ImageView();
      tokenView.setFitWidth(TOKEN_SIZE);
      tokenView.setFitHeight(TOKEN_SIZE);
      tokenView.setPreserveRatio(true);

      Optional<PieceUIData> pieceDataOpt = SnakeLadderPlayerSetupScene.AVAILABLE_PIECES.stream()
          .filter(pd -> pd.getIdentifier().equals(player.getPieceIdentifier()))
          .findFirst();

      if (pieceDataOpt.isPresent()) {
        Image playerImage = pieceDataOpt.get().getImage(TOKEN_SIZE); // Get image of correct size
        if (playerImage != null) {
          tokenView.setImage(playerImage);
        } else {
          System.err.println("BoardView: Image for piece " + player.getPieceIdentifier() + " is null. Player: " + player.getName());
          setFallbackTokenVisual(tokenView, player); // Use a fallback
        }
      } else {
        System.err.println("BoardView: PieceUIData not found for identifier: " + player.getPieceIdentifier() + ". Player: " + player.getName());
        setFallbackTokenVisual(tokenView, player); // Use a fallback
      }

      DropShadow shadow = new DropShadow(5, Color.color(0,0,0,0.5));
      shadow.setOffsetX(2);
      shadow.setOffsetY(2);
      tokenView.setEffect(shadow);

      playerTokenViews.put(player, tokenView);
      getChildren().add(tokenView); // Add to pane's children
    }
    refresh(); // Position them correctly
  }

  private void setFallbackTokenVisual(ImageView tokenView, Player player) {
    // This method could create a colored circle/rectangle and convert it to an Image
    // or use a pre-defined placeholder image. For simplicity, we'll not draw anything
    // or you can load a 'unknown_piece.png'.
    // For now, the ImageView will be empty if image is null.
    System.err.println("Fallback for player " + player.getName() + " with piece " + player.getPieceIdentifier());
  }


  public void refresh() {
    if (game.getPlayers() == null || playerTokenViews.isEmpty() && !game.getPlayers().isEmpty()) {
      // If tokens are not initialized but players exist, initialize them.
      // This handles cases where BoardView is created before players are fully in the model.
      initializePlayerTokenVisuals();
      if (playerTokenViews.isEmpty() && !game.getPlayers().isEmpty()) {
        System.err.println("BoardView refresh: Tokens still not initialized after attempt. Players might not have piece identifiers or images.");
        return; // Avoid NPE if initialization still fails
      }
    }


    for (Player player : game.getPlayers()) {
      if (player.getCurrentTile() == null) continue;

      int tileId = player.getCurrentTile().getId(); // 0-indexed
      Point2D position = tilePositions.get(tileId); // Center of the tile

      if (position != null && playerTokenViews.containsKey(player)) {
        ImageView tokenView = playerTokenViews.get(player);
        // Position ImageView so its center aligns with the tile's center (position)
        tokenView.setX(position.getX() - tokenView.getFitWidth() / 2);
        tokenView.setY(position.getY() - tokenView.getFitHeight() / 2);
        tokenView.toFront(); // Bring player tokens to the front
      }
    }
  }
}