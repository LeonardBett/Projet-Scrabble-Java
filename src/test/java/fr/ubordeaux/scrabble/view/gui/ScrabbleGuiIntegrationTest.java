package fr.ubordeaux.scrabble.view.gui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import fr.ubordeaux.scrabble.model.core.Game;
import fr.ubordeaux.scrabble.model.core.HumanPlayer;
import fr.ubordeaux.scrabble.model.enums.PlayerColor;
import fr.ubordeaux.scrabble.view.FakePanels.FakeBoardPanel;
import fr.ubordeaux.scrabble.view.FakePanels.FakeControlPanel;
import fr.ubordeaux.scrabble.view.FakePanels.FakeMessagePanel;
import fr.ubordeaux.scrabble.view.FakePanels.FakeRackPanel;
import fr.ubordeaux.scrabble.view.FakePanels.FakeScorePanel;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Integration tests for fake GUI panels.
 * Validates that fake panels work correctly for tracking method calls without
 * JavaFX runtime.
 */
class ScrabbleGuiIntegrationTest {
  private Game game;
  private FakeBoardPanel boardPanel;
  private FakeRackPanel rackPanel;
  private FakeMessagePanel messagePanel;
  private FakeScorePanel scorePanel;
  private FakeControlPanel controlPanel;

  @BeforeEach
  void setUp() {
    // Create game with fixed players for state reference
    game = new Game();
    game.addPlayer(new HumanPlayer("Player1", PlayerColor.BLUE));
    game.addPlayer(new HumanPlayer("Player2", PlayerColor.RED));
    game.startGame();

    // Create all fake panels
    boardPanel = new FakeBoardPanel();
    rackPanel = new FakeRackPanel();
    messagePanel = new FakeMessagePanel();
    scorePanel = new FakeScorePanel();
    controlPanel = new FakeControlPanel();
  }

  // ===== BoardPanel tests =====
  @Test
  void testBoardPanelPlaceTile() {
    boardPanel.placeTile(7, 7, 'A', 10);
    assertTrue(boardPanel.wasPlaceTileCalled(7, 7, 'A'),
        "boardPanel should track placeTile call");
  }

  @Test
  void testBoardPanelClearTile() {
    boardPanel.clearTile(7, 7);
    assertTrue(boardPanel.wasClearTileCalled(7, 7),
        "boardPanel should track clearTile call");
  }

  @Test
  void testBoardPanelUpdateBoard() {
    boardPanel.updateBoard();
    assertTrue(boardPanel.wasUpdateBoardCalled(),
        "boardPanel should track updateBoard call");
  }

  @Test
  void testBoardPanelClearAllPending() {
    boardPanel.clearAllPending();
    assertTrue(boardPanel.wasClearAllPendingCalled(),
        "boardPanel should track clearAllPending call");
  }

  @Test
  void testBoardPanelMultipleCalls() {
    boardPanel.placeTile(0, 0, 'A', 1);
    boardPanel.placeTile(1, 1, 'B', 3);
    boardPanel.clearTile(0, 0);
    boardPanel.updateBoard();
    assertEquals(4, boardPanel.callCount(),
        "boardPanel should track all calls");
  }

  @Test
  void testBoardPanelReset() {
    boardPanel.placeTile(5, 5, 'Z', 10);
    assertTrue(boardPanel.callCount() > 0, "boardPanel should have calls");
    boardPanel.reset();
    assertEquals(0, boardPanel.callCount(), "reset should clear all calls");
  }

  // ===== RackPanel tests =====
  @Test
  void testRackPanelSetRack() {
    rackPanel.setRack(game.getCurrentPlayer().getRack());
    assertTrue(rackPanel.wasSetRackCalled(),
        "rackPanel should track setRack call");
  }

  @Test
  void testRackPanelDisable() {
    rackPanel.setDisable(true);
    assertTrue(rackPanel.callCount() > 0,
        "rackPanel should track setDisable call");
  }

  @Test
  void testRackPanelReset() {
    rackPanel.setRack(null);
    assertTrue(rackPanel.callCount() > 0, "rackPanel should have calls");
    rackPanel.reset();
    assertEquals(0, rackPanel.callCount(), "reset should clear all calls");
  }

  // ===== MessagePanel tests =====
  @Test
  void testMessagePanelShowError() {
    messagePanel.showError("Test error message");
    assertTrue(messagePanel.wasErrorShown(),
        "messagePanel should track showError call");
  }

  @Test
  void testMessagePanelShowInfo() {
    messagePanel.showInfo("Title", "Message");
    assertTrue(messagePanel.wasInfoShown(),
        "messagePanel should track showInfo call");
  }

  @Test
  void testMessagePanelShowConfirmation() {
    boolean result = messagePanel.showConfirmation("Confirm action?");
    assertTrue(messagePanel.wasConfirmationAsked(),
        "messagePanel should track showConfirmation call");
    assertEquals(true, result, "confirmation should return default value");
  }

