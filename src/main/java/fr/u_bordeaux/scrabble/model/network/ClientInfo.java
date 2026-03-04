package fr.u_bordeaux.scrabble.model.network;

import jdk.jshell.Snippet;

/** Store all information for a client (an online player). */
public class ClientInfo {
  private final int id; // Unique id for this player
  private final String name;

  private int wins;
  private int losses;
  private int gamesPlayed;

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
   * Gets player essential info. Use for the COMMAND players
   *
   * @return the player info
   */
  public String getPlayerInfo() {
    return String.format("ID=%d;NAME=%s;STATUS=%s", id, name, status);
  }

  /**
   * Gets player score info.
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
