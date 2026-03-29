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
      System.out.print("\n" + I18n.translate("cli.play.startPrompt"));
      String[] posInput = scanner.nextLine().trim().split("\\s+");
      if (posInput.length < 2) {
        throw new IllegalArgumentException(I18n.translate("cli.play.needTwoValues"));
      }

      int x;
      int y;

      if (posInput[0].matches("[a-oA-O]")) {
        String[] rows = {"a", "b", "c", "d", "e", "f", "g", "h", "i", "j", "k", "l", "m", "n", "o"};
        String row = posInput[0].toLowerCase();
        int rowIndex = -1;
        for (int i = 0; i < rows.length; i++) {
          if (rows[i].equals(row)) {
            rowIndex = i;
            break;
          }
        }
        if (rowIndex == -1) {
          throw new IllegalArgumentException(I18n.translate("cli.play.invalidRow", posInput[0]));
        }
        y = rowIndex;
        x = Integer.parseInt(posInput[1]) - 1;
      } else {
        x = Integer.parseInt(posInput[0]) - 1;
        y = Integer.parseInt(posInput[1]) - 1;
      }

      final Point startPoint = new Point(x, y);

      System.out.print(I18n.translate("cli.play.directionPrompt"));
      String dirInput = scanner.nextLine().trim().toUpperCase();
      Direction direction = dirInput.equals("H") ? Direction.HORIZONTAL : Direction.VERTICAL;

      System.out.print(I18n.translate("cli.play.lettersPrompt"));
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
