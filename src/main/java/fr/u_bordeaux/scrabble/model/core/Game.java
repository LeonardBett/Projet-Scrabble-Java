package fr.u_bordeaux.scrabble.model.core;

import fr.u_bordeaux.scrabble.model.enums.Direction;
import fr.u_bordeaux.scrabble.model.enums.SquareType;
import fr.u_bordeaux.scrabble.model.interfaces.Player;
import fr.u_bordeaux.scrabble.model.utils.Point;

import java.util.ArrayList;
import java.util.List;

/**
 * The main game engine.
 * Manages the board, players, turns, and executes moves.
 * This is where the "business logic" resides.
 */
public class Game {
    private final Board board;
    private final Bag bag;
    private final List<Player> players;
    private int currentPlayerIndex;
    private boolean isGameOver;

    public Game() {
        this.board = new Board();
        this.bag = new Bag();
        this.players = new ArrayList<>();
        this.currentPlayerIndex = 0;
        this.isGameOver = false;
    }

    public void addPlayer(Player player) {
        players.add(player);
    }

    /**
     * Starts the game by distributing initial tiles to players.
     */
    public void startGame() {
        if (players.isEmpty()) {
            throw new IllegalStateException("No players added to the game.");
        }
        for (Player player : players) {
            refillRack(player);
        }
        System.out.println("Game started! Tiles distributed.");
    }

    /**
     * Executes a player's move.
     * This method acts as the referee: it validates and applies the changes.
     *
     * @param move The move to execute.
     * @throws IllegalArgumentException if the move is invalid (rules violation).
     */
    public void executeMove(Move move) {
        if (isGameOver) {
            throw new IllegalStateException("Game is over.");
        }

        // 1. Validate that it's the correct player's turn
        if (!move.getPlayer().equals(getCurrentPlayer())) {
            throw new IllegalArgumentException("It is not " + move.getPlayer() + "'s turn.");
        }

        // 2. Dispatch logic based on move type
        switch (move.getType()) {
            case PLAY -> handlePlayMove(move);
            case EXCHANGE -> handleExchangeMove(move);
            case PASS -> handlePassMove(move);
        }

        // 3. Prepare next turn
        nextTurn();
    }

    private void handlePlayMove(Move move) {
        // 1. Extract move details
        Player player = move.getPlayer();
        List<Tile> tiles = move.getTiles();
        Point startPosition = move.getStartPosition();
        Direction direction = move.getDirection();

        // 2. Prepare lists for scoring and rack management
        List<Square> wordSquares = new ArrayList<>(); //list of squares that contains the tiles of this word
        List<Square> newlyPlacedSquares = new ArrayList<>(); //list of squares that was empty before the move
        List<Tile> tilesToConsume = new ArrayList<>(); //list of tiles to remove from the rack at the end

        // 3. Calculate direction deltas (dx, dy) to iterate over the board
        int x = startPosition.getX();
        int y = startPosition.getY();
        int dx = direction == Direction.HORIZONTAL ? 1 : 0;
        int dy = direction == Direction.VERTICAL ? 1 : 0;

        // 4. Iterate over each tile in the word to place them on the board
        for (Tile tile : tiles) {
            Point currentPos = new Point(x, y);
            Square square = board.getSquare(currentPos);

            // Check if the word goes out of bounds
            if (square == null) {
                throw new IllegalArgumentException("Word extends beyond board boundaries.");
            }

            // If the square is empty, we place the tile from the Move and add it to the list of newly placed squares (for scoring)
            if (square.isEmpty()) {
                square.setTile(tile);
                newlyPlacedSquares.add(square);
                tilesToConsume.add(tile);
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

        // 6. Remove the used tiles from the player's rack (and throw an error if not present form his rack)
        for (Tile tile : tilesToConsume) {
            if (!player.getRack().removeTile(tile)) {
                throw new IllegalArgumentException("Player does not have the tile " + tile.getCharacter());
            }
        }

        // 7. Refill the player's rack from the bag
        refillRack(player);
        System.out.println("Player " + player.getName() + " played a word for " + score + " points.");
    }

    private void handleExchangeMove(Move move) {
        Player player = move.getPlayer();
        List<Tile> tilesToExchange = move.getTiles();

        // Rule: Cannot exchange if bag has fewer than 7 tiles
        if (bag.size() < 7) {
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
        bag.putBack(tilesToExchange);

        // 3. Draw new tiles
        refillRack(player);

        System.out.println("Player " + player.getName() + " exchanged " + tilesToExchange.size() + " tiles.");
    }

    private void handlePassMove(Move move) {
        System.out.println("Player " + move.getPlayer() + " passed.");
    }

    private void nextTurn() {
        if (!players.isEmpty()) {
            currentPlayerIndex = (currentPlayerIndex + 1) % players.size();
        }
    }

    /**
     * Refills the player's rack from the bag until it is full or the bag is empty.
     */
    private void refillRack(Player player) {
        while (!player.getRack().isFull() && !bag.isEmpty()) {
            Tile tile = bag.drawTile();
            if (tile != null) {
                player.getRack().addTile(tile);
            }
        }
    }

    public Player getCurrentPlayer() {
        return players.isEmpty() ? null : players.get(currentPlayerIndex);
    }

    public Board getBoard() {
        return board;
    }

    /**
     * Debug function to display the board and player stats in the terminal.
     * Will be removed
     */
    public void printDebugState(boolean showBonusSquare) {
        System.out.println("\n--- DEBUG: GAME STATE ---");

        // 1. Print Board
        System.out.print("   ");
        for (int x = 0; x < Board.SIZE; x++) {
            System.out.printf("%2d ", x);
        }
        System.out.println();

        for (int y = 0; y < Board.SIZE; y++) {
            System.out.printf("%2d ", y);
            for (int x = 0; x < Board.SIZE; x++) {
                Square square = board.getSquare(new Point(x, y));
                if (!square.isEmpty()) {
                    System.out.print(" " + square.getTile().getCharacter() + " ");
                } else {
                    if(showBonusSquare){
                        switch (square.getSquareType()) {
                            case TRIPLE_WORD -> System.out.print("TW ");
                            case DOUBLE_WORD -> System.out.print("DW ");
                            case TRIPLE_LETTER -> System.out.print("TL ");
                            case DOUBLE_LETTER -> System.out.print("DL ");
                            default -> System.out.print(" . ");
                        }
                    } else {
                        System.out.print(" . ");
                    }
                }
            }
            System.out.println();
        }

        // 2. Print Players
        System.out.println("\nPlayers:");
        for (Player p : players) {
            System.out.println("- " + p.getName() + ": " + p.getScore() + " pts");
            System.out.println("  Rack: " + p.getRack().toString());
        }

        // 3. Print Bag
        System.out.println("\nBag: " + bag.size() + " tiles left");

        // 4. Print Turn
        System.out.println("\nNext Turn: " + getCurrentPlayer().getName());

        System.out.println("-------------------------\n");
    }
}