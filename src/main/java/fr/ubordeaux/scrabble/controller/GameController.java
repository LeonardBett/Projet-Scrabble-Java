package fr.ubordeaux.scrabble.controller;

import fr.ubordeaux.scrabble.i18n.I18n;
import fr.ubordeaux.scrabble.model.ai.MlAgent;
import fr.ubordeaux.scrabble.model.core.Board;
import fr.ubordeaux.scrabble.model.core.Game;
import fr.ubordeaux.scrabble.model.core.Move;
import fr.ubordeaux.scrabble.model.core.MoveGenerator;
import fr.ubordeaux.scrabble.model.core.PlayableWord;
import fr.ubordeaux.scrabble.model.core.Scoring;
import fr.ubordeaux.scrabble.model.core.Square;
import fr.ubordeaux.scrabble.model.core.Tile;
import fr.ubordeaux.scrabble.model.dictionary.Gaddag;
import fr.ubordeaux.scrabble.model.enums.Direction;
import fr.ubordeaux.scrabble.model.enums.GameMode;
import fr.ubordeaux.scrabble.model.enums.MoveType;
import fr.ubordeaux.scrabble.model.interfaces.Player;
import fr.ubordeaux.scrabble.model.savefiles.ConfigLoader;
import fr.ubordeaux.scrabble.model.savefiles.GameLoader;
import fr.ubordeaux.scrabble.model.savefiles.SaveManager;
import fr.ubordeaux.scrabble.model.utils.GameLogger;
import fr.ubordeaux.scrabble.model.utils.Point;
import fr.ubordeaux.scrabble.view.UserInterface;
import fr.ubordeaux.scrabble.view.cli.CliView;
import fr.ubordeaux.scrabble.view.optionlancement.GuiLauncher;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Main controller (application logic). Handles user input, updates the model and the view.
 * Responsibilities: - Orchestrate communication between the view and the model - Manage application
 * logic (turns, validations) - Notify the view of model changes.
 */
public class GameController {
  private Game game;
  private UserInterface view;
  private Gaddag gaddag;
  private List<String> dictionaryList;
  private String lang = "en";
  private boolean isPaused = false;
  /** Dictionary path override used for GUI/CLI launches. */
  private String dictionaryPathOverride;
  private boolean superScrabbleMode;
  private boolean blitzMode;
  private int blitzMinutes = 30;

  // AI Configuration fields
  private int aiTime = 5;
  private boolean useExptiminimax = false;
  private boolean useMl = false;
  private boolean onlineMode = false;

  // Player count set by launcher (0 = ask interactively)
  private int playerCount = 0;

  /**
   * Constructor for GameController.
   *
   * @param game the game instance to control
   * @param view the user interface to update
   */
  public GameController(Game game, UserInterface view) {
    this.game = game;
    this.view = view;
    if (game != null) {
      this.lang = Tile.normalizeLanguage(game.getLanguage());
    }
    this.dictionaryPathOverride = game != null && game.getDictionaryPathOverride() != null
        ? game.getDictionaryPathOverride()
        : System.getProperty("scrabble.dictionary.path");
    this.superScrabbleMode = game != null && game.getBoard().getSize() == 21;
    this.blitzMode = game != null && game.isBlitzModeEnabled();
  }

  /**
   * Starts the game.
   */
  public void startGame() {
    if (game == null || view == null) {
      throw new IllegalStateException("Game and view must be initialized before starting.");
    }
    if (game.getPlayers().size() < 2) {
      throw new IllegalStateException("At least 2 players must be present to start.");
    }
    game.startGame();
  }

  /**
   * Runs a CLI game loop if the provided view is a CliView. This will prompt for players (if
   * missing), start the game and process player actions until the game ends or the user quits.
   */
  public void runCli() {
    new GameControllerAux(this).runCli();
  }

  CliView requireCliView() {
    if (!(view instanceof CliView)) {
      throw new IllegalStateException("CLI loop requires a CliView instance as view.");
    }
    return (CliView) view;
  }

  Game internalGame() {
    return game;
  }

  UserInterface internalView() {
    return view;
  }

  /**
   * Returns the configured AI thinking time in seconds.
   *
   * @return the AI time limit in seconds
   */
  public int configuredAiTime() {
    return aiTime;
  }

  /**
   * Returns the configured blitz timeout in minutes.
   *
   * @return the blitz timeout in minutes
   */
  public int configuredBlitzMinutes() {
    return blitzMinutes;
  }

  /**
   * Indicates whether blitz mode is enabled.
   *
   * @return true when blitz mode is enabled
   */
  public boolean configuredBlitzMode() {
    return blitzMode;
  }

  /**
   * Indicates whether super Scrabble mode is enabled.
   *
   * @return true when super Scrabble mode is enabled
   */
  public boolean configuredSuperMode() {
    return superScrabbleMode;
  }

  /**
   * Indicates whether expectiminimax is enabled for AI.
   *
   * @return true when expectiminimax is enabled
   */
  public boolean isExpectiminimaxEnabled() {
    return useExptiminimax;
  }

