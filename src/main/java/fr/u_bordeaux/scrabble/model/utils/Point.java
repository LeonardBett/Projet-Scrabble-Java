package fr.u_bordeaux.scrabble.model.utils;

import java.util.Objects;

/**
 * Represents a 2D coordinate (x, y) on the board.
 */
public class Point {
    /** The horizontal coordinate (column). */
    private final int x;
    
    /** The vertical coordinate (row). */
    private final int y;

    /**
     * Creates a new immutable Point.
     *
     * @param x The x coordinate.
     * @param y The y coordinate.
     */
    public Point(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public int getX() {
        return x;
    }
    public int getY() {
        return y;
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof Point point)) return false;
        return x == point.x && y == point.y;
    }

    @Override
    public String toString() {
        return "(" + x + "," + y + ")";
    }

    @Override
    public int hashCode() {
        return Objects.hash(x, y);
    }
}
