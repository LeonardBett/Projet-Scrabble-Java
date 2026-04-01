package fr.ubordeaux.scrabble.model.dictionary;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class GaddagTest {

  private Gaddag gaddag;

  @BeforeEach
  void setUp() {
    gaddag = new Gaddag();
  }

  /**
   * Test that containsWord correctly identifies valid words in the dictionary and rejects
   * non-existent ones.
   */
  @Test
  void containsWordShouldReturnTrueForValidWords() {
    gaddag.add("APPLE");
    gaddag.add("BANANA");

    assertTrue(gaddag.containsWord("APPLE"));
    assertTrue(gaddag.containsWord("BANANA"));
    assertFalse(gaddag.containsWord("ORANGE"));
  }

  /**
   * Test that the Gaddag internal structure creates the necessary rotated paths for a word to allow
   * bidirectional search.
   */
  @Test
  void addShouldCreateInternalGaddagPaths() {
    // For the word "HI", Gaddag logic adds: "H>I" and "IH>"
    gaddag.add("HI");

    assertTrue(gaddag.contains("H>I"));
    assertTrue(gaddag.contains("IH>"));
  }

  /**
   * Test that findWordsWithRackAndHook returns the correct words that can be formed using a
   * specific rack and a mandatory hook tile on the board.
   */
  @Test
  void findWordsWithRackAndHookShouldReturnFormableWords() {
    gaddag.add("CAT");
    gaddag.add("CAR");
    gaddag.add("DOG");

    Character[] rack = {'A', 'T', 'R'};
    char hook = 'C';

    // Correction : La méthode retourne maintenant un Set<String>
    Set<String> results = gaddag.findWordsWithRackAndHook(rack, hook);

    assertNotNull(results);
    assertEquals(2, results.size());
    assertTrue(results.contains("CAT"));
    assertTrue(results.contains("CAR"));
    assertFalse(results.contains("DOG"));
  }

  /**
   * Test that the search is case-insensitive by ensuring words added in lower case are found when
   * searched in upper case.
   */
  @Test
  void containsWordShouldBeCaseInsensitive() {
    gaddag.add("apple");

    assertTrue(gaddag.containsWord("APPLE"));
    assertTrue(gaddag.containsWord("apple"));
  }

  /**
   * Test that findWordsWithRackAndHook correctly handles an empty rack by only returning the hook
   * if it is a valid 1-letter word (if applicable).
   */
  @Test
  void findWordsWithEmptyRackAndNoValidWordShouldReturnEmpty() {
    gaddag.add("APPLE");
    Character[] emptyRack = {};

    // Correction : Utilisation de Set<String>
    Set<String> results = gaddag.findWordsWithRackAndHook(emptyRack, 'Z');

    assertTrue(results.isEmpty());
  }

  /**
   * Test the equals and hashCode of GaddagResult to ensure that HashSet correctly handles duplicate
   * results.
   */
  @Test
  void gaddagResultEqualsShouldVerifyWordAndPath() {
    Gaddag.GaddagResult res1 = new Gaddag.GaddagResult("CAT", "C>AT");
    Gaddag.GaddagResult res2 = new Gaddag.GaddagResult("CAT", "C>AT");
    Gaddag.GaddagResult res3 = new Gaddag.GaddagResult("CAT", "TC>");

    assertEquals(res1, res2);
    assertFalse(res1.equals(res3));
    assertEquals(res1.hashCode(), res2.hashCode());
  }
}