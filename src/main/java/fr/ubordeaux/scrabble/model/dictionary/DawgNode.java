package fr.ubordeaux.scrabble.model.dictionary;

import java.util.Objects;
import java.util.TreeMap;

/**
 * Represents a single node within a DAWG.
 * Each node stores a character and manages transitions to its children.
 */
public class DawgNode {
  private final char content;
  private boolean finite = false;

  /**
   * Map of child nodes indexed by their character content.
   */
  public TreeMap<Character, DawgNode> children = new TreeMap<>();

  /**
   * Initializes a new node with the specified character.
   *
   * @param c The character content of this node.
   */
  public DawgNode(char c) {
    this.content = c;
  }

  /**
   * Retrieves the character stored in this node.
   *
   * @return The character content.
   */
  public char getContent() {
    return content;
  }

  /**
   * Sets whether this node marks the end of a valid word.
   *
   * @param value True if the node is a word terminator, false otherwise.
   */
  public void setFinite(boolean value) {
    finite = value;
  }

  /**
   * Checks if this node marks the end of a valid word.
   *
   * @return True if it is a finite state, false otherwise.
   */
  public boolean getFinite() {
    return finite;
  }

  /**
   * Identifies identical structures for graph minimization.
   * Two nodes are equal if they share the same content, finite status, and
   * children.
   *
   * @param o The object to compare with.
   * @return True if the nodes are structurally identical, false otherwise.
   */
  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    DawgNode node = (DawgNode) o;
    return content == node.content && finite == node.finite && children.equals(node.children);
  }

  /**
   * Generates a hash code based on the node's content, status, and child map.
   *
   * @return The computed hash value.
   */
  @Override
  public int hashCode() {
    return Objects.hash(content, finite, children);
  }
}