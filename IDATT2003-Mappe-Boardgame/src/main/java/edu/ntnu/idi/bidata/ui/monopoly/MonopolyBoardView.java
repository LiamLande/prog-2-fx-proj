package edu.ntnu.idi.bidata.ui.monopoly;

import edu.ntnu.idi.bidata.model.BoardGame;
import edu.ntnu.idi.bidata.model.Player;
import edu.ntnu.idi.bidata.model.Tile;
import edu.ntnu.idi.bidata.model.actions.monopoly.ChanceAction;
import edu.ntnu.idi.bidata.model.actions.monopoly.CommunityChestAction;
import edu.ntnu.idi.bidata.model.actions.monopoly.PropertyAction;
import edu.ntnu.idi.bidata.model.actions.monopoly.TaxAction;
import javafx.geometry.Point2D;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.control.Label;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;

import java.util.HashMap;
import java.util.Map;

/**
 * Inner class for rendering the Monopoly board.
 */
public class MonopolyBoardView extends Pane {
    public static final double SIZE = 1000;
    private static final double TOKEN_RADIUS = 8;

    private final BoardGame game;
    private final Map<Integer, Point2D> tileCenterPositions = new HashMap<>();
    private final Map<Player, Circle> playerTokensOnBoard = new HashMap<>();
    private final Map<Player, Circle> playerTokenUIsFromSidePanel;
    private int tilesOnEdgeInDefinition; // This is the 'N' in "corners are 0, N, 2N, 3N"

    private final Map<Integer, Rectangle> tileRects = new HashMap<>();
    private final Map<Integer, Group> tileTextGroups = new HashMap<>(); // To manage text updates/removal

    private final Color[] propertyColors = {
            Color.rgb(165, 42, 42),    // Brown
            Color.LIGHTSKYBLUE,        // Light Blue
            Color.rgb(255,105,180),    // Pink (HotPink)
            Color.ORANGE,              // Orange
            Color.RED,                 // Red
            Color.YELLOW,              // Yellow
            Color.LIMEGREEN,           // Green
            Color.ROYALBLUE            // Dark Blue
    };
    private int cellsPerSideGrid;
    private double cellSize;

    public MonopolyBoardView(BoardGame game, Map<Player, Circle> playerTokenUIsFromSidePanel) {
        this.game = game;
        this.playerTokenUIsFromSidePanel = playerTokenUIsFromSidePanel;
        setPrefSize(SIZE, SIZE);
        setMinSize(SIZE, SIZE);
        setMaxSize(SIZE, SIZE);
        setStyle("-fx-background-color: #C9E2C9;"); // Light green board background, e.g. Monopoly classic green

        initializeBoardLayout();
        initializeBoardVisuals();
        initializePlayerTokenVisuals(); // Create player tokens on board
        refresh();
    }

    private void initializeBoardLayout() {
        int tileCount = (game.getBoard() != null && game.getBoard().getTiles() != null) ? game.getBoard().getTiles().size() : 0;
        if (tileCount == 0) {
            this.cellsPerSideGrid = 1; // Default to a single cell if no tiles
            this.cellSize = SIZE;
            System.err.println("MonopolyBoardView: No tiles found in board model. Displaying single cell.");
            return;
        }

        if (tileCount > 0 && tileCount % 4 == 0) {
            this.cellsPerSideGrid = (tileCount / 4) + 1;
        } else {
            this.cellsPerSideGrid = (int) Math.ceil(Math.sqrt(tileCount));
            if (this.cellsPerSideGrid <= 0) this.cellsPerSideGrid = 1; // Ensure at least 1
            System.err.println("MonopolyBoardView: Non-standard tile count " + tileCount +
                    ". Using grid of " + this.cellsPerSideGrid + "x" + this.cellsPerSideGrid);
        }
        this.cellSize = SIZE / this.cellsPerSideGrid;
    }

