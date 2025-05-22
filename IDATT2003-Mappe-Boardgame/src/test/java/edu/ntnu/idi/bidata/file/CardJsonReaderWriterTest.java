package edu.ntnu.idi.bidata.file;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

import edu.ntnu.idi.bidata.model.Card;
import edu.ntnu.idi.bidata.util.JsonUtils;
import edu.ntnu.idi.bidata.util.Logger;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.Reader;
import java.io.StringReader;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CardJsonReaderWriterTest {

    // No @InjectMocks because all methods are static.
    // We will use MockedStatic for JsonUtils and Logger.

    @Test
    void read_nullRootJson_returnsEmptyMapAndLogsError() {
        try (MockedStatic<JsonUtils> mockedJsonUtils = Mockito.mockStatic(JsonUtils.class);
             MockedStatic<Logger> mockedLogger = Mockito.mockStatic(Logger.class)) {

            when(JsonUtils.read(any(Reader.class))).thenReturn(null);
            StringReader dummyReader = new StringReader("{}"); // Content doesn't matter as read is mocked

            Map<String, List<Card>> decks = CardJsonReaderWriter.read(dummyReader);

            assertTrue(decks.isEmpty());
            mockedLogger.verify(() -> Logger.info("Starting to read card configurations from JSON."));
            mockedLogger.verify(() -> Logger.error("Failed to parse root JSON for cards. Root object is null. Aborting card reading."));
            // The "Finished reading..." log is NOT called in this path due to early return
            mockedLogger.verify(() -> Logger.info(contains("Finished reading card configurations.")), never());
        }
    }

    @Test
    void read_emptyRootJson_returnsEmptyDecksAndLogsWarnings() {
        try (MockedStatic<JsonUtils> mockedJsonUtils = Mockito.mockStatic(JsonUtils.class);
             MockedStatic<Logger> mockedLogger = Mockito.mockStatic(Logger.class)) {

            JsonObject emptyRoot = new JsonObject();
            when(JsonUtils.read(any(Reader.class))).thenReturn(emptyRoot);
            StringReader dummyReader = new StringReader("{}");

            Map<String, List<Card>> decks = CardJsonReaderWriter.read(dummyReader);

            assertNotNull(decks.get("chance"));
            assertTrue(decks.get("chance").isEmpty());
            assertNotNull(decks.get("communityChest"));
            assertTrue(decks.get("communityChest").isEmpty());

            mockedLogger.verify(() -> Logger.warning(contains("JSON data does not contain a 'chanceCards' array")));
            mockedLogger.verify(() -> Logger.warning(contains("JSON data does not contain a 'communityChestCards' array")));
            mockedLogger.verify(() -> Logger.info("Finished reading card configurations. Loaded 2 deck types ('chance', 'communityChest')."));
        }
    }

    @Test
    void read_rootJsonWithNonArrayDecks_returnsEmptyDecksAndLogsWarnings() {
        try (MockedStatic<JsonUtils> mockedJsonUtils = Mockito.mockStatic(JsonUtils.class);
             MockedStatic<Logger> mockedLogger = Mockito.mockStatic(Logger.class)) {

            JsonObject rootWithObject = new JsonObject();
            rootWithObject.add("chanceCards", new JsonObject()); // Not an array
            rootWithObject.add("communityChestCards", new JsonPrimitive("not an array")); // Not an array

            when(JsonUtils.read(any(Reader.class))).thenReturn(rootWithObject);
            StringReader dummyReader = new StringReader("{\"chanceCards\":{}, \"communityChestCards\":\"string\"}");

            Map<String, List<Card>> decks = CardJsonReaderWriter.read(dummyReader);

            assertTrue(decks.get("chance").isEmpty());
            assertTrue(decks.get("communityChest").isEmpty());

            mockedLogger.verify(() -> Logger.warning(contains("'chanceCards' array or it's not a valid array")));
            mockedLogger.verify(() -> Logger.warning(contains("'communityChestCards' array or it's not a valid array")));
        }
    }

    @Test
    void read_validChanceCardsOnly_loadsChanceDeck() {
        try (MockedStatic<JsonUtils> mockedJsonUtils = Mockito.mockStatic(JsonUtils.class);
             MockedStatic<Logger> mockedLogger = Mockito.mockStatic(Logger.class)) {

            JsonObject root = new JsonObject();
            JsonArray chanceArray = new JsonArray();
            JsonObject chanceCard1Json = new JsonObject();
            chanceCard1Json.addProperty("id", 1);
            chanceCard1Json.addProperty("type", "Advance");
            chanceCard1Json.addProperty("description", "Go to Go");
            chanceArray.add(chanceCard1Json);
            root.add("chanceCards", chanceArray);

            when(JsonUtils.read(any(Reader.class))).thenReturn(root);
            StringReader dummyReader = new StringReader("{\"chanceCards\":[{\"id\":1,\"type\":\"Advance\",\"description\":\"Go to Go\"}]}");

            Map<String, List<Card>> decks = CardJsonReaderWriter.read(dummyReader);

            assertEquals(1, decks.get("chance").size());
            assertEquals("Advance", decks.get("chance").get(0).getType());
            assertTrue(decks.get("communityChest").isEmpty());

            mockedLogger.verify(() -> Logger.debug("Finished reading 1 Chance cards."));
            mockedLogger.verify(() -> Logger.warning(contains("'communityChestCards' array or it's not a valid array")));
        }
    }

    @Test
    void read_validCommunityChestCardsOnly_loadsCommunityDeck() {
        try (MockedStatic<JsonUtils> mockedJsonUtils = Mockito.mockStatic(JsonUtils.class);
             MockedStatic<Logger> mockedLogger = Mockito.mockStatic(Logger.class)) {

            JsonObject root = new JsonObject();
            JsonArray ccArray = new JsonArray();
            JsonObject ccCard1Json = new JsonObject();
            ccCard1Json.addProperty("id", 101);
            ccCard1Json.addProperty("type", "Pay");
            ccCard1Json.addProperty("description", "Pay Doctor's Fee");
            ccArray.add(ccCard1Json);
            root.add("communityChestCards", ccArray);

            when(JsonUtils.read(any(Reader.class))).thenReturn(root);
            StringReader dummyReader = new StringReader("{\"communityChestCards\":[{\"id\":101,\"type\":\"Pay\",\"description\":\"Pay Doctor's Fee\"}]}");


            Map<String, List<Card>> decks = CardJsonReaderWriter.read(dummyReader);

            assertTrue(decks.get("chance").isEmpty());
            assertEquals(1, decks.get("communityChest").size());
            assertEquals("Pay", decks.get("communityChest").get(0).getType());

            mockedLogger.verify(() -> Logger.debug("Finished reading 1 Community Chest cards."));
            mockedLogger.verify(() -> Logger.warning(contains("'chanceCards' array or it's not a valid array")));
        }
    }

    @Test
    void read_bothDecksValid_loadsBothDecks() {
        try (MockedStatic<JsonUtils> mockedJsonUtils = Mockito.mockStatic(JsonUtils.class);
             MockedStatic<Logger> mockedLogger = Mockito.mockStatic(Logger.class)) {

            JsonObject root = new JsonObject();
            JsonArray chanceArray = new JsonArray();
            JsonObject chanceCard1Json = new JsonObject();
            chanceCard1Json.addProperty("id", 1);
            chanceCard1Json.addProperty("type", "Advance");
            chanceCard1Json.addProperty("description", "Go to Go");
            chanceArray.add(chanceCard1Json);
            root.add("chanceCards", chanceArray);

            JsonArray ccArray = new JsonArray();
            JsonObject ccCard1Json = new JsonObject();
            ccCard1Json.addProperty("id", 101);
            ccCard1Json.addProperty("type", "Pay");
            ccCard1Json.addProperty("description", "Pay Doctor's Fee");
            ccArray.add(ccCard1Json);
            root.add("communityChestCards", ccArray);

            when(JsonUtils.read(any(Reader.class))).thenReturn(root);
            StringReader dummyReader = new StringReader("{\"chanceCards\":[...], \"communityChestCards\":[...]}");


            Map<String, List<Card>> decks = CardJsonReaderWriter.read(dummyReader);

            assertEquals(1, decks.get("chance").size());
            assertEquals(1, decks.get("communityChest").size());
            mockedLogger.verify(() -> Logger.info("Finished reading card configurations. Loaded 2 deck types ('chance', 'communityChest')."));
        }
    }

    @Test
    void readCardArray_emptyArray_returnsEmptyList() {
        try (MockedStatic<JsonUtils> mockedJsonUtils = Mockito.mockStatic(JsonUtils.class);
             MockedStatic<Logger> mockedLogger = Mockito.mockStatic(Logger.class)) {

            JsonObject root = new JsonObject();
            root.add("chanceCards", new JsonArray());
            when(JsonUtils.read(any(Reader.class))).thenReturn(root);
            StringReader dummyReader = new StringReader("{\"chanceCards\":[]}");

            Map<String, List<Card>> decks = CardJsonReaderWriter.read(dummyReader);
            assertTrue(decks.get("chance").isEmpty());
            mockedLogger.verify(() -> Logger.debug("Processing Chance card array. Expected number of entries: 0"));
            mockedLogger.verify(() -> Logger.debug("Successfully processed and loaded 0 cards for the Chance deck."));
        }
    }

    @Test
    void readCardArray_elementNotJsonObject_skipsElementAndLogs() {
        try (MockedStatic<JsonUtils> mockedJsonUtils = Mockito.mockStatic(JsonUtils.class);
             MockedStatic<Logger> mockedLogger = Mockito.mockStatic(Logger.class)) {

            JsonObject root = new JsonObject();
            JsonArray chanceArray = new JsonArray();
            chanceArray.add(new JsonPrimitive("not a card object"));
            JsonObject validCardJson = new JsonObject();
            validCardJson.addProperty("id", 1);
            validCardJson.addProperty("type", "Valid");
            validCardJson.addProperty("description", "This is valid");
            chanceArray.add(validCardJson);
            root.add("chanceCards", chanceArray);

            when(JsonUtils.read(any(Reader.class))).thenReturn(root);
            StringReader dummyReader = new StringReader("...");

            Map<String, List<Card>> decks = CardJsonReaderWriter.read(dummyReader);
            assertEquals(1, decks.get("chance").size());
            assertEquals("Valid", decks.get("chance").get(0).getType());
            mockedLogger.verify(() -> Logger.warning(eq("Skipping a non-JSON object element found in the Chance card array: \"not a card object\"")));
        }
    }

    @Test
    void readCardArray_cardMissingId_skipsCardAndLogs() {
        try (MockedStatic<JsonUtils> mockedJsonUtils = Mockito.mockStatic(JsonUtils.class);
             MockedStatic<Logger> mockedLogger = Mockito.mockStatic(Logger.class)) {
            JsonObject root = new JsonObject();
            JsonArray chanceArray = new JsonArray();
            JsonObject cardMissingId = new JsonObject();
            cardMissingId.addProperty("type", "TypeA");
            cardMissingId.addProperty("description", "DescA");
            chanceArray.add(cardMissingId);
            root.add("chanceCards", chanceArray);

            when(JsonUtils.read(any(Reader.class))).thenReturn(root);
            Map<String, List<Card>> decks = CardJsonReaderWriter.read(new StringReader("..."));
            assertTrue(decks.get("chance").isEmpty());
            mockedLogger.verify(() -> Logger.warning(eq("Skipping Chance card due to missing 'id' field: {\"type\":\"TypeA\",\"description\":\"DescA\"}")));
        }
    }

    @Test
    void readCardArray_cardMissingType_skipsCardAndLogs() {
        try (MockedStatic<JsonUtils> mockedJsonUtils = Mockito.mockStatic(JsonUtils.class);
             MockedStatic<Logger> mockedLogger = Mockito.mockStatic(Logger.class)) {
            JsonObject root = new JsonObject();
            JsonArray chanceArray = new JsonArray();
            JsonObject cardMissingType = new JsonObject();
            cardMissingType.addProperty("id", 1);
            cardMissingType.addProperty("description", "DescA");
            chanceArray.add(cardMissingType);
            root.add("chanceCards", chanceArray);

            when(JsonUtils.read(any(Reader.class))).thenReturn(root);
            Map<String, List<Card>> decks = CardJsonReaderWriter.read(new StringReader("..."));
            assertTrue(decks.get("chance").isEmpty());
            mockedLogger.verify(() -> Logger.warning(eq("Skipping Chance card with id 1 due to missing 'type' field: {\"id\":1,\"description\":\"DescA\"}")));
        }
    }

    @Test
    void readCardArray_cardMissingTypeButHasId_logsIdInWarning() {
        try (MockedStatic<JsonUtils> mockedJsonUtils = Mockito.mockStatic(JsonUtils.class);
             MockedStatic<Logger> mockedLogger = Mockito.mockStatic(Logger.class)) {
            JsonObject root = new JsonObject();
            JsonArray chanceArray = new JsonArray();
            JsonObject cardMissingType = new JsonObject();
            cardMissingType.addProperty("id", 99);
            cardMissingType.addProperty("description", "DescA");
            chanceArray.add(cardMissingType);
            root.add("chanceCards", chanceArray);

            when(JsonUtils.read(any(Reader.class))).thenReturn(root);
            CardJsonReaderWriter.read(new StringReader("..."));
            mockedLogger.verify(() -> Logger.warning(eq("Skipping Chance card with id 99 due to missing 'type' field: {\"id\":99,\"description\":\"DescA\"}")));
        }
    }

    @Test
    void readCardArray_cardMissingTypeAndId_logsCorrectWarningAndSkips() {
        try (MockedStatic<JsonUtils> mockedJsonUtils = Mockito.mockStatic(JsonUtils.class);
             MockedStatic<Logger> mockedLogger = Mockito.mockStatic(Logger.class)) {
            JsonObject root = new JsonObject();
            JsonArray chanceArray = new JsonArray();
            JsonObject cardMissingBoth = new JsonObject();
            cardMissingBoth.addProperty("description", "DescA");
            chanceArray.add(cardMissingBoth);
            root.add("chanceCards", chanceArray);

            when(JsonUtils.read(any(Reader.class))).thenReturn(root);
            Map<String, List<Card>> decks = CardJsonReaderWriter.read(new StringReader("..."));

            assertTrue(decks.get("chance").isEmpty());

            mockedLogger.verify(() -> Logger.warning(eq("Skipping Chance card due to missing 'id' field: {\"description\":\"DescA\"}")));
            mockedLogger.verify(() -> Logger.warning(contains("card with id UNKNOWN due to missing 'type' field")), never());
        }
    }


    @Test
    void readCardArray_cardMissingDescription_skipsCardAndLogs() {
        try (MockedStatic<JsonUtils> mockedJsonUtils = Mockito.mockStatic(JsonUtils.class);
             MockedStatic<Logger> mockedLogger = Mockito.mockStatic(Logger.class)) {
            JsonObject root = new JsonObject();
            JsonArray chanceArray = new JsonArray();
            JsonObject cardMissingDesc = new JsonObject();
            cardMissingDesc.addProperty("id", 1);
            cardMissingDesc.addProperty("type", "TypeA");
            chanceArray.add(cardMissingDesc);
            root.add("chanceCards", chanceArray);

            when(JsonUtils.read(any(Reader.class))).thenReturn(root);
            Map<String, List<Card>> decks = CardJsonReaderWriter.read(new StringReader("..."));
            assertTrue(decks.get("chance").isEmpty());
            mockedLogger.verify(() -> Logger.warning(eq("Skipping Chance card with id 1 due to missing 'description' field: {\"id\":1,\"type\":\"TypeA\"}")));
        }
    }

    @Test
    void readCardArray_cardIdNotInt_parsingError_skipsCardAndLogs() {
        try (MockedStatic<JsonUtils> mockedJsonUtils = Mockito.mockStatic(JsonUtils.class);
             MockedStatic<Logger> mockedLogger = Mockito.mockStatic(Logger.class)) {
            JsonObject root = new JsonObject();
            JsonArray chanceArray = new JsonArray();
            JsonObject cardInvalidId = new JsonObject();
            cardInvalidId.addProperty("id", "not-an-int");
            cardInvalidId.addProperty("type", "TypeA");
            cardInvalidId.addProperty("description", "DescA");
            chanceArray.add(cardInvalidId);
            root.add("chanceCards", chanceArray);

            when(JsonUtils.read(any(Reader.class))).thenReturn(root);
            Map<String, List<Card>> decks = CardJsonReaderWriter.read(new StringReader("..."));
            assertTrue(decks.get("chance").isEmpty());
            // Verify the call with specific string and the correct exception type for the second argument
            mockedLogger.verify(() -> Logger.error(
                    eq("Error parsing a Chance card. JSON content: {\"id\":\"not-an-int\",\"type\":\"TypeA\",\"description\":\"DescA\"}. Skipping this card."),
                    isA(java.lang.NumberFormatException.class) // Corrected Exception Type
            ));
        }
    }

    @Test
    void readCardArray_cardTypeNotString_parsingError_skipsCardAndLogs() {
        try (MockedStatic<JsonUtils> mockedJsonUtils = Mockito.mockStatic(JsonUtils.class);
             MockedStatic<Logger> mockedLogger = Mockito.mockStatic(Logger.class)) {
            JsonObject root = new JsonObject();
            JsonArray chanceArray = new JsonArray();
            JsonObject cardInvalidType = new JsonObject();
            cardInvalidType.addProperty("id", 1);
            cardInvalidType.add("type", new JsonArray());
            cardInvalidType.addProperty("description", "DescA");
            chanceArray.add(cardInvalidType);
            root.add("chanceCards", chanceArray);

            when(JsonUtils.read(any(Reader.class))).thenReturn(root);
            Map<String, List<Card>> decks = CardJsonReaderWriter.read(new StringReader("..."));
            assertTrue(decks.get("chance").isEmpty());
            // Verify the call with specific string and any matching exception type for the second argument
            mockedLogger.verify(() -> Logger.error(
                    eq("Error parsing a Chance card. JSON content: {\"id\":1,\"type\":[],\"description\":\"DescA\"}. Skipping this card."),
                    isA(java.lang.IllegalStateException.class) // GSON throws this if getAsString is called on a non-primitive/non-string
            ));
        }
    }
}