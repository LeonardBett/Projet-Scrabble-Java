package fr.ubordeaux.scrabble.view;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

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
  void formatServerListShouldHandleMultipleServers() {
    @SuppressWarnings("unchecked")
    List<String> items = (List<String>) invokeStatic("formatServerList", List.class,
        List.of(
            new ServerInfo("192.168.1.1", 5555, "server1"),
            new ServerInfo("10.0.0.1", 6666, "server2")));
    assertEquals(2, items.size());
  }

  @Test
  void formatServerListShouldHandleEmptyList() {
    @SuppressWarnings("unchecked")
    List<String> items = (List<String>) invokeStatic("formatServerList", List.class,
        List.of());
    assertTrue(items.isEmpty());
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
  void formatPlayersShouldHandleMultiplePlayers() {
    @SuppressWarnings("unchecked")
    List<String> items = (List<String>) invokeStatic("formatPlayers", List.class,
        List.of(
            Map.of("ID", "1", "NAME", "Alice", "STATUS", "READY"),
            Map.of("ID", "2", "NAME", "Bob", "STATUS", "WAITING")));
    assertEquals(2, items.size());
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
  void formatScoreboardShouldHandleMultipleEntries() {
    @SuppressWarnings("unchecked")
    List<String> items = (List<String>) invokeStatic("formatScoreboard", List.class,
        List.of(
            Map.of("NAME", "Alice", "WINS", "5", "LOSSES", "2", "TOTAL", "7"),
            Map.of("NAME", "Bob", "WINS", "3", "LOSSES", "4", "TOTAL", "7")));
    assertEquals(2, items.size());
    assertTrue(items.get(0).startsWith("1."));
    assertTrue(items.get(1).startsWith("2."));
  }

  @Test
  void formatServerStatusShouldIncludeMetrics() {
    String status = (String) invokeStatic("formatServerStatus", Map.class,
        Map.of("PORT", "12345", "CLIENTS", "2", "GAMES", "1"));
    assertNotNull(status);
    assertTrue(status.contains("12345"));
    assertTrue(status.contains("2"));
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
    assertEquals(true, invokeStatic("isValidSelectedServerIndex",
        int.class, int.class, 0, 1));
    assertEquals(false, invokeStatic("isValidSelectedServerIndex",
        int.class, int.class, -1, 1));
    assertEquals(false, invokeStatic("isValidSelectedServerIndex",
        int.class, int.class, 2, 2));
    assertEquals(false, invokeStatic("isValidSelectedServerIndex",
        int.class, int.class, 0, 0));
  }

  @Test
  void extractedStateHelpersShouldReflectLobbyRules() {
    assertEquals("🎮 Lancement de la partie...",
        invokeStatic("startingGameLogMessage"));
    assertEquals("✖ Déconnecté du serveur.", invokeStatic("disconnectedMessage"));

    assertEquals(true, invokeStatic("canRefreshScoreboard", boolean.class, true));
    assertEquals(false, invokeStatic("canRefreshScoreboard", boolean.class, false));
    assertEquals(true, invokeStatic("canHostStartGame", int.class, 2));
    assertEquals(true, invokeStatic("canHostStartGame", int.class, 4));
    assertEquals(false, invokeStatic("canHostStartGame", int.class, 1));
    assertEquals(false, invokeStatic("canHostStartGame", int.class, 0));

    assertEquals(true, invokeStatic("shouldDisableStartServer", boolean.class, true));
    assertEquals(false, invokeStatic("shouldDisableStartServer", boolean.class, false));
    assertEquals(true, invokeStatic("shouldDisableStopServer", boolean.class, false));
    assertEquals(false, invokeStatic("shouldDisableStopServer", boolean.class, true));
    assertEquals(true, invokeStatic("shouldDisableConnect", boolean.class, true));
    assertEquals(false, invokeStatic("shouldDisableConnect", boolean.class, false));
    assertEquals(true, invokeStatic("shouldDisableDisconnect", boolean.class, false));
    assertEquals(false, invokeStatic("shouldDisableDisconnect", boolean.class, true));
    assertEquals(true,
        invokeStatic("shouldDisableRefreshScoreboard", boolean.class, false));
    assertEquals(false,
        invokeStatic("shouldDisableRefreshScoreboard", boolean.class, true));
    assertEquals(true, invokeStatic("shouldDisableStartGame",
        boolean.class, int.class, false, 2));
    assertEquals(true, invokeStatic("shouldDisableStartGame",
        boolean.class, int.class, true, 1));
    assertEquals(false, invokeStatic("shouldDisableStartGame",
        boolean.class, int.class, true, 2));
    assertEquals(false, invokeStatic("shouldDisableStartGame",
        boolean.class, int.class, true, 4));
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

  @Test
  void shouldLogPlayersReadyShouldReturnCorrectValues() {
    assertTrue((boolean) invokeStatic("shouldLogPlayersReady", int.class, 2));
    assertTrue((boolean) invokeStatic("shouldLogPlayersReady", int.class, 3));
    assertFalse((boolean) invokeStatic("shouldLogPlayersReady", int.class, 1));
    assertFalse((boolean) invokeStatic("shouldLogPlayersReady", int.class, 0));
  }

  // ===== All text/constant methods =====
  @Test
  void windowTitleTextIsNonEmpty() {
    String title = (String) invokeStatic("windowTitleText");
    assertFalse(title.isEmpty());
  }

  @Test
  void mainTitleTextIsNonEmpty() {
    String title = (String) invokeStatic("mainTitleText");
    assertFalse(title.isEmpty());
  }

  @Test
  void consoleLabelTextIsNonEmpty() {
    String text = (String) invokeStatic("consoleLabelText");
    assertFalse(text.isEmpty());
  }

  @Test
  void rootBackgroundStyleIsNonEmpty() {
    String style = (String) invokeStatic("rootBackgroundStyle");
    assertFalse(style.isEmpty());
  }

  @Test
  void numericConstantsArePositive() {
    assertTrue((int) invokeStatic("rootSpacing") > 0);
    assertTrue((int) invokeStatic("rootPadding") > 0);
    assertTrue((int) invokeStatic("titleFontSize") > 0);
    assertTrue((int) invokeStatic("consoleAreaPrefHeight") > 0);
    assertTrue((int) invokeStatic("consoleLabelFontSize") > 0);
    assertTrue((int) invokeStatic("tabContentSpacing") > 0);
    assertTrue((int) invokeStatic("tabContentPadding") > 0);
    assertTrue((int) invokeStatic("rowSpacing") > 0);
    assertTrue((int) invokeStatic("statusLabelFontSize") > 0);
    assertTrue((int) invokeStatic("sceneWidth") > 0);
    assertTrue((int) invokeStatic("sceneHeight") > 0);
  }

  @Test
  void fontFamiliesAreNonEmpty() {
    assertFalse(((String) invokeStatic("titleFontFamily")).isEmpty());
    assertFalse(((String) invokeStatic("consoleLabelFontFamily")).isEmpty());
    assertFalse(((String) invokeStatic("statusLabelFontFamily")).isEmpty());
  }

  @Test
  void styleStringsAreNonEmpty() {
    assertFalse(((String) invokeStatic("tabPaneBackgroundStyle")).isEmpty());
    assertFalse(((String) invokeStatic("tabContentBackgroundStyle")).isEmpty());
    assertFalse(((String) invokeStatic("listViewStyle")).isEmpty());
    assertFalse(((String) invokeStatic("consoleAreaStyle")).isEmpty());
  }

  @Test
  void tabTitleTextsAreNonEmpty() {
    assertFalse(((String) invokeStatic("hostTabTitleText")).isEmpty());
    assertFalse(((String) invokeStatic("joinTabTitleText")).isEmpty());
    assertFalse(((String) invokeStatic("lobbyTabTitleText")).isEmpty());
  }

  @Test
  void hostRelatedTextsAreNonEmpty() {
    assertFalse(((String) invokeStatic("hostDescriptionText")).isEmpty());
    assertFalse(((String) invokeStatic("hostPortLabelText")).isEmpty());
    assertFalse(((String) invokeStatic("startServerButtonText")).isEmpty());
    assertFalse(((String) invokeStatic("stopServerButtonText")).isEmpty());
    assertFalse(((String) invokeStatic("startGameButtonText")).isEmpty());
    assertFalse(((String) invokeStatic("hostPlayersTitleText")).isEmpty());
    assertFalse(((String) invokeStatic("hostSectionTitleText")).isEmpty());
  }

  @Test
  void joinRelatedTextsAreNonEmpty() {
    assertFalse(((String) invokeStatic("joinSelectedButtonText")).isEmpty());
    assertFalse(((String) invokeStatic("ipLabelText")).isEmpty());
    assertFalse(((String) invokeStatic("portLabelText")).isEmpty());
    assertFalse(((String) invokeStatic("connectButtonText")).isEmpty());
    assertFalse(((String) invokeStatic("disconnectButtonText")).isEmpty());
    assertFalse(((String) invokeStatic("autoServersTitleText")).isEmpty());
    assertFalse(((String) invokeStatic("manualConnectionTitleText")).isEmpty());
  }

  @Test
  void lobbyRelatedTextsAreNonEmpty() {
    assertFalse(((String) invokeStatic("lobbyWaitingText")).isEmpty());
    assertFalse(((String) invokeStatic("refreshScoreboardButtonText")).isEmpty());
    assertFalse(((String) invokeStatic("lobbyPlayersTitleText")).isEmpty());
    assertFalse(((String) invokeStatic("scoreboardTitleText")).isEmpty());
  }

  @Test
  void buttonColorStringsAreNonEmpty() {
    assertFalse(((String) invokeStatic("startServerButtonColor")).isEmpty());
    assertFalse(((String) invokeStatic("stopServerButtonColor")).isEmpty());
    assertFalse(((String) invokeStatic("startGameButtonColor")).isEmpty());
    assertFalse(((String) invokeStatic("connectButtonColor")).isEmpty());
    assertFalse(((String) invokeStatic("disconnectButtonColor")).isEmpty());
    assertFalse(((String) invokeStatic("refreshScoreboardColor")).isEmpty());
  }

  @Test
  void defaultPortTextsAreValid() {
    String hostPort = (String) invokeStatic("defaultHostPortText");
    String joinHost = (String) invokeStatic("defaultJoinHostText");
    String joinPort = (String) invokeStatic("defaultJoinPortText");
    assertFalse(hostPort.isEmpty());
    assertFalse(joinHost.isEmpty());
    assertFalse(joinPort.isEmpty());
    assertEquals("localhost", joinHost);
  }

  @Test
  void consolePromptTextIsNonEmpty() {
    String prompt = (String) invokeStatic("consolePromptText");
    assertFalse(prompt.isEmpty());
  }

  @Test
  void formatServerLineFormatsCorrectly() {
    String line = (String) invokeStaticVarargs("formatServerLine",
        new Class<?>[] { String.class, String.class, int.class },
        "MyServer", "192.168.1.1", 5555);
    assertNotNull(line);
    assertTrue(line.contains("MyServer"));
    assertTrue(line.contains("192.168.1.1"));
    assertTrue(line.contains("5555"));
  }

  @Test
  void formatPlayerLineFormatsCorrectly() {
    String line = (String) invokeStaticVarargs("formatPlayerLine",
        new Class<?>[] { String.class, String.class, String.class },
        "1", "Alice", "READY");
    assertNotNull(line);
    assertTrue(line.contains("1"));
    assertTrue(line.contains("Alice"));
    assertTrue(line.contains("READY"));
  }

  @Test
  void formatScoreboardLineFormatsCorrectly() {
    String line = (String) invokeStaticVarargs("formatScoreboardLine",
        new Class<?>[] { int.class, String.class, String.class, String.class, String.class },
        1, "Alice", "5", "2", "7");
    assertNotNull(line);
    assertTrue(line.contains("Alice"));
    assertTrue(line.contains("5"));
  }

  // ===== Reflection helpers =====
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

  private Object invokeStatic(String methodName, Class<?> argType1, Class<?> argType2,
      Object arg1, Object arg2) {
    try {
      Method method = NetworkLobbyView.class.getDeclaredMethod(methodName, argType1, argType2);
      method.setAccessible(true);
      return method.invoke(null, arg1, arg2);
    } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
      throw new RuntimeException(e);
    }
  }

  private Object invokeStaticVarargs(String methodName, Class<?>[] argTypes,
      Object... args) {
    try {
      Method method = NetworkLobbyView.class.getDeclaredMethod(methodName, argTypes);
      method.setAccessible(true);
      return method.invoke(null, args);
    } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
      throw new RuntimeException(e);
    }
  }
}