    private void initializeBoardVisuals() {
        getChildren().clear();
        tileRects.clear();
        tileCenterPositions.clear();
        tileTextGroups.clear();

        int tileCount = (game.getBoard() != null && game.getBoard().getTiles() != null) ? game.getBoard().getTiles().size() : 0;
        if (tileCount == 0) return;

        double innerAreaSize = Math.max(0, (cellsPerSideGrid - 2) * cellSize);
        Rectangle centerRect = new Rectangle(cellSize, cellSize, innerAreaSize, innerAreaSize);
        centerRect.setFill(Color.rgb(210, 228, 210)); // Slightly different green for center
        centerRect.setStroke(Color.DARKSLATEGRAY);
        centerRect.setStrokeWidth(1);
        getChildren().add(centerRect);

        Label centerLabel = new Label("Mini Monopoly");
        centerLabel.setFont(Font.font("Kabel", FontWeight.BOLD, Math.max(10, innerAreaSize * 0.15)));
        centerLabel.setTextFill(Color.DARKRED);
        centerLabel.setPrefSize(innerAreaSize, innerAreaSize);
        centerLabel.setAlignment(Pos.CENTER);
        centerLabel.setLayoutX(cellSize);
        centerLabel.setLayoutY(cellSize);
        getChildren().add(centerLabel);

        for (int tileId = 0; tileId < tileCount; tileId++) {
            Tile tile = game.getBoard().getTile(tileId);
            if (tile == null) continue;

            Point2D centerPos = calculateTileCenterPosition(tileId);
            tileCenterPositions.put(tileId, centerPos);

            Rectangle tileRect = createTileRectangle(centerPos, tile, tileId);
            tileRect.setUserData(tile);
            getChildren().add(tileRect);
            tileRects.put(tileId, tileRect);

            Group textGroup = createTileTextGroup(tile, centerPos, tileId);
            getChildren().add(textGroup);
            tileTextGroups.put(tileId, textGroup);
        }
    }

    private Point2D calculateTileCenterPosition(int tileId) {
        double x, y;
        int tilesOnEdge = cellsPerSideGrid - 1;

        if (tilesOnEdge <= 0) return new Point2D(SIZE / 2.0, SIZE / 2.0);

        int side = tileId / tilesOnEdge;
        int posOnSide = tileId % tilesOnEdge;

        switch (side) {
            case 0: // Bottom row
                x = SIZE - (cellSize / 2.0) - (posOnSide * cellSize);
                y = SIZE - (cellSize / 2.0);
                break;
            case 1: // Left column
                x = cellSize / 2.0;
                y = SIZE - (cellSize / 2.0) - (posOnSide * cellSize);
                break;
            case 2: // Top row
                x = (cellSize / 2.0) + (posOnSide * cellSize);
                y = cellSize / 2.0;
                break;
            case 3: // Right column
                x = SIZE - (cellSize / 2.0);
                y = (cellSize / 2.0) + (posOnSide * cellSize);
                break;
            default:
                System.err.println("MonopolyBoardView: Tile " + tileId + " on unexpected side " + side + ". Placing at default.");
                x = cellSize / 2.0; y = cellSize / 2.0;
        }
        return new Point2D(x, y);
    }

    private Rectangle createTileRectangle(Point2D centerPos, Tile tile, int tileId) {
        Rectangle rect = new Rectangle(centerPos.getX() - cellSize / 2.0, centerPos.getY() - cellSize / 2.0, cellSize, cellSize);

        int tilesOnEdge = cellsPerSideGrid - 1;
        boolean isCorner = tilesOnEdge > 0 && (tileId % tilesOnEdge == 0);

        rect.setStroke(Color.DARKSLATEGRAY);
        rect.setStrokeWidth(1);

        if (isCorner) {
            rect.setFill(Color.LIGHTGRAY.deriveColor(0,1,1,0.8)); // Semi-transparent light gray
            if (tileId == 0) rect.setFill(Color.LIGHTGREEN.deriveColor(0,1,1,0.8));
        } else if (tile.getAction() instanceof PropertyAction pa) {
            rect.setFill(Color.IVORY.deriveColor(0,1,1,0.7)); // Base for property, mostly covered by color bar + text
            // Add color bar separately, rect acts as border/background
            Rectangle colorBar = createColorBar(pa, centerPos, tileId);
            getChildren().add(colorBar); // Add before text group potentially
            // Ownership stroke handled in refresh
        } else { // Chance, Community Chest, Tax
            rect.setFill(Color.WHITESMOKE.deriveColor(0,1,1,0.7));
        }
        return rect;
    }

