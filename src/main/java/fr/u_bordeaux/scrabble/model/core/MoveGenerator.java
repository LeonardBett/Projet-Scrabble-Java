package fr.u_bordeaux.scrabble.model.core;

import fr.u_bordeaux.scrabble.model.dictionary.GADDAG;
import fr.u_bordeaux.scrabble.model.utils.Point;
import fr.u_bordeaux.scrabble.model.enums.Direction;
import fr.u_bordeaux.scrabble.model.interfaces.Player;

import java.util.ArrayList;
import java.util.List;

public class MoveGenerator {

    /**
     * Original method used by the game.
     * It extracts the player's rack and delegates to the overloaded method.
     */
    public List<PlayableWord> getPlayableWordsList(Game game, GADDAG gaddag) {
        Player player = game.getCurrentPlayer();
        if (player == null || gaddag == null) return new ArrayList<>();
        return getPlayableWordsList(game.getBoard(), rackToCharArray(player), gaddag);
    }

    /**
     * Overloaded method used by the AI to simulate moves.
     */
    public List<PlayableWord> getPlayableWordsList(Board board, Character[] rackChars, GADDAG gaddag) {
        List<PlayableWord> playableMoves = new ArrayList<>();
        if (rackChars == null || gaddag == null) return playableMoves;

        for (int y = 0; y < Board.SIZE; y++) {
            for (int x = 0; x < Board.SIZE; x++) {
                Square square = board.getSquare(new Point(x, y));

                if (square != null && !square.isEmpty()) {
                    char hookLetter = square.getTile().getCharacter();

                    for (GADDAG.GaddagResult result : gaddag.findWordsWithRackAndHook(rackChars, hookLetter)) {
                        String word = result.word;
                        String gaddagRep = result.gaddagPath;

                        // Check Horizontal Placement
                        if (isPlayable(word, gaddagRep, x, y, Direction.HORIZONTAL, board, rackChars, gaddag)) {
                            playableMoves.add(new PlayableWord(x, y, word, Direction.HORIZONTAL, gaddagRep));
                        }
                        // Check Vertical Placement
                        if (isPlayable(word, gaddagRep, x, y, Direction.VERTICAL, board, rackChars, gaddag)) {
                            playableMoves.add(new PlayableWord(x, y, word, Direction.VERTICAL, gaddagRep));
                        }
                    }
                }
            }
        }
        return playableMoves;
    }

