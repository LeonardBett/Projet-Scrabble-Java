package fr.ubordeaux.scrabble;

import fr.ubordeaux.scrabble.i18n.I18n;
import fr.ubordeaux.scrabble.view.optionlancement.CliLauncher;
import fr.ubordeaux.scrabble.view.optionlancement.GuiLauncher;
import fr.ubordeaux.scrabble.view.optionlancement.HelpPrinter;
import fr.ubordeaux.scrabble.view.optionlancement.OptionPlayer;

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
    int blitzMinutes = 30;
    int aiTime = 5;
    boolean useExptiminimax = false;
    boolean useMl = false;
    String lang = resolveInitialLanguage(args);
    I18n.setLanguage(lang);

    for (int i = 0; i < args.length; i++) {
      switch (args[i]) {
        case "-h", "--help" -> HelpPrinter.printHelp();
        case "-V", "--version" -> HelpPrinter.printVersion();
        case "-g", "--gui" -> guiMode = true;
        case "-b", "--blitz" -> {
          if (i + 1 < args.length && (args[i + 1].equals("-t") || args[i + 1].equals("--time"))) {
            if (i + 2 >= args.length) {
              System.err.println(I18n.tr("app.err.missingBlitzTime"));
              blitzMode = true;
            } else {
              i++; // Skip -t/--time token and read the numeric value next.
              try {
                blitzMinutes = Integer.parseInt(args[++i]);
                blitzMode = true;
              } catch (NumberFormatException e) {
                System.err.println(I18n.tr("app.err.invalidBlitzTime"));
                blitzMode = true;
              }
            }
          } else {
            blitzMode = true;
          }
        }
        case "-ai-exptiminimax", "--ai-exptiminimax" -> useExptiminimax = true;
        case "--ai-ml" -> useMl = true;
        case "-p", "--players" -> {
          if (i + 1 >= args.length) {
            System.err.println(I18n.tr("app.err.playersMissing"));
            System.exit(1);
          }
          players = OptionPlayer.parsePlayers(args[++i]);
        }
        case "-l", "--lang" -> {
          if (i + 1 >= args.length) {
            System.err.println(I18n.tr("app.err.languageMissing"));
          } else {
            lang = args[++i].toLowerCase();
            if (!lang.equals("fr") && !lang.equals("en")) {
              System.err.println(I18n.tr("app.err.languageUnsupported", lang));
              lang = "en";
            }
            I18n.setLanguage(lang);
          }
        }
        case "-ai-time", "--ai-time" -> {
          if (i + 1 >= args.length) {
            System.err.println(I18n.tr("app.err.missingAiTime"));
          } else {
            try {
              aiTime = Integer.parseInt(args[++i]);
            } catch (NumberFormatException e) {
              System.err.println(I18n.tr("app.err.invalidAiTime"));
            }
          }
        }
        default -> {
          System.err.println(I18n.tr("app.err.unknownOption", args[i]));
          System.err.println(I18n.tr("app.err.useHelp"));
          System.exit(1);
        }
      }
    }

    I18n.setLanguage(lang);

    if (guiMode) {
      launchGui(args, players, blitzMode, blitzMinutes, aiTime, useExptiminimax, useMl, lang);
    } else {
      launchCli(players, blitzMode, blitzMinutes, aiTime, useExptiminimax, useMl, lang);
    }
  }

  private static String resolveInitialLanguage(String[] args) {
    for (int i = 0; i < args.length - 1; i++) {
      if ("-l".equals(args[i]) || "--lang".equals(args[i])) {
        String candidate = args[i + 1].toLowerCase();
        if ("fr".equals(candidate) || "en".equals(candidate)) {
          return candidate;
        }
      }
    }
    return "en";
  }

  
  /**
   * Launches the Command Line Interface (CLI) mode.
   *
   * @param players the number of players (0 = ask interactively)
   * @param blitzMode True if blitz mode is enabled.
   * @param blitzMinutes Time limit per player in minutes (used only when blitzMode is true).
   * @param aiTime The thinking time allocated for the AI in seconds.
   * @param useExptiminimax True if the AI should use the Exptiminimax algorithm.
   * @param useMl True if the AI should use Machine Learning for word search.
   * @param lang The language of the dictionary to load ("en" or "fr").
   */
  private static void launchCli(int players, boolean blitzMode, int blitzMinutes, int aiTime,
      boolean useExptiminimax, boolean useMl, String lang) {
    CliLauncher.launch(players, blitzMode, blitzMinutes, aiTime, useExptiminimax, useMl, lang);
  }


  /**
   * Launches the Graphical User Interface (GUI) mode.
   *
   * @param args Application command-line arguments passed to JavaFX.
   * @param players the number of players (0 = use default of 2)
   * @param blitzMode True if blitz mode is enabled.
   * @param blitzMinutes Time limit per player in minutes (used only when blitzMode is true).
   * @param aiTime The thinking time allocated for the AI in seconds.
   * @param useExptiminimax True if the AI should use the Exptiminimax algorithm.
   * @param useMl True if the AI should use Machine Learning for word search.
   * @param lang The language of the dictionary to load ("en" or "fr").
   */
  private static void launchGui(String[] args, int players, boolean blitzMode, int blitzMinutes,
      int aiTime, boolean useExptiminimax, boolean useMl, String lang) {
    GuiLauncher.launch(args, players, blitzMode, blitzMinutes, aiTime, useExptiminimax, useMl,
        lang);
  }


}
