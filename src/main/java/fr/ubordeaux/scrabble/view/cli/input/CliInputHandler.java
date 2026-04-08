package fr.ubordeaux.scrabble.view.cli.input;

import fr.ubordeaux.scrabble.controller.builders.ExchangeMoveBuilderController;
import fr.ubordeaux.scrabble.controller.builders.PlayMoveBuilderController;
import fr.ubordeaux.scrabble.i18n.I18n;
import fr.ubordeaux.scrabble.model.core.Move;
import fr.ubordeaux.scrabble.model.interfaces.Player;
import fr.ubordeaux.scrabble.view.cli.renderer.MessageRenderer;
import java.io.IOException;
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
    Move move = PlayMoveBuilderController.build(player, input);
    if (move == null) {
      messageRenderer.error(I18n.translate("cli.play.invalidNotation"));
      return null;
    }
    return move;
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
    Move move = ExchangeMoveBuilderController.build(lettersInput.trim().toUpperCase(), player);
    if (move == null) {
      messageRenderer.error(I18n.translate("cli.exchange.invalidFormat", "invalid letters"));
      return null;
    }
    return move;
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