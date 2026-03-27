package fr.ubordeaux.scrabble.view.gui;

import fr.ubordeaux.scrabble.controller.GameController;
import fr.ubordeaux.scrabble.i18n.I18n;
import fr.ubordeaux.scrabble.model.ai.AiPlayer;
import fr.ubordeaux.scrabble.model.core.Game;
import fr.ubordeaux.scrabble.model.core.HumanPlayer;
import fr.ubordeaux.scrabble.model.core.Move;
import fr.ubordeaux.scrabble.model.core.Rack;
import fr.ubordeaux.scrabble.model.core.Tile;
import fr.ubordeaux.scrabble.model.dictionary.Gaddag;
import fr.ubordeaux.scrabble.model.enums.PlayerColor;
import fr.ubordeaux.scrabble.model.interfaces.Player;
import fr.ubordeaux.scrabble.model.network.NetworkManager;
import fr.ubordeaux.scrabble.model.savefiles.GameLoader;
import fr.ubordeaux.scrabble.model.utils.Point;
import fr.ubordeaux.scrabble.view.gui.panel.BoardPanel;
import fr.ubordeaux.scrabble.view.gui.panel.ControlPanel;
import fr.ubordeaux.scrabble.view.gui.panel.MessagePanel;
import fr.ubordeaux.scrabble.view.gui.panel.RackPanel;
import fr.ubordeaux.scrabble.view.gui.panel.ScorePanel;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.MenuButton;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextInputDialog;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;

/**
 * Main JavaFX application window for the Scrabble game.
 *
 * <p>Manages the game board, rack, score panel and control buttons.
 * Supports both local and online multiplayer modes.
 */
public class ScrabbleGui extends Application {

  /**
   * Default constructor for ScrabbleGui.
   */
  public ScrabbleGui() {
  }

  private static Game gameInstance;
  private static JavaFxView viewInstance;
  private static String configuredLanguage = "en";

  private GameController controller;
  private BoardPanel boardPanel;
  private RackPanel rackPanel;
  private ScorePanel scorePanel;
  private ControlPanel controlPanel;
  private MessagePanel messagePanel;

  private final Map<Point, Tile> pendingTiles = new HashMap<>();
  private Tile currentlyDraggedTile = null;

  private NetworkManager networkManager;
  private NetworkGameBridge networkBridge;
  private NetworkLobbyView lobbyView;
  private boolean onlineMode = false;

  private MenuButton appMenuButton;
  private MenuItem newGameMenuItem;
  private MenuItem onlineMenuItem;
  private MenuItem saveMenuItem;
  private MenuItem loadMenuItem;
  private MenuItem quitMenuItem;

  /**
   * Sets the game instance for static access.
   *
   * @param game the game to use
   */
  public static void setGame(Game game) {
    gameInstance = game;
  }

  /**
   * Sets the view instance for static access.
   *
   * @param view the view to use
   */
  public static void setView(JavaFxView view) {
    viewInstance = view;
  }

  /**
   * Sets the language used by GUI dictionary loading.
   *
   * @param lang language code ("en" or "fr")
   */
  public static void setLanguage(String lang) {
    configuredLanguage = "fr".equalsIgnoreCase(lang) ? "fr" : "en";
    I18n.setLanguage(configuredLanguage);
  }

