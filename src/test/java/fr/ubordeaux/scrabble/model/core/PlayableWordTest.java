package fr.ubordeaux.scrabble.model.core;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import fr.ubordeaux.scrabble.model.enums.Direction;
import org.junit.jupiter.api.Test;

class PlayableWordTest {

  /**
   * Test that the PlayableWord constructor correctly initializes all fields (position, word,
   * direction, GADDAG representation) and allows score updates.
   */
  @Test
  void constructorShouldExposeAllFieldsAndAllowScoreUpdate() {
    PlayableWord playableWord = new PlayableWord(5, 6, "WORD", Direction.VERTICAL, "WO+RD");

    assertEquals(5, playableWord.getHookX());
    assertEquals(6, playableWord.getHookY());
    assertEquals("WORD", playableWord.getWord());
    assertEquals(Direction.VERTICAL, playableWord.getDirection());
    assertEquals("WO+RD", playableWord.getGaddagRepresentation());
    assertEquals(0, playableWord.getScore());

    playableWord.setScore(19);
    assertEquals(19, playableWord.getScore());
  }

  /**
   * Test that the toString method produces a string containing the main information about the
   * playable word (word, position, score).
   */
  @Test
  void toStringShouldContainMainInformation() {
    PlayableWord playableWord = new PlayableWord(1, 2, "HI", Direction.HORIZONTAL, "H+I");
    playableWord.setScore(10);

    String rendered = playableWord.toString();
    assertTrue(rendered.contains("word=HI"));
    assertTrue(rendered.contains("x=1"));
    assertTrue(rendered.contains("y=2"));
    assertTrue(rendered.contains("score=10"));
  }
}
