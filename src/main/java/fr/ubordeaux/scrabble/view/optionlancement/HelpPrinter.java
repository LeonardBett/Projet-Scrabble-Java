package fr.ubordeaux.scrabble.view.optionlancement;

import fr.ubordeaux.scrabble.i18n.I18n;

/**
 * Displays the help and version information of the program. SRP: Single Responsibility Principle —
 * all help text is centralized here.
 */
public class HelpPrinter {

  /**
   * The application version.
   */
  public static final String VERSION = "1.0.0";

  /**
   * The application name.
   */
  public static final String APP_NAME = "Scrabble U-Bordeaux";

  private HelpPrinter() {}

  /**
   * Prints the help message to standard output.
   */
  public static void printHelp() {
<<<<<<< HEAD
    System.out.println("""
<<<<<<< HEAD
          Usage : scrabble [OPTION]

          Options :
            -h, --help            Displays this help message and exits
            -V, --version         Displays the program version and exits
            -g, --gui             Launches the Graphical User Interface (JavaFX)
            -s, --super           Launches Super Scrabble mode with a 21x21 board
        -p N, --players N     Number of players: 2, 3 or 4 (default: 2)
            -b, --blitz           Launches the game in blitz mode
            -t TIME, --time TIME  Time limit per player in minutes in blitz mode (default: 30)
        -l, --lang LANG       Sets the dictionary language: 'en' or 'fr' (default: 'en')
=======
        Usage : scrabble [OPTION]

        Options :
          -h, --help            Displays this help message and exits
          -V, --version         Displays the program version and exits
          -g, --gui             Launches the Graphical User Interface (JavaFX)
          -p N, --players N     Number of players: 2, 3 or 4 (default: 2)
          -b, --blitz           Launches the game in blitz mode
          -t TIME, --time TIME  Time limit per player in minutes in blitz mode (default: 30)
          -l, --lang LANG       Sets the dictionary language: 'en' or 'fr' (default: 'en')
>>>>>>> c984150 (feat: Enhance game configuration and blitz mode functionality)

          AI Options :
            -ai-time TIME         Specifies the AI thinking time in seconds (default: 5)
            -ai-exptiminimax      Enables the Expectiminimax algorithm for the best move search
            --ai-ml               Enables the Machine Learning algorithm for word search

          Without any options, the game starts in terminal mode (CLI) with default parameters.

<<<<<<< HEAD
          Examples :
            java -jar scrabble.jar -l fr             Launches CLI with French dictionary
            java -jar scrabble.jar -b -t 20          Launches CLI blitz (20 min/player)
            java -jar scrabble.jar -g -b -t 10       Launches GUI blitz mode (10 min per player)
            java -jar scrabble.jar --ai-ml -l en     Launches CLI + ML with English models
          """);
=======
        Examples :
          java -jar scrabble.jar -l fr             Launches in CLI mode with the French dictionary
          java -jar scrabble.jar -b -t 20          Launches in CLI blitz mode with 20 min per player
          java -jar scrabble.jar -g -b -t 10       Launches in GUI blitz mode with 10 min per player
          java -jar scrabble.jar --ai-ml -l en     Launches in CLI with ML using English models
        """);
>>>>>>> c984150 (feat: Enhance game configuration and blitz mode functionality)
=======
    if (I18n.isFrench()) {
      System.out.println(I18n.tr("help.text.fr"));
      return;
    }
    System.out.println(I18n.tr("help.text.en"));
>>>>>>> 80eb4dd (Add internationalization support for GUI and CLI components)
  }

  /**
   * Prints the version string to standard output.
   */
  public static void printVersion() {
    System.out.println(APP_NAME + " v" + VERSION);
  }
}