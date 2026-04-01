package fr.ubordeaux.scrabble.view.cli;

import fr.ubordeaux.scrabble.i18n.I18n;
import fr.ubordeaux.scrabble.model.core.Move;
import fr.ubordeaux.scrabble.model.core.Tile;
import fr.ubordeaux.scrabble.model.enums.Direction;
import fr.ubordeaux.scrabble.model.interfaces.Player;
import fr.ubordeaux.scrabble.model.utils.Point;
import fr.ubordeaux.scrabble.view.cli.renderer.MessageRenderer;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.jline.reader.EndOfFileException;
import org.jline.reader.LineReader;
import org.jline.reader.LineReaderBuilder;
import org.jline.reader.UserInterruptException;
import org.jline.reader.impl.DefaultParser;
import org.jline.reader.impl.completer.StringsCompleter;
import org.jline.terminal.Terminal;
import org.jline.terminal.TerminalBuilder;

/**
 * Handles user input in the CLI.
 */
public class CliInputHandler {

  private LineReader lineReader;
  private final MessageRenderer messageRenderer;

  /**
   * Creates a new CliInputHandler with a JLine3 terminal.
   */
  public CliInputHandler() {
    this.messageRenderer = new MessageRenderer();
    try {
      TerminalBuilder builder = TerminalBuilder.builder();

      StringsCompleter completer = new StringsCompleter(
          "new", "help", "quit", "load", "save", "pause", "hint",
          "undo", "redo", "show board", "show history", "show time",
          "show configuration", "set", "exchange", "pass"
      );

      if (System.in instanceof java.io.ByteArrayInputStream) {
        builder.streams(System.in, System.out);
      } else {
        builder.system(true);
      }

      Terminal terminal = builder.build();

      DefaultParser parser = new DefaultParser();
      parser.setEscapeChars(new char[0]);

      this.lineReader = LineReaderBuilder.builder()
          .terminal(terminal)
          .parser(parser)
          .completer(completer)
          .build();
    } catch (IOException e) {
      messageRenderer.error("Failed to initialize JLine terminal: " + e.getMessage());
      throw new RuntimeException("CLI initialization failed", e);
    }
  }

  /**
   * Helper method to safely read a line from JLine, handling interruptions.
   *
   * @param prompt the prompt to display to the user
   * @return the trimmed input string
   */
  private String safeReadLine(String prompt) {
    try {
      String line = lineReader.readLine(prompt);
      return line == null ? "" : line.trim();
    } catch (UserInterruptException | EndOfFileException e) {
      return "quit";
    }
  }

  /**
   * Asks the player which action they want to perform.
   *
   * @return the action string chosen by the player
   */
  public String askAction() {
    return safeReadLine("\n>> ");
  }

  /**
   * Asks the player to build a "play word" move.
   *
   * @param player the player who is playing
   * @return the constructed Move, or null if input is invalid
   */
  public Move askPlayMove(Player player) {
    String input = safeReadLine("\n" + I18n.translate("cli.play.notationPrompt"));
    return parsePlayMoveNotation(player, input);
  }

  /**
   * Parses a move notation entered directly in the shell (for example: g5v COUNT).
   *
   * @param player player who is playing
   * @param input raw notation to parse
   * @return the constructed Move, or null if input is invalid
   */
  public Move parsePlayMoveNotation(Player player, String input) {
    try {
      if (input.isEmpty()) {
        throw new IllegalArgumentException(I18n.translate("cli.play.emptyInput"));
      }

      String[] parts = input.split("\\s+");
      if (parts.length < 2) {
        throw new IllegalArgumentException(I18n.translate("cli.play.missingLetters"));
      }

      int x = -1;
      int y = -1;
      Direction direction;
      int lettersStartIndex;

      if (parts.length >= 4 && parts[0].matches("\\d+") && parts[1].matches("\\d+")) {
        x = Integer.parseInt(parts[0]) - 1;
        y = Integer.parseInt(parts[1]) - 1;
        direction = parseDirection(parts[2]);
        lettersStartIndex = 3;
      } else if (parts.length >= 4 && parts[0].matches("[a-oA-O]") && parts[1].matches("\\d+")) {
        y = rowToIndex(parts[0]);
        x = Integer.parseInt(parts[1]) - 1;
        direction = parseDirection(parts[2]);
        lettersStartIndex = 3;
      } else {
        String posDir = parts[0];
        if (posDir.length() < 3) {
          throw new IllegalArgumentException(I18n.translate("cli.play.invalidNotation"));
        }

        direction = parseDirection(String.valueOf(posDir.charAt(posDir.length() - 1)));
        String posStr = posDir.substring(0, posDir.length() - 1);

        if (posStr.matches("[a-oA-O]\\d+")) {
          y = rowToIndex(posStr.substring(0, 1));
          x = Integer.parseInt(posStr.substring(1)) - 1;
        } else if (posStr.matches("\\d+[a-oA-O]")) {
          y = rowToIndex(posStr.substring(posStr.length() - 1));
          x = Integer.parseInt(posStr.substring(0, posStr.length() - 1)) - 1;
        } else {
          throw new IllegalArgumentException(I18n.translate("cli.play.invalidPositionFormat"));
        }
        lettersStartIndex = 1;
      }

      if (x < 0 || x >= 15 || y < 0 || y >= 15) {
        throw new IllegalArgumentException(I18n.translate("cli.play.invalidCoordinates"));
      }

      String lettersInput = String.join(" ",
          java.util.Arrays.copyOfRange(parts, lettersStartIndex, parts.length)).toUpperCase();
      if (lettersInput.trim().isEmpty()) {
        throw new IllegalArgumentException(I18n.translate("cli.play.missingLetters"));
      }

      final Point startPoint = new Point(x, y);

      List<Tile> tiles = new ArrayList<>();
      List<Tile> rack = player.getRack().getTiles();

      for (char letter : lettersInput.toCharArray()) {
        boolean found = false;
        for (Tile tile : rack) {
          if (tile.getCharacter() == letter) {
            boolean alreadyUsed = false;
            for (Tile t : tiles) {
              if (t == tile) {
                alreadyUsed = true;
                break;
              }
            }
            if (!alreadyUsed) {
              tiles.add(tile);
              found = true;
              break;
            }
          }
        }
        if (!found) {
          messageRenderer.error(I18n.translate("cli.play.letterNotInRack", letter));
          return null;
        }
      }

      return Move.createPlay(player, tiles, startPoint, direction);

    } catch (Exception e) {
      messageRenderer.error(I18n.translate("cli.play.invalidFormat", e.getMessage()));
      return null;
    }
  }

