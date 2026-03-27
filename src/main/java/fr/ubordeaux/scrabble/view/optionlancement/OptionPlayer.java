package fr.ubordeaux.scrabble.view.optionlancement;

import fr.ubordeaux.scrabble.i18n.I18n;

/**
 * Parse et valide l'argument du nombre de joueurs (-p N).
 */
public class OptionPlayer {

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

  private OptionPlayer() {
  }

  /**
   * Analyse et valide la valeur de l'argument {@code -p} (nombre de joueurs).
   * Termine le programme avec un message d'erreur si la valeur est invalide.
   *
   * @param value la chaîne à analyser
   * @return le nombre de joueurs valide
   */
  public static int parsePlayers(String value) {
    try {
      int n = Integer.parseInt(value);
      if (n < MIN || n > MAX) {
        System.err.println(I18n.translate("optionplayer.err.invalidCount", n, MIN, MAX));
        System.exit(1);
      }
      return n;
    } catch (NumberFormatException e) {
      System.err.println(I18n.translate("optionplayer.err.notInteger", value));
      System.exit(1);
      return DEFAULT;
    }
  }
}