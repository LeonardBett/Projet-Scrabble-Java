package fr.ubordeaux.scrabble.view;

import static org.junit.jupiter.api.Assertions.assertTrue;

import fr.ubordeaux.scrabble.view.cli.renderer.MessageRenderer;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import org.junit.jupiter.api.Test;

class CliMessageRendererTest {

  @Test
  void shouldRenderAllMessageTypes() {
    MessageRenderer renderer = new MessageRenderer();
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    final PrintStream original = System.out;
    System.setOut(new PrintStream(out));

    renderer.error("err");
    renderer.success("ok");
    renderer.info("info");
    renderer.warning("warn");
    renderer.sectionTitle("TITLE");
    renderer.separator();
    renderer.welcome();

    System.setOut(original);
    String output = out.toString();
    assertTrue(output.contains("Erreur : err"));
    assertTrue(output.contains("OK : ok"));
    assertTrue(output.contains("Info : info"));
    assertTrue(output.contains("Attention : warn"));
    assertTrue(output.contains("=== TITLE ==="));
    assertTrue(output.contains("Bienvenue dans le Scrabble CLI"));
  }
}
