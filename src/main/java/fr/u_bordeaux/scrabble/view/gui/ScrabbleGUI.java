package fr.u_bordeaux.scrabble.view.gui;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import fr.u_bordeaux.scrabble.controller.GameController;
import fr.u_bordeaux.scrabble.model.ai.AIPlayer;
import fr.u_bordeaux.scrabble.model.core.Game;
import fr.u_bordeaux.scrabble.model.core.HumanPlayer;
import fr.u_bordeaux.scrabble.model.core.Move;
import fr.u_bordeaux.scrabble.model.core.Rack;
import fr.u_bordeaux.scrabble.model.core.Tile;
import fr.u_bordeaux.scrabble.model.dictionary.GADDAG;
import fr.u_bordeaux.scrabble.model.interfaces.Player;
import fr.u_bordeaux.scrabble.model.network.NetworkManager;
import fr.u_bordeaux.scrabble.model.network.NetworkManager;
import fr.u_bordeaux.scrabble.model.utils.Point;
import fr.u_bordeaux.scrabble.view.gui.panel.BoardPanel;
import fr.u_bordeaux.scrabble.view.gui.panel.ControlPanel;
import fr.u_bordeaux.scrabble.view.gui.panel.MessagePanel;
import fr.u_bordeaux.scrabble.view.gui.panel.MessagePanel;
import fr.u_bordeaux.scrabble.view.gui.panel.RackPanel;
import fr.u_bordeaux.scrabble.view.gui.panel.ScorePanel;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.TextInputDialog;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import fr.u_bordeaux.scrabble.model.ai.AIPlayer;
import fr.u_bordeaux.scrabble.model.dictionary.GADDAG;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;

import fr.u_bordeaux.scrabble.model.ai.AIPlayer;
import fr.u_bordeaux.scrabble.model.dictionary.GADDAG;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;

import fr.u_bordeaux.scrabble.model.ai.AIPlayer;
import fr.u_bordeaux.scrabble.model.dictionary.GADDAG;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;

public class ScrabbleGUI extends Application {

    private static Game       gameInstance;
    private static JavaFxView viewInstance;
    private static Game       gameInstance;
    private static JavaFxView viewInstance;

    private GameController controller;
    private BoardPanel     boardPanel;
    private RackPanel      rackPanel;
    private ScorePanel     scorePanel;
    private ControlPanel   controlPanel;
    private MessagePanel   messagePanel;
    private MessagePanel   messagePanel;

    private final Map<Point, Tile> pendingTiles = new HashMap<>();
    private Tile currentlyDraggedTile = null;

    private NetworkManager    networkManager;
    private NetworkGameBridge networkBridge;
    private NetworkLobbyView  lobbyView;
    private boolean onlineMode = false;
    private NetworkManager    networkManager;
    private NetworkGameBridge networkBridge;
    private NetworkLobbyView  lobbyView;
    private boolean onlineMode = false;

    public static void setGame(Game game)       { gameInstance = game; }
    public static void setView(JavaFxView view) { viewInstance = view; }

    // ─── JavaFX entry point ───────────────────────────────────────────────────
    