    private Rectangle createColorBar(PropertyAction pa, Point2D tileCenterPos, int tileId) {
        int colorGroupIndex = getColorGroupIndex(pa.getColorGroup());
        Color propertyColor = propertyColors[colorGroupIndex % propertyColors.length];

        double barThicknessRatio = 0.22; // Bar takes ~22% of cell's dimension perpendicular to bar
        double barX = 0, barY = 0, barWidth = 0, barHeight = 0;

        int tilesOnEdge = cellsPerSideGrid - 1;
        int side = tilesOnEdge > 0 ? tileId / tilesOnEdge : 0;

        switch (side) {
            case 0: // Bottom row, bar at top of cell
                barX = tileCenterPos.getX() - cellSize / 2.0;
                barY = tileCenterPos.getY() - cellSize / 2.0;
                barWidth = cellSize; barHeight = cellSize * barThicknessRatio;
                break;
            case 1: // Left row, bar at right of cell
                barX = tileCenterPos.getX() + cellSize / 2.0 - (cellSize * barThicknessRatio);
                barY = tileCenterPos.getY() - cellSize / 2.0;
                barWidth = cellSize * barThicknessRatio; barHeight = cellSize;
                break;
            case 2: // Top row, bar at bottom of cell
                barX = tileCenterPos.getX() - cellSize / 2.0;
                barY = tileCenterPos.getY() + cellSize / 2.0 - (cellSize * barThicknessRatio);
                barWidth = cellSize; barHeight = cellSize * barThicknessRatio;
                break;
            case 3: // Right row, bar at left of cell
                barX = tileCenterPos.getX() - cellSize / 2.0;
                barY = tileCenterPos.getY() - cellSize / 2.0;
                barWidth = cellSize * barThicknessRatio; barHeight = cellSize;
                break;
        }

        Rectangle colorBarRect = new Rectangle(barX, barY, barWidth, barHeight);
        colorBarRect.setFill(propertyColor);
        colorBarRect.setStroke(Color.BLACK);
        colorBarRect.setStrokeWidth(0.5);
        return colorBarRect;
    }

