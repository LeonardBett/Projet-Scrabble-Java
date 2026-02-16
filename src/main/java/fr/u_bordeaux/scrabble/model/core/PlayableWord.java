package fr.u_bordeaux.scrabble.model.core;

import fr.u_bordeaux.scrabble.model.enums.Direction;

public class PlayableWord {
    private final int hookX;
    private final int hookY;
    private final String word;
    private final Direction direction;
    private int score; // à ajouter avec la methode de leonard
    private final String gaddagRepresentation;

    public PlayableWord(int hookX, int hookY, String word, Direction direction, String gaddagRepresentation) {
        this.hookX = hookX;
        this.hookY = hookY;
        this.word = word;
        this.direction = direction;
        this.score = 0;  //à ajouter avec la methode de leonard
        this.gaddagRepresentation = gaddagRepresentation;
    }

    // Getters
    public int getHookX() { return hookX; }
    public int getHookY() { return hookY; }

    public String getWord() { return word; }
    public Direction getDirection() { return direction; }
    public int getScore() { return score; }
    public String getGaddagRepresentation() { return gaddagRepresentation; }

    public void setScore(int score) { this.score = score; } // à ajouter avec la methode de leonard

    @Override
    public String toString() {
        return String.format("PlayableWord[word=%s, x=%d, y=%d, dir=%s, gaddag=%s, score=%d]",
                word, hookX, hookY, direction, gaddagRepresentation, score);
    }
}