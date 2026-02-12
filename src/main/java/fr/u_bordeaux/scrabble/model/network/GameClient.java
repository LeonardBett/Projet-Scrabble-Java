package fr.u_bordeaux.scrabble.model.network;

import static fr.u_bordeaux.scrabble.model.network.NetworkManager.DEFAULT_ADDRESS;
import static fr.u_bordeaux.scrabble.model.network.NetworkManager.DEFAULT_TCP_PORT;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.SocketException;

/** Network client to connect to a game server. */
public class GameClient {

  // Volatile because it can be read/write in the same time by two thread (main and
  // listenServerLoop)
  // Volatile flag used to maintain the loop active and allow a graceful shutdown of the thread.
  private volatile boolean isRunning = false;

  // Socket use to talk with the server
  private Socket socket;

  // Simplify receiving data from the server (get a string easily)
  private BufferedReader in;
  // Simplify sending data to the server (no need to use an array of bit)
  private PrintWriter out;

  // Variable needed for the PING command
  private long pingStartTime;

  // Need to keep the reference of the Thread running startHeartbeat,
  // because we have to stop it manually when quit() is called to avoid blocking for 30sec
  private Thread heartbeatThread;

  /**
   * Connect to a server on a specific address and port.
   *
   * @param address the address
   * @param port the port
   */
  public void connect(String address, int port) {
    try {
      // Try to connect to a server
      socket = new Socket(address, port);

      System.out.println("Client : connected to " + socket.getInetAddress().getHostName());

      // Set IO for ASCII communication
      in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
      out = new PrintWriter(socket.getOutputStream(), true);

      isRunning = true;

      // We use a Thread for listening to the server
      new Thread(this::listenServerLoop).start();

      // We use a Thread for sending a regular PING command to the server (to avoid disconnecting
      // with 60sec timeout)
      heartbeatThread = new Thread(this::startHeartbeat);
      heartbeatThread.start();

    } catch (IOException e) {
      e.printStackTrace();
      System.out.println("Client : Cant connect to the server");
    }
  }

  /**
   * Connect to a server on the default address and port.
   */
  public void connect() {
    connect(DEFAULT_ADDRESS, DEFAULT_TCP_PORT);
  }

  // Infinite loop for listening to the server incoming messages
  // This method will only be call in a Thread
  private void listenServerLoop() {
    try {
      // Infinite loop for listening to the server
      String serverMessage;
      while (isRunning && (serverMessage = in.readLine()) != null) {
        if ("PONG".equals(serverMessage)) {
          long pingEndTime = System.currentTimeMillis();
          System.out.println("Client : PONG TIME=" + (pingEndTime - pingStartTime) + "ms");
        } else {
          System.out.println("Client : Received: " + serverMessage);
        }
      }
    } catch (SocketException e) {
      // Socket closed, normal behavior if we called close()
      if (isRunning) {
        e.printStackTrace();
      }
    } catch (IOException e) {
      e.printStackTrace();
    } finally {
      if (isRunning) {
        quit();
      }
    }
  }

  /**
   * // Close the connexion with the server.
   */
  public void quit() {
    if (!isRunning) {
      System.out.println("Client : This client is already disconnected");
      return;
    }
    isRunning = false;

    System.out.println("Client : connection closed");
    // Try closing the socket, if success will stop listenServerLoop() Thread in consequence
    try {
      if (socket != null && !socket.isClosed()) {
        socket.close();
      }
    } catch (IOException e) {
      e.printStackTrace();
    }

    // Stop the heartbeat Thread
    if (heartbeatThread != null) {
      heartbeatThread.interrupt();
    }
  }

  /**
   * Send a message to the server.
   *
   * @param message the message
   */
  public void sendMessage(String message) {
    if (isRunning && out != null) {
      out.println(message);
    } else {
      System.out.println("Client : Client is not running/connected");
    }
  }

  /** Send ping to the server. */
  public void sendPing() {
    if (isRunning && out != null) {
      pingStartTime = System.currentTimeMillis();
      out.println("PING");
    } else {
      System.out.println("Client : Client is not running/connected");
    }
  }

  // Method use in a Thread
  // Needed since the server timeout is 60sec, we ping it every 30sec to avoid disconnecting
  private void startHeartbeat() {
    try {
      while (isRunning) {
        Thread.sleep(30000);
        if (isRunning && !socket.isClosed()) {
          this.sendPing();
        }
      }
    } catch (InterruptedException e) {
      // Normal stop for this thread, call by stop()
      Thread.currentThread().interrupt();
    }
  }
}
