package edu.ntnu.idi.bidata.file;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import edu.ntnu.idi.bidata.model.Card;
import edu.ntnu.idi.bidata.util.JsonUtils;
import edu.ntnu.idi.bidata.util.Logger;

import java.io.Reader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A utility class for reading and writing card configurations from/to JSON files.
 * This class provides methods to parse JSON data into a map of card decks, where each deck is a list of {@link Card} objects.
 * It is not meant to be instantiated.
 */
public class CardJsonReaderWriter {

    /**
     * Private constructor to prevent instantiation of this utility class.
     */
    private CardJsonReaderWriter() {
        // Prevent instantiation
    }

    /**
     * Reads card configurations from a JSON file and returns a map of decks.
     * Each deck is represented as a list of {@link Card} objects.
     *
     * @param reader The {@link Reader} to read the JSON data from.
     * @return A map where the keys are deck names (e.g., "chance", "communityChest") and the values are lists of {@link Card} objects.
     */
    public static Map<String, List<Card>> read(Reader reader) {
        Logger.info("Starting to read card configurations from JSON.");
        JsonObject root = JsonUtils.read(reader);

        if (root == null) {
            Logger.error("Failed to parse root JSON for cards. Root object is null. Aborting card reading.");
            return new HashMap<>(); // Return empty map to prevent NullPointerExceptions further down
        }
        Logger.debug("Successfully parsed root JSON for cards.");

        Map<String, List<Card>> decks = new HashMap<>();

        if (root.has("chanceCards") && root.get("chanceCards").isJsonArray()) {
            Logger.debug("Reading Chance cards...");
            List<Card> chanceCards = readCardArray(root.getAsJsonArray("chanceCards"), "Chance");
            decks.put("chance", chanceCards);
            Logger.debug("Finished reading " + chanceCards.size() + " Chance cards.");
        } else {
            Logger.warning("JSON data does not contain a 'chanceCards' array or it's not a valid array. Chance deck will be empty.");
            decks.put("chance", new ArrayList<>());
        }

        if (root.has("communityChestCards") && root.get("communityChestCards").isJsonArray()) {
            Logger.debug("Reading Community Chest cards...");
            List<Card> communityChestCards = readCardArray(root.getAsJsonArray("communityChestCards"), "Community Chest");
            decks.put("communityChest", communityChestCards);
            Logger.debug("Finished reading " + communityChestCards.size() + " Community Chest cards.");
        } else {
            Logger.warning("JSON data does not contain a 'communityChestCards' array or it's not a valid array. Community Chest deck will be empty.");
            decks.put("communityChest", new ArrayList<>());
        }

        Logger.info("Finished reading card configurations. Loaded " + decks.size() + " deck types ('chance', 'communityChest').");
        return decks;
    }

    // Added deckType parameter for more specific logging
    private static List<Card> readCardArray(JsonArray cardsJson, String deckType) {
        Logger.debug("Processing " + deckType + " card array. Expected number of entries: " + cardsJson.size());
        List<Card> cards = new ArrayList<>();

        for (JsonElement element : cardsJson) {
            if (!element.isJsonObject()) {
                Logger.warning("Skipping a non-JSON object element found in the " + deckType + " card array: " + element);
                continue;
            }
            JsonObject cardJson = element.getAsJsonObject();

            try {
                // Check for presence of essential fields before attempting to get them
                if (!cardJson.has("id")) {
                    Logger.warning("Skipping " + deckType + " card due to missing 'id' field: " + cardJson);
                    continue;
                }
                if (!cardJson.has("type")) {
                    Logger.warning("Skipping " + deckType + " card with id " + (cardJson.has("id") ? cardJson.get("id").getAsString() : "UNKNOWN") + " due to missing 'type' field: " + cardJson);
                    continue;
                }
                if (!cardJson.has("description")) {
                    Logger.warning("Skipping " + deckType + " card with id " + (cardJson.has("id") ? cardJson.get("id").getAsString() : "UNKNOWN") + " due to missing 'description' field: " + cardJson);
                    continue;
                }


                int id = cardJson.get("id").getAsInt(); // Could also throw if not int, but GSON usually handles well, or we catch below
                String type = cardJson.get("type").getAsString();
                String description = cardJson.get("description").getAsString();

                cards.add(new Card(id, type, description, cardJson));
                Logger.debug("Successfully created " + deckType + " card - ID: " + id + ", Type: '" + type + "'.");
            } catch (Exception e) {
                // Catching general exception for robustness during parsing individual cards (e.g., NumberFormatException if id is not int)
                Logger.error("Error parsing a " + deckType + " card. JSON content: " + cardJson.toString() + ". Skipping this card.", e);
            }
        }
        Logger.debug("Successfully processed and loaded " + cards.size() + " cards for the " + deckType + " deck.");
        return cards;
    }
}