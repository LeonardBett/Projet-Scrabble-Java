package fr.ubordeaux.scrabble.view.cli;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import fr.ubordeaux.scrabble.model.core.Game;
import fr.ubordeaux.scrabble.model.core.HumanPlayer;
import fr.ubordeaux.scrabble.model.enums.PlayerColor;
import fr.ubordeaux.scrabble.view.cli.CliView;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class CliViewTest {

  private Game game;
  private CliView view;
  private ByteArrayOutputStream out;

  @BeforeEach
  void setUp() {
    game = new Game();
    game.addPlayer(new HumanPlayer("Alice", PlayerColor.BLUE));
    game.addPlayer(new HumanPlayer("Bob", PlayerColor.RED));
    game.startGame();
    view = new CliView(game);
    out = new ByteArrayOutputStream();
    System.setOut(new PrintStream(out));
  }

  @Test
  void displayMessageShouldPrintToOutput() {
    view.displayMessage("Hello test");
    System.setOut(System.out);
    assertTrue(out.toString().contains("Hello test"));
  }

  @Test
  void displayErrorShouldPrintToOutput() {
    view.displayError("Error test");
    System.setOut(System.out);
    assertTrue(out.toString().contains("Error test"));
  }

  @Test
  void displaySuccessShouldPrintToOutput() {
    view.displaySuccess("Success test");
    System.setOut(System.out);
    assertTrue(out.toString().contains("Success test"));
  }

  @Test
  void refreshShouldNotThrow() {
    assertDoesNotThrow(() -> view.refresh());
    System.setOut(System.out);
  }

  @Test
  void displayGameStateShouldNotThrow() {
    assertDoesNotThrow(() -> view.displayGameState(true));
    assertDoesNotThrow(() -> view.displayGameState(false));
    System.setOut(System.out);
  }

  @Test
  void displayCurrentPlayerShouldNotThrow() {
    assertDoesNotThrow(() -> view.displayCurrentPlayer());
    System.setOut(System.out);
  }

  @Test
  void displayWelcomeShouldNotThrow() {
    assertDoesNotThrow(() -> view.displayWelcome());
    System.setOut(System.out);
  }

  @Test
  void setBlitzModeShouldNotThrow() {
    assertDoesNotThrow(() -> view.setBlitzMode(true));
    assertDoesNotThrow(() -> view.setBlitzMode(false));
  }

  @Test
  void setShowBonusSquaresShouldNotThrow() {
    assertDoesNotThrow(() -> view.setShowBonusSquares(true));
    assertDoesNotThrow(() -> view.setShowBonusSquares(false));
  }

  @Test
  void rendererGettersShouldNotReturnNull() {
    assertNotNull(view.getBoardRenderer());
    assertNotNull(view.getPlayerRenderer());
    assertNotNull(view.getRackRenderer());
    assertNotNull(view.getMessageRenderer());
  }

  @Test
  void constructorWithBlitzModeShouldNotThrow() {
    assertDoesNotThrow(() -> new CliView(game, true));
    System.setOut(System.out);
  }
}
