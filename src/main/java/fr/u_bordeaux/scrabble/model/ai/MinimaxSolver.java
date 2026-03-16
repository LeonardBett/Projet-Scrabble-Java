package fr.u_bordeaux.scrabble.model.ai;

import fr.u_bordeaux.scrabble.model.core.Board;
import fr.u_bordeaux.scrabble.model.core.Game;
import fr.u_bordeaux.scrabble.model.core.MoveGenerator;
import fr.u_bordeaux.scrabble.model.core.PlayableWord;
import fr.u_bordeaux.scrabble.model.core.Scoring;
import fr.u_bordeaux.scrabble.model.core.Square;
import fr.u_bordeaux.scrabble.model.core.Tile;
import fr.u_bordeaux.scrabble.model.dictionary.GADDAG;
import fr.u_bordeaux.scrabble.model.enums.Direction;
import fr.u_bordeaux.scrabble.model.interfaces.Player;
import fr.u_bordeaux.scrabble.model.utils.Point;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * Implements realistic Minimax and Expectiminimax algorithms for AI decision making.
 * Performs deep simulation by generating opponent moves from sampled remaining tiles.
 */
public class MinimaxSolver {

  private final MoveGenerator moveGenerator;
  private final int maxDepth;
  private boolean useExpectiminimax;
  private final Random random;
  private long timeLimitMs;

  // Constant to balance performance. Too high implies a very slow AI.
  private static final int SAMPLES_COUNT = 200;

  /**
   * Constructs the solver with a specified depth and time limit.
   *
   * @param maxDepth The depth of the search tree.
   * @param timeLimitSeconds The thinking time limit allocated in seconds.
   */
  public MinimaxSolver(int maxDepth, int timeLimitSeconds) {
    this.moveGenerator = new MoveGenerator();
    this.maxDepth = maxDepth;
    this.useExpectiminimax = false;
    this.random = new Random();
    this.timeLimitMs = timeLimitSeconds * 1000L;
  }

  /**
   * Toggles the use of the Expectiminimax algorithm.
   *
   * @param useExpectiminimax True to enable Expectiminimax, false to use standard Minimax.
   */
  public void setUseExpectiminimax(boolean useExpectiminimax) {
    this.useExpectiminimax = useExpectiminimax;
  }

  /**
   * Checks if the Expectiminimax algorithm is currently enabled.
   *
   * @return True if enabled, false otherwise.
   */
  public boolean isUsingExpectiminimax() {
    return useExpectiminimax;
  }

