package fr.ubordeaux.scrabble.model.core;

import fr.ubordeaux.scrabble.model.enums.Direction;
import fr.ubordeaux.scrabble.model.enums.MoveType;
import fr.ubordeaux.scrabble.model.interfaces.Player;
import fr.ubordeaux.scrabble.model.utils.Point;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Represents an action from a player (during their turn). This class is immutable to ensure thread
 * safety and consistency.
 */
public class Move {
  private final Player player;
  private final MoveType type;

  // Data for PLAY or EXCHANGE
  private final List<Tile> tiles;

  // Data for PLAY only
  private final Point startPosition;
  private final Direction direction;

  // Data for UNDO (to restore state)
  private int scoreGained;
  private List<Tile> drawnTiles; // Tiles drawn from bag to refill rack
  private List<Point> placedPositions; // positions where tiles were actually placed (for undo)
  private List<Tile> placedTiles; // tiles that were actually placed (aligned with
                                  // placedPositions)

  /**
   * Private constructor. Use factory methods to create instances.
   */
  private Move(Player player, MoveType type, List<Tile> tiles, Point startPosition,
      Direction direction) {
    this.player = Objects.requireNonNull(player, "Player cannot be null");
    this.type = Objects.requireNonNull(type, "MoveType cannot be null");
    this.tiles = tiles != null ? new ArrayList<>(tiles) : Collections.emptyList();
    this.startPosition = startPosition;
    this.direction = direction;
    this.drawnTiles = new ArrayList<>();
    this.placedPositions = new ArrayList<>();
    this.placedTiles = new ArrayList<>();
  }

  /**
   * Creates a PASS move.
   *
   * @param player player performing the pass.
   * @return immutable pass move.
   */
  public static Move createPass(Player player) {
    return new Move(player, MoveType.PASS, null, null, null);
  }

  /**
   * Creates an EXCHANGE move.
   *
   * @param player player performing the exchange.
   * @param tiles tiles to return to the bag.
   * @return immutable exchange move.
   */
  public static Move createExchange(Player player, List<Tile> tiles) {
    if (tiles == null || tiles.isEmpty()) {
      throw new IllegalArgumentException("Tiles to exchange cannot be empty");
    }
    return new Move(player, MoveType.EXCHANGE, tiles, null, null);
  }

  /**
   * Creates a PLAY move (placing a word).
   *
   * @param player player performing the play.
   * @param word The list of tiles forming the word (including existing ones or just new ones,
   *        depending on engine logic).
   * @param startPosition first target square for the play.
   * @param direction play direction.
   * @return immutable play move.
   */
  public static Move createPlay(Player player, List<Tile> word, Point startPosition,
      Direction direction) {
    if (word == null || word.isEmpty()) {
      throw new IllegalArgumentException("Word cannot be empty");
    }
    if (startPosition == null) {
      throw new IllegalArgumentException("Start position cannot be null");
    }
    if (direction == null) {
      throw new IllegalArgumentException("Direction cannot be null");
    }
    return new Move(player, MoveType.PLAY, word, startPosition, direction);
  }

  /**
   * Returns the player associated with this move.
   *
   * @return move owner.
   */
  public Player getPlayer() {
    return player;
  }

  /**
   * Returns the move type.
   *
   * @return PASS, EXCHANGE or PLAY.
   */
  public MoveType getType() {
    return type;
  }

  /**
   * Returns the tiles associated with the move. For PLAY: The tiles forming the word. For EXCHANGE:
   * The tiles to be exchanged.
   */
  public List<Tile> getTiles() {
    return Collections.unmodifiableList(tiles);
  }

  /**
   * Returns the board positions where tiles were placed.
   *
   * @return immutable list of placed positions.
   */
  public List<Point> getPlacedPositions() {
    return Collections.unmodifiableList(placedPositions);
  }

  /**
   * Stores placed positions for undo/redo purposes.
   *
   * @param placedPositions placed coordinates.
   */
  public void setPlacedPositions(List<Point> placedPositions) {
    this.placedPositions =
        placedPositions != null ? new ArrayList<>(placedPositions) : new ArrayList<>();
  }

  /**
   * Returns the tiles effectively placed on the board.
   *
   * @return immutable list of placed tiles.
   */
  public List<Tile> getPlacedTiles() {
    return Collections.unmodifiableList(placedTiles);
  }

  /**
   * Stores placed tiles for undo/redo purposes.
   *
   * @param placedTiles placed tiles.
   */
  public void setPlacedTiles(List<Tile> placedTiles) {
    this.placedTiles = placedTiles != null ? new ArrayList<>(placedTiles) : new ArrayList<>();
  }

  /**
   * Returns the starting position for PLAY moves.
   *
   * @return starting coordinates, or null for non-PLAY moves.
   */
  public Point getStartPosition() {
    return startPosition;
  }

  /**
   * Returns the direction for PLAY moves.
   *
   * @return direction, or null for non-PLAY moves.
   */
  public Direction getDirection() {
    return direction;
  }

  /**
   * Returns the score granted by this move.
   *
   * @return score delta recorded for history.
   */
  public int getScoreGained() {
    return scoreGained;
  }

  /**
   * Stores the score gained by the move.
   *
   * @param scoreGained score delta to persist for undo/redo.
   */
  public void setScoreGained(int scoreGained) {
    this.scoreGained = scoreGained;
  }

  /**
   * Returns tiles drawn after the move.
   *
   * @return list of drawn tiles.
   */
  public List<Tile> getDrawnTiles() {
    return drawnTiles;
  }

  /**
   * Stores tiles drawn after the move.
   *
   * @param drawnTiles tiles to persist for undo/redo.
   */
  public void setDrawnTiles(List<Tile> drawnTiles) {
    this.drawnTiles = drawnTiles;
  }
}
