package edu.ntnu.idi.bidata.service;

import edu.ntnu.idi.bidata.model.Card;
import edu.ntnu.idi.bidata.ui.monopoly.MonopolyGameScene;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Manages decks of cards for a game.
 * Allows drawing cards from named decks and shuffles them upon initialization.
 * This service also interacts directly with the {@link MonopolyGameScene} to display drawn cards.
 */
public class CardService {
    private final Map<String, List<Card>> decks;
    private final Map<String, Integer> currentIndexes = new HashMap<>();

    /**
     * Constructs a CardService with a set of named card decks.
     * Each deck is shuffled upon creation of the service.
     *
     * @param decks A map where keys are deck names (String) and values are lists of Cards (List&lt;Card&gt;).
     *              The provided lists will be shuffled.
     */
    public CardService(Map<String, List<Card>> decks) {
        this.decks = decks;

        // Initialize and shuffle decks
        for (Map.Entry<String, List<Card>> entry : decks.entrySet()) {
            Collections.shuffle(entry.getValue());
            currentIndexes.put(entry.getKey(), 0);
        }
    }

    /**
     * Draws a card from the specified deck.
     * Cards are drawn sequentially, and the deck wraps around when the end is reached.
     * After drawing a card, this method also triggers the UI to display the card image
     * via a direct call to {@link MonopolyGameScene#displayCardImage(Card)}.
     *
     * @param deckName The name of the deck to draw from.
     * @return The drawn {@link Card}.
     * @throws IllegalArgumentException if the deck name is not found or the deck is empty.
     */
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

        //Update the UI with the drawn card
        MonopolyGameScene monopolyGameScene = MonopolyGameScene.getInstance();
        monopolyGameScene.displayCardImage(card);

        return card;
    }
}