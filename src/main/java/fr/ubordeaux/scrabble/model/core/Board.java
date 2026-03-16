package fr.ubordeaux.scrabble.model.core;

import fr.ubordeaux.scrabble.model.factories.StandardBoardFactory;
import fr.ubordeaux.scrabble.model.utils.Point;

/**
 * Represents the game board (grid of squares). Manages the placement of tiles and special squares
 * (bonuses).
 */
public class Board {
  /** Official board side length (15x15). */
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
      throw new IllegalArgumentException(
          "Invalid board dimensions. Expected " + SIZE + "x" + SIZE + ".");
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

  /**
   * Returns the square at the provided coordinates.
   *
   * @param point board coordinates.
   * @return the corresponding square, or null when out of bounds.
   */
  public Square getSquare(Point point) {
    if (point.getX() >= 0 && point.getX() < SIZE && point.getY() >= 0 && point.getY() < SIZE) {
      return board[point.getX()][point.getY()];
    }
    return null;
  }
}
