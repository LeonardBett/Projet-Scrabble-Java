package fr.ubordeaux.scrabble.controller;

import fr.ubordeaux.scrabble.i18n.I18n;
import fr.ubordeaux.scrabble.model.ai.AiPlayer;
import fr.ubordeaux.scrabble.model.ai.MlAgent;
import fr.ubordeaux.scrabble.model.core.Game;
import fr.ubordeaux.scrabble.model.core.HumanPlayer;
import fr.ubordeaux.scrabble.model.core.Move;
import fr.ubordeaux.scrabble.model.core.MoveHandler;
import fr.ubordeaux.scrabble.model.dictionary.Gaddag;
import fr.ubordeaux.scrabble.model.enums.MoveType;
import fr.ubordeaux.scrabble.model.interfaces.Player;
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

    CliInputHandler input = new CliInputHandler();
    CliView cliView = (CliView) view;

    cliView.displayWelcome();

    if (game.getPlayers().size() < 2) {
      int num = playerCount > 0 ? playerCount : input.askNumberOfPlayers();
      for (int i = 1; i <= num; i++) {
        String name = input.askPlayerName(i);

        if (name.toUpperCase().startsWith("IA") || name.toUpperCase().startsWith("AI")) {
          AiPlayer bot = new AiPlayer(name, 3, 5);
          bot.setExpectiminimaxMode(this.useExptiminimax);

          if (this.useMl) {
            List<String> dictList = getOrLoadDictionaryList();
            String modelPath = "src/main/resources/ai/model_" + this.lang;
            MlAgent mlAgent = new MlAgent(modelPath, dictList);
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
              if (mlAgent != null) {
                mlAgent.close();
              }
            }));
            bot.setMlAgent(mlAgent);
            cliView.displayMessage(I18n.tr("controller.msg.mlActivated", name, this.lang));
          }
          addPlayer(bot);
        } else {
          addPlayer(new HumanPlayer(name));
        }
      }
    }

    startGame();

    if (game.isBlitzModeEnabled()) {
      cliView.displayMessage(I18n.tr("controller.msg.blitzEnabled",
          game.getPlayers().get(0).getRemainingTimeDisplay()));
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
        break;
      }

      // Tour de l'IA
      if (current instanceof AiPlayer) {
        cliView.displayMessage(I18n.tr("controller.msg.aiTurn", current.getName()));
        AiPlayer ai = (AiPlayer) current;
        try {
          ai.playTurn(game, currentGaddag);
          Thread.sleep(2000);
        } catch (Exception e) {
          cliView.displayError(I18n.tr("controller.msg.aiTurnError", e.getMessage()));
          e.printStackTrace();
          handlePlayerMove(Move.createPass(current));
        }
        continue;
      }

      // Tour humain
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
              cliView.displaySuccess(I18n.tr("controller.msg.movePlayed"));
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
              cliView.displaySuccess(I18n.tr("controller.msg.lettersExchanged"));
            } catch (RuntimeException e) {
              cliView.displayError(e.getMessage());
            }
          }
          break;
        }
        case "3": {
          try {
            handlePlayerMove(Move.createPass(current));
            cliView.displayMessage(I18n.tr("controller.msg.turnPassed", current.getName()));
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
          if (input.askConfirmation("Voulez-vous vraiment quitter ?")) {
            running = false;
          }
          break;
        }
        default:
          cliView.displayError(I18n.tr("controller.msg.invalidChoice"));
      }
    }

    stopBlitzWatcher();

    Player winner = game.determineWinner();
    if (winner != null) {
      cliView.displaySuccess(I18n.tr("controller.msg.gameOverWinner", winner.getName(),
          winner.getScore()));
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
              System.out.println(I18n.tr("controller.msg.blitzWarning", current.getName(),
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
    cliView.displayError(I18n.tr("controller.msg.blitzExpired", expired.getName()));
    cliView.displayMessage(I18n.tr("controller.msg.gameFinished"));
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
            throw new IllegalArgumentException(I18n.tr("controller.err.wordMissing", formedWord));
          }
        }
      }

      game.executeMove(move);
      view.refresh();

    } catch (IllegalArgumentException | IllegalStateException e) {
      throw new RuntimeException(I18n.tr("controller.err.invalidMove", e.getMessage()), e);
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
      System.err.println(I18n.tr("controller.warn.dictListLoad", e.getMessage()));
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
    System.out.println(I18n.tr("controller.msg.loadingDict", dictPath));

    try (InputStream is = getClass().getClassLoader().getResourceAsStream(dictPath)) {
      if (is == null) {
        throw new IllegalStateException(I18n.tr("controller.err.dictMissing", dictPath));
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

      System.out.println(I18n.tr("controller.msg.dictLoaded", wordCount));
      return gaddag;
    } catch (Exception e) {
      throw new IllegalStateException(I18n.tr("controller.err.dictLoad", e.getMessage()), e);
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
   * Sets the number of players to use at launch (skips the interactive prompt).
   *
   * @param count the number of players (2‑4)
   */
  public void setPlayerCount(int count) {
    this.playerCount = count;
  }
}