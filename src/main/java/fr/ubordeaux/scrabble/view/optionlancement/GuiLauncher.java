package fr.ubordeaux.scrabble.view.optionlancement;

import fr.ubordeaux.scrabble.model.ai.AiPlayer;
import fr.ubordeaux.scrabble.model.core.Game;
import fr.ubordeaux.scrabble.model.core.HumanPlayer;
import fr.ubordeaux.scrabble.model.enums.GameMode;
import fr.ubordeaux.scrabble.model.enums.PlayerColor;
import fr.ubordeaux.scrabble.model.savefiles.ConfigLoader;
import fr.ubordeaux.scrabble.model.savefiles.GameLoader;
import fr.ubordeaux.scrabble.view.gui.JavaFxView;
import fr.ubordeaux.scrabble.view.gui.ScrabbleGui;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import javafx.application.Application;

/**
 * Lance le jeu en mode GUI JavaFX.
 */
public class GuiLauncher {

  @FunctionalInterface
  interface LaunchHandler {
    void launch(Class<? extends Application> appClass, String[] args);
  }

  private static LaunchHandler launchHandler = Application::launch;

  private GuiLauncher() {}

  static void setLaunchHandlerForTests(LaunchHandler handler) {
    launchHandler = handler;
  }

  static void resetLaunchHandlerForTests() {
    launchHandler = Application::launch;
  }

  public static Game createConfiguredGame(int players, List<String> aiColors, boolean blitzMode,
      int blitzMinutes, int aiTime, boolean useExptiminimax) {
    return createConfiguredGame(GameMode.STANDARD, players, aiColors, blitzMode, blitzMinutes,
        aiTime, useExptiminimax);
  }

  /**
   * Creates a configured game for GUI or launcher usage.
   *
   * @param gameMode the board mode to use
   * @param players the number of players
   * @param aiColors colors controlled by AI players
   * @param blitzMode true to enable blitz mode
   * @param blitzMinutes time limit per player in minutes
   * @param aiTime AI thinking time in seconds
   * @param useExptiminimax true to enable the Expectiminimax algorithm
   * @return a configured game instance
   */
  public static Game createConfiguredGame(GameMode gameMode, int players, List<String> aiColors,
      boolean blitzMode, int blitzMinutes, int aiTime, boolean useExptiminimax) {
    int count = players > 0 ? players : OptionPlayer.DEFAULT;
    Game game = new Game(gameMode == null ? GameMode.STANDARD : gameMode);
    if (blitzMode) {
      game.enableBlitzMode(Duration.ofMinutes(blitzMinutes));
    }
    int humanCount = 1;
    for (int i = 1; i <= count; i++) {
      PlayerColor color = PlayerColor.fromIndex(i - 1);
      boolean isAi = false;

      if (aiColors != null) {
        if (aiColors.contains("A")) {
          isAi = true;
        } else if (aiColors.stream().anyMatch(c -> c.equalsIgnoreCase(color.name()))) {
          isAi = true;
        }
      }

      if (isAi) {
        AiPlayer ai = new AiPlayer("IA-" + color.name(), 3, aiTime, color);
        ai.setExpectiminimaxMode(useExptiminimax);
        game.addPlayer(ai);
      } else {
        game.addPlayer(new HumanPlayer("Player" + humanCount, color));
        humanCount++;
      }
    }
    return game;
  }

  /**
   * Creates a GUI game from the current .scrabblerc configuration.
   *
   * @return a fully initialized game ready to be attached to the GUI
   */
  public static Game createGameFromConfig() {
    ConfigLoader config = new ConfigLoader();
    config.loadConfig();

    String language = config.getOption("language", "en");
    ScrabbleGui.setLanguage(language);
    boolean superScrabble = Boolean.parseBoolean(config.getOption("super-scrabble", "false"));
    boolean blitzMode = Boolean.parseBoolean(config.getOption("blitz", "false"));
    int players = readIntConfig(config, "players-count", OptionPlayer.DEFAULT);
    int blitzMinutes = readIntConfig(config, "timeout", 30);
    int aiTime = readIntConfig(config, "ai-time", 5);
    boolean useExptiminimax = Boolean.parseBoolean(config.getOption("ai-exptiminimax", "false"));

    GameMode gameMode = superScrabble ? GameMode.SUPER : GameMode.STANDARD;
    return createConfiguredGame(gameMode, players, new ArrayList<>(), blitzMode, blitzMinutes,
        aiTime, useExptiminimax);
  }

  private static int readIntConfig(ConfigLoader config, String key, int fallback) {
    String rawValue = config.getOption(key, String.valueOf(fallback));
    try {
      return Integer.parseInt(rawValue.trim());
    } catch (NumberFormatException e) {
      return fallback;
    }
  }

