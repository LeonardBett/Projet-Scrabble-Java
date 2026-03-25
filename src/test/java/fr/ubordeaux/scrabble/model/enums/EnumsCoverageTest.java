package fr.ubordeaux.scrabble.model.enums;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;

class EnumsCoverageTest {

  @Test
  void directionValuesShouldBeStable() {
    assertEquals(2, Direction.values().length);
    assertEquals(Direction.HORIZONTAL, Direction.valueOf("HORIZONTAL"));
    assertEquals(Direction.VERTICAL, Direction.valueOf("VERTICAL"));
  }

  @Test
  void gameModeValuesShouldBeStable() {
    assertEquals(2, GameMode.values().length);
    assertEquals(GameMode.STANDARD, GameMode.valueOf("STANDARD"));
    assertEquals(GameMode.SUPER, GameMode.valueOf("SUPER"));
  }

  @Test
  void moveTypeValuesShouldBeStable() {
    assertEquals(3, MoveType.values().length);
    assertEquals(MoveType.PLAY, MoveType.valueOf("PLAY"));
    assertEquals(MoveType.EXCHANGE, MoveType.valueOf("EXCHANGE"));
    assertEquals(MoveType.PASS, MoveType.valueOf("PASS"));
  }

  @Test
  void playerColorShouldExposeAnsiCodesAndMapping() {
    assertNotNull(PlayerColor.BLUE.getAnsiCode());
    assertNotNull(PlayerColor.RED.getAnsiCode());
    assertNotNull(PlayerColor.YELLOW.getAnsiCode());
    assertNotNull(PlayerColor.GREEN.getAnsiCode());
    assertNotNull(PlayerColor.RESET.getAnsiCode());

    assertEquals(PlayerColor.BLUE, PlayerColor.fromIndex(0));
    assertEquals(PlayerColor.RED, PlayerColor.fromIndex(1));
    assertEquals(PlayerColor.YELLOW, PlayerColor.fromIndex(2));
    assertEquals(PlayerColor.GREEN, PlayerColor.fromIndex(3));
    assertEquals(PlayerColor.RESET, PlayerColor.fromIndex(-1));
    assertEquals(PlayerColor.RESET, PlayerColor.fromIndex(99));
  }

  @Test
  void squareTypeShouldExposeCorrectMultipliers() {
    assertEquals(1, SquareType.NORMAL.getLetterMultiplier());
    assertEquals(1, SquareType.NORMAL.getWordMultiplier());

    assertEquals(2, SquareType.DOUBLE_LETTER.getLetterMultiplier());
    assertEquals(1, SquareType.DOUBLE_LETTER.getWordMultiplier());

    assertEquals(3, SquareType.TRIPLE_LETTER.getLetterMultiplier());
    assertEquals(1, SquareType.TRIPLE_LETTER.getWordMultiplier());

    assertEquals(1, SquareType.DOUBLE_WORD.getLetterMultiplier());
    assertEquals(2, SquareType.DOUBLE_WORD.getWordMultiplier());

    assertEquals(1, SquareType.TRIPLE_WORD.getLetterMultiplier());
    assertEquals(3, SquareType.TRIPLE_WORD.getWordMultiplier());
  }
}