    public Group createTileTextGroup(Tile tile, Point2D centerPos, int tileId) {
        String nameStr = "Tile " + tileId; // Default name if not a property or special corner
        String priceStr = "";


        this.tilesOnEdgeInDefinition = cellsPerSideGrid - 1;
        // tilesOnEdgeInDefinition is N, where corners are 0, N, 2N, 3N. Total tiles = 4*N.
        int tilesOnEdge = this.tilesOnEdgeInDefinition;

        boolean isCorner = (tilesOnEdge > 0 && tileId % tilesOnEdge == 0);

        if (isCorner) {
            if (tileId == 0) nameStr = "GO";
            else if (tileId == tilesOnEdge) nameStr = "JAIL";
            else if (tileId == 2 * tilesOnEdge) nameStr = "FREE PARKING";
            else if (tileId == 3 * tilesOnEdge) nameStr = "GO TO JAIL";
            // Note: The provided image shows corners like "JAIL" also having "TILE 7" underneath.
            // This method creates a single text block (name + optional price).
            // To match the image for complex corners, you'd need a more advanced approach,
            // potentially returning multiple groups or custom nodes for those specific tiles.
        } else if (tile.getAction() instanceof PropertyAction pa) {
            nameStr = pa.getName();
            priceStr = "$" + pa.getCost();
        } else if (tile.getAction() instanceof ChanceAction ca) {
            nameStr = "Chance";
        } else if (tile.getAction() instanceof CommunityChestAction cca) {
            nameStr = "Community Chest";
        } else if (tile.getAction() instanceof TaxAction ta) {
            nameStr = ta.getDescription();
            priceStr = "$" + ta.getTaxAmount();
        }

        Text nameTextNode = new Text(nameStr);
        nameTextNode.setFont(Font.font("Arial", FontWeight.BOLD, Math.max(6, cellSize * 0.09)));
        nameTextNode.setTextAlignment(TextAlignment.CENTER); // For multi-line wrapped text
        nameTextNode.setWrappingWidth(cellSize * 0.85); // Max width for text before it wraps

        Text priceTextNode = null;
        if (!priceStr.isEmpty()) {
            priceTextNode = new Text(priceStr);
            priceTextNode.setFont(Font.font("Arial", FontWeight.NORMAL, Math.max(5, cellSize * 0.08)));
            priceTextNode.setTextAlignment(TextAlignment.CENTER);
            priceTextNode.setWrappingWidth(cellSize * 0.85);
        }

        Group textGroup = new Group(nameTextNode);
        if (priceTextNode != null) {
            textGroup.getChildren().add(priceTextNode);
        }

        // It's good practice to ensure nodes are part of a scene graph for accurate bounds,
        // but getLayoutBounds() is often sufficient if fonts are standard and set directly.
        double nameHeight = nameTextNode.getLayoutBounds().getHeight();
        double priceHeight = (priceTextNode != null) ? priceTextNode.getLayoutBounds().getHeight() : 0;
        double verticalSpacing = (priceTextNode != null && !nameStr.isEmpty()) ? cellSize * 0.03 : 0;
        double totalTextHeight = nameHeight + verticalSpacing + priceHeight;

        // Determine vertical position for the text block.
        // contentAreaVerticalOffsetFromCellCenter is the Y-coordinate of the center of the drawable text area,
        // relative to the cell's center (which will be the group's origin).
        double contentAreaVerticalOffsetFromCellCenter = 0;

        if (!isCorner && tile.getAction() instanceof PropertyAction) {
            // For properties, a color bar takes up the top 22% of the cell.
            double colorBarHeightRatio = 0.22;
            // The text should be centered in the area *below* the color bar.
            // Cell top: -cellSize/2. Color bar ends at: -cellSize/2 + cellSize*colorBarHeightRatio.
            // Text area is from (-cellSize/2 + cellSize*colorBarHeightRatio) to (+cellSize/2).
            // Midpoint of this text area relative to cell center (Y=0):
            contentAreaVerticalOffsetFromCellCenter = (cellSize * colorBarHeightRatio) / 2.0;
        } else {
            // For corners or non-property tiles, text is centered in the whole cell.
            // contentAreaVerticalOffsetFromCellCenter remains 0 (center of cell).
        }

        // Position text nodes relative to the group's (0,0) point.
        // The text block (name + price) should be centered around contentAreaVerticalOffsetFromCellCenter.
        double nameY = contentAreaVerticalOffsetFromCellCenter - (totalTextHeight / 2.0);
        nameTextNode.setY(nameY);

        if (priceTextNode != null) {
            priceTextNode.setY(nameY + nameHeight + verticalSpacing);
        }

        // Horizontally center each text node relative to the group's (0,0) point.
        nameTextNode.setX(-nameTextNode.getLayoutBounds().getWidth() / 2.0);
        if (priceTextNode != null) {
            priceTextNode.setX(-priceTextNode.getLayoutBounds().getWidth() / 2.0);
        }

        // Position the entire textGroup: its (0,0) origin will be at the cell's center.
        textGroup.setLayoutX(centerPos.getX());
        textGroup.setLayoutY(centerPos.getY());

        // Determine rotation angle for the textGroup.
        double rotationAngle = 0;
        int side = (tilesOnEdge > 0 && tileId < 4 * tilesOnEdge) ? tileId / tilesOnEdge : 0; // Determine side (0:bottom, 1:left, 2:top, 3:right)

        // Right row
        rotationAngle = switch (side) {
            case 0 -> 0;   // Bottom row
            case 1 -> -90; // Left row
            case 2 -> 0; // Top row
            case 3 -> 90;
            default -> rotationAngle;
        };

        // Apply overrides for specific corners if their text orientation differs from the side's default.
        if (isCorner) {
            if (tileId == 0) { // GO (typically on Side 0)
                rotationAngle = 0; // Standard GO text is horizontal.
            } else if (tileId == tilesOnEdge) { // JAIL (typically on Side 1)
                // Side 1 default is -90. Image shows "JAIL" rotated -90, which is fine.
                // No override needed if -90 is desired for this corner's name.
            } else if (tileId == 2 * tilesOnEdge) { // FREE PARKING (typically on Side 2)
                // Side 2 default is 180. Image shows "FREE PARKING" text horizontal (0 degrees).
                rotationAngle = 0;
            } else if (tileId == 3 * tilesOnEdge) { // GO TO JAIL (typically on Side 3)
                // Side 3 default is 90. Image shows "GO TO JAIL" text horizontal (0 degrees).
                rotationAngle = 0;
            }
        }
        textGroup.setRotate(rotationAngle);

        return textGroup;
    }

    private int getColorGroupIndex(String colorGroup) {
        if (colorGroup == null) return 0;
        return switch (colorGroup.toLowerCase()) { // Use toLowerCase for robustness
            case "brown" -> 0;
            case "light blue" -> 1;
            case "pink", "magenta" -> 2; // Added magenta as common alias
            case "orange" -> 3;
            case "red" -> 4;
            case "yellow" -> 5;
            case "green" -> 6;
            case "dark blue", "blue" -> 7; // Added blue
            default -> {
                System.err.println("MonopolyBoardView: Unknown color group '" + colorGroup + "'. Defaulting to Brown.");
                yield 0;
            }
        };
    }

    private Color getPlayerColorFromSidePanel(Player player) {
        Circle tokenUI = playerTokenUIsFromSidePanel.get(player);
        if (tokenUI != null && tokenUI.getFill() instanceof Color) {
            return (Color) tokenUI.getFill();
        }
        int playerIndex = game.getPlayers().indexOf(player);
        Color[] defaultColors = { Color.RED, Color.BLUE, Color.rgb(0,128,0), Color.GOLD, Color.PURPLE, Color.DARKCYAN };
        return playerIndex >=0 ? defaultColors[playerIndex % defaultColors.length] : Color.BLACK;
    }

