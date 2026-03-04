package fr.u_bordeaux.scrabble.model.network;

import fr.u_bordeaux.scrabble.model.core.Game;
import fr.u_bordeaux.scrabble.model.core.HumanPlayer;
import fr.u_bordeaux.scrabble.model.interfaces.Player;
import java.util.List;

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
}
