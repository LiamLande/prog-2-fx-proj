// file/BoardJsonReaderWriter.java
package edu.ntnu.idi.bidata.file;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import edu.ntnu.idi.bidata.app.GameVariant;
import edu.ntnu.idi.bidata.model.Board;
import edu.ntnu.idi.bidata.model.Tile;
// Import your specific action classes
import edu.ntnu.idi.bidata.model.actions.snakes.LadderAction;
import edu.ntnu.idi.bidata.model.actions.snakes.SnakeAction;
import edu.ntnu.idi.bidata.model.actions.snakes.SchrodingerBoxAction; // <--- IMPORT THE NEW ACTION
import edu.ntnu.idi.bidata.exception.JsonParseException;
import edu.ntnu.idi.bidata.util.JsonUtils;
import edu.ntnu.idi.bidata.model.actions.monopoly.*;

import java.io.Reader;

/**
 * Reads a Board configuration from JSON.
 * JSON structure:
 * {
 *   "tiles": [ {"id":0, "nextId":1, "action":{...}}, ... ]
 * }
 */
public class BoardJsonReaderWriter {

  /**
   * Reads and builds a Board from the given JSON reader.
   * @param reader source of JSON data
   * @param variant the game variant being loaded
   * @return constructed Board
   * @throws JsonParseException if JSON is invalid or I/O error
   */
  public static Board read(Reader reader, GameVariant variant) {
    JsonObject root = JsonUtils.read(reader); // Assuming JsonUtils.read handles parsing reader to JsonObject
    JsonArray tilesJson = root.getAsJsonArray("tiles");

    Board board = new Board();
    // First pass: create all tiles and add them to the board's map
    // This ensures all tile objects exist before setting next/previous links or actions.
    for (JsonElement elem : tilesJson) {
      JsonObject tileObj = elem.getAsJsonObject();
      int id = tileObj.get("id").getAsInt();
      board.addTile(new Tile(id)); // Assuming Board.addTile(Tile) correctly stores it
    }

    // Second pass: set links (nextId, prevId if present) and actions
    for (JsonElement elem : tilesJson) {
      JsonObject tileObj = elem.getAsJsonObject();
      int id = tileObj.get("id").getAsInt();
      Tile tile = board.getTile(id); // Retrieve the already created tile

      // Set next link (and implicitly previous for doubly linked lists)
      if (tileObj.has("nextId")) {
        int nextId = tileObj.get("nextId").getAsInt();
        Tile nextTile = board.getTile(nextId);
        if (nextTile != null) {
          tile.setNext(nextTile);
          // If your tiles are doubly linked and setPrevious is handled by setNext, great.
          // Otherwise, if you need to set previous explicitly from JSON:
          // if (tileObj.has("prevId")) {
          //    int prevId = tileObj.get("prevId").getAsInt();
          //    Tile prevTile = board.getTile(prevId);
          //    tile.setPrevious(prevTile);
          // }
          // For S&L, often the next link also implies the previous link for the next tile.
          // For Monopoly, you explicitly set next and previous usually.
          if (variant == GameVariant.MINI_MONOPOLY || variant == GameVariant.SNAKES_LADDERS) { // Assuming S&L also sets previous via next
            if (nextTile.getPrevious() == null) { // Basic check to avoid overwriting if already set
              nextTile.setPrevious(tile);
            }
          }
        } else {
          System.err.println("Warning: Next tile with id " + nextId + " not found for tile " + id);
        }
      }


      // Set action if present
      if (tileObj.has("action")) {
        JsonObject actionJson = tileObj.getAsJsonObject("action");
        String type = actionJson.get("type").getAsString();
        String description = actionJson.has("description") ? actionJson.get("description").getAsString() : "";

        switch (variant) {
          case MINI_MONOPOLY:
            // Monopoly specific actions
            switch (type) {
              case "GoAction":
                int reward = actionJson.get("reward").getAsInt();
                tile.setAction(new GoAction(description, reward));
                break;
              case "PropertyAction":
                String propName = actionJson.get("name").getAsString();
                int cost = actionJson.get("cost").getAsInt();
                int rent = actionJson.get("rent").getAsInt();
                // String colorGroup = actionJson.get("colorGroup").getAsString(); // If you use colorGroup
                tile.setAction(new PropertyAction(propName, cost, rent)); // Simplified constructor for example
                break;
              case "CommunityChestAction":
                tile.setAction(new CommunityChestAction(description));
                break;
              case "TaxAction":
                int amount = actionJson.get("amount").getAsInt();
                tile.setAction(new TaxAction(description, amount));
                break;
              case "RailroadAction":
                String rrName = actionJson.get("name").getAsString();
                int rrCost = actionJson.get("cost").getAsInt();
                int rrRent = actionJson.get("rent").getAsInt();
                tile.setAction(new RailroadAction(rrName, rrCost, rrRent));
                break;
              case "UtilityAction":
                String utilName = actionJson.get("name").getAsString();
                int utilCost = actionJson.get("cost").getAsInt();
                tile.setAction(new UtilityAction(utilName, utilCost));
                break;
              case "ChanceAction":
                tile.setAction(new ChanceAction(description));
                break;
              case "JailAction": // Just Visiting Jail
                tile.setAction(new JailAction(description));
                break;
              case "GoToJailAction":
                int targetJailTileId = actionJson.get("targetId").getAsInt(); // ID of the jail tile
                tile.setAction(new GoToJailAction(description, targetJailTileId));
                break;
              case "FreeParkingAction":
                tile.setAction(new FreeParkingAction(description));
                break;
              default:
                System.err.println("Warning: Unknown Monopoly action type '" + type + "' for tile " + id + ". No action set.");
                // Optionally throw new JsonParseException("Unknown Monopoly action type: " + type);
                break;
            }
            break; // End of MINI_MONOPOLY action switch

          case SNAKES_LADDERS:
            // Snakes & Ladders specific actions
            switch (type) {
              case "LadderAction":
                int ladderSteps = actionJson.get("steps").getAsInt();
                tile.setAction(new LadderAction(description, ladderSteps));
                break;
              case "SnakeAction":
                int snakeSteps = actionJson.get("steps").getAsInt();
                tile.setAction(new SnakeAction(description, snakeSteps));
                break;
              case "SchrodingerBoxAction": // <--- ADDED CASE
                // Description is optional for SchrodingerBoxAction, its constructor handles null/empty
                tile.setAction(new SchrodingerBoxAction(description));
                break;
              // Add cases for your other S&L specific "Noe Ekstra" actions here
              // e.g., "MoveToStartAction", "SkipTurnAction"
              // case "MoveToStartAction":
              //    tile.setAction(new MoveToStartAction(description));
              //    break;
              // case "SkipTurnAction":
              //    tile.setAction(new SkipTurnAction(description));
              //    break;
              default:
                System.err.println("Warning: Unknown Snakes & Ladders action type '" + type + "' for tile " + id + ". No action set.");
                // Optionally throw new JsonParseException("Unknown S&L action type: " + type);
                break;
            }
            break; // End of SNAKES_LADDERS action switch
        }
      }
    }
    return board;
  }
}