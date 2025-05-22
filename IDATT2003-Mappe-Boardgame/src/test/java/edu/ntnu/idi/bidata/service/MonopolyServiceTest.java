package edu.ntnu.idi.bidata.service;

import edu.ntnu.idi.bidata.exception.InvalidParameterException;
import edu.ntnu.idi.bidata.model.Board;
import edu.ntnu.idi.bidata.model.BoardGame;
import edu.ntnu.idi.bidata.model.Card;
import edu.ntnu.idi.bidata.model.Dice;
import edu.ntnu.idi.bidata.model.Player;
import edu.ntnu.idi.bidata.model.Tile;
import edu.ntnu.idi.bidata.model.actions.TileAction;
import edu.ntnu.idi.bidata.model.actions.monopoly.PropertyAction;
import edu.ntnu.idi.bidata.model.actions.monopoly.RailroadAction;
import edu.ntnu.idi.bidata.model.actions.monopoly.UtilityAction;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MonopolyServiceTest {

    @Mock
    BoardGame game;
    @Mock
    Player player1;
    @Mock
    Player player2;
    @Mock
    Board board;
    @Mock
    Tile tile0; // Represents the "Go" tile or starting tile
    @Mock
    Dice dice;
    @Mock
    CardService cardService;
    @Mock
    Card card;

    @InjectMocks
    MonopolyService monopolyService;

    private List<Player> players;
    private final PrintStream originalSystemOut = System.out;
    private final PrintStream originalSystemErr = System.err;
    private ByteArrayOutputStream systemOutContent;
    private ByteArrayOutputStream systemErrContent;


    @BeforeEach
    void setUp() {
        players = new ArrayList<>();
        lenient().when(game.getPlayers()).thenReturn(players);
        lenient().when(game.getBoard()).thenReturn(board);
        lenient().when(board.getTile(0)).thenReturn(tile0);
        lenient().when(game.getDice()).thenReturn(dice);

        // Capture System.out and System.err
        systemOutContent = new ByteArrayOutputStream();
        System.setOut(new PrintStream(systemOutContent));
        systemErrContent = new ByteArrayOutputStream();
        System.setErr(new PrintStream(systemErrContent));
    }

    @AfterEach
    void tearDown() {
        System.setOut(originalSystemOut);
        System.setErr(originalSystemErr);

        if (monopolyService != null && game != null) {
            // Ensure players list isn't null if game.getPlayers() is called in setup
            if (game.getPlayers() == null) {
                lenient().when(game.getPlayers()).thenReturn(new ArrayList<>());
            }
            monopolyService.setup(game); // Resets internal state like jailedPlayers, playerProperties
        }
    }


    @Test
    void setup_nullGame_throwsInvalidParameterException() {
        Exception exception = assertThrows(InvalidParameterException.class, () -> {
            monopolyService.setup(null);
        });
        assertEquals("Game cannot be null in MonopolyService.setup", exception.getMessage());
    }

    @Test
    void setup_validGame_emptyPlayers() {

        monopolyService.setup(game); // Pass the mocked game
        assertNull(monopolyService.getCurrentPlayer(this.game)); // Use the field 'game' which is the one setup uses
    }

    @Test
    void setup_validGame_withPlayers() {
        players.add(player1);
        players.add(player2);

        monopolyService.setup(game);

        verify(player1).setMoney(1500);
        verify(player1).setCurrentTile(tile0);
        verify(player2).setMoney(1500);
        verify(player2).setCurrentTile(tile0);
        assertEquals(player1, monopolyService.getCurrentPlayer(this.game));
    }

    @Test
    void getCurrentPlayer_noPlayers_returnsNull() {
        monopolyService.setup(game); // game has empty players list
        assertNull(monopolyService.getCurrentPlayer(this.game));
    }

    @Test
    void getCurrentPlayer_validIndex_returnsPlayer() {
        players.add(player1);
        monopolyService.setup(game); // Sets currentPlayerIndex to 0
        assertEquals(player1, monopolyService.getCurrentPlayer(this.game));
    }

    @Test
    void playTurn_playerNotInGame_throwsIllegalArgumentException() {
        players.add(player1);
        monopolyService.setup(game); // player1 is current

        Player playerNotInGame = mock(Player.class);
        when(playerNotInGame.getName()).thenReturn("Ghost");

        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            monopolyService.playTurn(game, playerNotInGame);
        });
        assertTrue(exception.getMessage().contains("is not in the game or not their turn"));
    }

    @Test
    void playTurn_emptyPlayerList_throwsException() {
        monopolyService.setup(game); // 'players' is empty

        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            // player1 is a mock but not in the game's player list which is empty
            monopolyService.playTurn(game, player1);
        });
        assertTrue(exception.getMessage().contains("is not in the game or not their turn"));
    }

    @Test
    void playTurn_wrongPlayerButInGame_syncsAndPlays() {
        players.add(player1);
        players.add(player2);
        monopolyService.setup(game); // player1 is current by default (index 0)

        when(dice.rollDie()).thenReturn(5);
        lenient().when(player2.getName()).thenReturn("Player2"); // For logging

        monopolyService.playTurn(game, player2); // Play turn for player2

        verify(player2).move(5);
        assertEquals(player1, monopolyService.getCurrentPlayer(this.game)); // Next player should be player1
    }

    @Test
    void playTurn_normalTurn_playerMoves_advancesTurn() {
        players.add(player1);
        players.add(player2);
        monopolyService.setup(game); // player1 is current

        when(dice.rollDie()).thenReturn(7);
        lenient().when(player1.getName()).thenReturn("Player1");

        int roll = monopolyService.playTurn(game, player1);

        assertEquals(7, roll);
        verify(player1).move(7);
        assertEquals(player2, monopolyService.getCurrentPlayer(this.game)); // Next player
    }

    @Test
    void playTurn_playerInJail_hasGetOutOfJailFreeCard() {
        players.add(player1);
        monopolyService.setup(game);

        monopolyService.sendToJail(player1);
        monopolyService.giveGetOutOfJailFreeCard(player1);

        when(dice.rollDie()).thenReturn(4);
        lenient().when(player1.getName()).thenReturn("Player1");

        int roll = monopolyService.playTurn(game, player1);

        assertEquals(4, roll);
        verify(player1).move(4);
        assertFalse(monopolyService.isInJail(player1));
        assertFalse(monopolyService.hasGetOutOfJailFreeCard(player1));
    }

    @Test
    void playTurn_playerInJail_noCard_handlesJailTurn_stillInJail() {
        players.add(player1);
        players.add(player2); // To check next player
        monopolyService.setup(game);

        monopolyService.sendToJail(player1); // Puts player1 in jail for 3 turns

        lenient().when(player1.getName()).thenReturn("Player1");

        int roll = monopolyService.playTurn(game, player1);

        assertEquals(0, roll); // Did not move
        verify(player1, never()).move(anyInt());
        assertTrue(monopolyService.isInJail(player1)); // Still in jail, turns decremented
        assertEquals(player2, monopolyService.getCurrentPlayer(this.game)); // Turn advanced
    }

    @Test
    void playTurn_playerInJail_noCard_lastJailTurn_getsOutAndMoves() {
        players.add(player1);
        monopolyService.setup(game);

        monopolyService.sendToJail(player1); // 3 turns
        monopolyService.handleJailTurn(player1); // 2 turns left
        monopolyService.handleJailTurn(player1); // 1 turn left

        when(dice.rollDie()).thenReturn(6);
        lenient().when(player1.getName()).thenReturn("Player1");

        // playTurn will call handleJailTurn one more time internally before deciding if it can move
        int roll = monopolyService.playTurn(game, player1);

        assertEquals(6, roll);
        verify(player1).move(6);
        assertFalse(monopolyService.isInJail(player1));
    }

    @Test
    void isFinished_onePlayerSolvent_true() {
        players.add(player1);
        players.add(player2);
        when(player1.getMoney()).thenReturn(100);
        when(player2.getMoney()).thenReturn(0); // Bankrupt

        assertTrue(monopolyService.isFinished(game));
    }

    @Test
    void isFinished_noPlayersSolvent_true() {
        players.add(player1);
        players.add(player2);
        when(player1.getMoney()).thenReturn(0);
        when(player2.getMoney()).thenReturn(0);

        assertTrue(monopolyService.isFinished(game));
    }

    @Test
    void isFinished_multiplePlayersSolvent_false() {
        players.add(player1);
        players.add(player2);
        when(player1.getMoney()).thenReturn(100);
        when(player2.getMoney()).thenReturn(100);

        assertFalse(monopolyService.isFinished(game));
    }

    @Test
    void isFinished_noPlayers_true() {
        // 'players' list is empty
        assertTrue(monopolyService.isFinished(game));
    }

    @Test
    void getWinner_oneSolventPlayer_returnsPlayer() {
        players.add(player1);
        players.add(player2);
        when(player1.getMoney()).thenReturn(0);
        when(player2.getMoney()).thenReturn(500);

        assertEquals(player2, monopolyService.getWinner(game));
    }

    @Test
    void getWinner_noSolventPlayer_returnsNull() {
        players.add(player1);
        players.add(player2);
        when(player1.getMoney()).thenReturn(0);
        when(player2.getMoney()).thenReturn(0);

        assertNull(monopolyService.getWinner(game));
    }

    @Test
    void getWinner_multipleSolventPlayers_returnsFirstFound() {
        players.add(player1);
        players.add(player2);
        when(player1.getMoney()).thenReturn(100);
        lenient().when(player2.getMoney()).thenReturn(500); // Made lenient

        assertEquals(player1, monopolyService.getWinner(game));
    }

    @Test
    void getWinner_noPlayers_returnsNull() {
        // 'players' is empty
        assertNull(monopolyService.getWinner(game));
    }

    @Test
    void sendToJail_putsPlayerInJail() {
        monopolyService.sendToJail(player1);
        assertTrue(monopolyService.isInJail(player1));
    }

    @Test
    void isInJail_playerIsIn_true() {
        monopolyService.sendToJail(player1);
        assertTrue(monopolyService.isInJail(player1));
    }

    @Test
    void isInJail_playerIsNotIn_false() {
        assertFalse(monopolyService.isInJail(player1));
    }

    @Test
    void handleJailTurn_playerNotInJail_doesNothing() {
        monopolyService.handleJailTurn(player1);
        assertFalse(monopolyService.isInJail(player1));
    }

    @Test
    void handleJailTurn_playerInJail_decrementsTurns() {
        monopolyService.sendToJail(player1); // 3 turns
        monopolyService.handleJailTurn(player1); // 2 turns left
        assertTrue(monopolyService.isInJail(player1));
    }

    @Test
    void handleJailTurn_playerInJail_lastTurn_releasesFromJail() {
        monopolyService.sendToJail(player1); // 3 turns
        monopolyService.handleJailTurn(player1); // 2 turns
        monopolyService.handleJailTurn(player1); // 1 turn
        monopolyService.handleJailTurn(player1); // 0 turns -> released
        assertFalse(monopolyService.isInJail(player1));
    }

    @Test
    void payRent_nullPayer_throwsException() {
        assertThrows(InvalidParameterException.class, () -> monopolyService.payRent(null, player2, 100));
    }

    @Test
    void payRent_nullOwner_throwsException() {
        assertThrows(InvalidParameterException.class, () -> monopolyService.payRent(player1, null, 100));
    }

    @Test
    void payRent_negativeAmount_throwsException() {
        assertThrows(InvalidParameterException.class, () -> monopolyService.payRent(player1, player2, -100));
    }

    @Test
    void payRent_successful() {
        when(player1.getMoney()).thenReturn(200);
        lenient().when(player1.getName()).thenReturn("Payer");
        lenient().when(player2.getName()).thenReturn("Owner");

        assertTrue(monopolyService.payRent(player1, player2, 100));
        verify(player1).decreaseMoney(100);
        verify(player2).increaseMoney(100);
    }

    @Test
    void payRent_payerCannotAfford() {
        when(player1.getMoney()).thenReturn(50); // Not enough
        lenient().when(player1.getName()).thenReturn("Payer");
        lenient().when(player2.getName()).thenReturn("Owner");

        assertFalse(monopolyService.payRent(player1, player2, 100));

        assertTrue(systemOutContent.toString().contains("Payer cannot afford to pay $100 rent."));
        verify(player1).decreaseMoney(50); // Pays all they have
        verify(player2, never()).increaseMoney(anyInt()); // Owner doesn't get full amount
    }

    @Test
    void payRent_decreaseMoneyThrowsException_returnsFalse() throws InvalidParameterException {
        when(player1.getMoney()).thenReturn(200);
        lenient().when(player1.getName()).thenReturn("Payer");
        doThrow(new InvalidParameterException("Test Exception")).when(player1).decreaseMoney(100);

        assertFalse(monopolyService.payRent(player1, player2, 100));

        assertTrue(systemErrContent.toString().contains("Error during rent payment (unexpected): Test Exception"));
        verify(player2, never()).increaseMoney(anyInt());
    }

    @Test
    void purchaseProperty_nullPlayer_throwsException() {
        PropertyAction pa = mock(PropertyAction.class);
        assertThrows(InvalidParameterException.class, () -> monopolyService.purchaseProperty(null, pa));
    }

    @Test
    void purchaseProperty_nullProperty_throwsException() {
        assertThrows(InvalidParameterException.class, () -> monopolyService.purchaseProperty(player1, null));
    }

    @Test
    void purchaseProperty_alreadyOwned_returnsFalse() {
        PropertyAction pa = mock(PropertyAction.class);
        when(pa.getOwner()).thenReturn(player2); // Owned by someone else
        lenient().when(pa.getName()).thenReturn("Boardwalk");

        assertFalse(monopolyService.purchaseProperty(player1, pa));
        assertTrue(systemErrContent.toString().contains("Attempt to purchase already owned property: Boardwalk"));
    }

    @Test
    void purchaseProperty_successful() {
        PropertyAction pa = mock(PropertyAction.class);
        when(player1.getMoney()).thenReturn(500);
        lenient().when(player1.getName()).thenReturn("Buyer");
        when(pa.getCost()).thenReturn(400);
        when(pa.getOwner()).thenReturn(null); // Not owned
        lenient().when(pa.getName()).thenReturn("Boardwalk");

        monopolyService.setup(game); // Ensure playerProperties map is initialized

        assertTrue(monopolyService.purchaseProperty(player1, pa));
        verify(player1).decreaseMoney(400);
        verify(pa).setOwner(player1);
        assertEquals(0, monopolyService.getRailroadsOwnedCount(player1));
    }

    @Test
    void purchaseProperty_cannotAfford() {
        PropertyAction pa = mock(PropertyAction.class);
        when(player1.getMoney()).thenReturn(300);
        lenient().when(player1.getName()).thenReturn("Buyer");
        when(pa.getCost()).thenReturn(400);
        when(pa.getOwner()).thenReturn(null);
        lenient().when(pa.getName()).thenReturn("Boardwalk");

        assertFalse(monopolyService.purchaseProperty(player1, pa));
        verify(player1, never()).decreaseMoney(anyInt());
        verify(pa, never()).setOwner(any(Player.class));
        assertTrue(systemOutContent.toString().contains("Buyer cannot afford to purchase Boardwalk"));
    }

    @Test
    void purchaseProperty_decreaseMoneyThrowsException_returnsFalse() throws InvalidParameterException {
        PropertyAction pa = mock(PropertyAction.class);
        when(player1.getMoney()).thenReturn(500);
        lenient().when(player1.getName()).thenReturn("Buyer");
        when(pa.getCost()).thenReturn(400);
        when(pa.getOwner()).thenReturn(null);
        lenient().when(pa.getName()).thenReturn("Boardwalk");
        doThrow(new InvalidParameterException("Test Exception")).when(player1).decreaseMoney(400);

        assertFalse(monopolyService.purchaseProperty(player1, pa));

        assertTrue(systemErrContent.toString().contains("Error during property purchase (unexpected): Test Exception"));
        verify(pa, never()).setOwner(player1);
    }

    @Test
    void addProperty_addsToNewPlayerAndExistingPlayer() {
        monopolyService.setup(game); // Ensures playerProperties is clear

        PropertyAction prop1 = mock(PropertyAction.class);
        PropertyAction prop2 = mock(PropertyAction.class);

        monopolyService.addProperty(player1, prop1);
        assertEquals(0, monopolyService.getUtilitiesOwnedCount(player1));

        monopolyService.addProperty(player1, prop2);
        assertEquals(0, monopolyService.getUtilitiesOwnedCount(player1));
    }

    @Test
    void getRailroadsOwnedCount_noProperties() {
        monopolyService.setup(game);
        assertEquals(0, monopolyService.getRailroadsOwnedCount(player1));
    }

    @Test
    void getRailroadsOwnedCount_hasNonRailroadProperties() {
        monopolyService.setup(game);
        PropertyAction pa = mock(PropertyAction.class);
        monopolyService.addProperty(player1, pa);
        assertEquals(0, monopolyService.getRailroadsOwnedCount(player1));
    }

    @Test
    void getRailroadsOwnedCount_hasRailroads() {
        monopolyService.setup(game);
        RailroadAction railroad1 = mock(RailroadAction.class);
        RailroadAction railroad2 = mock(RailroadAction.class);
        PropertyAction pa = mock(PropertyAction.class);
        monopolyService.addProperty(player1, railroad1);
        monopolyService.addProperty(player1, pa);
        monopolyService.addProperty(player1, railroad2);
        assertEquals(2, monopolyService.getRailroadsOwnedCount(player1));
    }

    @Test
    void getUtilitiesOwnedCount_noProperties() {
        monopolyService.setup(game);
        assertEquals(0, monopolyService.getUtilitiesOwnedCount(player1));
    }

    @Test
    void getUtilitiesOwnedCount_hasNonUtilityProperties() {
        monopolyService.setup(game);
        PropertyAction pa = mock(PropertyAction.class);
        monopolyService.addProperty(player1, pa);
        assertEquals(0, monopolyService.getUtilitiesOwnedCount(player1));
    }

    @Test
    void getUtilitiesOwnedCount_hasUtilities() {
        monopolyService.setup(game);
        UtilityAction utility1 = mock(UtilityAction.class);
        UtilityAction utility2 = mock(UtilityAction.class);
        PropertyAction pa = mock(PropertyAction.class);
        monopolyService.addProperty(player1, utility1);
        monopolyService.addProperty(player1, pa);
        monopolyService.addProperty(player1, utility2);
        assertEquals(2, monopolyService.getUtilitiesOwnedCount(player1));
    }

    @Test
    void setCardService_thenDrawCard() {
        monopolyService.setCardService(cardService);
        players.add(player1);
        monopolyService.setup(game);
        lenient().when(player1.getName()).thenReturn("TestPlayer");
        lenient().when(card.getDescription()).thenReturn("Dummy Card");

        when(cardService.drawCard(anyString())).thenReturn(card);
        when(card.getType()).thenReturn("DummyTypeNotImplemented");

        // Clear invocations from setup before verifying card action specifics
        Mockito.clearInvocations(player1, board, tile0); // Clear mocks that setup interacts with

        monopolyService.drawChanceCard(player1);
        verify(cardService).drawCard("chance");
        assertTrue(systemOutContent.toString().contains("INFO] TestPlayer drew: Dummy Card\n"));
    }

    @Test
    void drawChanceCard_callsExecuteCardAction_AdvanceToGo() {
        monopolyService.setCardService(cardService);
        players.add(player1);
        monopolyService.setup(game);
        lenient().when(player1.getName()).thenReturn("TestPlayer");
        lenient().when(card.getDescription()).thenReturn("Advance to Go");

        when(cardService.drawCard("chance")).thenReturn(card);
        when(card.getType()).thenReturn("AdvanceToGo");

        // Clear invocations from setup phase that might interfere with verifications below
        Mockito.clearInvocations(player1);

        Card drawnCard = monopolyService.drawChanceCard(player1);
        assertEquals(card, drawnCard);
        verify(player1).setCurrentTile(tile0);
        verify(player1).increaseMoney(200);
    }

    @Test
    void drawCommunityChestCard_callsExecuteCardAction_GoToJail() {
        monopolyService.setCardService(cardService);
        players.add(player1);
        monopolyService.setup(game);
        lenient().when(player1.getName()).thenReturn("TestPlayer");
        lenient().when(card.getDescription()).thenReturn("Go to Jail");

        when(cardService.drawCard("communityChest")).thenReturn(card);
        when(card.getType()).thenReturn("GoToJail");

        Mockito.clearInvocations(player1);

        Card drawnCard = monopolyService.drawCommunityChestCard(player1);
        assertEquals(card, drawnCard);
        assertTrue(monopolyService.isInJail(player1));
    }

    @Test
    void giveGetOutOfJailFreeCard_firstAndMultiple() {
        monopolyService.giveGetOutOfJailFreeCard(player1);
        assertTrue(monopolyService.hasGetOutOfJailFreeCard(player1));
        monopolyService.giveGetOutOfJailFreeCard(player1);
        assertTrue(monopolyService.hasGetOutOfJailFreeCard(player1));
    }

    @Test
    void hasGetOutOfJailFreeCard_false() {
        assertFalse(monopolyService.hasGetOutOfJailFreeCard(player1));
    }

    private void setupForCardExecutionTest() {
        monopolyService.setCardService(cardService);

        players.clear();
        players.add(player1);
        players.add(player2);

        monopolyService.setup(game);

        lenient().when(player1.getName()).thenReturn("Player1");
        lenient().when(player2.getName()).thenReturn("Player2");
        lenient().when(card.getDescription()).thenReturn("Test Card Description");
        lenient().when(cardService.drawCard(anyString())).thenReturn(card);
    }

    @Test
    void executeCardAction_AdvanceToIllinoisAve_found() {
        setupForCardExecutionTest();
        when(card.getType()).thenReturn("AdvanceToIllinoisAve");

        Map<Integer, Tile> tilesMap = new HashMap<>();
        Tile illinoisTile = mock(Tile.class);
        PropertyAction illinoisProp = mock(PropertyAction.class);
        when(illinoisProp.getName()).thenReturn("Illinois Avenue");
        when(illinoisTile.getAction()).thenReturn(illinoisProp);
        tilesMap.put(24, illinoisTile);

        when(board.getTiles()).thenReturn(tilesMap);
        Mockito.clearInvocations(player1); // Clear setup invocations

        monopolyService.drawChanceCard(player1);
        verify(player1).setCurrentTile(illinoisTile);
    }

    @Test
    void executeCardAction_AdvanceToIllinoisAve_notFound() {
        setupForCardExecutionTest();
        when(card.getType()).thenReturn("AdvanceToIllinoisAve");
        Map<Integer, Tile> tilesMap = new HashMap<>();
        Tile otherTile = mock(Tile.class);
        PropertyAction otherProp = mock(PropertyAction.class);
        when(otherProp.getName()).thenReturn("Some Other Place");
        when(otherTile.getAction()).thenReturn(otherProp);
        tilesMap.put(10, otherTile);
        when(board.getTiles()).thenReturn(tilesMap);
        Mockito.clearInvocations(player1);

        monopolyService.drawChanceCard(player1);
        verify(player1, never()).setCurrentTile(any(Tile.class));
    }

    @Test
    void executeCardAction_AdvanceToIllinoisAve_tileNotProperty() {
        setupForCardExecutionTest();
        when(card.getType()).thenReturn("AdvanceToIllinoisAve");
        Map<Integer, Tile> tilesMap = new HashMap<>();
        Tile nonPropertyTile = mock(Tile.class);
        TileAction nonPropertyAction = mock(TileAction.class);
        when(nonPropertyTile.getAction()).thenReturn(nonPropertyAction);
        tilesMap.put(24, nonPropertyTile);
        when(board.getTiles()).thenReturn(tilesMap);
        Mockito.clearInvocations(player1);

        monopolyService.drawChanceCard(player1);
        verify(player1, never()).setCurrentTile(any(Tile.class));
    }

    @Test
    void executeCardAction_AdvanceToIllinoisAve_emptyTileMap() {
        setupForCardExecutionTest();
        when(card.getType()).thenReturn("AdvanceToIllinoisAve");
        when(board.getTiles()).thenReturn(Collections.emptyMap());
        Mockito.clearInvocations(player1);

        monopolyService.drawChanceCard(player1);
        verify(player1, never()).setCurrentTile(any(Tile.class));
    }

    @Test
    void executeCardAction_GoBack() {
        setupForCardExecutionTest();
        when(card.getType()).thenReturn("GoBack");
        when(card.getIntProperty("spaces", 3)).thenReturn(3);
        Mockito.clearInvocations(player1);

        monopolyService.drawChanceCard(player1);
        verify(player1).move(-3);
    }

    @Test
    void executeCardAction_GetOutOfJailFree() {
        setupForCardExecutionTest();
        when(card.getType()).thenReturn("GetOutOfJailFree");
        Mockito.clearInvocations(player1);

        monopolyService.drawChanceCard(player1);
        assertTrue(monopolyService.hasGetOutOfJailFreeCard(player1));
    }

    @Test
    void executeCardAction_PayTax_SufficientFunds() {
        setupForCardExecutionTest();
        when(card.getType()).thenReturn("PayTax");
        when(card.getIntProperty("amount", 0)).thenReturn(50);
        when(player1.getMoney()).thenReturn(100);
        Mockito.clearInvocations(player1);
        monopolyService.drawChanceCard(player1);
        verify(player1).decreaseMoney(50);
    }
    @Test
    void executeCardAction_PayPoorTax_SufficientFunds() {
        setupForCardExecutionTest();
        when(card.getType()).thenReturn("PayPoorTax");
        when(card.getIntProperty("amount", 0)).thenReturn(15);
        when(player1.getMoney()).thenReturn(100);
        Mockito.clearInvocations(player1);
        monopolyService.drawChanceCard(player1);
        verify(player1).decreaseMoney(15);
    }
    @Test
    void executeCardAction_HospitalFees_SufficientFunds() {
        setupForCardExecutionTest();
        when(card.getType()).thenReturn("HospitalFees");
        when(card.getIntProperty("amount", 0)).thenReturn(100);
        when(player1.getMoney()).thenReturn(150);
        Mockito.clearInvocations(player1);
        monopolyService.drawChanceCard(player1);
        verify(player1).decreaseMoney(100);
    }
    @Test
    void executeCardAction_SchoolFees_SufficientFunds() {
        setupForCardExecutionTest();
        when(card.getType()).thenReturn("SchoolFees");
        when(card.getIntProperty("amount", 0)).thenReturn(150);
        when(player1.getMoney()).thenReturn(200);
        Mockito.clearInvocations(player1);
        monopolyService.drawChanceCard(player1);
        verify(player1).decreaseMoney(150);
    }
    @Test
    void executeCardAction_DoctorFees_SufficientFunds() {
        setupForCardExecutionTest();
        when(card.getType()).thenReturn("DoctorFees");
        when(card.getIntProperty("amount", 0)).thenReturn(50);
        when(player1.getMoney()).thenReturn(100);
        Mockito.clearInvocations(player1);
        monopolyService.drawChanceCard(player1);
        verify(player1).decreaseMoney(50);
    }


    @Test
    void executeCardAction_Pay_InsufficientFunds() {
        setupForCardExecutionTest();
        when(card.getType()).thenReturn("SchoolFees");
        when(card.getIntProperty("amount", 0)).thenReturn(150);
        when(player1.getMoney()).thenReturn(100);
        Mockito.clearInvocations(player1);
        monopolyService.drawChanceCard(player1);
        verify(player1, never()).decreaseMoney(anyInt());
        assertTrue(systemOutContent.toString().contains(player1.getName() + " cannot afford to pay $150"));
    }

    @Test
    void executeCardAction_BankPaysYou() {
        setupForCardExecutionTest();
        when(card.getType()).thenReturn("BankPaysYou");
        when(card.getIntProperty("amount", 0)).thenReturn(100);
        Mockito.clearInvocations(player1);
        monopolyService.drawChanceCard(player1);
        verify(player1).increaseMoney(100);
    }
    @Test
    void executeCardAction_BankErrorInYourFavor() {
        setupForCardExecutionTest();
        when(card.getType()).thenReturn("BankErrorInYourFavor");
        when(card.getIntProperty("amount", 0)).thenReturn(200);
        Mockito.clearInvocations(player1);
        monopolyService.drawChanceCard(player1);
        verify(player1).increaseMoney(200);
    }
    @Test
    void executeCardAction_BuildingLoanMatures() {
        setupForCardExecutionTest();
        when(card.getType()).thenReturn("BuildingLoanMatures");
        when(card.getIntProperty("amount", 0)).thenReturn(150);
        Mockito.clearInvocations(player1);
        monopolyService.drawChanceCard(player1);
        verify(player1).increaseMoney(150);
    }
    @Test
    void executeCardAction_CrosswordCompetition() {
        setupForCardExecutionTest();
        when(card.getType()).thenReturn("CrosswordCompetition");
        when(card.getIntProperty("amount", 0)).thenReturn(100);
        Mockito.clearInvocations(player1);
        monopolyService.drawChanceCard(player1);
        verify(player1).increaseMoney(100);
    }
    @Test
    void executeCardAction_SaleOfStock() {
        setupForCardExecutionTest();
        when(card.getType()).thenReturn("SaleOfStock");
        when(card.getIntProperty("amount", 0)).thenReturn(50);
        Mockito.clearInvocations(player1);
        monopolyService.drawChanceCard(player1);
        verify(player1).increaseMoney(50);
    }
    @Test
    void executeCardAction_HolidayFundMatures() {
        setupForCardExecutionTest();
        when(card.getType()).thenReturn("HolidayFundMatures");
        when(card.getIntProperty("amount", 0)).thenReturn(100);
        Mockito.clearInvocations(player1);
        monopolyService.drawChanceCard(player1);
        verify(player1).increaseMoney(100);
    }
    @Test
    void executeCardAction_IncomeTaxRefund() {
        setupForCardExecutionTest();
        when(card.getType()).thenReturn("IncomeTaxRefund");
        when(card.getIntProperty("amount", 0)).thenReturn(20);
        Mockito.clearInvocations(player1);
        monopolyService.drawChanceCard(player1);
        verify(player1).increaseMoney(20);
    }
    @Test
    void executeCardAction_LifeInsuranceMatures() {
        setupForCardExecutionTest();
        when(card.getType()).thenReturn("LifeInsuranceMatures");
        when(card.getIntProperty("amount", 0)).thenReturn(100);
        Mockito.clearInvocations(player1);
        monopolyService.drawChanceCard(player1);
        verify(player1).increaseMoney(100);
    }
    @Test
    void executeCardAction_ReceiveConsultancyFee() {
        setupForCardExecutionTest();
        when(card.getType()).thenReturn("ReceiveConsultancyFee");
        when(card.getIntProperty("amount", 0)).thenReturn(25);
        Mockito.clearInvocations(player1);
        monopolyService.drawChanceCard(player1);
        verify(player1).increaseMoney(25);
    }
    @Test
    void executeCardAction_BeautyContest() {
        setupForCardExecutionTest();
        when(card.getType()).thenReturn("BeautyContest");
        when(card.getIntProperty("amount", 0)).thenReturn(10);
        Mockito.clearInvocations(player1);
        monopolyService.drawChanceCard(player1);
        verify(player1).increaseMoney(10);
    }


    @Test
    void executeCardAction_ChairmanOfBoard_OthersHaveEnoughMoney() {
        setupForCardExecutionTest();
        when(card.getType()).thenReturn("ChairmanOfBoard");
        when(card.getIntProperty("amount", 50)).thenReturn(50);
        when(player2.getMoney()).thenReturn(100);
        Mockito.clearInvocations(player1, player2);
        monopolyService.drawChanceCard(player1);
        verify(player2).decreaseMoney(50);
        verify(player1).increaseMoney(50);
    }

    @Test
    void executeCardAction_ChairmanOfBoard_OthersNotEnoughMoney() {
        setupForCardExecutionTest();
        when(card.getType()).thenReturn("ChairmanOfBoard");
        when(card.getIntProperty("amount", 50)).thenReturn(50);
        when(player2.getMoney()).thenReturn(30);
        Mockito.clearInvocations(player1, player2);
        monopolyService.drawChanceCard(player1);
        verify(player2).decreaseMoney(30);
        verify(player1).increaseMoney(30);
    }

    @Test
    void executeCardAction_ChairmanOfBoard_NoOtherPlayers() {
        monopolyService.setCardService(cardService);
        players.clear();
        players.add(player1);
        monopolyService.setup(game);

        lenient().when(player1.getName()).thenReturn("Player1");
        lenient().when(card.getDescription()).thenReturn("Test Card Description");
        when(cardService.drawCard(anyString())).thenReturn(card);

        when(card.getType()).thenReturn("ChairmanOfBoard");
        when(card.getIntProperty("amount", 50)).thenReturn(50);
        Mockito.clearInvocations(player1);
        monopolyService.drawChanceCard(player1);
        verify(player1, atLeastOnce()).increaseMoney(anyInt());
    }

    @Test
    void executeCardAction_GrandOperaNight_OthersHaveEnoughMoney() {
        setupForCardExecutionTest();
        when(card.getType()).thenReturn("GrandOperaNight");
        when(card.getIntProperty("amount", 10)).thenReturn(10);
        when(player2.getMoney()).thenReturn(100);
        Mockito.clearInvocations(player1, player2);
        monopolyService.drawChanceCard(player1);
        verify(player2).decreaseMoney(10);
        verify(player1).increaseMoney(10);
    }

    @Test
    void executeCardAction_ItsYourBirthday_OthersHaveEnoughMoney() {
        setupForCardExecutionTest();
        when(card.getType()).thenReturn("ItsYourBirthday");
        when(card.getIntProperty("amount", 10)).thenReturn(10);
        when(player2.getMoney()).thenReturn(100);
        Mockito.clearInvocations(player1, player2);
        monopolyService.drawChanceCard(player1);
        verify(player2).decreaseMoney(10);
        verify(player1).increaseMoney(10);
    }

    @Test
    void executeCardAction_CollectFromPlayers_OthersNotEnoughMoney() {
        setupForCardExecutionTest();
        when(card.getType()).thenReturn("ItsYourBirthday");
        when(card.getIntProperty("amount", 10)).thenReturn(10);
        when(player2.getMoney()).thenReturn(5);
        Mockito.clearInvocations(player1, player2);
        monopolyService.drawChanceCard(player1);
        verify(player2).decreaseMoney(5);
        verify(player1).increaseMoney(5);
    }

    @Test
    void executeCardAction_DefaultCardType() {
        setupForCardExecutionTest(); // This calls monopolyService.setup(game) which calls player1.setCurrentTile(tile0)

        // Clear invocations that happened during the setup phase for relevant mocks
        Mockito.clearInvocations(player1, player2); // Add other mocks if they are verified with never() and called in setup

        when(card.getType()).thenReturn("UnknownCardType123");
        monopolyService.drawChanceCard(player1);

        // Verifications to ensure no standard actions took place *as a result of this card*
        verify(player1, never()).increaseMoney(anyInt());
        verify(player1, never()).decreaseMoney(anyInt());
        verify(player1, never()).setCurrentTile(any(Tile.class)); // This should now pass
        verify(player1, never()).move(anyInt());

        // The default card action should not put player1 in jail.
        assertFalse(monopolyService.isInJail(player1));

        assertTrue(systemOutContent.toString().contains("Player1 drew: Test Card Description"));
    }
}