  @Override
  public void start(Stage stage) {
    if (gameInstance == null) {
      throw new IllegalStateException("Appelez ScrabbleGui.setGame() avant de lancer.");
    }

    networkManager = new NetworkManager();
    networkBridge = new NetworkGameBridge(networkManager);
    networkBridge.setGui(this);

    messagePanel = new MessagePanel();
    scorePanel = new ScorePanel();
    controlPanel = new ControlPanel();
    boardPanel = new BoardPanel(gameInstance.getBoard());
    rackPanel = new RackPanel(getCurrentRack());

    if (viewInstance == null) {
      viewInstance = new JavaFxView(gameInstance);
    }
    viewInstance.setGui(this);

    boardPanel.setOnTileDropped(this::onTileDropped);
    rackPanel.setOnTileDragged(this::onTileDragged);

    controller = new GameController(gameInstance, viewInstance);
    VBox leftMenu = buildLeftMenu();

    BorderPane root = new BorderPane();
    root.setPadding(new Insets(10));
    root.setStyle("-fx-background-color: #115829;");
    root.setCenter(boardPanel);
    root.setLeft(leftMenu);

    connectButtons();

    VBox right = new VBox(15);
    right.setAlignment(Pos.TOP_CENTER);
    right.setPadding(new Insets(0, 0, 0, 15));
    right.getChildren().addAll(scorePanel, controlPanel);
    root.setRight(right);
    root.setBottom(rackPanel);

    stage.setOnCloseRequest(e -> networkBridge.dispose());
    stage.setTitle(I18n.translate("scrabble.windowTitle"));
    stage.setScene(new Scene(root, 1200, 800));
    stage.setFullScreen(true);
    stage.show();

    controller.startGame();
    if (gameInstance.isBlitzModeEnabled()) {
      scorePanel.startBlitzTimers(gameInstance.getPlayers(), this::onBlitzTimeExpired);
    }
    refreshAll();
  }

  private void connectButtons() {
    controlPanel.getPlayButton().setOnAction(e -> {
      if (gameInstance.isGameOver()) {
        return;
      }
      submitPendingTiles();
    });

    controlPanel.getPassButton().setOnAction(e -> {
      if (gameInstance.isGameOver()) {
        return;
      }
      if (onlineMode) {
        networkManager.pass();
      } else {
        controller.handlePlayerMove(Move.createPass(gameInstance.getCurrentPlayer()));
      }
    });

    controlPanel.getExchangeButton().setOnAction(e -> {
      if (gameInstance.isGameOver()) {
        return;
      }
      openExchangeDialog();
    });
    controlPanel.getCancelPlacementButton().setOnAction(e -> {
      if (gameInstance.isGameOver()) {
        return;
      }
      cancelPendingTiles();
    });
    controlPanel.getUndoButton().setOnAction(e -> {
      if (!onlineMode && !gameInstance.isGameOver()) {
        controller.undo();
      }
    });
    controlPanel.getRedoButton().setOnAction(e -> {
      if (!onlineMode && !gameInstance.isGameOver()) {
        controller.redo();
      }
    });
    controlPanel.getHintButton().setOnAction(e -> {
      if (!onlineMode && !gameInstance.isGameOver()) {
        controller.provideHint();
      }
    });

    newGameMenuItem.setOnAction(e -> handleNewGame());
    onlineMenuItem.setOnAction(e -> openNetworkLobby());
    saveMenuItem.setOnAction(e -> {
      if (gameInstance.isGameOver()) {
        showError(I18n.translate("scrabble.error.gameOverSave"));
        return;
      }

      javafx.stage.FileChooser chooser = new javafx.stage.FileChooser();
      chooser.setTitle(I18n.translate("scrabble.saveDialogTitle"));
      chooser.getExtensionFilters().add(
          new javafx.stage.FileChooser.ExtensionFilter(I18n.translate("scrabble.saveFileFilter"),
              "*.scrabble", "*.txt"));
      chooser.setInitialFileName(I18n.translate("scrabble.saveDefaultFile"));

      java.io.File file = chooser.showSaveDialog(appMenuButton.getScene().getWindow());
      if (file == null) {
        return; // utilisateur a annulé
      }

      try {
        new fr.ubordeaux.scrabble.model.savefiles.SaveManager()
            .saveGame(gameInstance, file.getAbsolutePath());
        showInfo(I18n.translate("scrabble.saveSuccessTitle"),
            I18n.translate("scrabble.saveSuccessMessage", file.getAbsolutePath()));
      } catch (java.io.IOException ex) {
        showError(I18n.translate("scrabble.saveError", ex.getMessage()));
      }
    });

    loadMenuItem.setOnAction(e -> {
      javafx.stage.FileChooser chooser = new javafx.stage.FileChooser();
      chooser.setTitle(I18n.translate("scrabble.loadDialogTitle"));
      chooser.getExtensionFilters().add(
          new javafx.stage.FileChooser.ExtensionFilter(I18n.translate("scrabble.loadFileFilter"),
              "*.scrabble", "*.txt"));

      java.io.File file = chooser.showOpenDialog(appMenuButton.getScene().getWindow());
      if (file == null) {
        return; // utilisateur a annulé
      }
      Game loadedGame;
      try {
        loadedGame = new GameLoader().loadGame(file.getAbsolutePath());
      } catch (Exception ex) {
        showError(I18n.translate("scrabble.loadError", ex.getMessage()));
        return;
      }
      if (loadedGame != null) {
        gameInstance = loadedGame;

        // Plus sûr de recréer la vue que d'appeler setGame() si la méthode n'existe pas
        viewInstance = new JavaFxView(gameInstance);
        viewInstance.setGui(this);

        controller = new GameController(gameInstance, viewInstance);
        boardPanel.setBoard(gameInstance.getBoard());
        pendingTiles.clear();

        if (gameInstance.isBlitzModeEnabled()) {
          scorePanel.startBlitzTimers(gameInstance.getPlayers(), this::onBlitzTimeExpired);
        } else {
          scorePanel.stopBlitzTimers();
        }

        refreshAll();
        showInfo(I18n.translate("scrabble.loadSuccessTitle"),
            I18n.translate("scrabble.loadSuccessMessage", file.getAbsolutePath()));
      } else {
        showError(I18n.translate("scrabble.loadGenericError"));
      }
    });
    quitMenuItem.setOnAction(e -> {
      if (messagePanel.showConfirmation(I18n.translate("scrabble.quitConfirmation"))) {
        networkBridge.dispose();
        Platform.exit();
      }
    });
  }

