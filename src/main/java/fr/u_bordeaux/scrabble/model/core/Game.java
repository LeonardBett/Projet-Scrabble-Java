package fr.u_bordeaux.scrabble.model.core;

import java.util.ArrayList;
import java.util.List;

import fr.u_bordeaux.scrabble.model.interfaces.Player;
import fr.u_bordeaux.scrabble.model.utils.Point;
import fr.u_bordeaux.scrabble.model.enums.MoveType;

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
    private final MoveHandler moveHandler;
    private final UndoRedo undoRedo;
    /** True once the first PLAY move has been successfully executed. */
    private boolean firstMoveDone;

    public Game() {
        this.board = new Board();
        this.bag = new Bag();
        this.players = new ArrayList<>();
        this.currentPlayerIndex = 0;
        this.isGameOver = false;
        this.moveHandler = new MoveHandler(this);
        this.undoRedo = new UndoRedo();
        this.firstMoveDone = false;
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
        applyMove(move);

        // 3. Add move to history
        undoRedo.addMove(move);

        // 4. Prepare next turn
        nextTurn();
    }

    // Need to be separated from executeMove() because it will be used for undo/redo
    private void applyMove(Move move) {
        switch (move.getType()) {
            case PLAY -> moveHandler.handlePlayMove(move);
            case EXCHANGE -> moveHandler.handleExchangeMove(move);
            case PASS -> moveHandler.handlePassMove(move);
        }
        // Mark that at least one play has occurred
        if (move.getType() == MoveType.PLAY) {
            setFirstMoveDone(true);
        }
    }

    public boolean isFirstMoveDone() {
        return firstMoveDone;
    }

    public void setFirstMoveDone(boolean firstMoveDone) {
        this.firstMoveDone = firstMoveDone;
    }

    public void nextTurn() {
        if (!players.isEmpty()) {
            currentPlayerIndex = (currentPlayerIndex + 1) % players.size();
        }
    }
    
    private void previousTurn() {
        if (!players.isEmpty()) {
            currentPlayerIndex = (currentPlayerIndex - 1 + players.size()) % players.size();
        }
    }

    // Returns true if there is at least one tile on the board
    private boolean boardHasAnyTile() {
        for (int x = 0; x < Board.SIZE; x++) {
            for (int y = 0; y < Board.SIZE; y++) {
                Square sq = board.getSquare(new fr.u_bordeaux.scrabble.model.utils.Point(x, y));
                if (sq != null && !sq.isEmpty()) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Refills the player's rack from the bag until it is full or the bag is empty.
     * @return The list of tiles added to the rack.
     */
    public List<Tile> refillRack(Player player) {
        List<Tile> addedTiles = new ArrayList<>();
        while (!player.getRack().isFull() && !bag.isEmpty()) {
            Tile tile = bag.drawTile();
            if (tile != null) {
                player.getRack().addTile(tile);
                addedTiles.add(tile);
            }
        }
        return addedTiles;
    }

    public Player getCurrentPlayer() {
        return players.isEmpty() ? null : players.get(currentPlayerIndex);
    }

    public Board getBoard() {
        return board;
    }

    public Bag getBag() {
        return bag;
    }

    public List<Player> getPlayers() {
    return players;
    }
    
    public UndoRedo getUndoRedo() {
        return undoRedo;
    }
    
    
    public boolean isGameOver() {
        return isGameOver;
    }
    
    public void setGameOver(boolean gameOver) {
        isGameOver = gameOver;
    }
    
    /**
     * Determines the winner of the game.
     * @return The player with the highest score
     */
    public Player determineWinner() {
        if (players.isEmpty()) {
            return null;
        }
        Player winner = players.get(0);
        for (Player player : players) {
            if (player.getScore() > winner.getScore()) {
                winner = player;
            }
        }
        return winner;
    }

    /**
     * Undo the last move(s).
     * If the current player is human, it undoes their last move and any subsequent AI moves.
     */
    public void undo() {
        if (!(getCurrentPlayer() instanceof HumanPlayer)) {
            System.out.println("Only human players can undo.");
            return;
        }
        
        if (!undoRedo.canUndo()) {
            System.out.println("Nothing to undo.");
            return;
        }

        boolean undoneHumanMove = false;
        while (!undoneHumanMove && undoRedo.canUndo()) {
            Move move = undoRedo.undo();
            moveHandler.revertMove(move);
            previousTurn(); // Move turn pointer back
            
            if (move.getPlayer() instanceof HumanPlayer) {
                undoneHumanMove = true;
            }
        }
        // If the board is now empty after undoing moves, reset the first-move flag
        if (!boardHasAnyTile()) {
            setFirstMoveDone(false);
        }
    }
    
    public void redo() {
        if (!(getCurrentPlayer() instanceof HumanPlayer)) {
            System.out.println("Only human players can redo.");
            return;
        }

        if (!undoRedo.canRedo()) {
            System.out.println("Nothing to redo.");
            return;
        }
        
        boolean redoneHumanMove = false;
        while (!redoneHumanMove && undoRedo.canRedo()) {
            Move move = undoRedo.redo();
            
            applyMove(move);
            nextTurn();
            
            if (move.getPlayer() instanceof HumanPlayer) {
                redoneHumanMove = true;
            }
        }
    }

    /**
     * Debug function to display the board and player stats in the terminal.
     * Will be removed
     */
    public void printDebugState(boolean showBonusSquare, boolean clientMode) {
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
                    if (showBonusSquare) {
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
        if(clientMode){
            System.out.println("\nBag: " + bag.getOnlineSize() + " tiles left");
        } else {
            System.out.println("\nBag: " + bag.size() + " tiles left");
        }

        // 4. Print Turn
        System.out.println("\nNext Turn: " + getCurrentPlayer().getName());

        System.out.println("-------------------------\n");
    }


    /**-----NETWORKING-----**/
    // These methods are needed for online play, for manipulating client side model
    // with data from the server side model directly, without redoing calculation
    /**
     * Finds a player in the game by their name.
     * Needed for networking
     * @param name The name of the player to find
     * @return The Player object if found, null otherwise.
     */
    public Player getPlayerFromName(String name) {
        for (Player p : players) {
            if (p.getName().equals(name)) {
                return p;
            }
        }
        return null;
    }

    /**
     * Synchronizes a player's rack with a specific list of tiles.
     * Used for updating local game in network play.
     */
    public void forceTilesToPlayer(String playerName, List<Tile> tiles) {
        Player p = getPlayerFromName(playerName);
        if (p != null) {
            p.getRack().setTiles(tiles);
        }
    }

    public int getCurrentPlayerIndex() {
        return currentPlayerIndex;
    }

    /**
     * Synchronizes the board with a full board state string from the server.
     * Used for updating local game in network play.
     */
    public void syncBoard(String boardData) {
        if (boardData == null || boardData.length() != 225) return; // Sécurité (15x15)

        for (int i = 0; i < boardData.length(); i++) {
            int x = i % 15;
            int y = i / 15;
            char c = boardData.charAt(i);

            Square sq = board.getSquare(new Point(x, y));
            if (c == '.') {
                // TODO: Méthode pour vider la case si nécessaire (ex: sq.setTile(null))
            } else {
                sq.setTile(new Tile(c));
            }
        }
    }
}