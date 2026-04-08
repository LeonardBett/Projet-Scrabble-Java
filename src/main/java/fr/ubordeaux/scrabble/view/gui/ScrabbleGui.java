package fr.ubordeaux.scrabble.view.gui;

import fr.ubordeaux.scrabble.controller.GameController;
import fr.ubordeaux.scrabble.controller.network.NetworkLobbyController;
import fr.ubordeaux.scrabble.i18n.I18n;
import fr.ubordeaux.scrabble.model.core.Game;
import fr.ubordeaux.scrabble.model.core.Move;
import fr.ubordeaux.scrabble.model.core.Rack;
import fr.ubordeaux.scrabble.model.core.Tile;
import fr.ubordeaux.scrabble.model.interfaces.Player;
import fr.ubordeaux.scrabble.model.network.NetworkManager;
import fr.ubordeaux.scrabble.model.utils.GameLogger;
import fr.ubordeaux.scrabble.model.utils.Point;
import fr.ubordeaux.scrabble.view.gui.main.ScrabbleGuiConfigDialog;
import fr.ubordeaux.scrabble.view.gui.main.ScrabbleGuiRefresh;
import fr.ubordeaux.scrabble.view.gui.network.NetworkGameBridge;
import fr.ubordeaux.scrabble.view.gui.network.NetworkLobbyView;
import fr.ubordeaux.scrabble.view.gui.panel.BoardPanel;
import fr.ubordeaux.scrabble.view.gui.panel.ControlPanel;
import fr.ubordeaux.scrabble.view.gui.panel.MessagePanel;
import fr.ubordeaux.scrabble.view.gui.panel.RackPanel;
import fr.ubordeaux.scrabble.view.gui.panel.ScorePanel;
import fr.ubordeaux.scrabble.view.optionlancement.GuiLauncher;
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
import javafx.scene.control.TextField;
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
  private NetworkLobbyController networkController;
  private NetworkGameBridge networkBridge;
  private NetworkLobbyView lobbyView;

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

  private final ScrabbleGuiConfigDialog configDialogDelegate = new ScrabbleGuiConfigDialog();
  private final ScrabbleGuiRefresh refreshDelegate = new ScrabbleGuiRefresh();

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
    startUi(stage);
  }

  /**
   * Runs the JavaFX startup lifecycle.
   *
   * @param stage JavaFX stage
   */
  public void startUi(Stage stage) {
    if (gameInstance == null) {
      gameInstance = GuiLauncher.createGameFromConfig();
    }

    networkManager = new NetworkManager();
    networkController = new NetworkLobbyController(networkManager);
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
      if (controller.isOnlineMode()) {
        networkController.pass();
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
      if (!controller.isOnlineMode() && !gameInstance.isGameOver()) {
        controller.undo();
      }
    });
    controlPanel.getRedoButton().setOnAction(e -> {
      if (!controller.isOnlineMode() && !gameInstance.isGameOver()) {
        controller.redo();
      }
    });
    controlPanel.getHintButton().setOnAction(e -> {
      if (!controller.isOnlineMode() && !gameInstance.isGameOver()) {
        controller.provideHint();
      }
    });
    controlPanel.getPauseButton().setOnAction(e -> {
      if (!controller.isOnlineMode() && !gameInstance.isGameOver()) {
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
        controller.saveGameToPath(file.getAbsolutePath());
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
        loadedGame = controller.loadGameFromPath(file.getAbsolutePath());
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
   * Binds a keyboard shortcut to a visible menu item and updates its display text.
   *
   * @param scene     the main application scene
   * @param keyString the shortcut string (e.g., "Ctrl+N")
   * @param menuItem  the menu item to bind and update
   */
  private void addMenuShortcut(Scene scene, String keyString, MenuItem menuItem) {
    try {
      if (keyString != null && !keyString.isBlank()) {
        String formattedKey = keyString.replaceAll("\\s+", "").toUpperCase();
        KeyCombination combination = KeyCombination.valueOf(formattedKey);
        menuItem.setAccelerator(combination);
        scene.getAccelerators().put(combination, menuItem::fire);
      }
    } catch (Exception e) {
      GameLogger.logError("Invalid shortcut ignored : " + keyString, e);
    }
  }


  /**
   * Binds a keyboard shortcut to a background action (invisible in menus).
   *
   * @param scene     the main application scene
   * @param keyString the shortcut string (e.g., "P" or "Ctrl+P")
   * @param action    the runnable action to execute
   */
  private void addActionShortcut(Scene scene, String keyString, Runnable action) {
    try {
      if (keyString != null && !keyString.isBlank()) {
        String formattedKey = keyString.replaceAll("\\s+", "").toUpperCase();
        KeyCombination combination = KeyCombination.valueOf(formattedKey);
        scene.getAccelerators().put(combination, action);
      }
    } catch (Exception e) {
      GameLogger.logError("Invalid shortcut ignonred : " + keyString, e);
    }
  }

  /**
   * Switches the GUI to online game mode using the provided game model.
   *
   * @param onlineGame the online game model received from the server
   */
  public void switchToOnlineGame(Game onlineGame) {
    // Delegate game state transition to controller
    controller.switchToOnlineMode(onlineGame);
    gameInstance = onlineGame;

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
    // Delegate game state transition to controller
    controller.exitOnlineMode();
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
    return controller != null && controller.isOnlineMode();
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
    GameController.TileDropAnalysis analysis = controller.analyzeTileDrop(
        currentlyDraggedTile,
        row,
        col,
        pendingTiles);
    if (!analysis.accepted()) {
      if (analysis.errorI18nKey() != null) {
        showError(I18n.translate(analysis.errorI18nKey()));
      }
      currentlyDraggedTile = null;
      return;
    }

    Tile tileToPlace = analysis.tile();

    if (analysis.needsJokerResolution()) {
      if (!Platform.isFxApplicationThread()) {
        currentlyDraggedTile = null;
        return;
      }

      TextInputDialog dialog = new TextInputDialog();
      dialog.setTitle("Joker");
      dialog.setHeaderText("You played a joker!");
      dialog.setContentText("Which character would you like to place ? (A-Z) :");

      Optional<String> result = dialog.showAndWait();

      Optional<Tile> resolved = controller.resolveDroppedTile(tileToPlace,
          result.isPresent() ? result.get() : null);
      if (resolved.isEmpty()) {
        currentlyDraggedTile = null;
        return;
      }
      tileToPlace = resolved.get();
    }

    pendingTiles.put(analysis.point(), tileToPlace);

    boardPanel.placeTile(row, col, tileToPlace.getCharacter(), tileToPlace.getValue());

    rackPanel.hideTile(currentlyDraggedTile);
    currentlyDraggedTile = null;
  }

  private void submitPendingTiles() {
    GameController.PendingMoveSubmitResult result = controller.submitPendingMove(pendingTiles);
    switch (result.status()) {
      case EMPTY -> showError(I18n.translate("scrabble.needTile"));
      case INVALID_ALIGNMENT -> {
        showError(I18n.translate("scrabble.invalidAlignment"));
        cancelPendingTiles();
      }
      case ONLINE_READY -> {
        GameController.NetworkPlayPayload payload = result.payload();
        networkController.play(payload.x(), payload.y(), payload.direction(), payload.word());
        pendingTiles.clear();
      }
      case LOCAL_APPLIED -> pendingTiles.clear();
      case LOCAL_REJECTED -> {
        showError(I18n.translate("scrabble.invalidMove", result.errorMessage()));
        cancelPendingTiles();
      }
      default -> {
        // All enum values are handled above; this keeps checkstyle satisfied.
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

      if (controller.isOnlineMode()) {
        networkController.exchange(letters);
      } else {
        Optional<Move> moveOpt = controller.buildExchangeMoveFromLetters(letters);
        if (moveOpt.isEmpty()) {
          showError(I18n.translate("scrabble.exchange.invalidRack"));
          return;
        }
        controller.handlePlayerMove(moveOpt.get());
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

    Optional<Integer> selectedCount = controller.configuredPlayerCount() > 0
        ? Optional.empty()
        : PlayerSetup.showDialog();
    if (controller.resolveNewGamePlayerCount(selectedCount).isEmpty()) {
      return;
    }

    // Nettoyage complet avant de recréer
    if (controller.isOnlineMode()) {
      networkBridge.dispose();
      networkManager = new NetworkManager();
      networkController = new NetworkLobbyController(networkManager);
      networkBridge = new NetworkGameBridge(networkManager);
      networkBridge.setGui(this);

      // We close this old network tab
      if (lobbyView != null) {
        lobbyView.close();
      }

      lobbyView = null;
      exitOnlineMode();
    }

    Optional<Game> recreated = controller.recreateConfiguredGameFromSelection(selectedCount);
    if (recreated.isEmpty()) {
      return;
    }
    gameInstance = recreated.get();

    boardPanel.clearAllPending();
    pendingTiles.clear();
    boardPanel.setBoard(gameInstance.getBoard());

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
    Optional<Player> expired = controller.handleBlitzTimeout();
    setGameplayControlsDisabled(true);
    expired.ifPresent(p -> showInfo(I18n.translate("scrabble.blitzTimeoutTitle"),
        I18n.translate("scrabble.blitzTimeoutMessage", p.getName())));
    refreshScores();
  }

  /**
   * Refreshes all GUI panels: board, rack, scores, and checks if it is the AI's turn.
   */
  public void refreshAll() {
    refreshDelegate.refreshAll(this);
  }

  private void showConfigurationDialog() {
    configDialogDelegate.showDialog(
        controller,
        gameInstance,
        this::applyConfigurationAssignments,
        () -> recreateGameFromCurrentConfiguration(false));
  }

  private void applyConfigurationAssignments(String rawAssignments) {
    GameController.ConfigurationApplySummary summary;
    try {
      summary = controller.applyConfigurationAssignments(rawAssignments);
    } catch (IllegalArgumentException ex) {
      showError(ex.getMessage());
      return;
    }

    if (summary.languageChanged()) {
      configuredLanguage = controller.configuredLanguage();
      refreshLocalizedTexts();
    }

    if (summary.blitzChanged()) {
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
    if (controller.shouldPerformAiTurn()) {
      // Disable board during AI turn
      boardPanel.setDisable(true);
      rackPanel.setDisable(true);
      controlPanel.setGameplayButtonsDisabled(true);

      // Delegate AI execution to controller with callback for UI updates
      controller.performAiTurn(() -> Platform.runLater(() -> {
        if (gameInstance.isGameOver()) {
          setGameplayControlsDisabled(true);
        } else {
          boardPanel.setDisable(false);
          rackPanel.setDisable(false);
          controlPanel.setGameplayButtonsDisabled(false);
        }
        refreshAll();
      }));
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
    refreshDelegate.refreshBoard(this);
  }

  /**
   * Refreshes the rack panel for the current player.
   */
  public void refreshRack() {
    refreshDelegate.refreshRack(this);
  }

  /**
   * Refreshes the score panel with updated player scores and bag info.
   */
  public void refreshScores() {
    refreshDelegate.refreshScores(this);
  }

  /**
   * Exposes board panel for helper delegates.
   *
   * @return board panel
   */
  public BoardPanel getBoardPanel() {
    return boardPanel;
  }

  /**
   * Exposes rack panel for helper delegates.
   *
   * @return rack panel
   */
  public RackPanel getRackPanel() {
    return rackPanel;
  }

  /**
   * Exposes score panel for helper delegates.
   *
   * @return score panel
   */
  public ScorePanel getScorePanel() {
    return scorePanel;
  }

  /**
   * Exposes current game instance for helper delegates.
   *
   * @return game instance
   */
  public Game getGameInstance() {
    return gameInstance;
  }

  /**
   * Exposes pause availability update for refresh helper.
   */
  public void syncPauseAvailability() {
    updatePauseAvailability();
  }

  /**
   * Exposes AI turn check for refresh helper.
   */
  public void checkAiTurnIfNeeded() {
    checkAiTurn();
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
   * Configures global keyboard shortcuts from the configuration file.
   *
   * @param scene the main application scene
   */
  private void setupShortcuts(Scene scene) {
    scene.getAccelerators().put(new KeyCodeCombination(KeyCode.COMMA, KeyCombination.CONTROL_DOWN),
        this::showConfigurationDialog);

    addMenuShortcut(scene, controller.getConfigOption("bind-new", "Ctrl+N"), newGameMenuItem);
    addMenuShortcut(scene, controller.getConfigOption("bind-load", "Ctrl+L"), loadMenuItem);
    addMenuShortcut(scene, controller.getConfigOption("bind-save", "Ctrl+S"), saveMenuItem);
    addMenuShortcut(scene, controller.getConfigOption("bind-quit", "Ctrl+Q"), quitMenuItem);

    addActionShortcut(scene, controller.getConfigOption("bind-undo", "Ctrl+U"),
        () -> controlPanel.getUndoButton().fire());
    addActionShortcut(scene, controller.getConfigOption("bind-redo", "Ctrl+R"),
        () -> controlPanel.getRedoButton().fire());
    addActionShortcut(scene, controller.getConfigOption("bind-hint", "Ctrl+H"),
        () -> controlPanel.getHintButton().fire());
    addActionShortcut(scene, controller.getConfigOption("bind-info", "Ctrl+I"),
        () -> showInfo("Infos", "Scrabble v1.0"));

    addActionShortcut(scene, controller.getConfigOption("bind-pause", "Ctrl+P"), () -> {
      if (!controller.isOnlineMode() && !gameInstance.isGameOver()) {
        controller.togglePause();
      }
    });
  }

}
