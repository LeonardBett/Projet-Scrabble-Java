package fr.ubordeaux.scrabble.view.cli;

import fr.ubordeaux.scrabble.model.dictionary.core.Move;
import fr.ubordeaux.scrabble.model.dictionary.core.Tile;
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
    messageRenderer.sectionTitle("CHOSE AN ACTION");
    System.out.println("1. Play a word");
    System.out.println("2. Exchange letters");
    System.out.println("3. Skip turn");
    System.out.println("4. Undo former move");
    System.out.println("5. Redo the canceled move");
    System.out.println("6. Quit");
    System.out.println("7. Ask for an hint");
    System.out.print("\nYour choice (1-7) : ");
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
      System.out.print("\nStart position (format: h 8 ou 8 8) : ");
      String[] posInput = scanner.nextLine().trim().split("\\s+");
      if (posInput.length < 2) {
        throw new IllegalArgumentException("Input two values: line column.");
      }

      int x;
      int y;

      if (posInput[0].matches("[a-oA-O]")) {
        String[] rows = { "a", "b", "c", "d", "e", "f", "g",
            "h", "i", "j", "k", "l", "m", "n", "o" };
        String row = posInput[0].toLowerCase();
        int rowIndex = -1;
        for (int i = 0; i < rows.length; i++) {
          if (rows[i].equals(row)) {
            rowIndex = i;
            break;
          }
        }
        if (rowIndex == -1) {
          throw new IllegalArgumentException("Ligne invalide: " + posInput[0]);
        }
        y = rowIndex;
        x = Integer.parseInt(posInput[1]) - 1;
      } else {
        x = Integer.parseInt(posInput[0]) - 1;
        y = Integer.parseInt(posInput[1]) - 1;
      }

      final Point startPoint = new Point(x, y);

      System.out.print("Direction (H for horizontal, V for vertical) : ");
      String dirInput = scanner.nextLine().trim().toUpperCase();
      Direction direction = dirInput.equals("H") ? Direction.HORIZONTAL : Direction.VERTICAL;

      System.out.print("Letters to play (ex: HELLO) : ");
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
          messageRenderer.error("The letter '" + letter + "' is not in your rack !");
          return null;
        }
      }

      return Move.createPlay(player, tiles, startPoint, direction);

    } catch (IllegalArgumentException | IllegalStateException e) {
      messageRenderer.error("Invalid format ! " + e.getMessage());
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
      System.out.print("\nLetters to exchange (ex: ABC) : ");
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
          messageRenderer.error("La lettre '" + letter + "' n'est pas dans votre chevalet !");
          return null;
        }
      }

      return Move.createExchange(player, tiles);

    } catch (Exception e) {
      messageRenderer.error("Format invalide ! " + e.getMessage());
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
      System.out.print("\nNumbers of players (2-4) : ");
      try {
        int num = Integer.parseInt(scanner.nextLine().trim());
        if (num >= 2 && num <= 4) {
          return num;
        }
        messageRenderer.warning("The numbers of player must be between 2 and 4.");
      } catch (NumberFormatException e) {
        messageRenderer.error("Please enter a valid number.");
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
    System.out.print("Player's name " + playerNumber + " : ");
    return scanner.nextLine().trim();
  }

  /**
   * Asks for a confirmation (yes/no).
   *
   * @param question the question to display
   * @return true if the user confirmed, false otherwise
   */
  public boolean askConfirmation(String question) {
    System.out.print(question + " (o/n) : ");
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