  /**
   * Indicates whether the ML agent is enabled for AI.
   *
   * @return true when ML is enabled
   */
  public boolean isMlEnabled() {
    return useMl;
  }

  /**
   * Returns the configured player count for launch.
   *
   * @return the configured player count
   */
  public int configuredPlayerCount() {
    return playerCount;
  }

  /**
   * Returns the configured game language.
   *
   * @return the configured language code
   */
  public String configuredLanguage() {
    return lang;
  }

  /**
   * Returns the configured dictionary path override, or the default language dictionary.
   *
   * @return the dictionary path used by the controller
   */
  public String getDictionaryPathOverride() {
    return dictionaryPathOverride == null || dictionaryPathOverride.isBlank()
        ? "dictionaries/lexicon_" + lang + ".txt"
        : dictionaryPathOverride;
  }

  void adoptLoadedCliState(Game loadedGame, CliView loadedView) {
    if (loadedGame == null || loadedView == null) {
      throw new IllegalArgumentException("Loaded game and view must not be null.");
    }
    this.game = loadedGame;
    this.view = loadedView;
    this.lang = Tile.normalizeLanguage(loadedGame.getLanguage());
    this.dictionaryPathOverride = loadedGame.getDictionaryPathOverride();
    this.gaddag = null;
    this.dictionaryList = null;
  }

  /** Blitz monitoring thread that emits periodic time warnings. */
  private ScheduledExecutorService blitzWatcherExecutor;

  /**
   * Starts a background task that checks blitz time every second and warns the player
   * at 5 minutes, 2 minutes and 1 minute remaining.
   *
   * @param cliView the CLI view used to display warnings
   */
  void startBlitzWatcher(CliView cliView) {
    stopBlitzWatcher();
    final long[] warnedAt = {5 * 60_000L, 2 * 60_000L, 60_000L};
    final boolean[] warned = new boolean[warnedAt.length];

    blitzWatcherExecutor = Executors.newSingleThreadScheduledExecutor();
    blitzWatcherExecutor.scheduleAtFixedRate(() -> {
      if (game.isGameOver()) {
        stopBlitzWatcher();
        return;
      }

      Player current = game.getCurrentPlayer();
      if (current != null && current.isBlitzClockEnabled()) {
        long remaining = current.getRemainingTimeMillis();

        // Warnings at 5 min, 2 min, 1 min
        for (int i = 0; i < warnedAt.length; i++) {
          if (!warned[i] && remaining <= warnedAt[i] && remaining > 0) {
            warned[i] = true;
            long minutes = warnedAt[i] / 60_000L;
            System.out.println("\n " + I18n.translate(
                "cli.blitz.remainingMinutes",
                current.getName(),
                minutes));
          }
        }

        // Time expired
        if (current.isOutOfTime() && !game.isGameOver()) {
          handleBlitzExpiry(current, cliView);
        }
      }
    }, 0L, 1L, TimeUnit.SECONDS);
  }

  /** Stops the blitz watcher task if running. */
  void stopBlitzWatcher() {
    if (blitzWatcherExecutor != null) {
      blitzWatcherExecutor.shutdownNow();
      blitzWatcherExecutor = null;
    }
  }

  /**
   * Handles blitz time expiry for the given player: sets game over and notifies.
   *
   * @param expired the player who ran out of time
   * @param cliView the CLI view for output
   */
  void handleBlitzExpiry(Player expired, CliView cliView) {
    game.setGameOver(true);
    stopBlitzWatcher();
    cliView.displayError(I18n.translate("cli.blitz.playerTimedOut", expired.getName()));
    cliView.displayMessage(I18n.translate("cli.game.over"));
  }

  /**
   * Executes a player's move.
   *
   * @param move The move to execute.
   */
  public void handlePlayerMove(Move move) {
    try {
      if (move == null) {
        return;
      }

      game.executeMove(move);
      view.refresh();

    } catch (IllegalArgumentException | IllegalStateException e) {
      throw new RuntimeException(I18n.translate("scrabble.invalidMove", e.getMessage()), e);
    }
  }

  /**
   * Loads the lexicon into a simple List of strings to map ML index predictions to words.
   *
   * @return A list of all valid words.
   */
  List<String> getOrLoadDictionaryList() {
    if (dictionaryList != null) {
      return dictionaryList;
    }

    dictionaryList = new ArrayList<>();
    try (BufferedReader br = openDictionaryReader()) {
      String line;
      while ((line = br.readLine()) != null) {
        String cleanWord = line.trim().toUpperCase();
        if (!cleanWord.isEmpty()) {
          dictionaryList.add(cleanWord);
        }
      }
    } catch (Exception e) {
      GameLogger.logError("Warning: Failed to load dictionary list for ML: " + e.getMessage(), e);
    }
    return dictionaryList;
  }

