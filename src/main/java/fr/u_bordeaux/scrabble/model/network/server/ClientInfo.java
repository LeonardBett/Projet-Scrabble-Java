package fr.u_bordeaux.scrabble.model.network.server;

import fr.u_bordeaux.scrabble.model.network.PlayerStatus;

/** Store all information for a client (an online player). Use in ClientHandler */
public class ClientInfo {
  private final int id; // Unique id for this player
  private final String name;

  // Stats needed for scoreboard
  private int wins;
  private int losses;
  private int gamesPlayed;

  // Status of this player
  private PlayerStatus status;

  /**
   * Instantiates a new Client info.
   *
   * @param id the online player unique id
   */
  public ClientInfo(int id) {
    this.id = id;
    this.name = "Player-" + id;
    this.wins = 0;
    this.losses = 0;
    this.gamesPlayed = 0;
    this.status = PlayerStatus.IDLE;
  }

  /**
   * Gets player essential info in a string. Use for the command PLAYERS
   *
   * @return the player info
   */
  public String getPlayerInfo() {
    return String.format("ID=%d;NAME=%s;STATUS=%s", id, name, status);
  }

  /**
   * Gets player score info in a String. Use for the command SCOREBOARD
   *
   * @return the player score info
   */
  public String getScoreboardLine() {
    return String.format(
        "ID=%d;NAME=%s;WINS=%d;LOSSES=%d;TOTAL=%d", id, name, wins, losses, gamesPlayed);
  }

  public int getId() {
    return id;
  }

  public String getName() {
    return name;
  }

  public PlayerStatus getStatus() {
    return status;
  }

  public void setStatus(PlayerStatus playerStatus) {
    this.status = playerStatus;
  }
}
