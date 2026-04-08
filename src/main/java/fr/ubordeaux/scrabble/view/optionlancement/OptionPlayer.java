package fr.ubordeaux.scrabble.view.optionlancement;

import fr.ubordeaux.scrabble.i18n.I18n;

/**
 * Parses and validates the player-count argument (-p N).
 */
public class OptionPlayer {

  @FunctionalInterface
  interface ExitHandler {
    void exit(int status);
  }

  /**
   * Minimum number of players allowed.
   */
  public static final int MIN = 2;

  /**
   * Maximum number of players allowed.
   */
  public static final int MAX = 4;

  /**
   * Default number of players.
   */
  public static final int DEFAULT = 2;

  private static ExitHandler exitHandler = System::exit;

  private OptionPlayer() {
  }

  static void setExitHandlerForTests(ExitHandler handler) {
    exitHandler = handler;
  }

  static void resetExitHandlerForTests() {
    exitHandler = System::exit;
  }

  /**
   * Parses and validates the value of argument {@code -p} (player count).
   * Exits the program with an error message when the value is invalid.
   *
   * @param value the input string to parse
   * @return validated player count
   */
  public static int parsePlayers(String value) {
    try {
      int n = Integer.parseInt(value);
      if (n < MIN || n > MAX) {
        System.err.println(I18n.translate("optionplayer.err.invalidCount", n, MIN, MAX));
        exitHandler.exit(1);
        return DEFAULT;
      }
      return n;
    } catch (NumberFormatException e) {
      System.err.println(I18n.translate("optionplayer.err.notInteger", value));
      exitHandler.exit(1);
      return DEFAULT;
    }
  }
}