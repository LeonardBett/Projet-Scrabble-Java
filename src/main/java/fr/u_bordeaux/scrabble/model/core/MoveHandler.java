package fr.u_bordeaux.scrabble.model.core;

import fr.u_bordeaux.scrabble.model.enums.Direction;
import fr.u_bordeaux.scrabble.model.enums.MoveType;
import fr.u_bordeaux.scrabble.model.interfaces.Player;
import fr.u_bordeaux.scrabble.model.utils.Point;

import java.util.ArrayList;
import java.util.List;

/**
 * Handles the execution of moves in the game.
 * This class encapsulates the logic for processing PLAY, EXCHANGE, and PASS moves.
 */
public class MoveHandler {
    private final Game game;

    public MoveHandler(Game game) {
        this.game = game;
    }

    public void handlePlayMove(Move move) {
        // 1. Extract move details
        Player player = move.getPlayer();
        List<Tile> tiles = move.getTiles();
        Point startPosition = move.getStartPosition();
        Direction direction = move.getDirection();

        // 2. Prepare lists for scoring and rack management
        List<Square> wordSquares = new ArrayList<>(); //list of squares that contains the tiles of this word
        List<Square> newlyPlacedSquares = new ArrayList<>(); //list of squares that was empty before the move

        // 3. Calculate direction deltas (dx, dy) to iterate over the board
        int x = startPosition.getX();
        int y = startPosition.getY();
        int dx = direction == Direction.HORIZONTAL ? 1 : 0;
        int dy = direction == Direction.VERTICAL ? 1 : 0;

        // 4. Iterate over each tile in the word to place them on the board
        for (Tile tile : tiles) {
            Point currentPos = new Point(x, y);
            Square square = game.getBoard().getSquare(currentPos);

            // Check if the word goes out of bounds
            if (square == null) {
                throw new IllegalArgumentException("Word extends beyond board boundaries.");
            }

            // If the square is empty, we place the tile from the Move and add it to the list of newly placed squares (for scoring)
            if (square.isEmpty()) {
                square.setTile(tile);
                newlyPlacedSquares.add(square);
            }
            // If not empty, we skip placing (it's an existing letter on the board)

            // Add the square to the list of the word's squares (for scoring)
            wordSquares.add(square);

            // Move to the next position
            x += dx;
            y += dy;
        }

        // 5. Calculate the score using the Scoring utility
        int score = Scoring.calculateWordScore(wordSquares, newlyPlacedSquares);
        player.addScore(score);
        
        // Save score in move for undo
        move.setScoreGained(score);

        // 6. Remove the used tiles from the player's rack (and throw an error if not present form his rack)
        for (Square square : newlyPlacedSquares) {
            Tile tile = square.getTile();
            if (!player.getRack().removeTile(tile)) {
                throw new IllegalArgumentException("Player does not have the tile " + tile.getCharacter());
            }
        }

        // 7. Refill the player's rack from the bag
        List<Tile> drawn = game.refillRack(player);
        move.setDrawnTiles(drawn);
        
        System.out.println("Player " + player.getName() + " played a word for " + score + " points.");
    }

    public void handleExchangeMove(Move move) {
        Player player = move.getPlayer();
        List<Tile> tilesToExchange = move.getTiles();

        // Rule: Cannot exchange if bag has fewer than 7 tiles
        if (game.getBag().size() < 7) {
            throw new IllegalStateException("Cannot exchange tiles: bag has fewer than 7 tiles left.");
        }

        // 1. Verify player has these tiles (and remove them)
        for (Tile tile : tilesToExchange) {
            if (!player.getRack().removeTile(tile)) {
                // In a real scenario, we should probably rollback or check beforehand
                throw new IllegalArgumentException("Player does not have the tile " + tile.getCharacter());
            }
        }

        // 2. Put them back in the bag
        game.getBag().putBack(tilesToExchange);

        // 3. Draw new tiles
        List<Tile> drawn = game.refillRack(player);
        move.setDrawnTiles(drawn);

        System.out.println("Player " + player.getName() + " exchanged " + tilesToExchange.size() + " tiles.");
    }

    public void handlePassMove(Move move) {
        System.out.println("Player " + move.getPlayer() + " passed.");
    }

    public void revertMove(Move move) {
        if (move.getType() != MoveType.PLAY) {
            return; // Only PLAY moves are handled for now
        }

        Player player = move.getPlayer();

        // 1. Revert Score
        player.addScore(-move.getScoreGained());

        // 2. Revert Rack & Bag (Remove drawn tiles from rack and put them back in bag)
        List<Tile> drawnTiles = move.getDrawnTiles();
        if (drawnTiles != null && !drawnTiles.isEmpty()) {
            for (Tile tile : drawnTiles) {
                player.getRack().removeTile(tile);
            }
            game.getBag().putBack(drawnTiles);
        }

        // 3. Revert Board (Remove placed tiles from board and put them back in rack)
        int x = move.getStartPosition().getX();
        int y = move.getStartPosition().getY();
        int dx = move.getDirection() == Direction.HORIZONTAL ? 1 : 0;
        int dy = move.getDirection() == Direction.VERTICAL ? 1 : 0;

        for (Tile tile : move.getTiles()) {
            Point currentPos = new Point(x, y);
            Square square = game.getBoard().getSquare(currentPos);

            // If the square contains the exact tile instance we played, remove it
            if (square != null && square.getTile() == tile) {
                square.setTile(null);
                player.getRack().addTile(tile);
            }
            
            x += dx;
            y += dy;
        }
    }
}