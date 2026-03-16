package fr.ubordeaux.scrabble.model.network;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import fr.ubordeaux.scrabble.model.core.Game;
import fr.ubordeaux.scrabble.model.core.Tile;
import fr.ubordeaux.scrabble.model.interfaces.Player;
import fr.ubordeaux.scrabble.model.network.server.ClientHandler;
import fr.ubordeaux.scrabble.model.network.server.ClientInfo;
import fr.ubordeaux.scrabble.model.network.server.GameServer;
import fr.ubordeaux.scrabble.model.network.server.OnlineGame;
import fr.ubordeaux.scrabble.model.utils.Point;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for OnlineGame logic. Focuses on move processing, turn validation, and
 * synchronization.
 */
class OnlineGameTest {

  private OnlineGame onlineGame;
  private FakeClientHandler player1;
  private FakeClientHandler player2;

  @BeforeEach
  void setUp() {
    player1 = new FakeClientHandler("Player-1", 1);
    player2 = new FakeClientHandler("Player-2", 2);

    onlineGame = new OnlineGame(List.of(player1, player2));
  }

  @Test
  void testGameInitialization() {
    assertTrue(player1.lastMessage.contains("SET_RACK:"));
    assertTrue(player2.lastMessage.contains("SET_RACK:"));
    assertEquals("Player-1", onlineGame.getGame().getCurrentPlayer().getName());
  }

  @Test
  void testProcessMoveWrongTurn() {
    PacketParser passPacket = new PacketParser("MOVE:TYPE=PASS");
    // Player 2 tries to play while it is Player 1's turn
    onlineGame.processMove(player2, passPacket);

    assertTrue(player2.lastMessage.contains("ERROR: It is not your turn!"));
  }

  @Test
  void testProcessPassMove() {
    Game game = onlineGame.getGame();

    PacketParser passPacket = new PacketParser("MOVE:TYPE=PASS");
    onlineGame.processMove(player1, passPacket);

    // Network verification
    assertTrue(player2.lastMessage.contains("OPPONENT_MOVE:PLAYER=Player-1;TYPE=PASS"));

    // Model verification: the turn has changed
    assertEquals("Player-2", game.getCurrentPlayer().getName());
  }

  @Test
  void testProcessExchangeMove() {
    Game game = onlineGame.getGame();
    // 1. Force known letters to be able to exchange them
    game.forceTilesToPlayer("Player-1", List.of(new Tile('A'), new Tile('B'), new Tile('C'),
        new Tile('X'), new Tile('Y'), new Tile('Z'), new Tile('E')));

    // 2. The player requests to exchange A, B, and C
    PacketParser exchangePacket = new PacketParser("MOVE:TYPE=EXCHANGE;TILES=A,B,C");
    onlineGame.processMove(player1, exchangePacket);

    // 3. Verifications
    assertEquals("Player-2", game.getCurrentPlayer().getName(), "The turn should pass to Player 2");
    assertTrue(player2.lastMessage.contains("OPPONENT_MOVE:PLAYER=Player-1;TYPE=EXCHANGE"));
    assertTrue(player1.lastMessage.contains("SET_RACK:"), "Player 1 should receive their new rack");

    // Verify that the rack is full again (7 letters) after drawing
    Player p1 = game.getPlayerFromName("Player-1");
    assertEquals(7, p1.getRack().getTiles().size());
  }

  @Test
  void testProcessPlayMove() {
    Game game = onlineGame.getGame();

    // 1. Force letters to be able to play the word "BON"
    game.forceTilesToPlayer("Player-1", List.of(new Tile('B'), new Tile('O'), new Tile('N'),
        new Tile('X'), new Tile('Y'), new Tile('Z'), new Tile('E')));

    // 2. The player places "BON" horizontally at (7,7)
    PacketParser playPacket = new PacketParser("MOVE:TYPE=PLAY;X=7;Y=7;DIR=H;TILES=BON");
    onlineGame.processMove(player1, playPacket);

    // 3. Network verification
    assertTrue(player2.lastMessage.contains("OPPONENT_MOVE:PLAYER=Player-1;TYPE=PLAY;BOARD="));
    assertTrue(player1.lastMessage.contains("SET_RACK:"));

    // 4. Model verification (Board)
    assertEquals('B', game.getBoard().getSquare(new Point(7, 7)).getTile().getCharacter());
    assertEquals('O', game.getBoard().getSquare(new Point(8, 7)).getTile().getCharacter());
    assertEquals('N', game.getBoard().getSquare(new Point(9, 7)).getTile().getCharacter());

    // 5. Model verification (Turn)
    assertEquals("Player-2", game.getCurrentPlayer().getName(), "The turn should pass to Player 2");
  }

  @Test
  void testTerminateGame() {
    onlineGame.terminateGame("Test termination");

    assertTrue(player1.lastMessage.contains("ERROR: Game terminated"));
    assertNull(player1.getOnlineGame());
  }

  /**
   * Inner helper class to simulate a ClientHandler without network complexity.
   */
  private static class FakeClientHandler extends ClientHandler {
    String lastMessage = "";
    ClientInfo info;

    FakeClientHandler(String name, int id) {
      super(null, null, id);
      this.info = new ClientInfo(id);
      // Force the name to match the convention expected by the test and the Game
      // model
      this.info.setName(name);
    }

    @Override
    public void sendMessage(String msg) {
      this.lastMessage = msg;
    }

    @Override
    public ClientInfo getClientInfo() {
      return this.info;
    }

    @Override
    public GameServer getServer() {
      return new GameServer() {
        @Override
        public void removeOnlineGame(OnlineGame g) {}
      };
    }
  }
}
