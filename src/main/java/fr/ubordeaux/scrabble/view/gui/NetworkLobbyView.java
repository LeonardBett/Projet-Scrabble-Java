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
 * Fenetre de lobby reseau (JavaFX) pour heberger, rejoindre et suivre les parties en ligne.
 */
public class NetworkLobbyView extends Stage {

  private final NetworkGameBridge bridge;
  private final NetworkManager networkManager;

  private TabPane tabPane;
  private TextField portField;
  private Button startServerButton;
  private Button stopServerButton;
  private Label serverStatusLabel;
  private TextField ipField;
  private TextField joinPortField;
  private Button connectButton;
  private Button disconnectButton;
  private ListView<String> serverListView;
  private final ObservableList<ServerInfo> discoveredServers = FXCollections.observableArrayList();
  private Button refreshServersButton;
  private ListView<String> playersListView;
  private Button refreshPlayersButton;
  private Button challengeButton;
  private TextField playerIdField;
  private ListView<String> scoreboardListView;
  private Button refreshScoreboardButton;
  private TextArea consoleArea;
  private boolean serverRunning = false;
  private boolean clientConnected = false;

  /**
   * Construit la fenêtre de lobby réseau et initialise l'interface.
   *
   * @param bridge le pont entre la vue réseau et le gestionnaire de réseau
   */
  public NetworkLobbyView(NetworkGameBridge bridge) {
    this.bridge = bridge;
    this.networkManager = bridge.getNetworkManager();
    bridge.setLobbyView(this);
    initUi();
    this.setTitle("Scrabble — Multijoueur en ligne");
    this.initModality(Modality.NONE);
    this.setResizable(false);
  }

  private void initUi() {
    VBox root = new VBox(10);
    root.setPadding(new Insets(15));
    root.setStyle("-fx-background-color: #1a2a3a;");

    Label title = new Label("Multijoueur en ligne");
    title.setFont(Font.font("Arial", FontWeight.BOLD, 20));
    title.setTextFill(Color.WHITE);

    tabPane = new TabPane();
    tabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);
    tabPane.setStyle("-fx-background-color: #243447;");
    tabPane.getTabs().addAll(buildHostTab(), buildJoinTab(), buildLobbyTab());

    consoleArea = new TextArea();
    consoleArea.setEditable(false);
    consoleArea.setPrefHeight(100);
    consoleArea.setStyle("-fx-control-inner-background: #0d1b2a; -fx-text-fill: #00ff88; "
        + "-fx-font-family: monospace; -fx-font-size: 11;");
    consoleArea.setPromptText("Logs reseau...");

    VBox.setVgrow(tabPane, Priority.ALWAYS);

    Label consoleLabel = new Label("Console :");
    consoleLabel.setTextFill(Color.LIGHTGRAY);
    consoleLabel.setFont(Font.font("Arial", FontWeight.BOLD, 12));

    root.getChildren().addAll(title, tabPane, consoleLabel, consoleArea);