    /**
     * Checks if the word fits, if the AI has the letters, AND if it doesn't create invalid cross-words.
     */
    private boolean isPlayable(String word, String gaddagPath, int hX, int hY, Direction dir, Board board, Character[] rackChars, GADDAG gaddag) {
        int hookIndex = gaddagPath.indexOf('>') - 1;
        int startX = (dir == Direction.HORIZONTAL) ? hX - hookIndex : hX;
        int startY = (dir == Direction.VERTICAL) ? hY - hookIndex : hY;

        // 1. Check board boundaries
        if (startX < 0 || startY < 0) return false;
        if (dir == Direction.HORIZONTAL && startX + word.length() > Board.SIZE) return false;
        if (dir == Direction.VERTICAL && startY + word.length() > Board.SIZE) return false;

        // 2. Check contiguous letters (make sure we don't accidentally extend an existing word)
        // If the square exactly before the word is NOT empty, we reject the move
        int beforeX = (dir == Direction.HORIZONTAL) ? startX - 1 : startX;
        int beforeY = (dir == Direction.VERTICAL) ? startY - 1 : startY;
        if (beforeX >= 0 && beforeY >= 0) {
            Square beforeSq = board.getSquare(new Point(beforeX, beforeY));
            if (beforeSq != null && !beforeSq.isEmpty()) {
                // System.out.println("Debug: Word " + word + " rejected because of contiguous letter before.");
                return false; 
            }
        }

        // If the square exactly after the word is NOT empty, we reject the move
        int afterX = (dir == Direction.HORIZONTAL) ? startX + word.length() : startX;
        int afterY = (dir == Direction.VERTICAL) ? startY + word.length() : startY;
        if (afterX < Board.SIZE && afterY < Board.SIZE) {
            Square afterSq = board.getSquare(new Point(afterX, afterY));
            if (afterSq != null && !afterSq.isEmpty()) {
                // System.out.println("Debug: Word " + word + " rejected because of contiguous letter after.");
                return false;
            }
        }

        // 3. Rack check & Cross-word check
        List<Character> rackCopy = new ArrayList<>(java.util.Arrays.asList(rackChars));
        int lettersFromRack = 0;

        for (int i = 0; i < word.length(); i++) {
            int currentX = startX + (dir == Direction.HORIZONTAL ? i : 0);
            int currentY = startY + (dir == Direction.VERTICAL ? i : 0);
            Square sq = board.getSquare(new Point(currentX, currentY));

            char letterNeeded = word.charAt(i);

            if (sq != null && !sq.isEmpty()) {
                // Must match the board
                if (sq.getTile().getCharacter() != letterNeeded) return false; 
            } else {
                // Must consume from rack
                if (rackCopy.contains(letterNeeded)) {
                    rackCopy.remove((Character) letterNeeded);
                    lettersFromRack++;
                } else if (rackCopy.contains(' ')) { 
                    rackCopy.remove((Character) ' ');
                    lettersFromRack++;
                } else {
                    return false; // AI doesn't have the letter!
                }

                // Check cross words formed by this new tile
                if (!isValidCrossWord(board, currentX, currentY, letterNeeded, dir, gaddag)) {
                    // System.out.println("Debug: Word " + word + " rejected due to invalid cross word.");
                    return false;
                }
            }
        }

        return lettersFromRack > 0;
    }

    /**
     * Verifies that placing a new letter creates a valid perpendicular word on the board.
     */
    private boolean isValidCrossWord(Board board, int x, int y, char placedLetter, Direction mainDir, GADDAG gaddag) {
        Direction crossDir = (mainDir == Direction.HORIZONTAL) ? Direction.VERTICAL : Direction.HORIZONTAL;
        
        // Find start of cross word
        int startX = x;
        int startY = y;
        while (true) {
            int prevX = (crossDir == Direction.HORIZONTAL) ? startX - 1 : startX;
            int prevY = (crossDir == Direction.VERTICAL) ? startY - 1 : startY;
            if (prevX < 0 || prevY < 0) break;
            Square prevSq = board.getSquare(new Point(prevX, prevY));
            if (prevSq == null || prevSq.isEmpty()) break;
            startX = prevX;
            startY = prevY;
        }
        
        // Build the complete cross word
        StringBuilder crossWord = new StringBuilder();
        int curX = startX;
        int curY = startY;
        while (curX < Board.SIZE && curY < Board.SIZE) {
            if (curX == x && curY == y) {
                crossWord.append(placedLetter);
            } else {
                Square sq = board.getSquare(new Point(curX, curY));
                if (sq == null || sq.isEmpty()) break;
                crossWord.append(sq.getTile().getCharacter());
            }
            if (crossDir == Direction.HORIZONTAL) curX++;
            else curY++;
        }
        
        // If length is 1, no cross word was formed (just the placed letter)
        if (crossWord.length() == 1) return true;
        
        // Otherwise, the perpendicular word MUST exist in the dictionary
        return gaddag.containsWord(crossWord.toString());
    }

/**
     * Converts a player's rack into an array of characters, properly preserving blanks.
     */
    private Character[] rackToCharArray(Player player) {
        List<Tile> tiles = player.getRack().getTiles();
        Character[] chars = new Character[tiles.size()];
        for (int i = 0; i < tiles.size(); i++) {
            chars[i] = tiles.get(i).getCharacter();
        }
        return chars;
    }
}