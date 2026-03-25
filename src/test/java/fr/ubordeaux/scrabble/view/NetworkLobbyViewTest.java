package fr.ubordeaux.scrabble.view;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import fr.ubordeaux.scrabble.model.network.NetworkManager;
import fr.ubordeaux.scrabble.model.network.server.ServerInfo;
import fr.ubordeaux.scrabble.view.gui.NetworkLobbyView;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

class NetworkLobbyViewTest {

  @Test
  void classShouldBeReachable() {
    assertNotNull(NetworkLobbyView.class);
  }


  @Test
  void shouldExposeMainUpdateMethods() {
    assertDoesNotThrow(() -> {
      Method players = NetworkLobbyView.class.getDeclaredMethod(
          "onPlayersReceived", java.util.List.class);
      Method servers = NetworkLobbyView.class.getDeclaredMethod(
          "onServerListUpdated", java.util.List.class);
      Method scoreboard = NetworkLobbyView.class.getDeclaredMethod(
          "onScoreboardReceived", java.util.List.class);
      assertNotNull(players);
      assertNotNull(servers);
      assertNotNull(scoreboard);
    });
  }

  @Test
  void formatServerListShouldBuildReadableEntries() {
    @SuppressWarnings("unchecked")
    List<String> items = (List<String>) invokeStatic("formatServerList", List.class,
        List.of(new ServerInfo("127.0.0.1", 12345, "srv")));
    assertEquals(1, items.size());
    assertNotNull(items.getFirst());
  }

  @Test
  void formatPlayersShouldIncludeIdAndStatus() {
    @SuppressWarnings("unchecked")
    List<String> items = (List<String>) invokeStatic("formatPlayers", List.class,
        List.of(Map.of("ID", "1", "NAME", "Alice", "STATUS", "READY")));
    assertEquals(1, items.size());
    assertNotNull(items.getFirst());
  }

  @Test
  void formatScoreboardShouldProduceRankedLines() {
    @SuppressWarnings("unchecked")
    List<String> items = (List<String>) invokeStatic("formatScoreboard", List.class,
        List.of(Map.of("NAME", "Alice", "WINS", "2", "LOSSES", "1", "TOTAL", "3")));
    assertEquals(1, items.size());
    assertNotNull(items.getFirst());
  }

  @Test
  void formatServerStatusShouldIncludeMetrics() {
    String status = (String) invokeStatic("formatServerStatus", Map.class,
        Map.of("PORT", "12345", "CLIENTS", "2", "GAMES", "1"));
    assertNotNull(status);
  }

  @Test
  void extractedHelperMessagesShouldBeConsistent() {
    assertEquals("● Serveur en écoute sur le port 5555",
        invokeStatic("serverRunningStatusText", int.class, 5555));
    assertEquals("● Serveur arrêté", invokeStatic("serverStoppedStatusText"));
    assertEquals("Serveur démarré sur le port 7777",
        invokeStatic("serverStartedLogMessage", int.class, 7777));
    assertEquals("Serveur arrêté.", invokeStatic("serverStoppedLogMessage"));
    assertEquals("❌ Port invalide : bad",
        invokeStatic("invalidHostPortMessage", String.class, "bad"));
    assertEquals("❌ Port invalide.", invokeStatic("invalidJoinPortMessage"));
    assertEquals("❌ Sélectionnez un serveur dans la liste.",
        invokeStatic("invalidServerSelectionMessage"));
    assertEquals("❌ Non connecté.", invokeStatic("notConnectedMessage"));
    assertEquals("🔗 Connexion à localhost:1234...",
        invokeStatic("connectingMessage", String.class, int.class, "localhost", 1234));
    assertEquals("✅ 3 joueur(s) connecté(s) — vous pouvez lancer la partie.",
        invokeStatic("playersReadyMessage", int.class, 3));
    assertEquals("🏁 Partie terminée : fin",
        invokeStatic("gameEndedMessage", String.class, "fin"));
  }

  @Test
  void extractedHelperValidationShouldCheckSelectedServerBounds() {
    assertEquals(true, invokeStatic("isValidSelectedServerIndex", int.class, int.class, 0, 1));
    assertEquals(false, invokeStatic("isValidSelectedServerIndex", int.class, int.class, -1, 1));
    assertEquals(false, invokeStatic("isValidSelectedServerIndex", int.class, int.class, 2, 2));
  }

