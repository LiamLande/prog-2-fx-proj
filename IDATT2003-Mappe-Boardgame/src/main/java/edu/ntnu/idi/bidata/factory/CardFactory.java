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

public final class CardFactory {
    private CardFactory() { /* non-instantiable */ }

    /**
     * Loads cards from a JSON resource and creates a CardService.
     * @param resourcePath path to .json file (e.g. "/data/cards/cards.json")
     * @return initialized CardService
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