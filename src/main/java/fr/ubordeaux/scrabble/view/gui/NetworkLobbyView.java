package fr.ubordeaux.scrabble.view.gui;

import fr.ubordeaux.scrabble.i18n.I18n;
import fr.ubordeaux.scrabble.model.network.NetworkManager;
import fr.ubordeaux.scrabble.model.network.server.ServerInfo;
import java.util.List;
import java.util.Map;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.Separator;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Modality;
import javafx.stage.Stage;

/**
 * Fenetre de lobby reseau (JavaFX) pour heberger et rejoindre des parties en ligne.
 *
 * <p>Les serveurs sont decouverts automatiquement via UDP. Le bouton "Lancer la partie" est visible
 * uniquement pour l'hote et s'active quand au moins 2 joueurs sont connectes.
 */
public class NetworkLobbyView extends Stage {

  private static String windowTitleText() {
    return "🌐 Scrabble — Multijoueur en ligne";
  }

  private static String mainTitleText() {
    return "🌐 Multijoueur en ligne";
  }

  private static String consoleLabelText() {
    return "Console :";
  }

  private static String rootBackgroundStyle() {
    return "-fx-background-color: #1a2a3a;";
  }

  private static int rootSpacing() {
    return 10;
  }

  private static int rootPadding() {
    return 15;
  }

  private static String titleFontFamily() {
    return "Arial";
  }

  private static int titleFontSize() {
    return 20;
  }

  private static String tabPaneBackgroundStyle() {
    return "-fx-background-color: #243447;";
  }

  private static String tabContentBackgroundStyle() {
    return "-fx-background-color: #243447;";
  }

  private static String listViewStyle() {
    return "-fx-control-inner-background: #1a2a3a; -fx-text-fill: white;";
  }

  private static String consoleAreaStyle() {
    return "-fx-control-inner-background: #0d1b2a; -fx-text-fill: #00ff88; "
        + "-fx-font-family: monospace; -fx-font-size: 11;";
  }

  private static int consoleAreaPrefHeight() {
    return 100;
  }

  private static String consolePromptText() {
    return "Logs réseau...";
  }

  private static String consoleLabelFontFamily() {
    return "Arial";
  }

  private static int consoleLabelFontSize() {
    return 12;
  }

  private static String statusLabelFontFamily() {
    return "Arial";
  }

  private static int statusLabelFontSize() {
    return 13;
  }

  private static int tabContentSpacing() {
    return 12;
  }

  private static int tabContentPadding() {
    return 20;
  }

  private static int rowSpacing() {
    return 10;
  }

  private static int sceneWidth() {
    return 520;
  }

  private static int sceneHeight() {
    return 640;
  }

  private static String hostTabTitleText() {
    return "🖥  Héberger";
  }

  private static String joinTabTitleText() {
    return "🔍 Rejoindre";
  }

  private static String lobbyTabTitleText() {
    return "🎮 Salon";
  }

  private static String hostDescriptionText() {
    return "Démarrez un serveur. Les joueurs sur le réseau local vous verront automatiquement.";
  }

  private static String hostPortLabelText() {
    return "Port TCP :";
  }

  private static String startServerButtonText() {
    return "▶  Démarrer le serveur";
  }

  private static String stopServerButtonText() {
    return "■  Arrêter le serveur";
  }

  private static String startGameButtonText() {
    return "🎮 Lancer la partie";
  }

  private static String hostPlayersTitleText() {
    return "Joueurs connectés au salon :";
  }

  private static String hostSectionTitleText() {
    return "Héberger une partie";
  }

  private static String joinSelectedButtonText() {
    return "🎮 Rejoindre le serveur sélectionné";
  }

  private static String ipLabelText() {
    return "IP :";
  }

  private static String portLabelText() {
    return "Port :";
  }

  private static String connectButtonText() {
    return "🔗 Se connecter";
  }

  private static String disconnectButtonText() {
    return "✖  Se déconnecter";
  }

  private static String autoServersTitleText() {
    return "Serveurs détectés automatiquement :";
  }

  private static String manualConnectionTitleText() {
    return "Ou connexion manuelle :";
  }

  private static String lobbyWaitingText() {
    return "En attente que l'hôte lance la partie...";
  }

  private static String refreshScoreboardButtonText() {
    return "🏆 Voir le classement";
  }

