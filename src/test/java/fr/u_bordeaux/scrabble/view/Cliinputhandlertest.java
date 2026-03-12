package fr.u_bordeaux.scrabble.view;

import fr.u_bordeaux.scrabble.model.core.HumanPlayer;
import fr.u_bordeaux.scrabble.model.core.Move;
import fr.u_bordeaux.scrabble.model.core.Tile;
import fr.u_bordeaux.scrabble.model.interfaces.Player;
import fr.u_bordeaux.scrabble.view.cli.CLIInputHandler;

import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.PrintStream;

import static org.junit.jupiter.api.Assertions.*;

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