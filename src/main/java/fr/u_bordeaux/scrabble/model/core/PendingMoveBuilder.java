package fr.u_bordeaux.scrabble.model.core;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import fr.u_bordeaux.scrabble.model.enums.Direction;
import fr.u_bordeaux.scrabble.model.interfaces.Player;
import fr.u_bordeaux.scrabble.model.utils.Point;

/**
 * Builds a PLAY Move from the pending tiles map.
 *
 * ✅ MVC: This is model logic (converting UI state → Move object).
 * It does NOT belong in the controller (which should receive a ready Move)
 * nor in the view (which should not contain domain logic).
 */
public class PendingMoveBuilder {

    private PendingMoveBuilder() {}

    /**
     * Builds a Move from the pending tiles, or returns null if the tiles
     * are not aligned (which the view will show as an error).
     */
    public static Move build(Map<Point, Tile> pendingTiles, Player player) {
        if (pendingTiles.isEmpty()) return null;

        // Sort points: first by row (Y), then by col (X)
        List<Point> points = new ArrayList<>(pendingTiles.keySet());
        points.sort((a, b) -> a.getY() != b.getY()
                ? Integer.compare(a.getY(), b.getY())
                : Integer.compare(a.getX(), b.getX()));

        Direction direction = detectDirection(points);
        if (direction == null) return null;

        Point start = points.get(0);

        // Tiles must be in the same order as the points
        List<Tile> tiles = new ArrayList<>();
        for (Point p : points) {
            tiles.add(pendingTiles.get(p));
        }

        return Move.createPlay(player, tiles, start, direction);
    }

    private static Direction detectDirection(List<Point> points) {
        if (points.size() == 1) return Direction.HORIZONTAL; // single tile: default
        boolean sameRow = points.stream().allMatch(p -> p.getY() == points.get(0).getY());
        boolean sameCol = points.stream().allMatch(p -> p.getX() == points.get(0).getX());
        if (sameRow) return Direction.HORIZONTAL;
        if (sameCol) return Direction.VERTICAL;
        return null;
    }
}
