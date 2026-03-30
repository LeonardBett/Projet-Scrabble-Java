package fr.ubordeaux.scrabble.model.interfaces;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import fr.ubordeaux.scrabble.model.core.Game;
import fr.ubordeaux.scrabble.model.dictionary.Gaddag;
import fr.ubordeaux.scrabble.model.enums.PlayerColor;
import java.time.Duration;
import org.junit.jupiter.api.Test;

class PlayerTest {

  @Test
  void setScoreShouldOverwriteCurrentScore() {
    StubPlayer player = new StubPlayer("Alice");

    player.addScore(10);
    player.setScore(3);

    assertEquals(3, player.getScore());
  }

  @Test
  void playTurnShouldThrowByDefault() {
    StubPlayer player = new StubPlayer("Alice");

    assertThrows(UnsupportedOperationException.class,
        () -> player.playTurn(new Game(), new Gaddag()));
  }

  @Test
  void enableBlitzClockShouldRejectNonPositiveDuration() {
    StubPlayer player = new StubPlayer("Alice");

    assertThrows(IllegalArgumentException.class, () -> player.enableBlitzClock(Duration.ZERO));
    assertThrows(
        IllegalArgumentException.class,
        () -> player.enableBlitzClock(Duration.ofSeconds(-1)));
  }

  @Test
  void disableBlitzClockShouldResetAllTimerState() {
    StubPlayer player = new StubPlayer("Alice");

    player.enableBlitzClock(Duration.ofSeconds(5));
    player.startTurnTimer();
    player.disableBlitzClock();

    assertFalse(player.isBlitzClockEnabled());
    assertFalse(player.isTurnTimerRunning());
    assertEquals(0L, player.getRemainingTimeMillis());
    assertFalse(player.isOutOfTime());
  }

  @Test
  void startTurnTimerShouldDoNothingWhenBlitzDisabled() {
    StubPlayer player = new StubPlayer("Alice");

    player.startTurnTimer();

    assertFalse(player.isTurnTimerRunning());
  }

  @Test
  void pauseTurnTimerShouldDecreaseRemainingTime() throws Exception {
    StubPlayer player = new StubPlayer("Alice");

    player.enableBlitzClock(Duration.ofMillis(200));
    player.startTurnTimer();
    Thread.sleep(20L);
    player.pauseTurnTimer();

    assertFalse(player.isTurnTimerRunning());
    assertTrue(player.getRemainingTimeMillis() < 200L);
    assertTrue(player.getRemainingTimeMillis() >= 0L);
  }

  @Test
  void remainingDisplayShouldBeFormattedAsMmSs() {
    StubPlayer player = new StubPlayer("Alice");

    player.enableBlitzClock(Duration.ofSeconds(65));

    assertEquals("01:05", player.getRemainingTimeDisplay());
  }

  private static final class StubPlayer extends Player {
    private StubPlayer(String name) {
      super(name, PlayerColor.BLUE);
    }
  }
}
