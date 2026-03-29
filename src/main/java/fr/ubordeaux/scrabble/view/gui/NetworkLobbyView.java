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

  private final NetworkGameBridge bridge;
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
  public NetworkLobbyView(NetworkGameBridge bridge) {
    this.bridge = bridge;
    this.networkManager = bridge.getNetworkManager();
    bridge.setLobbyView(this);

    initUi();

    this.setTitle(I18n.translate("lobby.windowTitle"));
    this.initModality(Modality.NONE);
    this.setResizable(false);

    // Start UDP discovery listening as soon as the window opens
    this.networkManager.startOnlinePlay();
  }

  /** Initializes the main UI components, tabs, and the console text area. */
  private void initUi() {
    VBox root = new VBox(10);
    root.setPadding(new Insets(15));
    root.setStyle("-fx-background-color: #1a2a3a;");

    Label title = new Label(I18n.translate("lobby.mainTitle"));
    title.setFont(Font.font("Arial", FontWeight.BOLD, 20));
    title.setTextFill(Color.WHITE);

    myIdLabel = new Label("");
    myIdLabel.setFont(Font.font("Arial", FontWeight.NORMAL, 14));
    myIdLabel.setTextFill(Color.LIGHTBLUE);
    root.getChildren().addFirst(new HBox(20, styledLabel("", Color.WHITE, 20, true), myIdLabel));

    tabPane = new TabPane();
    tabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);
    tabPane.setStyle("-fx-background-color: #243447;");
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
    consoleLabel.setFont(Font.font("Arial", FontWeight.BOLD, 12));

    root.getChildren().addAll(title, tabPane, consoleLabel, consoleArea);

    Scene scene = new Scene(root, 520, 800);
    this.setScene(scene);
    updateButtonStates();
  }

  // --- UI Builders for Tabs -------------------------------------------------

  private Tab buildHostTab() {
    final Tab tab = new Tab(I18n.translate("lobby.hostTab"));

    VBox content = new VBox(5);
    content.setPadding(new Insets(20));
    content.setStyle("-fx-background-color: #243447;");

    Label desc =
        styledLabel(
        I18n.translate("lobby.hostDescription"),
            Color.LIGHTGRAY);
    desc.setWrapText(true);

    HBox portRow = new HBox(10);
    portRow.setAlignment(Pos.CENTER_LEFT);
    portField = new TextField(String.valueOf(NetworkManager.DEFAULT_TCP_PORT));
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

    VBox content = new VBox(12);
    content.setPadding(new Insets(20));
    content.setStyle("-fx-background-color: #243447;");

    serverListView = new ListView<>();
    serverListView.setPrefHeight(140);
    serverListView.setStyle("-fx-control-inner-background: #1a2a3a; -fx-text-fill: white;");

    Button joinSelectedButton = createBtn(I18n.translate("lobby.joinSelected"), "#4CAF50");
    joinSelectedButton.setOnAction(e -> onJoinSelected());

    HBox ipRow = new HBox(10);
    ipRow.setAlignment(Pos.CENTER_LEFT);
    ipField = new TextField("localhost");
    ipField.setPrefWidth(160);
    joinPortField = new TextField(String.valueOf(NetworkManager.DEFAULT_TCP_PORT));
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
            styledLabel("Auto-discovered Servers :", Color.WHITE, 13, true),
            serverListView,
            joinSelectedButton,
            new Separator(),
            styledLabel("Or Manual Connection :", Color.WHITE, 13, true),
            ipRow,
            connectButton,
            disconnectButton);
    tab.setContent(content);
    return tab;
  }

  private Tab buildLobbyTab() {
    final Tab tab = new Tab(I18n.translate("lobby.roomTab"));

    VBox content = new VBox(12);
    content.setPadding(new Insets(20));
    content.setStyle("-fx-background-color: #243447;");

    playersListView = new ListView<>();
    playersListView.setPrefHeight(120);
    playersListView.setMinHeight(40);
    playersListView.setStyle("-fx-control-inner-background: #1a2a3a; -fx-text-fill: white;");

    // Enable multiple selections (holding Ctrl allows selecting multiple opponents)
    playersListView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);

    inviteButton = createBtn("Invite Selected Players", "#FF9800");
    inviteButton.setOnAction(e -> onInvitePlayers());

    pingButton = createBtn("Ping Server", "#9C27B0");
    pingButton.setOnAction(e -> networkManager.ping());

    serverStatusButton = createBtn("Server Status", "#607D8B");
    serverStatusButton.setOnAction(e -> networkManager.serverStatus());

    // Cancel button is hidden by default, shown only when an invite is pending
    cancelInviteButton = createBtn("Cancel Invitation", "#c0392b");
    cancelInviteButton.setOnAction(e -> onCancelInvitation());
    cancelInviteButton.setVisible(false);
    cancelInviteButton.setManaged(false);

    scoreboardListView = new ListView<>();
    scoreboardListView.setPrefHeight(120);
    scoreboardListView.setMinHeight(40);
    scoreboardListView.setStyle("-fx-control-inner-background: #1a2a3a; -fx-text-fill: white;");

    refreshScoreboardButton = createBtn(I18n.translate("lobby.refreshScoreboard"), "#9C27B0");
    refreshScoreboardButton.setOnAction(e -> onRefreshScoreboard());

    refreshPlayersButton = createBtn("Refresh Players", "#2196F3"); // Bleu
    refreshPlayersButton.setOnAction(e -> onRefreshPlayers());

    toggleStatusButton = createBtn("Go Away (AWAY)", "#607D8B"); // Gris bleuté
    toggleStatusButton.setOnAction(e -> onToggleStatus());

    viewPlayerDetailsButton = createBtn("View Player Details", "#009688"); // Vert canard (Teal)
    viewPlayerDetailsButton.setOnAction(e -> onViewPlayerDetails());

    content
        .getChildren()
        .addAll(
            styledLabel(
                "Connected Players (Ctrl+Click to select multiple) :", Color.WHITE, 13, true),
            playersListView,
            refreshPlayersButton,
            inviteButton,
            cancelInviteButton,
            new Separator(),
            styledLabel("Server Scoreboard :", Color.WHITE, 13, true),
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
        log("Error: Port must be between 0 and 65535.");
        return;
      }

      // We check if the server start is a success
      boolean success = networkManager.serverStart(port);

      if (success) {
        serverRunning = true;
        serverStatusLabel.setText("Server running on port " + port);
        serverStatusLabel.setTextFill(Color.LIMEGREEN);
        log("Server started on port " + port);
        updateButtonStates();
      } else {
        log("Failed: Port " + port + " is invalid or already in use.");
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Server Error");
        alert.setHeaderText("Unable to start server");
        alert.setContentText("Port " + port + " is probably already in use or invalid.");
        alert.show();
      }
    } catch (NumberFormatException ex) {
      log(I18n.translate("lobby.invalidHostPort", portField.getText()));
    }
  }

  private void onStopServer() {
    networkManager.serverStop();
    serverRunning = false;
    serverStatusLabel.setText(I18n.translate("lobby.serverStopped"));
    serverStatusLabel.setTextFill(Color.GRAY);
    playersListView.getItems().clear();
    scoreboardListView.getItems().clear();
    log(I18n.translate("lobby.serverStoppedLog"));
    updateButtonStates();
  }

  private void onJoinSelected() {
    int idx = serverListView.getSelectionModel().getSelectedIndex();
    if (idx < 0 || idx >= discoveredServers.size()) {
      return;
    }
    doConnect(discoveredServers.get(idx).getIp(), discoveredServers.get(idx).getPort());
  }

  private void onConnect() {
    try {
      int port = Integer.parseInt(joinPortField.getText().trim());

      // Check if the port is valid
      if (port < 0 || port > 65535) {
        log("Error: Port must be between 0 and 65535.");
        return;
      }

      doConnect(ipField.getText().trim(), port);
    } catch (NumberFormatException e) {
      log("Invalid port.");
    }
  }

  /**
   * Connects the client to the specified IP and Port, then shifts UI focus to the Lobby tab.
   *
   * @param ip the server IP.
   * @param port the server Port.
   */
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
    log("Disconnected: " + reason);

    myIdLabel.setText("");

    // We empty gui items
    playersListView.getItems().clear();
    scoreboardListView.getItems().clear();
    resetInviteButtons();

    // We reset away button
    isAway = false;
    toggleStatusButton.setText("Go Away (AWAY)");
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
    log("Connection failed: " + reason);
    updateButtonStates(); // Re-enable buttons for a new attempt

    Alert alert = new Alert(Alert.AlertType.ERROR);
    alert.setTitle("Network Error");
    alert.setHeaderText("Unable to connect");
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
      log("Please select exactly one player from the list to view their details.");
      return;
    }

    try {
      // We extract player id from the selected item
      String idStr = selected.getFirst().split("\\s+")[0].replace("#", "");
      networkManager.playersPlayerId(Integer.parseInt(idStr));
    } catch (Exception ignored) {
      log("Error reading ID: " + ignored.getMessage());
    }
  }

  /**
   * Displays a popup with the server ping latency.
   *
   * @param latencyMs the response time in milliseconds
   */
  public void onPongReceived(long latencyMs) {
    Alert alert = new Alert(Alert.AlertType.INFORMATION);
    alert.setTitle("Server Ping");
    alert.setHeaderText("Connection Test");
    alert.setContentText("Response time: " + latencyMs + " ms");
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
      log("Please select at least one opponent from the list.");
      return;
    }

    java.util.List<Integer> targetIds = new java.util.ArrayList<>();
    for (String s : selected) {
      try {
        // Parse the string formatted as "#12  PlayerName [STATUS]" to extract '12'
        String idStr = s.split("\\s+")[0].replace("#", "");
        targetIds.add(Integer.parseInt(idStr));
      } catch (Exception ignored) {
        log("Error reading ID: " + ignored.getMessage());
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

    log("Invitation sent! Waiting for responses...");

    // Switch the UI to show the 'Cancel' button
    inviteButton.setVisible(false);
    inviteButton.setManaged(false);
    cancelInviteButton.setVisible(true);
    cancelInviteButton.setManaged(true);
  }

  /** Sends the cancel command to the server and resets the UI buttons. */
  private void onCancelInvitation() {
    networkManager.cancel();
    log("You cancelled your invitation.");
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
    currentInvitationDialog.setTitle("New Invitation");
    currentInvitationDialog.setHeaderText("Invitation from: " + from);
    currentInvitationDialog.setContentText("Do you want to accept and join the game?");

    ButtonType acceptBtn = new ButtonType("Accept", ButtonBar.ButtonData.YES);
    ButtonType declineBtn = new ButtonType("Decline", ButtonBar.ButtonData.NO);
    currentInvitationDialog.getButtonTypes().setAll(acceptBtn, declineBtn);

    currentInvitationDialog
        .showAndWait()
        .ifPresent(
            type -> {
              if (type == acceptBtn) {
                networkManager.accept();
                log("You accepted the invitation from " + from + ".");
              } else {
                networkManager.decline();
                log("You declined the invitation from " + from + ".");
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
      log("The invitation was cancelled (" + reason + ").");
    } else {
      log("Invitation cancelled: " + reason);
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
    ObservableList<String> items = FXCollections.observableArrayList();
    for (ServerInfo s : servers) {
      items.add(String.format("%-20s  %s:%d", s.getName(), s.getIp(), s.getPort()));
    }
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
      String w = entry.getOrDefault("WINS", "0");
      String l = entry.getOrDefault("LOSSES", "0");
      String t = entry.getOrDefault("TOTAL", "0");
      items.add(String.format("%d. %-14s  W:%s  L:%s  T:%s", rank++, name, w, l, t));
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
    alert.setTitle("Server Status");
    alert.setHeaderText("Live server information");
    alert.setContentText(
        "TCP Listening Port: "
            + port
            + "\n"
            + "Connected Clients: "
            + clients
            + "\n"
            + "Games in Progress: "
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
    sb.append("The game is over. Here are the final scores:\n\n");
    for (Map<String, String> scoreEntry : finalScores) {
      String name = scoreEntry.getOrDefault("NAME", "Unknown");
      String score = scoreEntry.getOrDefault("SCORE", "0");
      sb.append("- ").append(name).append(": ").append(score).append(" points\n");
    }

    // We create and open the popup
    Alert alert = new Alert(Alert.AlertType.INFORMATION);
    alert.setTitle("Game Over");
    alert.setHeaderText("Match Results");
    alert.setContentText(sb.toString());
    alert.initOwner(this);
    alert.show();

    // We ask for the new scoreboard and player list
    networkManager.scoreboard();
    networkManager.players();
  }

  /**
   * Handles the end of a game by logging the reason and resetting the invitation UI.
   *
   * @param reason the string describing why the game ended
   */
  public void onGameInterrupted(String reason) {
    log("Game terminated: " + reason);
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
    String name = info.getOrDefault("NAME", "Unknown");
    String status = info.getOrDefault("STATUS", "Unknown");
    String wins = info.getOrDefault("WINS", "0");
    String losses = info.getOrDefault("LOSSES", "0");
    String total = info.getOrDefault("TOTAL", "0");

    Alert alert = new Alert(Alert.AlertType.INFORMATION);
    alert.setTitle("Player Details");
    alert.setHeaderText("Profile of: " + name);
    alert.setContentText(
        "Current Status: ["
            + status
            + "]\n\n"
            + "Wins: "
            + wins
            + "\n"
            + "Losses: "
            + losses
            + "\n"
            + "Games Played: "
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
      toggleStatusButton.setText("Return to Keyboard (BACK)");
      toggleStatusButton.setStyle(
          "-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-background-radius: 5;");
    } else if ("IDLE".equals(status)) {
      isAway = false;
      toggleStatusButton.setText("Go Away (AWAY)");
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
    myIdLabel.setText("My ID: #" + id);
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
