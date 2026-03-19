package fr.ubordeaux.scrabble.model.core;

import fr.ubordeaux.scrabble.model.factories.StandardBoardFactory;
import fr.ubordeaux.scrabble.model.utils.Point;

/**
 * Represents the game board (grid of squares). Manages the placement of tiles and special squares
 * (bonuses).
 */
public class Board {
  /**
   * Official board side length (15x15).
   */
  public static final int SIZE = 15;
  private final int size;
  private final Square[][] board;

  /**
   * Custom constructor: initializes the board with a given grid.
   *
   * @param board The grid of squares to use.
   * @throws IllegalArgumentException if the board is null or has invalid dimensions.
   */
  public Board(Square[][] board) {
    this(board, SIZE);
  }

  /**
   * Custom constructor: initializes the board with a given grid and explicit side size.
   *
   * @param board The grid of squares to use.
   * @param size The expected side size.
   * @throws IllegalArgumentException if the board is null or has invalid dimensions.
   */
  public Board(Square[][] board, int size) {
    if (size <= 0) {
      throw new IllegalArgumentException("Invalid board size. Expected a positive value.");
    }
    if (board == null || board.length != size) {
      throw new IllegalArgumentException(
          "Invalid board dimensions. Expected " + size + "x" + size + ".");
    }

    for (Square[] row : board) {
      if (row == null || row.length != size) {
        throw new IllegalArgumentException(
            "Invalid board dimensions. Expected " + size + "x" + size + ".");
      }
    }

    this.size = size;
    this.board = board;
  }

  /**
   * Constructor that initializes a board with a specific side size.
   *
   * @param size board side length.
   */
  public Board(int size) {
    Board tempBoard = StandardBoardFactory.createBoard(size);
    this.size = tempBoard.size;
    this.board = tempBoard.board;
  }

  /**
   * Default constructor: initializes a standard 15x15 Scrabble board.
   */
  public Board() {
    this(SIZE);
  }

  /**
   * Returns the side size of this board.
   *
   * @return board side length.
   */
  public int getSize() {
    return size;
  }

  /**
   * Returns the square at the provided coordinates.
   *
   * @param point board coordinates.
   * @return the corresponding square, or null when out of bounds.
   */
  public Square getSquare(Point point) {
    if (point.getX() >= 0 && point.getX() < size && point.getY() >= 0 && point.getY() < size) {
      return board[point.getX()][point.getY()];
    }
    return null;
  }
}
