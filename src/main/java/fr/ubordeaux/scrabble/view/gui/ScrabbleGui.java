package fr.ubordeaux.scrabble.view.gui;

import fr.ubordeaux.scrabble.controller.GameController;
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
import java.util.ArrayList;
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

  private static Game gameInstance;
  private static JavaFxView viewInstance;

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

  public static void setGame(Game game) {
    gameInstance = game;
  }

  public static void setView(JavaFxView view) {
    viewInstance = view;
  }

  @Override
  public void start(Stage stage) {
    if (gameInstance == null) {
      throw new IllegalStateException(missingGameErrorMessage());
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
    root.setPadding(new Insets(rootPadding()));
    root.setStyle(rootBackgroundStyle());
    root.setCenter(boardPanel);
    root.setLeft(leftMenu);

    connectButtons();

    VBox right = new VBox(rightPanelSpacing());
    right.setAlignment(Pos.TOP_CENTER);
    right.setPadding(new Insets(rightTopPadding(), rightRightPadding(),
        rightBottomPadding(), rightLeftPadding()));
    right.getChildren().addAll(scorePanel, controlPanel);
    root.setRight(right);
    root.setBottom(rackPanel);

    stage.setOnCloseRequest(e -> networkBridge.dispose());
    stage.setTitle(windowTitleText());
    stage.setScene(new Scene(root, windowWidth(), windowHeight()));
    stage.setFullScreen(true);
    stage.show();

    controller.startGame();
    if (shouldStartBlitz(gameInstance.isBlitzModeEnabled())) {
      scorePanel.startBlitzTimers(gameInstance.getPlayers(), this::onBlitzTimeExpired);
    }
    refreshAll();
  }

  private void connectButtons() {
    controlPanel.getPlayButton().setOnAction(e -> {
      if (shouldIgnoreGameplayAction(gameInstance.isGameOver())) {
        return;
      }
      submitPendingTiles();
    });

    controlPanel.getPassButton().setOnAction(e -> {
      if (shouldIgnoreGameplayAction(gameInstance.isGameOver())) {
        return;
      }
      if (shouldPassThroughNetwork(onlineMode)) {
        networkManager.pass();
      } else {
        controller.handlePlayerMove(Move.createPass(gameInstance.getCurrentPlayer()));
      }
    });

    controlPanel.getExchangeButton().setOnAction(e -> {
      if (shouldIgnoreGameplayAction(gameInstance.isGameOver())) {
        return;
      }
      openExchangeDialog();
    });
    controlPanel.getCancelPlacementButton().setOnAction(e -> {
      if (shouldIgnoreGameplayAction(gameInstance.isGameOver())) {
        return;
      }
      cancelPendingTiles();
    });
    controlPanel.getUndoButton().setOnAction(e -> {
      if (canUseUndoRedo(onlineMode, gameInstance.isGameOver())) {
        controller.undo();
      }
    });
    controlPanel.getRedoButton().setOnAction(e -> {
      if (canUseUndoRedo(onlineMode, gameInstance.isGameOver())) {
        controller.redo();
      }
    });
    controlPanel.getHelpButton().setOnAction(e ->
        showInfo(helpDialogTitle(), helpDialogMessage()));

    newGameMenuItem.setOnAction(e -> handleNewGame());
    onlineMenuItem.setOnAction(e -> openNetworkLobby());
    saveMenuItem.setOnAction(e -> showInfo(comingSoonTitle(), saveComingSoonMessage()));
    loadMenuItem.setOnAction(e -> showInfo(comingSoonTitle(), loadComingSoonMessage()));
    quitMenuItem.setOnAction(e -> {
      if (messagePanel.showConfirmation(quitConfirmationMessage())) {
        networkBridge.dispose();
        Platform.exit();
      }
    });
  }

  private void openNetworkLobby() {
    if (shouldOpenNetworkLobby(lobbyView)) {
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
    showInfo(onlineStartedTitle(), onlineStartedMessage());
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
    if (shouldIgnoreTileDrop(currentlyDraggedTile, gameInstance.isGameOver())) {
      return;
    }

    Point point = new Point(col, row);
    if (isOccupiedOrPending(gameInstance, pendingTiles, point)) {
      showError(occupiedCellMessage());
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
    System.out.println("Chargement du Gaddag pour l'interface graphique...");
    try (InputStream is =
            getClass().getClassLoader().getResourceAsStream("dictionaries/lexicon_en.txt")) {
      if (is != null) {
        try (BufferedReader br = new BufferedReader(new InputStreamReader(is))) {
          String line;
          while ((line = br.readLine()) != null) {
            String normalized = normalizedDictionaryLine(line);
            if (shouldAddDictionaryEntry(normalized)) {
              gaddag.add(normalized);
            }
          }
        }
      }
    } catch (IOException e) {
      showError(dictionaryLoadErrorMessage(e.getMessage()));
    }
  }

  private void submitPendingTiles() {
    if (shouldRejectSubmitWhenNoPending(pendingTiles)) {
      showError(placeAtLeastOneTileMessage());
      return;
    }

    Move move = PendingMoveBuilder.build(pendingTiles, gameInstance.getCurrentPlayer());
    if (shouldRejectSubmitWhenMoveNull(move)) {
      showError(invalidAlignmentMessage());
      cancelPendingTiles();
      return;
    }

    if (onlineMode) {
      // En mode online : on envoie directement au serveur, c'est lui qui valide
      String dir = moveDirectionToken(move);
      String word = buildPlayedWord(move);
      networkManager.play(moveOriginX(move), moveOriginY(move), dir, word);
      pendingTiles.clear();
    } else {
      // En mode local : on valide via le controller
      try {
        controller.handlePlayerMove(move);
        pendingTiles.clear();
      } catch (RuntimeException e) {
        showError(invalidMoveMessage(e.getMessage()));
        cancelPendingTiles();
      }
    }
  }

  private void openExchangeDialog() {
    if (shouldBlockExchangeWhilePending(pendingTiles)) {
      showError(cancelTilesBeforeExchangeMessage());
      return;
    }

    TextInputDialog dialog = new TextInputDialog();
    dialog.setTitle(exchangeDialogTitle());
    dialog.setHeaderText(exchangeDialogHeaderText());
    dialog.setContentText(exchangeDialogContentText());

    Optional<String> result = dialog.showAndWait();
    result.ifPresent(input -> {
      String letters = normalizeExchangeLetters(input);
      if (shouldSkipExchange(letters)) {
        return;
      }

      if (onlineMode) {
        networkManager.exchange(letters);
      } else {
        Move move = ExchangeMoveBuilder.build(letters, gameInstance.getCurrentPlayer());
        if (move == null) {
          showError(exchangeLettersNotInRackMessage());
          return;
        }
        controller.handlePlayerMove(move);
      }
    });
  }

  private void cancelPendingTiles() {
    if (shouldCancelWhenPendingEmpty(pendingTiles)) {
      return;
    }
    pendingTiles.forEach((p, t) -> boardPanel.clearTile(p.getY(), p.getX()));
    pendingTiles.clear();
    refreshRack();
  }

  private void handleNewGame() {
    if (shouldAbortNewGame(messagePanel.showConfirmation(newGameConfirmationMessage()))) {
      return;
    }

    Optional<Integer> countOpt = PlayerSetup.showDialog();
    if (shouldAbortWhenMissingPlayerCount(countOpt)) {
      return;
    }

    // Nettoyage complet avant de recréer
    if (shouldReinitializeNetworkForNewGame(onlineMode)) {
      networkBridge.dispose();
      networkManager = new NetworkManager();
      networkBridge = new NetworkGameBridge(networkManager);
      networkBridge.setGui(this);
      lobbyView = null;
      onlineMode = false;
    }

    gameInstance = new Game();
    int count = selectedPlayerCount(countOpt);

    if (shouldLoadGaddag(gaddag)) {
      loadDictionary();
    }
    createDefaultPlayers(count).forEach(gameInstance::addPlayer);

    viewInstance = new JavaFxView(gameInstance);
    viewInstance.setGui(this);
    controller = new GameController(gameInstance, viewInstance);

    setGameplayControlsDisabled(false);

    boardPanel.clearAllPending();
    pendingTiles.clear();
    boardPanel.setBoard(gameInstance.getBoard());

    // Démarrer la partie AVANT de rafraîchir l'affichage
    controller.startGame();
    if (shouldStartBlitz(gameInstance.isBlitzModeEnabled())) {
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
    findOutOfTimePlayerName(gameInstance.getPlayers())
        .ifPresent(name -> showInfo(blitzTimeoutTitle(), buildBlitzTimeoutMessage(name)));
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
    if (shouldRunAiTurn(current, gameInstance.isGameOver())) {
      if (shouldLoadDictionaryForAi(gaddag)) {
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
          Platform.runLater(() -> showError(aiErrorMessage(e.getMessage())));
          Platform.runLater(() -> controller.handlePlayerMove(Move.createPass(ai)));
        } finally {
          Platform.runLater(() -> {
            if (shouldKeepGameplayDisabledAfterAi(gameInstance.isGameOver())) {
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
    Label menuLabel = new Label(menuTitleText());
    menuLabel.setTextFill(Color.WHITE);
    menuLabel.setFont(Font.font(menuLabelFontFamily(), FontWeight.BOLD, menuLabelFontSize()));

    newGameMenuItem = new MenuItem(newGameMenuText());
    onlineMenuItem = new MenuItem(multiplayerMenuText());
    saveMenuItem = new MenuItem(saveMenuText());
    loadMenuItem = new MenuItem(loadMenuText());
    quitMenuItem = new MenuItem(quitMenuText());

    appMenuButton = new MenuButton(appMenuButtonText(), null,
        newGameMenuItem, onlineMenuItem, saveMenuItem, loadMenuItem, quitMenuItem);
    appMenuButton.setPrefWidth(appMenuButtonWidth());
    appMenuButton.setStyle(appMenuButtonStyle());

    VBox left = new VBox(leftMenuSpacing(), menuLabel, appMenuButton);
    left.setAlignment(Pos.TOP_LEFT);
    left.setPadding(new Insets(leftMenuTopPadding(), leftMenuRightPadding(),
          leftMenuBottomPadding(), leftMenuLeftPadding()));
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
    if (shouldSkipScoreRefresh(players)) {
      return;
    }
    String[] names = toPlayerNames(players);
    int[] scores = toPlayerScores(players);
    scorePanel.updateScores(names, scores);
    scorePanel.updateBagInfo(gameInstance.getBag().size());
    int idx = indexOfCurrentPlayer(players, gameInstance.getCurrentPlayer());
    if (shouldHighlightScoreIndex(idx)) {
      scorePanel.highlightCurrentPlayer(idx, names[idx]);
    }
  }

  public void showInfo(String title, String message) {
    messagePanel.showInfo(title, message);
  }

  public void showError(String message) {
    messagePanel.showError(message);
  }

  private Rack getCurrentRack() {
    Player p = gameInstance.getCurrentPlayer();
    return p != null ? p.getRack() : new Rack();
  }

  public static void main(String[] args) {
    launch(args);
  }

  static boolean isOccupiedOrPending(Game game, Map<Point, Tile> pending, Point point) {
    return !game.getBoard().getSquare(point).isEmpty() || pending.containsKey(point);
  }

  static String buildPlayedWord(Move move) {
    return move.getTiles().stream().map(t -> String.valueOf(t.getCharacter())).reduce("",
        String::concat);
  }

  static boolean canUseUndoRedo(boolean isOnlineMode, boolean isGameOver) {
    return !isOnlineMode && !isGameOver;
  }

  static boolean shouldIgnoreTileDrop(Tile draggedTile, boolean isGameOver) {
    return draggedTile == null || isGameOver;
  }

  static String normalizeExchangeLetters(String input) {
    return input.trim().toUpperCase();
  }

  static boolean shouldSkipExchange(String letters) {
    return letters.isEmpty();
  }

  static boolean shouldRunAiTurn(Player current, boolean isGameOver) {
    return current instanceof AiPlayer && !isGameOver;
  }

  static boolean shouldIgnoreGameplayAction(boolean isGameOver) {
    return isGameOver;
  }

  static boolean shouldPassThroughNetwork(boolean isOnlineMode) {
    return isOnlineMode;
  }

  static boolean shouldOpenNetworkLobby(NetworkLobbyView currentLobbyView) {
    return currentLobbyView == null;
  }

  static String normalizedDictionaryLine(String line) {
    return line.trim();
  }

  static boolean shouldAddDictionaryEntry(String normalizedLine) {
    return !normalizedLine.isEmpty();
  }

  static boolean shouldLoadDictionaryForAi(Gaddag current) {
    return current == null;
  }

  static boolean shouldSkipScoreRefresh(List<Player> players) {
    return players.isEmpty();
  }

  static boolean shouldHighlightScoreIndex(int idx) {
    return idx >= 0;
  }

  static boolean shouldKeepGameplayDisabledAfterAi(boolean isGameOver) {
    return isGameOver;
  }

  static String helpDialogTitle() {
    return "Help";
  }

  static String helpDialogMessage() {
    return "Fonction non implémentée pour le moment.";
  }

  static String comingSoonTitle() {
    return "À venir";
  }

  static String saveComingSoonMessage() {
    return "Sauvegarde bientôt disponible.";
  }

  static String loadComingSoonMessage() {
    return "Chargement bientôt disponible.";
  }

  static String quitConfirmationMessage() {
    return "Voulez-vous vraiment quitter ?";
  }

  static String onlineStartedTitle() {
    return "Partie en ligne";
  }

  static String onlineStartedMessage() {
    return "La partie a commencé ! Bonne chance 🎮";
  }

  static String occupiedCellMessage() {
    return "Cette case est déjà occupée !";
  }

  static String dictionaryLoadErrorMessage(String details) {
    return "Impossible de charger le dictionnaire : " + details;
  }

  static String placeAtLeastOneTileMessage() {
    return "Placez au moins une tuile avant de valider !";
  }

  static String invalidAlignmentMessage() {
    return "Les tuiles doivent être alignées horizontalement ou verticalement !";
  }

  static String invalidMoveMessage(String details) {
    return "Coup invalide : " + details;
  }

  static String cancelTilesBeforeExchangeMessage() {
    return "Annulez d'abord les tuiles placées (bouton ↩).";
  }

  static String exchangeLettersNotInRackMessage() {
    return "Certaines lettres ne sont pas dans votre chevalet !";
  }

  static String newGameConfirmationMessage() {
    return "Abandonner la partie en cours et recommencer ?";
  }

  static String menuTitleText() {
    return "MENU";
  }

  static String appMenuButtonText() {
    return "☰ Jeu";
  }

  static String newGameMenuText() {
    return "Nouvelle partie";
  }

  static String multiplayerMenuText() {
    return "Multijoueur";
  }

  static String saveMenuText() {
    return "Sauvegarder";
  }

  static String loadMenuText() {
    return "Charger";
  }

  static String quitMenuText() {
    return "Quitter";
  }

  static String missingGameErrorMessage() {
    return "Appelez ScrabbleGui.setGame() avant de lancer.";
  }

  static double rootPadding() {
    return 10;
  }

  static String rootBackgroundStyle() {
    return "-fx-background-color: #115829;";
  }

  static double rightPanelSpacing() {
    return 15;
  }

  static double rightTopPadding() {
    return 0;
  }

  static double rightRightPadding() {
    return 0;
  }

  static double rightBottomPadding() {
    return 0;
  }

  static double rightLeftPadding() {
    return 15;
  }

  static String windowTitleText() {
    return "Scrabble U-Bordeaux";
  }

  static int windowWidth() {
    return 1200;
  }

  static int windowHeight() {
    return 800;
  }

  static String exchangeDialogTitle() {
    return "Échanger des lettres";
  }

  static String exchangeDialogHeaderText() {
    return "Lettres de votre chevalet à échanger";
  }

  static String exchangeDialogContentText() {
    return "Lettres (ex: ABC) :";
  }

  static String aiErrorMessage(String details) {
    return "Erreur IA : " + details;
  }

  static double appMenuButtonWidth() {
    return 190;
  }

  static String appMenuButtonStyle() {
    return "-fx-background-color: #0B3D1D; -fx-text-fill: white;";
  }

  static double leftMenuSpacing() {
    return 8;
  }

  static double leftMenuTopPadding() {
    return 8;
  }

  static double leftMenuRightPadding() {
    return 15;
  }

  static double leftMenuBottomPadding() {
    return 0;
  }

  static double leftMenuLeftPadding() {
    return 0;
  }

  static String defaultPlayerName(int oneBasedIndex) {
    return "Joueur" + oneBasedIndex;
  }

  static Optional<String> findOutOfTimePlayerName(List<Player> players) {
    return players.stream()
        .filter(p -> p.isBlitzClockEnabled() && p.isOutOfTime())
        .map(Player::getName)
        .findFirst();
  }

  static String buildBlitzTimeoutMessage(String playerName) {
    return playerName + " a épuisé son temps. La partie est terminée !";
  }

  static String blitzTimeoutTitle() {
    return "⏱ Temps écoulé !";
  }

  static String menuLabelFontFamily() {
    return "Arial";
  }

  static int menuLabelFontSize() {
    return 14;
  }

  static String[] toPlayerNames(List<Player> players) {
    return players.stream().map(Player::getName).toArray(String[]::new);
  }

  static int[] toPlayerScores(List<Player> players) {
    return players.stream().mapToInt(Player::getScore).toArray();
  }

  static int indexOfCurrentPlayer(List<Player> players, Player current) {
    if (current == null) {
      return -1;
    }
    return players.indexOf(current);
  }

  static boolean shouldRejectSubmitWhenNoPending(Map<Point, Tile> pending) {
    return pending.isEmpty();
  }

  static boolean shouldRejectSubmitWhenMoveNull(Move move) {
    return move == null;
  }

  static int moveOriginX(Move move) {
    return move.getStartPosition().getX();
  }

  static int moveOriginY(Move move) {
    return move.getStartPosition().getY();
  }

  static String moveDirectionToken(Move move) {
    return move.getDirection().name().substring(0, 1);
  }

  static boolean shouldBlockExchangeWhilePending(Map<Point, Tile> pending) {
    return !pending.isEmpty();
  }

  static boolean shouldCancelWhenPendingEmpty(Map<Point, Tile> pending) {
    return pending.isEmpty();
  }

  static boolean shouldAbortNewGame(boolean confirmed) {
    return !confirmed;
  }

  static boolean shouldAbortWhenMissingPlayerCount(Optional<Integer> countOpt) {
    return countOpt.isEmpty();
  }

  static boolean shouldReinitializeNetworkForNewGame(boolean isOnlineMode) {
    return isOnlineMode;
  }

  static int selectedPlayerCount(Optional<Integer> countOpt) {
    return countOpt.orElse(0);
  }

  static boolean shouldLoadGaddag(Gaddag current) {
    return current == null;
  }

  static List<HumanPlayer> createDefaultPlayers(int count) {
    List<HumanPlayer> players = new ArrayList<>();
    for (int i = 1; i <= count; i++) {
      players.add(new HumanPlayer(defaultPlayerName(i), PlayerColor.fromIndex(i - 1)));
    }
    return players;
  }

  static boolean shouldStartBlitz(boolean isBlitzModeEnabled) {
    return isBlitzModeEnabled;
  }
}