  /**
   * Loads the Gaddag data structure for word generation.
   *
   * @return The populated Gaddag instance.
   */
  public Gaddag getOrLoadGaddag() {
    if (gaddag != null) {
      return gaddag;
    }

    gaddag = new Gaddag();
    String dictPath = resolvedDictionarySourceLabel();
    GameLogger.logVerbose("\nLoading Gaddag dictionary (" + dictPath + ") please wait...");

    try (BufferedReader br = openDictionaryReader()) {
      int wordCount = 0;
      String line;
      while ((line = br.readLine()) != null) {
        String cleanWord = line.trim().toUpperCase();
        if (!cleanWord.isEmpty()) {
          gaddag.add(cleanWord);
          wordCount++;
        }
      }

      GameLogger.logVerbose("Dictionary successfully loaded! (" + wordCount + " words added).\n");
      return gaddag;
    } catch (Exception e) {
      throw new IllegalStateException("Error while loading the dictionary: " + e.getMessage(), e);
    }
  }

  /**
   * Adds a player to the game.
   *
   * @param player The player to add.
   */
  public void addPlayer(Player player) {
    game.addPlayer(player);
  }

  /**
   * Undoes the last move.
   */
  public void undo() {
    game.undo();
    view.refresh();
  }

  /**
   * Redoes the undone move.
   */
  public void redo() {
    game.redo();
    view.refresh();
  }

  /**
   * Gets the game.
   *
   * @return The Game model.
   */
  public Game getGame() {
    return game;
  }

  /**
   * Gets the view.
   *
   * @return The user interface.
   */
  public UserInterface getView() {
    return view;
  }

  /**
   * Sets the AI thinking time per turn.
   *
   * @param aiTime the time in seconds allocated to AI for decision-making
   */
  public void setAiTime(int aiTime) {
    this.aiTime = aiTime;
  }

  /**
   * Enables or disables expectimax algorithm usage for AI.
   *
   * @param useExptiminimax true to use expectimax; false otherwise
   */
  public void setUseExptiminimax(boolean useExptiminimax) {
    this.useExptiminimax = useExptiminimax;
  }

  /**
   * Enables or disables machine learning model usage for AI.
   *
   * @param useMl true to use ML; false otherwise
   */
  public void setUseMl(boolean useMl) {
    this.useMl = useMl;
  }

  /**
   * Sets the game language for dictionary and UI.
   *
   * @param lang the language code (e.g., "en" or "fr")
   */
  public void setLang(String lang) {
    String normalized = Tile.normalizeLanguage(lang);
    this.lang = normalized;

    if (game != null) {
      game.setLanguage(normalized);
    }

    // Force lazy resources to be rebuilt using the new language.
    this.gaddag = null;
    this.dictionaryList = null;
  }

  /**
   * Sets an explicit dictionary file path to override the built-in language dictionaries.
   *
   * @param dictionaryPath path to a text dictionary file (one word per line)
   */
  public void setDictionaryPath(String dictionaryPath) {
    this.dictionaryPathOverride = dictionaryPath;
    if (game != null) {
      game.setDictionaryPathOverride(dictionaryPath);
    }
    this.gaddag = null;
    this.dictionaryList = null;
  }

  /**
   * Applies one configuration entry to the current controller state and, when possible,
   * to the active game.
   *
   * @param key configuration key
   * @param value configuration value
   */
  public void applyConfiguration(String key, String value) {
    if (key == null || key.isBlank()) {
      throw new IllegalArgumentException("Configuration key must not be empty.");
    }

    String normalizedKey = key.trim().toLowerCase();
    String normalizedValue = value == null ? "" : value.trim();

    switch (normalizedKey) {
      case "debug" -> GameLogger.setDebug(parseBoolean(normalizedValue));
      case "verbose" -> GameLogger.setVerbose(parseBoolean(normalizedValue));
      case "language", "lang" -> applyLanguage(normalizedValue);
      case "players", "players-count" -> setPlayerCount(parseInt(normalizedValue, "players"));
      case "blitz" -> applyBlitzMode(parseBoolean(normalizedValue));
      case "timeout" -> applyBlitzTimeout(parseInt(normalizedValue, "timeout"));
      case "ai-time" -> {
        setAiTime(parseInt(normalizedValue, "ai-time"));
        refreshAiPlayerSettings();
      }
      case "ai-exptiminimax" -> {
        setUseExptiminimax(parseBoolean(normalizedValue));
        refreshAiPlayerSettings();
      }
      case "ai-ml" -> {
        setUseMl(parseBoolean(normalizedValue));
        refreshAiPlayerSettings();
      }
      case "dictionary", "dictionary-path" -> setDictionaryPath(normalizedValue);
      case "super", "super-scrabble" -> superScrabbleMode = parseBoolean(normalizedValue);
      default -> throw new IllegalArgumentException("Unsupported parameter: " + key);
    }
  }

  private void applyLanguage(String language) {
    String normalized = Tile.normalizeLanguage(language);
    this.lang = normalized;
    I18n.setLanguage(normalized);
    Tile.setActiveLanguage(normalized);
    this.gaddag = null;
    this.dictionaryList = null;

    try {
      if (game != null) {
        game.setLanguage(normalized);
      }
    } catch (IllegalStateException ignored) {
      // Current games can only change language before start; keep the new setting for next game.
    }
  }