  @Test
  void testMessagePanelConfirmationResult() {
    messagePanel.confirmResult = false;
    boolean result = messagePanel.showConfirmation("Confirm?");
    assertEquals(false, result, "confirmation should return false");
  }

  @Test
  void testMessagePanelReset() {
    messagePanel.showError("Error");
    assertTrue(messagePanel.wasErrorShown(), "messagePanel should have calls");
    messagePanel.reset();
    assertEquals(0, messagePanel.callCount(), "reset should clear all calls");
    assertEquals(true, messagePanel.confirmResult, "reset should restore default confirmResult");
  }

  // ===== ScorePanel tests =====
  @Test
  void testScorePanelUpdateScores() {
    String[] names = { "Player1", "Player2" };
    int[] scores = { 100, 200 };
    scorePanel.updateScores(names, scores);
    assertTrue(scorePanel.wasScoresUpdated(),
        "scorePanel should track updateScores call");
  }

  @Test
  void testScorePanelUpdateBagInfo() {
    scorePanel.updateBagInfo(50);
    assertTrue(scorePanel.wasBagInfoUpdated(),
        "scorePanel should track updateBagInfo call");
  }

  @Test
  void testScorePanelHighlightCurrentPlayer() {
    scorePanel.highlightCurrentPlayer(0, "Player1");
    assertTrue(scorePanel.callCount() > 0,
        "scorePanel should track highlightCurrentPlayer call");
  }

  @Test
  void testScorePanelReset() {
    scorePanel.updateScores(new String[] { "P1" }, new int[] { 10 });
    assertTrue(scorePanel.callCount() > 0, "scorePanel should have calls");
    scorePanel.reset();
    assertEquals(0, scorePanel.callCount(), "reset should clear all calls");
  }

  // ===== ControlPanel tests =====
  @Test
  void testControlPanelSetGameplayButtonsDisabled() {
    controlPanel.setGameplayButtonsDisabled(true);
    assertTrue(controlPanel.wasPlayButtonsDisabled(true),
        "controlPanel should track setGameplayButtonsDisabled(true)");
  }

  @Test
  void testControlPanelSetGameplayButtonsEnabled() {
    controlPanel.setGameplayButtonsDisabled(false);
    assertTrue(controlPanel.wasPlayButtonsDisabled(false),
        "controlPanel should track setGameplayButtonsDisabled(false)");
  }

  @Test
  void testControlPanelPlayButton() {
    var callback = controlPanel.getPlayButton();
    assertNotNull(callback, "playButton callback should be available");
  }

  @Test
  void testControlPanelPassButton() {
    var callback = controlPanel.getPassButton();
    assertNotNull(callback, "passButton callback should be available");
  }

  @Test
  void testControlPanelExchangeButton() {
    var callback = controlPanel.getExchangeButton();
    assertNotNull(callback, "exchangeButton callback should be available");
  }

  @Test
  void testControlPanelReset() {
    controlPanel.setGameplayButtonsDisabled(true);
    assertTrue(controlPanel.callCount() > 0, "controlPanel should have calls");
    controlPanel.reset();
    assertEquals(0, controlPanel.callCount(), "reset should clear all calls");
  }

  // ===== Integration and error handling =====
  @Test
  void testGameStateAvailability() {
    assertNotNull(game, "game should be initialized");
    assertEquals(2, game.getPlayers().size(), "game should have 2 players");
    assertNotNull(game.getCurrentPlayer(), "game should have current player");
  }

  @Test
  void testAllPanelsCreated() {
    assertNotNull(boardPanel, "boardPanel should be created");
    assertNotNull(rackPanel, "rackPanel should be created");
    assertNotNull(messagePanel, "messagePanel should be created");
    assertNotNull(scorePanel, "scorePanel should be created");
    assertNotNull(controlPanel, "controlPanel should be created");
  }

  @Test
  void testPanelCallCountingIsAccurate() {
    boardPanel.placeTile(0, 0, 'A', 1);
    boardPanel.placeTile(1, 1, 'B', 2);
    boardPanel.clearTile(0, 0);
    assertEquals(3, boardPanel.callCount(), "call count should be accurate");
  }

  @Test
  void testMultiplePanelsIndependent() {
    boardPanel.placeTile(0, 0, 'A', 1);
    rackPanel.setRack(null);
    messagePanel.showError("Error");
    scorePanel.updateBagInfo(10);
    controlPanel.setGameplayButtonsDisabled(true);

    assertTrue(boardPanel.callCount() >= 1, "boardPanel should have its own calls");
    assertTrue(rackPanel.callCount() >= 1, "rackPanel should have its own calls");
    assertTrue(messagePanel.callCount() >= 1, "messagePanel should have its own calls");
    assertTrue(scorePanel.callCount() >= 1, "scorePanel should have its own calls");
    assertTrue(controlPanel.callCount() >= 1, "controlPanel should have its own calls");
  }
}
