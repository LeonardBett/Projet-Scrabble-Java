package fr.ubordeaux.scrabble.model.core;

import fr.ubordeaux.scrabble.model.enums.SquareType;
import fr.ubordeaux.scrabble.model.utils.Point;

/**
 * Represents a single square on the board. Can contain a tile and have an associated bonus.
 */
public class Square {
  /**
   * The fixed position of this square on the board.
   */
  private final Point position;

  /**
   * The type of the square (Normal, DL, TL, DW, TW).
   */
  private final SquareType squareType;

  /**
   * The tile currently placed on this square (null if empty).
   */
  private Tile tile;

  /**
   * Constructor for an empty square at the start of the game.
   *
   * @param position The coordinates of the square.
   * @param squareType The bonus type of the square.
   */
  public Square(Point position, SquareType squareType) {
    this(position, null, squareType);
  }

  /**
   * Full constructor (maybe useless).
   *
   * @param position   The coordinates of the square.
   * @param tile       The tile initially placed (can be null).
   * @param squareType The bonus type of the square.
   */
  public Square(Point position, Tile tile, SquareType squareType) {
    this.position = position;
    this.tile = tile;
    this.squareType = squareType;
  }

  /**
   * Indicates whether this square currently contains a tile.
   *
   * @return true when no tile is placed.
   */
  public boolean isEmpty() {
    return tile == null;
  }

  /**
   * Places or removes a tile on this square.
   *
   * @param tile tile to place, or null to clear the square.
   */
  public void setTile(Tile tile) {
    this.tile = tile;
  }

  /**
   * Returns the tile currently on this square.
   *
   * @return tile on the square, or null when empty.
   */
  public Tile getTile() {
    return tile;
  }

  /**
   * Returns this square coordinates.
   *
   * @return immutable board position.
   */
  public Point getPosition() {
    return position;
  }

  /**
   * Returns the bonus type associated with this square.
   *
   * @return square bonus type.
   */
  public SquareType getSquareType() {
    return squareType;
  }
}
