package fr.ubordeaux.scrabble.model.dictionary;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class DawgTest {

  private Dawg dawg;

  @BeforeEach
  void setUp() {
    dawg = new Dawg();
  }

  /**
   * Test that words added in alphabetical order are correctly stored and can be
   * retrieved using
   * contains.
   */
  @Test
  void addAndContainsShouldWorkWithAlphabeticalOrder() {
    dawg.add("APPLE");
    dawg.add("APPLY");
    dawg.add("BANANA");
    dawg.finish(); // Crucial to minimize the last word

    assertTrue(dawg.contains("APPLE"));
    assertTrue(dawg.contains("APPLY"));
    assertTrue(dawg.contains("BANANA"));
    assertFalse(dawg.contains("ORANGE"));
  }

  /**
   * Test that findWordsWithRackAndHook finds all valid words formable from a set
   * of letters.
   */
  @Test
  void findWordsWithRackShouldReturnAllFormableWords() {
    dawg.add("CAT");
    dawg.add("CAR");
    dawg.add("DOG");
    dawg.finish();

    Character[] rack = { 'A', 'T', 'C', 'R' };
    Set<String> results = dawg.findWordsWithRackAndHook(rack, ' ');

    assertTrue(results.contains("CAT"));
    assertTrue(results.contains("CAR"));
    assertFalse(results.contains("DOG"));
  }

  /**
   * Test that the hook constraint is respected: only words containing the hook
   * letter should be
   * returned.
   */
  @Test
  void findWordsWithHookShouldRespectConstraint() {
    dawg.add("CAT");
    dawg.add("ACT");
    dawg.add("BAT");
    dawg.finish();

    Character[] rack = { 'A', 'T' };
    char hook = 'C';

    Set<String> results = dawg.findWordsWithRackAndHook(rack, hook);

    assertTrue(results.contains("CAT"));
    assertTrue(results.contains("ACT"));
    assertFalse(results.contains("BAT")); // BAT does not contain the hook 'C'
  }

  /**
   * Test that the Joker (represented by a space ' ') can replace any letter to
   * form words.
   */
  @Test
  void findWordsWithJokerShouldSubstituteAnyLetter() {
    dawg.add("CAKE");
    dawg.finish();

    // Rack has 'C', 'A', 'K' and a Joker ' '
    Character[] rack = { 'C', 'A', 'K', ' ' };
    Set<String> results = dawg.findWordsWithRackAndHook(rack, ' ');

    assertTrue(results.contains("CAKE")); // Joker became 'E'
  }

  /**
   * Test that searching for a word not in the dictionary returns false.
   */
  @Test
  void containsShouldReturnFalseForNonExistentWord() {
    dawg.add("TEST");
    dawg.finish();

    assertFalse(dawg.contains("TE")); // Prefix but not a word
    assertFalse(dawg.contains("TESTS")); // Suffix not added
  }
}
