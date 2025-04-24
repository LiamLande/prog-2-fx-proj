// file/BoardJsonReaderWriter.java
package edu.ntnu.idi.bidata.file;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import edu.ntnu.idi.bidata.model.Board;
import edu.ntnu.idi.bidata.model.Card;
import edu.ntnu.idi.bidata.model.Tile;
import edu.ntnu.idi.bidata.model.actions.snakes.LadderAction;
import edu.ntnu.idi.bidata.model.actions.snakes.SnakeAction;
import edu.ntnu.idi.bidata.exception.JsonParseException;
import edu.ntnu.idi.bidata.util.JsonUtils;

import java.io.Reader;
import java.util.List;

/**
 * Reads a Board configuration from JSON.
 * JSON structure:
 * {
 *   "tiles": [ {"id":0, "nextId":1, "action":{...}}, ... ]
 * }
 */
public class CardJsonReaderWriter {

    /**
     * Reads and builds two decks of cards from the given JSON reader.
     * @param reader source of JSON data
     * @return constructed Board
     * @throws JsonParseException if JSON is invalid or I/O error
     */
//    public static List[Card] read(Reader reader) {
//        JsonObject root = JsonUtils.read(reader);
//
//        JsonArray chanceCardsJson = root.getAsJsonArray("chanceCards");
//
//
//
//
//        JsonArray communityChestCards = root.getAsJsonArray("communityChestCards");
//
//        return List.of(new Card(2,2, "Chance", chanceCardsJson),);
//                new Card(3, "Community Chest", communityChestCards));
//    }
}
