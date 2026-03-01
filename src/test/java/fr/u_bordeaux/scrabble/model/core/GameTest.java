package fr.u_bordeaux.scrabble.model.core;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GameTest {

    @Test
    void startGameShouldFailWhenNoPlayersExist() {
        Game game = new Game();

        assertThrows(IllegalStateException.class, game::startGame);
    }

    @Test
    void startGameShouldFillPlayerRacks() {
        Game game = new Game();
        HumanPlayer alice = new HumanPlayer("Alice");
        HumanPlayer bob = new HumanPlayer("Bob");

        game.addPlayer(alice);
        game.addPlayer(bob);

        int initialBagSize = game.getBag().size();
        game.startGame();

        assertEquals(Rack.MAX_SIZE, alice.getRack().getTiles().size());
        assertEquals(Rack.MAX_SIZE, bob.getRack().getTiles().size());
        assertEquals(initialBagSize - 2 * Rack.MAX_SIZE, game.getBag().size());
    }

    @Test
    void executeMoveShouldRejectWrongPlayerTurn() {
        Game game = new Game();
        HumanPlayer alice = new HumanPlayer("Alice");
        HumanPlayer bob = new HumanPlayer("Bob");
        game.addPlayer(alice);
        game.addPlayer(bob);

        Move wrongTurnMove = Move.createPass(bob);

        assertThrows(IllegalArgumentException.class, () -> game.executeMove(wrongTurnMove));
    }

    @Test
    void executePassMoveShouldAdvanceTurnAndTrackHistory() {
        Game game = new Game();
        HumanPlayer alice = new HumanPlayer("Alice");
        HumanPlayer bob = new HumanPlayer("Bob");
        game.addPlayer(alice);
        game.addPlayer(bob);

        game.executeMove(Move.createPass(alice));

        assertEquals(bob, game.getCurrentPlayer());
        assertEquals(1, game.getUndoRedo().getHistory().size());
    }

    @Test
    void determineWinnerShouldReturnHighestScoreOrNullWhenNoPlayers() {
        Game emptyGame = new Game();
        assertNull(emptyGame.determineWinner());

        Game game = new Game();
        HumanPlayer alice = new HumanPlayer("Alice");
        HumanPlayer bob = new HumanPlayer("Bob");
        alice.addScore(10);
        bob.addScore(15);
        game.addPlayer(alice);
        game.addPlayer(bob);

        assertEquals(bob, game.determineWinner());
    }

    @Test
    void refillRackShouldAddTilesUntilFullOrBagEmpty() {
        Game game = new Game();
        HumanPlayer player = new HumanPlayer("Alice");

        var drawn = game.refillRack(player);

        assertNotNull(drawn);
        assertTrue(drawn.size() <= Rack.MAX_SIZE);
        assertEquals(drawn.size(), player.getRack().getTiles().size());
    }
}