  private static String lobbyPlayersTitleText() {
    return "Joueurs connectés :";
  }

  private static String scoreboardTitleText() {
    return "Classement du serveur :";
  }

  private static String startServerButtonColor() {
    return "#4CAF50";
  }

  private static String stopServerButtonColor() {
    return "#F44336";
  }

  private static String startGameButtonColor() {
    return "#FF9800";
  }

  private static String connectButtonColor() {
    return "#4CAF50";
  }

  private static String disconnectButtonColor() {
    return "#F44336";
  }

  private static String refreshScoreboardColor() {
    return "#9C27B0";
  }

  private static String defaultHostPortText() {
    return String.valueOf(NetworkManager.DEFAULT_TCP_PORT);
  }

  private static String defaultJoinHostText() {
    return "localhost";
  }

  private static String defaultJoinPortText() {
    return String.valueOf(NetworkManager.DEFAULT_TCP_PORT);
  }

  private static String formatServerLine(String name, String ip, int port) {
    return String.format("%-20s  %s:%d", name, ip, port);
  }

  private static String formatPlayerLine(String id, String name, String status) {
    return String.format("#%-4s %-16s [%s]", id, name, status);
  }

  private static String formatScoreboardLine(int rank, String name, String wins,
      String losses, String total) {
    return String.format("%d. %-14s  V:%s  D:%s  T:%s", rank, name, wins, losses, total);
  }

  private static List<String> formatServerList(List<ServerInfo> servers) {
    return servers.stream()
        .map(s -> formatServerLine(s.getName(), s.getIp(), s.getPort()))
        .toList();
  }

  private static List<String> formatPlayers(List<Map<String, String>> players) {
    return players.stream()
        .map(p -> formatPlayerLine(p.getOrDefault("ID", "?"), p.getOrDefault("NAME", "Inconnu"),
            p.getOrDefault("STATUS", "?")))
        .toList();
  }

  private static List<String> formatScoreboard(List<Map<String, String>> scoreboard) {
    final int[] rank = { 1 };
    return scoreboard.stream()
        .map(entry -> formatScoreboardLine(rank[0]++, entry.getOrDefault("NAME", "?"),
            entry.getOrDefault("WINS", "0"), entry.getOrDefault("LOSSES", "0"),
            entry.getOrDefault("TOTAL", "0")))
        .toList();
  }

  private static String formatServerStatus(Map<String, String> info) {
    return "📊 Serveur — Port: " + info.get("PORT")
        + " | Clients: " + info.get("CLIENTS")
        + " | Parties: " + info.get("GAMES");
  }

  private static String serverRunningStatusText(int port) {
    return "● Serveur en écoute sur le port " + port;
  }

  private static String serverStoppedStatusText() {
    return "● Serveur arrêté";
  }

  private static String serverStartedLogMessage(int port) {
    return "Serveur démarré sur le port " + port;
  }

  private static String serverStoppedLogMessage() {
    return "Serveur arrêté.";
  }

  private static String invalidHostPortMessage(String value) {
    return "❌ Port invalide : " + value;
  }

  private static String invalidJoinPortMessage() {
    return "❌ Port invalide.";
  }

  private static String invalidServerSelectionMessage() {
    return "❌ Sélectionnez un serveur dans la liste.";
  }

  private static String notConnectedMessage() {
    return "❌ Non connecté.";
  }

  private static String connectingMessage(String ip, int port) {
    return "🔗 Connexion à " + ip + ":" + port + "...";
  }

  private static String playersReadyMessage(int count) {
    return "✅ " + count + " joueur(s) connecté(s) — vous pouvez lancer la partie.";
  }

  private static String gameEndedMessage(String reason) {
    return "🏁 Partie terminée : " + reason;
  }

  private static String startingGameLogMessage() {
    return "🎮 Lancement de la partie...";
  }

  private static String disconnectedMessage() {
    return "✖ Déconnecté du serveur.";
  }

  private static boolean isValidSelectedServerIndex(int index, int size) {
    return size > 0 && index >= 0 && index < size;
  }

  private static boolean canRefreshScoreboard(boolean connected) {
    return connected;
  }

  private static boolean canHostStartGame(int playerCount) {
    return playerCount >= 2;
  }

  private static boolean shouldDisableStartServer(boolean running) {
    return running;
  }

