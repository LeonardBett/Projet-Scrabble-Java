package fr.ubordeaux.scrabble;

<<<<<<< HEAD
<<<<<<< HEAD
import fr.ubordeaux.scrabble.model.enums.GameMode;
import fr.ubordeaux.scrabble.model.utils.GameLogger;
=======
import fr.ubordeaux.scrabble.i18n.I18n;
>>>>>>> 80eb4dd (Add internationalization support for GUI and CLI components)
=======
import fr.ubordeaux.scrabble.i18n.I18n;
>>>>>>> 80eb4dd275121a2ebeee7238584257f4cce5f53f
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

  @FunctionalInterface
  interface ExitHandler {
    void exit(int status);
  }

  @FunctionalInterface
  interface CliLauncherHandler {
    void launch(int players, List<String> aiColors, boolean blitzMode, int blitzMinutes, int aiTime,
        boolean useExptiminimax, boolean useMl, String lang);
  }

  @FunctionalInterface
  interface GuiLauncherHandler {
    void launch(String[] args, int players, List<String> aiColors, boolean blitzMode,
        int blitzMinutes, int aiTime, boolean useExptiminimax, boolean useMl, String lang);
  }

  private static ExitHandler exitHandler = System::exit;
  private static CliLauncherHandler cliLauncherHandler = CliLauncher::launch;
  private static GuiLauncherHandler guiLauncherHandler = GuiLauncher::launch;

  /**
   * Default constructor for App. Should not be instantiated directly.
   */
  public App() {
  }

  static void setExitHandlerForTests(ExitHandler handler) {
    exitHandler = handler;
  }

  static void setCliDelegateForTests(CliLauncherHandler handler) {
    cliLauncherHandler = handler;
  }

  static void setGuiDelegateForTests(GuiLauncherHandler handler) {
    guiLauncherHandler = handler;
  }

  static void resetHandlersForTests() {
    exitHandler = System::exit;
    cliLauncherHandler = CliLauncher::launch;
    guiLauncherHandler = GuiLauncher::launch;
  }

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
    String lang = resolveInitialLanguage(args);
    I18n.setLanguage(lang);
<<<<<<< HEAD

    // List to store the colors of players that should be controlled by AI
    List<String> aiColors = new ArrayList<>();
=======
>>>>>>> 80eb4dd275121a2ebeee7238584257f4cce5f53f

    for (int i = 0; i < args.length; i++) {
      switch (args[i]) {
        case "-h", "--help" -> HelpPrinter.printHelp();
        case "-V", "--version" -> HelpPrinter.printVersion();
        case "-g", "--gui" -> guiMode = true;
<<<<<<< HEAD
<<<<<<< HEAD
        case "-s", "--super" -> superMode = true;
=======
>>>>>>> c984150 (feat: Enhance game configuration and blitz mode functionality)
=======
>>>>>>> 80eb4dd275121a2ebeee7238584257f4cce5f53f
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
        case "-v", "--verbose" -> GameLogger.setVerbose(true);
        case "-d", "--debug" -> GameLogger.setDebug(true);
        case "--ai-ml" -> useMl = true;
        case "-a", "--ai" -> {
          if (i + 1 >= args.length) {
            System.err.println("'-a' attend une couleur (ex: -a BLUE).");
            exitHandler.exit(1);
          }
          aiColors.add(args[++i].toUpperCase());
        }
        case "-p", "--players" -> {
          if (i + 1 >= args.length) {
<<<<<<< HEAD
<<<<<<< HEAD
            System.err.println("'-p' attend un nombre (ex: -p 3).");
            exitHandler.exit(1);
=======
=======
>>>>>>> 80eb4dd275121a2ebeee7238584257f4cce5f53f
            System.err.println(I18n.tr("app.err.playersMissing"));
            System.exit(1);
>>>>>>> 80eb4dd (Add internationalization support for GUI and CLI components)
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
<<<<<<< HEAD
<<<<<<< HEAD
          System.err.println("Option inconnue : " + args[i]);
          System.err.println("Utilisez -h ou --help pour afficher l'aide.");
          exitHandler.exit(1);
=======
=======
>>>>>>> 80eb4dd275121a2ebeee7238584257f4cce5f53f
          System.err.println(I18n.tr("app.err.unknownOption", args[i]));
          System.err.println(I18n.tr("app.err.useHelp"));
          System.exit(1);
>>>>>>> 80eb4dd (Add internationalization support for GUI and CLI components)
        }
      }
    }

<<<<<<< HEAD
<<<<<<< HEAD
    GameMode mode = superMode ? GameMode.SUPER : GameMode.STANDARD;
    if (mode == null) {
      throw new IllegalStateException("Game mode should never be null.");
    }
=======
    I18n.setLanguage(lang);
>>>>>>> 80eb4dd (Add internationalization support for GUI and CLI components)

    if (guiMode) {
<<<<<<< HEAD
      launchGui(args, players, aiColors, blitzMode, blitzMinutes, aiTime, useExptiminimax, useMl,
          lang);
    } else {
      launchCli(players, aiColors, blitzMode, blitzMinutes, aiTime, useExptiminimax, useMl, lang);
    }
  }

