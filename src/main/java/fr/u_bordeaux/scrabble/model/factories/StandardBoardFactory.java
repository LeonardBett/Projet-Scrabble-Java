package fr.u_bordeaux.scrabble.model.factories;

import fr.u_bordeaux.scrabble.model.core.Board;
import fr.u_bordeaux.scrabble.model.core.Square;
import fr.u_bordeaux.scrabble.model.enums.SquareType;
import fr.u_bordeaux.scrabble.model.utils.Point;

/**
 * Factory class to create a standard Scrabble board configuration.
 */
public class StandardBoardFactory {

    /**
     * Private constructor to prevent instantiation of this utility class.
     */
    private StandardBoardFactory() {}

    /**
     * Creates a fully initialized Board with the standard Scrabble layout.
     *
     * @return A new Board instance.
     */
    public static Board createBoard() {
        int size = Board.SIZE;
        Square[][] squares = new Square[size][size];

        // 1. Fill with NORMAL squares
        for (int x = 0; x < size; x++) {
            for (int y = 0; y < size; y++) {
                squares[x][y] = new Square(new Point(x, y), SquareType.NORMAL);
            }
        }

        // 2. Apply bonuses
        applyBonuses(squares);

        return new Board(squares);
    }

    private static void applyBonuses(Square[][] squares) {
        // Triple Word (Red)
        setBonus(squares, SquareType.TRIPLE_WORD, 0, 0);
        setBonus(squares, SquareType.TRIPLE_WORD, 0, 7);
        setBonus(squares, SquareType.TRIPLE_WORD, 0, 14);
        setBonus(squares, SquareType.TRIPLE_WORD, 7, 0);
        setBonus(squares, SquareType.TRIPLE_WORD, 7, 14);
        setBonus(squares, SquareType.TRIPLE_WORD, 14, 0);
        setBonus(squares, SquareType.TRIPLE_WORD, 14, 7);
        setBonus(squares, SquareType.TRIPLE_WORD, 14, 14);

        // Double Word (Pink)
        for (int i = 1; i <= 4; i++) {
            setBonus(squares, SquareType.DOUBLE_WORD, i, i);
            setBonus(squares, SquareType.DOUBLE_WORD, i, 14 - i);
            setBonus(squares, SquareType.DOUBLE_WORD, 14 - i, i);
            setBonus(squares, SquareType.DOUBLE_WORD, 14 - i, 14 - i);
        }
        setBonus(squares, SquareType.DOUBLE_WORD, 7, 7);

        // Triple Letter (Dark Blue)
        setBonus(squares, SquareType.TRIPLE_LETTER, 1, 5);
        setBonus(squares, SquareType.TRIPLE_LETTER, 1, 9);
        setBonus(squares, SquareType.TRIPLE_LETTER, 5, 1);
        setBonus(squares, SquareType.TRIPLE_LETTER, 5, 5);
        setBonus(squares, SquareType.TRIPLE_LETTER, 5, 9);
        setBonus(squares, SquareType.TRIPLE_LETTER, 5, 13);
        setBonus(squares, SquareType.TRIPLE_LETTER, 9, 1);
        setBonus(squares, SquareType.TRIPLE_LETTER, 9, 5);
        setBonus(squares, SquareType.TRIPLE_LETTER, 9, 9);
        setBonus(squares, SquareType.TRIPLE_LETTER, 9, 13);
        setBonus(squares, SquareType.TRIPLE_LETTER, 13, 5);
        setBonus(squares, SquareType.TRIPLE_LETTER, 13, 9);

        // Double Letter (Light Blue)
        setBonus(squares, SquareType.DOUBLE_LETTER, 0, 3);
        setBonus(squares, SquareType.DOUBLE_LETTER, 0, 11);
        setBonus(squares, SquareType.DOUBLE_LETTER, 14, 3);
        setBonus(squares, SquareType.DOUBLE_LETTER, 14, 11);
        setBonus(squares, SquareType.DOUBLE_LETTER, 2, 6);
        setBonus(squares, SquareType.DOUBLE_LETTER, 2, 8);
        setBonus(squares, SquareType.DOUBLE_LETTER, 12, 6);
        setBonus(squares, SquareType.DOUBLE_LETTER, 12, 8);
        setBonus(squares, SquareType.DOUBLE_LETTER, 3, 0);
        setBonus(squares, SquareType.DOUBLE_LETTER, 3, 7);
        setBonus(squares, SquareType.DOUBLE_LETTER, 3, 14);
        setBonus(squares, SquareType.DOUBLE_LETTER, 11, 0);
        setBonus(squares, SquareType.DOUBLE_LETTER, 11, 7);
        setBonus(squares, SquareType.DOUBLE_LETTER, 11, 14);
        setBonus(squares, SquareType.DOUBLE_LETTER, 6, 2);
        setBonus(squares, SquareType.DOUBLE_LETTER, 6, 6);
        setBonus(squares, SquareType.DOUBLE_LETTER, 6, 8);
        setBonus(squares, SquareType.DOUBLE_LETTER, 6, 12);
        setBonus(squares, SquareType.DOUBLE_LETTER, 8, 2);
        setBonus(squares, SquareType.DOUBLE_LETTER, 8, 6);
        setBonus(squares, SquareType.DOUBLE_LETTER, 8, 8);
        setBonus(squares, SquareType.DOUBLE_LETTER, 8, 12);
        setBonus(squares, SquareType.DOUBLE_LETTER, 7, 3);
        setBonus(squares, SquareType.DOUBLE_LETTER, 7, 11);
    }

    private static void setBonus(Square[][] squares, SquareType type, int x, int y) {
        squares[x][y] = new Square(new Point(x, y), type);
    }
}