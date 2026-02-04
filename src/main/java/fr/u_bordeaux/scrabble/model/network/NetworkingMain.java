package fr.u_bordeaux.scrabble.model.network;

//temporary main for testing networking without GUI
public class NetworkingMain {
    public static void main(String[] args) throws InterruptedException {

        //We start the server in a Thread for not blocking this function with the while(true)
        GameServer gameServer = new GameServer();
        new Thread(gameServer::start).start();

        //We wait to be sur that the server has started
        Thread.sleep(50);

        //We create two clients and connect them to the server
        GameClient client1 = new GameClient();
        client1.connect("localhost", 12345);
        GameClient client2 = new GameClient();
        client2.connect("localhost", 12345);

        Thread.sleep(50);

        //Testing sending a message
        client1.sendMessage("Hello from client");

        Thread.sleep(50);

        //Testing the command PING
        client2.sendPing();

        Thread.sleep(50);

        //Closing the connexion from client1
        client1.quit();

        Thread.sleep(50);

        //Bug intended => sending a message after closing the connexion
        client1.sendMessage("This will bug");

        Thread.sleep(50);

        //Closing the connexion from client2
        client2.quit();

        Thread.sleep(50);

        //Stop the server
        gameServer.stop();
    }
}
