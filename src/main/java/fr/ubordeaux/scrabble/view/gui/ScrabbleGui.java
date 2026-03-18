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
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.TextInputDialog;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
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

  public static void setGame(Game game) {
    gameInstance = game;
  }

  public static void setView(JavaFxView view) {
    viewInstance = view;
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
    connectButtons();

    BorderPane root = new BorderPane();
    root.setPadding(new Insets(10));
    root.setStyle("-fx-background-color: #115829;");
    root.setCenter(boardPanel);

    VBox right = new VBox(15);
    right.setAlignment(Pos.TOP_CENTER);
    right.setPadding(new Insets(0, 0, 0, 15));
    right.getChildren().addAll(scorePanel, controlPanel);
    root.setRight(right);
    root.setBottom(rackPanel);

    stage.setOnCloseRequest(e -> networkBridge.dispose());
    stage.setTitle("Scrabble U-Bordeaux");
    stage.setScene(new Scene(root, 1200, 800));
    stage.setFullScreen(true);
    stage.show();

    controller.startGame();
    refreshAll();
  }

  private void connectButtons() {
    controlPanel.getPlayButton().setOnAction(e -> submitPendingTiles());

    controlPanel.getPassButton().setOnAction(e -> {
      if (onlineMode) {
        networkManager.pass();
      } else {
        controller.handlePlayerMove(Move.createPass(gameInstance.getCurrentPlayer()));
      }
    });

    controlPanel.getExchangeButton().setOnAction(e -> openExchangeDialog());
    controlPanel.getCancelPlacementButton().setOnAction(e -> cancelPendingTiles());
    controlPanel.getUndoButton().setOnAction(e -> {
      if (!onlineMode) {
        controller.undo();
      }
    });
    controlPanel.getRedoButton().setOnAction(e -> {
      if (!onlineMode) {
        controller.redo();
      }
    });
    controlPanel.getOnlineButton().setOnAction(e -> openNetworkLobby());
    controlPanel.getNewGameButton().setOnAction(e -> handleNewGame());

    controlPanel.getSaveButton()
        .setOnAction(e -> showInfo("À venir", "Sauvegarde bientôt disponible."));
    controlPanel.getLoadButton()
        .setOnAction(e -> showInfo("À venir", "Chargement bientôt disponible."));

    controlPanel.getQuitButton().setOnAction(e -> {
      if (messagePanel.showConfirmation("Voulez-vous vraiment quitter ?")) {
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
    showInfo("Partie en ligne", "La partie a commencé ! Bonne chance 🎮");
  }

  /**
   * Exits online mode and re-enables undo/redo buttons.
   */
  public void exitOnlineMode() {
    onlineMode = false;
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
    if (currentlyDraggedTile == null) {
      return;
    }

    Point point = new Point(col, row);
    if (!gameInstance.getBoard().getSquare(point).isEmpty() || pendingTiles.containsKey(point)) {
      showError("Cette case est déjà occupée !");
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
    try {
      InputStream is =
          getClass().getClassLoader().getResourceAsStream("dictionaries/lexicon_en.txt");
      if (is != null) {
        BufferedReader br = new BufferedReader(new InputStreamReader(is));
        String line;
        while ((line = br.readLine()) != null) {
          if (!line.trim().isEmpty()) {
            gaddag.add(line.trim());
          }
        }
        br.close();
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  private void submitPendingTiles() {
    if (pendingTiles.isEmpty()) {
      showError("Placez au moins une tuile avant de valider !");
      return;
    }

    Move move = PendingMoveBuilder.build(pendingTiles, gameInstance.getCurrentPlayer());
    if (move == null) {
      showError("Les tuiles doivent être alignées horizontalement ou verticalement !");
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
        showError("Coup invalide : " + e.getMessage());
        cancelPendingTiles();
      }
    }
  }

  private void openExchangeDialog() {
    if (!pendingTiles.isEmpty()) {
      showError("Annulez d'abord les tuiles placées (bouton ↩).");
      return;
    }

    TextInputDialog dialog = new TextInputDialog();
    dialog.setTitle("Échanger des lettres");
    dialog.setHeaderText("Lettres de votre chevalet à échanger");
    dialog.setContentText("Lettres (ex: ABC) :");

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
          showError("Certaines lettres ne sont pas dans votre chevalet !");
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
    if (!messagePanel.showConfirmation("Abandonner la partie en cours et recommencer ?")) {
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
    int playerIndex = 0;
    for (String name : namesOpt.get()) {
      PlayerColor assignedColor = PlayerColor.fromIndex(playerIndex);
      if (name.toUpperCase().startsWith("IA") || name.toUpperCase().startsWith("AI")) {
        gameInstance.addPlayer(new AiPlayer(name, 3, 5, assignedColor));
      } else {
        gameInstance.addPlayer(new HumanPlayer(name, assignedColor));
      }
      playerIndex++;
    }

    viewInstance = new JavaFxView(gameInstance);
    viewInstance.setGui(this);
    controller = new GameController(gameInstance, viewInstance);

    controlPanel.getUndoButton().setDisable(false);
    controlPanel.getRedoButton().setDisable(false);

    boardPanel.clearAllPending();
    pendingTiles.clear();
    boardPanel.setBoard(gameInstance.getBoard());

    // Démarrer la partie AVANT de rafraîchir l'affichage
    controller.startGame();
    refreshAll();
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
      final AiPlayer ai = (AiPlayer) current;
      boardPanel.setDisable(true);
      rackPanel.setDisable(true);
      controlPanel.setDisable(true);

      new Thread(() -> {
        try {
          Thread.sleep(1000);
          ai.playTurn(gameInstance, gaddag);
        } catch (Exception e) {
          e.printStackTrace();
          Platform.runLater(() -> controller.handlePlayerMove(Move.createPass(ai)));
        } finally {
          Platform.runLater(() -> {
            boardPanel.setDisable(false);
            rackPanel.setDisable(false);
            controlPanel.setDisable(false);
            refreshAll();
          });
        }
      }).start();
    }
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
}