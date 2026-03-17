package fr.ubordeaux.scrabble.model.dictionary;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class TrieTest {

  private Trie trie;

  @BeforeEach
  void setUp() {
    trie = new Trie();
  }

  /** Test that adding words updates the counts and allows finding them. */
  @Test
  void addShouldIncreaseWordAndNodeCounts() {
    trie.add("HELLO");

    assertEquals(1, trie.getWordCount());
    assertTrue(trie.contains("HELLO"));
    assertFalse(trie.contains("HELL")); // Not finite yet
  }

  /** Test that addAll correctly processes multiple words. */
  @Test
  void addAllShouldInsertMultipleWords() {
    String[] words = {"CAT", "CAR", "DOG"};
    trie.addAll(words);

    assertEquals(3, trie.getWordCount());
    assertTrue(trie.contains("CAR"));
    assertTrue(trie.contains("DOG"));
  }

  /** Test prefix finding logic. */
  @Test
  void findShouldReturnCorrectNodeForPrefix() {
    trie.add("SCRABBLE");
    Node prefixNode = trie.find("SCRA");

    assertNotNull(prefixNode);
    assertEquals('A', prefixNode.getContent());
    assertNull(trie.find("Z")); // Non-existent prefix
  }

  /** Test the recursive word retrieval. */
  @Test
  void getWordsShouldReturnAllInsertedWords() {
    trie.add("IN");
    trie.add("INK");

    ArrayList<String> words = trie.getWords();

    assertEquals(2, words.size());
    assertTrue(words.contains("IN"));
    assertTrue(words.contains("INK"));
  }

  /** Test that maxDepth is updated correctly during additions. */
  @Test
  void maxDepthShouldTrackLongestWord() {
    trie.add("SHORT");
    assertEquals(5, trie.getMaxDepth());

    trie.add("VERYLONGWORD");
    assertEquals(12, trie.getMaxDepth());
  }
}
