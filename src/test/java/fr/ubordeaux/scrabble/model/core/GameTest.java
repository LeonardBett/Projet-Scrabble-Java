package fr.ubordeaux.scrabble.model.core;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Duration;
import org.junit.jupiter.api.Test;

class GameTest {

  /**
   * Test that attempting to start a game without any players throws an IllegalStateException.
   */
  @Test
  void startGameShouldFailWhenNoPlayersExist() {
    Game game = new Game();

    assertThrows(IllegalStateException.class, game::startGame);
  }

  /**
   * Test that starting a game correctly fills each player's rack with the maximum number of tiles
   * from the bag.
   */
  @Test
  void startGameShouldFillPlayerRacks() {
    Game game = new Game();
    HumanPlayer alice = new HumanPlayer("Alice");
    HumanPlayer bob = new HumanPlayer("Bob");

    game.addPlayer(alice);
    game.addPlayer(bob);

    int initialBagSize = game.getBag().size();
    game.startGame();

    assertEquals(Rack.MAX_SIZE, alice.getRack().getTiles().size());
    assertEquals(Rack.MAX_SIZE, bob.getRack().getTiles().size());
    assertEquals(initialBagSize - 2 * Rack.MAX_SIZE, game.getBag().size());
  }

  /**
   * Test that executing a move for a player who is not the current player throws an
   * IllegalArgumentException.
   */
  @Test
  void executeMoveShouldRejectWrongPlayerTurn() {
    Game game = new Game();
    HumanPlayer alice = new HumanPlayer("Alice");
    HumanPlayer bob = new HumanPlayer("Bob");
    game.addPlayer(alice);
    game.addPlayer(bob);

    Move wrongTurnMove = Move.createPass(bob);

    assertThrows(IllegalArgumentException.class, () -> game.executeMove(wrongTurnMove));
  }

  /**
   * Test that executing a pass move advances the turn to the next player and adds the move to the
   * game's history.
   */
  @Test
  void executePassMoveShouldAdvanceTurnAndTrackHistory() {
    Game game = new Game();
    HumanPlayer alice = new HumanPlayer("Alice");
    HumanPlayer bob = new HumanPlayer("Bob");
    game.addPlayer(alice);
    game.addPlayer(bob);

    game.executeMove(Move.createPass(alice));

    assertEquals(bob, game.getCurrentPlayer());
    assertEquals(1, game.getUndoRedo().getHistory().size());
  }

  /**
   * Test that determining the winner returns the player with the highest score, or null if there
   * are no players in the game.
   */
  @Test
  void determineWinnerShouldReturnHighestScoreOrNullWhenNoPlayers() {
    Game emptyGame = new Game();
    assertNull(emptyGame.determineWinner());

    Game game = new Game();
    HumanPlayer alice = new HumanPlayer("Alice");
    HumanPlayer bob = new HumanPlayer("Bob");
    alice.addScore(10);
    bob.addScore(15);
    game.addPlayer(alice);
    game.addPlayer(bob);

    assertEquals(bob, game.determineWinner());
  }

  /**
   * Test that refilling a player's rack draws tiles from the bag until the rack is full or the bag
   * is empty.
   */
  @Test
  void refillRackShouldAddTilesUntilFullOrBagEmpty() {
    Game game = new Game();
    HumanPlayer player = new HumanPlayer("Alice");

    var drawn = game.refillRack(player);

    assertNotNull(drawn);
    assertTrue(drawn.size() <= Rack.MAX_SIZE);
    assertEquals(drawn.size(), player.getRack().getTiles().size());
  }

  @Test
  void blitzModeShouldStartOnlyCurrentPlayerTimer() {
    Game game = new Game();
    HumanPlayer alice = new HumanPlayer("Alice");
    HumanPlayer bob = new HumanPlayer("Bob");
    game.addPlayer(alice);
    game.addPlayer(bob);

    game.enableBlitzMode(Duration.ofSeconds(30));
    game.startGame();

    assertTrue(game.isBlitzModeEnabled());
    assertTrue(alice.isBlitzClockEnabled());
    assertTrue(bob.isBlitzClockEnabled());
    assertTrue(alice.isTurnTimerRunning());
    assertTrue(!bob.isTurnTimerRunning());
  }

  @Test
  void blitzModeShouldPauseAndResumeOnTurnChange() throws InterruptedException {
    Game game = new Game();
    HumanPlayer alice = new HumanPlayer("Alice");
    HumanPlayer bob = new HumanPlayer("Bob");
    game.addPlayer(alice);
    game.addPlayer(bob);

    game.enableBlitzMode(Duration.ofMillis(300));
    game.startGame();

    Thread.sleep(120);
    game.executeMove(Move.createPass(alice));

    assertTrue(!alice.isTurnTimerRunning());
    assertTrue(bob.isTurnTimerRunning());
    assertTrue(alice.getRemainingTimeMillis() < 300);
    assertTrue(bob.getRemainingTimeMillis() <= 300);
  }

  @Test
  void blitzModeShouldEndGameWhenCurrentPlayerTimesOut() throws InterruptedException {
    Game game = new Game();
    HumanPlayer alice = new HumanPlayer("Alice");
    HumanPlayer bob = new HumanPlayer("Bob");
    game.addPlayer(alice);
    game.addPlayer(bob);

    game.enableBlitzMode(Duration.ofMillis(80));
    game.startGame();
    Thread.sleep(150);

    assertThrows(IllegalStateException.class, () -> game.executeMove(Move.createPass(alice)));
    assertTrue(game.isGameOver());
  }
}