  /**
   * Starts the game in GUI mode with the given configuration.
   * 
   * <p>Uses {@code players} if provided via {@code -p}, otherwise defaults to 2. 
   * If {@code blitzMode} is true, enables blitz mode with {@code blitzMinutes} per player.
   *
   * @param args the command-line arguments passed to JavaFX
   * @param players the number of players (between 2 and 4)
   * @param aiColors colors controlled by AI players
   * @param blitzMode true to enable blitz mode
   * @param blitzMinutes time limit per player in minutes (only used when blitzMode is true)
   * @param aiTime AI thinking time in seconds
   * @param useExptiminimax true to enable the Expectiminimax algorithm
   * @param useMl true to enable the Machine Learning agent
   * @param lang the dictionary language ("en" or "fr")
   * @param saveFilePath path to a save file to load, or null to start a new game
   */
  public static void launch(String[] args, int players, List<String> aiColors, boolean blitzMode,
      int blitzMinutes, int aiTime, boolean useExptiminimax, boolean useMl, String lang,
      String saveFilePath) {
    launch(args, GameMode.STANDARD, players, aiColors, blitzMode, blitzMinutes, aiTime,
        useExptiminimax, useMl, lang, saveFilePath);
  }

  /**
   * Starts the game in GUI mode with the given configuration and game mode.
   *
   * @param args the command-line arguments passed to JavaFX
   * @param gameMode the board mode (standard or super)
   * @param players the number of players (between 2 and 4)
   * @param aiColors colors controlled by AI players
   * @param blitzMode true to enable blitz mode
   * @param blitzMinutes time limit per player in minutes (only used when blitzMode is true)
   * @param aiTime AI thinking time in seconds
   * @param useExptiminimax true to enable the Expectiminimax algorithm
   * @param useMl true to enable the Machine Learning agent
   * @param lang the dictionary language ("en" or "fr")
   * @param saveFilePath path to a save file to load, or null to start a new game
   */
  public static void launch(String[] args, GameMode gameMode, int players, List<String> aiColors,
      boolean blitzMode, int blitzMinutes, int aiTime, boolean useExptiminimax, boolean useMl,
      String lang, String saveFilePath) {
    Game game;
    if (saveFilePath != null) {
      try {
        game = new GameLoader().loadGame(saveFilePath);
      } catch (Exception e) {
        throw new IllegalArgumentException("Could not load save file: " + saveFilePath, e);
      }
    } else {
      game = createConfiguredGame(gameMode, players, aiColors, blitzMode, blitzMinutes, aiTime,
          useExptiminimax);
    }

    JavaFxView view = new JavaFxView(game);
    ScrabbleGui.setGame(game);
    ScrabbleGui.setView(view);
    ScrabbleGui.setLanguage(lang);
    launchHandler.launch(ScrabbleGui.class, args);
  }

  /**
   * Starts the game in GUI mode without blitz mode.
   *
   * @param args the command-line arguments passed to JavaFX
   * @param players the number of players (0 = use default of 2)
   */
  public static void launch(String[] args, int players) {
    launch(args, players, List.of(), false, 30, 5, false, false, "en", null);
  }

  /**
   * Starts the game in GUI mode without loading a save file.
   *
   * @param args the command-line arguments passed to JavaFX
   * @param players the number of players (between 2 and 4)
   * @param aiColors colors controlled by AI players
   * @param blitzMode true to enable blitz mode
   * @param blitzMinutes time limit per player in minutes (only used when blitzMode is true)
   * @param aiTime AI thinking time in seconds
   * @param useExptiminimax true to enable the Expectiminimax algorithm
   * @param useMl true to enable the Machine Learning agent
   * @param lang the dictionary language ("en" or "fr")
   */
  public static void launch(String[] args, int players, List<String> aiColors, boolean blitzMode,
      int blitzMinutes, int aiTime, boolean useExptiminimax, boolean useMl, String lang) {
    launch(args, GameMode.STANDARD, players, aiColors, blitzMode, blitzMinutes, aiTime,
        useExptiminimax, useMl, lang, null);
  }

  /**
   * Starts the game in GUI mode without loading a save file, with an explicit mode.
   *
   * @param args the command-line arguments passed to JavaFX
   * @param gameMode the board mode (standard or super)
   * @param players the number of players (between 2 and 4)
   * @param aiColors colors controlled by AI players
   * @param blitzMode true to enable blitz mode
   * @param blitzMinutes time limit per player in minutes (only used when blitzMode is true)
   * @param aiTime AI thinking time in seconds
   * @param useExptiminimax true to enable the Expectiminimax algorithm
   * @param useMl true to enable the Machine Learning agent
   * @param lang the dictionary language ("en" or "fr")
   */
  public static void launch(String[] args, GameMode gameMode, int players,
      List<String> aiColors, boolean blitzMode, int blitzMinutes, int aiTime,
      boolean useExptiminimax, boolean useMl, String lang) {
    launch(args, gameMode, players, aiColors, blitzMode, blitzMinutes, aiTime,
        useExptiminimax, useMl, lang, null);
  }
}
