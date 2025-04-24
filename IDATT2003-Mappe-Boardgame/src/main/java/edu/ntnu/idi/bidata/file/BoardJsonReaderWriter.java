// file/BoardJsonReaderWriter.java
package edu.ntnu.idi.bidata.file;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import edu.ntnu.idi.bidata.model.Board;
import edu.ntnu.idi.bidata.model.Tile;
import edu.ntnu.idi.bidata.model.actions.snakes.LadderAction;
import edu.ntnu.idi.bidata.model.actions.snakes.SnakeAction;
import edu.ntnu.idi.bidata.exception.JsonParseException;
import edu.ntnu.idi.bidata.util.JsonUtils;

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
  public static Board read(Reader reader) {
    JsonObject root = JsonUtils.read(reader);
    JsonArray tilesJson = root.getAsJsonArray("tiles");

    Board board = new Board();
    // First pass: create tiles
    for (JsonElement elem : tilesJson) {
      JsonObject tileObj = elem.getAsJsonObject();
      int id = tileObj.get("id").getAsInt();
      board.addTile(new Tile(id));
    }

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

    return board;
  }
}