  private static boolean shouldDisableStopServer(boolean running) {
    return !running;
  }

  private static boolean shouldDisableConnect(boolean connected) {
    return connected;
  }

  private static boolean shouldDisableDisconnect(boolean connected) {
    return !connected;
  }

  private static boolean shouldDisableRefreshScoreboard(boolean connected) {
    return !connected;
  }

  private static boolean shouldDisableStartGame(boolean running, int playerCount) {
    return !running || !canHostStartGame(playerCount);
  }

  private static boolean shouldLogPlayersReady(int playerCount) {
    return playerCount >= 2;
  }

  private final NetworkGameBridge bridge;
  private final NetworkManager networkManager;

  private TabPane tabPane;

  // Onglet Héberger
  private TextField portField;
  private Button startServerButton;
  private Button stopServerButton;
  private Label serverStatusLabel;
  private ListView<String> lobbyPlayerListView;
  private Button startGameButton;

  // Onglet Rejoindre
  private TextField ipField;
  private TextField joinPortField;
  private Button connectButton;
  private Button disconnectButton;
  private ListView<String> serverListView;
  private final ObservableList<ServerInfo> discoveredServers = FXCollections.observableArrayList();

  // Onglet Salon (client)
  private ListView<String> playersListView;
  private ListView<String> scoreboardListView;
  private Button refreshScoreboardButton;

  private TextArea consoleArea;

  private boolean serverRunning = false;
  private boolean clientConnected = false;

  /** Number of players currently in the host lobby (updated via onPlayersReceived). */
  private int lobbyPlayerCount = 0;

  /**
   * Creates the network lobby view.
   *
   * @param bridge the network game bridge
   */
  public NetworkLobbyView(NetworkGameBridge bridge) {
    this.bridge = bridge;
    this.networkManager = bridge.getNetworkManager();
    bridge.setLobbyView(this);

    initUi();

    this.setTitle(I18n.translate("lobby.windowTitle"));
    this.initModality(Modality.NONE);
    this.setResizable(false);

    this.networkManager.startOnlinePlay();
  }

  // ─── Construction de l'UI ────────────────────────────────────────────────

  private void initUi() {
    VBox root = new VBox(rootSpacing());
    root.setPadding(new Insets(rootPadding()));
    root.setStyle(rootBackgroundStyle());

    Label title = new Label(I18n.translate("lobby.mainTitle"));
    title.setFont(Font.font("Arial", FontWeight.BOLD, 20));
    title.setTextFill(Color.WHITE);

    tabPane = new TabPane();
    tabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);
    tabPane.setStyle(tabPaneBackgroundStyle());
    tabPane.getTabs().addAll(buildHostTab(), buildJoinTab(), buildLobbyTab());

    consoleArea = new TextArea();
    consoleArea.setEditable(false);
    consoleArea.setPrefHeight(100);
    consoleArea.setStyle(
        "-fx-control-inner-background: #0d1b2a; -fx-text-fill: #00ff88; "
            + "-fx-font-family: monospace; -fx-font-size: 11;");
    consoleArea.setPromptText(I18n.translate("lobby.consolePrompt"));

    VBox.setVgrow(tabPane, Priority.ALWAYS);

    Label consoleLabel = new Label(I18n.translate("lobby.consoleLabel"));
    consoleLabel.setTextFill(Color.LIGHTGRAY);
    consoleLabel.setFont(Font.font(consoleLabelFontFamily(), FontWeight.BOLD,
        consoleLabelFontSize()));

    root.getChildren().addAll(title, tabPane, consoleLabel, consoleArea);

