package fr.ubordeaux.scrabble.view.gui;

import fr.ubordeaux.scrabble.model.core.Game;
import fr.ubordeaux.scrabble.model.network.NetworkManager;
import fr.ubordeaux.scrabble.model.network.NetworkObserver;
import fr.ubordeaux.scrabble.model.network.server.ServerInfo;
import java.util.List;
import java.util.Map;
import javafx.application.Platform;

/**
 * Bridges network observer callbacks to the JavaFX GUI and lobby views. It listens to network
 * events (via NetworkObserver) and updates the UI accordingly by forwarding the data to the
 * appropriate view (ScrabbleGui or NetworkLobbyView). All UI updates are wrapped in
 * Platform.runLater() to ensure thread safety with JavaFX.
 */
public class NetworkGameBridge implements NetworkObserver {

  private final NetworkManager networkManager;
  private ScrabbleGui gui;
  private NetworkLobbyView lobbyView;

  /**
   * Constructs the bridge and registers it as an observer to the NetworkManager.
   *
   * @param networkManager the network manager instance handling the client/server logic.
   */
  public NetworkGameBridge(NetworkManager networkManager) {
    this.networkManager = networkManager;
    this.networkManager.addObserver(this);
  }

  /**
   * Links the main game GUI to this bridge.
   *
   * @param gui the ScrabbleGui instance.
   */
  public void setGui(ScrabbleGui gui) {
    this.gui = gui;
  }

  /**
   * Links the lobby view to this bridge.
   *
   * @param lobbyView the NetworkLobbyView instance.
   */
  public void setLobbyView(NetworkLobbyView lobbyView) {
    this.lobbyView = lobbyView;
  }

  /**
   * Gets the associated network manager.
   *
   * @return the network manager instance.
   */
  public NetworkManager getNetworkManager() {
    return networkManager;
  }

  // ─── Core Game Updates ────────────────────────────────────────────────────

  /**
   * Triggered when the local game model is updated by the server. If the GUI is not yet in online
   * mode (first update), it switches to the online view. Otherwise, it just refreshes the board,
   * rack, and scores.
   */
  @Override
  public void localModelUpdate() {
    Platform.runLater(
        () -> {
          if (gui == null) {
            return;
          }

          Game onlineGame = networkManager.getLocalGame();
          if (onlineGame == null) {
            return;
          }

          if (!gui.isOnlineMode()) {
            gui.switchToOnlineGame(onlineGame);
          } else {
            gui.refreshAll();
          }
        });
  }

  /**
   * Triggered when the game ends (win, draw, or disconnection).
   *
   * @param finalScore the array of maps containing the result.
   */
  @Override
  public void gameEndedUpdate(List<Map<String, String>> finalScore) {
    Platform.runLater(
        () -> {
          if (gui != null) {
            gui.exitOnlineMode();
          }
          if (lobbyView != null) {
            lobbyView.onGameEnded(finalScore);
          }
        });
  }

  @Override
  public void serverWelcomeUpdate(int myId) {
    Platform.runLater(
        () -> {
          if (lobbyView != null) {
            lobbyView.onWelcomeReceived(myId);
          }
        });
  }

  // ─── Lobby & Server Info Updates ──────────────────────────────────────────

  /**
   * Triggered when the server sends its global status.
   *
   * @param info a map containing server stats (PORT, CLIENTS, GAMES).
   */
  @Override
  public void serverStatusUpdate(Map<String, String> info) {
    Platform.runLater(
        () -> {
          if (lobbyView != null) {
            lobbyView.onServerStatusReceived(info);
          }
        });
  }

  /**
   * Triggered when the server sends the updated list of connected players.
   *
   * @param players a list of maps containing player data (ID, NAME, STATUS).
   */
  @Override
  public void playersUpdate(List<Map<String, String>> players) {
    Platform.runLater(
        () -> {
          if (lobbyView != null) {
            lobbyView.onPlayersReceived(players);
          }
        });
  }

  /**
   * Triggered when the server sends the scoreboard data.
   *
   * @param scoreboard a list of maps containing player statistics.
   */
  @Override
  public void scoreboardUpdate(List<Map<String, String>> scoreboard) {
    Platform.runLater(
        () -> {
          if (lobbyView != null) {
            lobbyView.onScoreboardReceived(scoreboard);
          }
        });
  }

