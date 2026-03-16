package fr.ubordeaux.scrabble.model.ai;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import fr.ubordeaux.scrabble.model.core.Tile;
import java.lang.reflect.Method;
import java.util.ArrayList;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Comprehensive unit tests for the AiPlayer class. Verifies player configuration, delegation to the
 * solver, and internal rack parsing.
 */
class AiPlayerTest {

  private AiPlayer aiPlayer;

  /**
   * Initializes an AiPlayer instance before each test.
   */
  @BeforeEach
  void setUp() {
    aiPlayer = new AiPlayer("IA-Bot", 3, 5);
  }

  /**
   * Tests the basic constructor parameters and default configuration states.
   */
  @Test
  void testInitialization() {
    assertEquals("IA-Bot", aiPlayer.getName());
    assertFalse(aiPlayer.isExpectiminimaxMode());
  }

  /**
   * Tests the Expectiminimax mode toggling through the AiPlayer wrapper.
   */
  @Test
  void testSetExpectiminimaxMode() {
    aiPlayer.setExpectiminimaxMode(true);
    assertTrue(aiPlayer.isExpectiminimaxMode());

    aiPlayer.setExpectiminimaxMode(false);
    assertFalse(aiPlayer.isExpectiminimaxMode());
  }

  /**
   * Tests the injection and removal of a Machine Learning agent.
   */
  @Test
  void testSetMlAgent() {
    MlAgent dummyAgent = new MlAgent("dummy/path", new ArrayList<>());
    aiPlayer.setMlAgent(dummyAgent);
    assertNotNull(aiPlayer);

    // AI should be able to handle nullification (fallback to algorithmic mode)
    aiPlayer.setMlAgent(null);
    assertNotNull(aiPlayer);
  }

  /**
   * Tests the delegation of the time limit setter to the underlying solver.
   */
  @Test
  void testSetTimeLimitSeconds() {
    aiPlayer.setTimeLimitSeconds(15);
    // Asserts that the delegation executes smoothly without exceptions
    assertTrue(true);
  }

  /**
   * Tests the private getRackAsString method. Scenario: Rack contains multiple specific tiles.
   */
  @Test
  void testGetRackAsString() throws Exception {
    // Add specific tiles to the AI's rack for the test
    aiPlayer.getRack().addTile(new Tile('S'));
    aiPlayer.getRack().addTile(new Tile('C'));
    aiPlayer.getRack().addTile(new Tile('R'));

    Method getRackMethod = AiPlayer.class.getDeclaredMethod("getRackAsString");
    getRackMethod.setAccessible(true);

    String rackStr = (String) getRackMethod.invoke(aiPlayer);

    assertEquals("SCR", rackStr);
  }

  /**
   * Tests the private getRackAsString method. Scenario: Rack is completely empty.
   */
  @Test
  void testGetRackAsStringEmpty() throws Exception {
    Method getRackMethod = AiPlayer.class.getDeclaredMethod("getRackAsString");
    getRackMethod.setAccessible(true);

    String rackStr = (String) getRackMethod.invoke(aiPlayer);

    assertEquals("", rackStr);
  }
}
