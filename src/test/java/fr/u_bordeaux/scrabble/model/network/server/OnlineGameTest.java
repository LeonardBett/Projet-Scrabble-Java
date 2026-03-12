package fr.u_bordeaux.scrabble.model.network.server;

import static org.junit.jupiter.api.Assertions.*;

import fr.u_bordeaux.scrabble.model.network.PacketParser;
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
    // We create a mock-like environment without real sockets
    player1 = new FakeClientHandler("Alice", 1);
    player2 = new FakeClientHandler("Bob", 2);

    // Starting a game with 2 players automatically triggers broadcastGameStart
    onlineGame = new OnlineGame(List.of(player1, player2));
  }

  @Test
  void testGameInitialization() {
    // Verify that GAME_START was broadcasted
    assertTrue(player1.lastMessage.contains("SET_RACK:"));
    assertTrue(player2.lastMessage.contains("SET_RACK:"));
  }

  @Test
  void testProcessMoveWrongTurn() {
    // It's Alice's turn (player 1). If Bob (player 2) tries to play, it should fail.
    PacketParser passPacket = new PacketParser("MOVE:TYPE=PASS");
    onlineGame.processMove(player2, passPacket);

    assertTrue(player2.lastMessage.contains("ERROR: It is not your turn!"));
  }

  @Test
  void testProcessPassMove() {
    PacketParser passPacket = new PacketParser("MOVE:TYPE=PASS");
    onlineGame.processMove(player1, passPacket);

    // Verify broadcast of the move to all players
    assertTrue(player2.lastMessage.contains("OPPONENT_MOVE:PLAYER=Player-1;TYPE=PASS"));
  }

  @Test
  void testTerminateGame() {
    onlineGame.terminateGame("Test termination");

    // Verify notification and cleanup
    assertTrue(player1.lastMessage.contains("ERROR: Game terminated"));
    assertNull(player1.getOnlineGame());
  }

  /** Inner helper class to simulate a ClientHandler without network complexity. */
  private static class FakeClientHandler extends ClientHandler {
    String lastMessage = "";
    ClientInfo info;

    FakeClientHandler(String name, int id) {
      super(null, null, id); // We don't need real socket/server for these tests
      this.info = new ClientInfo(id);
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
        public void removeOnlineGame(OnlineGame g) {} // Do nothing
      };
    }
  }
}
