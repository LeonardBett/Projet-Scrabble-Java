package fr.ubordeaux.scrabble.view;

import static org.junit.jupiter.api.Assertions.assertTrue;

import fr.ubordeaux.scrabble.i18n.I18n;
import fr.ubordeaux.scrabble.model.core.HumanPlayer;
import fr.ubordeaux.scrabble.model.enums.PlayerColor;
import fr.ubordeaux.scrabble.view.cli.renderer.RackRenderer;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class RackRendererTest {

  @BeforeEach
  void setUp() {
    I18n.setLanguage("en");
  }

  @AfterEach
  void tearDown() {
    I18n.setLanguage("en");
  }

  @Test
  void renderShouldPrintRackLabelAndTiles() {
    HumanPlayer player = new HumanPlayer("Alice", PlayerColor.BLUE);

    ByteArrayOutputStream out = new ByteArrayOutputStream();
    PrintStream original = System.out;
    System.setOut(new PrintStream(out));

    new RackRenderer().render(player);

    System.setOut(original);
    String output = out.toString();
    assertTrue(output.contains("Rack: ["));
    assertTrue(output.contains("]"));
  }
}
