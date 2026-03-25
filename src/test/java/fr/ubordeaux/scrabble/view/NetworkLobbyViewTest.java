package fr.ubordeaux.scrabble.view;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import fr.ubordeaux.scrabble.view.gui.NetworkLobbyView;
import java.lang.reflect.Method;
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
}
