package fr.u_bordeaux.scrabble.model.ai;

import fr.u_bordeaux.scrabble.model.core.*;
import fr.u_bordeaux.scrabble.model.dictionary.GADDAG;
import fr.u_bordeaux.scrabble.model.enums.Direction;
import fr.u_bordeaux.scrabble.model.interfaces.Player;
import fr.u_bordeaux.scrabble.model.utils.Point;

import java.util.*;

/**
 * Implements realistic Minimax and Expectiminimax algorithms for AI decision making.
 * Performs deep simulation by generating opponent moves from sampled remaining tiles.
 */
public class MinimaxSolver {

    private final MoveGenerator moveGenerator;
    private final int maxDepth;
    private boolean useExpectiminimax;
    private final Random random;

    // Constant to balance performance (Too high = very slow AI)
    // 5 is a good compromise for testing in real conditions without waiting 10 minutes.
    private static final int SAMPLES_COUNT = 5; 

    public MinimaxSolver(int maxDepth) {
        this.moveGenerator = new MoveGenerator();
        this.maxDepth = maxDepth;
        this.useExpectiminimax = false; 
        this.random = new Random();
    }

    public void setUseExpectiminimax(boolean useExpectiminimax) {
        this.useExpectiminimax = useExpectiminimax;
    }

    public boolean isUsingExpectiminimax() {
        return useExpectiminimax;
    }

    public PlayableWord findBestMove(Game game, GADDAG gaddag) {
        List<PlayableWord> possibleMoves = moveGenerator.getPlayableWordsList(game, gaddag);
        if (possibleMoves.isEmpty()) return null; 

        PlayableWord bestMove = null;
        double bestScore = Double.NEGATIVE_INFINITY;
        
        // Calculate unseen letters (Bag + Opponents) once for the whole turn
        List<Character> unseenTiles = getUnseenTiles(game);

        for (PlayableWord move : possibleMoves) {
            // 1. Calculate the AI's immediate gain
            int immediateScore = simulateAndScoreWord(game.getBoard(), move);
            double rackLeaveScore = evaluateRackLeave(game, move);
            double totalScore = immediateScore + rackLeaveScore;
            
            // Save the real score for display purposes
            move.setScore(immediateScore);

            // 2. Simulate the opponent's response (Depth)
            if (maxDepth > 1) {
                // Temporarily place our word on the board so the opponent can hook onto it
                List<Square> placedSquares = placeWordTemporarily(game.getBoard(), move);

                if (useExpectiminimax) {
                    // Expectiminimax: Subtract the AVERAGE of the best opponent moves
                    totalScore -= expectiminimax(game.getBoard(), unseenTiles, gaddag);
                } else {
                    // Minimax: Subtract the WORST POSSIBLE MOVE from our samples
                    totalScore -= minimax(game.getBoard(), unseenTiles, gaddag);
                }

                // Remove our word to test the next one
                removeWordTemporarily(placedSquares);
            }

            if (totalScore > bestScore) {
                bestScore = totalScore;
                bestMove = move;
            }
        }

        return bestMove;
    }

    /**
     * EXPECTIMINIMAX: Realistic Monte-Carlo simulation.
     * Generates probable racks and averages the best response.
     */
    private double expectiminimax(Board board, List<Character> unseen, GADDAG gaddag) {
        double totalExpectedOpponentScore = 0.0;
        
        for (int i = 0; i < SAMPLES_COUNT; i++) {
            Character[] simulatedRack = drawRandomRack(unseen, 7);
            totalExpectedOpponentScore += getBestOpponentScore(board, simulatedRack, gaddag);
        }
        
        return totalExpectedOpponentScore / SAMPLES_COUNT;
    }

    /**
     * MINIMAX: Worst-case scenario simulation.
     * Among the drawn racks, keeps the one that deals the most damage.
     */
    private double minimax(Board board, List<Character> unseen, GADDAG gaddag) {
        double maxDamage = 0.0;
        
        for (int i = 0; i < SAMPLES_COUNT; i++) {
            Character[] simulatedRack = drawRandomRack(unseen, 7);
            double oppScore = getBestOpponentScore(board, simulatedRack, gaddag);
            if (oppScore > maxDamage) {
                maxDamage = oppScore;
            }
        }
        
        return maxDamage; // Returns the score of the worst enemy attack
    }

