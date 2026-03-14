package fr.u_bordeaux.scrabble.view;

import fr.u_bordeaux.scrabble.model.core.HumanPlayer;
import fr.u_bordeaux.scrabble.model.core.Move;
import fr.u_bordeaux.scrabble.model.core.Tile;
import fr.u_bordeaux.scrabble.model.enums.Direction;
import fr.u_bordeaux.scrabble.model.enums.MoveType;
import fr.u_bordeaux.scrabble.model.interfaces.Player;
import fr.u_bordeaux.scrabble.model.utils.Point;
import fr.u_bordeaux.scrabble.view.gui.PendingMoveBuilder;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class PendingMoveBuilderTest {

    private Player player;

    @BeforeEach
    void setUp() {
        player = new HumanPlayer("Bob");
    }

    @Test
    void buildShouldReturnNullForEmptyMap() {
        assertNull(PendingMoveBuilder.build(new HashMap<>(), player));
    }

    @Test
    void buildShouldReturnHorizontalMoveForSingleTile() {
        Map<Point, Tile> pending = new HashMap<>();
        pending.put(new Point(7, 7), new Tile('A'));

        Move move = PendingMoveBuilder.build(pending, player);
        assertNotNull(move);
        assertEquals(Direction.HORIZONTAL, move.getDirection());
        assertEquals(MoveType.PLAY, move.getType());
    }

    @Test
    void buildShouldReturnHorizontalMoveWhenTilesOnSameRow() {
        Map<Point, Tile> pending = new HashMap<>();
        pending.put(new Point(5, 7), new Tile('H'));
        pending.put(new Point(6, 7), new Tile('I'));
        pending.put(new Point(7, 7), new Tile('S'));

        Move move = PendingMoveBuilder.build(pending, player);
        assertNotNull(move);
        assertEquals(Direction.HORIZONTAL, move.getDirection());
        assertEquals(3, move.getTiles().size());
    }

    @Test
    void buildShouldReturnVerticalMoveWhenTilesOnSameColumn() {
        Map<Point, Tile> pending = new HashMap<>();
        pending.put(new Point(7, 5), new Tile('H'));
        pending.put(new Point(7, 6), new Tile('I'));
        pending.put(new Point(7, 7), new Tile('S'));

        Move move = PendingMoveBuilder.build(pending, player);
        assertNotNull(move);
        assertEquals(Direction.VERTICAL, move.getDirection());
        assertEquals(3, move.getTiles().size());
    }

    @Test
    void buildShouldReturnNullWhenTilesNotAligned() {
        Map<Point, Tile> pending = new HashMap<>();
        pending.put(new Point(5, 5), new Tile('A'));
        pending.put(new Point(6, 6), new Tile('B')); // diagonal → invalid

        Move move = PendingMoveBuilder.build(pending, player);
        assertNull(move);
    }

    @Test
    void buildShouldSetCorrectStartPosition() {
        Map<Point, Tile> pending = new HashMap<>();
        pending.put(new Point(3, 7), new Tile('A'));
        pending.put(new Point(4, 7), new Tile('B'));

        Move move = PendingMoveBuilder.build(pending, player);
        assertNotNull(move);
        assertEquals(3, move.getStartPosition().getX());
        assertEquals(7, move.getStartPosition().getY());
    }
}
