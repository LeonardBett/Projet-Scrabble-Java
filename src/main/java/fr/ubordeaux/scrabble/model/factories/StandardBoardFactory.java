package fr.ubordeaux.scrabble.model.factories;

import fr.ubordeaux.scrabble.model.core.Board;
import fr.ubordeaux.scrabble.model.core.Square;
import fr.ubordeaux.scrabble.model.enums.SquareType;
import fr.ubordeaux.scrabble.model.utils.Point;

/**
 * Factory class to create a standard Scrabble board configuration.
 */
public class StandardBoardFactory {
  private static final int BASE_SIZE = Board.SIZE;

  private static final int[][] TRIPLE_WORD_COORDS = {
    {0, 0}, {0, 7}, {0, 14}, {7, 0}, {7, 14}, {14, 0}, {14, 7}, {14, 14}
  };

  private static final int[][] DOUBLE_WORD_COORDS = {
    {1, 1}, {1, 13}, {13, 1}, {13, 13},
    {2, 2}, {2, 12}, {12, 2}, {12, 12},
    {3, 3}, {3, 11}, {11, 3}, {11, 11},
    {4, 4}, {4, 10}, {10, 4}, {10, 10},
    {7, 7}
  };

  private static final int[][] TRIPLE_LETTER_COORDS = {
    {1, 5}, {1, 9}, {5, 1}, {5, 5}, {5, 9}, {5, 13},
    {9, 1}, {9, 5}, {9, 9}, {9, 13}, {13, 5}, {13, 9}
  };

  private static final int[][] DOUBLE_LETTER_COORDS = {
    {0, 3}, {0, 11}, {14, 3}, {14, 11},
    {2, 6}, {2, 8}, {12, 6}, {12, 8},
    {3, 0}, {3, 7}, {3, 14}, {11, 0}, {11, 7}, {11, 14},
    {6, 2}, {6, 6}, {6, 8}, {6, 12},
    {8, 2}, {8, 6}, {8, 8}, {8, 12},
    {7, 3}, {7, 11}
  };

  /**
   * Private constructor to prevent instantiation of this utility class.
   */
  private StandardBoardFactory() {
  }

  /**
   * Creates a fully initialized Board with the standard Scrabble layout.
   *
   * @return A new Board instance.
   */
  public static Board createBoard() {
    return createBoard(Board.SIZE);
  }

  /**
   * Creates a fully initialized Board with a specific side size.
   *
   * @param size board side length.
   * @return A new Board instance.
   */
  public static Board createBoard(int size) {
    if (size <= 0) {
      throw new IllegalArgumentException("Board size must be positive.");
    }

    Square[][] squares = new Square[size][size];

    // 1. Fill with NORMAL squares
    for (int x = 0; x < size; x++) {
      for (int y = 0; y < size; y++) {
        squares[x][y] = new Square(new Point(x, y), SquareType.NORMAL);
      }
    }

    // 2. Apply bonuses
    applyBonuses(squares, size);

    return new Board(squares, size);
  }

  private static void applyBonuses(Square[][] squares, int size) {
    applyScaledBonusGroup(squares, size, SquareType.TRIPLE_WORD, TRIPLE_WORD_COORDS);
    applyScaledBonusGroup(squares, size, SquareType.DOUBLE_WORD, DOUBLE_WORD_COORDS);
    applyScaledBonusGroup(squares, size, SquareType.TRIPLE_LETTER, TRIPLE_LETTER_COORDS);
    applyScaledBonusGroup(squares, size, SquareType.DOUBLE_LETTER, DOUBLE_LETTER_COORDS);
  }

  private static void applyScaledBonusGroup(Square[][] squares, int size, SquareType type,
      int[][] baseCoords) {
    for (int[] coord : baseCoords) {
      int x = scaleCoordinate(coord[0], size);
      int y = scaleCoordinate(coord[1], size);
      setBonus(squares, type, x, y);
    }
  }

  private static int scaleCoordinate(int baseCoordinate, int targetSize) {
    if (targetSize == BASE_SIZE) {
      return baseCoordinate;
    }
    return (int) Math.round(baseCoordinate * (targetSize - 1.0) / (BASE_SIZE - 1.0));
  }

  private static void setBonus(Square[][] squares, SquareType type, int x, int y) {
    squares[x][y] = new Square(new Point(x, y), type);
  }
}
