package fr.ubordeaux.scrabble.model.dictionary;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Objects;

public class Gaddag extends Trie {
  private static final char separator = '>';

  public Gaddag() {
    root = new Node(Node.root);
  }

  public static class GaddagResult {
    public final String word;
    public final String gaddagPath;

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
      return this.word; // Remplace 'word' par le nom exact de ta variable de texte
    }
  }

  @Override
  public void add(String word) {
    if (word.isEmpty()) {
      return;
    }

    word = word.toUpperCase();

    String prefix;
    char[] ch;
    int i;
    for (i = 1; i < word.length(); i++) {
      prefix = word.substring(0, i);
      // Get a substring from index 0 with increasing length i.
      // Start from 1 because a hook is required.
      ch = prefix.toCharArray();
      reverse(ch); // reverse the prefix in order to respect Gaddag spec
      super.add(new String(ch) + separator + word.substring(i));
    }
    ch = word.toCharArray();
    reverse(ch);
    // For the last letter, reverse the full word.
    super.add(new String(ch) + separator + word.substring(i));
  }

  private void reverse(char[] validData) {
    for (int i = 0; i < validData.length / 2; i++) {
      int temp = validData[i];
      validData[i] = validData[validData.length - i - 1];
      validData[validData.length - i - 1] = (char) temp;
    }
  }

  public HashSet<GaddagResult> findWordsWithRackAndHook(Character[] rack, char hook) {
    HashSet<GaddagResult> words = new HashSet<>();
    Arrays.sort(rack);
    ArrayList<Character> rackList = new ArrayList<>(Arrays.asList(rack));

    if (hook == ' ') {
      char tile;
      while (rackList.size() > 1) {
        tile = rackList.removeFirst();
        // We initialise the gaddagPath with ""
        findWordsRecurse(words, "", "", rackList, tile, root, true);
      }
    } else {
      findWordsRecurse(words, "", "", rackList, hook, root, true);
    }
    return words;
  }

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
   * Check if a word is in the dictionary with gaddag.
   */
  public boolean containsWord(String word) {
    if (word == null || word.length() < 2) {
      // Check for a one-letter word
      return this.contains(word.toUpperCase());
    }

    String upperWord = word.toUpperCase();
    char firstLetter = upperWord.charAt(0);

    // We build the gaddag path : FirstLetter + > + rest
    String gaddagPath =
        new String(String.valueOf(firstLetter)) + separator + upperWord.substring(1);

    // We check with contains
    return this.contains(gaddagPath);
  }
}
