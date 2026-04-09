package fr.ubordeaux.scrabble.view.cli.network;

import fr.ubordeaux.scrabble.i18n.I18n;
import fr.ubordeaux.scrabble.model.core.Game;
import fr.ubordeaux.scrabble.model.network.NetworkManager;
import fr.ubordeaux.scrabble.model.network.NetworkObserver;
import fr.ubordeaux.scrabble.model.network.server.ServerInfo;
import fr.ubordeaux.scrabble.view.cli.CliView;
import java.util.List;
import java.util.Map;

/** CLI observer for network events. */
public class CliNetworkBridge implements NetworkObserver {

  private final NetworkManager networkManager;
  private final CliView cliView;

  /**
   * Creates a new CLI network bridge.
   *
   * @param networkManager network manager used to access local online game
   * @param cliView CLI view used for displaying network events
   */
  public CliNetworkBridge(NetworkManager networkManager, CliView cliView) {
    this.networkManager = networkManager;
    this.cliView = cliView;
  }

  @Override
  public void localModelUpdate() {
    cliView.displayMessage(I18n.translate("cli.network.event.modelUpdated"));
    Game localGame = networkManager.getLocalGame();
    if (localGame != null) {
      new CliView(localGame).refresh();
    }
  }

  @Override
  public void gameEndedUpdate(List<Map<String, String>> finalScore) {
    cliView.displayMessage(I18n.translate("cli.network.event.gameEnded"));
    if (finalScore == null || finalScore.isEmpty()) {
      return;
    }
    for (Map<String, String> scoreLine : finalScore) {
      cliView.displayMessage(String.valueOf(scoreLine));
    }
  }

  @Override
  public void serverWelcomeUpdate(int myId) {
    cliView.displayMessage(I18n.translate("cli.network.event.welcome", myId));
  }

  @Override
  public void serverStatusUpdate(Map<String, String> info) {
    cliView.displayMessage(I18n.translate("cli.network.event.serverStatus"));
    cliView.displayMessage(String.valueOf(info));
  }

  @Override
  public void playersUpdate(List<Map<String, String>> players) {
    cliView.displayMessage(I18n.translate("cli.network.event.players"));
    if (players == null || players.isEmpty()) {
      cliView.displayMessage(I18n.translate("cli.network.event.playersEmpty"));
      return;
    }
    for (Map<String, String> player : players) {
      cliView.displayMessage(String.valueOf(player));
    }
  }

  @Override
  public void scoreboardUpdate(List<Map<String, String>> scoreboard) {
    cliView.displayMessage(I18n.translate("cli.network.event.scoreboard"));
    if (scoreboard == null || scoreboard.isEmpty()) {
      cliView.displayMessage(I18n.translate("cli.network.event.scoreboardEmpty"));
      return;
    }
    for (Map<String, String> row : scoreboard) {
      cliView.displayMessage(String.valueOf(row));
    }
  }

  @Override
  public void pongUpdate(long latencyMs) {
    cliView.displayMessage(I18n.translate("cli.network.event.pong", latencyMs));
  }

  @Override
  public void serverListUpdate(List<ServerInfo> activeServers) {
    // Explicitly requested by lobby commands; no automatic rendering needed here.
  }

  @Override
  public void messageUpdate(String message) {
    cliView.displayMessage(
        I18n.translate("cli.network.event.serverMessage", I18n.translate(message)));
  }

  @Override
  public void invitationReceivedUpdate(String from) {
    cliView.displayMessage(I18n.translate("cli.network.event.invitationReceived", from));
  }

  @Override
  public void invitationAcceptedUpdate(String playerAccepted) {
    cliView.displayMessage(I18n.translate("cli.network.event.invitationAccepted", playerAccepted));
  }

  @Override
  public void invitationDeclinedUpdate(String playerDeclined) {
    cliView.displayMessage(I18n.translate("cli.network.event.invitationDeclined", playerDeclined));
  }

  @Override
  public void invitationCancelledUpdate(String reason) {
    cliView.displayMessage(
        I18n.translate("cli.network.event.invitationCancelled", I18n.translate(reason)));
  }

  @Override
  public void playersPlayerIdUpdate(Map<String, String> playerInfo) {
    cliView.displayMessage(I18n.translate("cli.network.event.playerInfo"));
    cliView.displayMessage(String.valueOf(playerInfo));
  }

  @Override
  public void playerStatusUpdate(String status) {
    cliView.displayMessage(I18n.translate("cli.network.event.statusUpdated", status));
  }

  @Override
  public void clientDisconnectedUpdate(String reason) {
    networkManager.quit();
    cliView.displayError(
        I18n.translate("cli.network.event.clientDisconnected", I18n.translate(reason)));
  }

  @Override
  public void gameInterruptedUpdate(String reason) {
    cliView.displayError(
        I18n.translate("cli.network.event.gameInterrupted", I18n.translate(reason)));
  }

  @Override
  public void connectionFailedUpdate(String reason) {
    cliView.displayError(
        I18n.translate("cli.network.event.connectionFailed", I18n.translate(reason)));
  }

  @Override
  public void invitationFailedUpdate(String reason) {
    cliView.displayError(
        I18n.translate("cli.network.event.invitationFailed", I18n.translate(reason)));
  }

  @Override
  public void moveRefusedUpdate(String reason) {
    cliView.displayError(I18n.translate("cli.network.event.moveRefused", I18n.translate(reason)));
  }
}
