package fr.u_bordeaux.scrabble.model.network;

import fr.u_bordeaux.scrabble.model.core.Game;
import fr.u_bordeaux.scrabble.model.core.HumanPlayer;
import fr.u_bordeaux.scrabble.model.core.Tile;
import fr.u_bordeaux.scrabble.model.interfaces.Player;
import java.util.List;
import java.util.stream.Collectors;

/** This class represent an outgoing online game on the server. */
public class ActiveGame {
  private final Game game;
  private final List<ClientHandler> handlers;

  /**
   * Instantiates a new online game on the server.
   *
   * @param handlers the players
   */
  public ActiveGame(List<ClientHandler> handlers) {
    this.game = new Game();
    this.handlers = handlers;

    // Loop for adding each handler to the game
    for (ClientHandler handler : handlers) {
      // We create a player for this client
      Player player = new HumanPlayer(handler.getClientInfo().getName());
      // We add it to the game
      this.game.addPlayer(player);

      handler.setActiveGame(this);
    }

    this.game.startGame();

    broadcastGameStart();

    // Synchronize each player's rack with their client
    for (int i = 0; i < handlers.size(); i++) {
      sendRack(handlers.get(i), game.getPlayers().get(i));
    }
  }

  /**
   * Broadcast a message to all player from this active game.
   *
   * @param message the message to send
   */
  public void broadcast(String message) {
    for (ClientHandler p : handlers) {
      p.sendMessage(message);
    }
  }

  /**
   * Create the GAME_START message with the data of all participating players,
   * then broadcast it to them.
   */
  private void broadcastGameStart() {
    StringBuilder sb = new StringBuilder("GAME_START:");
    for (int i = 0; i < handlers.size(); i++) {
      ClientInfo info = handlers.get(i).getClientInfo();
      sb.append(String.format("ID=%d;NAME=%s", info.getId(), info.getName()));
      if (i < handlers.size() - 1) {
        sb.append("|");
      }
    }
    broadcast(sb.toString());
  }

  /**
   * Sends the current full rack to a specific player.
   * This ensures the client is always synchronized with the server's authoritative model.
   */
  public void sendRack(ClientHandler handler, Player player) {
    List<Tile> tiles = player.getRack().getTiles();

    // We create a simple message of characters: "A,B,C,Q,Z,Y,X"
    String tilesMessage = tiles.stream()
            .map(t -> String.valueOf(t.getCharacter()))
            .collect(Collectors.joining(","));

    handler.sendMessage("SET_RACK:TILES=" + tilesMessage);
  }
}