  private void openNetworkLobby() {
    if (lobbyView == null) {
      lobbyView = new NetworkLobbyView(networkBridge);
    }
    lobbyView.show();
    lobbyView.toFront();
  }

  /**
   * Switches the GUI to online game mode using the provided game model.
   *
   * @param onlineGame the online game model received from the server
   */
  public void switchToOnlineGame(Game onlineGame) {
    gameInstance = onlineGame;
    onlineMode = true;
    controlPanel.getUndoButton().setDisable(true);
    controlPanel.getRedoButton().setDisable(true);
    boardPanel.setBoard(gameInstance.getBoard());
    pendingTiles.clear();
    refreshAll();
    showInfo(I18n.translate("scrabble.onlineStartedTitle"),
        I18n.translate("scrabble.onlineStartedMessage"));
  }

  /**
   * Exits online mode and re-enables undo/redo buttons.
   */
  public void exitOnlineMode() {
    onlineMode = false;
    scorePanel.stopBlitzTimers();
    controlPanel.getUndoButton().setDisable(false);
    controlPanel.getRedoButton().setDisable(false);
  }

  /**
   * Returns whether the game is currently in online multiplayer mode.
   *
   * @return true if online mode is active
   */
  public boolean isOnlineMode() {
    return onlineMode;
  }

  /**
   * Called by RackPanel when a tile drag starts.
   *
   * @param tile The tile being dragged from the rack.
   */
  public void onTileDragged(Tile tile) {
    this.currentlyDraggedTile = tile;
  }

  /**
   * Called by BoardPanel when a tile is dropped on a cell.
   *
   * @param row the row index of the drop target
   * @param col the column index of the drop target
   */
  public void onTileDropped(int row, int col) {
    if (currentlyDraggedTile == null || gameInstance.isGameOver()) {
      return;
    }

    Point point = new Point(col, row);
    if (!gameInstance.getBoard().getSquare(point).isEmpty() || pendingTiles.containsKey(point)) {
      showError(I18n.translate("scrabble.cellOccupied"));
      currentlyDraggedTile = null;
      return;
    }

    pendingTiles.put(point, currentlyDraggedTile);
    boardPanel.placeTile(row, col, currentlyDraggedTile.getCharacter(),
        currentlyDraggedTile.getValue());
    rackPanel.hideTile(currentlyDraggedTile);
    currentlyDraggedTile = null;
  }