=======
      launchGui(args, players, blitzMode, blitzMinutes, aiTime, useExptiminimax, useMl, lang);
    } else {
      launchCli(players, blitzMode, blitzMinutes, aiTime, useExptiminimax, useMl, lang);
    }
  }

=======
    I18n.setLanguage(lang);

    if (guiMode) {
      launchGui(args, players, blitzMode, blitzMinutes, aiTime, useExptiminimax, useMl, lang);
    } else {
      launchCli(players, blitzMode, blitzMinutes, aiTime, useExptiminimax, useMl, lang);
    }
  }

>>>>>>> 80eb4dd275121a2ebeee7238584257f4cce5f53f
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

  
<<<<<<< HEAD
>>>>>>> c984150 (feat: Enhance game configuration and blitz mode functionality)
=======
>>>>>>> 80eb4dd275121a2ebeee7238584257f4cce5f53f
  /**
   * Launches the Command Line Interface (CLI) mode.
   *
   * @param players the number of players (0 = ask interactively)
<<<<<<< HEAD
<<<<<<< HEAD
   * @param aiColors colors controlled by AI players
=======
>>>>>>> c984150 (feat: Enhance game configuration and blitz mode functionality)
=======
>>>>>>> 80eb4dd275121a2ebeee7238584257f4cce5f53f
   * @param blitzMode True if blitz mode is enabled.
   * @param blitzMinutes Time limit per player in minutes (used only when blitzMode is true).
   * @param aiTime The thinking time allocated for the AI in seconds.
   * @param useExptiminimax True if the AI should use the Exptiminimax algorithm.
   * @param useMl True if the AI should use Machine Learning for word search.
   * @param lang The language of the dictionary to load ("en" or "fr").
   */
<<<<<<< HEAD
<<<<<<< HEAD

  private static void launchCli(int players, List<String> aiColors, boolean blitzMode,
      int blitzMinutes, int aiTime, boolean useExptiminimax, boolean useMl, String lang) {
    cliLauncherHandler.launch(players, aiColors, blitzMode, blitzMinutes, aiTime,
        useExptiminimax, useMl, lang);
=======
  private static void launchCli(int players, boolean blitzMode, int blitzMinutes, int aiTime,
      boolean useExptiminimax, boolean useMl, String lang) {
    CliLauncher.launch(players, blitzMode, blitzMinutes, aiTime, useExptiminimax, useMl, lang);
>>>>>>> c984150 (feat: Enhance game configuration and blitz mode functionality)
=======
  private static void launchCli(int players, boolean blitzMode, int blitzMinutes, int aiTime,
      boolean useExptiminimax, boolean useMl, String lang) {
    CliLauncher.launch(players, blitzMode, blitzMinutes, aiTime, useExptiminimax, useMl, lang);
>>>>>>> 80eb4dd275121a2ebeee7238584257f4cce5f53f
  }


  /**
   * Launches the Graphical User Interface (GUI) mode.
   *
   * @param args Application command-line arguments passed to JavaFX.
   * @param players the number of players (0 = use default of 2)
<<<<<<< HEAD
<<<<<<< HEAD
   * @param aiColors colors controlled by AI players
=======
>>>>>>> c984150 (feat: Enhance game configuration and blitz mode functionality)
=======
>>>>>>> 80eb4dd275121a2ebeee7238584257f4cce5f53f
   * @param blitzMode True if blitz mode is enabled.
   * @param blitzMinutes Time limit per player in minutes (used only when blitzMode is true).
   * @param aiTime The thinking time allocated for the AI in seconds.
   * @param useExptiminimax True if the AI should use the Exptiminimax algorithm.
   * @param useMl True if the AI should use Machine Learning for word search.
   * @param lang The language of the dictionary to load ("en" or "fr").
   */
<<<<<<< HEAD
<<<<<<< HEAD
  private static void launchGui(String[] args, int players, List<String> aiColors,
      boolean blitzMode, int blitzMinutes, int aiTime, boolean useExptiminimax, boolean useMl,
      String lang) {
    guiLauncherHandler.launch(args, players, aiColors, blitzMode, blitzMinutes, aiTime,
        useExptiminimax, useMl, lang);
=======
  private static void launchGui(String[] args, int players, boolean blitzMode, int blitzMinutes,
      int aiTime, boolean useExptiminimax, boolean useMl, String lang) {
<<<<<<< HEAD
    GuiLauncher.launch(args, players, blitzMode, blitzMinutes);
>>>>>>> c984150 (feat: Enhance game configuration and blitz mode functionality)
=======
    GuiLauncher.launch(args, players, blitzMode, blitzMinutes, aiTime, useExptiminimax, useMl,
        lang);
>>>>>>> 80eb4dd (Add internationalization support for GUI and CLI components)
=======
  private static void launchGui(String[] args, int players, boolean blitzMode, int blitzMinutes,
      int aiTime, boolean useExptiminimax, boolean useMl, String lang) {
    GuiLauncher.launch(args, players, blitzMode, blitzMinutes, aiTime, useExptiminimax, useMl,
        lang);
>>>>>>> 80eb4dd275121a2ebeee7238584257f4cce5f53f
  }


}
