package fr.ubordeaux.scrabble.view.optionlancement;

import fr.ubordeaux.scrabble.i18n.I18n;

/**
 * Displays the help and version information of the program. SRP: Single Responsibility Principle —
 * all help text is centralized here.
 */
public class HelpPrinter {

  /** The application version. */
  public static final String VERSION = "1.0.0";

  /** The application name. */
  public static final String APP_NAME = "Scrabble U-Bordeaux";

  private HelpPrinter() {}

  /**
   * Prints the help message to standard output.
   */
  public static void printHelp() {
    if (I18n.isFrench()) {
      System.out.println(I18n.tr("help.text.fr"));
      return;
    }
    System.out.println(I18n.tr("help.text.en"));
  }

  /**
   * Prints the version string to standard output.
   */
  public static void printVersion() {
    System.out.println(APP_NAME + " v" + VERSION);
  }
}