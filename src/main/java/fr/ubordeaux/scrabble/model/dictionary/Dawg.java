package fr.ubordeaux.scrabble.model.dictionary;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class Dawg {
  private final DawgNode root = new DawgNode('~');
  private String lastWord = "";

  // List of nodes not yet minimized for the current word being processed
  private List<DawgNode> uncheckedNodes = new ArrayList<>();

  // Registry of unique minimized nodes (the heart of the Dawg's memory
  // efficiency)
  private Map<DawgNode, DawgNode> minimizedNodes = new HashMap<>();

  public Dawg() {
    uncheckedNodes.add(root);
  }

  /**
   * Adds a word to the Dawg. WARNING: Words must be added in alphabetical order for correct
   * minimization.
   */
  public void add(String word) {
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
    minimize(0);
  }

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
   * Searches for all words constructible with a given rack. If a hook is specified, it is added to
   * the rack and the word MUST contain it.
   */
  public Set<String> findWordsWithRackAndHook(Character[] rack, char hook) {
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
      return results;
    }
    return allFound;
  }

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
