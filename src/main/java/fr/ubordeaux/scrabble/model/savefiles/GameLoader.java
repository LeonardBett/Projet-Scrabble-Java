package fr.ubordeaux.scrabble.model.savefiles;

import fr.ubordeaux.scrabble.model.core.Game;
import fr.ubordeaux.scrabble.model.core.HumanPlayer;
import fr.ubordeaux.scrabble.model.core.Move;
import fr.ubordeaux.scrabble.model.core.Tile;
import fr.ubordeaux.scrabble.model.enums.Direction;
import fr.ubordeaux.scrabble.model.enums.PlayerColor;
import fr.ubordeaux.scrabble.model.interfaces.Player;
import fr.ubordeaux.scrabble.model.utils.Point;
import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;

/**
 * Robust loader for Scrabble save files.
 * Handles single-line (#) and multi-line ({}) comments (Requirement F22).
 */
public class GameLoader {
  private boolean isInBlockComment;

  /**
   * Constructs a new GameLoader instance.
   */
  public GameLoader() {
    this.isInBlockComment = false;
  }

  /**
   * Loads a game state from an ASCII text file.
   *
   * @param filePath Path to the .scrabble save file.
   * @return A restored Game instance.
   * @throws Exception if format is invalid, specifying the line number (F24).
   */
  public Game loadGame(String filePath) throws Exception {
    Game game = new Game();
    this.isInBlockComment = false;
    int lineCount = 0;

    try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
      String line;
      String section = "";
      int boardRow = 0;

      while ((line = reader.readLine()) != null) {
        lineCount++;
        line = processLineComments(line).trim();

        if (line.isEmpty() || isInBlockComment) {
          continue;
        }

        try {
          if (line.startsWith("[")) {
            section = line.toLowerCase();
            if (!section.equals("[settings]") && !section.equals("[game]")
                && !section.equals("[history]")) {
              throw new IllegalArgumentException("Unknown section found: " + section);
            }
            continue;
          }

          switch (section) {
            case "[settings]":
              parseSettings(game, line);
              break;
            case "[game]":
              boardRow = parseGameState(game, line, boardRow);
              break;
            case "[history]":
              parseHistory(game, line);
              break;
            default:
              throw new IllegalStateException("Data found before any valid section header.");
          }
        } catch (Exception e) {
          throw new Exception("Format error at line " + lineCount + ": " + e.getMessage());
        }
      }
    }
    return game;
  }

  /**
   * Cleans an input line by removing single-line (#) and multi-line ({}) comments.
   *
   * @param line The raw line from the save file.
   * @return The cleaned line without comments.
   */
  private String processLineComments(String line) {
    StringBuilder cleaned = new StringBuilder();
    char[] chars = line.toCharArray();
    for (char c : chars) {
      if (isInBlockComment) {
        if (c == '}') {
          isInBlockComment = false;
        }
        continue;
      }
      if (c == '{') {
        isInBlockComment = true;
        continue;
      }
      if (c == '#') {
        break;
      }
      cleaned.append(c);
    }
    return cleaned.toString();
  }

  /**
   * Parses global game settings such as Blitz mode from the configuration section.
   *
   * @param game The game instance to update.
   * @param line The current line containing setting data.
   */
  private void parseSettings(Game game, String line) {
    String[] parts = line.split("\\s+");
    if (parts.length < 2) {
      return;
    }
    if (parts[0].equals("blitz") && parts[1].equals("true")) {
      game.enableBlitzMode();
    }
  }

  /**
   * Restores the board state, player scores, and racks from the game section.
   *
   * @param game     The game instance to update.
   * @param line     The current line containing game state data.
   * @param boardRow The current board row index being processed.
   * @return The updated board row index.
   */
  private int parseGameState(Game game, String line, int boardRow) {
    if (line.length() == 1 && Character.isDigit(line.charAt(0))) {
      int currentPlayerIdx = Character.getNumericValue(line.charAt(0)) - 1;
      game.setCurrentPlayerIndex(currentPlayerIdx); // <-- C'est cette ligne qui manque !
      return boardRow;
    }

    // Ajout de la condition "boardRow < 15" pour éviter de déborder du plateau
    if (boardRow < 15 && line.length() == 15 && (line.contains("-") || line.matches(".*[A-Z].*"))) {
      for (int x = 0; x < 15; x++) {
        char c = line.charAt(x);
        if (c != '-') {
          game.getBoard().getSquare(new Point(x, boardRow)).setTile(new Tile(c));
          game.setFirstMoveDone(true);
        }
      }
      return boardRow + 1;
    }

    String trim = line.substring(line.indexOf(":") + 1).trim();
    if (line.startsWith("score-")) {
      int playerIdx = Integer.parseInt(line.substring(6, line.indexOf(":"))) - 1;
      int scoreValue = Integer.parseInt(trim);
      ensurePlayerExists(game, playerIdx);
      game.getPlayers().get(playerIdx).addScore(scoreValue);
    }

    if (line.startsWith("rack-")) {
      int playerIdx = Integer.parseInt(line.substring(5, line.indexOf(":"))) - 1;
      ensurePlayerExists(game, playerIdx);
      Player p = game.getPlayers().get(playerIdx);
      for (char c : trim.toCharArray()) {
        p.getRack().addTile(new Tile(c));
      }
    }
    return boardRow;
  }

  /**
   * Reconstructs the move history from the history section to support undo/redo.
   *
   * @param game The game instance to update.
   * @param line The current line containing history data.
   */
  private void parseHistory(Game game, String line) {
    String[] parts = line.split("\\s+");
    if (parts.length < 2) {
      return;
    }
    int playerIdx = Integer.parseInt(parts[0]) - 1;
    ensurePlayerExists(game, playerIdx);
    Player player = game.getPlayers().get(playerIdx);

    if (parts.length == 2 && parts[1].equalsIgnoreCase("pass")) {
      game.getUndoRedo().addMove(Move.createPass(player));
    } else if (parts.length == 3) {
      String moveData = parts[1];
      String wordStr = parts[2];
      char rowChar = moveData.charAt(0);
      int y = rowChar - 'a';
      int x = Integer.parseInt(moveData.substring(1, moveData.length() - 1)) - 1;
      Direction dir = moveData.endsWith("h") ? Direction.HORIZONTAL : Direction.VERTICAL;

      List<Tile> tiles = new ArrayList<>();
      for (char c : wordStr.toCharArray()) {
        tiles.add(new Tile(c));
      }
      game.getUndoRedo().addMove(Move.createPlay(player, tiles, new Point(x, y), dir));
    }
  }

  /**
   * Ensures that a player exists in the game instance, creating a new player if necessary.
   *
   * @param game  The game instance to check.
   * @param index The index of the player to verify.
   */
  private void ensurePlayerExists(Game game, int index) {
    while (game.getPlayers().size() <= index) {
      int nextIdx = game.getPlayers().size();
      PlayerColor color = PlayerColor.values()[nextIdx % PlayerColor.values().length];
      game.addPlayer(new HumanPlayer("Player " + (nextIdx + 1), color));
    }
  }
}