    /**
     * Finds the best possible score for a simulated rack on the current board.
     */
    private double getBestOpponentScore(Board board, Character[] rack, GADDAG gaddag) {
        List<PlayableWord> oppMoves = moveGenerator.getPlayableWordsList(board, rack, gaddag);
        double maxScore = 0;
        
        for (PlayableWord move : oppMoves) {
            int score = simulateAndScoreWord(board, move);
            if (score > maxScore) {
                maxScore = score;
            }
        }
        return maxScore;
    }

    /**
     * Precisely calculates the letters that are neither on the board nor in the AI's rack.
     */
    private List<Character> getUnseenTiles(Game game) {
        // 1. Standard French Scrabble distribution
        Map<Character, Integer> counts = new HashMap<>();
        char[] letters = "ABCDEFGHIJKLMNOPQRSTUVWXYZ ".toCharArray();
        int[] qty = {9,2,2,3,15,2,2,2,8,1,1,5,3,6,6,2,1,6,6,6,6,2,1,1,1,1,2}; 
        for(int i = 0; i < letters.length; i++) counts.put(letters[i], qty[i]);

        // 2. Subtract tiles from the board
        for (int x = 0; x < Board.SIZE; x++) {
            for (int y = 0; y < Board.SIZE; y++) {
                Square sq = game.getBoard().getSquare(new Point(x, y));
                if (sq != null && !sq.isEmpty()) {
                    char c = sq.getTile().getCharacter();
                    counts.put(c, counts.getOrDefault(c, 1) - 1);
                }
            }
        }

        // 3. Subtract tiles from the AI's rack
        for (Tile t : game.getCurrentPlayer().getRack().getTiles()) {
            char c = t.getCharacter();
            counts.put(c, counts.getOrDefault(c, 1) - 1);
        }

        // Create the final list of unseen tiles
        List<Character> unseen = new ArrayList<>();
        for (Map.Entry<Character, Integer> entry : counts.entrySet()) {
            for (int i = 0; i < entry.getValue(); i++) {
                if (entry.getValue() > 0) unseen.add(entry.getKey());
            }
        }
        return unseen;
    }

    /**
     * Draws N random letters from the list of unseen letters.
     */
    private Character[] drawRandomRack(List<Character> unseen, int size) {
        List<Character> copy = new ArrayList<>(unseen);
        Collections.shuffle(copy, random);
        int rackSize = Math.min(size, copy.size());
        
        Character[] rack = new Character[rackSize];
        for (int i = 0; i < rackSize; i++) {
            rack[i] = copy.get(i);
        }
        return rack;
    }

    // =========================================================================
    // UTILITY METHODS FOR PHYSICAL BOARD SIMULATION
    // =========================================================================

    private int simulateAndScoreWord(Board board, PlayableWord move) {
        List<Square> placedSquares = placeWordTemporarily(board, move);
        
        // Also retrieve the complete list (old + new tiles) for scoring
        List<Square> wordSquares = new ArrayList<>();
        int startX = getStartX(board, move);
        int startY = getStartY(board, move);
        
        for (int i = 0; i < move.getWord().length(); i++) {
            Point p = new Point(
                move.getDirection() == Direction.HORIZONTAL ? startX + i : startX,
                move.getDirection() == Direction.VERTICAL ? startY + i : startY
            );
            wordSquares.add(board.getSquare(p));
        }

        int score = 0;
        try {
            if (!wordSquares.isEmpty()) {
                score = Scoring.calculateWordScore(wordSquares, placedSquares);
            }
        } catch (Exception e) { /* Ignored for simulation */ }
        
        removeWordTemporarily(placedSquares);
        return score;
    }

