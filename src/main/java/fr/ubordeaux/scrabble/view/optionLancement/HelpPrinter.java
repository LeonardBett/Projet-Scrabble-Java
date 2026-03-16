package fr.ubordeaux.scrabble.view.optionLancement;


/**
 * Displays the help and version information of the program.
 *
 * <p>
 * SRP: Single Responsibility Principle — all help text is centralized here.
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
    System.out.println("""
        Usage : scrabble [OPTION]

        Options :
          -h, --help            Displays this help message and exits
          -V, --version         Displays the program version and exits
          -g, --gui             Launches the Graphical User Interface (JavaFX)
          -p N, --players N     Number of players: 2, 3 or 4 (default: 2)
          -b, --blitz           Launches the game in blitz mode
          -l, --lang LANG       Sets the dictionary language: 'en' or 'fr' (default: 'en')

        AI Options :
          -ai-time TIME         Specifies the AI thinking time in seconds (default: 5)
          -ai-exptiminimax      Enables the Expectiminimax algorithm for the best move search
          --ai-ml               Enables the Machine Learning algorithm for word search

        Without any options, the game starts in terminal mode (CLI) with default parameters.

        Examples :
          java -jar scrabble.jar -l fr           Launches in CLI mode with the French dictionary
          java -jar scrabble.jar --ai-ml -l en   Launches in CLI with ML using English models
        """);
  }

  /**
   * Prints the version string to standard output.
   */
  public static void printVersion() {
    System.out.println(APP_NAME + " v" + VERSION);
  }
}
