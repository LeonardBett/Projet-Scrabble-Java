package fr.u_bordeaux.scrabble.model.core;

import fr.u_bordeaux.scrabble.model.enums.Direction;
import fr.u_bordeaux.scrabble.model.enums.MoveType;
import fr.u_bordeaux.scrabble.model.utils.Point;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MoveTest {

    @Test
    void createPassShouldBuildPassMove() {
        HumanPlayer player = new HumanPlayer("Alice");
        Move move = Move.createPass(player);

        assertEquals(player, move.getPlayer());
        assertEquals(MoveType.PASS, move.getType());
        assertTrue(move.getTiles().isEmpty());
    }

    @Test
    void createExchangeShouldRejectEmptyTiles() {
        HumanPlayer player = new HumanPlayer("Bob");

        assertThrows(IllegalArgumentException.class, () -> Move.createExchange(player, List.of()));
        assertThrows(IllegalArgumentException.class, () -> Move.createExchange(player, null));
    }

    @Test
    void createPlayShouldValidateRequiredFields() {
        HumanPlayer player = new HumanPlayer("Carol");
        List<Tile> word = List.of(new Tile('C'));

        assertThrows(IllegalArgumentException.class, () -> Move.createPlay(player, List.of(), new Point(7, 7), Direction.HORIZONTAL));
        assertThrows(IllegalArgumentException.class, () -> Move.createPlay(player, word, null, Direction.HORIZONTAL));
        assertThrows(IllegalArgumentException.class, () -> Move.createPlay(player, word, new Point(7, 7), null));
    }

    @Test
    void moveShouldExposeUnmodifiableTilesAndDefensiveCopiesForPlacedData() {
        HumanPlayer player = new HumanPlayer("Dan");
        Move move = Move.createPlay(player, List.of(new Tile('D')), new Point(7, 7), Direction.HORIZONTAL);

        assertThrows(UnsupportedOperationException.class, () -> move.getTiles().add(new Tile('E')));

        List<Point> placedPositions = new ArrayList<>();
        placedPositions.add(new Point(7, 7));
        move.setPlacedPositions(placedPositions);
        placedPositions.add(new Point(7, 8));
        assertEquals(1, move.getPlacedPositions().size());

        List<Tile> placedTiles = new ArrayList<>();
        placedTiles.add(new Tile('D'));
        move.setPlacedTiles(placedTiles);
        placedTiles.add(new Tile('E'));
        assertEquals(1, move.getPlacedTiles().size());
    }

    @Test
    void scoreGainedShouldBeMutable() {
        HumanPlayer player = new HumanPlayer("Eve");
        Move move = Move.createPass(player);

        move.setScoreGained(42);
        assertEquals(42, move.getScoreGained());
    }
}
