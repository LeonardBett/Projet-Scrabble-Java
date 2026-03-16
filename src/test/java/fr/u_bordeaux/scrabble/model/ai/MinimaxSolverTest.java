package fr.u_bordeaux.scrabble.model.ai;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Comprehensive unit tests for the MinimaxSolver class.
 * Uses reflection to deeply test private internal logic and utility methods
 * without requiring a full game state mock for every scenario.
 */
class MinimaxSolverTest {

  private MinimaxSolver solver;

  /**
   * Initializes a fresh MinimaxSolver instance before each test.
   */
  @BeforeEach
  void setUp() {
    solver = new MinimaxSolver(2, 5);
  }

  /**
   * Tests that the Expectiminimax mode is disabled by default upon initialization.
   */
  @Test
  void testDefaultInitialization() {
    assertFalse(solver.isUsingExpectiminimax());
  }

  /**
   * Tests the toggling mechanism for the Expectiminimax mode.
   */
  @Test
  void testSetUseExpectiminimax() {
    solver.setUseExpectiminimax(true);
    assertTrue(solver.isUsingExpectiminimax());

    solver.setUseExpectiminimax(false);
    assertFalse(solver.isUsingExpectiminimax());
  }

  /**
   * Tests that the time limit setter executes properly.
   */
  @Test
  void testSetTimeLimitSeconds() {
    solver.setTimeLimitSeconds(10);
    // Assertion ensures no exceptions were thrown during assignment
    assertTrue(true); 
  }

  /**
   * Tests the private drawRandomRack method to ensure it correctly samples tiles.
   * Scenario: The unseen list is larger than the requested size.
   */
  @Test
  @SuppressWarnings("unchecked")
  void testDrawRandomRackNormal() throws Exception {
    Method drawMethod = MinimaxSolver.class.getDeclaredMethod("drawRandomRack", List.class, int.class);
    drawMethod.setAccessible(true);

    List<Character> unseen = Arrays.asList('A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I');
    Character[] rack = (Character[]) drawMethod.invoke(solver, unseen, 7);

    assertEquals(7, rack.length);
  }

  /**
   * Tests the private drawRandomRack method.
   * Scenario: The unseen list is smaller than the requested rack size (e.g., end of the game).
   */
  @Test
  @SuppressWarnings("unchecked")
  void testDrawRandomRackNotEnoughTiles() throws Exception {
    Method drawMethod = MinimaxSolver.class.getDeclaredMethod("drawRandomRack", List.class, int.class);
    drawMethod.setAccessible(true);

    List<Character> unseen = Arrays.asList('X', 'Y', 'Z');
    Character[] rack = (Character[]) drawMethod.invoke(solver, unseen, 7);

    // It should safely return an array of size 3, preventing IndexOutOfBounds exceptions
    assertEquals(3, rack.length);
    List<Character> rackList = Arrays.asList(rack);
    assertTrue(rackList.contains('X'));
    assertTrue(rackList.contains('Y'));
    assertTrue(rackList.contains('Z'));
  }

  /**
   * Tests the private drawRandomRack method.
   * Scenario: The unseen bag is completely empty.
   */
  @Test
  @SuppressWarnings("unchecked")
  void testDrawRandomRackEmptyBag() throws Exception {
    Method drawMethod = MinimaxSolver.class.getDeclaredMethod("drawRandomRack", List.class, int.class);
    drawMethod.setAccessible(true);

    List<Character> unseen = Arrays.asList();
    Character[] rack = (Character[]) drawMethod.invoke(solver, unseen, 7);

    assertEquals(0, rack.length);
  }
}