  private int rowToIndex(String row) {
    char lower = Character.toLowerCase(row.charAt(0));
    if (lower < 'a' || lower > 'o') {
      throw new IllegalArgumentException(I18n.translate("cli.play.invalidRow", row));
    }
    return lower - 'a';
  }

  private Direction parseDirection(String dirValue) {
    String normalized = dirValue.toUpperCase();
    if ("H".equals(normalized) || "G".equals(normalized)) {
      return Direction.HORIZONTAL;
    }
    if ("V".equals(normalized)) {
      return Direction.VERTICAL;
    }
    throw new IllegalArgumentException(I18n.translate("cli.play.invalidNotation"));
  }

  /**
   * Asks the player which letters to exchange.
   *
   * @param player the player who is exchanging tiles
   * @return the constructed Move, or null if input is invalid
   */
  public Move askExchangeMove(Player player) {
    String input = safeReadLine("\n" + I18n.translate("cli.exchange.prompt"));
    return parseExchangeLetters(player, input);
  }

  /**
   * Parses an exchange command payload (letters only).
   *
   * @param player player who is exchanging tiles
   * @param lettersInput letters to exchange
   * @return the constructed Move, or null if input is invalid
   */
  public Move parseExchangeLetters(Player player, String lettersInput) {
    try {
      lettersInput = lettersInput.trim().toUpperCase();

      List<Tile> tiles = new ArrayList<>();
      List<Tile> rack = player.getRack().getTiles();

      for (char letter : lettersInput.toCharArray()) {
        boolean found = false;
        for (Tile tile : rack) {
          if (tile.getCharacter() == letter) {
            boolean alreadyUsed = false;
            for (Tile t : tiles) {
              if (t == tile) {
                alreadyUsed = true;
                break;
              }
            }
            if (!alreadyUsed) {
              tiles.add(tile);
              found = true;
              break;
            }
          }
        }
        if (!found) {
          messageRenderer.error(I18n.translate("cli.exchange.letterNotInRack", letter));
          return null;
        }
      }

      return Move.createExchange(player, tiles);

    } catch (Exception e) {
      messageRenderer.error(I18n.translate("cli.exchange.invalidFormat", e.getMessage()));
      return null;
    }
  }

  /**
   * Asks for the number of players.
   *
   * @return the number of players (between 2 and 4)
   */
  public int askNumberOfPlayers() {
    while (true) {
      String input = safeReadLine("\n" + I18n.translate("cli.players.countPrompt"));
      if (input.equals("quit")) {
        System.exit(0);
      }
      try {
        int num = Integer.parseInt(input);
        if (num >= 2 && num <= 4) {
          return num;
        }
        messageRenderer.warning(I18n.translate("cli.players.rangeWarning"));
      } catch (NumberFormatException e) {
        messageRenderer.error(I18n.translate("cli.players.invalidNumber"));
      }
    }
  }

  /**
   * Asks for a player's name.
   *
   * @param playerNumber the player number (used in the prompt)
   * @return the name entered by the user
   */
  public String askPlayerName(int playerNumber) {
    return safeReadLine(I18n.translate("cli.players.namePrompt", playerNumber));
  }

  /**
   * Asks for a confirmation (yes/no).
   *
   * @param question the question to display
   * @return true if the user confirmed, false otherwise
   */
  public boolean askConfirmation(String question) {
    String prompt = question + " " + I18n.translate("cli.confirm.suffix");
    String response = safeReadLine(prompt).toLowerCase();
    return response.equals("o") || response.equals("oui") || response.equals("y")
        || response.equals("yes");
  }

  /**
   * Asks for a free text input.
   *
   * @param prompt prompt to display
   * @return entered text (trimmed)
   */
  public String askText(String prompt) {
    return safeReadLine(prompt + " ");
  }

  /**
   * Ferme le scanner.
   */
  public void close() {
    // No action needed for JLine, but kept for interface compatibility
  }
}