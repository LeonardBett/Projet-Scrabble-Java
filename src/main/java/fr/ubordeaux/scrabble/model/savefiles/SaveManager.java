package fr.ubordeaux.scrabble.model.savefiles;

import fr.ubordeaux.scrabble.model.ai.AiPlayer;
import fr.ubordeaux.scrabble.model.core.Board;
import fr.ubordeaux.scrabble.model.core.Game;
import fr.ubordeaux.scrabble.model.core.Move;
import fr.ubordeaux.scrabble.model.core.Square;
import fr.ubordeaux.scrabble.model.enums.Direction;
import fr.ubordeaux.scrabble.model.enums.MoveType;
import fr.ubordeaux.scrabble.model.interfaces.Player;
import fr.ubordeaux.scrabble.model.utils.GameLogger;
import fr.ubordeaux.scrabble.model.utils.Point;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Manages the generation of Scrabble save files in ASCII format.
 * Follows the structure: [settings], [game], and [history].
 */
public class SaveManager {

  /**
   * Constructs a new SaveManager instance.
   */
  public SaveManager() {
  }

  /**
   * Saves the current game state, including settings, board, and history, into a text file.
   *
   * @param game     The current game instance to be saved.
   * @param filePath The destination path of the save file.
   * @throws IOException If an error occurs during file writing.
   */
  public void saveGame(Game game, String filePath) throws IOException {
    try (PrintWriter writer = new PrintWriter(new FileWriter(filePath))) {

      writer.println("[settings] # Global game parameters");
      writer.println("blitz " + game.isBlitzModeEnabled());
      writer.println("super-scrabble " + (game.getBoard().getSize() == 21));
      writer.println("players-count " + game.getPlayers().size());
      writer.println("debug " + GameLogger.isDebug());
      writer.println("verbose " + GameLogger.isVerbose());
      List<Player> allPlayers = game.getPlayers();
      for (int i = 0; i < allPlayers.size(); i++) {
        Player p = allPlayers.get(i);
        if (p instanceof AiPlayer ia) {
          String aiMode;
          if (ia.isExpectiminimaxMode()) {
            aiMode = "Expectiminimax";
          } else if (ia.getMlAgent() != null) {
            aiMode = "Machine Learning";
          } else {
            aiMode = "MinMax";
          }
          writer.println("player-" + (i + 1) + "-type ai");
          writer.println("player-" + (i + 1) + "-ai-mode " + aiMode);
          writer.println("player-" + (i + 1) + "-name " + ia.getName());
        }
      }
      writer.println();

      writer.println("[game]");
      writer.println((game.getCurrentPlayerIndex() + 1) + " # current player index");

      saveBoard(writer, game.getBoard());
      writer.println();

      List<Player> players = game.getPlayers();
      for (int i = 0; i < players.size(); i++) {
        writer.println("rack-" + (i + 1) + ": " + serializeRack(players.get(i)));
      }

      writer.println();
      for (int i = 0; i < players.size(); i++) {
        writer.println("score-" + (i + 1) + ": " + players.get(i).getScore());
      }

      writer.println();
      writer.println("language " + game.getLanguage());
      writer.println();

      writer.println("[history]");
      saveHistory(writer, game, game.getUndoRedo().getHistory());
    }
  }

  /**
   * Serializes the 15x15 board into the save file using characters and dashes.
   *
   * @param writer The PrintWriter used to write the board state.
   * @param board  The game board containing the squares and tiles.
   */
  private void saveBoard(PrintWriter writer, Board board) {
    int size = board.getSize();
    for (int y = 0; y < size; y++) {
      StringBuilder line = new StringBuilder();
      for (int x = 0; x < size; x++) {
        Square sq = board.getSquare(new Point(x, y));
        if (sq == null || sq.isEmpty()) {
          line.append("-");
        } else {
          line.append(sq.getTile().getCharacter());
        }
      }
      writer.println(line);
    }
  }

  /**
   * Serializes the list of moves performed during the game into the save file.
   *
   * @param writer  The PrintWriter used to write the history.
   * @param game    The current game instance (used for player mapping).
   * @param history The list of moves to record.
   */
  private void saveHistory(PrintWriter writer, Game game, List<Move> history) {
    for (Move move : history) {
      int playerIndex = game.getPlayers().indexOf(move.getPlayer()) + 1;

      if (move.getType() == MoveType.PASS) {
        writer.println(playerIndex + " pass");
      } else if (move.getType() == MoveType.PLAY) {
        String coord = convertPointToCoord(move.getStartPosition());
        String dir = (move.getDirection() == Direction.HORIZONTAL) ? "h" : "v";
        String word = readFullWordFromBoard(game.getBoard(), move);

        writer.println(playerIndex + " " + coord + dir + " " + word);
      }
    }
  }

  /**
   * Converts the tiles on a player's rack into a single string.
   *
   * @param p The player whose rack needs serialization.
   * @return A string containing all characters currently on the player's rack.
   */
  private String serializeRack(Player p) {
    return p.getRack().getTiles().stream()
        .map(t -> String.valueOf(t.getCharacter()))
        .collect(Collectors.joining());
  }

  /**
   * Reads the full word from the board starting at the move's start position and following its
   * direction, until an empty square is found. This ensures tiles already on the board (not played
   * from the rack) are included in the saved history.
   *
   * @param board The current board state.
   * @param move  The PLAY move whose full word must be reconstructed.
   * @return The full word string as it appears on the board.
   */
  private String readFullWordFromBoard(Board board, Move move) {
    StringBuilder word = new StringBuilder();
    int x = move.getStartPosition().getX();
    int y = move.getStartPosition().getY();
    boolean horizontal = move.getDirection() == Direction.HORIZONTAL;

    int boardSize = board.getSize();
    while (x < boardSize && y < boardSize) {
      Square sq = board.getSquare(new Point(x, y));
      if (sq == null || sq.isEmpty()) {
        break;
      }
      word.append(sq.getTile().getCharacter());
      if (horizontal) {
        x++;
      } else {
        y++;
      }
    }

    // Fallback: if board read failed (e.g. move not yet applied), use rack tiles
    return !word.isEmpty() ? word.toString() : move.getTiles().stream()
        .map(t -> String.valueOf(t.getCharacter()))
        .collect(Collectors.joining());
  }

  /**
   * Converts a logical Point (x, y) into Scrabble coordinate notation (e.g., a1, h8).
   *
   * @param p The Point to convert.
   * @return The coordinate string representing the point on the board.
   */
  private String convertPointToCoord(Point p) {
    char row = (char) ('a' + p.getY());
    int col = p.getX() + 1;
    return "" + row + col;
  }
}