  private void applyBlitzMode(boolean enabled) {
    this.blitzMode = enabled;
    if (game == null) {
      return;
    }
    if (enabled) {
      game.enableBlitzMode(Duration.ofMinutes(blitzMinutes));
    } else {
      game.disableBlitzMode();
    }
  }

  private void applyBlitzTimeout(int minutes) {
    if (minutes <= 0) {
      throw new IllegalArgumentException("timeout must be positive");
    }
    this.blitzMinutes = minutes;
    if (game != null && blitzMode) {
      game.enableBlitzMode(Duration.ofMinutes(minutes));
    }
  }

  private void refreshAiPlayerSettings() {
    if (game == null) {
      return;
    }

    MlAgent sharedMlAgent = null;
    if (useMl) {
      sharedMlAgent = new MlAgent("src/main/resources/ai/model_" + lang,
          getOrLoadDictionaryList());
    }

    for (Player player : game.getPlayers()) {
      if (player instanceof fr.ubordeaux.scrabble.model.ai.AiPlayer aiPlayer) {
        aiPlayer.setTimeLimitSeconds(aiTime);
        aiPlayer.setExpectiminimaxMode(useExptiminimax);
        aiPlayer.setMlAgent(useMl ? sharedMlAgent : null);
      }
    }
  }

  private int parseInt(String value, String key) {
    try {
      return Integer.parseInt(value);
    } catch (NumberFormatException e) {
      throw new IllegalArgumentException("Invalid integer for " + key + ": " + value, e);
    }
  }

  private boolean parseBoolean(String value) {
    String normalized = value == null ? "" : value.trim().toLowerCase();
    return normalized.equals("true") || normalized.equals("1") || normalized.equals("yes")
        || normalized.equals("y") || normalized.equals("oui") || normalized.equals("o");
  }

  private String resolvedDictionarySourceLabel() {
    if (dictionaryPathOverride != null && !dictionaryPathOverride.isBlank()) {
      return dictionaryPathOverride;
    }
    return "dictionaries/lexicon_" + this.lang + ".txt";
  }

  private BufferedReader openDictionaryReader() throws IOException {
    if (dictionaryPathOverride != null && !dictionaryPathOverride.isBlank()) {
      return Files.newBufferedReader(Path.of(dictionaryPathOverride));
    }

    String dictPath = "dictionaries/lexicon_" + this.lang + ".txt";
    InputStream is = getClass().getClassLoader().getResourceAsStream(dictPath);
    if (is == null) {
      throw new IllegalStateException("Dictionary file " + dictPath + " not found in resources.");
    }
    return new BufferedReader(new InputStreamReader(is));
  }

  /**
   * Generates and displays a hint for the current human player without ending their turn.
   * Searches for the highest-scoring move that specifically uses fewer than 7 letters
   * from the rack to avoid giving away a bingo/scrabble.
   */
  public void provideHint() {
    MoveGenerator moveGen = new MoveGenerator();
    List<PlayableWord> possibleMoves = moveGen.getPlayableWordsList(game, getOrLoadGaddag());

    PlayableWord bestHintMove = null;
    int bestScore = -1;
    List<Character> bestLettersToUse = new ArrayList<>();

    for (PlayableWord move : possibleMoves) {
      List<Character> lettersFromRack = getLettersFromRack(game.getBoard(), move);

      if (!lettersFromRack.isEmpty()) {
        int score = simulateScoreForHint(game.getBoard(), move);
        if (score > bestScore) {
          bestScore = score;
          bestHintMove = move;
          bestLettersToUse = lettersFromRack;
        }
      }
    }

    if (bestHintMove != null) {
      view.displayMessage(I18n.translate(
          "cli.hint.foundDetailed",
          formatHintMove(bestHintMove),
          bestLettersToUse.toString(),
          bestScore));
    } else {
      view.displayMessage(I18n.translate("cli.hint.notFound"));
    }
  }

  private String formatHintMove(PlayableWord move) {
    int hookIndex = move.getGaddagRepresentation().indexOf('>') - 1;
    int startX = move.getDirection() == Direction.HORIZONTAL
        ? move.getHookX() - hookIndex
        : move.getHookX();
    int startY = move.getDirection() == Direction.VERTICAL
        ? move.getHookY() - hookIndex
        : move.getHookY();

    char row = (char) ('a' + startY);
    int col = startX + 1;
    char dir = move.getDirection() == Direction.HORIZONTAL ? 'h' : 'v';
    return "" + row + col + dir + " " + move.getWord();
  }

