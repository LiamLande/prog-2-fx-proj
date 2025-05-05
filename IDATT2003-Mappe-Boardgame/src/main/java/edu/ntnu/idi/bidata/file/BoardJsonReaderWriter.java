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
   * @return constructed Board
   * @throws JsonParseException if JSON is invalid or I/O error
   */
  public static Board read(Reader reader, GameVariant variant) {
    JsonObject root = JsonUtils.read(reader);
    JsonArray tilesJson = root.getAsJsonArray("tiles");

    Board board = new Board();
    // First pass: create tiles
    for (JsonElement elem : tilesJson) {
      JsonObject tileObj = elem.getAsJsonObject();
      int id = tileObj.get("id").getAsInt();
      board.addTile(new Tile(id));
    }

    switch (variant) {
      case MINI_MONOPOLY:
        // Second pass: set links and actions
        for (JsonElement elem : tilesJson) {
          JsonObject tileObj = elem.getAsJsonObject();
          int id = tileObj.get("id").getAsInt();
          Tile tile = board.getTile(id);
          int nextId = tileObj.get("nextId").getAsInt();
          Tile next = board.getTile(nextId);
          tile.setNext(next);
          next.setPrevious(tile);

          if (tileObj.has("action")) {
            JsonObject a = tileObj.getAsJsonObject("action");
            String type = a.get("type").getAsString();

            switch (type) {
              case "GoAction":
                String goDesc = a.get("description").getAsString();
                int reward = a.get("reward").getAsInt();
                tile.setAction(new GoAction(goDesc, reward));
                break;
              case "PropertyAction":
                String propName = a.get("name").getAsString();
                int cost = a.get("cost").getAsInt();
                int rent = a.get("rent").getAsInt();
                String colorGroup = a.get("colorGroup").getAsString();
                tile.setAction(new PropertyAction(propName, cost, rent));
                break;
              case "CommunityChestAction":
                String chestDesc = a.get("description").getAsString();
                tile.setAction(new CommunityChestAction(chestDesc));
                break;
              case "TaxAction":
                String taxDesc = a.get("description").getAsString();
                int amount = a.get("amount").getAsInt();
                tile.setAction(new TaxAction(taxDesc, amount));
                break;
              case "RailroadAction":
                String rrName = a.get("name").getAsString();
                int rrCost = a.get("cost").getAsInt();
                int rrRent = a.get("rent").getAsInt();
                tile.setAction(new RailroadAction(rrName, rrCost, rrRent));
                break;
              case "UtilityAction":
                String utilName = a.get("name").getAsString();
                int utilCost = a.get("cost").getAsInt();
                tile.setAction(new UtilityAction(utilName, utilCost));
                break;
              case "ChanceAction":
                String chanceDesc = a.get("description").getAsString();
                tile.setAction(new ChanceAction(chanceDesc));
                break;
              case "JailAction":
                String jailDesc = a.get("description").getAsString();
                tile.setAction(new JailAction(jailDesc));
                break;
              case "GoToJailAction":
                String goToJailDesc = a.get("description").getAsString();
                int targetId = a.get("targetId").getAsInt();
                tile.setAction(new GoToJailAction(goToJailDesc, targetId));
                break;
              case "FreeParkingAction":
                String parkingDesc = a.get("description").getAsString();
                tile.setAction(new FreeParkingAction(parkingDesc));
                break;
              default:
                throw new JsonParseException("Unknown action type: " + type);
            }
          }
        }
        break;




      case SNAKES_LADDERS:
        // Second pass: set links and actions
        for (JsonElement elem : tilesJson) {
          JsonObject tileObj = elem.getAsJsonObject();
          int id = tileObj.get("id").getAsInt();
          Tile tile = board.getTile(id);

          if (tileObj.has("nextId")) {
            int nextId = tileObj.get("nextId").getAsInt();
            Tile next = board.getTile(nextId);
            tile.setNext(next);
            next.setPrevious(tile);
          }

          if (tileObj.has("action")) {
            JsonObject a = tileObj.getAsJsonObject("action");
            String type = a.get("type").getAsString();
            String desc = a.get("description").getAsString();
            int steps = a.get("steps").getAsInt();
            switch (type) {
              case "LadderAction":
                tile.setAction(new LadderAction(desc, steps));
                break;
              case "SnakeAction":
                tile.setAction(new SnakeAction(desc, steps));
                break;
              default:
                throw new JsonParseException("Unknown action type: " + type);
            }
          }
        }
        break;
    }



    return board;
  }
}
