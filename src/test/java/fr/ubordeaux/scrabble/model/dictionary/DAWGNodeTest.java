package fr.ubordeaux.scrabble.model.dictionary;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class DAWGNodeTest {

  /**
   * Test that a DAWGNode is correctly initialized with its character and is not finite by default.
   */
  @Test
  void constructorShouldInitializeCorrectly() {
    DAWGNode node = new DAWGNode('A');
    assertEquals('A', node.getContent());
    assertFalse(node.getFinite());
  }

  /**
   * Test that setting the finite flag correctly updates the node's state.
   */
  @Test
  void setFiniteShouldUpdateState() {
    DAWGNode node = new DAWGNode('B');
    node.setFinite(true);
    assertTrue(node.getFinite());
  }

  /**
   * Test that two nodes are considered equal if they have the same content, same finite status, and
   * identical children. This is critical for DAWG minimization.
   */
  @Test
  void equalsShouldIdentifyIdenticalStructures() {
    DAWGNode node1 = new DAWGNode('C');
    DAWGNode node2 = new DAWGNode('C');

    // Initial state: same content, both non-finite, no children
    assertEquals(node1, node2);
    assertEquals(node1.hashCode(), node2.hashCode());

    // Different finite status
    node1.setFinite(true);
    assertNotEquals(node1, node2);

    // Same finite status but different children
    node2.setFinite(true);
    node1.children.put('D', new DAWGNode('D'));
    assertNotEquals(node1, node2);

    // Same children
    node2.children.put('D', new DAWGNode('D'));
    assertEquals(node1, node2);
  }
}
