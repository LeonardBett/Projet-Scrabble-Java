package fr.ubordeaux.scrabble.view.gui.network;

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
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.SelectionMode;
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
 * The JavaFX window for the network lobby. Allows users to host a server, discover/join local
 * servers, and invite other players in the lobby to start a multiplayer game.
 */
public class NetworkLobbyView extends Stage {

  private static String rootBackgroundStyle() {
    return "-fx-background-color: #1a2a3a;";
  }

  private static int rootSpacing() {
    return 10;
  }

  private static int rootPadding() {
    return 15;
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

  private static String consoleLabelFontFamily() {
    return "Arial";
  }

  private static int consoleLabelFontSize() {
    return 12;
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
    return 800;
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

  private static List<String> formatServerList(List<ServerInfo> servers) {
    return servers.stream()
        .map(s -> formatServerLine(s.getName(), s.getIp(), s.getPort()))
        .toList();
  }

  private final NetworkManager networkManager;
  private TabPane tabPane;

  // Host Tab Controls
  private TextField portField;
  private Button startServerButton;
  private Button stopServerButton;
  private Label serverStatusLabel;

  // Join Tab Controls
  private TextField ipField;
  private TextField joinPortField;
  private Button connectButton;
  private Button disconnectButton;
  private Button joinSelectedButton;
  private ListView<String> serverListView;
  private final ObservableList<ServerInfo> discoveredServers = FXCollections.observableArrayList();

  // Lobby Tab Controls (Visible to both host and clients)
  private int lobbyPlayerCount = 0;
  private ListView<String> playersListView;
  private ListView<String> scoreboardListView;
  private Button refreshScoreboardButton;
  private Button refreshPlayersButton;
  private Button inviteButton;
  private Button cancelInviteButton;
  private Button toggleStatusButton;
  private boolean isAway = false;
  private Button viewPlayerDetailsButton;
  private Label myIdLabel;
  private Button serverStatusButton;
  private Button pingButton;

  // Keeps track of the currently displayed invitation dialog to avoid duplicates
  private Alert currentInvitationDialog = null;

  private TextArea consoleArea;

  private boolean serverRunning = false;
  private boolean clientConnected = false;

  /**
   * Constructs the lobby view.
   *
   * @param bridge the network bridge connecting this view to the backend.
   */
  @SuppressWarnings("this-escape")
  public NetworkLobbyView(NetworkGameBridge bridge) {
    this.networkManager = bridge.getNetworkManager();
    bridge.setLobbyView(this);

    initUi();

    this.setTitle(I18n.translate("lobby.windowTitle"));
    this.initModality(Modality.NONE);
    this.setResizable(false);
    this.setOnCloseRequest(event -> onLobbyClose());

    // Start UDP discovery listening as soon as the window opens
    this.networkManager.startOnlinePlay();
  }

  /**
   * Stops all network background activity when the lobby window is closed.
   * Executes in background thread to prevent blocking the JavaFX Application Thread,
   * but waits for completion with a timeout.
   */
  private void onLobbyClose() {
    Thread shutdownThread = new Thread(() -> {
      networkManager.stopOnlinePlay();
      serverRunning = false;
      clientConnected = false;
    });
    shutdownThread.setDaemon(true);
    shutdownThread.start();
    
    // Wait for shutdown to complete with timeout
    try {
      shutdownThread.join(2000); // 2 seconds max
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
    }
  }

  /** Initializes the main UI components, tabs, and the console text area. */
  private void initUi() {
    VBox root = new VBox(rootSpacing());
    root.setPadding(new Insets(rootPadding()));
    root.setStyle(rootBackgroundStyle());

    Label title = new Label(I18n.translate("lobby.mainTitle"));
    title.setFont(Font.font("Arial", FontWeight.BOLD, 20));
    title.setTextFill(Color.WHITE);

    myIdLabel = new Label("");
    myIdLabel.setFont(Font.font("Arial", FontWeight.NORMAL, 14));
    myIdLabel.setTextFill(Color.LIGHTBLUE);
    root.getChildren().addFirst(new HBox(20, styledLabel("", Color.WHITE, 20, true), myIdLabel));

    tabPane = new TabPane();
    tabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);
    tabPane.setStyle(tabPaneBackgroundStyle());
    tabPane.getTabs().addAll(buildHostTab(), buildJoinTab(), buildLobbyTab());

    consoleArea = new TextArea();
    consoleArea.setEditable(false);
    consoleArea.setPrefHeight(200);
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

  // --- UI Builders for Tabs -------------------------------------------------

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

    content
        .getChildren()
        .addAll(
            styledLabel(I18n.translate("lobby.hostSectionTitle"), Color.WHITE, 15, true),
            desc,
            portRow,
            startServerButton,
            stopServerButton,
            serverStatusLabel);
    tab.setContent(content);
    return tab;
  }

  private Tab buildJoinTab() {
    final Tab tab = new Tab(I18n.translate("lobby.joinTab"));

    VBox content = new VBox(tabContentSpacing());
    content.setPadding(new Insets(tabContentPadding()));
    content.setStyle(tabContentBackgroundStyle());

    serverListView = new ListView<>();
    serverListView.setPrefHeight(140);
    serverListView.setStyle(listViewStyle());

    joinSelectedButton = createBtn(I18n.translate("lobby.joinSelected"), "#4CAF50");
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

    content
        .getChildren()
        .addAll(
            styledLabel(I18n.translate("lobby.autoDiscoveredServersLabel"), Color.WHITE, 13, true),
            serverListView,
            joinSelectedButton,
            new Separator(),
            styledLabel(I18n.translate("lobby.manualConnectionLabel"), Color.WHITE, 13, true),
            ipRow,
            connectButton,
            disconnectButton);
    tab.setContent(content);
    return tab;
  }

  private Tab buildLobbyTab() {
    final Tab tab = new Tab(I18n.translate("lobby.roomTab"));

    VBox content = new VBox(tabContentSpacing());
    content.setPadding(new Insets(tabContentPadding()));
    content.setStyle(tabContentBackgroundStyle());

    playersListView = new ListView<>();
    playersListView.setPrefHeight(120);
    playersListView.setMinHeight(40);
    playersListView.setStyle("-fx-control-inner-background: #1a2a3a; -fx-text-fill: white;");

    Label waitLabel = styledLabel(I18n.translate("lobby.waitHost"), Color.LIGHTGRAY);
    waitLabel.setWrapText(true);
    // Enable multiple selections (holding Ctrl allows selecting multiple opponents)
    playersListView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);

    inviteButton = createBtn(I18n.translate("lobby.invitePlayersButton"), "#FF9800");
    inviteButton.setOnAction(e -> onInvitePlayers());

    pingButton = createBtn(I18n.translate("lobby.pingButton"), "#9C27B0");
    pingButton.setOnAction(e -> networkManager.ping());

    serverStatusButton = createBtn(I18n.translate("lobby.serverStatusButton"), "#607D8B");
    serverStatusButton.setOnAction(e -> networkManager.serverStatus());

    // Cancel button is hidden by default, shown only when an invite is pending
    cancelInviteButton = createBtn(I18n.translate("lobby.cancelInvitationButton"), "#c0392b");
    cancelInviteButton.setOnAction(e -> onCancelInvitation());
    cancelInviteButton.setVisible(false);
    cancelInviteButton.setManaged(false);

    scoreboardListView = new ListView<>();
    scoreboardListView.setPrefHeight(120);
    scoreboardListView.setStyle(listViewStyle());

    refreshScoreboardButton = createBtn(I18n.translate("lobby.refreshScoreboard"), "#9C27B0");
    refreshScoreboardButton.setOnAction(e -> onRefreshScoreboard());

    refreshPlayersButton = createBtn(I18n.translate("lobby.refreshPlayersButton"), "#2196F3");
    refreshPlayersButton.setOnAction(e -> onRefreshPlayers());

    toggleStatusButton = createBtn(I18n.translate("lobby.toggleStatusButton"), "#607D8B");
    toggleStatusButton.setOnAction(e -> onToggleStatus());

    viewPlayerDetailsButton =
        createBtn(I18n.translate("lobby.viewPlayerDetailsButton"), "#009688");
    viewPlayerDetailsButton.setOnAction(e -> onViewPlayerDetails());

    content
        .getChildren()
        .addAll(
            styledLabel(
                I18n.translate("lobby.playerDetailsLabel"), Color.WHITE, 13, true),
            playersListView,
            refreshPlayersButton,
            inviteButton,
            cancelInviteButton,
            new Separator(),
            styledLabel(I18n.translate("lobby.scoreboardLabel"), Color.WHITE, 13, true),
            scoreboardListView,
            toggleStatusButton,
            viewPlayerDetailsButton,
            serverStatusButton,
            pingButton,
            refreshScoreboardButton);
    tab.setContent(content);
    return tab;
  }