    Scene scene = new Scene(root, 520, 620);
    this.setScene(scene);
    updateButtonStates();
  }

  private Tab buildHostTab() {
    final Tab tab = new Tab("Heberger");

    VBox content = new VBox(12);
    content.setPadding(new Insets(20));
    content.setStyle("-fx-background-color: #243447;");

    Label desc = styledLabel(
        "Demarrez un serveur pour que d'autres joueurs vous rejoignent sur le reseau local.",
        Color.LIGHTGRAY);
    desc.setWrapText(true);

    HBox portRow = new HBox(10);
    portRow.setAlignment(Pos.CENTER_LEFT);
    portField = new TextField(String.valueOf(NetworkManager.DEFAULT_TCP_PORT));
    portField.setPrefWidth(100);
    portRow.getChildren().addAll(styledLabel("Port TCP :", Color.WHITE), portField);

    startServerButton = createBtn("Demarrer le serveur", "#4CAF50");
    stopServerButton = createBtn("Arreter le serveur", "#F44336");

    serverStatusLabel = styledLabel("Serveur arrete", Color.GRAY);
    serverStatusLabel.setFont(Font.font("Arial", FontWeight.BOLD, 13));

    startServerButton.setOnAction(e -> onStartServer());
    stopServerButton.setOnAction(e -> onStopServer());

    content.getChildren().addAll(
        styledLabel("Heberger une partie", Color.WHITE, 15, true),
        desc, portRow, startServerButton, stopServerButton, serverStatusLabel);

    tab.setContent(content);
    return tab;
  }

  private Tab buildJoinTab() {
    final Tab tab = new Tab("Rejoindre");

    VBox content = new VBox(12);
    content.setPadding(new Insets(20));
    content.setStyle("-fx-background-color: #243447;");

    final Label autoTitle =
        styledLabel("Serveurs detectes automatiquement :", Color.WHITE, 13, true);
    serverListView = new ListView<>();
    serverListView.setPrefHeight(130);
    serverListView.setStyle("-fx-control-inner-background: #1a2a3a; -fx-text-fill: white;");

    refreshServersButton = createBtn("Actualiser la liste", "#2196F3");
    refreshServersButton.setOnAction(e -> onRefreshServers());

    Button joinSelectedButton = createBtn("Rejoindre le serveur selectionne", "#4CAF50");
    joinSelectedButton.setOnAction(e -> onJoinSelected());

    final Label manualTitle = styledLabel("Ou connexion manuelle :", Color.WHITE, 13, true);

    HBox ipRow = new HBox(10);
    ipRow.setAlignment(Pos.CENTER_LEFT);
    ipField = new TextField("localhost");
    ipField.setPrefWidth(160);
    joinPortField = new TextField(String.valueOf(NetworkManager.DEFAULT_TCP_PORT));
    joinPortField.setPrefWidth(80);
    ipRow.getChildren().addAll(
        styledLabel("IP :", Color.WHITE), ipField,
        styledLabel("Port :", Color.WHITE), joinPortField);

    connectButton = createBtn("Se connecter", "#4CAF50");
    disconnectButton = createBtn("Se deconnecter", "#F44336");

    connectButton.setOnAction(e -> onConnect());
    disconnectButton.setOnAction(e -> onDisconnect());

    content.getChildren().addAll(autoTitle, serverListView, refreshServersButton,
        joinSelectedButton, new Separator(), manualTitle, ipRow, connectButton, disconnectButton);

    tab.setContent(content);
    return tab;
  }

  private Tab buildLobbyTab() {
    final Tab tab = new Tab("Salon");

    VBox content = new VBox(12);
    content.setPadding(new Insets(20));
    content.setStyle("-fx-background-color: #243447;");

    final Label playersTitle = styledLabel("Joueurs connectes :", Color.WHITE, 13, true);
    playersListView = new ListView<>();
    playersListView.setPrefHeight(140);
    playersListView.setStyle("-fx-control-inner-background: #1a2a3a; -fx-text-fill: white;");

    refreshPlayersButton = createBtn("Actualiser les joueurs", "#2196F3");
    refreshPlayersButton.setOnAction(e -> onRefreshPlayers());

    HBox challengeRow = new HBox(10);
    challengeRow.setAlignment(Pos.CENTER_LEFT);
    playerIdField = new TextField();
    playerIdField.setPromptText("ID du joueur");
    playerIdField.setPrefWidth(100);
    challengeButton = createBtn("Defier ce joueur", "#FF9800");
    challengeRow.getChildren().addAll(
        styledLabel("ID :", Color.WHITE), playerIdField, challengeButton);
    challengeButton.setOnAction(e -> onChallenge());

    final Label sbTitle = styledLabel("Classement du serveur :", Color.WHITE, 13, true);
    scoreboardListView = new ListView<>();
    scoreboardListView.setPrefHeight(120);
    scoreboardListView.setStyle("-fx-control-inner-background: #1a2a3a; -fx-text-fill: white;");

    refreshScoreboardButton = createBtn("Voir le classement", "#9C27B0");
    refreshScoreboardButton.setOnAction(e -> onRefreshScoreboard());

    content.getChildren().addAll(playersTitle, playersListView, refreshPlayersButton,
        challengeRow, new Separator(), sbTitle, scoreboardListView, refreshScoreboardButton);

    tab.setContent(content);
    return tab;
  }

  private void onStartServer() {
    try {
      int port = Integer.parseInt(portField.getText().trim());
      networkManager.startOnlinePlay();
      networkManager.serverStart(port);
      serverRunning = true;
      serverStatusLabel.setText("Serveur en ecoute sur le port " + port);
      serverStatusLabel.setTextFill(Color.LIMEGREEN);
      log("Serveur demarre sur le port " + port);
      updateButtonStates();
    } catch (NumberFormatException ex) {
      log("Port invalide : " + portField.getText());
    }
  }

  private void onStopServer() {
    networkManager.serverStop();
    serverRunning = false;
    serverStatusLabel.setText("Serveur arrete");
    serverStatusLabel.setTextFill(Color.GRAY);
    log("Serveur arrete.");
    updateButtonStates();
  }

  private void onRefreshServers() {
    networkManager.startOnlinePlay();
    List<ServerInfo> servers = networkManager.serverList();
    discoveredServers.setAll(servers);
    ObservableList<String> items = FXCollections.observableArrayList();
    for (ServerInfo s : servers) {
      items.add(String.format("%-20s  %s:%d", s.getName(), s.getIp(), s.getPort()));
    }
    serverListView.setItems(items);
    log(servers.size() + " serveur(s) trouve(s).");
  }

  private void onJoinSelected() {
    int idx = serverListView.getSelectionModel().getSelectedIndex();
    if (idx < 0 || idx >= discoveredServers.size()) {
      log("Selectionnez un serveur dans la liste.");
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
      log("Port invalide.");
      return;
    }
    doConnect(ip, port);
  }

  private void doConnect(String ip, int port) {
    networkManager.startOnlinePlay();
    networkManager.join(ip, port);
    clientConnected = true;
    log("Connexion a " + ip + ":" + port + "...");
    updateButtonStates();
    tabPane.getSelectionModel().select(2);
    onRefreshPlayers();
  }

  private void onDisconnect() {
    networkManager.quit();
    clientConnected = false;
    log("Deconnecte du serveur.");
    updateButtonStates();
  }

  private void onRefreshPlayers() {
    if (!clientConnected) {
      log("Non connecte.");
      return;
    }
    networkManager.players();
    log("Actualisation des joueurs...");
  }

  private void onChallenge() {
    if (!clientConnected) {
      log("Non connecte.");
      return;
    }
    String idStr = playerIdField.getText().trim();
    try {
      int targetId = Integer.parseInt(idStr);
      networkManager.newPlayerId(targetId);
      log("Defi envoye au joueur #" + targetId);
    } catch (NumberFormatException e) {
      log("ID invalide : " + idStr);
    }
  }

  private void onRefreshScoreboard() {
    if (!clientConnected) {
      log("Non connecte.");
      return;
    }
    networkManager.scoreboard();
    log("Chargement du classement...");
  }

  /**
   * Met à jour la liste des serveurs découverts automatiquement sur le réseau local.
   *
   * @param servers liste des serveurs détectés
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
   * Affiche la liste des joueurs connectés au serveur dans l'onglet Salon.
   *
   * @param players liste de maps contenant les informations de chaque joueur (ID, NAME, STATUS)
   */
  public void onPlayersReceived(List<Map<String, String>> players) {
    ObservableList<String> items = FXCollections.observableArrayList();
    for (Map<String, String> p : players) {
      String id = p.getOrDefault("ID", "?");
      String name = p.getOrDefault("NAME", "Inconnu");
      String status = p.getOrDefault("STATUS", "?");
      items.add(String.format("#%-4s %-16s [%s]", id, name, status));
    }
    playersListView.setItems(items);
  }

  /**
   * Affiche le classement du serveur dans l'onglet Salon.
   *
   * @param scoreboard liste de maps contenant les données de classement
   */
  public void onScoreboardReceived(List<Map<String, String>> scoreboard) {
    ObservableList<String> items = FXCollections.observableArrayList();
    int rank = 1;
    for (Map<String, String> entry : scoreboard) {
      String name = entry.getOrDefault("NAME", "?");
      String wins = entry.getOrDefault("WINS", "0");
      String losses = entry.getOrDefault("LOSSES", "0");
      String total = entry.getOrDefault("TOTAL", "0");
      items.add(String.format("%d. %-14s  V:%s  D:%s  T:%s",
          rank++, name, wins, losses, total));
    }
    scoreboardListView.setItems(items);
  }

  /**
   * Affiche les informations de statut du serveur dans la console.
   *
   * @param info map contenant les données du serveur (PORT, CLIENTS, GAMES)
   */
  public void onServerStatusReceived(Map<String, String> info) {
    log("Serveur — Port: " + info.get("PORT")
        + " | Clients: " + info.get("CLIENTS")
        + " | Parties: " + info.get("GAMES"));
  }

  /**
   * Affiche un message reçu du serveur dans la console.
   *
   * @param message le message à afficher
   */
  public void onMessageReceived(String message) {
    log(message);
  }

  /**
   * Notifie la vue que la partie en ligne est terminée et met à jour l'état des boutons.
   *
   * @param reason message décrivant la raison de la fin de partie
   */
  public void onGameEnded(String reason) {
    log("Partie terminee : " + reason);
    clientConnected = false;
    updateButtonStates();
  }

  private void updateButtonStates() {
    startServerButton.setDisable(serverRunning);
    stopServerButton.setDisable(!serverRunning);
    connectButton.setDisable(clientConnected);
    disconnectButton.setDisable(!clientConnected);
    refreshPlayersButton.setDisable(!clientConnected);
    challengeButton.setDisable(!clientConnected);
    refreshScoreboardButton.setDisable(!clientConnected);
  }

  private void log(String message) {
    consoleArea.appendText(message + "\n");
  }

  private Button createBtn(String text, String color) {
    Button btn = new Button(text);
    btn.setPrefWidth(280);
    btn.setPrefHeight(36);
    btn.setFont(Font.font("Arial", FontWeight.BOLD, 12));
    btn.setStyle("-fx-background-color: " + color + "; -fx-text-fill: white;"
        + " -fx-background-radius: 5; -fx-cursor: hand;");
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