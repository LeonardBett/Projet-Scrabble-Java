package fr.u_bordeaux.scrabble.model.core;

import fr.u_bordeaux.scrabble.model.enums.Direction;
import fr.u_bordeaux.scrabble.model.enums.MoveType;
import fr.u_bordeaux.scrabble.model.interfaces.Player;
import fr.u_bordeaux.scrabble.model.utils.Point;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Represents an action from a player (during their turn).
 * This class is immutable to ensure thread safety and consistency.
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

    /**
     * Private constructor. Use factory methods to create instances.
     */
    private Move(Player player, MoveType type, List<Tile> tiles, Point startPosition, Direction direction) {
        this.player = Objects.requireNonNull(player, "Player cannot be null");
        this.type = Objects.requireNonNull(type, "MoveType cannot be null");
        this.tiles = tiles != null ? new ArrayList<>(tiles) : Collections.emptyList();
        this.startPosition = startPosition;
        this.direction = direction;
        this.drawnTiles = new ArrayList<>();
    }

    /**
     * Creates a PASS move.
     */
    public static Move createPass(Player player) {
        return new Move(player, MoveType.PASS, null, null, null);
    }

    /**
     * Creates an EXCHANGE move.
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
     * @param word The list of tiles forming the word (including existing ones or just new ones, depending on engine logic).
     */
    public static Move createPlay(Player player, List<Tile> word, Point startPosition, Direction direction) {
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

    public Player getPlayer() {
        return player;
    }

    public MoveType getType() {
        return type;
    }

    /**
     * Returns the tiles associated with the move.
     * For PLAY: The tiles forming the word.
     * For EXCHANGE: The tiles to be exchanged.
     */
    public List<Tile> getTiles() {
        return Collections.unmodifiableList(tiles);
    }

    public Point getStartPosition() {
        return startPosition;
    }

    public Direction getDirection() {
        return direction;
    }


    public int getScoreGained() {
        return scoreGained;
    }

    public void setScoreGained(int scoreGained) {
        this.scoreGained = scoreGained;
    }

    public List<Tile> getDrawnTiles() {
        return drawnTiles;
    }

    public void setDrawnTiles(List<Tile> drawnTiles) {
        this.drawnTiles = drawnTiles;
    }
}