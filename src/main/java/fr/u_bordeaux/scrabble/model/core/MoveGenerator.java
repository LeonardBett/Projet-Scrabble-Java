package fr.u_bordeaux.scrabble.model.core;

import fr.u_bordeaux.scrabble.model.dictionary.GADDAG;
import fr.u_bordeaux.scrabble.model.utils.Point;
import fr.u_bordeaux.scrabble.model.enums.Direction;
import fr.u_bordeaux.scrabble.model.interfaces.Player;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class MoveGenerator {

    /**
     *
     * @param game
     * @param gaddag
     * @return Return all words playables with the actual game and an actual rack
     */
    public List<PlayableWord> getPlayableWordsList(Game game, GADDAG gaddag) {
        // On utilise une List car le même mot peut être joué à plusieurs endroits !
        List<PlayableWord> playableMoves = new ArrayList<>();
        Board board = game.getBoard();
        Player player = game.getCurrentPlayer();

        if (player == null || gaddag == null) return playableMoves;

        Character[] rackChars = rackToCharArray(player);

        for (int y = 0; y < Board.SIZE; y++) {
            for (int x = 0; x < Board.SIZE; x++) {
                Square square = board.getSquare(new Point(x, y));

                if (square != null && !square.isEmpty()) {
                    char hookLetter = square.getTile().getCharacter();

                    // Récupération des mots ET de leur représentation GADDAG
                    for (GADDAG.GaddagResult result : gaddag.findWordsWithRackAndHook(rackChars, hookLetter)) {
                        String word = result.word;
                        String gaddagRep = result.gaddagPath;

                        // Vérification Horizontale
                        if (isPlayable(word, x, y, Direction.HORIZONTAL, board)) {
                            playableMoves.add(new PlayableWord(x, y, word, Direction.HORIZONTAL, gaddagRep));
                        }

                        // Vérification Verticale
                        if (isPlayable(word, x, y, Direction.VERTICAL, board)) {
                            playableMoves.add(new PlayableWord(x, y, word, Direction.VERTICAL, gaddagRep));
                        }
                    }
                }
            }
        }
        return playableMoves;
    }

    /**
     *
     * @param word
     * @param hX
     * @param hY
     * @param dir
     * @param board
     * @return Check if the word is playables without crossing the board borders
     */
    private boolean isPlayable(String word, int hX, int hY, Direction dir, Board board) {
        char hookChar = board.getSquare(new Point(hX, hY)).getTile().getCharacter();

        for (int i = 0; i < word.length(); i++) {
            if (word.charAt(i) == hookChar) {
                int startX = (dir == Direction.HORIZONTAL) ? hX - i : hX;
                int startY = (dir == Direction.VERTICAL) ? hY - i : hY;

                // Check board limits and collision
                if (!isOutOfBounds(word, startX, startY, dir) && !hasInvalidCollision(word, startX, startY, dir, board)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     *
     * @param word
     * @param x
     * @param y
     * @param dir
     * @return Retrun true if the word is out of bounds
     */
    private boolean isOutOfBounds(String word, int x, int y, Direction dir) {
        if (x < 0 || y < 0) return true;
        if (dir == Direction.HORIZONTAL && x + word.length() > Board.SIZE) return true;
        return dir == Direction.VERTICAL && y + word.length() > Board.SIZE;
    }

    /**
     *
     * @param word
     * @param startX
     * @param startY
     * @param dir
     * @param board
     * @return Return True if the word is blocked by some other letters on the board
     */
    private boolean hasInvalidCollision(String word, int startX, int startY, Direction dir, Board board) {
        for (int i = 0; i < word.length(); i++) {
            int curX = (dir == Direction.HORIZONTAL) ? startX + i : startX;
            int curY = (dir == Direction.VERTICAL) ? startY + i : startY;

            Square s = board.getSquare(new Point(curX, curY));
            if (s != null && !s.isEmpty()) {
                if (s.getTile().getCharacter() != word.charAt(i)) return true;
            }
        }
        return false;
    }

    /**
     *
     * @param player
     * @return
     */
    private Character[] rackToCharArray(Player player) {
        String rackString = player.getRack().toString().toUpperCase().replaceAll("[^A-Z]", "");
        Character[] chars = new Character[rackString.length()];
        for (int i = 0; i < rackString.length(); i++) chars[i] = rackString.charAt(i);
        return chars;
    }
}