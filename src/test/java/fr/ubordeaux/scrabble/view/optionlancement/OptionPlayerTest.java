package fr.ubordeaux.scrabble.view.optionlancement;

import static org.junit.jupiter.api.Assertions.assertEquals;

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
