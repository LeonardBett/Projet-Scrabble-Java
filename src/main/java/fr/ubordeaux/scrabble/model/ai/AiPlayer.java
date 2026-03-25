package fr.ubordeaux.scrabble.model.ai;

import fr.ubordeaux.scrabble.model.core.Game;
import fr.ubordeaux.scrabble.model.core.Move;
import fr.ubordeaux.scrabble.model.core.MoveGenerator;
import fr.ubordeaux.scrabble.model.core.PlayableWord;
import fr.ubordeaux.scrabble.model.core.Square;
import fr.ubordeaux.scrabble.model.core.Tile;
import fr.ubordeaux.scrabble.model.dictionary.Gaddag;
import fr.ubordeaux.scrabble.model.enums.Direction;
import fr.ubordeaux.scrabble.model.enums.PlayerColor;
import fr.ubordeaux.scrabble.model.interfaces.Player;
import fr.ubordeaux.scrabble.model.utils.GameLogger;
import fr.ubordeaux.scrabble.model.utils.Point;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents an artificial player (AI). Can be augmented with a Machine Learning agent (MlAgent) to
 * predict words.
 */
public class AiPlayer extends Player {

  private final MinimaxSolver solver;
  private MlAgent mlAgent;
  private PlayerColor color;

  /**
   * Constructs an AI Player with a defined difficulty and time limit.
   *
   * @param name The name of the player.
   * @param difficultyLevel Defines the search depth.
   * @param timeLimitSeconds The time limit allocated for the AI to play.
   * @param color The color assigned to the player.
   */
  public AiPlayer(String name, int difficultyLevel, int timeLimitSeconds, PlayerColor color) {
    super(name, color);
    this.solver = new MinimaxSolver(difficultyLevel, timeLimitSeconds);
  }

  /**
   * Injects the Machine Learning agent to enable neural network word search.
   *
   * @param mlAgent The initialized Machine Learning agent.
   */
  public void setMlAgent(MlAgent mlAgent) {
    this.mlAgent = mlAgent;
  }

  /**
   * Toggles the AI into Expectiminimax mode or standard Minimax mode.
   *
   * @param enable True to enable Expectiminimax, false for standard Minimax.
   */
  public void setExpectiminimaxMode(boolean enable) {
    solver.setUseExpectiminimax(enable);
    GameLogger.logVerbose("AI [" + getName() + "] mode changed to: "
        + (enable ? "EXPECTIMINIMAX" : "Classic MINIMAX"));
  }

  /**
   * Checks if the AI is using the Expectiminimax strategy.
   *
   * @return True if Expectiminimax is active, false otherwise.
   */
  public boolean isExpectiminimaxMode() {
    return solver.isUsingExpectiminimax();
  }

  /**
   * Extracts the letters currently in the AI's rack as a string format.
   *
   * @return A string containing the characters from the rack.
   */
  private String getRackAsString() {
    StringBuilder sb = new StringBuilder();
    for (Tile t : getRack().getTiles()) {
      sb.append(t.getCharacter());
    }
    return sb.toString();
  }

  /**
   * Analyzes the game state and executes the best action for the current turn. Integrates a
   * fallback mechanism if the ML agent is missing or fails.
   *
   * @param game The current game instance.
   * @param gaddag The Gaddag dictionary used to validate moves.
   */
  public void playTurn(Game game, Gaddag gaddag) {
    GameLogger.logVerbose("AI " + getName() + " is computing its move...");

    PlayableWord bestPlay = null;

    // Phase 1: Machine Learning Search Step
    if (this.mlAgent != null) {
      if (this.mlAgent.isModelLoaded()) {
        String rackStr = getRackAsString();
        GameLogger.logVerbose("[ML] Neural Network is analyzing "
            +
            "the current rack: [" + rackStr + "]");

        List<String> mlPredictions = mlAgent.predictWords(rackStr, 100);

        MoveGenerator moveGen = new MoveGenerator();
        List<PlayableWord> allLegalMoves = moveGen.getPlayableWordsList(game, gaddag);

        for (String predictedWord : mlPredictions) {
          for (PlayableWord legalMove : allLegalMoves) {
            if (legalMove.getWord().equals(predictedWord)) {
              bestPlay = legalMove;
              GameLogger.logVerbose(
                  "[ML] SUCCESS: Found legal board placement for predicted word: " + predictedWord);
              break;
            }
          }
          if (bestPlay != null) {
            break;
          }
        }

        if (bestPlay == null) {
          GameLogger.logVerbose("[ML] NOTE: None of the top predicted words fit on the board. "
              + "Falling back to algorithmic search.");
        }
      } else {
        GameLogger.logVerbose("[ML] Model is missing. Bypassing neural network and defaulting "
            + "to algorithmic search.");
      }
    }

    // Phase 2: Algorithmic Fallback & Resolution Step
    if (bestPlay == null) {
      GameLogger.logVerbose("[AI] Proceeding with "
          + (isExpectiminimaxMode() ? "Expectiminimax" : "Minimax") + " algorithm...");
      bestPlay = solver.findBestMove(game, gaddag);
    }

    Move moveToExecute;
    if (bestPlay != null) {
      GameLogger.logVerbose("The AI decided to play: " + bestPlay.getWord() + " for "
          + bestPlay.getScore() + " points.");

      int hookIndex = bestPlay.getGaddagRepresentation().indexOf('>') - 1;

      int startX = bestPlay.getDirection() == Direction.HORIZONTAL
          ? bestPlay.getHookX() - hookIndex
          : bestPlay.getHookX();
      int startY = bestPlay.getDirection() == Direction.VERTICAL
          ? bestPlay.getHookY() - hookIndex
          : bestPlay.getHookY();

      Point startPos = new Point(startX, startY);

      List<Character> myRack = new ArrayList<>();
      for (Tile t : getRack().getTiles()) {
        myRack.add(t.getCharacter());
      }

      List<Tile> wordTiles = new ArrayList<>();
      String word = bestPlay.getWord();

      for (int i = 0; i < word.length(); i++) {
        int currentX = startX
            + (bestPlay.getDirection() == Direction.HORIZONTAL ? i : 0);
        int currentY = startY
            + (bestPlay.getDirection() == Direction.VERTICAL ? i : 0);

        Square sq = game.getBoard().getSquare(new Point(currentX, currentY));

        if (sq != null && sq.isEmpty()) {
          char neededChar = word.charAt(i);

          if (myRack.contains(neededChar)) {
            myRack.remove((Character) neededChar);
            wordTiles.add(new Tile(neededChar));
          } else {
            myRack.remove((Character) ' ');
            wordTiles.add(new Tile(neededChar, true));
          }
        }
      }

      moveToExecute = Move.createPlay(this, wordTiles, startPos, bestPlay.getDirection());
    } else {
      GameLogger.logVerbose("The AI could not find any playable word and passes its turn.");
      moveToExecute = Move.createPass(this);
    }

    game.executeMove(moveToExecute);
  }

  /**
   * Updates the maximum allowed thinking time for the AI player.
   *
   * @param timeLimitSeconds The requested time limit in seconds.
   */
  public void setTimeLimitSeconds(int timeLimitSeconds) {
    solver.setTimeLimitSeconds(timeLimitSeconds);
  }
}