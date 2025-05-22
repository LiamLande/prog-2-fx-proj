package edu.ntnu.idi.bidata.service;

import edu.ntnu.idi.bidata.model.Board;
import edu.ntnu.idi.bidata.model.BoardGame;
import edu.ntnu.idi.bidata.model.Dice;
import edu.ntnu.idi.bidata.model.Player;
import edu.ntnu.idi.bidata.model.Tile;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SnakesLaddersServiceTest {

    @Mock
    BoardGame gameMock;
    @Mock
    Player player1Mock;
    @Mock
    Player player2Mock;
    @Mock
    Tile startTileMock;
    @Mock
    Tile intermediateTileMock;
    @Mock
    Tile endTileMock; // A tile where getNext() is null
    @Mock
    Board boardMock;
    @Mock
    Dice diceMock;

    @InjectMocks
    SnakesLaddersService snakesLaddersService;

    private List<Player> playersList;

    @BeforeEach
    void setUp() {
        playersList = new ArrayList<>();
        lenient().when(gameMock.getPlayers()).thenReturn(playersList);
        lenient().when(gameMock.getBoard()).thenReturn(boardMock);
        lenient().when(boardMock.getStart()).thenReturn(startTileMock);
        lenient().when(gameMock.getDice()).thenReturn(diceMock);

        // Default tile behaviors for isFinished/getWinner
        lenient().when(startTileMock.getNext()).thenReturn(intermediateTileMock); // Not an end tile
        lenient().when(intermediateTileMock.getNext()).thenReturn(startTileMock); // Not an end tile (can be any other tile)
        lenient().when(endTileMock.getNext()).thenReturn(null); // This signifies an end tile

        // Player names for exception messages
        lenient().when(player1Mock.getName()).thenReturn("Player1");
        lenient().when(player2Mock.getName()).thenReturn("Player2");

        // Default getCurrentTile for players to avoid NPE in isFinished
        lenient().when(player1Mock.getCurrentTile()).thenReturn(intermediateTileMock);
        lenient().when(player2Mock.getCurrentTile()).thenReturn(intermediateTileMock);
    }

    @Test
    void setup_withNoPlayers_currentPlayerIndexIsNegativeOne() {
        // playersList is empty by default from setUp
        snakesLaddersService.setup(gameMock);
        assertNull(snakesLaddersService.getCurrentPlayer(gameMock));
        verify(boardMock).getStart(); // Verifies getStart was called
    }

    @Test
    void setup_withPlayers_setsCurrentPlayerAndTiles() {
        playersList.add(player1Mock);
        playersList.add(player2Mock);

        snakesLaddersService.setup(gameMock);

        verify(player1Mock).setCurrentTile(startTileMock);
        verify(player2Mock).setCurrentTile(startTileMock);
        assertEquals(player1Mock, snakesLaddersService.getCurrentPlayer(gameMock));
    }

    @Test
    void getCurrentPlayer_noPlayersSetup_returnsNull() {
        snakesLaddersService.setup(gameMock); // playersList is empty
        assertNull(snakesLaddersService.getCurrentPlayer(gameMock));
    }

    @Test
    void getCurrentPlayer_withPlayers_returnsCorrectPlayer() {
        playersList.add(player1Mock);
        playersList.add(player2Mock);
        snakesLaddersService.setup(gameMock); // player1Mock is current

        assertEquals(player1Mock, snakesLaddersService.getCurrentPlayer(gameMock));
    }

    @Test
    void playTurn_emptyPlayerList_throwsIllegalArgumentException() {
        snakesLaddersService.setup(gameMock); // playersList is empty
        // player1Mock is not in playersList, so this should throw
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            snakesLaddersService.playTurn(gameMock, player1Mock);
        });
        assertTrue(exception.getMessage().contains("is not in the game or not their turn"));
    }

    @Test
    void playTurn_playerNotCurrentButInGame_updatesIndexAndPlays() {
        playersList.add(player1Mock);
        playersList.add(player2Mock);
        snakesLaddersService.setup(gameMock); // player1Mock is current (index 0)

        when(diceMock.rollDie()).thenReturn(3);
        // Ensure getCurrentTile is stubbed for ALL players in the list for isFinished check
        when(player1Mock.getCurrentTile()).thenReturn(intermediateTileMock); // For isFinished
        when(player2Mock.getCurrentTile()).thenReturn(intermediateTileMock); // For isFinished

        int roll = snakesLaddersService.playTurn(gameMock, player2Mock); // Playing player2Mock's turn

        assertEquals(3, roll);
        verify(player2Mock).move(3);
        assertEquals(player1Mock, snakesLaddersService.getCurrentPlayer(gameMock)); // Next player should be player1Mock
    }

    @Test
    void playTurn_playerNotInGame_throwsIllegalArgumentException() {
        playersList.add(player1Mock);
        snakesLaddersService.setup(gameMock); // player1Mock is current

        Player unknownPlayer = mock(Player.class);
        when(unknownPlayer.getName()).thenReturn("Unknown");
        // unknownPlayer is not in playersList

        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            snakesLaddersService.playTurn(gameMock, unknownPlayer);
        });
        assertTrue(exception.getMessage().contains("Unknown is not in the game or not their turn."));
    }

    @Test
    void playTurn_currentPlayer_playsAndAdvancesTurn() {
        playersList.add(player1Mock);
        playersList.add(player2Mock);
        snakesLaddersService.setup(gameMock); // player1Mock is current

        when(diceMock.rollDie()).thenReturn(5);
        // Mock getCurrentTile for ALL players for isFinished check within playTurn
        when(player1Mock.getCurrentTile()).thenReturn(intermediateTileMock);
        when(player2Mock.getCurrentTile()).thenReturn(intermediateTileMock);


        int roll = snakesLaddersService.playTurn(gameMock, player1Mock);

        assertEquals(5, roll);
        verify(player1Mock).move(5);
        assertEquals(player2Mock, snakesLaddersService.getCurrentPlayer(gameMock)); // Turn advanced to player2Mock
    }

    @Test
    void playTurn_currentPlayer_singlePlayerGame_advancesToSelf() {
        playersList.add(player1Mock);
        snakesLaddersService.setup(gameMock); // player1Mock is current

        when(diceMock.rollDie()).thenReturn(4);
        when(player1Mock.getCurrentTile()).thenReturn(intermediateTileMock); // Game not finished

        int roll = snakesLaddersService.playTurn(gameMock, player1Mock);

        assertEquals(4, roll);
        verify(player1Mock).move(4);
        assertEquals(player1Mock, snakesLaddersService.getCurrentPlayer(gameMock)); // Still player1Mock's turn
    }

    @Test
    void playTurn_currentPlayer_gameFinishes_doesNotAdvanceTurnIndex() {
        playersList.add(player1Mock);
        playersList.add(player2Mock);
        snakesLaddersService.setup(gameMock); // player1Mock is current

        when(diceMock.rollDie()).thenReturn(6);
        // Simulate player1 landing on end tile.
        when(player1Mock.getCurrentTile()).thenReturn(endTileMock); // Player1 is on the end tile
        // player2 is not on end tile for this scenario. This stubbing is only needed if player1 *wasn't* on endTile.
        // Since player1 IS on endTile, anyMatch will short-circuit. So, make this lenient.
        lenient().when(player2Mock.getCurrentTile()).thenReturn(intermediateTileMock);


        int roll = snakesLaddersService.playTurn(gameMock, player1Mock);

        assertEquals(6, roll);
        verify(player1Mock).move(6);
        // Since game is finished by player1, the currentPlayerIndex should remain on player1.
        assertEquals(player1Mock, snakesLaddersService.getCurrentPlayer(gameMock));
    }


    @Test
    void isFinished_noPlayers_returnsFalse() {
        // playersList is empty
        assertFalse(snakesLaddersService.isFinished(gameMock));
    }

    @Test
    void isFinished_onePlayer_onEndTile_returnsTrue() {
        playersList.add(player1Mock);
        when(player1Mock.getCurrentTile()).thenReturn(endTileMock); // endTileMock.getNext() is null
        assertTrue(snakesLaddersService.isFinished(gameMock));
    }

    @Test
    void isFinished_onePlayer_notOnEndTile_returnsFalse() {
        playersList.add(player1Mock);
        when(player1Mock.getCurrentTile()).thenReturn(intermediateTileMock); // intermediateTileMock.getNext() is not null
        assertFalse(snakesLaddersService.isFinished(gameMock));
    }

    @Test
    void isFinished_multiplePlayers_oneOnEndTile_returnsTrue() {
        playersList.add(player1Mock);
        playersList.add(player2Mock);
        when(player1Mock.getCurrentTile()).thenReturn(intermediateTileMock);
        when(player2Mock.getCurrentTile()).thenReturn(endTileMock);
        assertTrue(snakesLaddersService.isFinished(gameMock));
    }

    @Test
    void isFinished_multiplePlayers_noneOnEndTile_returnsFalse() {
        playersList.add(player1Mock);
        playersList.add(player2Mock);
        when(player1Mock.getCurrentTile()).thenReturn(intermediateTileMock);
        when(player2Mock.getCurrentTile()).thenReturn(startTileMock); // or intermediateTileMock
        assertFalse(snakesLaddersService.isFinished(gameMock));
    }

    @Test
    void getWinner_noPlayers_returnsNull() {
        assertNull(snakesLaddersService.getWinner(gameMock));
    }

    @Test
    void getWinner_onePlayer_onEndTile_returnsPlayer() {
        playersList.add(player1Mock);
        when(player1Mock.getCurrentTile()).thenReturn(endTileMock);
        assertEquals(player1Mock, snakesLaddersService.getWinner(gameMock));
    }

    @Test
    void getWinner_onePlayer_notOnEndTile_returnsNull() {
        playersList.add(player1Mock);
        when(player1Mock.getCurrentTile()).thenReturn(intermediateTileMock);
        assertNull(snakesLaddersService.getWinner(gameMock));
    }

    @Test
    void getWinner_multiplePlayers_oneWinner_returnsWinner() {
        playersList.add(player1Mock);
        playersList.add(player2Mock);
        when(player1Mock.getCurrentTile()).thenReturn(intermediateTileMock);
        when(player2Mock.getCurrentTile()).thenReturn(endTileMock);
        assertEquals(player2Mock, snakesLaddersService.getWinner(gameMock));
    }

    @Test
    void getWinner_multiplePlayers_multiplePotentialWinners_returnsFirst() {
        // This tests the findFirst() behavior
        playersList.add(player1Mock);
        playersList.add(player2Mock);
        when(player1Mock.getCurrentTile()).thenReturn(endTileMock);
        // For player2Mock, make this lenient as it might not be called if player1 is found first.
        lenient().when(player2Mock.getCurrentTile()).thenReturn(endTileMock);
        assertEquals(player1Mock, snakesLaddersService.getWinner(gameMock)); // player1Mock is first in list
    }

    @Test
    void getWinner_multiplePlayers_noWinner_returnsNull() {
        playersList.add(player1Mock);
        playersList.add(player2Mock);
        when(player1Mock.getCurrentTile()).thenReturn(intermediateTileMock);
        when(player2Mock.getCurrentTile()).thenReturn(startTileMock); // or intermediateTileMock
        assertNull(snakesLaddersService.getWinner(gameMock));
    }
}