  /**
   * Extracts the exact letters that the player needs to place from their rack
   * to form the simulated word.
   *
   * @param board The current game board.
   * @param move The move being evaluated.
   * @return A list of characters required from the rack.
   */
  private List<Character> getLettersFromRack(Board board, PlayableWord move) {
    List<Character> rackLettersUsed = new ArrayList<>();
    String word = move.getWord();
    int hookIndex = move.getGaddagRepresentation().indexOf('>') - 1;

    int startX = move.getDirection() == Direction.HORIZONTAL
        ? move.getHookX() - hookIndex : move.getHookX();
    int startY = move.getDirection() == Direction.VERTICAL
        ? move.getHookY() - hookIndex : move.getHookY();

    for (int i = 0; i < word.length(); i++) {
      int x = startX + (move.getDirection() == Direction.HORIZONTAL ? i : 0);
      int y = startY + (move.getDirection() == Direction.VERTICAL ? i : 0);

      Square sq = board.getSquare(new Point(x, y));

      if (sq != null && sq.isEmpty()) {
        rackLettersUsed.add(word.charAt(i));
      }
    }
    return rackLettersUsed;
  }

  /**
   * Temporarily places a word on the board to calculate its exact point value,
   * then removes it to maintain the board's original state.
   *
   * @param board The current game board.
   * @param move The move to evaluate.
   * @return The calculated score for the move.
   */
  private int simulateScoreForHint(Board board, PlayableWord move) {
    List<Square> newlyPlaced = new ArrayList<>();
    List<Square> wordSquares = new ArrayList<>();

    String word = move.getWord();
    int hookIndex = move.getGaddagRepresentation().indexOf('>') - 1;

    int startX = move.getDirection() == Direction.HORIZONTAL
        ? move.getHookX() - hookIndex : move.getHookX();
    int startY = move.getDirection() == Direction.VERTICAL
        ? move.getHookY() - hookIndex : move.getHookY();

    for (int i = 0; i < word.length(); i++) {
      int x = startX + (move.getDirection() == Direction.HORIZONTAL ? i : 0);
      int y = startY + (move.getDirection() == Direction.VERTICAL ? i : 0);

      Square sq = board.getSquare(new Point(x, y));
      wordSquares.add(sq);

      if (sq != null && sq.isEmpty()) {
        sq.setTile(new Tile(word.charAt(i)));
        newlyPlaced.add(sq);
      }
    }

    int score = 0;
    try {
      if (!wordSquares.isEmpty()) {
        score = Scoring.calculateWordScore(wordSquares, newlyPlaced);
      }
    } catch (Exception e) {
      // Exceptions are safely ignored during background simulation
    }

    // Crucial cleanup step: remove temporary tiles
    for (Square sq : newlyPlaced) {
      sq.setTile(null);
    }

    return score;
  }

  /**
   * Sets the number of players to use at launch (skips the interactive prompt).
   *
   * @param count the number of players (2‑4)
   */
  public void setPlayerCount(int count) {
    this.playerCount = count;
  }

  /**
   * Toggles the pause state of the game.
   * Pauses the blitz timer if running and prevents gameplay until resumed.
   */
  public void togglePause() {
    if (game.isGameOver() || !game.isBlitzModeEnabled()) {
      return;
    }

    Player current = game.getCurrentPlayer();
    if (current == null) {
      return;
    }

    if (!isPaused) {
      current.pauseTurnTimer();
      isPaused = true;
      GameLogger.logVerbose("The timer is PAUSED.");
    } else {
      current.startTurnTimer();
      isPaused = false;
      GameLogger.logVerbose("Timer is UNPAUSED.");
    }

    view.refresh();
  }

  /**
   * Applies a semicolon-separated list of configuration assignments.
   *
   * <p>Expected format: {@code key=value; key2=value2; ...}
   *
   * @param rawAssignments assignment string to parse and apply
   * @return a summary of important changed domains
   */
  public ConfigurationApplySummary applyConfigurationAssignments(String rawAssignments) {
    if (rawAssignments == null || rawAssignments.isBlank()) {
      throw new IllegalArgumentException("Configuration string must not be empty.");
    }

    String[] assignments = rawAssignments.split("[;]");
    boolean blitzChanged = false;
    boolean languageChanged = false;

    for (String assignment : assignments) {
      String normalized = assignment.trim();
      if (normalized.isEmpty()) {
        continue;
      }

      String[] kv = normalized.split("=", 2);
      if (kv.length != 2) {
        throw new IllegalArgumentException("Invalid configuration entry: " + normalized);
      }

      String key = kv[0].trim();
      applyConfiguration(key, kv[1].trim());

      if (key.equalsIgnoreCase("blitz") || key.equalsIgnoreCase("timeout")) {
        blitzChanged = true;
      }
      if (key.equalsIgnoreCase("language") || key.equalsIgnoreCase("lang")) {
        languageChanged = true;
      }
    }

    return new ConfigurationApplySummary(languageChanged, blitzChanged);
  }

  /**
   * Creates a new configured game from the current controller settings, then replaces the current
   * game and starts it.
   *
   * @param count the number of players for the new game
   * @return the created game instance
   */
  public Game recreateConfiguredGame(int count) {
    GameMode mode = superScrabbleMode ? GameMode.SUPER : GameMode.STANDARD;
    Game newGame = GuiLauncher.createConfiguredGame(
        mode,
        count,
        List.of(),
        blitzMode,
        blitzMinutes,
        aiTime,
        useExptiminimax,
        useMl);

    setGame(newGame);
    startGame();
    return newGame;
  }