  /**
   * Finds the best possible move for the AI within the allocated time limit.
   *
   * @param game The current game state.
   * @param gaddag The GADDAG dictionary used to evaluate valid moves.
   * @return The best PlayableWord found, or null if no moves are possible.
   */
  public PlayableWord findBestMove(Game game, GADDAG gaddag) {
    long startTime = System.currentTimeMillis();

    List<PlayableWord> possibleMoves = moveGenerator.getPlayableWordsList(game, gaddag);
    if (possibleMoves.isEmpty()) {
      return null;
    }

    PlayableWord bestMove = null;
    double bestScore = Double.NEGATIVE_INFINITY;

    List<Character> unseenTiles = getUnseenTiles(game);

    for (PlayableWord move : possibleMoves) {
      // Check if the allocated time limit has been reached
      if (System.currentTimeMillis() - startTime >= this.timeLimitMs) {
        System.out.println("Time limit reached. The AI stops and plays the best move found.");
        break;
      }

      // Calculate the AI's immediate point gain and rack leave heuristic
      int immediateScore = simulateAndScoreWord(game.getBoard(), move);
      double rackLeaveScore = evaluateRackLeave(game, move);
      double totalScore = immediateScore + rackLeaveScore;

      // Save the immediate score for final display purposes
      move.setScore(immediateScore);

      // Simulate the opponent's response for a specified depth
      if (maxDepth > 1) {
        // Temporarily place the word on the board to allow opponent simulation to hook onto it
        List<Square> placedSquares = placeWordTemporarily(game.getBoard(), move);

        if (useExpectiminimax) {
          // Expectiminimax subtracts the average of the best opponent moves
          totalScore -= expectiminimax(game.getBoard(), unseenTiles, gaddag, startTime);
        } else {
          // Minimax subtracts the worst possible move from the opponent
          totalScore -= minimax(game.getBoard(), unseenTiles, gaddag, startTime);
        }
        
        // Remove the temporary word to evaluate the next potential move
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
   * Executes the Expectiminimax algorithm through realistic Monte-Carlo simulation.
   * Generates probable racks and averages the best response.
   *
   * @param board The current board state.
   * @param unseen The list of letters not currently on the board or in the AI's rack.
   * @param gaddag The GADDAG dictionary.
   * @param startTime The timestamp when the move search began.
   * @return The average expected opponent score.
   */
  private double expectiminimax(
      Board board, List<Character> unseen, GADDAG gaddag, long startTime) {
    double totalExpectedOpponentScore = 0.0;
    int samplesEvaluated = 0;

    for (int i = 0; i < SAMPLES_COUNT; i++) {
      if (System.currentTimeMillis() - startTime >= this.timeLimitMs) {
        break;
      }
      Character[] simulatedRack = drawRandomRack(unseen, 7);
      totalExpectedOpponentScore += getBestOpponentScore(board, simulatedRack, gaddag);
      samplesEvaluated++;
    }

    // Security check to prevent division by zero if timeout occurred immediately
    if (samplesEvaluated == 0) {
      return 0.0;
    }

    return totalExpectedOpponentScore / samplesEvaluated;
  }

  /**
   * Executes the Minimax algorithm by evaluating the worst-case scenario.
   * Among the drawn racks, keeps the one that deals the highest damage.
   *
   * @param board The current board state.
   * @param unseen The list of letters not currently on the board or in the AI's rack.
   * @param gaddag The GADDAG dictionary.
   * @param startTime The timestamp when the move search began.
   * @return The highest possible opponent score from the simulated samples.
   */
  private double minimax(Board board, List<Character> unseen, GADDAG gaddag, long startTime) {
    double maxDamage = 0.0;

    for (int i = 0; i < SAMPLES_COUNT; i++) {
      if (System.currentTimeMillis() - startTime >= this.timeLimitMs) {
        break;
      }
      Character[] simulatedRack = drawRandomRack(unseen, 7);
      double oppScore = getBestOpponentScore(board, simulatedRack, gaddag);
      if (oppScore > maxDamage) {
        maxDamage = oppScore;
      }
    }

    return maxDamage;
  }

  /**
   * Finds the maximum possible score for a simulated rack on the current board.
   *
   * @param board The current board state.
   * @param rack The simulated opponent rack.
   * @param gaddag The GADDAG dictionary.
   * @return The maximum score achieved by the best move.
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
   * Calculates the letters that are neither on the board nor in the AI's rack.
   *
   * @param game The current game instance.
   * @return A list of remaining unseen characters.
   */
  private List<Character> getUnseenTiles(Game game) {
    Map<Character, Integer> counts = new HashMap<>();
    char[] letters = "ABCDEFGHIJKLMNOPQRSTUVWXYZ ".toCharArray();
    int[] qty = {9, 2, 2, 3, 15, 2, 2, 2, 8, 1, 1, 5, 3, 6, 6, 2, 1, 6, 6, 6, 6, 2, 1, 1, 1, 1, 2};
    
    for (int i = 0; i < letters.length; i++) {
      counts.put(letters[i], qty[i]);
    }

    // Subtract tiles already present on the board
    for (int x = 0; x < Board.SIZE; x++) {
      for (int y = 0; y < Board.SIZE; y++) {
        Square sq = game.getBoard().getSquare(new Point(x, y));
        if (sq != null && !sq.isEmpty()) {
          char c = sq.getTile().getCharacter();
          counts.put(c, counts.getOrDefault(c, 1) - 1);
        }
      }
    }

    // Subtract tiles present in the AI's rack
    for (Tile t : game.getCurrentPlayer().getRack().getTiles()) {
      char c = t.getCharacter();
      counts.put(c, counts.getOrDefault(c, 1) - 1);
    }

    List<Character> unseen = new ArrayList<>();
    for (Map.Entry<Character, Integer> entry : counts.entrySet()) {
      for (int i = 0; i < entry.getValue(); i++) {
        if (entry.getValue() > 0) {
          unseen.add(entry.getKey());
        }
      }
    }
    return unseen;
  }

  /**
   * Draws a specified number of random letters from the unseen list.
   *
   * @param unseen The list of available unseen characters.
   * @param size The number of characters to draw.
   * @return An array of drawn characters representing a simulated rack.
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

  /**
   * Simulates a move on the board and calculates its point value.
   *
   * @param board The current board.
   * @param move The word to be played.
   * @return The score calculated for the simulated move.
   */
  private int simulateAndScoreWord(Board board, PlayableWord move) {
    List<Square> placedSquares = placeWordTemporarily(board, move);

    List<Square> wordSquares = new ArrayList<>();
    int startX = getStartX(board, move);
    int startY = getStartY(board, move);

    for (int i = 0; i < move.getWord().length(); i++) {
      Point p = new Point(
          move.getDirection() == Direction.HORIZONTAL ? startX + i : startX,
          move.getDirection() == Direction.VERTICAL ? startY + i : startY);
      wordSquares.add(board.getSquare(p));
    }

    int score = 0;
    try {
      if (!wordSquares.isEmpty()) {
        score = Scoring.calculateWordScore(wordSquares, placedSquares);
      }
    } catch (Exception e) {
      // Exception ignored during simulation
    }

    removeWordTemporarily(placedSquares);
    return score;
  }

  /**
   * Temporarily places a word on the board for simulation purposes.
   *
   * @param board The current board.
   * @param move The word to place.
   * @return A list of the squares that were newly modified.
   */
  private List<Square> placeWordTemporarily(Board board, PlayableWord move) {
    List<Square> newlyPlacedSquares = new ArrayList<>();
    String word = move.getWord();
    Direction dir = move.getDirection();

    int startX = getStartX(board, move);
    int startY = getStartY(board, move);

    for (int i = 0; i < word.length(); i++) {
      Point p = new Point(
          dir == Direction.HORIZONTAL ? startX + i : startX,
          dir == Direction.VERTICAL ? startY + i : startY);
      Square sq = board.getSquare(p);

      if (sq != null && sq.isEmpty()) {
        sq.setTile(new Tile(word.charAt(i)));
        newlyPlacedSquares.add(sq);
      }
    }
    return newlyPlacedSquares;
  }

  /**
   * Removes temporary tiles placed during simulation.
   *
   * @param placedSquares The list of squares to clear.
   */
  private void removeWordTemporarily(List<Square> placedSquares) {
    for (Square sq : placedSquares) {
      sq.setTile(null);
    }
  }

  /**
   * Retrieves the starting X coordinate for a PlayableWord.
   *
   * @param board The current board.
   * @param move The move to process.
   * @return The starting X index.
   */
  private int getStartX(Board board, PlayableWord move) {
    int hookIndex = move.getGaddagRepresentation().indexOf('>') - 1;
    return (move.getDirection() == Direction.HORIZONTAL)
        ? move.getHookX() - hookIndex
        : move.getHookX();
  }

  /**
   * Retrieves the starting Y coordinate for a PlayableWord.
   *
   * @param board The current board.
   * @param move The move to process.
   * @return The starting Y index.
   */
  private int getStartY(Board board, PlayableWord move) {
    int hookIndex = move.getGaddagRepresentation().indexOf('>') - 1;
    return (move.getDirection() == Direction.VERTICAL)
        ? move.getHookY() - hookIndex
        : move.getHookY();
  }

  /**
   * Evaluates the strategic value of the tiles left in the rack after a potential move.
   *
   * @param game The current game state.
   * @param move The simulated move.
   * @return A heuristic score representing the quality of the remaining rack.
   */
  private double evaluateRackLeave(Game game, PlayableWord move) {
    Player player = game.getCurrentPlayer();
    Board board = game.getBoard();

    List<String> rackLetters = new ArrayList<>();
    for (Tile t : player.getRack().getTiles()) {
      rackLetters.add(String.valueOf(t.getCharacter()));
    }

    String word = move.getWord();
    Direction dir = move.getDirection();

    int startX = getStartX(board, move);
    int startY = getStartY(board, move);

    for (int i = 0; i < word.length(); i++) {
      Point p = new Point(
          dir == Direction.HORIZONTAL ? startX + i : startX,
          dir == Direction.VERTICAL ? startY + i : startY);
      Square s = board.getSquare(p);

      // If the square is empty, the letter is taken from the hand
      if (s != null && s.isEmpty()) {
        rackLetters.remove(String.valueOf(word.charAt(i)));
      }
    }

    double leaveScore = 0.0;
    int vowels = 0;
    int consonants = 0;

    for (String l : rackLetters) {
      if ("AEIOUY".contains(l)) {
        vowels++;
      } else if (!l.equals(" ")) {
        consonants++; // Do not count the blank joker as a consonant
      }

      if (l.equals("S")) {
        leaveScore += 8.0;
      }
      if (l.equals(" ")) {
        leaveScore += 15.0; // Huge bonus for retaining the blank
      }
      if ("JKQXZ".contains(l)) {
        leaveScore -= 6.0; // Penalty for difficult letters
      }
      if ("VWH".contains(l)) {
        leaveScore -= 2.0;
      }
    }

    // Apply consonant-vowel balance bonus or penalty
    if (rackLetters.size() >= 2) {
      if (Math.abs(vowels - consonants) <= 1) {
        leaveScore += 5.0; // Excellent balance
      } else if (vowels == 0 || consonants == 0) {
        leaveScore -= 12.0; // Poor balance penalty
      }
    }

    return leaveScore;
  }

  /**
   * Updates the search time limit.
   *
   * @param timeLimitSeconds The new time limit in seconds.
   */
  public void setTimeLimitSeconds(int timeLimitSeconds) {
    this.timeLimitMs = timeLimitSeconds * 1000L;
  }
}