  private static Gaddag gaddag;

  private void loadDictionary() {
    gaddag = new Gaddag();
    System.out.println("Loading Gaddag for GUI (" + configuredLanguage + ")...");
    String dictPath = "dictionaries/lexicon_" + configuredLanguage + ".txt";
    try (InputStream is = getClass().getClassLoader().getResourceAsStream(dictPath)) {
      if (is != null) {
        try (BufferedReader br = new BufferedReader(new InputStreamReader(is))) {
          String line;
          while ((line = br.readLine()) != null) {
            if (!line.trim().isEmpty()) {
              gaddag.add(line.trim());
            }
          }
        }
      } else if (!"en".equals(configuredLanguage)) {
        try (InputStream fallback =
                getClass().getClassLoader().getResourceAsStream("dictionaries/lexicon_en.txt")) {
          if (fallback != null) {
            try (BufferedReader br = new BufferedReader(new InputStreamReader(fallback))) {
              String line;
              while ((line = br.readLine()) != null) {
                if (!line.trim().isEmpty()) {
                  gaddag.add(line.trim());
                }
              }
            }
          }
        }
      }
    } catch (IOException e) {
      showError(I18n.translate("scrabble.dictLoadError", e.getMessage()));
    }
  }

  private void submitPendingTiles() {
    if (pendingTiles.isEmpty()) {
      showError(I18n.translate("scrabble.needTile"));
      return;
    }

    Move move = PendingMoveBuilder.build(pendingTiles, gameInstance.getCurrentPlayer());
    if (move == null) {
      showError(I18n.translate("scrabble.invalidAlignment"));
      cancelPendingTiles();
      return;
    }

    if (onlineMode) {
      // En mode online : on envoie directement au serveur, c'est lui qui valide
      Point origin = move.getStartPosition();
      String dir = move.getDirection().name().substring(0, 1);
      String word = move.getTiles().stream().map(t -> String.valueOf(t.getCharacter())).reduce("",
          String::concat);
      networkManager.play(origin.getX(), origin.getY(), dir, word);
      pendingTiles.clear();
    } else {
      // En mode local : on valide via le controller
      try {
        controller.handlePlayerMove(move);
        pendingTiles.clear();
      } catch (RuntimeException e) {
        showError(I18n.translate("scrabble.invalidMove", e.getMessage()));
        cancelPendingTiles();
      }
    }
  }

  private void openExchangeDialog() {
    if (!pendingTiles.isEmpty()) {
      showError(I18n.translate("scrabble.exchange.cancelPending"));
      return;
    }

    TextInputDialog dialog = new TextInputDialog();
    dialog.setTitle(I18n.translate("scrabble.exchange.title"));
    dialog.setHeaderText(I18n.translate("scrabble.exchange.header"));
    dialog.setContentText(I18n.translate("scrabble.exchange.content"));

    Optional<String> result = dialog.showAndWait();
    result.ifPresent(input -> {
      String letters = input.trim().toUpperCase();
      if (letters.isEmpty()) {
        return;
      }

      if (onlineMode) {
        networkManager.exchange(letters);
      } else {
        Move move = ExchangeMoveBuilder.build(letters, gameInstance.getCurrentPlayer());
        if (move == null) {
          showError(I18n.translate("scrabble.exchange.invalidRack"));
          return;
        }
        controller.handlePlayerMove(move);
      }
    });
  }

  private void cancelPendingTiles() {
    if (pendingTiles.isEmpty()) {
      return;
    }
    pendingTiles.forEach((p, t) -> boardPanel.clearTile(p.getY(), p.getX()));
    pendingTiles.clear();
    refreshRack();
  }

