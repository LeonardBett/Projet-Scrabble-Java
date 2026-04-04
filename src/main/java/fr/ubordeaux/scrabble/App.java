package fr.ubordeaux.scrabble;

import fr.ubordeaux.scrabble.i18n.I18n;
import fr.ubordeaux.scrabble.model.core.Board;
import fr.ubordeaux.scrabble.model.core.Game;
import fr.ubordeaux.scrabble.model.core.MoveGenerator;
import fr.ubordeaux.scrabble.model.core.PlayableWord;
import fr.ubordeaux.scrabble.model.core.Tile;
import fr.ubordeaux.scrabble.model.dictionary.Gaddag;
import fr.ubordeaux.scrabble.model.enums.GameMode;
import fr.ubordeaux.scrabble.model.network.NetworkManager;
import fr.ubordeaux.scrabble.model.savefiles.ConfigLoader;
import fr.ubordeaux.scrabble.model.savefiles.GameLoader;
import fr.ubordeaux.scrabble.model.utils.GameLogger;
import fr.ubordeaux.scrabble.model.utils.Point;
import fr.ubordeaux.scrabble.view.optionlancement.CliLauncher;
import fr.ubordeaux.scrabble.view.optionlancement.GuiLauncher;
import fr.ubordeaux.scrabble.view.optionlancement.HelpPrinter;
import fr.ubordeaux.scrabble.view.optionlancement.OptionPlayer;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Scanner;

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
        GameMode gameMode,
        int players,
        List<String> aiColors,
        boolean blitzMode,
        int blitzMinutes,
        int aiTime,
        boolean useExptiminimax,
        boolean useMl,
        String lang,
        String saveFilePath);
  }

  @FunctionalInterface
  interface GuiLauncherHandler {
    void launch(
        String[] args,
        GameMode gameMode,
        int players,
        List<String> aiColors,
        boolean blitzMode,
        int blitzMinutes,
        int aiTime,
        boolean useExptiminimax,
        boolean useMl,
        String lang,
        String saveFilePath);
  }

  private static ExitHandler exitHandler = System::exit;
  private static CliLauncherHandler cliLauncherHandler = CliLauncher::launch;
  private static GuiLauncherHandler guiLauncherHandler = GuiLauncher::launch;

  private enum HelpLaunchChoice {
    CLI,
    GUI,
    NONE
  }

  private enum HelpLaunchMode {
    NONE,
    SHORTCUT,
    ARGS
  }

  private static final class HelpLaunchRequest {
    private final HelpLaunchMode mode;
    private final String[] args;
    private final HelpLaunchChoice shortcut;

    private HelpLaunchRequest(HelpLaunchMode mode, String[] args, HelpLaunchChoice shortcut) {
      this.mode = mode;
      this.args = args;
      this.shortcut = shortcut;
    }

    private static HelpLaunchRequest none() {
      return new HelpLaunchRequest(HelpLaunchMode.NONE, new String[] {}, HelpLaunchChoice.NONE);
    }

    private static HelpLaunchRequest shortcut(HelpLaunchChoice shortcut) {
      return new HelpLaunchRequest(HelpLaunchMode.SHORTCUT, new String[] {}, shortcut);
    }

    private static HelpLaunchRequest args(String[] args) {
      return new HelpLaunchRequest(HelpLaunchMode.ARGS, args, HelpLaunchChoice.NONE);
    }
  }

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

  private static int parseConfigInt(ConfigLoader configLoader, String key, int fallback) {
    String rawValue = configLoader.getOption(key, String.valueOf(fallback));
    try {
      return Integer.parseInt(rawValue);
    } catch (NumberFormatException e) {
      System.err.println("Warning: invalid value for " + key + ": " + rawValue
          + ". Using default " + fallback + ".");
      return fallback;
    }
  }

  /**
   * Starts the application and routes to CLI or GUI mode based on command-line options.
   *
   * @param args Application command-line arguments.
   */
  public static void main(String[] args) {
    // Load configuration (user/home/.scrabblerc) (f2)
    ConfigLoader configLoader = new ConfigLoader();
    configLoader.loadConfig();

    String lang = languageFromEnvironment();
    lang = configLoader.getOption("language", lang);

    // Set language
    I18n.setLanguage(lang);

    // Load the parameters from the config file
    GameLogger.setVerbose(Boolean.parseBoolean(configLoader.getOption("verbose", "false")));
    GameLogger.setDebug(Boolean.parseBoolean(configLoader.getOption("debug", "false")));
    int players = parseConfigInt(configLoader, "players-count", OptionPlayer.DEFAULT);
    boolean guiMode = Boolean.parseBoolean(configLoader.getOption("gui", "false"));
    boolean superMode = Boolean.parseBoolean(configLoader.getOption("super-scrabble",
        "false"));
    boolean blitzMode = Boolean.parseBoolean(configLoader.getOption("blitz", "false"));
    int blitzMinutes = parseConfigInt(configLoader, "timeout", 30);
    int aiTime = parseConfigInt(configLoader, "ai-time", 5);
    boolean useExptiminimax = Boolean.parseBoolean(configLoader.getOption("ai-exptiminimax",
        "false"));
    boolean useMl = Boolean.parseBoolean(configLoader.getOption("ai-ml", "false"));
    boolean timeOptionProvided = false;
    boolean contestMode = false;
    String contestFilePath = null;
    String customDictionaryPath = null;

    // Network arguments
    boolean startServer = false; // Server mode
    boolean daemonMode = false; // Headless mode
    int serverPort =
        NetworkManager.DEFAULT_TCP_PORT; // Server port, default value for headless start
    boolean helpRequested = false;

    // List to store the colors of players that should be controlled by AI
    List<String> aiColors = new ArrayList<>();

    for (int i = 0; i < args.length; i++) {
      switch (args[i]) {
        case "-h", "--help" -> {
          HelpPrinter.printHelp();
          return;
        }
        case "--list-languages" -> {
          printSupportedLanguages();
          return;
        }
        case "-V", "--version" -> {
          HelpPrinter.printVersion();
          return;
        }
        case "-g", "--gui" -> guiMode = true;
        case "-s", "--super" -> superMode = true;
        case "-b", "--blitz" -> blitzMode = true;
        case "-t", "--time" -> {
          timeOptionProvided = true;
          if (i + 1 >= args.length) {
            System.err.println("Missing value for blitz time. Using default of 30 minutes.");
          } else {
            try {
              blitzMinutes = Integer.parseInt(args[++i]);
            } catch (NumberFormatException e) {
              System.err.println("Invalid blitz time. Using default of 30 minutes.");
            }
          }
        }
        case "-ai-exptiminimax", "--ai-exptiminimax" -> useExptiminimax = true;
        case "-c", "--contest" -> {
          contestMode = true;
          if (i + 1 >= args.length || args[i + 1].startsWith("-")) {
            System.err.println("Missing file path for contest mode.");
            exitHandler.exit(1);
            return;
          }
          contestFilePath = args[++i];
        }
        case "-v", "--verbose" -> GameLogger.setVerbose(true);
        case "-d", "--debug" -> GameLogger.setDebug(true);
        case "--ai-ml" -> useMl = true;
        case "-a", "--ai" -> {
          if (i + 1 >= args.length) {
            System.err.println(I18n.translate("app.err.missingAiColor"));
            exitHandler.exit(1);
            return;
          }
          aiColors.add(args[++i].toUpperCase());
        }
        case "-p", "--players" -> {
          if (i + 1 >= args.length) {
            System.err.println(I18n.translate("app.err.missingPlayers"));
            exitHandler.exit(1);
            return;
          }
          players = OptionPlayer.parsePlayers(args[++i]);
        }
        case "-l", "--lang", "--language" -> {
          if (i + 1 >= args.length) {
            System.err.println(I18n.translate("app.warn.missingLanguageValue"));
          } else {
            lang = normalizeLanguageOrDefault(args[++i]);
            I18n.setLanguage(lang);
          }
        }
        case "-D", "--dictionary" -> {
          if (i + 1 >= args.length || args[i + 1].startsWith("-")) {
            System.err.println("Missing value for dictionary path.");
            exitHandler.exit(1);
            return;
          }
          customDictionaryPath = args[++i];
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
                return;
              }
            } catch (NumberFormatException e) {
              System.err.println("-S / --server : Invalid port argument.");
              exitHandler.exit(1);
              return;
            }
          } else {
            System.err.println("-S / --server : Missing port argument.");
            exitHandler.exit(1);
            return;
          }
        }

        case "--daemon" -> {
          daemonMode = true;
          startServer = true;
        }

        default -> {
          // Accept a single positional argument as save file path.
          if (args.length == 1 && !args[i].startsWith("-")) {
            continue;
          }
          System.err.println(I18n.translate("app.err.unknownOption", args[i]));
          System.err.println(I18n.translate("app.err.helpHint"));
          HelpPrinter.printHelp();
          exitHandler.exit(1);
          return;
        }
      }
    }

    if (helpRequested) {
      HelpPrinter.printHelp();
      HelpLaunchRequest request = promptLaunchRequestAfterHelp();
      if (request.mode == HelpLaunchMode.NONE) {
        return;
      }
      if (request.mode == HelpLaunchMode.SHORTCUT) {
        guiMode = request.shortcut == HelpLaunchChoice.GUI;
      } else {
        main(request.args);
        return;
      }
    }

    // Check program start with server start
    if (timeOptionProvided && !blitzMode) {
      System.err.println("Warning: --time is ignored without --blitz.");
      blitzMinutes = 30;
    }

    if (customDictionaryPath != null) {
      System.setProperty("scrabble.dictionary.path", customDictionaryPath);
    } else {
      System.clearProperty("scrabble.dictionary.path");
    }

    if (contestMode) {
      try {
        runContest(contestFilePath);
      } catch (Exception e) {
        System.err.println("Contest mode error: " + e.getMessage());
        exitHandler.exit(1);
      }
      return;
    }

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

    String saveFilePath = (args.length == 1 && !args[0].startsWith("-")) ? args[0] : null;

    if (guiMode) {
      launchGui(
          args,
          mode,
          players,
          aiColors,
          blitzMode,
          blitzMinutes,
          aiTime,
          useExptiminimax,
          useMl,
          lang,
          saveFilePath);
    } else {
      launchCli(
          mode,
          players,
          aiColors,
          blitzMode,
          blitzMinutes,
          aiTime,
          useExptiminimax,
          useMl,
          lang,
          saveFilePath);
    }
  }

  private static HelpLaunchRequest promptLaunchRequestAfterHelp() {
    if (System.console() == null) {
      return HelpLaunchRequest.none();
    }

    System.out.print(I18n.translate("app.help.launchPrompt") + " ");
    Scanner scanner = new Scanner(System.in);
    String answer = scanner.nextLine();
    if (answer == null) {
      return HelpLaunchRequest.none();
    }

    String normalized = answer.trim().toLowerCase(Locale.ROOT);
    if (normalized.isBlank() || normalized.equals("n") || normalized.equals("no")
        || normalized.equals("non") || normalized.equals("q") || normalized.equals("quit")) {
      return HelpLaunchRequest.none();
    }

    if (normalized.equals("c") || normalized.equals("cli")) {
      return HelpLaunchRequest.shortcut(HelpLaunchChoice.CLI);
    }

    if (normalized.equals("g") || normalized.equals("gui")) {
      return HelpLaunchRequest.shortcut(HelpLaunchChoice.GUI);
    }

    String[] launchArgs = normalized.split("\\s+");
    if (launchArgs.length == 0) {
      System.out.println(I18n.translate("app.help.launchInvalidChoice"));
      return HelpLaunchRequest.none();
    }

    return HelpLaunchRequest.args(launchArgs);
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
      GameMode gameMode,
      int players,
      List<String> aiColors,
      boolean blitzMode,
      int blitzMinutes,
      int aiTime,
      boolean useExptiminimax,
      boolean useMl,
      String lang,
      String saveFilePath) {
    cliLauncherHandler.launch(
        gameMode,
        players,
        aiColors,
        blitzMode,
        blitzMinutes,
        aiTime,
        useExptiminimax,
        useMl,
        lang,
        saveFilePath);
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
      GameMode gameMode,
      int players,
      List<String> aiColors,
      boolean blitzMode,
      int blitzMinutes,
      int aiTime,
      boolean useExptiminimax,
      boolean useMl,
      String lang,
      String saveFilePath) {
    guiLauncherHandler.launch(
        args,
        gameMode,
        players,
        aiColors,
        blitzMode,
        blitzMinutes,
        aiTime,
        useExptiminimax,
        useMl,
        lang,
        saveFilePath);
  }

  private static void runContest(String contestFilePath) throws Exception {
    if (contestFilePath == null || contestFilePath.isBlank()) {
      throw new IllegalArgumentException("Contest file path is required.");
    }

    Game game = new GameLoader().loadGame(contestFilePath);
    Gaddag gaddag = loadContestDictionary(game.getLanguage());
    MoveGenerator generator = new MoveGenerator();
    List<PlayableWord> moves = generator.getPlayableWordsList(game, gaddag);

    PlayableWord bestMove = null;
    int bestScore = Integer.MIN_VALUE;
    for (PlayableWord move : moves) {
      int score = estimateMoveScore(game.getBoard(), move);
      if (score > bestScore) {
        bestScore = score;
        bestMove = move;
      }
    }

    if (bestMove == null) {
      System.out.println("pass");
      return;
    }

    System.out.println(formatContestMove(bestMove));
  }

  private static Gaddag loadContestDictionary(String language) throws IOException {
    String normalizedLang = normalizeLanguageOrDefault(language);
    String dictPath = "dictionaries/lexicon_" + normalizedLang + ".txt";
    Gaddag gaddag = new Gaddag();

    try (InputStream is = App.class.getClassLoader().getResourceAsStream(dictPath)) {
      if (is == null) {
        throw new IOException("Dictionary not found for language: " + normalizedLang);
      }
      try (BufferedReader br = new BufferedReader(new InputStreamReader(is))) {
        String line;
        while ((line = br.readLine()) != null) {
          String word = line.trim().toUpperCase();
          if (!word.isEmpty()) {
            gaddag.add(word);
          }
        }
      }
    }
    return gaddag;
  }

  private static List<Character> getLettersFromRack(Board board, PlayableWord move) {
    List<Character> lettersFromRack = new ArrayList<>();
    String word = move.getWord();
    int hookIndex = move.getGaddagRepresentation().indexOf('>') - 1;

    int startX = move.getDirection() == fr.ubordeaux.scrabble.model.enums.Direction.HORIZONTAL
        ? move.getHookX() - hookIndex
        : move.getHookX();
    int startY = move.getDirection() == fr.ubordeaux.scrabble.model.enums.Direction.VERTICAL
        ? move.getHookY() - hookIndex
        : move.getHookY();

    for (int i = 0; i < word.length(); i++) {
      int x = move.getDirection() == fr.ubordeaux.scrabble.model.enums.Direction.HORIZONTAL
          ? startX + i
          : startX;
      int y = move.getDirection() == fr.ubordeaux.scrabble.model.enums.Direction.VERTICAL
          ? startY + i
          : startY;
      if (board.getSquare(new Point(x, y)).isEmpty()) {
        lettersFromRack.add(word.charAt(i));
      }
    }
    return lettersFromRack;
  }

  private static int estimateMoveScore(Board board, PlayableWord move) {
    int score = 0;
    for (char c : getLettersFromRack(board, move)) {
      score += new Tile(c).getValue();
    }
    return score;
  }

  private static String formatContestMove(PlayableWord move) {
    int hookIndex = move.getGaddagRepresentation().indexOf('>') - 1;
    int startX = move.getDirection() == fr.ubordeaux.scrabble.model.enums.Direction.HORIZONTAL
        ? move.getHookX() - hookIndex
        : move.getHookX();
    int startY = move.getDirection() == fr.ubordeaux.scrabble.model.enums.Direction.VERTICAL
        ? move.getHookY() - hookIndex
        : move.getHookY();

    char row = (char) ('a' + startY);
    int col = startX + 1;
    char dir = move.getDirection() == fr.ubordeaux.scrabble.model.enums.Direction.HORIZONTAL
        ? 'h'
        : 'v';

    return "" + row + col + dir + " " + move.getWord();
  }

  private static void printSupportedLanguages() {
    for (String language : I18n.getSupportedLanguages()) {
      System.out.println(language);
    }
  }
}
