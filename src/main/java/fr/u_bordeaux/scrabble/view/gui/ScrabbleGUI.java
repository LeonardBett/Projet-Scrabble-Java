package fr.u_bordeaux.scrabble.view.gui;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import fr.u_bordeaux.scrabble.controller.GameController;
import fr.u_bordeaux.scrabble.model.core.Game;
import fr.u_bordeaux.scrabble.model.core.HumanPlayer;
import fr.u_bordeaux.scrabble.model.core.Move;
import fr.u_bordeaux.scrabble.model.core.Rack;
import fr.u_bordeaux.scrabble.model.core.Tile;
import fr.u_bordeaux.scrabble.model.interfaces.Player;
import fr.u_bordeaux.scrabble.model.utils.Point;
import fr.u_bordeaux.scrabble.view.gui.panel.BoardPanel;
import fr.u_bordeaux.scrabble.view.gui.panel.ControlPanel;
import fr.u_bordeaux.scrabble.view.gui.panel.RackPanel;
import fr.u_bordeaux.scrabble.view.gui.panel.ScorePanel;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TextInputDialog;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import fr.u_bordeaux.scrabble.model.ai.AIPlayer;
import fr.u_bordeaux.scrabble.model.dictionary.GADDAG;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;




/**
 * JavaFX application window.
 *
 * ✅ MVC PROPRE:
 *  - ZERO logique métier (pas de Direction, pas de validation de tuiles)
 *  - Délègue à PendingMoveBuilder et ExchangeMoveBuilder
 *  - Envoie des Move au contrôleur, point final
 */
public class ScrabbleGUI extends Application {

    private static Game        gameInstance;
    private static JavaFxView  viewInstance;

    private GameController controller;
    private BoardPanel     boardPanel;
    private RackPanel      rackPanel;
    private ScorePanel     scorePanel;
    private ControlPanel   controlPanel;

    /** UI state: tiles placed but not yet sent to controller */
    private final Map<Point, Tile> pendingTiles = new HashMap<>();
    private Tile currentlyDraggedTile = null;

    // ─── Static setters ───────────────────────────────────────────────────────

    public static void setGame(Game game)       { gameInstance = game; }
    public static void setView(JavaFxView view) { viewInstance = view; }

    // ─── JavaFX entry point ───────────────────────────────────────────────────
    
    @Override
    public void start(Stage stage) {
        if (gameInstance == null) {
            throw new IllegalStateException("Call ScrabbleGUI.setGame() before launching.");
        }

        // Ask player names
        Optional<List<String>> namesOpt = PlayerSetup.showDialog();
        if (namesOpt.isEmpty()) { Platform.exit(); return; }

        if (gaddag == null) loadDictionary();

        for (String name : namesOpt.get()) {
            String upperName = name.toUpperCase();
            
            // 1. Check for advanced AI (Expectiminimax) first
            if (upperName.startsWith("IAEX") || upperName.startsWith("AIEX")) {
                // Depth 2 minimum is required for Expectiminimax to anticipate the opponent
                AIPlayer ai = new AIPlayer(name, 2);
                ai.setExpectiminimaxMode(true);
                gameInstance.addPlayer(ai);
                
            // 2. Check for standard AI (Classic Minimax)
            } else if (upperName.startsWith("IA") || upperName.startsWith("AI")) {
                // Standard AI
                AIPlayer ai = new AIPlayer(name, 2);
                gameInstance.addPlayer(ai);
                
            // 3. Otherwise, it's a human player
            } else {
                gameInstance.addPlayer(new HumanPlayer(name));
            }
        }

        // Wire view
        if (viewInstance == null) viewInstance = new JavaFxView(gameInstance);
        viewInstance.setGUI(this);

        // Build panels
        scorePanel   = new ScorePanel();
        controlPanel = new ControlPanel();
        boardPanel   = new BoardPanel(gameInstance.getBoard());
        rackPanel    = new RackPanel(getCurrentRack());

        boardPanel.setOnTileDropped(this::onTileDropped);
        rackPanel.setOnTileDragged(this::onTileDragged);

        // Build controller
        controller = new GameController(gameInstance, viewInstance);
        connectButtons();

        // Layout
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

        stage.setTitle("Scrabble U-Bordeaux");
        stage.setScene(new Scene(root, 1200, 800));
        stage.setFullScreen(true);
        stage.show();

        controller.startGame();
        refreshAll();
    }