  private void handleNewGame() {
    if (!messagePanel.showConfirmation(I18n.translate("scrabble.newGameConfirmation"))) {
      return;
    }

    Optional<Integer> countOpt = PlayerSetup.showDialog();
    if (countOpt.isEmpty()) {
      return;
    }

    // Nettoyage complet avant de recréer
    if (onlineMode) {
      networkBridge.dispose();
      networkManager = new NetworkManager();
      networkBridge = new NetworkGameBridge(networkManager);
      networkBridge.setGui(this);
      lobbyView = null;
      onlineMode = false;
    }

    gameInstance = new Game();
    int count = countOpt.get();

    if (gaddag == null) {
      loadDictionary();
    }
    for (int i = 1; i <= count; i++) {
      PlayerColor color = PlayerColor.fromIndex(i - 1);
      gameInstance.addPlayer(new HumanPlayer(I18n.translate("scrabble.defaultPlayer", i), color));
    }

    viewInstance = new JavaFxView(gameInstance);
    viewInstance.setGui(this);
    controller = new GameController(gameInstance, viewInstance);

    setGameplayControlsDisabled(false);

    boardPanel.clearAllPending();
    pendingTiles.clear();
    boardPanel.setBoard(gameInstance.getBoard());

    // Démarrer la partie AVANT de rafraîchir l'affichage
    controller.startGame();
    if (gameInstance.isBlitzModeEnabled()) {
      scorePanel.startBlitzTimers(gameInstance.getPlayers(), this::onBlitzTimeExpired);
    } else {
      scorePanel.stopBlitzTimers();
    }
    refreshAll();
  }

  /**
   * Called when a player's blitz time has expired.
   * Ends the game and notifies the players.
   */
  private void onBlitzTimeExpired() {
    gameInstance.setGameOver(true);
    setGameplayControlsDisabled(true);
    // Find the player who ran out of time
    gameInstance.getPlayers().stream()
        .filter(p -> p.isBlitzClockEnabled() && p.isOutOfTime())
        .findFirst()
        .ifPresent(p -> showInfo(I18n.translate("scrabble.blitzTimeoutTitle"),
            I18n.translate("scrabble.blitzTimeoutMessage", p.getName())));
    refreshScores();
  }

  /**
   * Refreshes all GUI panels: board, rack, scores, and checks if it is the AI's turn.
   */
  public void refreshAll() {
    refreshBoard();
    refreshRack();
    refreshScores();
    checkAiTurn();
  }

  private void checkAiTurn() {
    if (boardPanel.isDisable()) {
      return;
    }
    Player current = gameInstance.getCurrentPlayer();
    if (current instanceof AiPlayer && !gameInstance.isGameOver()) {
      if (gaddag == null) {
        loadDictionary();
      }
      final AiPlayer ai = (AiPlayer) current;
      boardPanel.setDisable(true);
      rackPanel.setDisable(true);
      controlPanel.setGameplayButtonsDisabled(true);

      new Thread(() -> {
        try {
          Thread.sleep(1000);
          ai.playTurn(gameInstance, gaddag);
        } catch (InterruptedException e) {
          Thread.currentThread().interrupt();
          Platform.runLater(() -> controller.handlePlayerMove(Move.createPass(ai)));
        } catch (RuntimeException e) {
          Platform.runLater(() -> showError(I18n.translate("scrabble.aiError", e.getMessage())));
          Platform.runLater(() -> controller.handlePlayerMove(Move.createPass(ai)));
        } finally {
          Platform.runLater(() -> {
            if (gameInstance.isGameOver()) {
              setGameplayControlsDisabled(true);
            } else {
              boardPanel.setDisable(false);
              rackPanel.setDisable(false);
              controlPanel.setGameplayButtonsDisabled(false);
            }
            refreshAll();
          });
        }
      }).start();
    }
  }

  private void setGameplayControlsDisabled(boolean disabled) {
    boardPanel.setDisable(disabled);
    rackPanel.setDisable(disabled);
    controlPanel.setGameplayButtonsDisabled(disabled);
  }

