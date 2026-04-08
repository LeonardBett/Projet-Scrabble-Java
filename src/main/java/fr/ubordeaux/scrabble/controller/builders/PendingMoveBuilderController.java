package fr.ubordeaux.scrabble.controller.builders;

import fr.ubordeaux.scrabble.model.core.Board;
import fr.ubordeaux.scrabble.model.core.Game;
import fr.ubordeaux.scrabble.model.core.Move;
import fr.ubordeaux.scrabble.model.core.Square;
import fr.ubordeaux.scrabble.model.core.Tile;
import fr.ubordeaux.scrabble.model.enums.Direction;
import fr.ubordeaux.scrabble.model.interfaces.Player;
import fr.ubordeaux.scrabble.model.utils.Point;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Builds a PLAY move from pending tile placements.
 */
public final class PendingMoveBuilderController {

  private PendingMoveBuilderController() {
  }

  /**
   * Builds a move from pending tiles or returns null when alignment is invalid.
   *
   * @param pendingTiles pending tiles keyed by board point
   * @param player player performing the move
   * @param game game used to infer one-tile direction
   * @return play move or null when invalid
   */
  public static Move build(Map<Point, Tile> pendingTiles, Player player, Game game) {
    if (pendingTiles.isEmpty()) {
      return null;
    }

    List<Point> points = new ArrayList<>(pendingTiles.keySet());
    points.sort((a, b) -> a.getY() != b.getY()
        ? Integer.compare(a.getY(), b.getY())
        : Integer.compare(a.getX(), b.getX()));

    Direction direction = detectDirection(points, game != null ? game.getBoard() : null);
    if (direction == null) {
      return null;
    }

    Point start = points.get(0);
    List<Tile> tiles = new ArrayList<>();
    for (Point p : points) {
      tiles.add(pendingTiles.get(p));
    }

    return Move.createPlay(player, tiles, start, direction);
  }

  private static Direction detectDirection(List<Point> points, Board board) {
    if (points.size() == 1) {
      return detectDirectionFromBoard(points.get(0), board);
    }
    boolean sameRow = points.stream().allMatch(p -> p.getY() == points.get(0).getY());
    boolean sameCol = points.stream().allMatch(p -> p.getX() == points.get(0).getX());
    if (sameRow) {
      return Direction.HORIZONTAL;
    }
    if (sameCol) {
      return Direction.VERTICAL;
    }
    return null;
  }

  private static Direction detectDirectionFromBoard(Point p, Board board) {
    if (board == null) {
      return Direction.HORIZONTAL;
    }
    boolean hasVerticalNeighbour =
        isOccupied(board, p.getX(), p.getY() - 1) || isOccupied(board, p.getX(), p.getY() + 1);
    boolean hasHorizontalNeighbour =
        isOccupied(board, p.getX() - 1, p.getY()) || isOccupied(board, p.getX() + 1, p.getY());

    if (hasVerticalNeighbour && !hasHorizontalNeighbour) {
      return Direction.VERTICAL;
    }
    return Direction.HORIZONTAL;
  }

  private static boolean isOccupied(Board board, int x, int y) {
    Square sq = board.getSquare(new Point(x, y));
    return sq != null && !sq.isEmpty();
  }
}