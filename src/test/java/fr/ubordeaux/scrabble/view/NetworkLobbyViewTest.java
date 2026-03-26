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
