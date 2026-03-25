package fr.ubordeaux.scrabble.model.utils;

import java.util.Objects;

/**
 * Represents a 2D coordinate (x, y) on the board.
 */
public class Point {
  /**
   * The horizontal coordinate (column).
   */
  private final int xpos;

  /**
   * The vertical coordinate (row).
   */
  private final int ypos;

  /**
   * Creates a new immutable Point.
   *
   * @param x The x coordinate.
   * @param y The y coordinate.
   */
  public Point(int x, int y) {
    this.xpos = x;
    this.ypos = y;
  }

  /**
   * Returns the X coordinate of this point.
   *
   * @return the x position
   */
  public int getX() {
    return xpos;
  }

  /**
   * Returns the Y coordinate of this point.
   *
   * @return the y position
   */
  public int getY() {
    return ypos;
  }

  @Override
  public boolean equals(Object obj) {
    if (!(obj instanceof Point point)) {
      return false;
    }
    return xpos == point.xpos && ypos == point.ypos;
  }

  @Override
  public String toString() {
    return "(" + xpos + "," + ypos + ")";
  }

  @Override
  public int hashCode() {
    return Objects.hash(xpos, ypos);
  }
}
