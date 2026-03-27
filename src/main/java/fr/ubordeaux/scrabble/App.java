package fr.ubordeaux.scrabble;

import fr.ubordeaux.scrabble.i18n.I18n;
import fr.ubordeaux.scrabble.model.enums.GameMode;
import fr.ubordeaux.scrabble.model.network.NetworkManager;
import fr.ubordeaux.scrabble.model.utils.GameLogger;
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

  private static final String DEFAULT_LANG = "en";

  @FunctionalInterface
  interface ExitHandler {
    void exit(int status);
  }

  @FunctionalInterface
  interface CliLauncherHandler {
    void launch(
        int players,
        List<String> aiColors,
        boolean blitzMode,
        int blitzMinutes,
        int aiTime,
        boolean useExptiminimax,
        boolean useMl,
        String lang);
  }

  @FunctionalInterface
  interface GuiLauncherHandler {
    void launch(
        String[] args,
        int players,
        List<String> aiColors,
        boolean blitzMode,
        int blitzMinutes,
        int aiTime,
        boolean useExptiminimax,
        boolean useMl,
        String lang);
  }

  private static ExitHandler exitHandler = System::exit;
  private static CliLauncherHandler cliLauncherHandler = CliLauncher::launch;
  private static GuiLauncherHandler guiLauncherHandler = GuiLauncher::launch;

  /** Default constructor for App. Should not be instantiated directly. */
  public App() {}

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

  private static String normalizeLanguageOrDefault(String rawLang) {
    if (rawLang == null || rawLang.isBlank()) {
      return DEFAULT_LANG;
    }

    String normalized = rawLang.trim().toLowerCase();
    int dotIndex = normalized.indexOf('.');
    if (dotIndex >= 0) {
      normalized = normalized.substring(0, dotIndex);
    }
    int atIndex = normalized.indexOf('@');
    if (atIndex >= 0) {
      normalized = normalized.substring(0, atIndex);
    }
    int underscoreIndex = normalized.indexOf('_');
    if (underscoreIndex >= 0) {
      normalized = normalized.substring(0, underscoreIndex);
    }

    if (normalized.equals("c") || normalized.equals("posix")) {
      return DEFAULT_LANG;
    }

    if (!normalized.equals("fr") && !normalized.equals("en")) {
      System.err.println(I18n.translate("app.warn.unsupportedLanguage", rawLang));
      return DEFAULT_LANG;
    }
    return normalized;
  }

  private static String languageFromEnvironment() {
    String lcAll = System.getenv("LC_ALL");
    if (lcAll != null && !lcAll.isBlank()) {
      return normalizeLanguageOrDefault(lcAll);
    }

    String lang = System.getenv("LANG");
    if (lang != null && !lang.isBlank()) {
      return normalizeLanguageOrDefault(lang);
    }

    return DEFAULT_LANG;
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
    String lang = languageFromEnvironment();
    I18n.setLanguage(lang);

    // Network arguments
    boolean startServer = false; // Server mode
    boolean daemonMode = false; // Headless mode
    int serverPort =
        NetworkManager.DEFAULT_TCP_PORT; // Server port, default value for headless start

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
        case "-v", "--verbose" -> GameLogger.setVerbose(true);
        case "-d", "--debug" -> GameLogger.setDebug(true);
        case "--ai-ml" -> useMl = true;
        case "-a", "--ai" -> {
          if (i + 1 >= args.length) {
            System.err.println(I18n.translate("app.err.missingAiColor"));
            exitHandler.exit(1);
          }
          aiColors.add(args[++i].toUpperCase());
        }
        case "-p", "--players" -> {
          if (i + 1 >= args.length) {
            System.err.println(I18n.translate("app.err.missingPlayers"));
            exitHandler.exit(1);
          }
          players = OptionPlayer.parsePlayers(args[++i]);
        }
        case "-l", "--lang" -> {
          if (i + 1 >= args.length) {
            System.err.println(I18n.translate("app.warn.missingLanguageValue"));
          } else {
            lang = normalizeLanguageOrDefault(args[++i]);
            I18n.setLanguage(lang);
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

        // Upper S because there is already an -s option in the specifications
        case "-S", "--server" -> {
          startServer = true;
          // We check that the next argument is a valid port number
          if (i + 1 < args.length && !args[i + 1].startsWith("-")) {
            try {
              serverPort = Integer.parseInt(args[++i]);
              if (serverPort < 0 || serverPort > 65535) {
                System.err.println("-S / --server : The port number must be between 0 and 65535.");
                exitHandler.exit(1);
              }
            } catch (NumberFormatException e) {
              System.err.println("-S / --server : Invalid port argument.");
              exitHandler.exit(1);
            }
          } else {
            System.err.println("-S / --server : Missing port argument.");
            exitHandler.exit(1);
          }
        }

        // Upper D because there is already an -d option in the specifications
        case "-D", "--daemon" -> {
          daemonMode = true;
          startServer = true;
        }

        default -> {
          System.err.println(I18n.translate("app.err.unknownOption", args[i]));
          System.err.println(I18n.translate("app.err.helpHint"));
          exitHandler.exit(1);
        }
      }
    }

    // Check program start with server start
    if (startServer) {
      NetworkManager tempManager = new NetworkManager();
      boolean success = tempManager.serverStart(serverPort);

      if (!success) {
        System.err.println(
            "Critical error : Could not start server on port " + serverPort);
        exitHandler.exit(1);
        return;
      }

      System.out.println("Server start with success on port " + serverPort);

      // In headless mode, the program stop here, and we do not launch GUI/CLI
      if (daemonMode) {
        System.out.println("Headless server start with success (daemon mode).");
        return;
      }
    }

    I18n.setLanguage(lang);

    GameMode mode = superMode ? GameMode.SUPER : GameMode.STANDARD;
    if (mode == null) {
      throw new IllegalStateException("Game mode should never be null.");
    }

    if (guiMode) {
      launchGui(
          args, players, aiColors, blitzMode, blitzMinutes, aiTime, useExptiminimax, useMl, lang);
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
  private static void launchCli(
      int players,
      List<String> aiColors,
      boolean blitzMode,
      int blitzMinutes,
      int aiTime,
      boolean useExptiminimax,
      boolean useMl,
      String lang) {
    cliLauncherHandler.launch(
        players, aiColors, blitzMode, blitzMinutes, aiTime, useExptiminimax, useMl, lang);
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
  private static void launchGui(
      String[] args,
      int players,
      List<String> aiColors,
      boolean blitzMode,
      int blitzMinutes,
      int aiTime,
      boolean useExptiminimax,
      boolean useMl,
      String lang) {
    guiLauncherHandler.launch(
        args, players, aiColors, blitzMode, blitzMinutes, aiTime, useExptiminimax, useMl, lang);
  }
}
