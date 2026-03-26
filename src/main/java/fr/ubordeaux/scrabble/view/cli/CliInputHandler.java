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
<<<<<<< HEAD
<<<<<<< HEAD
    messageRenderer.sectionTitle("CHOSE AN ACTION");
    System.out.println("1. Play a word");
    System.out.println("2. Exchange letters");
    System.out.println("3. Skip turn");
    System.out.println("4. Undo former move");
    System.out.println("5. Redo the canceled move");
    System.out.println("6. Quit");
    System.out.println("7. Ask for an hint");
    System.out.print("\nYour choice (1-7) : ");
=======
=======
>>>>>>> 80eb4dd275121a2ebeee7238584257f4cce5f53f
    messageRenderer.sectionTitle(I18n.tr("cli.input.actionTitle"));
    System.out.println(I18n.tr("cli.input.action1"));
    System.out.println(I18n.tr("cli.input.action2"));
    System.out.println(I18n.tr("cli.input.action3"));
    System.out.println(I18n.tr("cli.input.action4"));
    System.out.println(I18n.tr("cli.input.action5"));
    System.out.println(I18n.tr("cli.input.action6"));
    System.out.print(I18n.tr("cli.input.choicePrompt"));
<<<<<<< HEAD
>>>>>>> 80eb4dd (Add internationalization support for GUI and CLI components)
=======
>>>>>>> 80eb4dd275121a2ebeee7238584257f4cce5f53f
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
<<<<<<< HEAD
<<<<<<< HEAD
      System.out.print("\nStart position (format: h 8 ou 8 8) : ");
      String[] posInput = scanner.nextLine().trim().split("\\s+");
      if (posInput.length < 2) {
        throw new IllegalArgumentException("Input two values: line column.");
=======
      System.out.print(I18n.tr("cli.input.startPosition"));
      String[] posInput = scanner.nextLine().trim().split("\\s+");
      if (posInput.length < 2) {
        throw new IllegalArgumentException(I18n.tr("cli.input.twoValues"));
>>>>>>> 80eb4dd (Add internationalization support for GUI and CLI components)
=======
      System.out.print(I18n.tr("cli.input.startPosition"));
      String[] posInput = scanner.nextLine().trim().split("\\s+");
      if (posInput.length < 2) {
        throw new IllegalArgumentException(I18n.tr("cli.input.twoValues"));
>>>>>>> 80eb4dd275121a2ebeee7238584257f4cce5f53f
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
          throw new IllegalArgumentException(I18n.tr("cli.input.invalidRow", posInput[0]));
        }
        y = rowIndex;
        x = Integer.parseInt(posInput[1]) - 1;
      } else {
        x = Integer.parseInt(posInput[0]) - 1;
        y = Integer.parseInt(posInput[1]) - 1;
      }

      final Point startPoint = new Point(x, y);

<<<<<<< HEAD
<<<<<<< HEAD
      System.out.print("Direction (H for horizontal, V for vertical) : ");
      String dirInput = scanner.nextLine().trim().toUpperCase();
      Direction direction = dirInput.equals("H") ? Direction.HORIZONTAL : Direction.VERTICAL;

      System.out.print("Letters to play (ex: HELLO) : ");
=======
      System.out.print(I18n.tr("cli.input.direction"));
      String dirInput = scanner.nextLine().trim().toUpperCase();
      Direction direction = dirInput.equals("H") ? Direction.HORIZONTAL : Direction.VERTICAL;

      System.out.print(I18n.tr("cli.input.lettersToPlay"));
>>>>>>> 80eb4dd (Add internationalization support for GUI and CLI components)
=======
      System.out.print(I18n.tr("cli.input.direction"));
      String dirInput = scanner.nextLine().trim().toUpperCase();
      Direction direction = dirInput.equals("H") ? Direction.HORIZONTAL : Direction.VERTICAL;

      System.out.print(I18n.tr("cli.input.lettersToPlay"));
>>>>>>> 80eb4dd275121a2ebeee7238584257f4cce5f53f
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
<<<<<<< HEAD
<<<<<<< HEAD
          messageRenderer.error("The letter '" + letter + "' is not in your rack !");
=======
          messageRenderer.error(I18n.tr("cli.input.tileNotInRack", letter));
>>>>>>> 80eb4dd (Add internationalization support for GUI and CLI components)
=======
          messageRenderer.error(I18n.tr("cli.input.tileNotInRack", letter));
>>>>>>> 80eb4dd275121a2ebeee7238584257f4cce5f53f
          return null;
        }
      }

      return Move.createPlay(player, tiles, startPoint, direction);

    } catch (Exception e) {
<<<<<<< HEAD
<<<<<<< HEAD
      messageRenderer.error("Invalid format ! " + e.getMessage());
=======
      messageRenderer.error(I18n.tr("cli.input.invalidFormat", e.getMessage()));
>>>>>>> 80eb4dd (Add internationalization support for GUI and CLI components)
=======
      messageRenderer.error(I18n.tr("cli.input.invalidFormat", e.getMessage()));
>>>>>>> 80eb4dd275121a2ebeee7238584257f4cce5f53f
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
<<<<<<< HEAD
<<<<<<< HEAD
      System.out.print("\nLetters to exchange (ex: ABC) : ");
=======
      System.out.print(I18n.tr("cli.input.lettersToExchange"));
>>>>>>> 80eb4dd (Add internationalization support for GUI and CLI components)
=======
      System.out.print(I18n.tr("cli.input.lettersToExchange"));
>>>>>>> 80eb4dd275121a2ebeee7238584257f4cce5f53f
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
          messageRenderer.error(I18n.tr("cli.input.tileNotInRack", letter));
          return null;
        }
      }

      return Move.createExchange(player, tiles);

    } catch (Exception e) {
      messageRenderer.error(I18n.tr("cli.input.invalidFormat", e.getMessage()));
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
<<<<<<< HEAD
<<<<<<< HEAD
      System.out.print("\nNumbers of players (2-4) : ");
=======
      System.out.print(I18n.tr("cli.input.playersPrompt"));
>>>>>>> 80eb4dd (Add internationalization support for GUI and CLI components)
=======
      System.out.print(I18n.tr("cli.input.playersPrompt"));
>>>>>>> 80eb4dd275121a2ebeee7238584257f4cce5f53f
      try {
        int num = Integer.parseInt(scanner.nextLine().trim());
        if (num >= 2 && num <= 4) {
          return num;
        }
<<<<<<< HEAD
<<<<<<< HEAD
        messageRenderer.warning("The numbers of player must be between 2 and 4.");
      } catch (NumberFormatException e) {
        messageRenderer.error("Please enter a valid number.");
=======
        messageRenderer.warning(I18n.tr("cli.input.playersRange"));
      } catch (NumberFormatException e) {
        messageRenderer.error(I18n.tr("cli.input.playersValidNumber"));
>>>>>>> 80eb4dd (Add internationalization support for GUI and CLI components)
=======
        messageRenderer.warning(I18n.tr("cli.input.playersRange"));
      } catch (NumberFormatException e) {
        messageRenderer.error(I18n.tr("cli.input.playersValidNumber"));
>>>>>>> 80eb4dd275121a2ebeee7238584257f4cce5f53f
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
<<<<<<< HEAD
<<<<<<< HEAD
    System.out.print("Player's name " + playerNumber + " : ");
=======
    System.out.print(I18n.tr("cli.input.playerNamePrompt", playerNumber));
>>>>>>> 80eb4dd (Add internationalization support for GUI and CLI components)
=======
    System.out.print(I18n.tr("cli.input.playerNamePrompt", playerNumber));
>>>>>>> 80eb4dd275121a2ebeee7238584257f4cce5f53f
    return scanner.nextLine().trim();
  }

  /**
   * Asks for a confirmation (yes/no).
   *
   * @param question the question to display
   * @return true if the user confirmed, false otherwise
   */
  public boolean askConfirmation(String question) {
    System.out.print(question + " " + I18n.tr("cli.input.confirmSuffix"));
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
