package fr.ubordeaux.scrabble.view.gui;

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

    this.setTitle(windowTitleText());
    this.initModality(Modality.NONE);
    this.setResizable(false);

    this.networkManager.startOnlinePlay();
  }

  // ─── Construction de l'UI ────────────────────────────────────────────────

  private void initUi() {
    VBox root = new VBox(rootSpacing());
    root.setPadding(new Insets(rootPadding()));
    root.setStyle(rootBackgroundStyle());

    Label title = new Label(mainTitleText());
    title.setFont(Font.font(titleFontFamily(), FontWeight.BOLD, titleFontSize()));
    title.setTextFill(Color.WHITE);

    tabPane = new TabPane();
    tabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);
    tabPane.setStyle(tabPaneBackgroundStyle());
    tabPane.getTabs().addAll(buildHostTab(), buildJoinTab(), buildLobbyTab());

    consoleArea = new TextArea();
    consoleArea.setEditable(false);
    consoleArea.setPrefHeight(consoleAreaPrefHeight());
    consoleArea.setStyle(consoleAreaStyle());
    consoleArea.setPromptText(consolePromptText());

    VBox.setVgrow(tabPane, Priority.ALWAYS);

    Label consoleLabel = new Label(consoleLabelText());
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
    final Tab tab = new Tab(hostTabTitleText());

    VBox content = new VBox(tabContentSpacing());
    content.setPadding(new Insets(tabContentPadding()));
    content.setStyle(tabContentBackgroundStyle());

    Label desc =
        styledLabel(
            "Démarrez un serveur. Les joueurs sur le réseau local vous verront automatiquement.",
            Color.LIGHTGRAY);
    desc.setWrapText(true);

    HBox portRow = new HBox(rowSpacing());
    portRow.setAlignment(Pos.CENTER_LEFT);
    portField = new TextField(defaultHostPortText());
    portField.setPrefWidth(100);
    portRow.getChildren().addAll(styledLabel(hostPortLabelText(), Color.WHITE), portField);

    startServerButton = createBtn(startServerButtonText(), startServerButtonColor());
    stopServerButton = createBtn(stopServerButtonText(), stopServerButtonColor());

    serverStatusLabel = styledLabel(serverStoppedStatusText(), Color.GRAY);
    serverStatusLabel.setFont(Font.font(statusLabelFontFamily(), FontWeight.BOLD,
        statusLabelFontSize()));

    startServerButton.setOnAction(e -> onStartServer());
    stopServerButton.setOnAction(e -> onStopServer());

    // Bouton lancer la partie — actif quand >= 2 joueurs
    startGameButton = createBtn(startGameButtonText(), startGameButtonColor());
    startGameButton.setDisable(true);
    startGameButton.setOnAction(e -> onStartGame());

    // Liste des joueurs dans le lobby (mis à jour auto)
    final Label playersTitle =
        styledLabel("Joueurs connectés au salon :", Color.WHITE, 13, true);
    lobbyPlayerListView = new ListView<>();
    lobbyPlayerListView.setPrefHeight(130);
    lobbyPlayerListView.setStyle(listViewStyle());

    content
        .getChildren()
        .addAll(
            styledLabel("Héberger une partie", Color.WHITE, 15, true),
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
    final Tab tab = new Tab(joinTabTitleText());

    VBox content = new VBox(tabContentSpacing());
    content.setPadding(new Insets(tabContentPadding()));
    content.setStyle(tabContentBackgroundStyle());

    serverListView = new ListView<>();
    serverListView.setPrefHeight(140);
    serverListView.setStyle(listViewStyle());

    Button joinSelectedButton = createBtn(joinSelectedButtonText(), connectButtonColor());
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
            styledLabel("IP :", Color.WHITE), ipField,
            styledLabel("Port :", Color.WHITE), joinPortField);

    connectButton = createBtn(connectButtonText(), connectButtonColor());
    disconnectButton = createBtn(disconnectButtonText(), disconnectButtonColor());

    connectButton.setOnAction(e -> onConnect());
    disconnectButton.setOnAction(e -> onDisconnect());

    final Label autoTitle =
        styledLabel(autoServersTitleText(), Color.WHITE, 13, true);
    final Label manualTitle = styledLabel(manualConnectionTitleText(), Color.WHITE, 13, true);

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
    final Tab tab = new Tab(lobbyTabTitleText());

    VBox content = new VBox(tabContentSpacing());
    content.setPadding(new Insets(tabContentPadding()));
    content.setStyle(tabContentBackgroundStyle());

    playersListView = new ListView<>();
    playersListView.setPrefHeight(150);
    playersListView.setStyle(listViewStyle());

    Label waitLabel = styledLabel("En attente que l'hôte lance la partie...", Color.LIGHTGRAY);
    waitLabel.setWrapText(true);

    scoreboardListView = new ListView<>();
    scoreboardListView.setPrefHeight(120);
    scoreboardListView.setStyle(listViewStyle());

    refreshScoreboardButton = createBtn(refreshScoreboardButtonText(), refreshScoreboardColor());
    refreshScoreboardButton.setOnAction(e -> onRefreshScoreboard());

    final Label playersTitle = styledLabel(lobbyPlayersTitleText(), Color.WHITE, 13, true);
    final Label sbTitle = styledLabel(scoreboardTitleText(), Color.WHITE, 13, true);

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
      serverStatusLabel.setText(serverRunningStatusText(port));
      serverStatusLabel.setTextFill(Color.LIMEGREEN);
      log("Serveur démarré sur le port " + port);
      doConnect("127.0.0.1", port);

      updateButtonStates();
    } catch (NumberFormatException ex) {
      log(invalidHostPortMessage(portField.getText()));
    }
  }

  private void onStopServer() {
    networkManager.serverStop();
    serverRunning = false;
    lobbyPlayerCount = 0;
    serverStatusLabel.setText(serverStoppedStatusText());
    serverStatusLabel.setTextFill(Color.GRAY);
    lobbyPlayerListView.getItems().clear();
    log(serverStoppedLogMessage());
    updateButtonStates();
  }

  /**
   * Called by the host to start the game with all currently connected players. Sends NEW commands
   * targeting each connected player ID.
   */
  private void onStartGame() {
    bridge.requestGameStart();
    log(startingGameLogMessage());
  }

  private void onJoinSelected() {
    int idx = serverListView.getSelectionModel().getSelectedIndex();
    if (!isValidSelectedServerIndex(idx, discoveredServers.size())) {
      log(invalidServerSelectionMessage());
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
      log(invalidJoinPortMessage());
      return;
    }
    doConnect(ip, port);
  }

  private void doConnect(String ip, int port) {
    networkManager.startOnlinePlay();
    networkManager.join(ip, port);
    clientConnected = true;
    log(connectingMessage(ip, port));
    updateButtonStates();
    tabPane.getSelectionModel().select(2);
    // Demande la liste des joueurs pour afficher le salon
    networkManager.players();
  }

  private void onDisconnect() {
    networkManager.quit();
    clientConnected = false;
    log(disconnectedMessage());
    updateButtonStates();
  }

  private void onRefreshScoreboard() {
    if (!canRefreshScoreboard(clientConnected)) {
      log(notConnectedMessage());
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
    serverListView.setItems(FXCollections.observableArrayList(formatServerList(servers)));
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

    ObservableList<String> items = FXCollections.observableArrayList(formatPlayers(players));

    // Update both views
    lobbyPlayerListView.setItems(FXCollections.observableArrayList(items));
    playersListView.setItems(FXCollections.observableArrayList(items));

    // Enable start button for host if >= 2 players
    if (serverRunning) {
      startGameButton.setDisable(!canHostStartGame(lobbyPlayerCount));

      // Auto-launch when exactly 2 players are ready (host + 1 client)
      if (shouldLogPlayersReady(lobbyPlayerCount)) {
        log(playersReadyMessage(lobbyPlayerCount));
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
    scoreboardListView.setItems(FXCollections.observableArrayList(formatScoreboard(scoreboard)));
  }

  /**
   * Called when server status info is received.
   *
   * @param info the status info map
   */
  public void onServerStatusReceived(Map<String, String> info) {
    log(
        "📊 Serveur — Port: "
            + info.get("PORT")
            + " | Clients: "
            + info.get("CLIENTS")
            + " | Parties: "
            + info.get("GAMES"));
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
    log(gameEndedMessage(reason));
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

  static List<String> formatServerList(List<ServerInfo> servers) {
    ObservableList<String> items = FXCollections.observableArrayList();
    for (ServerInfo s : servers) {
      items.add(formatServerLine(s.getName(), s.getIp(), s.getPort()));
    }
    return items;
  }

  static List<String> formatPlayers(List<Map<String, String>> players) {
    ObservableList<String> items = FXCollections.observableArrayList();
    for (Map<String, String> p : players) {
      String id = p.getOrDefault("ID", "?");
      String name = p.getOrDefault("NAME", "Inconnu");
      String status = p.getOrDefault("STATUS", "?");
      items.add(formatPlayerLine(id, name, status));
    }
    return items;
  }

  static List<String> formatScoreboard(List<Map<String, String>> scoreboard) {
    ObservableList<String> items = FXCollections.observableArrayList();
    int rank = 1;
    for (Map<String, String> entry : scoreboard) {
      String name = entry.getOrDefault("NAME", "?");
      String wins = entry.getOrDefault("WINS", "0");
      String losses = entry.getOrDefault("LOSSES", "0");
      String total = entry.getOrDefault("TOTAL", "0");
      items.add(formatScoreboardLine(rank++, name, wins, losses, total));
    }
    return items;
  }

  static String formatServerStatus(Map<String, String> info) {
    return "📊 Serveur — Port: " + info.get("PORT")
        + " | Clients: " + info.get("CLIENTS")
        + " | Parties: " + info.get("GAMES");
  }

  static String formatServerLine(String name, String ip, int port) {
    return String.format("%-20s  %s:%d", name, ip, port);
  }

  static String formatPlayerLine(String id, String name, String status) {
    return String.format("#%-4s %-16s [%s]", id, name, status);
  }

  static String formatScoreboardLine(int rank, String name, String wins, String losses,
      String total) {
    return String.format("%d. %-14s  V:%s  D:%s  T:%s", rank, name, wins, losses, total);
  }

  static String serverRunningStatusText(int port) {
    return "● Serveur en écoute sur le port " + port;
  }

  static String defaultHostPortText() {
    return String.valueOf(NetworkManager.DEFAULT_TCP_PORT);
  }

  static String defaultJoinHostText() {
    return "localhost";
  }

  static String defaultJoinPortText() {
    return String.valueOf(NetworkManager.DEFAULT_TCP_PORT);
  }

  static String consolePromptText() {
    return "Logs réseau...";
  }

  static String serverStoppedStatusText() {
    return "● Serveur arrêté";
  }

  static String serverStartedLogMessage(int port) {
    return "Serveur démarré sur le port " + port;
  }

  static String serverStoppedLogMessage() {
    return "Serveur arrêté.";
  }

  static String invalidHostPortMessage(String rawPort) {
    return "❌ Port invalide : " + rawPort;
  }

  static String invalidJoinPortMessage() {
    return "❌ Port invalide.";
  }

  static boolean isValidSelectedServerIndex(int selectedIndex, int serverCount) {
    return selectedIndex >= 0 && selectedIndex < serverCount;
  }

  static String invalidServerSelectionMessage() {
    return "❌ Sélectionnez un serveur dans la liste.";
  }

  static String connectingMessage(String ip, int port) {
    return "🔗 Connexion à " + ip + ":" + port + "...";
  }

  static String notConnectedMessage() {
    return "❌ Non connecté.";
  }

  static String playersReadyMessage(int count) {
    return "✅ " + count + " joueur(s) connecté(s) — vous pouvez lancer la partie.";
  }

  static boolean shouldLogPlayersReady(int count) {
    return count >= 2;
  }

  static String gameEndedMessage(String reason) {
    return "🏁 Partie terminée : " + reason;
  }

  static String windowTitleText() {
    return "🌐 Scrabble — Multijoueur en ligne";
  }

  static String mainTitleText() {
    return "🌐 Multijoueur en ligne";
  }

  static String consoleLabelText() {
    return "Console :";
  }

  static String rootBackgroundStyle() {
    return "-fx-background-color: #1a2a3a;";
  }

  static int rootSpacing() {
    return 10;
  }

  static int rootPadding() {
    return 15;
  }

  static String titleFontFamily() {
    return "Arial";
  }

  static int titleFontSize() {
    return 20;
  }

  static int consoleAreaPrefHeight() {
    return 100;
  }

  static String consoleLabelFontFamily() {
    return "Arial";
  }

  static int consoleLabelFontSize() {
    return 12;
  }

  static int tabContentSpacing() {
    return 12;
  }

  static int tabContentPadding() {
    return 20;
  }

  static int rowSpacing() {
    return 10;
  }

  static String statusLabelFontFamily() {
    return "Arial";
  }

  static int statusLabelFontSize() {
    return 13;
  }

  static String tabPaneBackgroundStyle() {
    return "-fx-background-color: #243447;";
  }

  static String tabContentBackgroundStyle() {
    return "-fx-background-color: #243447;";
  }

  static String listViewStyle() {
    return "-fx-control-inner-background: #1a2a3a; -fx-text-fill: white;";
  }

  static String consoleAreaStyle() {
    return "-fx-control-inner-background: #0d1b2a; -fx-text-fill: #00ff88; "
        + "-fx-font-family: monospace; -fx-font-size: 11;";
  }

  static int sceneWidth() {
    return 520;
  }

  static int sceneHeight() {
    return 640;
  }

  static String hostTabTitleText() {
    return "🖥  Héberger";
  }

  static String joinTabTitleText() {
    return "🔍 Rejoindre";
  }

  static String lobbyTabTitleText() {
    return "🎮 Salon";
  }

  static String hostDescriptionText() {
    return "Démarrez un serveur. Les joueurs sur le réseau local vous verront automatiquement.";
  }

  static String hostPortLabelText() {
    return "Port TCP :";
  }

  static String startServerButtonText() {
    return "▶  Démarrer le serveur";
  }

  static String stopServerButtonText() {
    return "■  Arrêter le serveur";
  }

  static String startGameButtonText() {
    return "🎮 Lancer la partie";
  }

  static String hostPlayersTitleText() {
    return "Joueurs connectés au salon :";
  }

  static String hostSectionTitleText() {
    return "Héberger une partie";
  }

  static String joinSelectedButtonText() {
    return "🎮 Rejoindre le serveur sélectionné";
  }

  static String ipLabelText() {
    return "IP :";
  }

  static String portLabelText() {
    return "Port :";
  }

  static String connectButtonText() {
    return "🔗 Se connecter";
  }

  static String disconnectButtonText() {
    return "✖  Se déconnecter";
  }

  static String autoServersTitleText() {
    return "Serveurs détectés automatiquement :";
  }

  static String manualConnectionTitleText() {
    return "Ou connexion manuelle :";
  }

  static String lobbyWaitingText() {
    return "En attente que l'hôte lance la partie...";
  }

  static String refreshScoreboardButtonText() {
    return "🏆 Voir le classement";
  }

  static String lobbyPlayersTitleText() {
    return "Joueurs connectés :";
  }

  static String scoreboardTitleText() {
    return "Classement du serveur :";
  }

  static String startServerButtonColor() {
    return "#4CAF50";
  }

  static String stopServerButtonColor() {
    return "#F44336";
  }

  static String startGameButtonColor() {
    return "#FF9800";
  }

  static String connectButtonColor() {
    return "#4CAF50";
  }

  static String disconnectButtonColor() {
    return "#F44336";
  }

  static String refreshScoreboardColor() {
    return "#9C27B0";
  }

  static String startingGameLogMessage() {
    return "🎮 Lancement de la partie...";
  }

  static String disconnectedMessage() {
    return "✖ Déconnecté du serveur.";
  }

  static boolean canRefreshScoreboard(boolean connected) {
    return connected;
  }

  static boolean canHostStartGame(int playerCount) {
    return playerCount >= 2;
  }

  static boolean shouldDisableStartServer(boolean serverRunningFlag) {
    return serverRunningFlag;
  }

  static boolean shouldDisableStopServer(boolean serverRunningFlag) {
    return !serverRunningFlag;
  }

  static boolean shouldDisableConnect(boolean clientConnectedFlag) {
    return clientConnectedFlag;
  }

  static boolean shouldDisableDisconnect(boolean clientConnectedFlag) {
    return !clientConnectedFlag;
  }

  static boolean shouldDisableRefreshScoreboard(boolean clientConnectedFlag) {
    return !clientConnectedFlag;
  }

  static boolean shouldDisableStartGame(boolean serverRunningFlag, int playerCount) {
    return !serverRunningFlag || playerCount < 2;
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
    btn.setStyle("-fx-background-color: " + color + ";"
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