  private VBox buildLeftMenu() {
    Label menuLabel = new Label(I18n.translate("scrabble.menuTitle"));
    menuLabel.setTextFill(Color.WHITE);
    menuLabel.setFont(Font.font("Arial", FontWeight.BOLD, 14));

    newGameMenuItem = new MenuItem(I18n.translate("scrabble.menu.newGame"));
    onlineMenuItem = new MenuItem(I18n.translate("scrabble.menu.multiplayer"));
    saveMenuItem = new MenuItem(I18n.translate("scrabble.menu.save"));
    loadMenuItem = new MenuItem(I18n.translate("scrabble.menu.load"));
    quitMenuItem = new MenuItem(I18n.translate("scrabble.menu.quit"));

    appMenuButton = new MenuButton(I18n.translate("scrabble.menuButton"), null,
        newGameMenuItem, onlineMenuItem, saveMenuItem, loadMenuItem, quitMenuItem);
    appMenuButton.setPrefWidth(190);
    appMenuButton.setStyle("-fx-background-color: #0B3D1D; -fx-text-fill: white;");

    VBox left = new VBox(8, menuLabel, appMenuButton);
    left.setAlignment(Pos.TOP_LEFT);
    left.setPadding(new Insets(8, 15, 0, 0));
    return left;
  }

  /**
   * Refreshes the board panel to reflect the current game state.
   */
  public void refreshBoard() {
    boardPanel.updateBoard();
  }

  /**
   * Refreshes the rack panel for the current player.
   */
  public void refreshRack() {
    rackPanel.setRack(getCurrentRack());
    rackPanel.setOnTileDragged(this::onTileDragged);
  }

  /**
   * Refreshes the score panel with updated player scores and bag info.
   */
  public void refreshScores() {
    List<Player> players = gameInstance.getPlayers();
    if (players.isEmpty()) {
      return;
    }
    String[] names = players.stream().map(Player::getName).toArray(String[]::new);
    int[] scores = players.stream().mapToInt(Player::getScore).toArray();
    scorePanel.updateScores(names, scores);
    scorePanel.updateBagInfo(gameInstance.getBag().size());
    Player current = gameInstance.getCurrentPlayer();
    if (current != null) {
      int idx = players.indexOf(current);
      if (idx >= 0) {
        scorePanel.highlightCurrentPlayer(idx, current.getName());
      }
    }
  }

  /**
   * Displays an informational dialog.
   *
   * @param title the dialog title
   * @param message the message to display
   */
  public void showInfo(String title, String message) {
    messagePanel.showInfo(title, message);
  }

  /**
   * Displays an error dialog.
   *
   * @param message the error message to display
   */
  public void showError(String message) {
    messagePanel.showError(message);
  }

  private Rack getCurrentRack() {
    Player p = gameInstance.getCurrentPlayer();
    return p != null ? p.getRack() : new Rack();
  }

  private static boolean isOccupiedOrPending(Game game, Map<Point, Tile> pending, Point point) {
    return !game.getBoard().getSquare(point).isEmpty() || pending.containsKey(point);
  }

  private static String normalizeExchangeLetters(String input) {
    return input == null ? "" : input.trim().toUpperCase();
  }

  private static boolean shouldSkipExchange(String letters) {
    return letters == null || letters.isEmpty();
  }

  private static boolean shouldIgnoreGameplayAction(boolean gameOver) {
    return gameOver;
  }

  private static boolean shouldPassThroughNetwork(boolean onlineMode) {
    return onlineMode;
  }

  private static boolean canUseUndoRedo(boolean onlineMode, boolean gameOver) {
    return !onlineMode && !gameOver;
  }

  private static boolean shouldIgnoreTileDrop(Tile tile, boolean gameOver) {
    return tile == null || gameOver;
  }

  private static boolean shouldRunAiTurn(Player current, boolean gameOver) {
    return current instanceof AiPlayer && !gameOver;
  }

  private static boolean shouldKeepGameplayDisabledAfterAi(boolean gameOver) {
    return gameOver;
  }

