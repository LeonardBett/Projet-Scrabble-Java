package fr.u_bordeaux.scrabble.model.enums;

/**
 * Enum representing the different types of squares on the board (multipliers).
 */
public enum SquareType {
    NORMAL(1, 1),
    DOUBLE_LETTER(2, 1),
    TRIPLE_LETTER(3, 1),
    DOUBLE_WORD(1, 2),
    TRIPLE_WORD(1, 3);

    private final int letterMultiplier;
    private final int wordMultiplier;

    SquareType(int letterMultiplier, int wordMultiplier) {
        this.letterMultiplier = letterMultiplier;
        this.wordMultiplier = wordMultiplier;
    }

    public int getLetterMultiplier() {
        return letterMultiplier;
    }

    public int getWordMultiplier() {
        return wordMultiplier;
    }
}