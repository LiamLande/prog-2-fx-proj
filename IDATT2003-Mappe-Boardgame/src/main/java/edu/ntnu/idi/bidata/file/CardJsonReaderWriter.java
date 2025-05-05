package edu.ntnu.idi.bidata.file;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import edu.ntnu.idi.bidata.model.Card;
import edu.ntnu.idi.bidata.exception.JsonParseException;
import edu.ntnu.idi.bidata.util.JsonUtils;

import java.io.Reader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CardJsonReaderWriter {

    public static Map<String, List<Card>> read(Reader reader) {
        JsonObject root = JsonUtils.read(reader);

        Map<String, List<Card>> decks = new HashMap<>();

        decks.put("chance", readCardArray(root.getAsJsonArray("chanceCards")));
        decks.put("communityChest", readCardArray(root.getAsJsonArray("communityChestCards")));

        return decks;
    }

    private static List<Card> readCardArray(JsonArray cardsJson) {
        List<Card> cards = new ArrayList<>();

        for (JsonElement element : cardsJson) {
            JsonObject cardJson = element.getAsJsonObject();

            int id = cardJson.get("id").getAsInt();
            String type = cardJson.get("type").getAsString();
            String description = cardJson.get("description").getAsString();

            cards.add(new Card(id, type, description, cardJson));
        }

        return cards;
    }
}