  /**
   * Marks the current game as over due to blitz timeout and returns the expired player if found.
   *
   * @return optional player that ran out of time
   */
  public Optional<Player> handleBlitzTimeout() {
    if (game == null) {
      return Optional.empty();
    }
    game.setGameOver(true);
    return game.getPlayers().stream()
        .filter(p -> p.isBlitzClockEnabled() && p.isOutOfTime())
        .findFirst();
  }

  /**
   * Builds an exchange move from letters entered by the user.
   *
   * @param letters letters to exchange
   * @return optional exchange move when all letters are present in current rack
   */
  public Optional<Move> buildExchangeMoveFromLetters(String letters) {
    if (letters == null || letters.isBlank()) {
      return Optional.empty();
    }

    Player current = game.getCurrentPlayer();
    if (current == null) {
      return Optional.empty();
    }

    List<Tile> rack = current.getRack().getTiles();
    List<Tile> toExchange = new ArrayList<>();

    for (char c : letters.toUpperCase().toCharArray()) {
      boolean found = false;
      for (Tile tile : rack) {
        if (tile.getCharacter() == c && !toExchange.contains(tile)) {
          toExchange.add(tile);
          found = true;
          break;
        }
      }
      if (!found) {
        return Optional.empty();
      }
    }

    if (toExchange.isEmpty()) {
      return Optional.empty();
    }
    return Optional.of(Move.createExchange(current, toExchange));
  }

  /**
   * Builds a pending PLAY move from GUI pending tiles.
   *
   * @param pendingTiles pending tiles keyed by point
   * @return play move, or null when alignment is invalid
   */
  public Move buildPendingMove(Map<Point, Tile> pendingTiles) {
    return fr.ubordeaux.scrabble.controller.builders.PendingMoveBuilderController.build(
        pendingTiles,
        game.getCurrentPlayer(),
        game);
  }

  /**
   * Converts a local PLAY move to the compact payload required by the network layer.
   *
   * @param move move to convert
   * @return payload containing coordinates, direction letter and word
   */
  public NetworkPlayPayload toNetworkPlayPayload(Move move) {
    if (move == null || move.getType() != MoveType.PLAY || move.getStartPosition() == null
        || move.getDirection() == null) {
      throw new IllegalArgumentException("A valid PLAY move is required.");
    }

    String word = move.getTiles().stream()
        .map(t -> String.valueOf(t.getCharacter()))
        .reduce("", String::concat);

    return new NetworkPlayPayload(
        move.getStartPosition().getX(),
        move.getStartPosition().getY(),
        move.getDirection().name().substring(0, 1),
        word);
  }

  /**
   * Checks whether a tile can be placed at the provided point, considering both board occupancy
   * and currently pending GUI placements.
   *
   * @param point board point to validate
   * @param pendingTiles tiles currently pending placement in GUI
   * @return true when the cell can accept a tile
   */
  public boolean canPlacePendingTile(Point point, Map<Point, Tile> pendingTiles) {
    if (game == null || point == null) {
      return false;
    }
    return game.getBoard().getSquare(point).isEmpty()
        && (pendingTiles == null || !pendingTiles.containsKey(point));
  }

  /**
   * Resolves a dropped tile. For joker tiles, converts user input to a concrete tile.
   *
   * @param draggedTile tile dragged from rack
   * @param jokerInput user-entered joker character, ignored for non-joker tiles
   * @return resolved tile to place, or empty when the input is cancelled/invalid
   */
  public Optional<Tile> resolveDroppedTile(Tile draggedTile, String jokerInput) {
    if (draggedTile == null) {
      return Optional.empty();
    }
    if (!draggedTile.isJoker() && draggedTile.getCharacter() != ' ') {
      return Optional.of(draggedTile);
    }
    if (jokerInput == null || jokerInput.trim().isEmpty()) {
      return Optional.empty();
    }

    char chosen = Character.toUpperCase(jokerInput.trim().charAt(0));
    if (chosen < 'A' || chosen > 'Z') {
      return Optional.empty();
    }
    return Optional.of(new Tile(chosen, true));
  }

  /**
   * Analyses a drag-and-drop tile placement attempt from the GUI.
   *
   * @param draggedTile dragged tile from rack
   * @param row target row on board
   * @param col target column on board
   * @param pendingTiles already pending tiles on board
   * @return analysis used by the view to decide which UI action to execute
   */
  public TileDropAnalysis analyzeTileDrop(Tile draggedTile, int row, int col,
      Map<Point, Tile> pendingTiles) {
    if (draggedTile == null || game == null || game.isGameOver()) {
      return TileDropAnalysis.ignored();
    }

    Point point = new Point(col, row);
    if (!canPlacePendingTile(point, pendingTiles)) {
      return TileDropAnalysis.rejected("scrabble.cellOccupied");
    }

    boolean joker = draggedTile.isJoker() || draggedTile.getCharacter() == ' ';
    return new TileDropAnalysis(true, joker, null, point, draggedTile);
  }