    // ─── Button wiring ────────────────────────────────────────────────────────

    private void connectButtons() {
        // Game actions → controller
        controlPanel.getPlayButton().setOnAction(e -> submitPendingTiles());
        controlPanel.getPassButton().setOnAction(e -> 
            controller.handlePlayerMove(Move.createPass(gameInstance.getCurrentPlayer())));
        controlPanel.getExchangeButton().setOnAction(e -> openExchangeDialog());
        controlPanel.getUndoButton().setOnAction(e -> controller.undo());
        controlPanel.getRedoButton().setOnAction(e -> controller.redo());

        // Pure view actions
       
        controlPanel.getNewGameButton().setOnAction(e -> handleNewGame());
        controlPanel.getSaveButton().setOnAction(e -> showInfo("À venir", "Sauvegarde bientôt disponible."));
        controlPanel.getLoadButton().setOnAction(e -> showInfo("À venir", "Chargement bientôt disponible."));
        controlPanel.getQuitButton().setOnAction(e -> {
            Optional<ButtonType> r = new Alert(Alert.AlertType.CONFIRMATION,
                    "Voulez-vous vraiment quitter ?").showAndWait();
            if (r.isPresent() && r.get() == ButtonType.OK) Platform.exit();
        });
    }

    // ─── Drag & drop (pure view — no logic) ──────────────────────────────────

    public void onTileDragged(Tile tile) {
        currentlyDraggedTile = tile;
    }

    public void onTileDropped(int row, int col) {
        if (currentlyDraggedTile == null) return;

        Point point = new Point(col, row);

        if (!gameInstance.getBoard().getSquare(point).isEmpty()
                || pendingTiles.containsKey(point)) {
            showError("Cette case est déjà occupée !");
            currentlyDraggedTile = null;
            return;
        }

        pendingTiles.put(point, currentlyDraggedTile);
        boardPanel.placeTile(row, col, currentlyDraggedTile.getCharacter(), currentlyDraggedTile.getValue());
        rackPanel.hideTile(currentlyDraggedTile);
        currentlyDraggedTile = null;
    }

    // ─── Game actions (prepare data, delegate to builders, send to controller)

    private static GADDAG gaddag;