    Scene scene = new Scene(root, sceneWidth(), sceneHeight());
    this.setScene(scene);
    updateButtonStates();
  }

  // ─── Onglet Héberger ─────────────────────────────────────────────────────

  private Tab buildHostTab() {
    final Tab tab = new Tab(I18n.translate("lobby.hostTab"));

    VBox content = new VBox(tabContentSpacing());
    content.setPadding(new Insets(tabContentPadding()));
    content.setStyle(tabContentBackgroundStyle());

    Label desc =
        styledLabel(
        I18n.translate("lobby.hostDescription"),
            Color.LIGHTGRAY);
    desc.setWrapText(true);

    HBox portRow = new HBox(rowSpacing());
    portRow.setAlignment(Pos.CENTER_LEFT);
    portField = new TextField(defaultHostPortText());
    portField.setPrefWidth(100);
    portRow.getChildren().addAll(
        styledLabel(I18n.translate("lobby.portLabel"), Color.WHITE),
        portField);

    startServerButton = createBtn(I18n.translate("lobby.startServer"), "#4CAF50");
    stopServerButton = createBtn(I18n.translate("lobby.stopServer"), "#F44336");

    serverStatusLabel = styledLabel(I18n.translate("lobby.serverStopped"), Color.GRAY);
    serverStatusLabel.setFont(Font.font("Arial", FontWeight.BOLD, 13));

    startServerButton.setOnAction(e -> onStartServer());
    stopServerButton.setOnAction(e -> onStopServer());

    // Bouton lancer la partie — actif quand >= 2 joueurs
    startGameButton = createBtn(I18n.translate("lobby.startGame"), "#FF9800");
    startGameButton.setDisable(true);
    startGameButton.setOnAction(e -> onStartGame());

    // Liste des joueurs dans le lobby (mis à jour auto)
    final Label playersTitle = styledLabel(
        I18n.translate("lobby.hostPlayersTitle"), Color.WHITE, 13, true);
    lobbyPlayerListView = new ListView<>();
    lobbyPlayerListView.setPrefHeight(130);
    lobbyPlayerListView.setStyle(listViewStyle());

    content
        .getChildren()
        .addAll(
            styledLabel(I18n.translate("lobby.hostSectionTitle"), Color.WHITE, 15, true),
            desc,
            portRow,
            startServerButton,
            stopServerButton,
            serverStatusLabel,
            new Separator(),
            playersTitle,
            lobbyPlayerListView,
            startGameButton);

    tab.setContent(content);
    return tab;
  }

  // ─── Onglet Rejoindre ────────────────────────────────────────────────────

  private Tab buildJoinTab() {
    final Tab tab = new Tab(I18n.translate("lobby.joinTab"));

    VBox content = new VBox(tabContentSpacing());
    content.setPadding(new Insets(tabContentPadding()));
    content.setStyle(tabContentBackgroundStyle());

    serverListView = new ListView<>();
    serverListView.setPrefHeight(140);
    serverListView.setStyle(listViewStyle());

    Button joinSelectedButton = createBtn(I18n.translate("lobby.joinSelected"), "#4CAF50");
    joinSelectedButton.setOnAction(e -> onJoinSelected());

    HBox ipRow = new HBox(rowSpacing());
    ipRow.setAlignment(Pos.CENTER_LEFT);
    ipField = new TextField(defaultJoinHostText());
    ipField.setPrefWidth(160);
    joinPortField = new TextField(defaultJoinPortText());
    joinPortField.setPrefWidth(80);
    ipRow
        .getChildren()
        .addAll(
            styledLabel(I18n.translate("lobby.ipLabel"), Color.WHITE), ipField,
            styledLabel(I18n.translate("lobby.joinPortLabel"), Color.WHITE), joinPortField);

    connectButton = createBtn(I18n.translate("lobby.connect"), "#4CAF50");
    disconnectButton = createBtn(I18n.translate("lobby.disconnect"), "#F44336");

    connectButton.setOnAction(e -> onConnect());
    disconnectButton.setOnAction(e -> onDisconnect());

    final Label autoTitle =
        styledLabel(I18n.translate("lobby.autoServersTitle"), Color.WHITE, 13, true);
    final Label manualTitle =
        styledLabel(
        I18n.translate("lobby.manualTitle"), Color.WHITE, 13, true);

    content
        .getChildren()
        .addAll(
            autoTitle,
            serverListView,
            joinSelectedButton,
            new Separator(),
            manualTitle,
            ipRow,
            connectButton,
            disconnectButton);

    tab.setContent(content);
    return tab;
  }

  // ─── Onglet Salon (client) ────────────────────────────────────────────────

  private Tab buildLobbyTab() {
    final Tab tab = new Tab(I18n.translate("lobby.roomTab"));

    VBox content = new VBox(tabContentSpacing());
    content.setPadding(new Insets(tabContentPadding()));
    content.setStyle(tabContentBackgroundStyle());

    playersListView = new ListView<>();
    playersListView.setPrefHeight(150);
    playersListView.setStyle(listViewStyle());

    Label waitLabel = styledLabel(I18n.translate("lobby.waitHost"), Color.LIGHTGRAY);
    waitLabel.setWrapText(true);

    scoreboardListView = new ListView<>();
    scoreboardListView.setPrefHeight(120);
    scoreboardListView.setStyle(listViewStyle());

    refreshScoreboardButton = createBtn(I18n.translate("lobby.refreshScoreboard"), "#9C27B0");
    refreshScoreboardButton.setOnAction(e -> onRefreshScoreboard());

    final Label playersTitle = styledLabel(
        I18n.translate("lobby.roomPlayersTitle"), Color.WHITE, 13, true);
    final Label sbTitle =
        styledLabel(I18n.translate("lobby.scoreboardTitle"), Color.WHITE, 13, true);

    content
        .getChildren()
        .addAll(
            playersTitle,
            playersListView,
            waitLabel,
            new Separator(),
            sbTitle,
            scoreboardListView,
            refreshScoreboardButton);

    tab.setContent(content);
    return tab;
  }

  // ─── Actions ─────────────────────────────────────────────────────────────

  private void onStartServer() {
    try {
      int port = Integer.parseInt(portField.getText().trim());
      networkManager.startOnlinePlay();
      networkManager.serverStart(port);
      serverRunning = true;
      serverStatusLabel.setText(I18n.translate("lobby.serverRunning", port));
      serverStatusLabel.setTextFill(Color.LIMEGREEN);
      log(I18n.translate("lobby.serverStartedLog", port));
      doConnect("127.0.0.1", port);

      updateButtonStates();
    } catch (NumberFormatException ex) {
      log(I18n.translate("lobby.invalidHostPort", portField.getText()));
    }
  }

  private void onStopServer() {
    networkManager.serverStop();
    serverRunning = false;
    lobbyPlayerCount = 0;
    serverStatusLabel.setText(I18n.translate("lobby.serverStopped"));
    serverStatusLabel.setTextFill(Color.GRAY);
    lobbyPlayerListView.getItems().clear();
    log(I18n.translate("lobby.serverStoppedLog"));
    updateButtonStates();
  }

  /**
   * Called by the host to start the game with all currently connected players. Sends NEW commands
   * targeting each connected player ID.
   */
  private void onStartGame() {
    bridge.requestGameStart();
    log(I18n.translate("lobby.startingGame"));
  }

  private void onJoinSelected() {
    int idx = serverListView.getSelectionModel().getSelectedIndex();
    if (idx < 0 || idx >= discoveredServers.size()) {
      log(I18n.translate("lobby.invalidServerSelection"));
      return;
    }
    ServerInfo selected = discoveredServers.get(idx);
    doConnect(selected.getIp(), selected.getPort());
  }

  private void onConnect() {
    String ip = ipField.getText().trim();
    int port;
    try {
      port = Integer.parseInt(joinPortField.getText().trim());
    } catch (NumberFormatException e) {
      log(I18n.translate("lobby.invalidJoinPort"));
      return;
    }
    doConnect(ip, port);
  }

  private void doConnect(String ip, int port) {
    networkManager.startOnlinePlay();
    networkManager.join(ip, port);
    clientConnected = true;
    log(I18n.translate("lobby.connecting", ip, port));
    updateButtonStates();
    tabPane.getSelectionModel().select(2);
    // Demande la liste des joueurs pour afficher le salon
    networkManager.players();
  }

  private void onDisconnect() {
    networkManager.quit();
    clientConnected = false;
    log(I18n.translate("lobby.disconnected"));
    updateButtonStates();
  }

  private void onRefreshScoreboard() {
    if (!clientConnected) {
      log(I18n.translate("lobby.notConnected"));
      return;
    }
    networkManager.scoreboard();
  }

  // ─── Callbacks depuis NetworkGameBridge ──────────────────────────────────

  /**
   * Called when the server list is updated via UDP discovery.
   *
   * @param servers the discovered servers
   */
  public void onServerListUpdated(List<ServerInfo> servers) {
    discoveredServers.setAll(servers);
    ObservableList<String> items = FXCollections.observableArrayList(formatServerList(servers));
    serverListView.setItems(items);
  }

  /**
   * Called when the player list is received from the server. Updates both the host lobby list and
   * the client salon list. If the host has >= 2 players, activates the start button and
   * auto-launches.
   *
   * @param players the list of player info maps
   */
  public void onPlayersReceived(List<Map<String, String>> players) {
    lobbyPlayerCount = players.size();

    ObservableList<String> items = FXCollections.observableArrayList();
    for (Map<String, String> p : players) {
      String id = p.getOrDefault("ID", "?");
      String name = p.getOrDefault("NAME", I18n.translate("lobby.unknownPlayer"));
      String status = p.getOrDefault("STATUS", "?");
      items.add(String.format("#%-4s %-16s [%s]", id, name, status));
    }

    // Update both views
    lobbyPlayerListView.setItems(FXCollections.observableArrayList(items));
    playersListView.setItems(FXCollections.observableArrayList(items));

    // Enable start button for host if >= 2 players
    if (serverRunning) {
      startGameButton.setDisable(lobbyPlayerCount < 2);

      // Auto-launch when exactly 2 players are ready (host + 1 client)
      if (lobbyPlayerCount >= 2) {
        log(I18n.translate("lobby.playersReady", lobbyPlayerCount));
      }
    }

    // If this was triggered by onStartGame, now send the NEW command with all IDs
    // This is handled via the playersUpdate callback from bridge
  }

  /**
   * Called when the scoreboard is received.
   *
   * @param scoreboard the scoreboard entries
   */
  public void onScoreboardReceived(List<Map<String, String>> scoreboard) {
    ObservableList<String> items = FXCollections.observableArrayList(formatScoreboard(scoreboard));
    scoreboardListView.setItems(items);
  }

  /**
   * Called when server status info is received.
   *
   * @param info the status info map
   */
  public void onServerStatusReceived(Map<String, String> info) {
    log(I18n.translate("lobby.serverStatus",
        info.get("PORT"), info.get("CLIENTS"), info.get("GAMES")));
  }

  /**
   * Called for generic messages from the server.
   *
   * @param message the message text
   */
  public void onMessageReceived(String message) {
    log(message);
  }

  /**
   * Called when the game ends.
   *
   * @param reason the end reason
   */
  public void onGameEnded(String reason) {
    log(I18n.translate("lobby.gameEnded", reason));
    clientConnected = false;
    lobbyPlayerCount = 0;
    updateButtonStates();
  }

  // ─── Helpers ─────────────────────────────────────────────────────────────

  /**
   * Returns true if this lobby is in host (server) mode.
   *
   * @return true when the server is running
   */
  public boolean isHostMode() {
    return serverRunning;
  }

  /**
   * Returns the IDs of connected players for use by the host to start a game.
   *
   * @return the current player count in the lobby
   */
  public int getLobbyPlayerCount() {
    return lobbyPlayerCount;
  }

  private void updateButtonStates() {
    startServerButton.setDisable(shouldDisableStartServer(serverRunning));
    stopServerButton.setDisable(shouldDisableStopServer(serverRunning));
    connectButton.setDisable(shouldDisableConnect(clientConnected));
    disconnectButton.setDisable(shouldDisableDisconnect(clientConnected));
    refreshScoreboardButton.setDisable(shouldDisableRefreshScoreboard(clientConnected));
    startGameButton.setDisable(shouldDisableStartGame(serverRunning, lobbyPlayerCount));
  }

  private void log(String message) {
    consoleArea.appendText(message + "\n");
  }

  private Button createBtn(String text, String color) {
    Button btn = new Button(text);
    btn.setPrefWidth(280);
    btn.setPrefHeight(36);
    btn.setFont(Font.font("Arial", FontWeight.BOLD, 12));
    btn.setStyle(
        "-fx-background-color: "
            + color
            + ";"
            + "-fx-text-fill: white;"
            + "-fx-background-radius: 5;"
            + "-fx-cursor: hand;");
    btn.setOnMouseEntered(e -> btn.setOpacity(0.8));
    btn.setOnMouseExited(e -> btn.setOpacity(1.0));
    return btn;
  }

  private Label styledLabel(String text, Color color) {
    Label l = new Label(text);
    l.setTextFill(color);
    return l;
  }

  private Label styledLabel(String text, Color color, int size, boolean bold) {
    Label l = new Label(text);
    l.setTextFill(color);
    l.setFont(Font.font("Arial", bold ? FontWeight.BOLD : FontWeight.NORMAL, size));
    return l;
  }
}
