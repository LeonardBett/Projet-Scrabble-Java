package fr.ubordeaux.scrabble.view;

import static org.junit.jupiter.api.Assertions.assertTrue;

import fr.ubordeaux.scrabble.model.dictionary.core.HumanPlayer;
import fr.ubordeaux.scrabble.model.enums.PlayerColor;
import fr.ubordeaux.scrabble.model.interfaces.Player;
import fr.ubordeaux.scrabble.view.cli.renderer.PlayerRenderer;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import org.junit.jupiter.api.Test;

class PlayerRendererCliTest {

  @Test
  void renderCurrentPlayerShouldPrintTurnLine() {
    Player alice = new HumanPlayer("Alice", PlayerColor.BLUE);

    ByteArrayOutputStream out = new ByteArrayOutputStream();
    PrintStream original = System.out;
    System.setOut(new PrintStream(out));

    new PlayerRenderer().renderCurrentPlayer(alice);

    System.setOut(original);
    assertTrue(out.toString().contains("Current turn: Alice"));
  }
}