  private static boolean shouldOpenNetworkLobby(NetworkLobbyView lobby) {
    return lobby == null;
  }

  private static String normalizedDictionaryLine(String line) {
    return line == null ? "" : line.trim();
  }

  private static boolean shouldAddDictionaryEntry(String line) {
    return line != null && !line.isEmpty();
  }

  private static boolean shouldLoadDictionaryForAi(Gaddag currentGaddag) {
    return currentGaddag == null;
  }

  private static boolean shouldSkipScoreRefresh(List<Player> players) {
    return players == null || players.isEmpty();
  }

  private static boolean shouldHighlightScoreIndex(int index) {
    return index >= 0;
  }

  private static boolean shouldRejectSubmitWhenNoPending(Map<Point, Tile> pending) {
    return pending == null || pending.isEmpty();
  }

  private static boolean shouldRejectSubmitWhenMoveNull(Move move) {
    return move == null;
  }

  private static boolean shouldBlockExchangeWhilePending(Map<Point, Tile> pending) {
    return pending != null && !pending.isEmpty();
  }

  private static boolean shouldCancelWhenPendingEmpty(Map<Point, Tile> pending) {
    return pending == null || pending.isEmpty();
  }

  private static boolean shouldStartBlitz(boolean blitzEnabled) {
    return blitzEnabled;
  }

  private static String[] toPlayerNames(List<Player> players) {
    if (players == null || players.isEmpty()) {
      return new String[0];
    }
    return players.stream().map(Player::getName).toArray(String[]::new);
  }

  private static int[] toPlayerScores(List<Player> players) {
    if (players == null || players.isEmpty()) {
      return new int[0];
    }
    return players.stream().mapToInt(Player::getScore).toArray();
  }

  private static int indexOfCurrentPlayer(List<Player> players, Player current) {
    if (players == null || current == null) {
      return -1;
    }
    return players.indexOf(current);
  }

  private static String buildBlitzTimeoutMessage(String playerName) {
    return playerName + " a épuisé son temps. La partie est terminée !";
  }

  private static String blitzTimeoutTitle() {
    return "⏱ Temps écoulé !";
  }

  private static List<HumanPlayer> createDefaultPlayers(int count) {
    List<HumanPlayer> players = new java.util.ArrayList<>();
    for (int i = 1; i <= count; i++) {
      players.add(new HumanPlayer(defaultPlayerName(i), PlayerColor.fromIndex(i - 1)));
    }
    return players;
  }

  private static Optional<String> findOutOfTimePlayerName(List<Player> players) {
    return players.stream()
        .filter(p -> p.isBlitzClockEnabled() && p.isOutOfTime())
        .map(Player::getName)
        .findFirst();
  }

  private static String buildPlayedWord(Move move) {
    if (move == null || move.getTiles() == null) {
      return "";
    }
    return move.getTiles().stream().map(t -> String.valueOf(t.getCharacter())).reduce("",
        String::concat);
  }

  private static int moveOriginX(Move move) {
    return move.getStartPosition().getX();
  }

  private static int moveOriginY(Move move) {
    return move.getStartPosition().getY();
  }

  private static String moveDirectionToken(Move move) {
    return move.getDirection().name().substring(0, 1);
  }

  private static boolean shouldAbortNewGame(boolean confirmed) {
    return !confirmed;
  }

  private static boolean shouldAbortWhenMissingPlayerCount(Optional<Integer> countOpt) {
    return countOpt == null || countOpt.isEmpty();
  }

  private static boolean shouldReinitializeNetworkForNewGame(boolean onlineMode) {
    return onlineMode;
  }

  private static int selectedPlayerCount(Optional<Integer> countOpt) {
    return countOpt.orElse(0);
  }

  private static boolean shouldLoadGaddag(Gaddag currentGaddag) {
    return currentGaddag == null;
  }

  private static String defaultPlayerName(int index) {
    return "Joueur" + index;
  }

  private static String menuTitleText() {
    return "MENU";
  }

  private static int windowWidth() {
    return 1200;
  }

  private static int windowHeight() {
    return 800;
  }

