package fr.ubordeaux.scrabble.model.dictionary;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class DawgNodeTest {

  /**
   * Test that a DawgNode is correctly initialized with its character and is not finite by default.
   */
  @Test
  void constructorShouldInitializeCorrectly() {
    DawgNode node = new DawgNode('A');
    assertEquals('A', node.getContent());
    assertFalse(node.getFinite());
  }

  /**
   * Test that setting the finite flag correctly updates the node's state.
   */
  @Test
  void setFiniteShouldUpdateState() {
    DawgNode node = new DawgNode('B');
    node.setFinite(true);
    assertTrue(node.getFinite());
  }

  /**
   * Test that two nodes are considered equal if they have the same content, same finite status, and
   * identical children. This is critical for Dawg minimization.
   */
  @Test
  void equalsShouldIdentifyIdenticalStructures() {
    DawgNode node1 = new DawgNode('C');
    DawgNode node2 = new DawgNode('C');

    // Initial state: same content, both non-finite, no children
    assertEquals(node1, node2);
    assertEquals(node1.hashCode(), node2.hashCode());

    // Different finite status
    node1.setFinite(true);
    assertNotEquals(node1, node2);

    // Same finite status but different children
    node2.setFinite(true);
    node1.children.put('D', new DawgNode('D'));
    assertNotEquals(node1, node2);

    // Same children
    node2.children.put('D', new DawgNode('D'));
    assertEquals(node1, node2);
  }
}
