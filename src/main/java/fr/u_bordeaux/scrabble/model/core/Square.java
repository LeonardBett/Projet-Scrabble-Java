package fr.u_bordeaux.scrabble.model.core;

import fr.u_bordeaux.scrabble.model.enums.SquareType;
import fr.u_bordeaux.scrabble.model.utils.Point;

/**
 * Represents a single square on the board.
 * Can contain a tile and have an associated bonus.
 */
public class Square {
    /** The fixed position of this square on the board. */
    private final Point position;

    /** The type of the square (Normal, DL, TL, DW, TW). */
    private final SquareType squareType;

    /** The tile currently placed on this square (null if empty). */
    private Tile tile;

    /**
     * Constructor for an empty square at the start of the game.
     */
    public Square(Point position, SquareType squareType) {
        this(position, null, squareType);
    }

    /**
     * Full constructor (maybe useless).
     *
     * @param position The coordinates of the square.
     * @param tile The tile initially placed (can be null).
     * @param squareType The bonus type of the square.
     */
    public Square(Point position, Tile tile, SquareType squareType) {
        this.position = position;
        this.tile = tile;
        this.squareType = squareType;
    }

    public boolean isEmpty() {
        return tile == null;
    }

    public void setTile(Tile tile) {
        this.tile = tile;
    }

    public Tile getTile() {
        return tile;
    }

    public Point getPosition() {
        return position;
    }

    public SquareType getSquareType() {
        return squareType;
    }
}