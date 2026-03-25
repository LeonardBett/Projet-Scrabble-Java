package fr.ubordeaux.scrabble;

import fr.ubordeaux.scrabble.model.enums.GameMode;
import fr.ubordeaux.scrabble.view.optionlancement.CliLauncher;
import fr.ubordeaux.scrabble.view.optionlancement.GuiLauncher;
import fr.ubordeaux.scrabble.view.optionlancement.HelpPrinter;
import fr.ubordeaux.scrabble.view.optionlancement.OptionPlayer;
import java.util.ArrayList;
import java.util.List;

/**
 * Main entry point of the application. Parses command-line arguments and launches the appropriate
 * interface.
 */
public class App {

  /**
   * Starts the application and routes to CLI or GUI mode based on command-line options.
   *
   * @param args Application command-line arguments.
   */
  public static void main(String[] args) {
    int players = OptionPlayer.DEFAULT;
    boolean guiMode = false;
    boolean superMode = false;
    boolean blitzMode = false;
    int blitzMinutes = 30;
    int aiTime = 5;
    boolean useExptiminimax = false;
    boolean useMl = false;
    String lang = "en";

    // List to store the colors of players that should be controlled by AI
    List<String> aiColors = new ArrayList<>();

    for (int i = 0; i < args.length; i++) {
      switch (args[i]) {
        case "-h", "--help" -> HelpPrinter.printHelp();
        case "-V", "--version" -> HelpPrinter.printVersion();
        case "-g", "--gui" -> guiMode = true;
        case "-s", "--super" -> superMode = true;
        case "-b", "--blitz" -> {
          if (i + 1 < args.length && (args[i + 1].equals("-t") || args[i + 1].equals("--time"))) {
            if (i + 2 >= args.length) {
              System.err.println("Missing value for blitz time. Using default of 30 minutes.");
              blitzMode = true;
            } else {
              i++; // Skip -t/--time token and read the numeric value next.
              try {
                blitzMinutes = Integer.parseInt(args[++i]);
                blitzMode = true;
              } catch (NumberFormatException e) {
                System.err.println("Invalid blitz time. Using default of 30 minutes.");
                blitzMode = true;
              }
            }
          } else {
            blitzMode = true;
          }
        }
        case "-ai-exptiminimax", "--ai-exptiminimax" -> useExptiminimax = true;
        case "--ai-ml" -> useMl = true;
        case "-a", "--ai" -> {
          if (i + 1 >= args.length) {
            System.err.println("'-a' attend une couleur (ex: -a BLUE).");
            System.exit(1);
          }
          aiColors.add(args[++i].toUpperCase());
        }
        case "-p", "--players" -> {
          if (i + 1 >= args.length) {
            System.err.println("'-p' attend un nombre (ex: -p 3).");
            System.exit(1);
          }
          players = OptionPlayer.parsePlayers(args[++i]);
        }
        case "-l", "--lang" -> {
          if (i + 1 >= args.length) {
            System.err.println("Missing value for language. Using default 'en'.");
          } else {
            lang = args[++i].toLowerCase();
            if (!lang.equals("fr") && !lang.equals("en")) {
              System.err.println("Unsupported language: " + lang + ". Falling back to 'en'.");
              lang = "en";
            }
          }
        }
        case "-ai-time", "--ai-time" -> {
          if (i + 1 >= args.length) {
            System.err.println("Missing value for AI time. Using default of 5 seconds.");
          } else {
            try {
              aiTime = Integer.parseInt(args[++i]);
            } catch (NumberFormatException e) {
              System.err.println("Invalid AI time. Using default of 5 seconds.");
            }
          }
        }
        default -> {
          System.err.println("Option inconnue : " + args[i]);
          System.err.println("Utilisez -h ou --help pour afficher l'aide.");
          System.exit(1);
        }
      }
    }

    GameMode mode = superMode ? GameMode.SUPER : GameMode.STANDARD;

    if (guiMode) {
      launchGui(args, players, aiColors, blitzMode, blitzMinutes, aiTime, useExptiminimax, useMl,
          lang);
    } else {
      launchCli(players, aiColors, blitzMode, blitzMinutes, aiTime, useExptiminimax, useMl, lang);
    }
  }

  /**
   * Launches the Command Line Interface (CLI) mode.
   *
   * @param players the number of players (0 = ask interactively)
   * @param aiColors colors controlled by AI players
   * @param blitzMode True if blitz mode is enabled.
   * @param blitzMinutes Time limit per player in minutes (used only when blitzMode is true).
   * @param aiTime The thinking time allocated for the AI in seconds.
   * @param useExptiminimax True if the AI should use the Exptiminimax algorithm.
   * @param useMl True if the AI should use Machine Learning for word search.
   * @param lang The language of the dictionary to load ("en" or "fr").
   */

  private static void launchCli(int players, List<String> aiColors, boolean blitzMode,
      int blitzMinutes, int aiTime, boolean useExptiminimax, boolean useMl, String lang) {
    CliLauncher.launch(players, aiColors, blitzMode, blitzMinutes, aiTime, useExptiminimax, useMl,
        lang);
  }


  /**
   * Launches the Graphical User Interface (GUI) mode.
   *
   * @param args Application command-line arguments passed to JavaFX.
   * @param players the number of players (0 = use default of 2)
   * @param aiColors colors controlled by AI players
   * @param blitzMode True if blitz mode is enabled.
   * @param blitzMinutes Time limit per player in minutes (used only when blitzMode is true).
   * @param aiTime The thinking time allocated for the AI in seconds.
   * @param useExptiminimax True if the AI should use the Exptiminimax algorithm.
   * @param useMl True if the AI should use Machine Learning for word search.
   * @param lang The language of the dictionary to load ("en" or "fr").
   */
  private static void launchGui(String[] args, int players, List<String> aiColors,
      boolean blitzMode, int blitzMinutes, int aiTime, boolean useExptiminimax, boolean useMl,
      String lang) {
    GuiLauncher.launch(args, players, aiColors, blitzMode, blitzMinutes, aiTime, useExptiminimax,
        useMl, lang);
  }


}