  /**
   * Triggered by the UDP discovery service when local servers are found.
   *
   * @param activeServers a list of discovered server information objects.
   */
  @Override
  public void serverListUpdate(List<ServerInfo> activeServers) {
    Platform.runLater(
        () -> {
          if (lobbyView != null) {
            lobbyView.onServerListUpdated(activeServers);
          }
        });
  }

  /**
   * Triggered when a generic text message is received from the server.
   *
   * @param message the message string.
   */
  @Override
  public void messageUpdate(String message) {
    Platform.runLater(
        () -> {
          if (lobbyView != null) {
            lobbyView.onMessageReceived(message);
          }
        });
  }

  // ─── F40: Invitation System Updates ───────────────────────────────────────

  /**
   * Triggered when this client receives an invitation from another player.
   *
   * @param from the name of the player who sent the invitation.
   */
  @Override
  public void invitationReceivedUpdate(String from) {
    Platform.runLater(
        () -> {
          if (lobbyView != null) {
            lobbyView.onInvitationReceived(from);
          }
        });
  }

  /**
   * Triggered when an invited player accepts the invitation.
   *
   * @param playerAccepted the name of the player who accepted.
   */
  @Override
  public void invitationAcceptedUpdate(String playerAccepted) {
    Platform.runLater(
        () -> {
          if (lobbyView != null) {
            lobbyView.onMessageReceived(playerAccepted + " a accepte l'invitation.");
          }
        });
  }

  /**
   * Triggered when an invited player declines the invitation.
   *
   * @param playerDeclined the name of the player who declined.
   */
  @Override
  public void invitationDeclinedUpdate(String playerDeclined) {
    Platform.runLater(
        () -> {
          if (lobbyView != null) {
            lobbyView.onMessageReceived(playerDeclined + " a refuse l'invitation.");
          }
        });
  }

  /**
   * Triggered when a pending invitation is cancelled (by the host or by the server).
   *
   * @param reason the reason for the cancellation.
   */
  @Override
  public void invitationCancelledUpdate(String reason) {
    Platform.runLater(
        () -> {
          if (lobbyView != null) {
            lobbyView.onInvitationCancelled(reason);
          }
        });
  }

  /**
   * Triggered when detailed player information is received.
   *
   * @param playerInfo map containing player details.
   */
  @Override
  public void playersPlayerIdUpdate(Map<String, String> playerInfo) {
    Platform.runLater(
        () -> {
          if (lobbyView != null) {
            lobbyView.onPlayerDetailsReceived(playerInfo);
          }
        });
  }

  @Override
  public void playerStatusUpdate(String status) {
    Platform.runLater(
        () -> {
          if (lobbyView != null) {
            lobbyView.onPlayerStatusChanged(status);
          }
        });
  }

  @Override
  public void clientDisconnectedUpdate(String reason) {
    Platform.runLater(
        () -> {
          // We update networkManager for destroying GameServer and GameClient instance
          networkManager.quit();

          // If we are in gui, we exit online mode
          if (gui != null) {
            gui.exitOnlineMode();
            gui.showInfo("Disconnected", reason);
          }
          // We reset lobby
          if (lobbyView != null) {
            lobbyView.onClientDisconnected(reason);
          }
        });
  }

  @Override
  public void connectionFailedUpdate(String reason) {
    Platform.runLater(
        () -> {
          // Clean up the manager to allow future connection attempts
          networkManager.quit();

          if (lobbyView != null) {
            lobbyView.onConnectionFailed(reason);
          }
        });
  }

  @Override
  public void invitationFailedUpdate(String reason) {
    Platform.runLater(
        () -> {
          if (lobbyView != null) {
            lobbyView.onInvitationFailed(reason);
          }
        });
  }

  @Override
  public void gameInterruptedUpdate(String reason) {
    Platform.runLater(
        () -> {
          if (gui != null) {
            gui.exitOnlineMode();
            gui.showInfo("Partie interrompue", reason);
          }
          if (lobbyView != null) {
            lobbyView.onGameInterrupted(reason);
          }
        });
  }

  // ─── Cleanup ──────────────────────────────────────────────────────────────

  /**
   * Cleans up the bridge by removing it from the observers list and stopping network play. Should
   * be called when the application is closing.
   */
  public void dispose() {
    networkManager.removeObserver(this);
    networkManager.stopOnlinePlay();
  }
}
