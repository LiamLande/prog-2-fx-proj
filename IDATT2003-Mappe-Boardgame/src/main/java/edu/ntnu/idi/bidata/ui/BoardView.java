package edu.ntnu.idi.bidata.ui;

import edu.ntnu.idi.bidata.model.BoardGame;
import edu.ntnu.idi.bidata.model.Player;
import edu.ntnu.idi.bidata.model.Tile;
import edu.ntnu.idi.bidata.model.actions.TileAction;
import edu.ntnu.idi.bidata.model.actions.snakes.LadderAction;
import edu.ntnu.idi.bidata.model.actions.snakes.SchrodingerBoxAction;
import edu.ntnu.idi.bidata.model.actions.snakes.SnakeAction;
import java.io.InputStream;
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
  private static final String SCHRODINGER_BOX_TILE_IMG = "/images/schrodinger_box_tile.png"; // New Image Path

  private Image lightTileImg, darkTileImg, schrodingerBoxImg;
  // private Image snakeHeadImg, ladderBaseImg; // Optional

  public BoardView(BoardGame game, SnakeLadderPlayerSetupScene.Theme boardTheme) {
    this.game = game;
    this.theme = boardTheme;
    setPrefSize(SIZE, SIZE);

    loadThemeAndSpecialTileImages(); // Modified to load all necessary tile images
    initializeBoardVisuals();
    drawSnakesAndLadders();
    // Player tokens initialized by GameScene via initializePlayerTokenVisuals() or refresh()
  }

  private Image loadImageFromResources(String path) {
    // Centralized image loading helper
    try {
      InputStream is = getClass().getResourceAsStream(path);
      if (is == null && !path.startsWith("/")) { // Try alternative path if first fails
        is = getClass().getResourceAsStream("/" + path);
      }
      Objects.requireNonNull(is, "Cannot load image resource from path: " + path);
      return new Image(is);
    } catch (Exception e) {
      System.err.println("Failed to load image: " + path + ". Error: " + e.getMessage());
      // Return a placeholder or null, or throw an error to make the issue visible
      // For critical images, throwing an error might be better during development.
      // throw new RuntimeException("Failed to load critical image: " + path, e);
      return null; // Or a default placeholder image object
    }
  }


  private void loadThemeAndSpecialTileImages() {
    String lightPath, darkPath;
    if (this.theme == SnakeLadderPlayerSetupScene.Theme.JUNGLE) {
      lightPath = JUNGLE_TILE_LIGHT;
      darkPath = JUNGLE_TILE_DARK;
    } else { // Default to EGYPT
      lightPath = EGYPT_TILE_LIGHT;
      darkPath = EGYPT_TILE_DARK;
    }
    lightTileImg = loadImageFromResources(lightPath);
    darkTileImg  = loadImageFromResources(darkPath);
    schrodingerBoxImg = loadImageFromResources(SCHRODINGER_BOX_TILE_IMG); // Load Schrödinger image

    // Optional: Load images for snake/ladder tiles
    // snakeHeadImg = loadImageFromResources(SNAKE_HEAD_TILE_IMG);
    // ladderBaseImg = loadImageFromResources(LADDER_BASE_TILE_IMG);

    if (lightTileImg == null || darkTileImg == null) {
      System.err.println("Warning: Default theme tile images failed to load. Board may not render correctly.");
    }
    if (schrodingerBoxImg == null) {
      System.err.println("Warning: Schrodinger Box tile image failed to load.");
    }
  }

  private void initializeBoardVisuals() {
    getChildren().clear(); // Clear previous visuals if any (e.g., if re-initializing)
    tilePositions.clear();

    if (game.getBoard() == null || game.getBoard().getTiles().isEmpty()) {
      System.err.println("BoardView: Board model or tiles are not initialized.");
      return;
    }

    int tileCount = game.getBoard().getTiles().size();
    int boardSize = (int) Math.sqrt(tileCount); // Assuming a square board for sqrt logic
    if (boardSize * boardSize != tileCount && tileCount > 0) {
      // Non-square board, sqrt logic for boardSize might be incorrect.
      // Need a more robust way to determine rows/cols for non-square or non-grid layouts.
      // For now, proceed assuming it's roughly square for serpentine layout.
      System.err.println("BoardView: Tile count " + tileCount + " is not a perfect square. Board layout might be approximate.");
      if (tileCount > 0) boardSize = (int)Math.ceil(Math.sqrt(tileCount)); // Adjust for serpentine
    }
    if (boardSize == 0 && tileCount > 0) boardSize = 1; // Handle single tile case or very small boards
    if (tileCount == 0) return; // No tiles to draw

    double cellSize = SIZE / boardSize;

    // Draw tiles first
    for (Tile tile : game.getBoard().getTiles().values()) {
      int id = tile.getId(); // Assuming 0-indexed IDs
      Point2D tileCenterPos = calculateTilePosition(id, boardSize, cellSize); // Gets center of tile
      tilePositions.put(id, tileCenterPos);

      Rectangle bg = createTileBackgroundRectangle(tile, tileCenterPos.getX() - cellSize / 2, tileCenterPos.getY() - cellSize / 2, cellSize);
      Text num = createTileNumber(id, tileCenterPos.getX(), tileCenterPos.getY());
      getChildren().addAll(bg, num);
    }

    // Then draw snakes/ladders so they are on top of tiles
    drawSnakesAndLadders();

    // Player tokens are added/updated by initializePlayerTokenVisuals() or refresh()
    // and should be added last to be on top of everything.
  }

  private Point2D calculateTilePosition(int id, int boardSize, double cellSize) {
    // boardSize here is the dimension (e.g., 10 for a 10x10 board)
    int logicalRow = id / boardSize; // 0-indexed row from bottom for calculation
    int offsetInRow = id % boardSize; // 0-indexed position within that logical row

    int logicalCol;
    if (logicalRow % 2 == 0) { // Even rows (0, 2, 4...) go left-to-right (0, 1, ..., boardSize-1)
      logicalCol = offsetInRow;
    } else { // Odd rows (1, 3, 5...) go right-to-left (boardSize-1, ..., 1, 0)
      logicalCol = boardSize - 1 - offsetInRow;
    }

    // Convert to UI coordinates where (0,0) is top-left
    // UI row increases downwards, UI col increases to the right
    int uiVisualRow = boardSize - 1 - logicalRow;

    double centerX = logicalCol * cellSize + cellSize / 2;
    double centerY = uiVisualRow * cellSize + cellSize / 2;

    return new Point2D(centerX, centerY);
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

  private Rectangle createTileBackgroundRectangle(Tile tile, double x, double y, double size) {
    Rectangle bg = new Rectangle(x, y, size, size);
    Image tileImageToUse = null;
    TileAction action = tile.getAction();

    // 1. Check for specific action tile images
    if (action instanceof SchrodingerBoxAction) {
      tileImageToUse = schrodingerBoxImg;
    }
    // else if (action instanceof SnakeAction) { // Or check if this tile IS a snake head
    //     tileImageToUse = snakeHeadImg;
    // } else if (action instanceof LadderAction) { // Or check if this tile IS a ladder base
    //     tileImageToUse = ladderBaseImg;
    // }

    // 2. If no specific action image, use theme-based alternating pattern
    if (tileImageToUse == null) {
      int id = tile.getId();
      int boardDimension = (int) Math.sqrt(game.getBoard().getTiles().size()); // Recalculate or pass
      if (boardDimension == 0) boardDimension = 1;
      int row = id / boardDimension;
      int colInRow = id % boardDimension;
      int actualCol = (row % 2 == 0) ? colInRow : (boardDimension - 1 - colInRow);
      boolean isLight = (row + actualCol) % 2 == 0;
      tileImageToUse = isLight ? lightTileImg : darkTileImg;
    }

    // 3. Apply image or fallback color
    if (tileImageToUse != null) {
      // Using 0,0,1,1,true for ImagePattern means the image will scale to fill the rectangle
      ImagePattern pattern = new ImagePattern(tileImageToUse, 0, 0, 1, 1, true);
      bg.setFill(pattern);
    } else {
      // Fallback if any image is null (e.g. schrodingerBoxImg was null)
      int id = tile.getId();
      int boardDimension = (int) Math.sqrt(game.getBoard().getTiles().size());
      if (boardDimension == 0) boardDimension = 1;
      int row = id / boardDimension;
      int colInRow = id % boardDimension;
      int actualCol = (row % 2 == 0) ? colInRow : (boardDimension - 1 - colInRow);
      bg.setFill(((row + actualCol) % 2 == 0) ? Color.LIGHTYELLOW : Color.LIGHTGOLDENRODYELLOW); // Fallback colors
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
    playerTokenViews.values().forEach(getChildren()::remove); // Remove old ImageViews if any
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
        Image playerImage = pieceDataOpt.get().getImage(TOKEN_SIZE); // Get image appropriately sized
        if (playerImage != null) {
          tokenView.setImage(playerImage);
        } else {
          System.err.println("BoardView: Image for piece " + player.getPieceIdentifier() + " is null. Player: " + player.getName());
          setFallbackTokenVisual(tokenView, player);
        }
      } else {
        System.err.println("BoardView: PieceUIData not found for identifier: " + player.getPieceIdentifier() + ". Player: " + player.getName());
        setFallbackTokenVisual(tokenView, player);
      }

      DropShadow shadow = new DropShadow(5, Color.color(0,0,0,0.5));
      shadow.setOffsetX(2);
      shadow.setOffsetY(2);
      tokenView.setEffect(shadow);

      playerTokenViews.put(player, tokenView);
      // Add to children, but refresh() will position it.
      // Ensure it's added if not already:
      if (!getChildren().contains(tokenView)) {
        getChildren().add(tokenView);
      }
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
    // Check if tokens need to be initialized (e.g., if view was created before players were fully set up)
    if (game.getPlayers() != null && playerTokenViews.size() != game.getPlayers().size()) {
      initializePlayerTokenVisuals();
    }
    if (playerTokenViews.isEmpty() && game.getPlayers() != null && !game.getPlayers().isEmpty()) {
      // If still empty after trying to initialize, log it.
      System.err.println("BoardView refresh: Player tokens are not initialized. Check player setup and piece identifiers.");
    }

    for (Player player : game.getPlayers()) {
      if (player.getCurrentTile() == null) continue;

      int tileId = player.getCurrentTile().getId();
      Point2D tileCenterPosition = tilePositions.get(tileId); // This is the CENTER of the tile

      if (tileCenterPosition != null && playerTokenViews.containsKey(player)) {
        ImageView tokenView = playerTokenViews.get(player);
        // Position ImageView so its center aligns with the tile's center
        tokenView.setX(tileCenterPosition.getX() - tokenView.getFitWidth() / 2);
        tokenView.setY(tileCenterPosition.getY() - tokenView.getFitHeight() / 2);
        tokenView.toFront(); // Ensure player tokens are drawn on top of tiles and lines
      }
    }
  }
}