    public void updatePlayerTokenColors() {
        if (game == null || game.getPlayers() == null) return;
        for(Player player : game.getPlayers()){
            Circle tokenOnBoard = playerTokensOnBoard.get(player);
            if(tokenOnBoard != null){
                tokenOnBoard.setFill(getPlayerColorFromSidePanel(player));
            } else { // Player token not on board, might be new player
                Circle newToken = createTokenForPlayer(player);
                playerTokensOnBoard.put(player, newToken);
                getChildren().add(newToken);
                // Position will be updated in refresh()
            }
        }
    }

    private Circle createTokenForPlayer(Player player) {
        Color playerColor = getPlayerColorFromSidePanel(player);
        Circle token = new Circle(TOKEN_RADIUS, playerColor);
        token.setStroke(Color.BLACK);
        token.setStrokeWidth(1.0); // Thinner stroke for smaller tokens
        token.setEffect(new javafx.scene.effect.DropShadow(2, Color.rgb(0,0,0,0.4)));
        token.setUserData(player); // To identify this node as a player token
        return token;
    }

    private void initializePlayerTokenVisuals() {
        // Remove old player token Circle nodes from the scene graph
        getChildren().removeIf(node -> node.getUserData() instanceof Player && node instanceof Circle);
        playerTokensOnBoard.clear(); // Clear the map

        if (game == null || game.getPlayers() == null) return;
        for (Player player : game.getPlayers()) {
            Circle token = createTokenForPlayer(player);
            playerTokensOnBoard.put(player, token);
            getChildren().add(token);
        }
    }

    public void refresh() {
        // Update tile ownership visuals
        for (int tileId = 0; tileId < game.getBoard().getTiles().size(); tileId++) {
            Tile tile = game.getBoard().getTile(tileId);
            Rectangle tileRect = tileRects.get(tileId);
            if (tileRect == null || tile == null) continue;

            if (tile.getAction() instanceof PropertyAction pa) {
                if (pa.getOwner() != null) {
                    tileRect.setStroke(getPlayerColorFromSidePanel(pa.getOwner()));
                    tileRect.setStrokeWidth(2.5); // Thicker stroke for owned
                } else {
                    tileRect.setStroke(Color.DARKSLATEGRAY);
                    tileRect.setStrokeWidth(1);
                }
            }
        }

        // Update player token positions
        Map<Integer, Integer> playersOnTileCount = new HashMap<>();
        for (Player player : game.getPlayers()) {
            if (player.getCurrentTile() != null) {
                int tileId = player.getCurrentTile().getId();
                playersOnTileCount.put(tileId, playersOnTileCount.getOrDefault(tileId, 0) + 1);
            }
        }

        Map<Integer, Integer> currentPlayerIndexOnTile = new HashMap<>();
        for (Player player : game.getPlayers()) {
            if (player.getCurrentTile() == null) continue;
            int tileId = player.getCurrentTile().getId();
            Point2D tileCenterPos = tileCenterPositions.get(tileId);
            Circle token = playerTokensOnBoard.get(player);

            if (tileCenterPos != null && token != null) {
                int totalOnThisTile = playersOnTileCount.get(tileId);
                int playerIdxOnTile = currentPlayerIndexOnTile.getOrDefault(tileId, 0);
                currentPlayerIndexOnTile.put(tileId, playerIdxOnTile + 1);

                double offsetX = 0;
                double offsetY = 0;
                double offsetStep = TOKEN_RADIUS * 1.5;

                if (totalOnThisTile > 1) {
                    // Simple grid layout (2xN)
                    int col = playerIdxOnTile % 2; // 0 or 1
                    int row = playerIdxOnTile / 2; // 0, 1, 2...
                    offsetX = (col - 0.5) * offsetStep;
                    offsetY = (row - ( (double)(totalOnThisTile-1)/2.0 ) /2.0 ) * offsetStep ; // Center rows vertically
                    if (totalOnThisTile > 6) { // Jitter for many players
                        offsetX = (Math.random() - 0.5) * cellSize * 0.15;
                        offsetY = (Math.random() - 0.5) * cellSize * 0.15;
                    }
                }
                token.setCenterX(tileCenterPos.getX() + offsetX);
                token.setCenterY(tileCenterPos.getY() + offsetY);
                token.toFront();
            } else if (token == null) {
                System.err.println("MonopolyBoardView: Token for player " + player.getName() + " not found during refresh.");
            }
        }
    }
}