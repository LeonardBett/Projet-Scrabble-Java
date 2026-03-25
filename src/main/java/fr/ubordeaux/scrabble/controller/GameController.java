package fr.ubordeaux.scrabble.controller;

import fr.ubordeaux.scrabble.model.ai.AiPlayer;
import fr.ubordeaux.scrabble.model.ai.MlAgent;
import fr.ubordeaux.scrabble.model.core.Board;
import fr.ubordeaux.scrabble.model.core.Game;
import fr.ubordeaux.scrabble.model.core.HumanPlayer;
import fr.ubordeaux.scrabble.model.core.Move;
import fr.ubordeaux.scrabble.model.core.MoveGenerator;
import fr.ubordeaux.scrabble.model.core.MoveHandler;
import fr.ubordeaux.scrabble.model.core.PlayableWord;
import fr.ubordeaux.scrabble.model.core.Scoring;
import fr.ubordeaux.scrabble.model.core.Square;
import fr.ubordeaux.scrabble.model.core.Tile;
import fr.ubordeaux.scrabble.model.dictionary.Gaddag;
import fr.ubordeaux.scrabble.model.enums.Direction;
import fr.ubordeaux.scrabble.model.enums.MoveType;
import fr.ubordeaux.scrabble.model.enums.PlayerColor;
import fr.ubordeaux.scrabble.model.interfaces.Player;
import fr.ubordeaux.scrabble.model.utils.GameLogger;
import fr.ubordeaux.scrabble.model.utils.Point;
import fr.ubordeaux.scrabble.view.UserInterface;
import fr.ubordeaux.scrabble.view.cli.CliInputHandler;
import fr.ubordeaux.scrabble.view.cli.CliView;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
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
  private String lang = "en"; // Default

  // AI Configuration fields
  private int aiTime = 5;
  private boolean useExptiminimax = false;
  private boolean useMl = false;

  // Player count set by launcher (0 = ask interactively)
  private int playerCount = 0;

  public GameController(Game game, UserInterface view) {
    this.game = game;
    this.view = view;
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
    if (!(view instanceof CliView)) {
      throw new IllegalStateException("CLI loop requires a CliView instance as view.");
    }

    final CliInputHandler input = new CliInputHandler();
    CliView cliView = (CliView) view;

    MlAgent sharedMlAgent = null;

    if (this.useMl) {
      List<String> dictList = getOrLoadDictionaryList();
      String modelPath = "src/main/resources/ai/model_" + this.lang;
      sharedMlAgent = new MlAgent(modelPath, dictList);

      final MlAgent finalAgent = sharedMlAgent;
      Runtime.getRuntime().addShutdownHook(new Thread(() -> {
        if (finalAgent != null) {
          finalAgent.close();
        }
      }));
    }

    for (Player p : game.getPlayers()) {
      if (p instanceof AiPlayer) {
        AiPlayer bot = (AiPlayer) p;
        bot.setExpectiminimaxMode(this.useExptiminimax);
        if (sharedMlAgent != null) {
          bot.setMlAgent(sharedMlAgent);
        }
      }
    }

    cliView.displayWelcome();

    if (game.getPlayers().size() < 2) {
      int num = playerCount > 0 ? playerCount : input.askNumberOfPlayers();
      for (int i = 1; i <= num; i++) {
        String name = input.askPlayerName(i);
        PlayerColor assignedColor = PlayerColor.fromIndex(i - 1);

        if (name.toUpperCase().startsWith("IA") || name.toUpperCase().startsWith("AI")) {
          // Utilisation de aiTime au lieu du 5 par défaut
          AiPlayer bot = new AiPlayer(name, 3, this.aiTime, assignedColor);
          bot.setExpectiminimaxMode(this.useExptiminimax);

          if (sharedMlAgent != null) {
            bot.setMlAgent(sharedMlAgent);
            cliView.displayMessage("-> ML Agent activated for " + name + " (" + this.lang + ")");
          }
          addPlayer(bot);
        } else {
          addPlayer(new HumanPlayer(name, assignedColor));
        }
      }
    }

    startGame();

    if (game.isBlitzModeEnabled()) {
      cliView.displayMessage("⏱  Mode blitz activated — time per player : "
          + game.getPlayers().get(0).getRemainingTimeDisplay());
      startBlitzWatcher(cliView);
    }

    Gaddag currentGaddag = getOrLoadGaddag();

    boolean running = true;
    while (running && !game.isGameOver()) {
      view.refresh();
      Player current = game.getCurrentPlayer();

      // Vérification temps écoulé (blitz)
      if (game.isBlitzModeEnabled() && current != null && current.isOutOfTime()) {
        handleBlitzExpiry(current, cliView);
        game.setGameOver(true);
        view.displayError("Time's up for " + current.getName() + ". Game is over.");
        break;
      }

      // --- GESTION DU TOUR DE L'IA ---
      if (current instanceof AiPlayer) {
        cliView.displayMessage("\n--- It's AI (" + current.getName() + ") turn ---");
        AiPlayer ai = (AiPlayer) current;
        try {
          ai.playTurn(game, currentGaddag);
          Thread.sleep(2000);
        } catch (Exception e) {
          cliView.displayError("Error during AI's turn: " + e.getMessage());
          e.printStackTrace();
          handlePlayerMove(Move.createPass(current));
        }
        continue;
      }

      // --- GESTION DU TOUR D'UN JOUEUR HUMAIN ---
      String action = input.askAction();

      // Re-vérifier le temps après la saisie (le joueur a peut-être pris trop longtemps)
      if (game.isBlitzModeEnabled() && current.isOutOfTime()) {
        handleBlitzExpiry(current, cliView);
        break;
      }

      switch (action) {
        case "1": {
          Move move = input.askPlayMove(current);
          if (move != null) {
            try {
              handlePlayerMove(move);
              cliView.displaySuccess("Move done.");
            } catch (RuntimeException e) {
              cliView.displayError(e.getMessage());
            }
          }
          break;
        }
        case "2": {
          Move move = input.askExchangeMove(current);
          if (move != null) {
            try {
              handlePlayerMove(move);
              cliView.displaySuccess("Letters exchanged.");
            } catch (RuntimeException e) {
              cliView.displayError(e.getMessage());
            }
          }
          break;
        }
        case "3": {
          try {
            handlePlayerMove(Move.createPass(current));
            cliView.displayMessage(current.getName() + " skips his turn.");
          } catch (RuntimeException e) {
            cliView.displayError(e.getMessage());
          }
          break;
        }
        case "4": {
          undo();
          break;
        }
        case "5": {
          redo();
          break;
        }
        case "6": {
          if (input.askConfirmation("Do you really want to quit ?")) {
            running = false;
          }
          break;
        }
        case "7": {
          provideHint();
          break;
        }
        default:
          cliView.displayError("Invalid choice.");
      }
    }

    stopBlitzWatcher();

    Player winner = game.determineWinner();
    if (winner != null) {
      cliView.displaySuccess("Game over. Winnenr: " + winner.getName()
          + " (" + winner.getScore() + " pts)");
    }

    input.close();
  }

  /** Thread de surveillance blitz — affiche un avertissement toutes les minutes. */
  private volatile Thread blitzWatcherThread;

  /**
   * Starts a background thread that checks blitz time every second and warns the player
   * at 5 minutes, 2 minutes and 1 minute remaining.
   *
   * @param cliView the CLI view used to display warnings
   */
  private void startBlitzWatcher(CliView cliView) {
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
              System.out.println("\n " + current.getName()
                  + " — " + minutes + " minute(s) remaining !");
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
  private void stopBlitzWatcher() {
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
  private void handleBlitzExpiry(Player expired, CliView cliView) {
    game.setGameOver(true);
    stopBlitzWatcher();
    cliView.displayError("\nTime's up" + expired.getName() + " !");
    cliView.displayMessage("Game is over.");
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
      throw new RuntimeException("Invalid move: " + e.getMessage(), e);
    }
  }

  /**
   * Loads the lexicon into a simple List of strings to map ML index predictions to words.
   *
   * @return A list of all valid words.
   */
  private List<String> getOrLoadDictionaryList() {
    if (dictionaryList != null) {
      return dictionaryList;
    }

    dictionaryList = new ArrayList<>();
    String dictPath = "dictionaries/lexicon_" + this.lang + ".txt";

    try (InputStream is = getClass().getClassLoader().getResourceAsStream(dictPath)) {
      if (is != null) {
        try (BufferedReader br = new BufferedReader(new InputStreamReader(is))) {
          String line;
          while ((line = br.readLine()) != null) {
            String cleanWord = line.trim().toUpperCase();
            if (!cleanWord.isEmpty()) {
              dictionaryList.add(cleanWord);
            }
          }
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
  private Gaddag getOrLoadGaddag() {
    if (gaddag != null) {
      return gaddag;
    }

    gaddag = new Gaddag();
    String dictPath = "dictionaries/lexicon_" + this.lang + ".txt";
    GameLogger.logVerbose("\nLoading Gaddag dictionary (" + dictPath + ") please wait...");

    try (InputStream is = getClass().getClassLoader().getResourceAsStream(dictPath)) {
      if (is == null) {
        throw new IllegalStateException("Dictionary file " + dictPath + " not found in resources.");
      }

      int wordCount = 0;
      try (BufferedReader br = new BufferedReader(new InputStreamReader(is))) {
        String line;
        while ((line = br.readLine()) != null) {
          String cleanWord = line.trim().toUpperCase();
          if (!cleanWord.isEmpty()) {
            gaddag.add(cleanWord);
            wordCount++;
          }
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

  public void setAiTime(int aiTime) {
    this.aiTime = aiTime;
  }

  public void setUseExptiminimax(boolean useExptiminimax) {
    this.useExptiminimax = useExptiminimax;
  }

  public void setUseMl(boolean useMl) {
    this.useMl = useMl;
  }

  public void setLang(String lang) {
    this.lang = lang;
  }

  /**
   * Generates and displays a hint for the current human player without ending their turn.
   * Searches for the highest-scoring move that specifically uses fewer than 7 letters
   * from the rack to avoid giving away a bingo/scrabble.
   */
  private void provideHint() {
    MoveGenerator moveGen = new MoveGenerator();
    List<PlayableWord> possibleMoves = moveGen.getPlayableWordsList(game, getOrLoadGaddag());

    PlayableWord bestHintMove = null;
    int bestScore = -1;
    List<Character> bestLettersToUse = new ArrayList<>();

    for (PlayableWord move : possibleMoves) {
      List<Character> lettersFromRack = getLettersFromRack(game.getBoard(), move);

      // Strict constraint: The hint must never give away a 7-letter play
      if (!lettersFromRack.isEmpty() && lettersFromRack.size() < 7) {
        int score = simulateScoreForHint(game.getBoard(), move);
        if (score > bestScore) {
          bestScore = score;
          bestHintMove = move;
          bestLettersToUse = lettersFromRack;
        }
      }
    }

    if (bestHintMove != null) {
      view.displayMessage("\n Hint : You can use the letters "
          + bestLettersToUse.toString()
          + " to make a word of " + bestScore + " points.\n");
    } else {
      view.displayMessage("\n Hint : No words shorter than 7"
          +
          "letters were found with your rack.\n");
    }
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
}
