package fr.u_bordeaux.scrabble.model.core;

import fr.u_bordeaux.scrabble.model.enums.Direction;
import fr.u_bordeaux.scrabble.model.utils.Point;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MoveHandlerTest {

    @Test
    void getCompleteWordShouldIncludeExistingPrefixAndSuffix() {
        Game game = new Game();
        MoveHandler handler = new MoveHandler(game);

        game.getBoard().getSquare(new Point(6, 7)).setTile(new Tile('c'));
        game.getBoard().getSquare(new Point(8, 7)).setTile(new Tile('t'));

        String word = handler.getCompleteWord(new Point(7, 7), Direction.HORIZONTAL, List.of(new Tile('a')));

        assertEquals("ct", word);
    }

    @Test
    void handlePlayMoveShouldPlaceTilesScoreAndRefill() {
        Game game = new Game();
        HumanPlayer alice = new HumanPlayer("Alice");
        game.addPlayer(alice);
        MoveHandler handler = new MoveHandler(game);

        alice.getRack().setTiles(new ArrayList<>(List.of(new Tile('a'))));
        Move move = Move.createPlay(alice, List.of(new Tile('a')), new Point(7, 7), Direction.HORIZONTAL);

        handler.handlePlayMove(move);

        assertNotNull(game.getBoard().getSquare(new Point(7, 7)).getTile());
        assertTrue(alice.getScore() > 0);
        assertEquals(1, move.getPlacedPositions().size());
        assertEquals(1, move.getPlacedTiles().size());
        assertNotNull(move.getDrawnTiles());
    }

    @Test
    void handlePlayMoveShouldRejectTileNotInRack() {
        Game game = new Game();
        HumanPlayer alice = new HumanPlayer("Alice");
        game.addPlayer(alice);
        MoveHandler handler = new MoveHandler(game);

        alice.getRack().setTiles(new ArrayList<>());
        Move move = Move.createPlay(alice, List.of(new Tile('a')), new Point(7, 7), Direction.HORIZONTAL);

        assertThrows(IllegalArgumentException.class, () -> handler.handlePlayMove(move));
    }

    @Test
    void handlePlayMoveShouldRejectDisconnectedWordAfterFirstMove() {
        Game game = new Game();
        HumanPlayer alice = new HumanPlayer("Alice");
        game.addPlayer(alice);
        MoveHandler handler = new MoveHandler(game);

        game.setFirstMoveDone(true);
        game.getBoard().getSquare(new Point(7, 7)).setTile(new Tile('a'));
        alice.getRack().setTiles(new ArrayList<>(List.of(new Tile('b'))));

        Move move = Move.createPlay(alice, List.of(new Tile('b')), new Point(0, 0), Direction.HORIZONTAL);

        assertThrows(IllegalArgumentException.class, () -> handler.handlePlayMove(move));
    }

    @Test
    void handlePlayMoveShouldRejectOutOfBoundsWord() {
        Game game = new Game();
        HumanPlayer alice = new HumanPlayer("Alice");
        game.addPlayer(alice);
        MoveHandler handler = new MoveHandler(game);

        alice.getRack().setTiles(new ArrayList<>(List.of(new Tile('a'), new Tile('b'))));
        Move move = Move.createPlay(alice, List.of(new Tile('a'), new Tile('b')), new Point(14, 14), Direction.HORIZONTAL);

        assertThrows(IllegalArgumentException.class, () -> handler.handlePlayMove(move));
    }

    @Test
    void exchangeMoveShouldValidateBagAndRackAndSucceed() {
        Game game = new Game();
        HumanPlayer alice = new HumanPlayer("Alice");
        game.addPlayer(alice);
        MoveHandler handler = new MoveHandler(game);

        alice.getRack().setTiles(new ArrayList<>(List.of(
                new Tile('a'), new Tile('b'), new Tile('c'),
                new Tile('d'), new Tile('e'), new Tile('f'), new Tile('g'))));

        Move invalid = Move.createExchange(alice, List.of(new Tile('z')));
        assertThrows(IllegalArgumentException.class, () -> handler.handleExchangeMove(invalid));

        Move valid = Move.createExchange(alice, List.of(new Tile('a'), new Tile('b')));
        handler.handleExchangeMove(valid);
        assertEquals(Rack.MAX_SIZE, alice.getRack().getTiles().size());
        assertEquals(2, valid.getDrawnTiles().size());

        while (game.getBag().size() >= 7) {
            game.getBag().drawTile();
        }
        Move blockedByBag = Move.createExchange(alice, List.of(new Tile('c')));
        assertThrows(IllegalStateException.class, () -> handler.handleExchangeMove(blockedByBag));
    }

    @Test
    void revertMoveShouldHandlePassAndRestorePlayState() {
        Game game = new Game();
        HumanPlayer alice = new HumanPlayer("Alice");
        game.addPlayer(alice);
        MoveHandler handler = new MoveHandler(game);

        assertDoesNotThrow(() -> handler.revertMove(Move.createPass(alice)));

        alice.getRack().setTiles(new ArrayList<>(List.of(new Tile('a'))));
        Move play = Move.createPlay(alice, List.of(new Tile('a')), new Point(7, 7), Direction.HORIZONTAL);
        handler.handlePlayMove(play);
        int scoreAfterPlay = alice.getScore();

        handler.revertMove(play);

        assertTrue(scoreAfterPlay > 0);
        assertEquals(0, alice.getScore());
        assertEquals(1, alice.getRack().getTiles().size());
        assertNull(game.getBoard().getSquare(new Point(7, 7)).getTile());
    }
}
