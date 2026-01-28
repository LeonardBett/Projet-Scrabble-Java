package fr.u_bordeaux.scrabble.model.core;

import fr.u_bordeaux.scrabble.model.factories.StandardBoardFactory;
import fr.u_bordeaux.scrabble.model.utils.Point;

/**
 * Represents the game board (grid of squares).
 * Manages the placement of tiles and special squares (bonuses).
 */
public class Board {
    public static final int SIZE = 15;
    private Square[][] board;

    /**
     * Custom constructor: initializes the board with a given grid.
     *
     * @param board The grid of squares to use.
     * @throws IllegalArgumentException if the board is null or has invalid dimensions.
     */
    public Board(Square[][] board) {
        if (board == null || board.length != SIZE || board[0].length != SIZE) {
            throw new IllegalArgumentException("Invalid board dimensions. Expected " + SIZE + "x" + SIZE + ".");
        }
        this.board = board;
    }

    /**
     * Default constructor: initializes a standard Scrabble board.
     */
    public Board() {
        // Delegate creation to the factory
        Board tempBoard = StandardBoardFactory.createBoard();
        this.board = tempBoard.board;
    }

    public Square getSquare(Point point) {
        if (point.getX() >= 0 && point.getX() < SIZE && point.getY() >= 0 && point.getY() < SIZE) {
            return board[point.getX()][point.getY()];
        }
        return null;
    }
}