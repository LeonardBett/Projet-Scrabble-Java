package fr.ubordeaux.scrabble.model.network;

import fr.ubordeaux.scrabble.model.network.client.GameClient;
import fr.ubordeaux.scrabble.model.network.server.GameServer;
import fr.ubordeaux.scrabble.model.network.server.ServerInfo;
import java.util.Map;
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
    discoveryService.startBroadcasting("TestServer", 12345, "localhost");
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

  // Bad method generated by AI, will be removed
  // Just used to test online play before implementing CLI/GUI
  // Don't take it in consideration for understanding the network code
  // TODO: REMOVE IT FOR FINAL VERSION
  private static void testManualMultiplayer(String[] args) throws InterruptedException {
    NetworkManager nm = new NetworkManager();

    // --- VÉRIFICATION DE L'OBSERVER ---
    NetworkObserver consoleObserver =
        new NetworkObserver() {
          @Override
          public void localModelUpdate() {
            System.out.println("[OBSERVER] -> Le modèle local a été mis à jour.");
          }

          @Override
          public void gameEndedUpdate(String reason) {
            System.out.println("[OBSERVER] -> Fin de partie: " + reason);
          }

          @Override
          public void serverStatusUpdate(java.util.Map<String, String> info) {
            System.out.println("[OBSERVER] -> Statut du serveur reçu: " + info);
          }

          @Override
          public void playersUpdate(java.util.List<java.util.Map<String, String>> players) {
            System.out.println("[OBSERVER] -> Liste des joueurs reçue: " + players);
          }

          @Override
          public void scoreboardUpdate(java.util.List<java.util.Map<String, String>> scoreboard) {
            System.out.println("[OBSERVER] -> Tableau des scores reçu: " + scoreboard);
          }

          @Override
          public void serverListUpdate(java.util.List<ServerInfo> activeServers) {}

          @Override
          public void messageUpdate(String message) {
            System.out.println("[OBSERVER MSG] -> " + message);
          }

          @Override
          public void invitationReceivedUpdate(String from) {
            System.out.println(
                "\n[OBSERVER] -> INVITATION REÇUE de " + from + " ! (Tapez 'accept' ou 'decline')");
            System.out.print("> ");
          }

          @Override
          public void invitationAcceptedUpdate(String playerAccepted) {
            System.out.println(
                "\n[OBSERVER] -> Le joueur " + playerAccepted + " a ACCEPTÉ l'invitation.");
            System.out.print("> ");
          }

          @Override
          public void invitationDeclinedUpdate(String playerDeclined) {
            System.out.println(
                "\n[OBSERVER] -> Le joueur " + playerDeclined + " a REFUSÉ l'invitation.");
            System.out.print("> ");
          }

          @Override
          public void invitationCancelledUpdate(String reason) {
            System.out.println("\n[OBSERVER] -> Invitation annulé car : " + reason);
          }

          @Override
          public void playersPlayerIdUpdate(Map<String, String> playerInfo) {
            System.out.println("\n[OBSERVER] -> Détails du joueur : " + playerInfo);
          }

          @Override
          public void playerStatusUpdate(String status) {
            System.out.println("\n[OBSERVER] -> Nouveau statut confirmé : " + status);
          }
        };

    nm.addObserver(consoleObserver);
    // ----------------------------------

    nm.startOnlinePlay();

    if (args.length > 0 && args[0].equalsIgnoreCase("server")) {
      System.out.println("[SERVEUR] Démarrage automatique du serveur et du broadcast...");
      nm.serverStart();
      Thread.sleep(500);
    }

    System.out.println("\n--- Mode Réseau Actif ---");
    System.out.println("Commandes réseau : list | join <IP> | quit");
    // NOUVEAU : Mise à jour de l'aide pour afficher les nouvelles commandes
    System.out.println(
        "Partie : players [ID] | status | new <ID1> [ID2] [ID3]"
            + "| accept | decline | cancel | away | back");
    System.out.println("Jeu : play <x> <y> <H|V> <tiles> | exchange <tiles> | pass");

    Scanner scanner = new Scanner(System.in);
    while (true) {
      System.out.print("> ");
      String input = scanner.nextLine();
      String[] parts = input.split(" ");
      if (parts.length == 0) {
        continue;
      }

      String command = parts[0].toLowerCase();

      switch (command) {
        case "list" -> {
          System.out.println("Serveurs détectés sur le WiFi :");
          nm.serverList()
              .forEach(s -> System.out.println(" - " + s.getName() + " IP: " + s.getIp()));
        }
        case "join" -> {
          if (parts.length > 1) {
            System.out.println("Tentative de connexion à " + parts[1] + "...");
            nm.join(parts[1]);
          } else {
            System.out.println("Usage: join <IP>");
          }
        }
        // NOUVEAU : Gestion de l'argument optionnel pour players
        case "players" -> {
          if (parts.length > 1) {
            try {
              nm.playersPlayerId(Integer.parseInt(parts[1]));
            } catch (NumberFormatException e) {
              System.out.println("L'ID doit être un nombre entier.");
            }
          } else {
            nm.players();
          }
        }
        case "status" -> nm.serverStatus();
        case "scoreboard" -> nm.scoreboard();
        case "new" -> {
          if (parts.length == 2) {
            nm.newPlayerId(Integer.parseInt(parts[1]));
          } else if (parts.length == 3) {
            nm.newPlayerId(Integer.parseInt(parts[1]), Integer.parseInt(parts[2]));
          } else if (parts.length == 4) {
            nm.newPlayerId(
                Integer.parseInt(parts[1]), Integer.parseInt(parts[2]), Integer.parseInt(parts[3]));
          } else {
            System.out.println("Usage: new <ID1> [ID2] [ID3]");
          }
        }
        case "accept" -> nm.accept();
        case "decline" -> nm.decline();
        // NOUVEAU : Ajout des commandes away et back
        case "away" -> nm.away();
        case "back" -> nm.back();
        case "play" -> {
          if (parts.length == 5) {
            nm.play(Integer.parseInt(parts[1]), Integer.parseInt(parts[2]), parts[3], parts[4]);
          } else {
            System.out.println("Usage: play <x> <y> <H|V> <tiles>");
          }
        }
        case "exchange" -> {
          if (parts.length > 1) {
            nm.exchange(parts[1]);
          }
        }
        case "pass" -> nm.pass();
        case "quit" -> {
          nm.stopOnlinePlay();
          System.exit(0);
        }
        case "cancel" -> nm.cancel();
        default -> System.out.println("Commande inconnue.");
      }
    }
  }
}
