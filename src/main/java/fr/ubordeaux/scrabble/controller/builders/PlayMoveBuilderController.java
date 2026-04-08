package fr.ubordeaux.scrabble.controller.builders;

import fr.ubordeaux.scrabble.model.core.Move;
import fr.ubordeaux.scrabble.model.core.Tile;
import fr.ubordeaux.scrabble.model.enums.Direction;
import fr.ubordeaux.scrabble.model.interfaces.Player;
import fr.ubordeaux.scrabble.model.utils.Point;
import java.util.ArrayList;
import java.util.List;

/**
 * Builds a play move from CLI notation.
 */
public final class PlayMoveBuilderController {

  private PlayMoveBuilderController() {
  }

  /**
   * Builds a play move from CLI input.
   *
   * @param player active player
   * @param input raw CLI notation
   * @return play move, or null when notation/rack content is invalid
   */
  public static Move build(Player player, String input) {
    if (input == null || input.isEmpty()) {
      return null;
    }

    String[] parts = input.split("\\s+");
    if (parts.length < 2) {
      return null;
    }

    int x;
    int y;
    Direction direction;
    int lettersStartIndex;

    try {
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
          return null;
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
          return null;
        }
        lettersStartIndex = 1;
      }
    } catch (RuntimeException e) {
      return null;
    }

    if (x < 0 || x >= 15 || y < 0 || y >= 15) {
      return null;
    }

    String lettersInput = String.join("", java.util.Arrays.copyOfRange(parts, lettersStartIndex,
        parts.length));
    if (lettersInput.trim().isEmpty()) {
      return null;
    }

    Point startPoint = new Point(x, y);
    List<Tile> tiles = new ArrayList<>();
    List<Tile> availableRack = new ArrayList<>(player.getRack().getTiles());

    for (char inputChar : lettersInput.toCharArray()) {
      boolean isJokerRequested = Character.isLowerCase(inputChar);
      char targetChar = Character.toUpperCase(inputChar);
      boolean found = false;

      for (int i = 0; i < availableRack.size(); i++) {
        Tile tile = availableRack.get(i);

        if (isJokerRequested) {
          if (tile.isJoker() || tile.getCharacter() == ' ') {
            tiles.add(new Tile(targetChar, true));
            availableRack.remove(i);
            found = true;
            break;
          }
        } else if (tile.getCharacter() == targetChar && !tile.isJoker()
            && tile.getCharacter() != ' ') {
          tiles.add(tile);
          availableRack.remove(i);
          found = true;
          break;
        }
      }

      if (!found) {
        return null;
      }
    }

    return Move.createPlay(player, tiles, startPoint, direction);
  }

  private static int rowToIndex(String row) {
    char lower = Character.toLowerCase(row.charAt(0));
    if (lower < 'a' || lower > 'o') {
      throw new IllegalArgumentException();
    }
    return lower - 'a';
  }

  private static Direction parseDirection(String dirValue) {
    String normalized = dirValue.toUpperCase();
    if ("H".equals(normalized) || "G".equals(normalized)) {
      return Direction.HORIZONTAL;
    }
    if ("V".equals(normalized)) {
      return Direction.VERTICAL;
    }
    throw new IllegalArgumentException();
  }
}