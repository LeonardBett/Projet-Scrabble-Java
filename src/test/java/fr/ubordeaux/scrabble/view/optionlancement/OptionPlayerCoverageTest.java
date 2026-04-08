package fr.ubordeaux.scrabble.view.optionlancement;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import fr.ubordeaux.scrabble.i18n.I18n;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class OptionPlayerCoverageTest {

  @BeforeEach
  void setUp() {
    I18n.setLanguage("fr");
  }

  @AfterEach
  void tearDown() {
    OptionPlayer.resetExitHandlerForTests();
    I18n.setLanguage("en");
  }

  @Test
  void parsePlayersShouldAcceptValidBounds() {
    assertEquals(2, OptionPlayer.parsePlayers("2"));
    assertEquals(3, OptionPlayer.parsePlayers("3"));
    assertEquals(4, OptionPlayer.parsePlayers("4"));
  }

  @Test
  void parsePlayersShouldExitForOutOfRangeValues() {
    ByteArrayOutputStream err = new ByteArrayOutputStream();
    PrintStream originalErr = System.err;
    System.setErr(new PrintStream(err));

    final int[] status = { -1 };
    OptionPlayer.setExitHandlerForTests(code -> {
      status[0] = code;
      throw new RuntimeException("exit-called");
    });

    try {
      OptionPlayer.parsePlayers("5");
    } catch (RuntimeException e) {
      assertEquals("exit-called", e.getMessage());
    } finally {
      System.setErr(originalErr);
    }

    assertEquals(1, status[0]);
    assertTrue(err.toString().contains("Nombre de joueurs invalide"));
  }

  @Test
  void parsePlayersShouldExitForBelowMinimumValue() {
    ByteArrayOutputStream err = new ByteArrayOutputStream();
    PrintStream originalErr = System.err;
    System.setErr(new PrintStream(err));

    final int[] status = { -1 };
    OptionPlayer.setExitHandlerForTests(code -> {
      status[0] = code;
      throw new RuntimeException("exit-called");
    });

    try {
      OptionPlayer.parsePlayers("1");
    } catch (RuntimeException e) {
      assertEquals("exit-called", e.getMessage());
    } finally {
      System.setErr(originalErr);
    }

    assertEquals(1, status[0]);
    assertTrue(err.toString().contains("Nombre de joueurs invalide"));
  }

  @Test
  void parsePlayersShouldExitForNonInteger() {
    ByteArrayOutputStream err = new ByteArrayOutputStream();
    PrintStream originalErr = System.err;
    System.setErr(new PrintStream(err));

    final int[] status = { -1 };
    OptionPlayer.setExitHandlerForTests(code -> {
      status[0] = code;
      throw new RuntimeException("exit-called");
    });

    try {
      OptionPlayer.parsePlayers("abc");
    } catch (RuntimeException e) {
      assertEquals("exit-called", e.getMessage());
    } finally {
      System.setErr(originalErr);
    }

    assertEquals(1, status[0]);
    assertTrue(err.toString().contains("'-p' attend un entier"));
  }
}
