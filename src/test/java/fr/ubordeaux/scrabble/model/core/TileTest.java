package fr.ubordeaux.scrabble.model.core;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class TileTest {

  /**
   * Test that the Tile constructor correctly assigns the character and retrieves its standard point
   * value from the scoring system.
   */
  @Test
  void constructorShouldAssignCharacterAndStandardValue() {
    Tile tile = new Tile('a');

    assertEquals('a', tile.getCharacter());
    assertEquals(1, tile.getValue());
  }

  /**
   * Test that getStandardValue returns the correct Scrabble point values for various letters,
   * including high-value letters (J, Z) and blanks.
   */
  @Test
  void getStandardValueShouldReturnExpectedScores() {
    assertEquals(1, Tile.getStandardValue('E'));
    assertEquals(2, Tile.getStandardValue('D'));
    assertEquals(3, Tile.getStandardValue('C'));
    assertEquals(4, Tile.getStandardValue('F'));
    assertEquals(8, Tile.getStandardValue('J'));
    assertEquals(10, Tile.getStandardValue('Z'));
    assertEquals(0, Tile.getStandardValue(' '));
    assertEquals(0, Tile.getStandardValue('?'));
  }

  /**
   * Test that equals and hashCode methods work correctly based on the tile's character and value,
   * ensuring proper behavior in collections.
   */
  @Test
  void equalsAndHashCodeShouldUseCharacterAndValue() {
    Tile first = new Tile('K');
    Tile same = new Tile('K');
    Tile different = new Tile('L');

    assertEquals(first, same);
    assertEquals(first.hashCode(), same.hashCode());
    assertNotEquals(first, different);
    Tile jokerDisplayedK = new Tile('K', true);
    assertNotEquals(first, jokerDisplayedK);
  }

  @Test
  void jokerConstructorShouldCreateZeroPointTile() {
    Tile jokerAsG = new Tile('G', true);

    assertEquals('G', jokerAsG.getCharacter());
    assertEquals(0, jokerAsG.getValue());
    assertTrue(jokerAsG.isJoker());
  }

  /**
   * Test that the toString method returns the tile's character as a string.
   */
  @Test
  void toStringShouldReturnCharacterAsString() {
    Tile tile = new Tile('Q');

    assertEquals("Q", tile.toString());
  }
}