    private void loadDictionary() {
        gaddag = new GADDAG();
        System.out.println("Chargement du GADDAG pour l'interface graphique...");
        try {
            InputStream is = getClass().getClassLoader().getResourceAsStream("dictionaries/lexicon_en.txt");
            if (is != null) {
                BufferedReader br = new BufferedReader(new InputStreamReader(is));
                String line;
                while ((line = br.readLine()) != null) {
                    if (!line.trim().isEmpty()) gaddag.add(line.trim());
                }
                br.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    /**
     * Delegates to PendingMoveBuilder.
     * Captures game engine exceptions to gracefully cancel invalid moves.
     */
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

        // NEW: Try-catch block to handle invalid moves rejected by the game engine
        try {
            // Attempt to execute the move in the model
            controller.handlePlayerMove(move);
            
            // If the move is valid and executed successfully, clear the pending tiles map
            pendingTiles.clear();
            
        } catch (RuntimeException e) {
            // The game engine rejected the move (e.g., word doesn't touch existing tiles)
            // Show the exact error message to the player
            showError("Coup invalide : " + e.getMessage());
            
            // Visually remove the tiles from the board and put them back on the rack
            cancelPendingTiles(); 
        }
    }

    /**
     * ✅ DÉLÈGUE à ExchangeMoveBuilder — zero logique ici !
     */
    private void openExchangeDialog() {
        if (!pendingTiles.isEmpty()) {
            showError("Annulez d'abord les tuiles placées.");
            return;
        }

        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Échanger des lettres");
        dialog.setHeaderText("Lettres de votre chevalet à échanger");
        dialog.setContentText("Lettres (ex: ABC) :");

        Optional<String> result = dialog.showAndWait();
        result.ifPresent(input -> {
            Move move = ExchangeMoveBuilder.build(input.trim().toUpperCase(),
                    gameInstance.getCurrentPlayer());
            if (move == null) {
                showError("Certaines lettres ne sont pas dans votre chevalet !");
                return;
            }
            controller.handlePlayerMove(move);
        });
    }

    private void cancelPendingTiles() {
        pendingTiles.forEach((p, t) -> boardPanel.clearTile(p.getY(), p.getX()));
        pendingTiles.clear();
        refreshRack();
    }

    private void handleNewGame() {
        Optional<ButtonType> confirm = new Alert(Alert.AlertType.CONFIRMATION,
                "Abandonner la partie en cours et recommencer ?").showAndWait();
        if (confirm.isEmpty() || confirm.get() != ButtonType.OK) return;

        gameInstance = new Game();
        Optional<List<String>> namesOpt = PlayerSetup.showDialog();
        if (namesOpt.isEmpty()) return;

        if (gaddag == null) loadDictionary();

        for (String name : namesOpt.get()) {
            if (name.toUpperCase().startsWith("IA") || name.toUpperCase().startsWith("AI")) {
                gameInstance.addPlayer(new AIPlayer(name, 3));
            } else {
                gameInstance.addPlayer(new HumanPlayer(name));
            }
        }

        viewInstance = new JavaFxView(gameInstance);
        viewInstance.setGUI(this);
        controller = new GameController(gameInstance, viewInstance);

        boardPanel.clearAllPending();
        pendingTiles.clear();
        boardPanel.setBoard(gameInstance.getBoard());
        refreshAll();
        controller.startGame();
    }

    // ─── Refresh (called by JavaFxView) ──────────────────────────────────────

public void refreshAll() {
        refreshBoard();
        refreshRack();
        refreshScores();
        
        // NEW: At each refresh, check if it is the AI's turn to play
        checkAITurn();
    }

    private void checkAITurn() {
        // Prevent launching the AI multiple times if the UI is already locked
        if (boardPanel.isDisable()) {
            return;
        }

        Player current = gameInstance.getCurrentPlayer();
        
        // If the current player is an AI and the game is not over
        if (current instanceof AIPlayer && !gameInstance.isGameOver()) {
            AIPlayer ai = (AIPlayer) current;
            
            // 1. Disable the UI (to prevent the human from clicking everywhere)
            boardPanel.setDisable(true);
            rackPanel.setDisable(true);
            controlPanel.setDisable(true);

            // 2. Launch the AI's logic in a SEPARATE THREAD
            new Thread(() -> {
                try {
                    // Small 1-second pause for visual effect ("AI is thinking...")
                    Thread.sleep(1000); 
                    
                    // The AI runs its algorithm (which will auto-stop at 4.8s)
                    ai.playTurn(gameInstance, gaddag);
                    
                } catch (Exception e) {
                    e.printStackTrace();
                    // In case of an AI error, force it to pass its turn
                    Platform.runLater(() -> {
                        controller.handlePlayerMove(Move.createPass(ai));
                    });
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

    public void refreshBoard() {
        boardPanel.updateBoard();
    }

    public void refreshRack() {
        rackPanel.setRack(getCurrentRack());
        rackPanel.setOnTileDragged(this::onTileDragged);
    }

    public void refreshScores() {
        List<Player> players = gameInstance.getPlayers();
        String[] names  = players.stream().map(Player::getName).toArray(String[]::new);
        int[]    scores = players.stream().mapToInt(Player::getScore).toArray();
        scorePanel.updateScores(names, scores);
        scorePanel.updateBagInfo(gameInstance.getBag().size());
        int idx = players.indexOf(gameInstance.getCurrentPlayer());
        if (idx >= 0) scorePanel.highlightCurrentPlayer(idx);
    }

    // ─── Popup helpers ────────────────────────────────────────────────────────

    public void showInfo(String title, String message) {
        Alert a = new Alert(Alert.AlertType.INFORMATION);
        a.setTitle(title); a.setHeaderText(null); a.setContentText(message);
        a.showAndWait();
    }

    public void showError(String message) {
        Alert a = new Alert(Alert.AlertType.ERROR);
        a.setTitle("Erreur"); a.setHeaderText(null); a.setContentText(message);
        a.showAndWait();
    }

    private Rack getCurrentRack() {
        Player p = gameInstance.getCurrentPlayer();
        return p != null ? p.getRack() : new Rack();
    }

    public static void main(String[] args) { launch(args); }
}