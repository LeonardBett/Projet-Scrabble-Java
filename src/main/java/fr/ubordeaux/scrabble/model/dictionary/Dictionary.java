package fr.ubordeaux.scrabble.model.dictionary;

import fr.ubordeaux.scrabble.model.core.Tile;
import java.util.List;
import java.util.Set;

/**
 * Abstract interface for dictionary modules (Requirement F27).
 *
 * <p>Defines the contract that all dictionary implementations ({@link Dawg}, {@link Gaddag})
 * must respect: adding words, checking validity, and finding playable words from a rack.
 */
public interface Dictionary {

  /**
   * Adds a word to the dictionary structure.
   *
   * @param word The word to insert.
   */
  void add(String word);

  /**
   * Adds multiple words to the dictionary.
   *
   * @param words An array of words to insert.
   */
  default void addAll(String[] words) {
    for (String word : words) {
      add(word);
    }
  }

  /**
   * Checks whether a given word exists in the dictionary.
   *
   * <p>Implementations must normalize the word to uppercase before lookup.
   *
   * @param word The word to check.
   * @return {@code true} if the word is valid, {@code false} otherwise.
   */
  boolean contains(String word);

  /**
   * Finds all valid words constructible from the given rack letters and an optional anchor letter.
   *
   * <p>The returned set contains plain uppercase word strings.
   * If {@code hook} is {@code ' '} (space), no anchor constraint is applied.
   *
   * @param rack The player's available letters. A space ({@code ' '}) represents a blank tile.
   * @param hook The mandatory anchor letter already on the board, or {@code ' '} if none.
   * @return A {@link Set} of valid uppercase words that can be formed.
   */
  Set<String> findWordsWithRackAndHook(Character[] rack, char hook);

  /**
   * Calculates the raw score of a word based solely on tile values,
   * without applying any board bonuses (letter/word multipliers).
   *
   * @param tiles The tiles composing the word, in order.
   * @return The raw score of the word.
   */
  default int calculateRawWordScore(List<Tile> tiles) {
    if (tiles == null || tiles.isEmpty()) {
      throw new IllegalArgumentException("Tiles list cannot be null or empty");
    }

    int score = 0;
    for (Tile tile : tiles) {
      score += tile.getValue();
    }

    return score;
  }
}
