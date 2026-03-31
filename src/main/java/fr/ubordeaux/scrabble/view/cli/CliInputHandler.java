package fr.ubordeaux.scrabble.view.cli;

import fr.ubordeaux.scrabble.i18n.I18n;
import fr.ubordeaux.scrabble.model.core.Move;
import fr.ubordeaux.scrabble.model.core.Tile;
import fr.ubordeaux.scrabble.model.enums.Direction;
import fr.ubordeaux.scrabble.model.interfaces.Player;
import fr.ubordeaux.scrabble.model.utils.Point;
import fr.ubordeaux.scrabble.view.cli.renderer.MessageRenderer;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

/**
 * Handles user input in the CLI.
 */
public class CliInputHandler {

  private final Scanner scanner;
  private final MessageRenderer messageRenderer;

  /**
   * Creates a new CliInputHandler with a stdin scanner.
   */
  public CliInputHandler() {
    this.scanner = new Scanner(System.in);
    this.messageRenderer = new MessageRenderer();
  }

  /**
   * Asks the player which action they want to perform.
   *
   * @return the action string chosen by the player
   */
  public String askAction() {
    messageRenderer.sectionTitle(I18n.translate("cli.action.title"));
    System.out.println(I18n.translate("cli.action.play"));
    System.out.println(I18n.translate("cli.action.exchange"));
    System.out.println(I18n.translate("cli.action.pass"));
    System.out.println(I18n.translate("cli.action.undo"));
    System.out.println(I18n.translate("cli.action.redo"));
    System.out.println(I18n.translate("cli.action.quit"));
    System.out.println(I18n.translate("cli.action.hint"));
    System.out.print("\n" + I18n.translate("cli.action.choicePrompt"));
    return scanner.nextLine().trim();
  }

  /**
   * Asks the player to build a "play word" move.
   *
   * @param player the player who is playing
   * @return the constructed Move, or null if input is invalid
   */
  public Move askPlayMove(Player player) {
    try {
      System.out.print("\n" + I18n.translate("cli.play.notationPrompt"));
      String input = scanner.nextLine().trim();
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
    try {
      System.out.print("\n" + I18n.translate("cli.exchange.prompt"));
      String lettersInput = scanner.nextLine().trim().toUpperCase();

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
      System.out.print("\n" + I18n.translate("cli.players.countPrompt"));
      try {
        int num = Integer.parseInt(scanner.nextLine().trim());
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
    System.out.print(I18n.translate("cli.players.namePrompt", playerNumber));
    return scanner.nextLine().trim();
  }

  /**
   * Asks for a confirmation (yes/no).
   *
   * @param question the question to display
   * @return true if the user confirmed, false otherwise
   */
  public boolean askConfirmation(String question) {
    System.out.print(question + " " + I18n.translate("cli.confirm.suffix"));
    String response = scanner.nextLine().trim().toLowerCase();
    return response.equals("o") || response.equals("oui") || response.equals("y")
        || response.equals("yes");
  }

  /**
   * Ferme le scanner.
   */
  public void close() {
    scanner.close();
  }
}
