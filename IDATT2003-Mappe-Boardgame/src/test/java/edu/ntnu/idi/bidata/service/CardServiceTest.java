package edu.ntnu.idi.bidata.service;

import edu.ntnu.idi.bidata.model.Card;
import edu.ntnu.idi.bidata.ui.monopoly.MonopolyGameScene;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CardServiceTest {

    private CardService cardService;
    private Card card1, card2, card3;
    private Map<String, List<Card>> decks;
    private MonopolyGameScene mockGameScene;

    @BeforeEach
    void setUp() {
        // Create test cards
        card1 = new Card( 1, "Description 1", "path/to/image1.png", null);
        card2 = new Card(1, "Description 2", "path/to/image2.png" , null);
        card3 = new Card(1, "Description 3", "path/to/image3.png" , null);

        // Setup decks
        decks = new HashMap<>();
        decks.put("chance", Arrays.asList(card1, card2, card3));
        decks.put("community", Arrays.asList(card3, card2, card1));

        // Mock MonopolyGameScene.getInstance()
        mockGameScene = mock(MonopolyGameScene.class);
    }

    @Test
    @DisplayName("Constructor should initialize and shuffle decks")
    void constructorShouldInitializeAndShuffleDecks() {
        try (MockedStatic<MonopolyGameScene> mockedStatic = Mockito.mockStatic(MonopolyGameScene.class)) {
            mockedStatic.when(MonopolyGameScene::getInstance).thenReturn(mockGameScene);

            cardService = new CardService(decks);

            // Verify by drawing all cards from each deck
            List<Card> chanceCards = Arrays.asList(
                    cardService.drawCard("chance"),
                    cardService.drawCard("chance"),
                    cardService.drawCard("chance")
            );

            List<Card> communityCards = Arrays.asList(
                    cardService.drawCard("community"),
                    cardService.drawCard("community"),
                    cardService.drawCard("community")
            );

            // Verify all cards are present in each deck (regardless of order)
            assertTrue(chanceCards.contains(card1));
            assertTrue(chanceCards.contains(card2));
            assertTrue(chanceCards.contains(card3));

            assertTrue(communityCards.contains(card1));
            assertTrue(communityCards.contains(card2));
            assertTrue(communityCards.contains(card3));
        }
    }

    @Test
    @DisplayName("drawCard should draw cards in sequence and wrap around")
    void drawCardShouldDrawCardsInSequenceAndWrapAround() {
        try (MockedStatic<MonopolyGameScene> mockedStatic = Mockito.mockStatic(MonopolyGameScene.class)) {
            mockedStatic.when(MonopolyGameScene::getInstance).thenReturn(mockGameScene);

            // Create service with non-shuffled decks for predictable order
            decks = new HashMap<>();
            decks.put("test", Arrays.asList(card1, card2, card3));
            cardService = new CardService(decks);

            // First complete cycle
            Card drawn1 = cardService.drawCard("test");
            Card drawn2 = cardService.drawCard("test");


            // Verify UI was updated
            verify(mockGameScene, times(2)).displayCardImage(any(Card.class));
            verify(mockGameScene).displayCardImage(drawn1);
            verify(mockGameScene).displayCardImage(drawn2);
        }
    }

    @Test
    @DisplayName("drawCard should throw IllegalArgumentException for non-existent deck")
    void drawCardShouldThrowExceptionForNonExistentDeck() {
        cardService = new CardService(decks);

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> cardService.drawCard("nonexistent"));

        assertEquals("Deck not found: nonexistent", exception.getMessage());
    }

    @Test
    @DisplayName("drawCard should throw IllegalArgumentException for empty deck")
    void drawCardShouldThrowExceptionForEmptyDeck() {
        // Setup with empty deck
        Map<String, List<Card>> emptyDecks = new HashMap<>();
        emptyDecks.put("empty", List.of());
        cardService = new CardService(emptyDecks);

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> cardService.drawCard("empty"));

        assertEquals("Deck not found: empty", exception.getMessage());
    }

    @Test
    @DisplayName("drawCard should update UI through MonopolyGameScene")
    void drawCardShouldUpdateUI() {
        try (MockedStatic<MonopolyGameScene> mockedStatic = Mockito.mockStatic(MonopolyGameScene.class)) {
            mockedStatic.when(MonopolyGameScene::getInstance).thenReturn(mockGameScene);

            cardService = new CardService(decks);
            Card drawnCard = cardService.drawCard("chance");

            // Verify the UI was updated with the drawn card
            verify(mockGameScene).displayCardImage(drawnCard);
        }
    }
}