  // --- Button Actions -------------------------------------------------------

  private void onStartServer() {
    try {
      int port = Integer.parseInt(portField.getText().trim());

      if (port < 0 || port > 65535) {
        log(I18n.translate("lobby.invalidPortRange"));
        return;
      }

      // We check if the server start is a success
      boolean success = networkManager.serverStart(port);

      if (success) {
        serverRunning = true;
        serverStatusLabel.setText(I18n.translate("lobby.serverRunning", port));
        serverStatusLabel.setTextFill(Color.LIMEGREEN);
        log(I18n.translate("lobby.serverStartedLog", port));
        updateButtonStates();
      } else {
        log(I18n.translate("lobby.serverErrorContent", port));
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(I18n.translate("lobby.serverErrorTitle"));
        alert.setHeaderText(I18n.translate("lobby.serverErrorHeader"));
        alert.setContentText(I18n.translate("lobby.serverErrorContent", port));
        alert.show();
      }
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

    log(I18n.translate("lobby.serverStoppedLog"));
    updateButtonStates();
  }

  private void onJoinSelected() {
    int idx = serverListView.getSelectionModel().getSelectedIndex();
    if (idx < 0 || idx >= discoveredServers.size()) {
      log(I18n.translate("lobby.invalidServerSelection"));
      return;
    }
    doConnect(discoveredServers.get(idx).getIp(), discoveredServers.get(idx).getPort());
  }

  private void onConnect() {
    try {
      int port = Integer.parseInt(joinPortField.getText().trim());

      // Check if the port is valid
      if (port < 0 || port > 65535) {
        log(I18n.translate("lobby.invalidPortRange"));
        return;
      }

      doConnect(ipField.getText().trim(), port);
    } catch (NumberFormatException e) {
      log(I18n.translate("lobby.invalidJoinPort"));
    }
  }

  private void doConnect(String ip, int port) {
    networkManager.join(ip, port);
    clientConnected = true;
    log(I18n.translate("lobby.connecting", ip, port));
    updateButtonStates();

    networkManager.players();
  }

  private void onDisconnect() {
    networkManager.quit();
  }

  /**
   * Called when the client is disconnected (manually or by server shutdown).
   *
   * @param reason the reason for disconnection
   */
  public void onClientDisconnected(String reason) {
    clientConnected = false;
    log(I18n.translate("lobby.disconnected"));
    

    myIdLabel.setText("");

    // We empty gui items
    playersListView.getItems().clear();
    scoreboardListView.getItems().clear();
    resetInviteButtons();

    // We reset away button
    isAway = false;
    toggleStatusButton.setText(I18n.translate("lobby.toggleStatusButton"));
    toggleStatusButton.setStyle(
        "-fx-background-color: #607D8B; -fx-text-fill: white; -fx-cursor: hand;");

    updateButtonStates();
  }

  /**
   * Called when the connection attempt fails.
   *
   * @param reason the failure reason.
   */
  public void onConnectionFailed(String reason) {
    this.clientConnected = false;
    log(I18n.translate("lobby.connectionFailedPrefix") + reason);
    updateButtonStates(); // Re-enable buttons for a new attempt

    Alert alert = new Alert(Alert.AlertType.ERROR);
    alert.setTitle(I18n.translate("lobby.networkErrorTitle"));
    alert.setHeaderText(I18n.translate("lobby.networkErrorHeader"));
    alert.setContentText(reason);
    alert.show();
  }

  private void onRefreshScoreboard() {
    if (clientConnected) {
      networkManager.scoreboard();
    }
  }

  private void onRefreshPlayers() {
    if (clientConnected) {
      networkManager.players();
    }
  }

  private void onToggleStatus() {
    if (!clientConnected) {
      log(I18n.translate("lobby.notConnected"));
      return;
    }

    if (!isAway) {
      networkManager.away();
    } else {
      networkManager.back();
    }
  }

  private void onViewPlayerDetails() {
    ObservableList<String> selected = playersListView.getSelectionModel().getSelectedItems();
    if (selected.size() != 1) {
      log(I18n.translate("lobby.selectOnePlayer"));
      return;
    }

    try {
      // We extract player id from the selected item
      String idStr = selected.getFirst().split("\\s+")[0].replace("#", "");
      networkManager.playersPlayerId(Integer.parseInt(idStr));
    } catch (RuntimeException ex) {
      log(I18n.translate("lobby.errorReadingId", ex.getMessage()));
    }
  }

  /**
   * Displays a popup with the server ping latency.
   *
   * @param latencyMs the response time in milliseconds
   */
  public void onPongReceived(long latencyMs) {
    Alert alert = new Alert(Alert.AlertType.INFORMATION);
    alert.setTitle(I18n.translate("lobby.serverPingTitle"));
    alert.setHeaderText(I18n.translate("lobby.serverPingHeader"));
    alert.setContentText(I18n.translate("lobby.serverPingContent", latencyMs));
    alert.show();
  }

  // --- F40 Invitation Logic -----------------------------------------------

  /**
   * Extracts the IDs from the selected players in the list view and sends the NEW command (which
   * triggers invitations) via the NetworkManager.
   */
  private void onInvitePlayers() {
    ObservableList<String> selected = playersListView.getSelectionModel().getSelectedItems();
    if (selected.isEmpty()) {
      log(I18n.translate("lobby.selectOpponents"));
      return;
    }

    java.util.List<Integer> targetIds = new java.util.ArrayList<>();
    for (String s : selected) {
      try {
        // Parse the string formatted as "#12  PlayerName [STATUS]" to extract '12'.
        targetIds.add(Integer.valueOf(s.split("\\s+")[0].replace("#", "")));
      } catch (RuntimeException ex) {
        log(I18n.translate("lobby.errorReadingIDMessage", ex.getMessage()));
      }
    }

    // Send the appropriate command based on the number of invited opponents
    if (targetIds.size() == 1) {
      networkManager.newPlayerId(targetIds.get(0));
    } else if (targetIds.size() == 2) {
      networkManager.newPlayerId(targetIds.get(0), targetIds.get(1));
    } else if (targetIds.size() >= 3) {
      networkManager.newPlayerId(targetIds.get(0), targetIds.get(1), targetIds.get(2));
    }

    log(I18n.translate("lobby.invitationSentMessage"));

    // Switch the UI to show the 'Cancel' button
    inviteButton.setVisible(false);
    inviteButton.setManaged(false);
    cancelInviteButton.setVisible(true);
    cancelInviteButton.setManaged(true);
  }

  /** Sends the cancel command to the server and resets the UI buttons. */
  private void onCancelInvitation() {
    networkManager.cancel();
    log(I18n.translate("lobby.invitationCancelled"));
    resetInviteButtons();
  }

  /** Restores the default state of the invitation buttons (Show Invite, Hide Cancel). */
  private void resetInviteButtons() {
    inviteButton.setVisible(true);
    inviteButton.setManaged(true);
    cancelInviteButton.setVisible(false);
    cancelInviteButton.setManaged(false);
  }

  /**
   * Displays a confirmation dialog when an invitation is received from another player. Sends ACCEPT
   * or DECLINE to the server based on the user's choice.
   *
   * @param from the name of the inviter.
   */
  public void onInvitationReceived(String from) {
    currentInvitationDialog = new Alert(Alert.AlertType.CONFIRMATION);
    currentInvitationDialog.setTitle(I18n.translate("lobby.newInvitationTitle"));
    currentInvitationDialog.setHeaderText(
        I18n.translate("lobby.invitationHeader", from));
    currentInvitationDialog.setContentText(
        I18n.translate("lobby.invitationContent"));

    ButtonType acceptBtn =
        new ButtonType(I18n.translate("lobby.acceptButton"),
            ButtonBar.ButtonData.YES);
    ButtonType declineBtn =
        new ButtonType(I18n.translate("lobby.declineButton"),
            ButtonBar.ButtonData.NO);
    currentInvitationDialog.getButtonTypes().setAll(acceptBtn, declineBtn);

    currentInvitationDialog
        .showAndWait()
        .ifPresent(
            type -> {
              if (type == acceptBtn) {
                networkManager.accept();
                log(I18n.translate("lobby.youAcceptedInvitation", from));
              } else {
                networkManager.decline();
                log(I18n.translate("lobby.youDeclinedInvitation", from));
              }
              currentInvitationDialog = null;
            });
  }

  /**
   * Called when an ongoing invitation is canceled. Closes the dialog if it was open.
   *
   * @param reason the reason for cancellation.
   */
  public void onInvitationCancelled(String reason) {
    if (currentInvitationDialog != null && currentInvitationDialog.isShowing()) {
      // Force close the dialog if the host canceled while we were looking at it
      currentInvitationDialog.setResult(ButtonType.CANCEL);
      currentInvitationDialog.close();
      currentInvitationDialog = null;
      log(I18n.translate("lobby.invitationCancelledReason", reason));
    } else {
      log(I18n.translate("lobby.invitationCancelledGeneric", reason));
    }
    // Also reset buttons in case we were the host who canceled
    resetInviteButtons();
  }

  // --- Callbacks from NetworkGameBridge -----------------------------------

  /**
   * Updates the list of available servers discovered on the local network.
   *
   * @param servers the list of discovered server information objects
   */
  public void onServerListUpdated(List<ServerInfo> servers) {
    discoveredServers.setAll(servers);
    ObservableList<String> items = FXCollections.observableArrayList(formatServerList(servers));
    serverListView.setItems(items);
  }

  /**
   * Updates the lobby player list when new player data is received from the server.
   *
   * @param players the list of maps containing player data (ID, NAME, STATUS)
   */
  public void onPlayersReceived(List<Map<String, String>> players) {
    ObservableList<String> items = FXCollections.observableArrayList();
    for (Map<String, String> p : players) {
      String id = p.getOrDefault("ID", "?");
      String name = p.getOrDefault("NAME", I18n.translate("lobby.unknownPlayer"));
      String status = p.getOrDefault("STATUS", "?");
      items.add(String.format("#%-4s %-16s [%s]", id, name, status));
    }

    lobbyPlayerCount = players.size();

    // Update players list in lobby tab
    playersListView.setItems(FXCollections.observableArrayList(items));

    // Enable start button for host if >= 2 players
    if (serverRunning) {
      if (lobbyPlayerCount >= 2) {
        log(I18n.translate("lobby.playersReady", lobbyPlayerCount));
      }
    }

    // If this was triggered by onStartGame, now send the NEW command with all IDs
    // This is handled via the playersUpdate callback from bridge
  }

  /**
   * Called when the scoreboard is received.
    playersListView.setItems(items);
  }

  /**
   * Updates the scoreboard view with the latest statistics from the server.
   *
   * @param scoreboard the list of maps containing player statistics (WINS, LOSSES, TOTAL)
   */
  public void onScoreboardReceived(List<Map<String, String>> scoreboard) {
    ObservableList<String> items = FXCollections.observableArrayList();
    int rank = 1;
    for (Map<String, String> entry : scoreboard) {
      String name = entry.getOrDefault("NAME", "?");
      String wins = entry.getOrDefault("WINS", "0");
      String losses = entry.getOrDefault("LOSSES", "0");
      String total = entry.getOrDefault("TOTAL", "0");
      items.add(String.format("%d. %-14s  V:%s  D:%s  T:%s", rank++, name, wins, losses, total));
    }
    scoreboardListView.setItems(items);
  }

  /**
   * Displays a popup with the current server status.
   *
   * @param info map containing PORT, CLIENTS, GAMES
   */
  public void onServerStatusReceived(Map<String, String> info) {
    String port = info.getOrDefault("PORT", "Unknown");
    String clients = info.getOrDefault("CLIENTS", "0");
    String games = info.getOrDefault("GAMES", "0");

    Alert alert = new Alert(Alert.AlertType.INFORMATION);
    alert.setTitle(I18n.translate("lobby.serverStatusTitle"));
    alert.setHeaderText(I18n.translate("lobby.serverStatusHeader"));
    alert.setContentText(
        I18n.translate("lobby.serverStatusPortLabel")
            + port
            + "\n"
            + I18n.translate("lobby.serverStatusClientsLabel")
            + clients
            + "\n"
            + I18n.translate("lobby.serverStatusGamesLabel")
            + games);
    alert.show();
  }

  /**
   * Logs a generic message received from the server.
   *
   * @param message the message string to display
   */
  public void onMessageReceived(String message) {
    log(message);
  }

  /**
   * Open a popup with finals score of this finished game.
   *
   * @param finalScores list if map contenting players names and scores
   */
  public void onGameEnded(List<Map<String, String>> finalScores) {
    resetInviteButtons();

    // We build the score string with data from the finalScores list
    StringBuilder sb = new StringBuilder();
    sb.append(I18n.translate("lobby.gameOverMessage"));
    for (Map<String, String> scoreEntry : finalScores) {
      String name = scoreEntry.getOrDefault("NAME", "Unknown");
      String score = scoreEntry.getOrDefault("SCORE", "0");
      sb.append(I18n.translate("lobby.finalScoreFormat", name, score)).append("\n");
    }

    // We create and open the popup
    Alert alert = new Alert(Alert.AlertType.INFORMATION);
    alert.setTitle(I18n.translate("lobby.gameOverTitle"));
    alert.setHeaderText(I18n.translate("lobby.gameOverHeader"));
    alert.setContentText(sb.toString());
    alert.initOwner(this);
    alert.show();

    // We ask for the new scoreboard and player list
    networkManager.scoreboard();
    networkManager.players();
  }

  /**
   * Called when the game ends with a textual reason.
   *
   * @param reason the end reason message
   */
  public void onGameEnded(String reason) {
    log(I18n.translate("lobby.gameEnded", reason));
    clientConnected = false;
    lobbyPlayerCount = 0;
    updateButtonStates();
  }

  /**
   * Returns whether this lobby is currently hosting a server.
   *
   * @return true when host mode is active
   */
  public boolean isHostMode() {
    return serverRunning;
  }

  /**
   * Returns the number of players currently present in the lobby.
   *
   * @return current lobby player count
   */
  public int getLobbyPlayerCount() {
    return lobbyPlayerCount;
  }

  // ─── Helpers ─────────────────────────────────────────────────────────────

  /**
   * Handles the end of a game by logging the reason and resetting the invitation UI.
   *
   * @param reason the string describing why the game ended
   */
  public void onGameInterrupted(String reason) {
    log(I18n.translate("lobby.gameTerminated", reason));
    resetInviteButtons();

    // We ask for the new player list
    networkManager.players();
  }

  /**
   * Displays a popup with the detailed statistics of a specific player.
   *
   * @param info the map containing the player's detailed information
   */
  public void onPlayerDetailsReceived(Map<String, String> info) {
    String name = info.getOrDefault("NAME", I18n.translate("lobby.unknownPlayer"));
    String status = info.getOrDefault("STATUS", "Unknown");
    String wins = info.getOrDefault("WINS", "0");
    String losses = info.getOrDefault("LOSSES", "0");
    String total = info.getOrDefault("TOTAL", "0");

    Alert alert = new Alert(Alert.AlertType.INFORMATION);
    alert.setTitle(I18n.translate("lobby.playerDetailsTitle"));
    alert.setHeaderText(I18n.translate("lobby.playerDetailsHeader", name));
    alert.setContentText(
        I18n.translate("lobby.playerDetailsStatus") + " ["
            + status
            + "]\n\n"
            + I18n.translate("lobby.playerDetailsWins") + " "
            + wins
            + "\n"
            + I18n.translate("lobby.playerDetailsLosses") + " "
            + losses
            + "\n"
            + I18n.translate("lobby.playerDetailsTotal")
            + " "
            + total);
    alert.show();
  }

  /**
   * Update the away/back button.
   *
   * @param status The new client status
   */
  public void onPlayerStatusChanged(String status) {
    if ("AWAY".equals(status)) {
      isAway = true;
      toggleStatusButton.setText(I18n.translate("lobby.playerStatusBack"));
      toggleStatusButton.setStyle(
          "-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-background-radius: 5;");
    } else if ("IDLE".equals(status)) {
      isAway = false;
      toggleStatusButton.setText(I18n.translate("lobby.playerStatusAway"));
      toggleStatusButton.setStyle(
          "-fx-background-color: #607D8B; -fx-text-fill: white; -fx-background-radius: 5;");
    }
  }

  /** Handled when an invitation failed. Logs the error and resets buttons. */
  public void onInvitationCancel() {
    resetInviteButtons();
  }

  /**
   * Handled when the server send the id to the client.
   *
   * @param id this client id
   */
  public void onWelcomeReceived(int id) {
    myIdLabel.setText(I18n.translate("lobby.myIdLabel", id));
  }

  // --- Helpers -------------------------------------------------------------

  /** Updates the enabled/disabled state of UI buttons based on connection status. */
  private void updateButtonStates() {
    // Host
    portField.setDisable(serverRunning);
    startServerButton.setDisable(serverRunning);
    stopServerButton.setDisable(!serverRunning);

    // Connect
    ipField.setDisable(clientConnected);
    joinPortField.setDisable(clientConnected);
    connectButton.setDisable(clientConnected);
    joinSelectedButton.setDisable(clientConnected);
    disconnectButton.setDisable(!clientConnected);

    // Lobby
    refreshScoreboardButton.setDisable(!clientConnected);
    refreshPlayersButton.setDisable(!clientConnected);
    inviteButton.setDisable(!clientConnected);
    toggleStatusButton.setDisable(!clientConnected);
    viewPlayerDetailsButton.setDisable(!clientConnected);
    serverStatusButton.setDisable(!clientConnected);
    pingButton.setDisable(!clientConnected);
  }

  /**
   * Appends a message to the UI console area.
   *
   * @param message the text to append.
   */
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
            + "; -fx-text-fill: white; -fx-background-radius: 5; -fx-cursor: hand;");

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