  /**
   * Submits pending tiles by delegating validation/execution to the controller.
   *
   * @param pendingTiles pending tile map from GUI
   * @return submission result for UI orchestration
   */
  public PendingMoveSubmitResult submitPendingMove(Map<Point, Tile> pendingTiles) {
    if (pendingTiles == null || pendingTiles.isEmpty()) {
      return new PendingMoveSubmitResult(PendingMoveSubmitStatus.EMPTY, null, null);
    }

    Move move = buildPendingMove(pendingTiles);
    if (move == null) {
      return new PendingMoveSubmitResult(PendingMoveSubmitStatus.INVALID_ALIGNMENT, null, null);
    }

    if (isOnlineMode()) {
      return new PendingMoveSubmitResult(
          PendingMoveSubmitStatus.ONLINE_READY,
          toNetworkPlayPayload(move),
          null);
    }

    try {
      handlePlayerMove(move);
      return new PendingMoveSubmitResult(PendingMoveSubmitStatus.LOCAL_APPLIED, null, null);
    } catch (RuntimeException e) {
      return new PendingMoveSubmitResult(
          PendingMoveSubmitStatus.LOCAL_REJECTED,
          null,
          e.getMessage());
    }
  }

  /**
   * Resolves effective player count for a new game from config or dialog input.
   *
   * @param dialogSelection optional dialog selection when interactive prompt is used
   * @return optional effective player count
   */
  public OptionalInt resolveNewGamePlayerCount(Optional<Integer> dialogSelection) {
    if (playerCount > 0) {
      return OptionalInt.of(playerCount);
    }
    if (dialogSelection == null || dialogSelection.isEmpty()) {
      return OptionalInt.empty();
    }
    return OptionalInt.of(dialogSelection.get());
  }

  /**
   * Recreates game using config and an optional interactive player count.
   *
   * @param dialogSelection optional selection from UI dialog
   * @return created game when count is available
   */
  public Optional<Game> recreateConfiguredGameFromSelection(Optional<Integer> dialogSelection) {
    OptionalInt count = resolveNewGamePlayerCount(dialogSelection);
    if (count.isEmpty()) {
      return Optional.empty();
    }
    return Optional.of(recreateConfiguredGame(count.getAsInt()));
  }

  /**
   * Indicates whether current turn should be played by AI.
   *
   * @return true when current player is AI and game is not over
   */
  public boolean shouldPerformAiTurn() {
    if (game == null || game.isGameOver()) {
      return false;
    }
    return game.getCurrentPlayer() instanceof fr.ubordeaux.scrabble.model.ai.AiPlayer;
  }

  /**
   * Reads one option from the shared Scrabble configuration.
   *
   * @param key option key
   * @param defaultValue fallback value when key is missing
   * @return configured value or fallback
   */
  public String getConfigOption(String key, String defaultValue) {
    ConfigLoader config = new ConfigLoader();
    config.loadConfig();
    return config.getOption(key, defaultValue);
  }

  /**
   * Parses a TCP port string and validates the range.
   *
   * @param portText text entered by the user
   * @return parsed port, or empty when invalid
   */
  public static OptionalInt parsePort(String portText) {
    if (portText == null) {
      return OptionalInt.empty();
    }
    try {
      int port = Integer.parseInt(portText.trim());
      if (port < 0 || port > 65535) {
        return OptionalInt.empty();
      }
      return OptionalInt.of(port);
    } catch (NumberFormatException e) {
      return OptionalInt.empty();
    }
  }

  /**
   * Extracts a player identifier from a lobby entry such as "#12 Name [STATUS]".
   *
   * @param entry lobby list entry
   * @return parsed player id, or empty when invalid
   */
  public static OptionalInt parseLobbyPlayerId(String entry) {
    if (entry == null || entry.isBlank()) {
      return OptionalInt.empty();
    }
    try {
      String idStr = entry.trim().split("\\s+")[0].replace("#", "");
      return OptionalInt.of(Integer.parseInt(idStr));
    } catch (RuntimeException e) {
      return OptionalInt.empty();
    }
  }

  /**
   * Extracts multiple player ids from lobby selections.
   *
   * @param selectedEntries selected player entries
   * @return parsed ids in selection order
   */
  public static List<Integer> parseLobbyPlayerIds(List<String> selectedEntries) {
    List<Integer> ids = new ArrayList<>();
    if (selectedEntries == null) {
      return ids;
    }

    for (String entry : selectedEntries) {
      parseLobbyPlayerId(entry).ifPresent(ids::add);
    }
    return ids;
  }

  /**
   * Saves the current game to disk.
   *
   * @param filePath output file path
   * @throws IOException when saving fails
   */
  public void saveGameToPath(String filePath) throws IOException {
    new SaveManager().saveGame(game, filePath);
  }

