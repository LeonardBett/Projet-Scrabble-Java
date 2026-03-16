package fr.ubordeaux.scrabble.view.optionlancement;


/**
 * Parse et valide l'argument du nombre de joueurs (-p N).
 */
public class OptionPlayer {

  public static final int MIN = 2;
  public static final int MAX = 4;
  public static final int DEFAULT = 2;

  private OptionPlayer() {}

  public static int parsePlayers(String value) {
    try {
      int n = Integer.parseInt(value);
      if (n < MIN || n > MAX) {
        System.err.println("Nombre de joueurs invalide : " + n + " (valeurs acceptées : " + MIN
            + " à " + MAX + ").");
        System.exit(1);
      }
      return n;
    } catch (NumberFormatException e) {
      System.err.println("'-p' attend un entier, reçu : " + value);
      System.exit(1);
      return DEFAULT;
    }
  }
}
