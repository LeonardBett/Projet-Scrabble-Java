package fr.ubordeaux.scrabble.model.dictionary;

import java.util.Collection;
import java.util.TreeMap;

/**
 * Represents a single node in a prefix tree (Trie) or GADDAG.
 * Each node stores a character and maintains a sorted map of its children.
 */
public class Node implements Comparable<Node> {
  /**
   * The default character representation for the root node.
   */
  public static char root = '~';

  private final char content;
  private boolean finite = false;

  /**
   * Map of child nodes indexed by their character content.
   */
  public TreeMap<Character, Node> children = new TreeMap<>();

  /**
   * Retrieves the character content of this node.
   *
   * @return the character stored in the node.
   */
  public char getContent() {
    return content;
  }

  /**
   * Sets whether this node represents the end of a valid word.
   *
   * @param value true if this node is a word terminator, false otherwise.
   */
  public void setFinite(boolean value) {
    finite = value;
  }

  /**
   * Checks if this node marks the end of a valid word.
   *
   * @return true if the node is finite, false otherwise.
   */
  public boolean getFinite() {
    return finite;
  }

  /**
   * Initializes a new node with the given character.
   *
   * @param c the character to be stored in this node.
   */
  public Node(char c) {
    this.content = c;
    this.finite = false;
  }

  /**
   * Returns a collection of characters representing the keys of all child nodes.
   *
   * @return a collection of characters for children transitions.
   */
  public Collection<Character> getKeys() {
    return children.keySet();
  }

  /**
   * Retrieves all child nodes connected to this node.
   *
   * @return a collection of child Node instances.
   */
  public Collection<Node> getChildren() {
    return children.values();
  }

  /**
   * Verifies if this node has a specific child character.
   *
   * @param c2 the character to check for.
   * @return true if the child exists, false otherwise.
   */
  public boolean hasChild(char c2) {
    return children.containsKey(c2);
  }

  /**
   * Retrieves the child node associated with a specific character.
   *
   * @param c the character of the child to retrieve.
   * @return the corresponding Node instance, or null if not found.
   */
  public Node getChild(char c) {
    return children.get(c);
  }

  /**
   * Adds a new child node to this node's transitions.
   *
   * @param c2 the character content of the new child.
   */
  public void addChild(char c2) {
    children.put(c2, new Node(c2));
  }

  /**
   * Provides a string representation of the node's character.
   *
   * @return the character held by the node as a string.
   */
  @Override
  public String toString() {
    return String.valueOf(content);
  }

  /**
   * Compares this node with another object for equality based on character
   * content.
   *
   * @param o the object to compare against.
   * @return true if the other object is a Node with the same character content.
   */
  @Override
  public boolean equals(Object o) {
    return o instanceof Node && content == ((Node) o).getContent();
  }

  /**
   * Compares this node to another based on alphabetical order of their
   * characters.
   *
   * @param o the other node to compare against.
   * @return a negative, zero, or positive integer based on comparison.
   */
  @Override
  public int compareTo(Node o) {
    return content - o.content;
  }
}