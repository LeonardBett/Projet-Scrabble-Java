package fr.ubordeaux.scrabble.model.dictionary;


import fr.ubordeaux.scrabble.model.utils.GameLogger;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Directed Acyclic Word Graph (DAWG) implementation for space-efficient dictionary storage.
 * It optimizes memory by merging nodes with identical suffixes.
 * An alternative of GADDAG.
 */
public class Dawg implements Dictionary {
  private final DawgNode root = new DawgNode('~');
  private String lastWord = "";

  // List of nodes not yet minimized for the current word being processed
  private List<DawgNode> uncheckedNodes = new ArrayList<>();

  // Registry of unique minimized nodes (the heart of the Dawg's memory
  // efficiency)
  private Map<DawgNode, DawgNode> minimizedNodes = new HashMap<>();

  /**
   * Initializes a new Dawg structure with an empty root node.
   */
  public Dawg() {
    uncheckedNodes.add(root);
  }

  /**
   * Adds a word to the Dawg. Words must be added in alphabetical order
   * to ensure proper minimization.
   *
   * @param word The word to insert into the graph.
   */
  public void add(String word) {
    GameLogger.logDebug("DAWG: adding word \"" + word.toUpperCase() + "\"");
    word = word.toUpperCase();

    // Find the longest common prefix with the previously added word
    int commonPrefix = 0;
    for (int i = 0; i < Math.min(word.length(), lastWord.length()); i++) {
      if (word.charAt(i) != lastWord.charAt(i)) {
        break;
      }
      commonPrefix++;
    }

    // Minimize nodes from the end of the previous word up to the common prefix
    minimize(commonPrefix);

    // Add the suffix of the new word starting from the common prefix
    DawgNode node = uncheckedNodes.get(commonPrefix);
    for (int i = commonPrefix; i < word.length(); i++) {
      DawgNode nextNode = new DawgNode(word.charAt(i));
      node.children.put(word.charAt(i), nextNode);
      uncheckedNodes.add(nextNode);
      node = nextNode;
    }

    node.setFinite(true);
    lastWord = word;
  }

  /**
   * Finalizes the Dawg. Must be called after the last word is added.
   */
  public void finish() {
    GameLogger.logVerbose("DAWG: minimization complete.");
    minimize(0);
  }

  /**
   * Reduces memory usage by merging redundant nodes with identical transitions.
   *
   * @param lowerBound The depth index to traverse back to for minimization.
   */
  private void minimize(int lowerBound) {
    // Traverse backward from the end of the list to the requested bound
    for (int i = uncheckedNodes.size() - 1; i > lowerBound; i--) {
      DawgNode child = uncheckedNodes.remove(i);
      DawgNode parent = uncheckedNodes.get(i - 1);
      char charToChild = child.getContent();

      if (minimizedNodes.containsKey(child)) {
        // If an identical node already exists, point the parent to the existing one
        parent.children.put(charToChild, minimizedNodes.get(child));
      } else {
        // Otherwise, register this node as a reference for future potential merges
        minimizedNodes.put(child, child);
      }
    }
  }

  /**
   * Verifies if a specific word exists in the dictionary by traversing the graph.
   *
   * @param word The word to search for.
   * @return True if the word is valid and finite, false otherwise.
   */
  public boolean contains(String word) {
    DawgNode node = root;
    for (char c : word.toUpperCase().toCharArray()) {
      node = node.children.get(c);
      if (node == null) {
        return false;
      }
    }
    return node.getFinite();
  }

  /**
   * Finds all valid words formable from a set of letters and a required anchor letter.
   *
   * @param rack The player's available letters.
   * @param hook The mandatory letter already present on the board.
   * @return A set of all constructible words containing the hook.
   */
  public Set<String> findWordsWithRackAndHook(Character[] rack, char hook) {
    GameLogger.logVerbose("DAWG: searching words for rack=" + Arrays.toString(rack) + " hook='" + hook + "'");
    Set<String> results = new HashSet<>();
    List<Character> availableLetters = new ArrayList<>(Arrays.asList(rack));

    // If a hook is provided, add it to the available letters for backtracking
    if (hook != ' ') {
      availableLetters.add(hook);
    }

    Set<String> allFound = new HashSet<>();
    backtrack(root, "", availableLetters, allFound);

    // Filter results: only keep words that actually use the hook letter
    if (hook != ' ') {
      for (String word : allFound) {
        if (word.indexOf(hook) != -1) {
          results.add(word);
        }
      }
      GameLogger.logVerbose("DAWG: found " + results.size() + " word(s).");
      return results;
    }
    return allFound;
  }

  /**
   * Recursively explores the graph using backtracking to find constructible words.
   *
   * @param node        The current graph node.
   * @param currentWord The prefix built so far.
   * @param rack        The remaining letters available in the rack.
   * @param results     The collection of valid words found.
   */
  private void backtrack(DawgNode node, String currentWord, List<Character> rack,
                         Set<String> results) {
    if (node.getFinite()) {
      results.add(currentWord);
    }

    for (Character c : node.children.keySet()) {
      int index = rack.indexOf(c);
      if (index != -1) {
        // Letter found in rack: descend into the graph
        rack.remove(index);
        backtrack(node.children.get(c), currentWord + c, rack, results);
        rack.add(index, c); // Backtrack: return letter to the rack
      } else {
        // Letter not in rack: try using a Joker (space character)
        int jokerIndex = rack.indexOf(' ');
        if (jokerIndex != -1) {
          rack.remove(jokerIndex);
          backtrack(node.children.get(c), currentWord + c, rack, results);
          rack.add(jokerIndex, ' '); // Backtrack: return Joker to the rack
        }
      }
    }
  }
}