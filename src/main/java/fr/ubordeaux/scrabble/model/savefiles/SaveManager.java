package fr.ubordeaux.scrabble.model.savefiles;

import fr.ubordeaux.scrabble.model.dictionary.core.Board;
import fr.ubordeaux.scrabble.model.dictionary.core.Game;
import fr.ubordeaux.scrabble.model.dictionary.core.Move;
import fr.ubordeaux.scrabble.model.dictionary.core.Square;
import fr.ubordeaux.scrabble.model.enums.Direction;
import fr.ubordeaux.scrabble.model.enums.MoveType;
import fr.ubordeaux.scrabble.model.interfaces.Player;
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
   * Saves the current game state, including settings, board, and history, into a
   * text file.
   *
   * @param game     The current game instance to be saved.
   * @param filePath The destination path of the save file.
   * @throws IOException If an error occurs during file writing.
   */
  public void saveGame(Game game, String filePath) throws IOException {
    try (PrintWriter writer = new PrintWriter(new FileWriter(filePath))) {

      writer.println("[settings] # Global game parameters");
      writer.println("blitz " + game.isBlitzModeEnabled());
      writer.println("super-scrabble " + "TODO");
      writer.println("players-count " + game.getPlayers().size());
      writer.println("turn-limit " + "TODO");
      writer.println("debug " + "TODO");
      writer.println("verbose " + "TODO");
      writer.println("ai-mode " + "TODO");
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
      writer.println("language en");
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
    for (int y = 0; y < 15; y++) {
      StringBuilder line = new StringBuilder();
      for (int x = 0; x < 15; x++) {
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
        String word = move.getTiles().stream()
            .map(t -> String.valueOf(t.getCharacter()))
            .collect(Collectors.joining());

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
   * Converts a logical Point (x, y) into Scrabble coordinate notation (e.g., a1,
   * h8).
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