package fr.ubordeaux.scrabble.model.utils;

import java.util.Objects;

/**
 * Represents a 2D coordinate (x, y) on the board.
 */
public class Point {
  /** The horizontal coordinate (column). */
  private final int xCoordinate;

  /** The vertical coordinate (row). */
  private final int yCoordinate;

  /**
   * Creates a new immutable Point.
   *
   * @param x The x coordinate.
   * @param y The y coordinate.
   */
  public Point(int x, int y) {
    this.xCoordinate = x;
    this.yCoordinate = y;
  }

  public int getX() {
    return xCoordinate;
  }

  public int getY() {
    return yCoordinate;
  }

  @Override
  public boolean equals(Object obj) {
    if (!(obj instanceof Point point)) {
      return false;
    }
    return xCoordinate == point.xCoordinate && yCoordinate == point.yCoordinate;
  }

  @Override
  public String toString() {
    return "(" + xCoordinate + "," + yCoordinate + ")";
  }

  @Override
  public int hashCode() {
    return Objects.hash(xCoordinate, yCoordinate);
  }
}
