package fr.u_bordeaux.scrabble.model.network.server;

import fr.u_bordeaux.scrabble.model.core.Game;
import fr.u_bordeaux.scrabble.model.core.Move;
import fr.u_bordeaux.scrabble.model.core.Square;
import fr.u_bordeaux.scrabble.model.core.Tile;
import fr.u_bordeaux.scrabble.model.enums.Direction;
import fr.u_bordeaux.scrabble.model.interfaces.Player;
import fr.u_bordeaux.scrabble.model.network.PacketParser;
import fr.u_bordeaux.scrabble.model.network.PlayerStatus;
import fr.u_bordeaux.scrabble.model.utils.Point;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/** This class represent an outgoing online game on the server. */
public class OnlineGame {

  // Game main model, in opposite of client local model
  private final Game game;
  // List of handler (client) connected to this game
  private final List<ClientHandler> handlers;

  /**
   * Instantiates a new online game on the server with given client.
   *
   * @param handlers the clients
   */
  public OnlineGame(List<ClientHandler> handlers) {
    this.game = new Game();
    this.handlers = handlers;

    // Loop for adding each handler to the game
    for (ClientHandler handler : handlers) {
      // We create a player for this client
      Player player = new Player(handler.getClientInfo().getName()) {};
      // We add it to the game
      this.game.addPlayer(player);

      // We set this game as the active game for this handler
      handler.setOnlineGame(this);
    }

    // Now that the game is set up, we start it
    this.game.startGame();

    // We announce the start of the game to all clients
    broadcastGameStart();

    // Synchronize each player's rack from the main model with their client
    for (int i = 0; i < handlers.size(); i++) {
      sendRack(handlers.get(i), game.getPlayers().get(i));
    }

    // Debug: print the board server side
    game.printDebugState(false, false);
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

    // Add the info of the bag size
    sb.append("BAG=").append(game.getBag().size()).append("|");

    // Add the info of all participating players (ID have is not use client side for now)
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
   *
   * @param handler the handler
   * @param player the player
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
   * @param packetParser the parsed move packetParser
   */
  public void processMove(ClientHandler sender, PacketParser packetParser) {
    // Check if we have data in the packetParser
    if (packetParser.getEntries().isEmpty()) {
      return;
    }

    // We get the current player index
    Player currentPlayer = game.getPlayers().get(game.getCurrentPlayerIndex());
    // The sender can only play if it's his turn
    if (!sender.getClientInfo().getName().equals(currentPlayer.getName())) {
      sender.sendMessage("ERROR: It is not your turn!");
      return;
    }

    Map<String, String> data = packetParser.getEntries().getFirst();
    String type = data.get("TYPE");

    try {
      switch (type) {
        case "PLAY" -> handlePlay(sender, data, currentPlayer);
        case "EXCHANGE" -> handleExchange(sender, data, currentPlayer);
        case "PASS" -> handlePass(sender, currentPlayer);
        default -> sender.sendMessage("ERROR: Unknown move type");
      }
    } catch (Exception e) {
      sender.sendMessage("ERROR: Invalid move type - " + e.getMessage());
    }
  }

  /** Handles the logic for placing a word on the board. */
  private void handlePlay(ClientHandler sender, Map<String, String> data, Player player) {
    // We get the start position of the word
    int x = Integer.parseInt(data.get("X"));
    int y = Integer.parseInt(data.get("Y"));
    Point startPosition = new Point(x, y);

    // We get the direction of the word
    String dirStr = data.get("DIR");
    Direction direction = dirStr.equalsIgnoreCase("H") ? Direction.HORIZONTAL : Direction.VERTICAL;

    // We extract and create a list of tiles to place
    String tilesStr = data.get("TILES");
    tilesStr = tilesStr.replaceAll("[\\[\\]\\s,]", "").toUpperCase();
    List<Tile> tilesToPlace = new ArrayList<>();
    for (char c : tilesStr.toCharArray()) {
      tilesToPlace.add(new Tile(c));
    }

    // We create and execute a move with these data
    Move playMove = Move.createPlay(player, tilesToPlace, startPosition, direction);
    game.executeMove(playMove);

    //    System.out.println(
    //        "Server : "
    //            + sender.getClientInfo().getName()
    //            + " plays "
    //            + tilesStr
    //            + " at "
    //            + x
    //            + ":"
    //            + y);

    // We notify and update players with new board/score/bag
    broadcast(
        String.format(
            "OPPONENT_MOVE:PLAYER=%s;TYPE=PLAY;BOARD=%s;SCORE=%d;BAG=%s",
            sender.getClientInfo().getName(),
            getSerializedBoard(),
            player.getScore(),
            game.getBag().size()));

    // We send the new rack to the player who played
    sendRack(sender, player);

    // Debug: print the board server side
    game.printDebugState(false, false);
  }

  /** Handles the exchange of tiles. */
  private void handleExchange(ClientHandler sender, Map<String, String> data, Player player) {
    // We extract, clean and create a list of tiles to exchange
    String tilesStr = data.get("TILES");
    tilesStr = tilesStr.replaceAll("[\\[\\]\\s,]", "").toUpperCase();
    List<Tile> tilesToExchange = new ArrayList<>();
    for (char c : tilesStr.toCharArray()) {
      tilesToExchange.add(new Tile(c));
    }

    // We create and execute a move with these data
    Move exchangeMove = Move.createExchange(player, tilesToExchange);
    game.executeMove(exchangeMove);

    // System.out.println("Server : " + sender.getClientInfo().getName() + " plays " + tilesStr);

    // We notify and update players with new board/score/bag
    // Maybe not useful in this case, but is needed for changing the turn of locals models
    broadcast(
        String.format("OPPONENT_MOVE:PLAYER=%s;TYPE=EXCHANGE", sender.getClientInfo().getName()));

    // We send the new rack to the player who played
    sendRack(sender, player);

    // Debug: print the board server side
    game.printDebugState(false, false);
  }

  /** Handles skipping a turn. */
  private void handlePass(ClientHandler sender, Player player) {
    // We create and execute a move with these data
    Move passMove = Move.createPass(player);
    game.executeMove(passMove);

    // We notify and update players with new board/score/bag
    // Maybe not useful in this case, but is needed for changing the turn of locals models
    broadcast(String.format("OPPONENT_MOVE:PLAYER=%s;TYPE=PASS", sender.getClientInfo().getName()));

    // Debug: print the board server side
    game.printDebugState(false, false);
  }

  /**
   * Serializes the entire board into a single string for synchronization. Empty squares are
   * represented by a dot (.).
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

  /**
   * Terminates the game, notifies remaining players and cleans up server references.
   *
   * @param reason The reason why the game is ending (e.g., "A player disconnected")
   */
  public void terminateGame(String reason) {
    // Notify all remaining connected players
    broadcast("ERROR: Game terminated - " + reason);

    // Reset player status so they can play again
    for (ClientHandler handler : handlers) {
      handler.getClientInfo().setStatus(PlayerStatus.IDLE);
      handler.setOnlineGame(null);
    }

    // Remove this game from the server's list
    handlers.getFirst().getServer().removeOnlineGame(this);
  }
}
