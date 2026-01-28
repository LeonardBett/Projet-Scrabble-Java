package fr.u_bordeaux.scrabble.model.core;

import fr.u_bordeaux.scrabble.model.enums.SquareType;
import java.util.List;

/**
 * Utility class for calculating scores based on Scrabble rules.
 * Handles word scores, multipliers, and Bingo bonuses.
 */
public class Scoring {

    private static final int BINGO_BONUS = 50;

    /**
     * Private constructor to prevent instantiation.
     */
    private Scoring() {}

    /**
     * Calculates the score for a specific word formed on the board.
     *
     * @param wordSquares        The list of squares that make up the word.
     * @param newlyPlacedSquares The list of squares where tiles were placed during this turn.
     *                           Bonuses (DL, TL, DW, TW) are only applied to these squares.
     * @return The total score for the word.
     */
    public static int calculateWordScore(List<Square> wordSquares, List<Square> newlyPlacedSquares) {

        if (wordSquares == null || newlyPlacedSquares == null) {
            throw new NullPointerException("WordSquares or newlyPlacedSquares is null");
        }

        if (wordSquares.isEmpty()) {
            throw new IllegalArgumentException("Cannot calculate score for an empty word");
        }

        int wordScore = 0;
        int totalWordMultiplier = 1;

        for (Square square : wordSquares) {
            if (square.isEmpty()) {
                throw new IllegalArgumentException("Square of wordSquares is empty");
            }

            int tileValue = square.getTile().getValue();

            // By default, no multipliers apply (if the tile was already on the board)
            int letterMultiplier = 1;

            // Check if the tile is newly placed to apply bonuses
            if (newlyPlacedSquares.contains(square)) {
                SquareType type = square.getSquareType();
                letterMultiplier = type.getLetterMultiplier();
                totalWordMultiplier *= type.getWordMultiplier();
            }

            wordScore += (tileValue * letterMultiplier);
        }

        return (wordScore * totalWordMultiplier) + calculateBingoBonus(newlyPlacedSquares.size());
    }

    /**
     * Calculates the Bingo bonus (Scrabble).
     *
     * @param tilesPlacedCount The number of tiles placed by the player in this turn.
     * @return 50 points if the player used all 7 tiles, otherwise 0.
     */
    public static int calculateBingoBonus(int tilesPlacedCount) {
        return (tilesPlacedCount + 1) == Rack.MAX_SIZE ? BINGO_BONUS : 0;
    }
}