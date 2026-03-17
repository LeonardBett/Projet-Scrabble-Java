package fr.ubordeaux.scrabble.model.dictionary;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class NodeTest {

  /** Test that a node is correctly initialized with a character and is not finite by default. */
  @Test
  void nodeShouldInitializeCorrectly() {
    Node node = new Node('A');
    assertEquals('A', node.getContent());
    assertFalse(node.getFinite());
  }

  /** Test that adding and retrieving children works correctly. */
  @Test
  void nodeShouldManageChildrenCorrectly() {
    Node parent = new Node('P');
    parent.addChild('C');

    assertTrue(parent.hasChild('C'));
    assertNotNull(parent.getChild('C'));
    assertEquals('C', parent.getChild('C').getContent());
  }

  /** Test equality between nodes based on their content. */
  @Test
  void nodesWithSameContentShouldBeEqual() {
    Node n1 = new Node('X');
    Node n2 = new Node('X');
    Node n3 = new Node('Y');

    assertEquals(n1, n2);
    assertNotEquals(n1, n3);
  }
}
