package edu.ntnu.idi.bidata.service;

import edu.ntnu.idi.bidata.model.Card;
import edu.ntnu.idi.bidata.model.Player;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CardService {
    private final Map<String, List<Card>> decks;
    private final Map<String, Integer> currentIndexes = new HashMap<>();

    public CardService(Map<String, List<Card>> decks) {
        this.decks = decks;

        // Initialize and shuffle decks
        for (Map.Entry<String, List<Card>> entry : decks.entrySet()) {
            Collections.shuffle(entry.getValue());
            currentIndexes.put(entry.getKey(), 0);
        }
    }

    public Card drawCard(String deckName) {
        List<Card> deck = decks.get(deckName);
        if (deck == null || deck.isEmpty()) {
            throw new IllegalArgumentException("Deck not found: " + deckName);
        }

        int index = currentIndexes.getOrDefault(deckName, 0);
        Card card = deck.get(index);

        // Move to next card or wrap around
        index = (index + 1) % deck.size();
        currentIndexes.put(deckName, index);

        return card;
    }

    // Additional methods for card execution would go here
}