package edu.ntnu.idi.bidata.factory;

import edu.ntnu.idi.bidata.file.CardJsonReaderWriter;
import edu.ntnu.idi.bidata.model.Card;
import edu.ntnu.idi.bidata.service.CardService;
import edu.ntnu.idi.bidata.exception.JsonParseException;

import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Factory class for creating CardService instances.
 * This class provides static methods to load card data from JSON resources and initialize a CardService.
 * It is a final class with a private constructor to prevent instantiation.
 */
public final class CardFactory {
    /** 
     * Private constructor to prevent instantiation of this utility class.
     * The comment {@code /* non-instantiable *\/} is a common convention for this purpose.
     */
    private CardFactory() { /* non-instantiable */ }

    /**
     * Loads card decks from a specified JSON resource file on the classpath and creates a {@link CardService}.
     * This method uses {@link CardJsonReaderWriter} to parse the JSON file, which is expected to contain
     * a map of deck names to lists of {@link Card} objects. The resulting map is then used to
     * instantiate and return a new {@link CardService}.
     *
     * @param resourcePath The path to the JSON file within the classpath (e.g., "/data/cards/monopoly_cards.json").
     *                     This path should lead to a JSON file structured for card decks.
     * @return An initialized {@link CardService} containing the decks of cards loaded from the JSON file.
     * @throws JsonParseException If an error occurs during JSON parsing, if the resource cannot be found/read,
     *                            or if any other {@link Exception} occurs during the process. The original exception
     *                            is wrapped in the {@link JsonParseException}.
     */
    public static CardService createCardServiceFromJson(String resourcePath) {
        try (Reader reader = new InputStreamReader(
            Objects.requireNonNull(CardFactory.class.getResourceAsStream(resourcePath)),
                StandardCharsets.UTF_8)) {
            Map<String, List<Card>> decks = CardJsonReaderWriter.read(reader);
            return new CardService(decks);
        } catch (Exception e) {
            throw new JsonParseException("Failed to load cards from JSON: " + resourcePath, e);
        }
    }
}