package fr.ubordeaux.scrabble.view;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import fr.ubordeaux.scrabble.view.optionlancement.OptionPlayer;
import org.junit.jupiter.api.Test;

class OptionPlayerTest {

  @Test
  void defaultShouldBe2() {
    assertEquals(2, OptionPlayer.DEFAULT);
  }

  @Test
  void minShouldBe2() {
    assertEquals(2, OptionPlayer.MIN);
  }

  @Test
  void maxShouldBe4() {
    assertEquals(4, OptionPlayer.MAX);
  }

  @Test
  void parsePlayersShouldReturn2ForValidInput() {
    assertEquals(2, OptionPlayer.parsePlayers("2"));
  }

  @Test
  void parsePlayersShouldReturn3ForValidInput() {
    assertEquals(3, OptionPlayer.parsePlayers("3"));
  }

  @Test
  void parsePlayersShouldReturn4ForValidInput() {
    assertEquals(4, OptionPlayer.parsePlayers("4"));
  }
}
