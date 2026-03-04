package fr.u_bordeaux.scrabble.model.network;

import fr.u_bordeaux.scrabble.model.core.*;
import fr.u_bordeaux.scrabble.model.enums.Direction;
import fr.u_bordeaux.scrabble.model.interfaces.Player;
import fr.u_bordeaux.scrabble.model.utils.Point;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
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
   * Create the GAME_START message with the data of all participating players, then broadcast it to
   * them.
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
   * Sends the current full rack to a specific player. This ensures the client is always
   * synchronized with the server's authoritative model.
   */
  public void sendRack(ClientHandler handler, Player player) {
    List<Tile> tiles = player.getRack().getTiles();

    // We create a simple message of characters: "A,B,C,Q,Z,Y,X"
    String tilesMessage =
        tiles.stream().map(t -> String.valueOf(t.getCharacter())).collect(Collectors.joining(","));

    handler.sendMessage("SET_RACK:TILES=" + tilesMessage);
  }

  /**
   * Processes a move command from a client and updates the game state.
   *
   * @param sender the client handler who sent the move
   * @param packet the parsed move packet
   */
  public void processMove(ClientHandler sender, Packet packet) {
    // Check if we have data
    if (packet.getEntries().isEmpty()) {
      return;
    }

    Map<String, String> data = packet.getEntries().getFirst();
    String type = data.get("TYPE");

    // We get the current player index
    Player currentPlayer = game.getPlayers().get(game.getCurrentPlayerIndex());

    // We can only play if it's our turn
    if (!sender.getClientInfo().getName().equals(currentPlayer.getName())) {
      sender.sendMessage("ERROR: It is not your turn!");
      return;
    }

    try {
      switch (type) {
        case "PLAY" -> handlePlay(sender, data);
        case "EXCHANGE" -> handleExchange(sender, data);
        case "PASS" -> handlePass(sender);
        default -> sender.sendMessage("ERROR: Unknown move type");
      }
    } catch (Exception e) {
      sender.sendMessage("ERROR: Invalid move - " + e.getMessage());
    }
  }

  /** Handles the logic for placing a word on the board. */
  private void handlePlay(ClientHandler sender, Map<String, String> data) {
    // We get the start position
    int x = Integer.parseInt(data.get("X"));
    int y = Integer.parseInt(data.get("Y"));
    Point startPosition = new Point(x, y);

    // We get the direction of the word
    String dirStr = data.get("DIR");
    Direction direction = dirStr.equalsIgnoreCase("H") ? Direction.HORIZONTAL : Direction.VERTICAL;

    // We get the Player from his name
    Player player = game.getPlayerFromName(sender.getClientInfo().getName());
    if (player == null) {
      sender.sendMessage("ERROR: Player not found, can't play");
      return;
    }

    // We extract the tiles to place
    String tilesStr = data.get("TILES");
    List<Tile> tilesToPlace = new ArrayList<>();
    for (char c : tilesStr.toCharArray()) {
      tilesToPlace.add(new Tile(c));
    }

    // We create and execute a move from this data
    Move playMove = Move.createPlay(player, tilesToPlace, startPosition, direction);
    game.executeMove(playMove);

    System.out.println(
        "Server : "
            + sender.getClientInfo().getName()
            + " plays "
            + tilesStr
            + " at "
            + x
            + ":"
            + y);

    // We notify and update players
    broadcast(String.format("OPPONENT_MOVE:PLAYER=%s;TYPE=PLAY;BOARD=%s",
            sender.getClientInfo().getName(), getSerializedBoard()));

    sendRack(sender, player);

    game.printDebugState(false);
  }

  /** Handles the exchange of tiles. */
  private void handleExchange(ClientHandler sender, Map<String, String> data) {
    // TODO:
  }

  /** Handles skipping a turn. */
  private void handlePass(ClientHandler sender) {
    // TODO:
  }

  /**
   * Serializes the entire board into a single string for synchronization.
   * Empty squares are represented by a dot (.).
   */
  private String getSerializedBoard() {
    StringBuilder sb = new StringBuilder();
    for (int y = 0; y < 15; y++) {
      for (int x = 0; x < 15; x++) {
        Square sq = game.getBoard().getSquare(new Point(x, y));
        sb.append(sq.isEmpty() ? "." : sq.getTile().getCharacter());
      }
    }
    return sb.toString();
  }
}