  private static String helpDialogTitle() {
    return "Aide";
  }

  private static String helpDialogMessage() {
    return "Consultez les règles du Scrabble pour jouer.";
  }

  private static String occupiedCellMessage() {
    return "Cette case est déjà occupée !";
  }

  private static String placeAtLeastOneTileMessage() {
    return "Placez au moins une tuile avant de valider !";
  }

  private static String invalidAlignmentMessage() {
    return "Les tuiles doivent être alignées horizontalement ou verticalement !";
  }

  private static String exchangeDialogTitle() {
    return "Échanger des lettres";
  }

  private static String exchangeDialogHeaderText() {
    return "Lettres de votre chevalet à échanger";
  }

  private static String exchangeDialogContentText() {
    return "Lettres (ex: ABC) :";
  }

  private static String onlineStartedTitle() {
    return "Partie en ligne";
  }

  private static String onlineStartedMessage() {
    return "La partie a commencé ! Bonne chance 🎮";
  }

  private static String saveComingSoonMessage() {
    return "Sauvegarde bientôt disponible";
  }

  private static String loadComingSoonMessage() {
    return "Chargement bientôt disponible";
  }

  private static String comingSoonTitle() {
    return "Bientôt";
  }

  private static String appMenuButtonText() {
    return "☰ Jeu";
  }

  private static String newGameMenuText() {
    return "Nouvelle partie";
  }

  private static String multiplayerMenuText() {
    return "Multijoueur";
  }

  private static String saveMenuText() {
    return "Sauvegarder";
  }

  private static String loadMenuText() {
    return "Charger";
  }

  private static String quitMenuText() {
    return "Quitter";
  }

  private static String rootBackgroundStyle() {
    return "-fx-background-color: #115829;";
  }

  private static String appMenuButtonStyle() {
    return "-fx-background-color: #0B3D1D; -fx-text-fill: white;";
  }

  private static double rootPadding() {
    return 10.0;
  }

  private static double rightPanelSpacing() {
    return 15.0;
  }

  private static double appMenuButtonWidth() {
    return 190.0;
  }

  private static String menuLabelFontFamily() {
    return "Arial";
  }

  private static int menuLabelFontSize() {
    return 14;
  }

  private static String dictionaryLoadErrorMessage(String details) {
    return "Impossible de charger le dictionnaire : " + details;
  }

  private static String invalidMoveMessage(String details) {
    return "Coup invalide : " + details;
  }

  private static String cancelTilesBeforeExchangeMessage() {
    return "Annulez d'abord les tuiles placées (bouton ↩).";
  }

  private static String exchangeLettersNotInRackMessage() {
    return "Certaines lettres ne sont pas dans votre chevalet !";
  }

  private static String newGameConfirmationMessage() {
    return "Abandonner la partie en cours et recommencer ?";
  }

  private static String quitConfirmationMessage() {
    return "Voulez-vous vraiment quitter ?";
  }

  private static String missingGameErrorMessage() {
    return "Appelez ScrabbleGui.setGame() avant de lancer.";
  }

  private static String aiErrorMessage(String details) {
    return "Erreur IA : " + details;
  }

  private static String windowTitleText() {
    return "Scrabble U-Bordeaux";
  }

  private static double rightTopPadding() {
    return 0.0;
  }

  private static double rightRightPadding() {
    return 0.0;
  }

  private static double rightBottomPadding() {
    return 0.0;
  }

  private static double rightLeftPadding() {
    return 15.0;
  }

  private static double leftMenuSpacing() {
    return 8.0;
  }

  private static double leftMenuTopPadding() {
    return 8.0;
  }

  private static double leftMenuRightPadding() {
    return 15.0;
  }

  private static double leftMenuBottomPadding() {
    return 0.0;
  }

  private static double leftMenuLeftPadding() {
    return 0.0;
  }

  /**
   * Entry point for the JavaFX application.
   *
   * @param args application arguments
   */
  public static void main(String[] args) {
    launch(args);
  }
}