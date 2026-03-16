package fr.ubordeaux.scrabble.model.ai;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.Test;

/**
 * Comprehensive unit tests for the MlAgent class. Focuses on mathematical data preparation and
 * graceful failure handling.
 */
class MlAgentTest {

  /**
   * Tests the initialization using an invalid path. Ensures the application catches the TensorFlow
   * exception gracefully without crashing.
   */
  @Test
  void testInitializationWithInvalidPath() {
    List<String> dictionary = Arrays.asList("TEST", "WORD");

    try (MlAgent agent = new MlAgent("invalid/path/to/model", dictionary)) {
      assertNotNull(agent);
      // Predict should safely return an empty list if the model is missing
      List<String> predictions = agent.predictWords("ABCDEFG", 5);
      assertTrue(predictions.isEmpty());
    }
  }

  /**
   * Tests the predictWords method when provided with an empty rack.
   */
  @Test
  void testEmptyRackPrediction() {
    try (MlAgent agent = new MlAgent("invalid/path", new ArrayList<>())) {
      List<String> predictions = agent.predictWords("", 10);
      assertTrue(predictions.isEmpty());
    }
  }

  /**
   * Tests the private vectorizeRack method to ensure letters are correctly mapped to a 26-float
   * frequency array (A-Z). Scenario: Standard uppercase letters.
   */
  @Test
  void testVectorizeRackStandardLetters() throws Exception {
    try (MlAgent agent = new MlAgent("invalid", new ArrayList<>())) {
      Method vectorizeMethod = MlAgent.class.getDeclaredMethod("vectorizeRack", String.class);
      vectorizeMethod.setAccessible(true);

      float[] expected = new float[26];
      expected[0] = 2.0f; // 'A'
      expected[1] = 1.0f; // 'B'

      float[] result = (float[]) vectorizeMethod.invoke(agent, "AAB");
      assertArrayEquals(expected, result, 0.001f);
    }
  }

  /**
   * Tests the private vectorizeRack method. Scenario: Lowercase letters mixed with uppercase
   * letters.
   */
  @Test
  void testVectorizeRackCaseInsensitive() throws Exception {
    try (MlAgent agent = new MlAgent("invalid", new ArrayList<>())) {
      Method vectorizeMethod = MlAgent.class.getDeclaredMethod("vectorizeRack", String.class);
      vectorizeMethod.setAccessible(true);

      float[] expected = new float[26];
      expected[2] = 3.0f; // 'C'

      float[] result = (float[]) vectorizeMethod.invoke(agent, "cCc");
      assertEquals(3.0f, result[2], 0.001f);
    }
  }

  /**
   * Tests the private vectorizeRack method. Scenario: Rack containing special characters, blanks,
   * or numbers.
   */
  @Test
  void testVectorizeRackWithSpecialCharacters() throws Exception {
    try (MlAgent agent = new MlAgent("invalid", new ArrayList<>())) {
      Method vectorizeMethod = MlAgent.class.getDeclaredMethod("vectorizeRack", String.class);
      vectorizeMethod.setAccessible(true);

      // The method should ignore numbers and special characters
      float[] result = (float[]) vectorizeMethod.invoke(agent, "A 1!B?");

      float[] expected = new float[26];
      expected[0] = 1.0f; // 'A'
      expected[1] = 1.0f; // 'B'

      assertArrayEquals(expected, result, 0.001f);
    }
  }
}
