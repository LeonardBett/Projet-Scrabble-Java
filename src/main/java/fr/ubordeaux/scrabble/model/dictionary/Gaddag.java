package fr.ubordeaux.scrabble.model.dictionary;

import fr.ubordeaux.scrabble.model.utils.GameLogger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Objects;

/**
 * Implementation of a GADDAG data structure for Scrabble word generation.
 * It stores word rotations in a Trie to allow searching for words from any anchor point.
 */
public class Gaddag extends Trie implements Dictionary {
  private static final char separator = '>';

  /**
   * Initializes a new GADDAG with a root node.
   */
  public Gaddag() {
    root = new Node(Node.root);
  }

  /**
   * Represents a word found in the GADDAG along with its internal path.
   */
  public static class GaddagResult {
    /**
     * The literal word found.
     */
    public final String word;

    /**
     * The specific path used to find it in the graph.
     */
    public final String gaddagPath;

    /**
     * Creates a result entry for a found word.
     *
     * @param word The literal word found.
     * @param gaddagPath The specific path used to find it in the graph.
     */
    public GaddagResult(String word, String gaddagPath) {
      this.word = word;
      this.gaddagPath = gaddagPath;
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) {
        return true;
      }
      if (o == null || getClass() != o.getClass()) {
        return false;
      }
      GaddagResult that = (GaddagResult) o;
      return word.equals(that.word) && gaddagPath.equals(that.gaddagPath);
    }

    @Override
    public int hashCode() {
      return Objects.hash(word, gaddagPath);
    }

    @Override
    public String toString() {
      return this.word;
    }
  }

  /**
   * Adds a word to the GADDAG by inserting all its necessary rotations into the underlying Trie.
   *
   * @param word The word to add to the dictionary.
   */
  @Override
  public void add(String word) {
    if (word.isEmpty()) {
      GameLogger.logDebug("GADDAG: adding word \"" + word.toUpperCase() + "\"");
      return;
    }

    word = word.toUpperCase();

    String prefix;
    char[] ch;
    int i;
    for (i = 1; i < word.length(); i++) {
      prefix = word.substring(0, i);
      ch = prefix.toCharArray();
      reverse(ch);
      super.add(new String(ch) + separator + word.substring(i));
    }
    ch = word.toCharArray();
    reverse(ch);
    super.add(new String(ch) + separator + word.substring(i));
  }

  /**
   * Utility method to reverse a character array in place.
   *
   * @param validData The character array to be reversed.
   */
  private void reverse(char[] validData) {
    for (int i = 0; i < validData.length / 2; i++) {
      int temp = validData[i];
      validData[i] = validData[validData.length - i - 1];
      validData[validData.length - i - 1] = (char) temp;
    }
  }

  /**
   * Finds all valid words from the rack and hook, returned as plain strings (F27 interface).
   *
   * @param rack The player's available letters.
   * @param hook The mandatory anchor letter on the board (use ' ' if no hook).
   * @return A {@link java.util.Set} of valid uppercase word strings.
   */
  @Override
  public java.util.Set<String> findWordsWithRackAndHook(Character[] rack, char hook) {
    java.util.Set<String> result = new HashSet<>();
    for (GaddagResult gr : findGaddagResults(rack, hook)) {
      result.add(gr.word);
    }
    return result;
  }

  /**
   * Finds all words that can be formed using the provided rack letters and a specific hook tile.
   * Returns full {@link GaddagResult} objects including the internal GADDAG path.
   *
   * @param rack The player's available letters.
   * @param hook The mandatory anchor letter on the board (use ' ' if no hook).
   * @return A set of GaddagResult containing valid words and their GADDAG paths.
   */
  public HashSet<GaddagResult> findGaddagResults(Character[] rack, char hook) {
    GameLogger.logVerbose("GADDAG: searching words for rack=" + Arrays.toString(rack)
        + " hook='" + hook + "'");
    HashSet<GaddagResult> words = new HashSet<>();
    Arrays.sort(rack);
    ArrayList<Character> rackList = new ArrayList<>(Arrays.asList(rack));

    if (hook == ' ') {
      char tile;
      while (rackList.size() > 1) {
        tile = rackList.removeFirst();
        findWordsRecurse(words, "", "", rackList, tile, root, true);
      }
    } else {
      findWordsRecurse(words, "", "", rackList, hook, root, true);
    }
    GameLogger.logVerbose("GADDAG: found " + words.size() + " word(s).");
    return words;
  }

  /**
   * Recursively explores the GADDAG to build words based on the current rack and direction.
   *
   * @param words The collection of results being populated.
   * @param word The word string built so far.
   * @param gaddagPath The path traversed in the graph.
   * @param rack The remaining available letters.
   * @param hook The current character to match in the graph.
   * @param cur The current node in the Trie.
   * @param direction The current building direction (true for prefix/left, false for suffix/right).
   */
  private void findWordsRecurse(HashSet<GaddagResult> words, String word, String gaddagPath,
                                ArrayList<Character> rack, char hook, Node cur, boolean direction) {
    Node hookNode = cur.getChild(hook);

    if (hookNode == null) {
      return;
    }

    String hookCh = hook == separator ? "" : String.valueOf(hook);
    word = (direction ? hookCh + word : word + hookCh);

    gaddagPath = gaddagPath + hook;

    if (hookNode.getFinite()) {
      words.add(new GaddagResult(word, gaddagPath));
    }

    for (char nodeKey : hookNode.getKeys()) {
      if (nodeKey == separator) {
        findWordsRecurse(words, word, gaddagPath, rack, separator, hookNode, false);
      } else if (rack.contains(nodeKey)) {
        ArrayList<Character> newRack = (ArrayList<Character>) rack.clone();
        newRack.remove((Character) nodeKey);
        findWordsRecurse(words, word, gaddagPath, newRack, nodeKey, hookNode, direction);
      }
    }
  }

  /**
   * Checks if a word or a GADDAG path exists in the dictionary.
   * If the string contains the separator, it performs a direct search.
   *
   * @param word The word or path to verify.
   * @return True if found, false otherwise.
   */
  @Override
  public boolean contains(String word) {
    if (word == null) {
      return false;
    }

    // If the string already contains the separator (e.g., "H>I" in tests),
    // perform a direct lookup in the trie (super.contains)
    if (word.indexOf(separator) != -1) {
      return super.contains(word.toUpperCase());
    }

    // Otherwise, treat the string as a natural word that must be rotated
    return containsWord(word);
  }

  /**
   * Checks if a word exists in the dictionary by converting it to a GADDAG path.
   *
   * @param word The word to verify (e.g., "HI").
   * @return True if the rotated path (e.g., "H>I") exists in the Trie.
   */
  public boolean containsWord(String word) {
    if (word == null || word.isEmpty()) {
      return false;
    }

    // Single-letter words are stored as "A>".
    if (word.length() < 2) {
      return super.contains(word.toUpperCase() + separator);
    }

    String upperWord = word.toUpperCase();
    char firstLetter = upperWord.charAt(0);

    // Transform "HI" into internal path "H>I"
    String gaddagPath = String.valueOf(firstLetter) + separator + upperWord.substring(1);

    // Direct path lookup in the parent trie to avoid recursion
    return super.contains(gaddagPath);
  }
}