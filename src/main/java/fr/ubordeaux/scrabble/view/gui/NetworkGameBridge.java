package fr.ubordeaux.scrabble.view.gui;

import fr.ubordeaux.scrabble.i18n.I18n;
import fr.ubordeaux.scrabble.model.core.Game;
import fr.ubordeaux.scrabble.model.network.NetworkManager;
import fr.ubordeaux.scrabble.model.network.NetworkObserver;
import fr.ubordeaux.scrabble.model.network.server.ServerInfo;
import java.util.List;
import java.util.Map;
import javafx.application.Platform;

/** Bridges network observer callbacks to the JavaFX GUI and lobby views. */
public class NetworkGameBridge implements NetworkObserver {

  private final NetworkManager networkManager;
  private ScrabbleGui gui;
  private NetworkLobbyView lobbyView;

  // Le modèle local synchronisé par le serveur
  private Game localGame;

  /**
   * Constructor for NetworkGameBridge.
   *
   * @param networkManager the network manager instance
   */
  public NetworkGameBridge(NetworkManager networkManager) {
    this.networkManager = networkManager;
    this.networkManager.addObserver(this);
  }

  /**
   * Sets the GUI reference.
   *
   * @param gui the GUI instance
   */
  public void setGui(ScrabbleGui gui) {
    this.gui = gui;
  }

  /**
   * Sets the lobby view reference.
   *
   * @param lobbyView the lobby view instance
   */
  public void setLobbyView(NetworkLobbyView lobbyView) {
    this.lobbyView = lobbyView;
  }

  /**
   * Returns the network manager.
   *
   * @return the network manager instance
   */
  public NetworkManager getNetworkManager() {
    return networkManager;
  }

  // ─── NetworkObserver ──────────────────────────────────────────────────────

  /**
   * Appelé quand le modèle local (board, rack, scores) est mis à jour par le serveur. Si c'est le
   * premier update (GAME_START), bascule la GUI sur le modèle réseau. Sinon, rafraîchit simplement
   * l'affichage.
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
            // Première fois : bascule la GUI en mode online
            gui.switchToOnlineGame(onlineGame);
          } else {
            // Déjà en mode online : rafraîchit juste l'affichage
            gui.refreshAll();
          }
        });
  }

  /** Appelé quand la partie se termine (victoire, déconnexion, abandon). */
  @Override
  public void gameEndedUpdate(String reason) {
<<<<<<< HEAD
    Platform.runLater(
        () -> {
          if (gui != null) {
            gui.exitOnlineMode();
            gui.showInfo("Partie terminée", reason);
          }
          if (lobbyView != null) {
            lobbyView.onGameEnded(reason);
          }
        });
=======
    Platform.runLater(() -> {
      if (gui != null) {
        gui.exitOnlineMode();
        gui.showInfo(I18n.tr("network.bridge.gameEndedTitle"), reason);
      }
      if (lobbyView != null) {
        lobbyView.onGameEnded(reason);
      }
    });
>>>>>>> 80eb4dd (Add internationalization support for GUI and CLI components)
  }

  /** Appelé en réponse à la commande SERVER_STATUS. */
  @Override
  public void serverStatusUpdate(Map<String, String> info) {
    Platform.runLater(
        () -> {
          if (lobbyView != null) {
            lobbyView.onServerStatusReceived(info);
          }
        });
  }

  // Flag activé uniquement quand l'hôte clique "Lancer la partie"
  private boolean pendingGameStart = false;

<<<<<<< HEAD
  // Flag activé uniquement quand l'hôte clique "Lancer la partie"
  private boolean pendingGameStart = false;

=======
>>>>>>> 80eb4dd275121a2ebeee7238584257f4cce5f53f
  /**
   * Appelé par le lobby quand l'hôte clique sur "Lancer la partie".
   * Déclenche la récupération de la liste des joueurs puis envoie NEW.
   */
  public void requestGameStart() {
    pendingGameStart = true;
    networkManager.players();
  }

  /**
   * Appelé en réponse à la commande PLAYERS. Met à jour le lobby et, si l'hôte
   * a explicitement demandé le lancement, envoie la commande NEW.
   */
  @Override
  public void playersUpdate(List<Map<String, String>> players) {
    Platform.runLater(() -> {
      if (lobbyView != null) {
        lobbyView.onPlayersReceived(players);
      }

      if (pendingGameStart && players.size() >= 2) {
        pendingGameStart = false;

        int[] ids = players.stream()
            .mapToInt(p -> {
              try {
                return Integer.parseInt(p.getOrDefault("ID", "0"));
              } catch (NumberFormatException ex) {
                return 0;
              }
            })
            .filter(id -> id > 0)
            .toArray();

        if (ids.length == 2) {
          networkManager.newPlayerId(ids[1]);
        } else if (ids.length == 3) {
          networkManager.newPlayerId(ids[1], ids[2]);
        } else if (ids.length >= 4) {
          networkManager.newPlayerId(ids[1], ids[2], ids[3]);
        }
      }
    });
  }

  /**
   * Appelé en réponse à la commande SCOREBOARD.
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

  /** Appelé quand la liste des serveurs disponibles sur le réseau change. */
  @Override
  public void serverListUpdate(List<ServerInfo> activeServers) {
    Platform.runLater(
        () -> {
          if (lobbyView != null) {
            lobbyView.onServerListUpdated(activeServers);
          }
        });
  }

  /** Appelé pour les messages génériques du serveur (ping, erreurs, etc.). */
  @Override
  public void messageUpdate(String message) {
    Platform.runLater(
        () -> {
          if (lobbyView != null) {
            lobbyView.onMessageReceived(message);
          }
        });
  }

  // ─── Nettoyage ────────────────────────────────────────────────────────────

  /** À appeler quand on ferme l'application pour se désenregistrer proprement. */
  public void dispose() {
    networkManager.removeObserver(this);
    networkManager.stopOnlinePlay();
  }
}