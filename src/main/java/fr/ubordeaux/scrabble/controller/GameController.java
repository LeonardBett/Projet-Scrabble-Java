package fr.ubordeaux.scrabble.controller;

import fr.ubordeaux.scrabble.i18n.I18n;
import fr.ubordeaux.scrabble.model.ai.MlAgent;
import fr.ubordeaux.scrabble.model.core.Board;
import fr.ubordeaux.scrabble.model.core.Game;
import fr.ubordeaux.scrabble.model.core.Move;
import fr.ubordeaux.scrabble.model.core.MoveGenerator;
import fr.ubordeaux.scrabble.model.core.MoveHandler;
import fr.ubordeaux.scrabble.model.core.PlayableWord;
import fr.ubordeaux.scrabble.model.core.Scoring;
import fr.ubordeaux.scrabble.model.core.Square;
import fr.ubordeaux.scrabble.model.core.Tile;
import fr.ubordeaux.scrabble.model.dictionary.Gaddag;
import fr.ubordeaux.scrabble.model.enums.Direction;
import fr.ubordeaux.scrabble.model.enums.GameMode;
import fr.ubordeaux.scrabble.model.enums.MoveType;
import fr.ubordeaux.scrabble.model.enums.PlayerColor;
import fr.ubordeaux.scrabble.model.interfaces.Player;
import fr.ubordeaux.scrabble.model.utils.GameLogger;
import fr.ubordeaux.scrabble.model.utils.Point;
import fr.ubordeaux.scrabble.view.UserInterface;
import fr.ubordeaux.scrabble.view.cli.CliView;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

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
  private String dictionaryPathOverride;
  private boolean superScrabbleMode;
  private boolean blitzMode;
  private int blitzMinutes = 30;

  // AI Configuration fields
  private int aiTime = 5;
  private boolean useExptiminimax = false;
  private boolean useMl = false;

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
    this.dictionaryPathOverride = System.getProperty("scrabble.dictionary.path");
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

  public int configuredAiTime() {
    return aiTime;
  }

  public int configuredBlitzMinutes() {
    return blitzMinutes;
  }

  public boolean configuredBlitzMode() {
    return blitzMode;
  }

  public boolean configuredSuperMode() {
    return superScrabbleMode;
  }

  public boolean isExpectiminimaxEnabled() {
    return useExptiminimax;
  }

  public boolean isMlEnabled() {
    return useMl;
  }

  public int configuredPlayerCount() {
    return playerCount;
  }

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
    this.gaddag = null;
    this.dictionaryList = null;
  }

  /** Thread de surveillance blitz — affiche un avertissement toutes les minutes. */
  private volatile Thread blitzWatcherThread;

  /**
   * Starts a background thread that checks blitz time every second and warns the player
   * at 5 minutes, 2 minutes and 1 minute remaining.
   *
   * @param cliView the CLI view used to display warnings
   */
  void startBlitzWatcher(CliView cliView) {
    blitzWatcherThread = new Thread(() -> {
      final long[] warnedAt = {5 * 60_000L, 2 * 60_000L, 60_000L};
      boolean[] warned = new boolean[warnedAt.length];

      while (!Thread.currentThread().isInterrupted() && !game.isGameOver()) {
        Player current = game.getCurrentPlayer();
        if (current != null && current.isBlitzClockEnabled()) {
          long remaining = current.getRemainingTimeMillis();

          // Avertissements à 5 min, 2 min, 1 min
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

          // Temps expiré
          if (current.isOutOfTime() && !game.isGameOver()) {
            handleBlitzExpiry(current, cliView);
            break;
          }

          // Réinitialiser les avertissements au changement de joueur
          Player newCurrent = game.getCurrentPlayer();
          if (newCurrent != current) {
            warned = new boolean[warnedAt.length];
          }
        }

        try {
          Thread.sleep(1000);
        } catch (InterruptedException e) {
          Thread.currentThread().interrupt();
        }
      }
    });
    blitzWatcherThread.setDaemon(true);
    blitzWatcherThread.start();
  }

  /** Stops the blitz watcher thread if running. */
  void stopBlitzWatcher() {
    if (blitzWatcherThread != null) {
      blitzWatcherThread.interrupt();
      blitzWatcherThread = null;
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

      if (move.getType() == MoveType.PLAY) {
        Gaddag dictionary = getOrLoadGaddag();
        MoveHandler moveHandler = new MoveHandler(game);
        for (String formedWord : moveHandler.getFormedWords(move.getStartPosition(),
            move.getDirection(), move.getTiles())) {
          if (formedWord == null || formedWord.isBlank()
              || !dictionary.containsWord(formedWord.toUpperCase())) {
            throw new IllegalArgumentException("Word not found in dictionary: " + formedWord);
          }
        }
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
  Gaddag getOrLoadGaddag() {
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
}
