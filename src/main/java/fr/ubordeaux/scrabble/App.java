package fr.ubordeaux.scrabble;

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
    boolean blitzMode = false;
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
        case "-b", "--blitz" -> blitzMode = true;
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

    if (guiMode) {
      GuiLauncher.launch(args, players);
    } else {
      CliLauncher.launch(players, aiColors);
    }
  }
}