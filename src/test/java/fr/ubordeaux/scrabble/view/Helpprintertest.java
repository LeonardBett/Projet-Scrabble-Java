package fr.ubordeaux.scrabble.view;

import static org.junit.jupiter.api.Assertions.*;

import fr.ubordeaux.scrabble.view.optionLancement.HelpPrinter;
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
