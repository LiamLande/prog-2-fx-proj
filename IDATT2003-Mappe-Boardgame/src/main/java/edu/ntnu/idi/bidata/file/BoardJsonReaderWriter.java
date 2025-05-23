// file/BoardJsonReaderWriter.java
package edu.ntnu.idi.bidata.file;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import edu.ntnu.idi.bidata.app.GameVariant;
import edu.ntnu.idi.bidata.model.Board;
import edu.ntnu.idi.bidata.model.Tile;
import edu.ntnu.idi.bidata.model.actions.snakes.LadderAction;
import edu.ntnu.idi.bidata.model.actions.snakes.SnakeAction;
import edu.ntnu.idi.bidata.model.actions.snakes.SchrodingerBoxAction;
import edu.ntnu.idi.bidata.exception.JsonParseException;
import edu.ntnu.idi.bidata.util.JsonUtils;
import edu.ntnu.idi.bidata.model.actions.monopoly.*;
import edu.ntnu.idi.bidata.util.Logger; // Added Logger import

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
    Logger.info("Starting to read board configuration for game variant: " + variant);
    JsonObject root = JsonUtils.read(reader);
    JsonArray tilesJson = root.getAsJsonArray("tiles");
    Logger.debug("Successfully parsed root JSON and tiles array. Number of tile entries: " + (tilesJson != null ? tilesJson.size() : "null"));


    Board board = new Board();

    Logger.debug("First pass: Creating tiles without links or actions.");
    assert tilesJson != null;
    for (JsonElement elem : tilesJson) {
      JsonObject tileObj = elem.getAsJsonObject();
      int id = tileObj.get("id").getAsInt();
      board.addTile(new Tile(id));
      Logger.debug("Created and added tile with id: " + id);
    }

    Logger.debug("Second pass: Setting links and actions for tiles.");
    for (JsonElement elem : tilesJson) {
      JsonObject tileObj = elem.getAsJsonObject();
      int id = tileObj.get("id").getAsInt();
      Tile tile = board.getTile(id);

      if (tile == null) {
        Logger.error("Critical error: Tile with id " + id + " was expected but not found in the board during second pass. Skipping this tile entry.");
        continue;
      }
      Logger.debug("Processing tile id: " + id + " for links and actions.");

      if (tileObj.has("nextId")) {
        int nextId = tileObj.get("nextId").getAsInt();
        Tile nextTile = board.getTile(nextId);
        if (nextTile != null) {
          tile.setNext(nextTile);
          Logger.debug("Set next link for tile " + id + " to tile " + nextId);
          if (variant == GameVariant.MINI_MONOPOLY || variant == GameVariant.SNAKES_LADDERS) {
            if (nextTile.getPrevious() == null) {
              nextTile.setPrevious(tile);
              Logger.debug("Set previous link for tile " + nextId + " to tile " + id);
            } else {
              Logger.debug("Previous link for tile " + nextId + " was already set to " + nextTile.getPrevious().getId() + ". Not overwriting from tile " + id);
            }
          }
        } else {
          Logger.warning("Next tile with id " + nextId + " not found for tile " + id + ". Link not set.");
        }
      }


      if (tileObj.has("action")) {
        JsonObject actionJson = tileObj.getAsJsonObject("action");
        String type = actionJson.get("type").getAsString();
        String description = actionJson.has("description") ? actionJson.get("description").getAsString() : "";
        Logger.debug("Processing action of type '" + type + "' for tile " + id);

        switch (variant) {
          case MINI_MONOPOLY:
            switch (type) {
              case "GoAction":
                int reward = actionJson.get("reward").getAsInt();
                tile.setAction(new GoAction(description, reward));
                Logger.debug("Set GoAction for tile " + id + " with reward " + reward);
                break;
              case "PropertyAction":
                String propName = actionJson.get("name").getAsString();
                int cost = actionJson.get("cost").getAsInt();
                int rent = actionJson.get("rent").getAsInt();
                String colorGroup = actionJson.get("colorGroup").getAsString();
                tile.setAction(new PropertyAction(propName, cost, rent, colorGroup));
                Logger.debug("Set PropertyAction '" + propName + "' for tile " + id);
                break;
              case "CommunityChestAction":
                tile.setAction(new CommunityChestAction(description));
                Logger.debug("Set CommunityChestAction for tile " + id);
                break;
              case "TaxAction":
                int amount = actionJson.get("amount").getAsInt();
                tile.setAction(new TaxAction(description, amount));
                Logger.debug("Set TaxAction for tile " + id + " with amount " + amount);
                break;
              case "RailroadAction":
                String rrName = actionJson.get("name").getAsString();
                int rrCost = actionJson.get("cost").getAsInt();
                int rrRent = actionJson.get("rent").getAsInt();
                tile.setAction(new RailroadAction(rrName, rrCost, rrRent));
                Logger.debug("Set RailroadAction '" + rrName + "' for tile " + id);
                break;
              case "UtilityAction":
                String utilName = actionJson.get("name").getAsString();
                int utilCost = actionJson.get("cost").getAsInt();
                tile.setAction(new UtilityAction(utilName, utilCost));
                Logger.debug("Set UtilityAction '" + utilName + "' for tile " + id);
                break;
              case "ChanceAction":
                tile.setAction(new ChanceAction(description));
                Logger.debug("Set ChanceAction for tile " + id);
                break;
              case "JailAction":
                tile.setAction(new JailAction(description));
                Logger.debug("Set JailAction (Just Visiting) for tile " + id);
                break;
              case "GoToJailAction":
                int targetJailTileId = actionJson.get("targetId").getAsInt();
                tile.setAction(new GoToJailAction(description, targetJailTileId));
                Logger.debug("Set GoToJailAction for tile " + id + ", targeting jail tile " + targetJailTileId);
                break;
              case "FreeParkingAction":
                tile.setAction(new FreeParkingAction(description));
                Logger.debug("Set FreeParkingAction for tile " + id);
                break;
              default:
                Logger.warning("Unknown Monopoly action type '" + type + "' for tile " + id + ". No action set.");
                break;
            }
            break;

          case SNAKES_LADDERS:
            switch (type) {
              case "LadderAction":
                int ladderSteps = actionJson.get("steps").getAsInt();
                tile.setAction(new LadderAction(description, ladderSteps));
                Logger.debug("Set LadderAction for tile " + id + " with " + ladderSteps + " steps.");
                break;
              case "SnakeAction":
                int snakeSteps = actionJson.get("steps").getAsInt();
                tile.setAction(new SnakeAction(description, snakeSteps));
                Logger.debug("Set SnakeAction for tile " + id + " with " + snakeSteps + " steps.");
                break;
              case "SchrodingerBoxAction":
                tile.setAction(new SchrodingerBoxAction(description));
                Logger.debug("Set SchrodingerBoxAction for tile " + id);
                break;
              default:
                Logger.warning("Unknown Snakes & Ladders action type '" + type + "' for tile " + id + ". No action set.");
                break;
            }
            break;
        }
      } else {
        Logger.debug("No action specified for tile " + id);
      }
    }
    Logger.info("Successfully finished reading and constructing board. Total tiles: " + board.getTiles().size());
    return board;
  }
}