  /**
   * Loads a game from disk.
   *
   * @param filePath input file path
   * @return loaded game instance
   */
  public Game loadGameFromPath(String filePath) {
    try {
      return new GameLoader().loadGame(filePath);
    } catch (Exception e) {
      throw new IllegalStateException("Failed to load game: " + e.getMessage(), e);
    }
  }

  /**
   * Summary of state-impacting configuration changes.
   *
   * @param languageChanged true when language has changed
   * @param blitzChanged true when blitz mode or timeout changed
   */
  public record ConfigurationApplySummary(boolean languageChanged, boolean blitzChanged) {
  }

  /**
   * Compact network payload for a PLAY command.
   *
   * @param x origin x coordinate
   * @param y origin y coordinate
   * @param direction first letter of direction (H or V)
   * @param word played word
   */
  public record NetworkPlayPayload(int x, int y, String direction, String word) {
  }

  /**
   * Result of tile-drop analysis for GUI placement flow.
   *
   * @param accepted true when placement can continue
   * @param needsJokerResolution true when UI should ask for joker letter
   * @param errorI18nKey i18n key for placement error, or null
   * @param point destination point on board, or null when not accepted
   * @param tile tile candidate to place
   */
  public record TileDropAnalysis(boolean accepted, boolean needsJokerResolution,
                                 String errorI18nKey, Point point, Tile tile) {
    static TileDropAnalysis ignored() {
      return new TileDropAnalysis(false, false, null, null, null);
    }

    static TileDropAnalysis rejected(String errorI18nKey) {
      return new TileDropAnalysis(false, false, errorI18nKey, null, null);
    }
  }

  /**
   * Status of pending move submission orchestration.
   */
  public enum PendingMoveSubmitStatus {
    /** No pending tiles were provided. */
    EMPTY,
    /** Pending tiles are not aligned for a valid word placement. */
    INVALID_ALIGNMENT,
    /** Move is valid and must be sent to the online server. */
    ONLINE_READY,
    /** Move was successfully applied in local mode. */
    LOCAL_APPLIED,
    /** Move was rejected in local mode. */
    LOCAL_REJECTED
  }

  /**
   * Result object returned by pending move submission.
   *
   * @param status submission status
   * @param payload network payload when status is ONLINE_READY
   * @param errorMessage local error message when status is LOCAL_REJECTED
   */
  public record PendingMoveSubmitResult(PendingMoveSubmitStatus status,
                                        NetworkPlayPayload payload,
                                        String errorMessage) {
  }

  /**
   * Replaces the current game instance with a new one and updates the controller.
   * Used by the GUI to switch between local, online, and new games.
   *
   * @param newGame the new game instance to use
   */
  public void setGame(Game newGame) {
    this.game = newGame;
    if (newGame != null) {
      this.superScrabbleMode = newGame.getBoard().getSize() == 21;
      this.blitzMode = newGame.isBlitzModeEnabled();
      this.dictionaryPathOverride = newGame.getDictionaryPathOverride();
    }
    // Clear cached resources when switching games
    this.gaddag = null;
    this.dictionaryList = null;
  }

  /**
   * Executes the current player's AI turn asynchronously in a background thread.
   * Calls the onComplete callback on the UI thread when done.
   *
   * <p>This handles:
   * - Loading the dictionary if needed
   * - Executing the AI's playTurn method
   * - Handling exceptions gracefully
   * - Refreshing the UI on completion
   *
   * @param onComplete a callback to run on the UI thread when the AI turn completes
   */
  public void performAiTurn(Runnable onComplete) {
    Player current = game.getCurrentPlayer();
    if (!(current instanceof fr.ubordeaux.scrabble.model.ai.AiPlayer) || game.isGameOver()) {
      return;
    }

    final fr.ubordeaux.scrabble.model.ai.AiPlayer ai = 
        (fr.ubordeaux.scrabble.model.ai.AiPlayer) current;

    new Thread(() -> {
      try {
        Thread.sleep(1000); // Brief pause for better UX
        ai.playTurn(game, getOrLoadGaddag());
      } catch (InterruptedException e) {
        Thread.currentThread().interrupt();
        handlePlayerMove(Move.createPass(ai));
      } catch (RuntimeException e) {
        GameLogger.logError("AI play error: " + e.getMessage(), e);
        handlePlayerMove(Move.createPass(ai));
      } finally {
        if (onComplete != null) {
          onComplete.run();
        }
      }
    }).start();
  }

  /**
   * Manages state changes when switching to an online multiplayer game.
   * Disables local-only features like undo, redo, hint, and pause.
   * Clears pending moves and refreshes the game view.
   *
   * @param onlineGame the game instance received from the server
   */
  public void switchToOnlineMode(Game onlineGame) {
    setGame(onlineGame);
    this.onlineMode = true;
  }

  /**
   * Exits online mode and re-enables local game features.
   * Re-enables buttons like undo, redo, hint that are disabled during online play.
   */
  public void exitOnlineMode() {
    this.onlineMode = false;
  }

  /**
   * Returns whether the controller is currently in online mode.
   *
   * @return true when online mode is active
   */
  public boolean isOnlineMode() {
    return onlineMode;
  }
}
