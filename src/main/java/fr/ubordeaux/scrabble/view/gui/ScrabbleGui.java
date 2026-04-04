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
import fr.ubordeaux.scrabble.model.savefiles.ConfigLoader;
import fr.ubordeaux.scrabble.model.savefiles.GameLoader;
import fr.ubordeaux.scrabble.model.utils.Point;
import fr.ubordeaux.scrabble.view.gui.builders.ExchangeMoveBuilder;
import fr.ubordeaux.scrabble.view.gui.builders.PendingMoveBuilder;
import fr.ubordeaux.scrabble.view.gui.config.ControllerConfigSnapshot;
import fr.ubordeaux.scrabble.view.gui.dictionary.GuiDictionaryLoader;
import fr.ubordeaux.scrabble.view.gui.network.NetworkGameBridge;
import fr.ubordeaux.scrabble.view.gui.network.NetworkLobbyView;
import fr.ubordeaux.scrabble.view.gui.panel.BoardPanel;
import fr.ubordeaux.scrabble.view.gui.panel.ControlPanel;
import fr.ubordeaux.scrabble.view.gui.panel.MessagePanel;
import fr.ubordeaux.scrabble.view.gui.panel.RackPanel;
import fr.ubordeaux.scrabble.view.gui.panel.ScorePanel;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Dialog;
import javafx.scene.control.DialogPane;
import javafx.scene.control.Label;
import javafx.scene.control.MenuButton;
import javafx.scene.control.MenuItem;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.control.TextInputDialog;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Region;
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
  private Stage primaryStage;

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
  private MenuItem configurationMenuItem;
  private MenuItem infoMenuItem;
  private MenuItem quitMenuItem;
  private Button newGameButton;
  private Button onlineButton;
  private Button saveButton;
  private Button loadButton;
  private Button configurationButton;
  private Button infoButton;
  private Button quitButton;

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
    primaryStage = stage;
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
    BorderPane root = new BorderPane();
    root.setPadding(new Insets(10));
    root.setStyle("-fx-background-color: #115829;");
    root.setCenter(boardPanel);

    final VBox menuPanel = buildLeftMenu();
    root.setLeft(menuPanel);
    BorderPane.setAlignment(menuPanel, Pos.TOP_LEFT);
    connectButtons();

    VBox right = new VBox(15);
    right.setAlignment(Pos.TOP_CENTER);
    right.setPadding(new Insets(0, 0, 0, 15));
    right.getChildren().addAll(scorePanel, controlPanel);
    root.setRight(right);
    root.setBottom(rackPanel);

    stage.setOnCloseRequest(e -> {
      networkBridge.dispose();
      Platform.exit(); // We close the network window
    });
    stage.setTitle(I18n.translate("scrabble.windowTitle"));
    Scene scene = new Scene(root, 1200, 800);
    setupShortcuts(scene);
    stage.setScene(scene);
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
    controlPanel.getPauseButton().setOnAction(e -> {
      if (!onlineMode && !gameInstance.isGameOver()) {
        controller.togglePause();
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

      java.io.File file = chooser.showSaveDialog(controlPanel.getScene().getWindow());
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

      java.io.File file = chooser.showOpenDialog(controlPanel.getScene().getWindow());
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
    configurationMenuItem.setOnAction(
        e -> showConfigurationDialog());
    infoMenuItem.setOnAction(
        e -> showInfo(I18n.translate("scrabble.menu.info"), I18n.translate("scrabble.info.text")));
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
   * Bind a String to an action.
   */
  private void addDynamicShortcut(Scene scene, String keyString, Runnable action) {
    try {
      if (keyString != null && !keyString.isBlank()) {
        KeyCombination combination = KeyCombination.valueOf(keyString);
        scene.getAccelerators().put(combination, action);
      }
    } catch (IllegalArgumentException e) {
      System.err.println("Raccourci clavier invalide ignoré : " + keyString);
    }
  }

  /**
   * Switches the GUI to online game mode using the provided game model.
   *
   * @param onlineGame the online game model received from the server
   */
  public void switchToOnlineGame(Game onlineGame) {
    gameInstance = onlineGame;
    onlineMode = true;

    // Disable useless button in network client mode
    controlPanel.getUndoButton().setDisable(true);
    controlPanel.getRedoButton().setDisable(true);
    controlPanel.getHintButton().setDisable(true);
    controlPanel.getPauseButton().setDisable(true);
    configurationMenuItem.setDisable(true);
    saveMenuItem.setDisable(true);
    loadMenuItem.setDisable(true);

    // We disable timer in client mode
    scorePanel.stopBlitzTimers();

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

    // Re enable theses button when exiting online client mode
    controlPanel.getUndoButton().setDisable(false);
    controlPanel.getRedoButton().setDisable(false);
    controlPanel.getHintButton().setDisable(false);
    controlPanel.getPauseButton().setDisable(false);
    configurationMenuItem.setDisable(false);
    saveMenuItem.setDisable(false);
    loadMenuItem.setDisable(false);
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
    System.out.println("Loading Gaddag for GUI (" + configuredLanguage + ")...");
    try {
      gaddag = GuiDictionaryLoader.load(getClass().getClassLoader(), configuredLanguage);
    } catch (IOException e) {
      showError(I18n.translate("scrabble.dictLoadError", e.getMessage()));
    }
  }

  private void submitPendingTiles() {
    if (pendingTiles.isEmpty()) {
      showError(I18n.translate("scrabble.needTile"));
      return;
    }

    Player tmp = gameInstance.getCurrentPlayer();
    Move move = PendingMoveBuilder.build(pendingTiles, tmp, gameInstance);
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
    recreateGameFromCurrentConfiguration(true);
  }

  private void recreateGameFromCurrentConfiguration(boolean askConfirmation) {
    if (askConfirmation
        && !messagePanel.showConfirmation(I18n.translate("scrabble.newGameConfirmation"))) {
      return;
    }

    ControllerConfigSnapshot configSnapshot = ControllerConfigSnapshot.capture(controller);

    Optional<Integer> countOpt = controller.configuredPlayerCount() > 0
        ? Optional.of(controller.configuredPlayerCount())
        : PlayerSetup.showDialog();
    if (countOpt.isEmpty()) {
      return;
    }

    // Nettoyage complet avant de recréer
    if (onlineMode) {
      networkBridge.dispose();
      networkManager = new NetworkManager();
      networkBridge = new NetworkGameBridge(networkManager);
      networkBridge.setGui(this);

      // We close this old network tab
      if (lobbyView != null) {
        lobbyView.close();
      }

      lobbyView = null;
      exitOnlineMode();
    }

    gameInstance = new Game(
      configSnapshot.superScrabbleMode() ? fr.ubordeaux.scrabble.model.enums.GameMode.SUPER
            : fr.ubordeaux.scrabble.model.enums.GameMode.STANDARD,
      configSnapshot.language());
    final int count = countOpt.get();

    if (configSnapshot.blitzMode()) {
      gameInstance.enableBlitzMode(
          java.time.Duration.ofMinutes(configSnapshot.blitzMinutes()));
    }

    configuredLanguage = configSnapshot.language();
    gaddag = null;
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
    configSnapshot.applyTo(controller);

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
    updatePauseAvailability();
    refreshBoard();
    refreshRack();
    refreshScores();
    checkAiTurn();
  }

  private void showConfigurationDialog() {
    Dialog<ButtonType> dialog = new Dialog<>();
    dialog.setTitle(I18n.translate("scrabble.config.dialog.title"));
    dialog.setHeaderText(I18n.translate("scrabble.config.dialogHeader"));

    DialogPane dialogPane = dialog.getDialogPane();
    ButtonType applyRestartButton = new ButtonType(
        I18n.translate("scrabble.config.dialog.applyRestart"),
        javafx.scene.control.ButtonBar.ButtonData.LEFT);
    dialogPane.getButtonTypes().addAll(ButtonType.OK, applyRestartButton, ButtonType.CANCEL);

    ChoiceBox<String> languageChoice = new ChoiceBox<>();
    languageChoice.getItems().addAll("en", "fr");
    languageChoice.setValue(controller.configuredLanguage());

    final Spinner<Integer> playerSpinner = new Spinner<>(
        new SpinnerValueFactory.IntegerSpinnerValueFactory(2, 8,
            Math.max(2, controller.configuredPlayerCount() > 0
                ? controller.configuredPlayerCount()
                : gameInstance.getPlayers().size())));

    CheckBox superScrabbleBox = new CheckBox();
    superScrabbleBox.setSelected(controller.configuredSuperMode());

    CheckBox blitzBox = new CheckBox();
    blitzBox.setSelected(controller.configuredBlitzMode());

    Spinner<Integer> blitzTimeoutSpinner = new Spinner<>(
        new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 120,
            controller.configuredBlitzMinutes()));
    blitzTimeoutSpinner.setEditable(true);
    blitzTimeoutSpinner.disableProperty().bind(blitzBox.selectedProperty().not());

    Spinner<Integer> aiTimeSpinner = new Spinner<>(
        new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 60,
            controller.configuredAiTime()));
    aiTimeSpinner.setEditable(true);

    CheckBox expectiminimaxBox = new CheckBox();
    expectiminimaxBox.setSelected(controller.isExpectiminimaxEnabled());

    CheckBox mlBox = new CheckBox();
    mlBox.setSelected(controller.isMlEnabled());

    CheckBox debugBox = new CheckBox();
    debugBox.setSelected(fr.ubordeaux.scrabble.model.utils.GameLogger.isDebug());

    CheckBox verboseBox = new CheckBox();
    verboseBox.setSelected(fr.ubordeaux.scrabble.model.utils.GameLogger.isVerbose());

    javafx.scene.control.TextField dictionaryField = new javafx.scene.control.TextField(
        controller.getDictionaryPathOverride());
    dictionaryField.setPrefColumnCount(28);

    GridPane grid = new GridPane();
    grid.setHgap(12);
    grid.setVgap(10);
    grid.setPadding(new Insets(12, 0, 0, 0));

    int row = 0;
    grid.addRow(row++,
        new Label(I18n.translate("scrabble.config.label.language")),
        languageChoice);
    grid.addRow(row++,
        new Label(I18n.translate("scrabble.config.label.players")),
        playerSpinner);
    grid.addRow(row++,
        new Label(I18n.translate("scrabble.config.label.super")),
        superScrabbleBox);
    grid.addRow(row++, new Label(I18n.translate("scrabble.config.label.blitz")), blitzBox);
    grid.addRow(row++,
        new Label(I18n.translate("scrabble.config.label.timeout")),
        blitzTimeoutSpinner);
    grid.addRow(row++, new Label(I18n.translate("scrabble.config.label.aitime")), aiTimeSpinner);
    grid.addRow(row++,
        new Label(I18n.translate("scrabble.config.label.expectiminimax")),
        expectiminimaxBox);
    grid.addRow(row++, new Label(I18n.translate("scrabble.config.label.ml")), mlBox);
    grid.addRow(row++,
        new Label(I18n.translate("scrabble.config.label.dictionary")),
        dictionaryField);
    grid.addRow(row++, new Label(I18n.translate("scrabble.config.label.debug")), debugBox);
    grid.addRow(row, new Label(I18n.translate("scrabble.config.label.verbose")), verboseBox);

    dialogPane.setContent(grid);

    Optional<ButtonType> result = dialog.showAndWait();
    if (result.isEmpty() || (result.get() != ButtonType.OK && result.get() != applyRestartButton)) {
      return;
    }

    applyConfigurationAssignments(String.join("; ",
        "language=" + languageChoice.getValue(),
        "players=" + playerSpinner.getValue(),
        "super-scrabble=" + superScrabbleBox.isSelected(),
        "blitz=" + blitzBox.isSelected(),
        "timeout=" + blitzTimeoutSpinner.getValue(),
        "ai-time=" + aiTimeSpinner.getValue(),
        "ai-exptiminimax=" + expectiminimaxBox.isSelected(),
        "ai-ml=" + mlBox.isSelected(),
        "dictionary=" + dictionaryField.getText().trim(),
        "debug=" + debugBox.isSelected(),
        "verbose=" + verboseBox.isSelected()));

    if (result.get() == applyRestartButton) {
      recreateGameFromCurrentConfiguration(false);
    }
  }

  private void applyConfigurationAssignments(String rawAssignments) {
    String[] assignments = rawAssignments.split("[;]");
    boolean blitzChanged = false;
    boolean languageChanged = false;
    boolean dictionaryChanged = false;

    for (String assignment : assignments) {
      String normalized = assignment.trim();
      if (normalized.isEmpty()) {
        continue;
      }

      String[] kv = normalized.split("=", 2);
      if (kv.length != 2) {
        showError(I18n.translate("scrabble.config.invalidEntry", normalized));
        return;
      }

      try {
        String key = kv[0].trim();
        controller.applyConfiguration(key, kv[1].trim());
        if (key.equalsIgnoreCase("blitz") || key.equalsIgnoreCase("timeout")) {
          blitzChanged = true;
        }
        if (key.equalsIgnoreCase("language") || key.equalsIgnoreCase("lang")) {
          languageChanged = true;
        }
        if (key.equalsIgnoreCase("dictionary") || key.equalsIgnoreCase("dictionary-path")) {
          dictionaryChanged = true;
        }
      } catch (IllegalArgumentException ex) {
        showError(ex.getMessage());
        return;
      }
    }

    if (languageChanged) {
      configuredLanguage = controller.configuredLanguage();
      refreshLocalizedTexts();
    }

    if (languageChanged || dictionaryChanged) {
      gaddag = null;
    }

    if (blitzChanged) {
      if (gameInstance.isBlitzModeEnabled()) {
        scorePanel.startBlitzTimers(gameInstance.getPlayers(),
            this::onBlitzTimeExpired);
      } else {
        scorePanel.stopBlitzTimers();
      }
    }

    updatePauseAvailability();
    refreshAll();
    showInfo(I18n.translate("scrabble.menu.configuration"),
        I18n.translate("scrabble.config.saved"));
  }

  private void refreshLocalizedTexts() {
    if (primaryStage != null) {
      primaryStage.setTitle(I18n.translate("scrabble.windowTitle"));
    }

    if (appMenuButton != null) {
      appMenuButton.setText(I18n.translate("scrabble.menuButton"));
    }

    if (newGameMenuItem != null && newGameButton != null) {
      String label = I18n.translate("scrabble.menu.newGame");
      newGameMenuItem.setText(label);
      newGameButton.setText(label);
    }
    if (onlineMenuItem != null && onlineButton != null) {
      String label = I18n.translate("scrabble.menu.multiplayer");
      onlineMenuItem.setText(label);
      onlineButton.setText(label);
    }
    if (saveMenuItem != null && saveButton != null) {
      String label = I18n.translate("scrabble.menu.save");
      saveMenuItem.setText(label);
      saveButton.setText(label);
    }
    if (loadMenuItem != null && loadButton != null) {
      String label = I18n.translate("scrabble.menu.load");
      loadMenuItem.setText(label);
      loadButton.setText(label);
    }
    if (configurationMenuItem != null && configurationButton != null) {
      String label = I18n.translate("scrabble.menu.configuration");
      configurationMenuItem.setText(label);
      configurationButton.setText(label);
    }
    if (infoMenuItem != null && infoButton != null) {
      String label = I18n.translate("scrabble.menu.info");
      infoMenuItem.setText(label);
      infoButton.setText(label);
    }
    if (quitMenuItem != null && quitButton != null) {
      String label = I18n.translate("scrabble.menu.quit");
      quitMenuItem.setText(label);
      quitButton.setText(label);
    }

    if (controlPanel != null) {
      controlPanel.getPlayButton().setText(I18n.translate("control.play"));
      controlPanel.getPassButton().setText(I18n.translate("control.pass"));
      controlPanel.getExchangeButton().setText(I18n.translate("control.exchange"));
      controlPanel.getCancelPlacementButton().setText(I18n.translate("control.cancelPlacement"));
      controlPanel.getUndoButton().setText(I18n.translate("control.undo"));
      controlPanel.getRedoButton().setText(I18n.translate("control.redo"));
      controlPanel.getHintButton().setText(I18n.translate("control.hint"));
      controlPanel.getPauseButton().setText(I18n.translate("control.pause"));
    }
  }

  private void updatePauseAvailability() {
    if (controlPanel == null || gameInstance == null) {
      return;
    }
    controlPanel.setPauseVisible(gameInstance.isBlitzModeEnabled());
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
    configurationMenuItem = new MenuItem(I18n.translate("scrabble.menu.configuration"));
    infoMenuItem = new MenuItem(I18n.translate("scrabble.menu.info"));
    quitMenuItem = new MenuItem(I18n.translate("scrabble.menu.quit"));

    appMenuButton = new MenuButton(I18n.translate("scrabble.menuButton"), null,
      newGameMenuItem, onlineMenuItem, saveMenuItem, loadMenuItem, configurationMenuItem,
      infoMenuItem, quitMenuItem);
    appMenuButton.setPrefWidth(190);
    appMenuButton.setStyle("-fx-background-color: #0B3D1D; -fx-text-fill: white;");

    newGameButton = createMenuActionButton(newGameMenuItem.getText());
    newGameButton.setOnAction(e -> newGameMenuItem.fire());
    onlineButton = createMenuActionButton(onlineMenuItem.getText());
    onlineButton.setOnAction(e -> onlineMenuItem.fire());
    saveButton = createMenuActionButton(saveMenuItem.getText());
    saveButton.setOnAction(e -> saveMenuItem.fire());
    saveButton.disableProperty().bind(saveMenuItem.disableProperty());
    loadButton = createMenuActionButton(loadMenuItem.getText());
    loadButton.setOnAction(e -> loadMenuItem.fire());
    loadButton.disableProperty().bind(loadMenuItem.disableProperty());
    configurationButton = createMenuActionButton(configurationMenuItem.getText());
    configurationButton.setOnAction(e -> configurationMenuItem.fire());
    configurationButton.disableProperty().bind(configurationMenuItem.disableProperty());
    infoButton = createMenuActionButton(infoMenuItem.getText());
    infoButton.setOnAction(e -> infoMenuItem.fire());
    quitButton = createMenuActionButton(quitMenuItem.getText());
    quitButton.setOnAction(e -> quitMenuItem.fire());

    VBox panel = new VBox(8, menuLabel, newGameButton, onlineButton, saveButton, loadButton,
        configurationButton, infoButton, quitButton);
    panel.setAlignment(Pos.TOP_CENTER);
    panel.setPadding(new Insets(15, 10, 10, 10));
    panel.setStyle("-fx-background-color: rgba(0,0,0,0.4); -fx-background-radius: 10;");
    panel.setPrefWidth(250);
    panel.setMaxHeight(Region.USE_PREF_SIZE);
    return panel;
  }

  private Button createMenuActionButton(String text) {
    Button button = new Button(text);
    button.setPrefWidth(220);
    button.setPrefHeight(38);
    button.setFont(Font.font("Arial", FontWeight.BOLD, 13));
    button.setStyle("-fx-background-color: #0B3D1D;" + "-fx-text-fill: white;"
        + "-fx-background-radius: 5;" + "-fx-cursor: hand;");
    button.setOnMouseEntered(e -> button.setOpacity(0.8));
    button.setOnMouseExited(e -> button.setOpacity(1.0));
    return button;
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
    Player current = gameInstance.getCurrentPlayer();
    if (current != null) {
      int idx = gameInstance.getPlayers().indexOf(current);
      if (idx >= 0) {
        rackPanel.setCurrentPlayerNumber(idx + 1);
      }
    }
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
   * Call when by observer reaction when a move send by this client is refused.
   *
   * @param reason the reason why the move was refused
   */
  public void handleMoveRefused(String reason) {
    showError(I18n.translate(reason));
    boardPanel.clearAllPending();
    pendingTiles.clear();
    refreshRack();
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

  /**
   * Entry point for the JavaFX application.
   *
   * @param args application arguments
   */
  public static void main(String[] args) {
    launch(args);
  }

  /**
   * Configure global keyboard shortcuts.
   *
   * @param scene main scene of the application.
   */
  private void setupShortcuts(Scene scene) {
    ConfigLoader config = new ConfigLoader();
    config.loadConfig();

    addDynamicShortcut(scene, config.getOption("bind-new", "Ctrl+N"),
        () -> newGameMenuItem.fire());
    addDynamicShortcut(scene, config.getOption("bind-load", "Ctrl+L"),
        () -> loadMenuItem.fire());
    addDynamicShortcut(scene, config.getOption("bind-save", "Ctrl+S"),
        () -> saveMenuItem.fire());
    addDynamicShortcut(scene, config.getOption("bind-quit", "Ctrl+Q"),
        () -> quitMenuItem.fire());

    addDynamicShortcut(scene, config.getOption("bind-undo", "Ctrl+U"),
        () -> controlPanel.getUndoButton().fire());
    addDynamicShortcut(scene, config.getOption("bind-redo", "Ctrl+R"),
        () -> controlPanel.getRedoButton().fire());
    addDynamicShortcut(scene, config.getOption("bind-hint", "Ctrl+H"),
        () -> controlPanel.getHintButton().fire());

    addDynamicShortcut(scene, config.getOption("bind-info", "Ctrl+I"),
        () -> showInfo("Informations", "Scrabble U-Bordeaux\nVersion 1.0\n"));

    addDynamicShortcut(scene, config.getOption("bind-pause", "Ctrl+P"),
        () -> {
          if (!onlineMode && !gameInstance.isGameOver()) {
            controller.togglePause();
          }
        });
  }

}