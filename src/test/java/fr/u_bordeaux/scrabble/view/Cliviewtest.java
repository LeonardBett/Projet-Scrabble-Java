package fr.u_bordeaux.scrabble.view;

import fr.u_bordeaux.scrabble.model.core.Game;
import fr.u_bordeaux.scrabble.model.core.HumanPlayer;
import fr.u_bordeaux.scrabble.view.cli.CLIView;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import static org.junit.jupiter.api.Assertions.*;

class CLIViewTest {

    private Game game;
    private CLIView view;
    private ByteArrayOutputStream out;

    @BeforeEach
    void setUp() {
        game = new Game();
        game.addPlayer(new HumanPlayer("Alice"));
        game.addPlayer(new HumanPlayer("Bob"));
        game.startGame();
        view = new CLIView(game);
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
        view.displayError("Erreur test");
        System.setOut(System.out);
        assertTrue(out.toString().contains("Erreur test"));
    }

    @Test
    void displaySuccessShouldPrintToOutput() {
        view.displaySuccess("Succès test");
        System.setOut(System.out);
        assertTrue(out.toString().contains("Succès test"));
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
        assertDoesNotThrow(() -> new CLIView(game, true));
        System.setOut(System.out);
    }
}