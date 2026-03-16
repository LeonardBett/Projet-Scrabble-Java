package fr.ubordeaux.scrabble.view;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import fr.ubordeaux.scrabble.view.optionlancement.HelpPrinter;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import org.junit.jupiter.api.Test;

class HelpPrinterTest {

  @Test
  void versionConstantShouldNotBeNull() {
    assertNotNull(HelpPrinter.VERSION);
    assertFalse(HelpPrinter.VERSION.isBlank());
  }

  @Test
  void appNameConstantShouldNotBeNull() {
    assertNotNull(HelpPrinter.APP_NAME);
    assertFalse(HelpPrinter.APP_NAME.isBlank());
  }

  @Test
  void printHelpShouldPrintToStdout() {
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    System.setOut(new PrintStream(out));

    HelpPrinter.printHelp();

    System.setOut(System.out);
    String output = out.toString();
    assertTrue(output.contains("-h"));
    assertTrue(output.contains("--help"));
    assertTrue(output.contains("-g"));
    assertTrue(output.contains("-p"));
  }

  @Test
  void printVersionShouldContainAppNameAndVersion() {
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    System.setOut(new PrintStream(out));

    HelpPrinter.printVersion();

    System.setOut(System.out);
    String output = out.toString();
    assertTrue(output.contains(HelpPrinter.APP_NAME));
    assertTrue(output.contains(HelpPrinter.VERSION));
  }
}