    private List<Square> placeWordTemporarily(Board board, PlayableWord move) {
        List<Square> newlyPlacedSquares = new ArrayList<>();
        String word = move.getWord();
        Direction dir = move.getDirection();
        
        int startX = getStartX(board, move);
        int startY = getStartY(board, move);
        
        for (int i = 0; i < word.length(); i++) {
            Point p = new Point(
                dir == Direction.HORIZONTAL ? startX + i : startX,
                dir == Direction.VERTICAL ? startY + i : startY
            );
            Square sq = board.getSquare(p);
            
            if (sq != null && sq.isEmpty()) {
                sq.setTile(new Tile(word.charAt(i)));
                newlyPlacedSquares.add(sq);
            }
        }
        return newlyPlacedSquares;
    }

    private void removeWordTemporarily(List<Square> placedSquares) {
        for (Square sq : placedSquares) {
            sq.setTile(null);
        }
    }

    private int getStartX(Board board, PlayableWord move) {
        Square hookSquare = board.getSquare(new Point(move.getHookX(), move.getHookY()));
        char hookChar = hookSquare != null && !hookSquare.isEmpty() ? hookSquare.getTile().getCharacter() : '\0';
        for (int i = 0; i < move.getWord().length(); i++) {
            if (move.getWord().charAt(i) == hookChar) {
                return (move.getDirection() == Direction.HORIZONTAL) ? move.getHookX() - i : move.getHookX();
            }
        }
        return move.getHookX();
    }

    private int getStartY(Board board, PlayableWord move) {
        Square hookSquare = board.getSquare(new Point(move.getHookX(), move.getHookY()));
        char hookChar = hookSquare != null && !hookSquare.isEmpty() ? hookSquare.getTile().getCharacter() : '\0';
        for (int i = 0; i < move.getWord().length(); i++) {
            if (move.getWord().charAt(i) == hookChar) {
                return (move.getDirection() == Direction.VERTICAL) ? move.getHookY() - i : move.getHookY();
            }
        }
        return move.getHookY();
    }

    private double evaluateRackLeave(Game game, PlayableWord move) {
        Player player = game.getCurrentPlayer();
        Board board = game.getBoard();
        
        // 1. Retrieve letters from the current rack
        List<String> rackLetters = new ArrayList<>();
        String rackStr = player.getRack().toString().replaceAll("[^A-Z ]", "");
        for(char c : rackStr.toCharArray()) rackLetters.add(String.valueOf(c));

        String word = move.getWord();
        Direction dir = move.getDirection();
        
        // 2. Find starting coordinates
        int startX = getStartX(board, move);
        int startY = getStartY(board, move);

        // 3. Remove letters placed on the board from the rack leave
        for (int i = 0; i < word.length(); i++) {
            Point p = new Point(
                dir == Direction.HORIZONTAL ? startX + i : startX,
                dir == Direction.VERTICAL ? startY + i : startY
            );
            Square s = board.getSquare(p);
            
            // If the square is empty, it means the letter comes from our hand
            if (s != null && s.isEmpty()) {
                rackLetters.remove(String.valueOf(word.charAt(i)));
            }
        }

        // 4. Calculate the rack leave score (Heuristic)
        double leaveScore = 0.0;
        int vowels = 0;
        int consonants = 0;

        for (String l : rackLetters) {
            if ("AEIOUY".contains(l)) vowels++;
            else if (!l.equals(" ")) consonants++; // Do not count the blank (joker) as a consonant
            
            // Bonus / Malus depending on the letters
            if (l.equals("S")) leaveScore += 8.0; 
            if (l.equals(" ")) leaveScore += 15.0; // Blank = Huge bonus
            if ("JKQXZ".contains(l)) leaveScore -= 6.0; // Hard letters to keep
            if ("VWH".contains(l)) leaveScore -= 2.0;
        }

        // 5. Consonant-Vowel balance Bonus/Malus
        if (rackLetters.size() >= 2) {
            if (Math.abs(vowels - consonants) <= 1) {
                leaveScore += 5.0; // Excellent balance
            } else if (vowels == 0 || consonants == 0) {
                leaveScore -= 12.0; // Disaster (All consonants or All vowels)
            }
        }

        return leaveScore;
    }
}