  @Test
  void extractedStateHelpersShouldReflectLobbyRules() {
    assertEquals("🎮 Lancement de la partie...", invokeStatic("startingGameLogMessage"));
    assertEquals("✖ Déconnecté du serveur.", invokeStatic("disconnectedMessage"));

    assertEquals(true, invokeStatic("canRefreshScoreboard", boolean.class, true));
    assertEquals(false, invokeStatic("canRefreshScoreboard", boolean.class, false));
    assertEquals(true, invokeStatic("canHostStartGame", int.class, 2));
    assertEquals(false, invokeStatic("canHostStartGame", int.class, 1));

    assertEquals(true, invokeStatic("shouldDisableStartServer", boolean.class, true));
    assertEquals(false, invokeStatic("shouldDisableStartServer", boolean.class, false));
    assertEquals(true, invokeStatic("shouldDisableStopServer", boolean.class, false));
    assertEquals(false, invokeStatic("shouldDisableStopServer", boolean.class, true));
    assertEquals(true, invokeStatic("shouldDisableConnect", boolean.class, true));
    assertEquals(false, invokeStatic("shouldDisableConnect", boolean.class, false));
    assertEquals(true, invokeStatic("shouldDisableDisconnect", boolean.class, false));
    assertEquals(false, invokeStatic("shouldDisableDisconnect", boolean.class, true));
    assertEquals(true, invokeStatic("shouldDisableRefreshScoreboard", boolean.class, false));
    assertEquals(false, invokeStatic("shouldDisableRefreshScoreboard", boolean.class, true));
    assertEquals(true, invokeStatic("shouldDisableStartGame", boolean.class, int.class, false, 2));
    assertEquals(true, invokeStatic("shouldDisableStartGame", boolean.class, int.class, true, 1));
    assertEquals(false, invokeStatic("shouldDisableStartGame", boolean.class, int.class, true, 2));
  }

  @Test
  void extractedFormattingHelpersShouldBuildExpectedLines() {
    assertEquals("srv                   127.0.0.1:12345",
        invokeStatic("formatServerLine", String.class, String.class, int.class,
            "srv", "127.0.0.1", 12345));
    assertEquals("#1    Alice            [READY]",
        invokeStatic("formatPlayerLine", String.class, String.class, String.class,
            "1", "Alice", "READY"));
    assertEquals("2. Alice           V:3  D:1  T:4",
        invokeStatic("formatScoreboardLine", int.class, String.class, String.class,
            String.class, String.class, 2, "Alice", "3", "1", "4"));
    assertEquals(true, invokeStatic("shouldLogPlayersReady", int.class, 2));
    assertEquals(false, invokeStatic("shouldLogPlayersReady", int.class, 1));
    assertEquals(String.valueOf(NetworkManager.DEFAULT_TCP_PORT),
        invokeStatic("defaultHostPortText"));
    assertEquals("localhost", invokeStatic("defaultJoinHostText"));
    assertEquals(String.valueOf(NetworkManager.DEFAULT_TCP_PORT),
        invokeStatic("defaultJoinPortText"));
    assertEquals("Logs réseau...", invokeStatic("consolePromptText"));
    assertEquals("🌐 Scrabble — Multijoueur en ligne", invokeStatic("windowTitleText"));
    assertEquals("🌐 Multijoueur en ligne", invokeStatic("mainTitleText"));
    assertEquals("Console :", invokeStatic("consoleLabelText"));
    assertEquals("-fx-background-color: #1a2a3a;", invokeStatic("rootBackgroundStyle"));
    assertEquals(10, invokeStatic("rootSpacing"));
    assertEquals(15, invokeStatic("rootPadding"));
    assertEquals("Arial", invokeStatic("titleFontFamily"));
    assertEquals(20, invokeStatic("titleFontSize"));
    assertEquals(100, invokeStatic("consoleAreaPrefHeight"));
    assertEquals("Arial", invokeStatic("consoleLabelFontFamily"));
    assertEquals(12, invokeStatic("consoleLabelFontSize"));
    assertEquals(12, invokeStatic("tabContentSpacing"));
    assertEquals(20, invokeStatic("tabContentPadding"));
    assertEquals(10, invokeStatic("rowSpacing"));
    assertEquals("Arial", invokeStatic("statusLabelFontFamily"));
    assertEquals(13, invokeStatic("statusLabelFontSize"));
    assertEquals("-fx-background-color: #243447;", invokeStatic("tabPaneBackgroundStyle"));
    assertEquals("-fx-background-color: #243447;", invokeStatic("tabContentBackgroundStyle"));
    assertEquals("-fx-control-inner-background: #1a2a3a; -fx-text-fill: white;",
        invokeStatic("listViewStyle"));
    assertEquals("-fx-control-inner-background: #0d1b2a; -fx-text-fill: #00ff88; "
        + "-fx-font-family: monospace; -fx-font-size: 11;",
        invokeStatic("consoleAreaStyle"));
    assertEquals(520, invokeStatic("sceneWidth"));
    assertEquals(640, invokeStatic("sceneHeight"));
    assertEquals("🖥  Héberger", invokeStatic("hostTabTitleText"));
    assertEquals("🔍 Rejoindre", invokeStatic("joinTabTitleText"));
    assertEquals("🎮 Salon", invokeStatic("lobbyTabTitleText"));
    assertEquals(
        "Démarrez un serveur. Les joueurs sur le réseau local vous verront automatiquement.",
        invokeStatic("hostDescriptionText"));
    assertEquals("Port TCP :", invokeStatic("hostPortLabelText"));
    assertEquals("▶  Démarrer le serveur", invokeStatic("startServerButtonText"));
    assertEquals("■  Arrêter le serveur", invokeStatic("stopServerButtonText"));
    assertEquals("🎮 Lancer la partie", invokeStatic("startGameButtonText"));
    assertEquals("Joueurs connectés au salon :", invokeStatic("hostPlayersTitleText"));
    assertEquals("Héberger une partie", invokeStatic("hostSectionTitleText"));
    assertEquals("🎮 Rejoindre le serveur sélectionné", invokeStatic("joinSelectedButtonText"));
    assertEquals("IP :", invokeStatic("ipLabelText"));
    assertEquals("Port :", invokeStatic("portLabelText"));
    assertEquals("🔗 Se connecter", invokeStatic("connectButtonText"));
    assertEquals("✖  Se déconnecter", invokeStatic("disconnectButtonText"));
    assertEquals("Serveurs détectés automatiquement :", invokeStatic("autoServersTitleText"));
    assertEquals("Ou connexion manuelle :", invokeStatic("manualConnectionTitleText"));
    assertEquals("En attente que l'hôte lance la partie...", invokeStatic("lobbyWaitingText"));
    assertEquals("🏆 Voir le classement", invokeStatic("refreshScoreboardButtonText"));
    assertEquals("Joueurs connectés :", invokeStatic("lobbyPlayersTitleText"));
    assertEquals("Classement du serveur :", invokeStatic("scoreboardTitleText"));
    assertEquals("#4CAF50", invokeStatic("startServerButtonColor"));
    assertEquals("#F44336", invokeStatic("stopServerButtonColor"));
    assertEquals("#FF9800", invokeStatic("startGameButtonColor"));
    assertEquals("#4CAF50", invokeStatic("connectButtonColor"));
    assertEquals("#F44336", invokeStatic("disconnectButtonColor"));
    assertEquals("#9C27B0", invokeStatic("refreshScoreboardColor"));
    assertEquals(280, invokeStatic("buttonPrefWidth"));
    assertEquals(36, invokeStatic("buttonPrefHeight"));
    assertEquals("Arial", invokeStatic("buttonFontFamily"));
    assertEquals(12, invokeStatic("buttonFontSize"));
    assertEquals(0.8, invokeStatic("buttonHoverOpacity"));
    assertEquals(1.0, invokeStatic("buttonNormalOpacity"));
    assertEquals("-fx-background-color: #123456;"
        + "-fx-text-fill: white;"
        + "-fx-background-radius: 5;"
        + "-fx-cursor: hand;",
        invokeStatic("buttonStyle", String.class, "#123456"));
  }

