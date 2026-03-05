package fr.u_bordeaux.scrabble.model.core;

import fr.u_bordeaux.scrabble.model.enums.SquareType;
import fr.u_bordeaux.scrabble.model.utils.Point;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class BoardTest {

    /**
     * Test that the Board constructor rejects invalid inputs such as
     * null grids or grids with incorrect dimensions.
     */
    @Test
    void constructorShouldRejectInvalidDimensions() {
        Square[][] wrongSize = new Square[Board.SIZE - 1][Board.SIZE];

        assertThrows(IllegalArgumentException.class, () -> new Board(null));
        assertThrows(IllegalArgumentException.class, () -> new Board(wrongSize));
    }

    /**
     * Test that getSquare returns the correct square for valid coordinates
     * and null for coordinates outside the board boundaries.
     */
    @Test
    void getSquareShouldReturnSquareInsideBoundsAndNullOutside() {
        Board board = new Board(createEmptyGrid());

        assertNotNull(board.getSquare(new Point(0, 0)));
        assertNotNull(board.getSquare(new Point(Board.SIZE - 1, Board.SIZE - 1)));
        assertNull(board.getSquare(new Point(-1, 0)));
        assertNull(board.getSquare(new Point(0, -1)));
        assertNull(board.getSquare(new Point(Board.SIZE, 0)));
        assertNull(board.getSquare(new Point(0, Board.SIZE)));
    }

    /**
     * Test that the default Board constructor creates a standard Scrabble board
     * with the correct special square types (triple word, double word, etc.).
     */
    @Test
    void defaultConstructorShouldCreateStandardBoardLayout() {
        Board board = new Board();

        assertEquals(SquareType.TRIPLE_WORD, board.getSquare(new Point(0, 0)).getSquareType());
        assertEquals(SquareType.DOUBLE_WORD, board.getSquare(new Point(7, 7)).getSquareType());
        assertEquals(SquareType.NORMAL, board.getSquare(new Point(7, 6)).getSquareType());
    }

    private Square[][] createEmptyGrid() {
        Square[][] grid = new Square[Board.SIZE][Board.SIZE];
        for (int x = 0; x < Board.SIZE; x++) {
            for (int y = 0; y < Board.SIZE; y++) {
                grid[x][y] = new Square(new Point(x, y), SquareType.NORMAL);
            }
        }
        return grid;
    }
}