    @Override
    public void start(Stage stage) {
        if (gameInstance == null) {
            throw new IllegalStateException("Appelez ScrabbleGUI.setGame() avant de lancer.");
        }

        networkManager = new NetworkManager();
        networkBridge  = new NetworkGameBridge(networkManager);
        networkBridge.setGui(this);

        messagePanel = new MessagePanel();
        scorePanel   = new ScorePanel();
        controlPanel = new ControlPanel();
        boardPanel   = new BoardPanel(gameInstance.getBoard());
        rackPanel    = new RackPanel(getCurrentRack());

        Optional<List<String>> namesOpt = PlayerSetup.showDialog();
        if (namesOpt.isEmpty()) { Platform.exit(); return; }

        if (gaddag == null) loadDictionary();


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

        if (viewInstance == null) viewInstance = new JavaFxView(gameInstance);
        viewInstance.setGUI(this);


        if (viewInstance == null) viewInstance = new JavaFxView(gameInstance);
        viewInstance.setGUI(this);


        if (viewInstance == null) viewInstance = new JavaFxView(gameInstance);
        viewInstance.setGUI(this);

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


        controlPanel.getPassButton().setOnAction(e -> {
            if (onlineMode) {
                networkManager.pass();
            } else {
                controller.handlePlayerMove(Move.createPass(gameInstance.getCurrentPlayer()));
            }
        });

        controlPanel.getExchangeButton().setOnAction(e -> openExchangeDialog());
        controlPanel.getCancelPlacementButton().setOnAction(e -> cancelPendingTiles());
        controlPanel.getCancelPlacementButton().setOnAction(e -> cancelPendingTiles());

        controlPanel.getUndoButton().setOnAction(e -> { if (!onlineMode) controller.undo(); });
        controlPanel.getRedoButton().setOnAction(e -> { if (!onlineMode) controller.redo(); });

        controlPanel.getOnlineButton().setOnAction(e -> openNetworkLobby());
        controlPanel.getUndoButton().setOnAction(e -> { if (!onlineMode) controller.undo(); });
        controlPanel.getRedoButton().setOnAction(e -> { if (!onlineMode) controller.redo(); });

        controlPanel.getOnlineButton().setOnAction(e -> openNetworkLobby());
        controlPanel.getNewGameButton().setOnAction(e -> handleNewGame());

        controlPanel.getSaveButton().setOnAction(e ->
            showInfo("À venir", "Sauvegarde bientôt disponible."));
        controlPanel.getLoadButton().setOnAction(e ->
            showInfo("À venir", "Chargement bientôt disponible."));


        controlPanel.getSaveButton().setOnAction(e ->
            showInfo("À venir", "Sauvegarde bientôt disponible."));
        controlPanel.getLoadButton().setOnAction(e ->
            showInfo("À venir", "Chargement bientôt disponible."));

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

    public void switchToOnlineGame(Game onlineGame) {
        gameInstance = onlineGame;
        onlineMode   = true;
        controlPanel.getUndoButton().setDisable(true);
        controlPanel.getRedoButton().setDisable(true);
        boardPanel.setBoard(gameInstance.getBoard());
        pendingTiles.clear();
        refreshAll();
        showInfo("Partie en ligne", "La partie a commencé ! Bonne chance 🎮");
    }

    public void exitOnlineMode() {
        onlineMode = false;
        controlPanel.getUndoButton().setDisable(false);
        controlPanel.getRedoButton().setDisable(false);
    }

    public boolean isOnlineMode() { return onlineMode; }
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

    public void switchToOnlineGame(Game onlineGame) {
        gameInstance = onlineGame;
        onlineMode   = true;
        controlPanel.getUndoButton().setDisable(true);
        controlPanel.getRedoButton().setDisable(true);
        boardPanel.setBoard(gameInstance.getBoard());
        pendingTiles.clear();
        refreshAll();
        showInfo("Partie en ligne", "La partie a commencé ! Bonne chance 🎮");
    }

    public void exitOnlineMode() {
        onlineMode = false;
        controlPanel.getUndoButton().setDisable(false);
        controlPanel.getRedoButton().setDisable(false);
    }

    public boolean isOnlineMode() { return onlineMode; }

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

        if (onlineMode) {
            Point  origin = move.getStartPosition();
            String dir    = move.getDirection().name().substring(0, 1);
            String word   = move.getTiles().stream()
                                .map(t -> String.valueOf(t.getCharacter()))
                                .reduce("", String::concat);
            networkManager.play(origin.getX(), origin.getY(), dir, word);
        } else {
                
        } catch (RuntimeException e) {
            // The game engine rejected the move (e.g., word doesn't touch existing tiles)
            // Show the exact error message to the player
            showError("Coup invalide : " + e.getMessage());
            
            // Visually remove the tiles from the board and put them back on the rack
            cancelPendingTiles(); 
        }
        pendingTiles.clear();

        if (onlineMode) {
            Point  origin = move.getStartPosition();
            String dir    = move.getDirection().name().substring(0, 1);
            String word   = move.getTiles().stream()
                                .map(t -> String.valueOf(t.getCharacter()))
                                .reduce("", String::concat);
            networkManager.play(origin.getX(), origin.getY(), dir, word);
        } else {
            controller.handlePlayerMove(move);
        }
    }

    private void openExchangeDialog() {
        if (!pendingTiles.isEmpty()) {
            showError("Annulez d'abord les tuiles placées (bouton ↩).");
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
            if (letters.isEmpty()) return;

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
            String letters = input.trim().toUpperCase();
            if (letters.isEmpty()) return;

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
        if (pendingTiles.isEmpty()) return;
        if (pendingTiles.isEmpty()) return;
        pendingTiles.forEach((p, t) -> boardPanel.clearTile(p.getY(), p.getX()));
        pendingTiles.clear();
        refreshRack();
    }

    private void handleNewGame() {
        if (!messagePanel.showConfirmation("Abandonner la partie en cours et recommencer ?")) return;
        if (!messagePanel.showConfirmation("Abandonner la partie en cours et recommencer ?")) return;

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

        onlineMode = false;
        controlPanel.getUndoButton().setDisable(false);
        controlPanel.getRedoButton().setDisable(false);

        onlineMode = false;
        controlPanel.getUndoButton().setDisable(false);
        controlPanel.getRedoButton().setDisable(false);

        boardPanel.clearAllPending();
        pendingTiles.clear();
        boardPanel.setBoard(gameInstance.getBoard());
        refreshAll();
        controller.startGame();
    }

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
        Player current = gameInstance.getCurrentPlayer();
        int idx = players.indexOf(current);
        if (idx >= 0 && current != null) {
            scorePanel.highlightCurrentPlayer(idx, current.getName());
        }
    }
        Player current = gameInstance.getCurrentPlayer();
        int idx = players.indexOf(current);
        if (idx >= 0 && current != null) {
            scorePanel.highlightCurrentPlayer(idx, current.getName());
        }
    }

    public void showInfo(String title, String message) {
        messagePanel.showInfo(title, message);
        messagePanel.showInfo(title, message);
    }

    public void showError(String message) {
        messagePanel.showError(message);
        messagePanel.showError(message);
    }

    private Rack getCurrentRack() {
        Player p = gameInstance.getCurrentPlayer();
        return p != null ? p.getRack() : new Rack();
    }

    public static void main(String[] args) { launch(args); }
}