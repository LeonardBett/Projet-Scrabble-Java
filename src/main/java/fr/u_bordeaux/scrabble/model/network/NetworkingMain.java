package fr.u_bordeaux.scrabble.model.network;

// Temporary main for testing networking implementation without GUI
public class NetworkingMain {
    public static void main(String[] args) throws InterruptedException {

        // Test of the implementation of GameServer, ClientHandler and GameClient
        System.out.println("\nTest GameServer, ClientHandler and GameClient");

        //We start the server in a Thread for not blocking this function with the while(true)
        GameServer gameServer = new GameServer();
        new Thread(gameServer::start).start();

        //We wait to be sur that the server has started
        Thread.sleep(50);

        //We create clients and connect them to the server
        GameClient client1 = new GameClient();
        client1.connect("localhost", 12345);
        Thread.sleep(50);
        GameClient client2 = new GameClient();
        client2.connect("localhost", 12345);
        Thread.sleep(50);

        //Testing sending a message
        client1.sendMessage("Hello from client");
        Thread.sleep(50);

        //Testing the command PING
        client2.sendPing();
        Thread.sleep(50);

        //Closing the connexion from client1 manually
        client1.quit();
        Thread.sleep(50);

        //Bug intended => sending a message after closing the connexion
        client1.sendMessage("This will bug");
        Thread.sleep(50);

        //Stop the server and disconnect the clients
        gameServer.stop();
        Thread.sleep(50);

        //Test of the DiscoveryService (Broadcast and listen of server)
        System.out.println("\nTest DiscoveryService");
        DiscoveryService discoveryService = new DiscoveryService();

        // The discoveryService start listening to other servers (when the local player start online play)
        discoveryService.startListening();
        Thread.sleep(500);

        // The discoveryService start broadcasting to other online players (when the local player create a server)
        discoveryService.startBroadcasting("TestServer");
        Thread.sleep(500);

        // The local player ask for current online server (only his own server will appear)
        System.out.println("Listening : Current servers are " + discoveryService.getActiveServer());
        Thread.sleep(50);

        // The discoveryService stop broadcasting his server (when the local player stop his server)
        discoveryService.stopBroadcasting();
        Thread.sleep(50);

        // The local player ask for current online server (his own server will still appear, since the timeout is 15sec)
        System.out.println("Listening : Current servers are " + discoveryService.getActiveServer());
        Thread.sleep(16000);

        // The local player ask for current online server (his own server will no longer appear, since the timeout is finished)
        System.out.println("Listening : Current servers are " + discoveryService.getActiveServer());
        Thread.sleep(50);

        // The discoveryService stop listening to other servers (when the local player stop online play)
        discoveryService.stopListening();
        Thread.sleep(50);
    }
}
