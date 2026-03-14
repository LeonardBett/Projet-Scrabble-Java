package fr.u_bordeaux.scrabble.view.gui;

import java.util.List;
import java.util.Map;

import fr.u_bordeaux.scrabble.model.core.Game;
import fr.u_bordeaux.scrabble.model.network.NetworkManager;
import fr.u_bordeaux.scrabble.model.network.NetworkObserver;
import fr.u_bordeaux.scrabble.model.network.server.ServerInfo;
import javafx.application.Platform;

/**
 * Bridges the network layer (NetworkObserver) to the JavaFX GUI.
 *
 * ✅ MVC:
 *  - Implements NetworkObserver (contrat avec le réseau)
 *  - Fait le lien entre les événements réseau et la GUI/Lobby
 *  - Ne contient aucune logique métier
 */
public class NetworkGameBridge implements NetworkObserver {

    private final NetworkManager networkManager;
    private ScrabbleGUI gui;
    private NetworkLobbyView lobbyView;

    // Le modèle local synchronisé par le serveur
    private Game localGame;

    public NetworkGameBridge(NetworkManager networkManager) {
        this.networkManager = networkManager;
        this.networkManager.addObserver(this);
    }

    public void setGui(ScrabbleGUI gui) {
        this.gui = gui;
    }

    public void setLobbyView(NetworkLobbyView lobbyView) {
        this.lobbyView = lobbyView;
    }

    public NetworkManager getNetworkManager() {
        return networkManager;
    }

    // ─── NetworkObserver ──────────────────────────────────────────────────────

    /**
     * Appelé quand le modèle local (board, rack, scores) est mis à jour par le serveur.
     * Si c'est le premier update (GAME_START), bascule la GUI sur le modèle réseau.
     * Sinon, rafraîchit simplement l'affichage.
     */
    @Override
    public void localModelUpdate() {
        Platform.runLater(() -> {
            if (gui == null) return;
            Game onlineGame = networkManager.getLocalGame();
            if (onlineGame == null) return;

            if (!gui.isOnlineMode()) {
                // Première fois : bascule la GUI en mode online
                gui.switchToOnlineGame(onlineGame);
            } else {
                // Déjà en mode online : rafraîchit juste l'affichage
                gui.refreshAll();
            }
        });
    }

    /**
     * Appelé quand la partie se termine (victoire, déconnexion, abandon).
     */
    @Override
    public void gameEndedUpdate(String reason) {
        Platform.runLater(() -> {
            if (gui != null) {
                gui.exitOnlineMode();
                gui.showInfo("Partie terminée", reason);
            }
            if (lobbyView != null) {
                lobbyView.onGameEnded(reason);
            }
        });
    }

    /**
     * Appelé en réponse à la commande SERVER_STATUS.
     */
    @Override
    public void serverStatusUpdate(Map<String, String> info) {
        Platform.runLater(() -> {
            if (lobbyView != null) {
                lobbyView.onServerStatusReceived(info);
            }
        });
    }

    /**
     * Appelé en réponse à la commande PLAYERS.
     */
    @Override
    public void playersUpdate(List<Map<String, String>> players) {
        Platform.runLater(() -> {
            if (lobbyView != null) {
                lobbyView.onPlayersReceived(players);
            }
        });
    }

    /**
     * Appelé en réponse à la commande SCOREBOARD.
     */
    @Override
    public void scoreboardUpdate(List<Map<String, String>> scoreboard) {
        Platform.runLater(() -> {
            if (lobbyView != null) {
                lobbyView.onScoreboardReceived(scoreboard);
            }
        });
    }

    /**
     * Appelé quand la liste des serveurs disponibles sur le réseau change.
     */
    @Override
    public void serverListUpdate(List<ServerInfo> activeServers) {
        Platform.runLater(() -> {
            if (lobbyView != null) {
                lobbyView.onServerListUpdated(activeServers);
            }
        });
    }

    /**
     * Appelé pour les messages génériques du serveur (ping, erreurs, etc.).
     */
    @Override
    public void messageUpdate(String message) {
        Platform.runLater(() -> {
            if (lobbyView != null) {
                lobbyView.onMessageReceived(message);
            }
        });
    }

    // ─── Nettoyage ────────────────────────────────────────────────────────────

    /**
     * À appeler quand on ferme l'application pour se désenregistrer proprement.
     */
    public void dispose() {
        networkManager.removeObserver(this);
        networkManager.stopOnlinePlay();
    }
}