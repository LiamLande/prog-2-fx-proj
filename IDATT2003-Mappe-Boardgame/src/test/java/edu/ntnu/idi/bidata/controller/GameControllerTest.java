// src/test/java/edu/ntnu/idi/bidata/controller/GameControllerTest.java
package edu.ntnu.idi.bidata.controller;

import edu.ntnu.idi.bidata.model.*;
import edu.ntnu.idi.bidata.model.actions.monopoly.*;
import edu.ntnu.idi.bidata.model.actions.snakes.SchrodingerBoxAction;
import edu.ntnu.idi.bidata.service.MonopolyService;
import edu.ntnu.idi.bidata.service.ServiceLocator;
import edu.ntnu.idi.bidata.ui.SceneManager;
import edu.ntnu.idi.bidata.ui.monopoly.MonopolyBoardView;
import edu.ntnu.idi.bidata.ui.monopoly.MonopolyGameScene;
import edu.ntnu.idi.bidata.ui.sl.SnakeLadderBoardView;
import edu.ntnu.idi.bidata.ui.sl.SnakeLadderGameScene;
import edu.ntnu.idi.bidata.util.Logger;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class GameControllerTest {

  private GameController gameController;
  private BoardGame mockGameModel;

  // UI Mocks (using actual class names from your image)
  private SnakeLadderGameScene mockSlScene;
  private MonopolyGameScene mockMonopolyScene;
  private SnakeLadderBoardView mockSlBoardView;
  private MonopolyBoardView mockMonopolyBoardView;

  private Player mockPlayer1;
  private Player mockPlayer2;
  private MonopolyService mockMonopolyService;
  private Tile mockTile;
  private SchrodingerBoxAction mockSchrodingerActionInstance;
  private PropertyAction mockPropertyAction;
  private Board mockBoard;

  private final ByteArrayOutputStream outContent = new ByteArrayOutputStream();
  private final ByteArrayOutputStream errContent = new ByteArrayOutputStream();
  private final PrintStream originalOut = System.out;
  private final PrintStream originalErr = System.err;

  @BeforeEach
  void setUp() {
    System.setOut(new PrintStream(outContent));
    System.setErr(new PrintStream(errContent));
    Logger.info("-------------------- Test Case Start --------------------");

    mockGameModel = mock(BoardGame.class);

    // UI Mocks
    mockSlScene = mock(SnakeLadderGameScene.class);
    mockMonopolyScene = mock(MonopolyGameScene.class);
    mockSlBoardView = mock(SnakeLadderBoardView.class);
    mockMonopolyBoardView = mock(MonopolyBoardView.class);

    when(mockSlScene.getBoardView()).thenReturn(mockSlBoardView);
    when(mockMonopolyScene.getBoardView()).thenReturn(mockMonopolyBoardView);

    mockPlayer1 = mock(Player.class);
    mockPlayer2 = mock(Player.class);
    mockMonopolyService = mock(MonopolyService.class);
    mockTile = mock(Tile.class);
    mockSchrodingerActionInstance = mock(SchrodingerBoxAction.class);
    mockPropertyAction = mock(PropertyAction.class);
    mockBoard = mock(Board.class);

    when(mockPlayer1.getName()).thenReturn("PlayerOne");
    when(mockPlayer2.getName()).thenReturn("PlayerTwo");
    when(mockPlayer1.getCurrentTile()).thenReturn(mockTile);
    when(mockTile.getId()).thenReturn(1);
    when(mockGameModel.getBoard()).thenReturn(mockBoard);

    ServiceLocator.setMonopolyService(mockMonopolyService);
    gameController = new GameController(mockGameModel);

    when(mockGameModel.getCurrentPlayer()).thenReturn(mockPlayer1); // Default current player
    when(mockGameModel.isGameStarted()).thenReturn(false);
  }

  @AfterEach
  void tearDown() {
    ServiceLocator.setMonopolyService(null);
    System.setOut(originalOut);
    System.setErr(originalErr);

    outContent.reset();
    errContent.reset();
    Logger.info("-------------------- Test Case End ----------------------");
  }

  private String getOut() { return outContent.toString(); }
  private String getErr() { return errContent.toString(); }


  @Test
  @DisplayName("Constructor should add self as observer and locate MonopolyService")
  void testConstructor() {
    verify(mockGameModel).addObserver(gameController);
    assertNotNull(gameController);
    assertTrue(getOut().contains("GameController initialized."));
    // Depending on whether mockMonopolyService was non-null when ServiceLocator was called by constructor
    if (mockMonopolyService != null) {
      assertTrue(getOut().contains("MonopolyService located and assigned."), "MonopolyService should be located. Logs: " + getOut());
    } else {
      assertTrue(getErr().contains("MonopolyService not found via ServiceLocator."), "Warning for not found service expected. Logs: " + getErr());
    }
  }

  @Test
  @DisplayName("Constructor handles null MonopolyService from ServiceLocator")
  void testConstructor_NullMonopolyService() {
    ServiceLocator.setMonopolyService(null); // Ensure it's null BEFORE controller construction
    GameController controllerWithNullService = new GameController(mockGameModel);
    assertTrue(getErr().contains("MonopolyService not found via ServiceLocator."), "Err Logs: " + getErr());
    assertNotNull(controllerWithNullService);
  }

  @Test
  @DisplayName("setActiveView should set view and log; initialize if game started (SnakeLadder)")
  void testSetActiveView_GameStarted_SnakeLadder() {
    when(mockGameModel.isGameStarted()).thenReturn(true);
    gameController.setActiveView(mockSlScene); // Use the class member mock

    assertTrue(getOut().contains("Active view set to: SnakeLadderGameScene"), "Actual logs: " + getOut());
    assertTrue(getOut().contains("Initializing/refreshing view for current game state."), "Actual logs: " + getOut());
  }

  @Test
  @DisplayName("setActiveView should set view and log; initialize if game started (Monopoly)")
  void testSetActiveView_GameStarted_Monopoly() {
    when(mockGameModel.isGameStarted()).thenReturn(true);
    gameController.setActiveView(mockMonopolyScene);

    assertTrue(getOut().contains("Active view set to: MonopolyGameScene"), "Actual logs: " + getOut());
    assertTrue(getOut().contains("Initializing/refreshing view for current game state."), "Actual logs: " + getOut());
  }


  @Test
  @DisplayName("setActiveView should set view and log; skip init if game not started")
  void testSetActiveView_GameNotStarted() {
    when(mockGameModel.isGameStarted()).thenReturn(false);
    gameController.setActiveView(mockSlScene); // Any ControlledScene mock

    assertTrue(getOut().contains("Active view set to: SnakeLadderGameScene"), "Actual logs: " + getOut());
    assertTrue(getOut().contains("Skipping view initialization/refresh: Active view is null or game not started."), "Actual logs: " + getOut());
  }

  @Test
  @DisplayName("setActiveView with null view should log skipping")
  void testSetActiveView_NullView_LogsSkipping() {
    when(mockGameModel.isGameStarted()).thenReturn(true);
    gameController.setActiveView(null);

    assertTrue(getOut().contains("Active view set to: null"), "Actual logs: " + getOut());
    assertTrue(getOut().contains("Skipping view initialization/refresh: Active view is null or game not started."), "Actual logs: " + getOut());
    assertFalse(getErr().contains("Attempted to initialize/refresh view, but activeView is null."), "This warning should not appear. Actual err: " + getErr());
  }



  @Test
  @DisplayName("startGame should call gameModel.init()")
  void testStartGame() {
    gameController.startGame();
    verify(mockGameModel).init();
  }

  // --- BoardGameObserver Callbacks ---

  @Test
  @DisplayName("onGameStart should set current player and log (even if activeView is null)")
  void testOnGameStart_NullActiveView() {
    gameController.setActiveView(null);
    when(mockGameModel.getCurrentPlayer()).thenReturn(mockPlayer1);

    gameController.onGameStart(List.of(mockPlayer1, mockPlayer2));

    assertTrue(getErr().contains("onGameStart: activeView is null, UI updates will be skipped."), "Actual Logs: " + getErr());
  }

  @Test
  @DisplayName("onGameOver should log winner (activeView is null)")
  void testOnGameOver_NullActiveView() {
    gameController.setActiveView(null);
    gameController.onGameOver(mockPlayer1);
    assertTrue(getErr().contains("onGameOver: activeView is null, UI updates will be skipped."), "Actual Logs: " + getErr());
    assertTrue(getOut().contains("Game Over. Winner: PlayerOne"), "Actual Logs: " + getOut());
  }

  @Test
  @DisplayName("onGameOver with null winner")
  void testOnGameOver_NullWinner() {
    gameController.setActiveView(null);
    gameController.onGameOver(null);
    assertTrue(getOut().contains("Game Over. Winner: No one (Draw or Error)"), "Actual Logs: " + getOut());
  }

  // --- Player Action Requests ---


  @Test
  @DisplayName("handleRollDiceRequest should do nothing if game is finished")
  void testHandleRollDiceRequest_GameFinished() {
    when(mockGameModel.isFinished()).thenReturn(true);
    gameController.onGameStart(List.of(mockPlayer1));

    gameController.handleRollDiceRequest();
    verify(mockGameModel, never()).playTurn(any(Player.class));
    assertTrue(getErr().contains("Roll dice request ignored: Game is finished."), "Actual logs: " + getErr());
  }

  @Test
  @DisplayName("handleRollDiceRequest should do nothing if current player is null")
  void testHandleRollDiceRequest_NullCurrentPlayer() {
    when(mockGameModel.getCurrentPlayer()).thenReturn(null);
    gameController.onGameStart(Collections.emptyList());

    gameController.handleRollDiceRequest();
    verify(mockGameModel, never()).playTurn(any(Player.class));
    assertTrue(getErr().contains("Roll dice request ignored: Current player is null."), "Actual logs: " + getErr());
  }

  @Test
  @DisplayName("handleObserveSchrodingerBoxRequest does nothing if not awaiting choice")
  void testHandleObserveSchrodingerBoxRequest_NotInAwaitingState() {
    gameController.onGameStart(List.of(mockPlayer1)); // Not awaiting
    gameController.handleObserveSchrodingerBoxRequest();
    verify(mockSchrodingerActionInstance, never()).executeObserve(any(), any());
    assertTrue(getErr().contains("ObserveSchrodingerBoxRequest called inappropriately."), "Actual logs: " + getErr());
  }

  @Test
  @DisplayName("handleIgnoreSchrodingerBoxRequest does nothing if not awaiting choice")
  void testHandleIgnoreSchrodingerBoxRequest_NotInAwaitingState() {
    gameController.onGameStart(List.of(mockPlayer1)); // Not awaiting
    gameController.handleIgnoreSchrodingerBoxRequest();
    verify(mockSchrodingerActionInstance, never()).executeIgnore(any());
    assertTrue(getErr().contains("IgnoreSchrodingerBoxRequest called inappropriately."), "Actual logs: " + getErr());
  }


  @Test
  @DisplayName("getGameModel returns the game model")
  void testGetGameModel() {
    assertEquals(mockGameModel, gameController.getGameModel());
  }


  private void setupForHandleLandedOnProperty() {
    gameController.setActiveView(mockMonopolyScene); // Set active view to Monopoly
    when(mockTile.getAction()).thenReturn(mockPropertyAction);
    when(mockPlayer1.getCurrentTile()).thenReturn(mockTile);
    when(mockGameModel.getCurrentPlayer()).thenReturn(mockPlayer1); // P1's turn initially
    gameController.onGameStart(List.of(mockPlayer1, mockPlayer2)); // Sets controller.currentPlayer to P1

    clearInvocations(mockMonopolyService); // Clear any service calls from previous setup
  }

  @Test
  @DisplayName("handleLandedOnProperty: unowned, player cannot afford")
  void testHandleLandedOnProperty_Unowned_CannotAfford() {
    setupForHandleLandedOnProperty();
    when(mockPropertyAction.getOwner()).thenReturn(null);
    when(mockPropertyAction.getName()).thenReturn("ExpensiveProp");
    when(mockPropertyAction.getCost()).thenReturn(1000);
    when(mockPlayer1.getMoney()).thenReturn(100);

    gameController.onRoundPlayed(List.of(3), List.of(mockPlayer1, mockPlayer2));
    assertTrue(getOut().contains("Player PlayerOne cannot afford unowned property ExpensiveProp"), "Actual out: " + getOut());
  }

  @Test
  @DisplayName("handleLandedOnProperty: unowned, player can afford, chooses not to buy")
  void testHandleLandedOnProperty_Unowned_CanAfford_NoBuy() {
    setupForHandleLandedOnProperty();
    when(mockMonopolyScene.showPropertyPurchaseDialog(mockPlayer1, mockPropertyAction)).thenReturn(false); // Chooses not to buy

    when(mockPropertyAction.getOwner()).thenReturn(null);
    when(mockPropertyAction.getName()).thenReturn("AffordableProp");
    when(mockPropertyAction.getCost()).thenReturn(50);
    when(mockPlayer1.getMoney()).thenReturn(200);

    gameController.onRoundPlayed(List.of(3), List.of(mockPlayer1, mockPlayer2));
    assertTrue(getOut().contains("Player PlayerOne chose not to buy unowned property AffordableProp"), "Actual out: " + getOut());
    verify(mockMonopolyService, never()).purchaseProperty(any(), any());
  }

  @Test
  @DisplayName("handleLandedOnProperty: unowned, player can afford, buys, purchase succeeds")
  void testHandleLandedOnProperty_Unowned_CanAfford_Buys_Success() {
    setupForHandleLandedOnProperty();
    when(mockMonopolyScene.showPropertyPurchaseDialog(mockPlayer1, mockPropertyAction)).thenReturn(true);
    when(mockMonopolyService.purchaseProperty(mockPlayer1, mockPropertyAction)).thenReturn(true);

    when(mockPropertyAction.getOwner()).thenReturn(null);
    when(mockPropertyAction.getName()).thenReturn("GoodDealProp");
    when(mockPropertyAction.getCost()).thenReturn(50);
    when(mockPlayer1.getMoney()).thenReturn(200); // getMoney used for display after purchase

    gameController.onRoundPlayed(List.of(3), List.of(mockPlayer1, mockPlayer2));
    verify(mockMonopolyService).purchaseProperty(mockPlayer1, mockPropertyAction);
    assertTrue(getOut().contains("Property GoodDealProp purchased by PlayerOne"), "Actual out: " + getOut());
  }

  @Test
  @DisplayName("handleLandedOnProperty: unowned, player can afford, buys, purchase fails")
  void testHandleLandedOnProperty_Unowned_CanAfford_Buys_Fails() {
    setupForHandleLandedOnProperty();
    when(mockMonopolyScene.showPropertyPurchaseDialog(mockPlayer1, mockPropertyAction)).thenReturn(true);
    when(mockMonopolyService.purchaseProperty(mockPlayer1, mockPropertyAction)).thenReturn(false);

    when(mockPropertyAction.getOwner()).thenReturn(null);
    when(mockPropertyAction.getName()).thenReturn("BadDealProp");
    when(mockPropertyAction.getCost()).thenReturn(50);
    when(mockPlayer1.getMoney()).thenReturn(200);

    gameController.onRoundPlayed(List.of(3), List.of(mockPlayer1, mockPlayer2));
    verify(mockMonopolyService).purchaseProperty(mockPlayer1, mockPropertyAction);
    assertTrue(getErr().contains("Purchase of BadDealProp by PlayerOne failed despite affording it"), "Actual err: " + getErr());
  }

  @Test
  @DisplayName("handleLandedOnProperty: owned by another, rent paid successfully")
  void testHandleLandedOnProperty_OwnedByAnother_RentPaid() {
    setupForHandleLandedOnProperty();
    when(mockMonopolyService.payRent(eq(mockPlayer1), eq(mockPlayer2), anyInt())).thenReturn(true);

    when(mockPropertyAction.getOwner()).thenReturn(mockPlayer2);
    when(mockPlayer2.getName()).thenReturn("OwnerPlayer2"); // For log message
    when(mockPropertyAction.getName()).thenReturn("RentedProp");
    when(mockPropertyAction.getRent()).thenReturn(20);

    gameController.onRoundPlayed(List.of(3), List.of(mockPlayer1, mockPlayer2)); // P1 lands
    verify(mockMonopolyService).payRent(mockPlayer1, mockPlayer2, 20);
    assertTrue(getOut().contains("Player PlayerOne paid $20 rent to OwnerPlayer2 for RentedProp"), "Actual out: " + getOut());
  }

  @Test
  @DisplayName("handleLandedOnProperty: owned by another, rent payment fails")
  void testHandleLandedOnProperty_OwnedByAnother_RentFails() {
    setupForHandleLandedOnProperty();
    when(mockMonopolyService.payRent(eq(mockPlayer1), eq(mockPlayer2), anyInt())).thenReturn(false);

    when(mockPropertyAction.getOwner()).thenReturn(mockPlayer2);
    when(mockPlayer2.getName()).thenReturn("OwnerPlayer2");
    when(mockPropertyAction.getName()).thenReturn("CostlyRentedProp");
    when(mockPropertyAction.getRent()).thenReturn(500);

    gameController.onRoundPlayed(List.of(3), List.of(mockPlayer1, mockPlayer2)); // P1 lands
    verify(mockMonopolyService).payRent(mockPlayer1, mockPlayer2, 500);
    assertTrue(getErr().contains("Player PlayerOne could not afford to pay $500 rent for CostlyRentedProp"), "Actual err: " + getErr());
  }

  @Test
  @DisplayName("handleLandedOnProperty: player lands on own property")
  void testHandleLandedOnProperty_OwnsProperty() {
    setupForHandleLandedOnProperty();
    when(mockPropertyAction.getOwner()).thenReturn(mockPlayer1); // Owned by P1
    when(mockPropertyAction.getName()).thenReturn("MyOwnProp");

    gameController.onRoundPlayed(List.of(3), List.of(mockPlayer1, mockPlayer2)); // P1 lands
    verify(mockMonopolyService, never()).payRent(any(), any(), anyInt());
    verify(mockMonopolyService, never()).purchaseProperty(any(), any());
    assertTrue(getOut().contains("Player PlayerOne landed on their own property: MyOwnProp"), "Actual out: " + getOut());
  }
}