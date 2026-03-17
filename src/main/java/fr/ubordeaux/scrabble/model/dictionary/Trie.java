package fr.ubordeaux.scrabble.model.dictionary;

import java.util.ArrayList;

/**
 * Implementation of a prefix tree (Trie) for efficient word storage and prefix searching.
 */
public class Trie {
  protected Node root;
  protected long nodeCount;
  protected int wordCount;
  protected short maxDepth;

  /**
   * Retrieves the root node of the trie.
   *
   * @return the root Node instance.
   */
  public Node getRoot() {
    return root;
  }

  /**
   * Returns the total number of words stored in the trie.
   *
   * @return total word count as an integer.
   */
  public int getWordCount() {
    return wordCount;
  }

  /**
   * Returns the length of the longest word stored in the trie.
   *
   * @return maximum word depth as a short.
   */
  public short getMaxDepth() {
    return maxDepth;
  }

  /**
   * Initializes an empty trie with a root node.
   */
  public Trie() {
    this.root = new Node(Node.root);
    this.nodeCount = 1;
    this.wordCount = 0;
    this.maxDepth = 0;
  }

  /**
   * Adds multiple words to the trie from a provided array.
   *
   * @param words an array of strings to be added.
   */
  public void addAll(String[] words) {
    for (String word : words) {
      this.add(word);
    }
  }

  /**
   * Adds a single word to the trie, creating new nodes as necessary.
   *
   * @param word the string to insert into the trie.
   */
  public void add(String word) {
    if (word.isEmpty()) {
      return;
    }
    char[] characters = word.toUpperCase().toCharArray();
    Node current = this.root;

    for (char character : characters) {
      if (current.hasChild(character)) {
        current = current.getChild(character);
      } else {
        current.addChild(character);
        this.nodeCount++;
        current = current.getChild(character);
      }
    }

    current.setFinite(true);

    if (characters.length > maxDepth) {
      this.maxDepth = (short) characters.length;
    }
    this.wordCount++;
  }

  /**
   * Verifies if a complete word exists in the trie.
   *
   * @param word the string to check for.
   * @return true if the full word exists, false otherwise.
   */
  public boolean contains(String word) {
    char[] characters = word.toUpperCase().toCharArray();
    Node tmp = root;

    for (char character : characters) {
      if (tmp.hasChild(character)) {
        tmp = tmp.getChild(character);
      } else {
        return false;
      }
    }

    return tmp.getFinite();
  }

  /**
   * Locates the terminal node corresponding to a specific prefix.
   *
   * @param prefix the string prefix to search for.
   * @return the ending Node of the prefix, or null if not found.
   */
  public Node find(String prefix) {
    char[] characters = prefix.toUpperCase().toCharArray();

    Node tmp = root;
    for (char character : characters) {
      if (tmp.hasChild(character)) {
        tmp = tmp.getChild(character);
      } else {
        return null;
      }
    }

    return tmp;
  }

  /**
   * Retrieves all valid words stored in the trie.
   *
   * @return an ArrayList of all complete words.
   */
  public ArrayList<String> getWords() {
    ArrayList<String> words = new ArrayList<>();
    dig("", root, words);
    return words;
  }

  /**
   * Recursively traverses the trie nodes to collect all finite words.
   *
   * @param word the accumulated string from previous nodes.
   * @param cur the current node being explored.
   * @param words the collection being populated with found words.
   */
  private void dig(String word, Node cur, ArrayList<String> words) {
    if (cur.getFinite()) {
      words.add(word);
    }

    if (cur.getChildren() != null) {
      for (Node node : cur.getChildren()) {
        dig(word + node.getContent(), node, words);
      }
    }
  }
}