  @Test
  void formattingHelpersShouldHandleMissingFieldsWithDefaults() {
    @SuppressWarnings("unchecked")
    List<String> players = (List<String>) invokeStatic("formatPlayers", List.class,
        List.of(Map.of()));
    @SuppressWarnings("unchecked")
    List<String> scoreboard = (List<String>) invokeStatic("formatScoreboard", List.class,
        List.of(Map.of()));

    assertEquals("#?    Inconnu          [?]", players.getFirst());
    assertEquals("1. ?               V:0  D:0  T:0", scoreboard.getFirst());
  }

  private Object invokeStatic(String methodName) {
    try {
      Method method = NetworkLobbyView.class.getDeclaredMethod(methodName);
      method.setAccessible(true);
      return method.invoke(null);
    } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
      throw new RuntimeException(e);
    }
  }

  private Object invokeStatic(String methodName, Class<?> argType, Object arg) {
    try {
      Method method = NetworkLobbyView.class.getDeclaredMethod(methodName, argType);
      method.setAccessible(true);
      return method.invoke(null, arg);
    } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
      throw new RuntimeException(e);
    }
  }

  private Object invokeStatic(String methodName, Class<?> argType1, Class<?> argType2, Object arg1,
      Object arg2) {
    try {
      Method method = NetworkLobbyView.class.getDeclaredMethod(methodName, argType1, argType2);
      method.setAccessible(true);
      return method.invoke(null, arg1, arg2);
    } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
      throw new RuntimeException(e);
    }
  }

  private Object invokeStatic(String methodName, Class<?> argType1, Class<?> argType2,
      Class<?> argType3, Object arg1, Object arg2, Object arg3) {
    try {
      Method method = NetworkLobbyView.class.getDeclaredMethod(methodName, argType1, argType2,
          argType3);
      method.setAccessible(true);
      return method.invoke(null, arg1, arg2, arg3);
    } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
      throw new RuntimeException(e);
    }
  }

  private Object invokeStatic(String methodName, Class<?> argType1, Class<?> argType2,
      Class<?> argType3, Class<?> argType4, Class<?> argType5, Object arg1, Object arg2,
      Object arg3, Object arg4, Object arg5) {
    try {
      Method method = NetworkLobbyView.class.getDeclaredMethod(methodName, argType1, argType2,
          argType3, argType4, argType5);
      method.setAccessible(true);
      return method.invoke(null, arg1, arg2, arg3, arg4, arg5);
    } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
      throw new RuntimeException(e);
    }
  }

}
