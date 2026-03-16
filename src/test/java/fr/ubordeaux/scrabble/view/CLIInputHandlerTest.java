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

import fr.ubordeaux.scrabble.model.core.HumanPlayer;
import fr.ubordeaux.scrabble.model.core.Move;
import fr.ubordeaux.scrabble.model.core.Tile;
import fr.ubordeaux.scrabble.model.interfaces.Player;
import fr.ubordeaux.scrabble.view.cli.CLIInputHandler;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.PrintStream;
import org.junit.jupiter.api.Test;

class CLIInputHandlerTest {

  private CLIInputHandler handlerWithInput(String input) {
    InputStream in = new ByteArrayInputStream(input.getBytes());
    System.setIn(in);
    return new CLIInputHandler();
  }

  @Test
  void askActionShouldReturnUserInput() {
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    System.setOut(new PrintStream(out));
    CLIInputHandler handler = handlerWithInput("1\n");

    String result = handler.askAction();
    System.setOut(System.out);
    System.setIn(System.in);

    assertEquals("1", result);
  }

  @Test
  void askConfirmationShouldReturnTrueForO() {
    CLIInputHandler handler = handlerWithInput("o\n");
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    System.setOut(new PrintStream(out));

    boolean result = handler.askConfirmation("Confirmer ?");
    System.setOut(System.out);
    System.setIn(System.in);

    assertTrue(result);
  }

  @Test
  void askConfirmationShouldReturnTrueForOui() {
    CLIInputHandler handler = handlerWithInput("oui\n");
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    System.setOut(new PrintStream(out));

    boolean result = handler.askConfirmation("Confirmer ?");
    System.setOut(System.out);
    System.setIn(System.in);

    assertTrue(result);
  }

  @Test
  void askConfirmationShouldReturnFalseForN() {
    CLIInputHandler handler = handlerWithInput("n\n");
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    System.setOut(new PrintStream(out));

    boolean result = handler.askConfirmation("Confirmer ?");
    System.setOut(System.out);
    System.setIn(System.in);

    assertFalse(result);
  }

  @Test
  void askPlayerNameShouldReturnTrimmedName() {
    CLIInputHandler handler = handlerWithInput("  Alice  \n");
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    System.setOut(new PrintStream(out));

    String name = handler.askPlayerName(1);
    System.setOut(System.out);
    System.setIn(System.in);

    assertEquals("Alice", name);
  }

  @Test
  void askNumberOfPlayersShouldReturnValidNumber() {
    CLIInputHandler handler = handlerWithInput("3\n");
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    System.setOut(new PrintStream(out));

    int result = handler.askNumberOfPlayers();
    System.setOut(System.out);
    System.setIn(System.in);

    assertEquals(3, result);
  }

  @Test
  void askNumberOfPlayersShouldRetryOnInvalidThenSucceed() {
    // First "abc" is invalid, then "2" is valid
    CLIInputHandler handler = handlerWithInput("abc\n2\n");
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    System.setOut(new PrintStream(out));

    int result = handler.askNumberOfPlayers();
    System.setOut(System.out);
    System.setIn(System.in);

    assertEquals(2, result);
  }

  @Test
  void askExchangeMoveWithValidLettersShouldReturnMove() {
    Player player = new HumanPlayer("Alice");
    player.getRack().addTile(new Tile('A'));
    player.getRack().addTile(new Tile('B'));

    CLIInputHandler handler = handlerWithInput("AB\n");
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    System.setOut(new PrintStream(out));

    Move move = handler.askExchangeMove(player);
    System.setOut(System.out);
    System.setIn(System.in);

    assertNotNull(move);
  }

  @Test
  void askExchangeMoveWithInvalidLetterShouldReturnNull() {
    Player player = new HumanPlayer("Alice");
    player.getRack().addTile(new Tile('A'));

    CLIInputHandler handler = handlerWithInput("Z\n");
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    System.setOut(new PrintStream(out));

    Move move = handler.askExchangeMove(player);
    System.setOut(System.out);
    System.setIn(System.in);

    assertNull(move);
  }

  @Test
  void askPlayMoveWithValidInputShouldReturnMove() {
    Player player = new HumanPlayer("Alice");
    player.getRack().addTile(new Tile('H'));
    player.getRack().addTile(new Tile('I'));

    // row letter format: "h 8", direction "H", letters "HI"
    CLIInputHandler handler = handlerWithInput("h 8\nH\nHI\n");
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    System.setOut(new PrintStream(out));

    Move move = handler.askPlayMove(player);
    System.setOut(System.out);
    System.setIn(System.in);

    assertNotNull(move);
  }

  @Test
  void askPlayMoveWithMissingLetterInRackShouldReturnNull() {
    Player player = new HumanPlayer("Alice");
    player.getRack().addTile(new Tile('A'));

    // tries to play Z which is not in rack
    CLIInputHandler handler = handlerWithInput("h 8\nH\nZ\n");
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    System.setOut(new PrintStream(out));

    Move move = handler.askPlayMove(player);
    System.setOut(System.out);
    System.setIn(System.in);

    assertNull(move);
  }
}
