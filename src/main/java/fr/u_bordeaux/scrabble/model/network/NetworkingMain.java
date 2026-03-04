package fr.u_bordeaux.scrabble.model.network;

import java.util.Scanner;

/** The type Networking main. */
// Temporary main for testing networking implementation without GUI
public class NetworkingMain {
  /**
   * The entry point of application.
   *
   * @param args the input arguments
   * @throws InterruptedException the interrupted exception
   */
  public static void main(String[] args) throws InterruptedException {
    // testIndividualNetworkClass();
    // testDiscoveryService();
    // testNetworkManager();
    testManualMultiplayer(args);
  }

  // Test network class without using NetworkManager
  private static void testIndividualNetworkClass() throws InterruptedException {
    // Test of the implementation of GameServer, ClientHandler and GameClient
    System.out.println("\nTest GameServer, ClientHandler and GameClient");

    // We start the server in a Thread for not blocking this function with the while(true)
    GameServer gameServer = new GameServer();
    new Thread(gameServer::start).start();

    // We wait to be sur that the server has started
    Thread.sleep(50);

    // We create clients and connect them to the server
    GameClient client1 = new GameClient();
    client1.connect("localhost", 12345);
    Thread.sleep(50);
    GameClient client2 = new GameClient();
    client2.connect("localhost", 12345);
    Thread.sleep(50);

    // Testing sending a message
    client1.sendMessage("Hello from client");
    Thread.sleep(50);

    // Testing the command PING
    client2.sendPing();
    Thread.sleep(50);

    // Closing the connexion from client1 manually
    client1.quit();
    Thread.sleep(50);

    // Bug intended => sending a message after closing the connexion
    client1.sendMessage("This will bug");
    Thread.sleep(50);

    // Stop the server and disconnect the clients
    gameServer.stop();
    Thread.sleep(50);
  }

  // Test DiscoveryService without using NetworkManager
  private static void testDiscoveryService() throws InterruptedException {
    // Test of the DiscoveryService (Broadcast and listen of server)
    System.out.println("\nTest DiscoveryService");
    DiscoveryService discoveryService = new DiscoveryService();

    // The discoveryService start listening to other servers (when the local player start online
    // play)
    discoveryService.startListening();
    Thread.sleep(500);

    // The discoveryService start broadcasting to other online players (when the local player create
    // a server)
    discoveryService.startBroadcasting("TestServer");
    Thread.sleep(500);

    // The local player ask for current online server (only his own server will appear)
    System.out.println("Listening : Current servers are " + discoveryService.getActiveServer());
    Thread.sleep(50);

    // The discoveryService stop broadcasting his server (when the local player stop his server)
    discoveryService.stopBroadcasting();
    Thread.sleep(50);

    // The local player ask for current online server (his own server will still appear, since the
    // timeout is 15sec)
    System.out.println("Listening : Current servers are " + discoveryService.getActiveServer());
    Thread.sleep(16000);

    // The local player ask for current online server (his own server will no longer appear, since
    // the timeout is finished)
    System.out.println("Listening : Current servers are " + discoveryService.getActiveServer());
    Thread.sleep(50);

    // The discoveryService stop listening to other servers (when the local player stop online play)
    discoveryService.stopListening();
    Thread.sleep(50);
  }

  // Test the network package using NetworkManager
  private static void testNetworkManager() throws InterruptedException {
    NetworkManager networkManager = new NetworkManager();

    // User start online play
    networkManager.startOnlinePlay();
    Thread.sleep(100);

    // User look for server, but none will be found
    System.out.println(networkManager.serverList());
    Thread.sleep(100);

    // User create a server
    networkManager.serverStart();
    Thread.sleep(100);

    // User look for server, his own server will be found
    System.out.println(networkManager.serverList());
    Thread.sleep(100);

    // User join a server (his own)
    networkManager.join("localhost", 12345);
    Thread.sleep(100);

    // User send a ping to the server he is connected to (his own)
    networkManager.ping();
    Thread.sleep(100);

    // User ask for server information
    networkManager.serverStatus();
    Thread.sleep(100);

    // User ask for servers players
    networkManager.players();
    Thread.sleep(100);

    // User ask for scoreboard
    networkManager.scoreboard();
    Thread.sleep(100);

    // User quit the server he is connected to (his own)
    networkManager.quit();
    Thread.sleep(100);

    // User stop his server
    networkManager.serverStop();
    Thread.sleep(100);

    // User quit online play
    networkManager.stopOnlinePlay();
    Thread.sleep(100);
  }

  // Bad temp method fully generated by AI, will be removed
  // Just used to test online play without CLI/GUI
  // TODO: REMOVE IT FOR FINAL VERSION
  private static void testManualMultiplayer(String[] args) throws InterruptedException {
    NetworkManager nm = new NetworkManager();
    nm.startOnlinePlay();

    if (args.length > 0 && args[0].equalsIgnoreCase("server")) {
      System.out.println("[JOUEUR 1] Création du serveur...");
      nm.serverStart();
      Thread.sleep(500);
    } else {
      System.out.println("[JOUEUR 2] Connexion au serveur...");
      nm.join("localhost"); //

      Scanner scanner = new Scanner(System.in);
      // Message d'aide mis à jour avec les nouvelles commandes
      System.out.println("Prêt ! Commandes disponibles :");
      System.out.println(" - players | status | scoreboard | quit");
      System.out.println(" - new <ID>");
      System.out.println(" - play <x> <y> <H|V> <tiles>");
      System.out.println(" - exchange <tiles>");
      System.out.println(" - pass");

      while (true) {
        String input = scanner.nextLine();
        String[] parts = input.split(" ");
        String command = parts[0].toLowerCase();

        switch (command) {
          case "players" -> nm.players();
          case "status" -> nm.serverStatus();
          case "scoreboard" -> nm.scoreboard();
          case "new" -> {
            if (parts.length > 1) nm.newPlayerId(Integer.parseInt(parts[1]));
          }
          case "play" -> {
            if (parts.length == 5) {
              int x = Integer.parseInt(parts[1]);
              int y = Integer.parseInt(parts[2]);
              String dir = parts[3];
              String tiles = parts[4];
              nm.play(x, y, dir, tiles);
            } else {
              System.out.println("Usage: play <x> <y> <H|V> <tiles>");
            }
          }
          case "exchange" -> {
            if (parts.length > 1) nm.exchange(parts[1]);
          }
          case "pass" -> nm.pass();
          case "quit" -> {
            nm.stopOnlinePlay();
            System.exit(0);
          }
          default -> System.out.println("Commande inconnue");
        }
      }
    }
  }
}
