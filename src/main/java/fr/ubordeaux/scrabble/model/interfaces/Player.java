package fr.ubordeaux.scrabble.model.interfaces;

import fr.ubordeaux.scrabble.model.core.Rack;
import fr.ubordeaux.scrabble.model.enums.PlayerColor;
import java.time.Duration;

/**
 * Abstract class or interface representing a player (Human or AI).
 */
public abstract class Player {
  protected String name;
  protected int score;
  protected Rack rack;
  private boolean blitzClockEnabled;
  private long remainingTimeNanos;
  private long activeSinceNanos;
  private boolean turnTimerRunning;
  private final PlayerColor color;

  /**
   * Base constructor for any player.
   *
   * @param name  The name of the player.
   * @param color The color assigned to the player.
   */
  public Player(String name, PlayerColor color) {
    this.name = name;
    this.score = 0;
    this.rack = new Rack();
    this.blitzClockEnabled = false;
    this.remainingTimeNanos = 0L;
    this.activeSinceNanos = 0L;
    this.turnTimerRunning = false;
    this.color = color;
  }

  /**
   * Returns the player's display name.
   *
   * @return The player name.
   */
  public String getName() {
    return name;
  }

  /**
   * Returns the current score.
   *
   * @return The current score.
   */
  public int getScore() {
    return score;
  }

  /**
   * Adds points to the current score.
   *
   * @param points Number of points to add.
   */
  public void addScore(int points) {
    this.score += points;
  }

  /**
   * Sets the score to an absolute value. Used for network synchronization
   * where the server sends the total score rather than a delta.
   *
   * @param points The exact score to set.
   */
  public void setScore(int points) {
    this.score = points;
  }

  /**
   * Returns the rack associated with this player.
   *
   * @return The player's rack.
   */
  public Rack getRack() {
    return rack;
  }

  /**
   * Enables the blitz clock for this player and initializes remaining time.
   *
   * @param initialTime Initial available time for this player.
   */
  public void enableBlitzClock(Duration initialTime) {
    if (initialTime == null || initialTime.isNegative() || initialTime.isZero()) {
      throw new IllegalArgumentException("Initial blitz time must be positive.");
    }
    this.blitzClockEnabled = true;
    this.remainingTimeNanos = initialTime.toNanos();
    this.activeSinceNanos = 0L;
    this.turnTimerRunning = false;
  }

  /**
   * Disables the blitz clock and clears timer state.
   */
  public void disableBlitzClock() {
    this.blitzClockEnabled = false;
    this.remainingTimeNanos = 0L;
    this.activeSinceNanos = 0L;
    this.turnTimerRunning = false;
  }

  /**
   * Starts this player's turn timer if blitz mode is active.
   */
  public void startTurnTimer() {
    if (!blitzClockEnabled || turnTimerRunning || isOutOfTime()) {
      return;
    }
    this.activeSinceNanos = System.nanoTime();
    this.turnTimerRunning = true;
  }

  /**
   * Pauses this player's turn timer and commits elapsed time.
   */
  public void pauseTurnTimer() {
    if (!blitzClockEnabled || !turnTimerRunning) {
      return;
    }
    long elapsed = Math.max(0L, System.nanoTime() - activeSinceNanos);
    remainingTimeNanos = Math.max(0L, remainingTimeNanos - elapsed);
    turnTimerRunning = false;
    activeSinceNanos = 0L;
  }

  /**
   * Indicates whether blitz timing is enabled for this player.
   *
   * @return True if blitz timing is enabled.
   */
  public boolean isBlitzClockEnabled() {
    return blitzClockEnabled;
  }

  /**
   * Indicates whether the current turn timer is running.
   *
   * @return True if the turn timer is currently running.
   */
  public boolean isTurnTimerRunning() {
    return turnTimerRunning;
  }

  /**
   * Returns the remaining blitz time in milliseconds.
   *
   * @return Remaining time in milliseconds.
   */
  public long getRemainingTimeMillis() {
    long nanos = getRemainingTimeNanos();
    return nanos / 1_000_000L;
  }

  /**
   * Indicates whether the player has exhausted their blitz time.
   *
   * @return True if no time remains.
   */
  public boolean isOutOfTime() {
    return blitzClockEnabled && getRemainingTimeNanos() <= 0L;
  }

  /**
   * Returns remaining blitz time formatted as mm:ss.
   *
   * @return Remaining time as a display string.
   */
  public String getRemainingTimeDisplay() {
    long totalSeconds = Math.max(0L, getRemainingTimeMillis() / 1000L);
    long minutes = totalSeconds / 60L;
    long seconds = totalSeconds % 60L;
    return String.format("%02d:%02d", minutes, seconds);
  }

  private long getRemainingTimeNanos() {
    if (!blitzClockEnabled) {
      return 0L;
    }
    if (!turnTimerRunning) {
      return remainingTimeNanos;
    }
    long elapsed = Math.max(0L, System.nanoTime() - activeSinceNanos);
    return Math.max(0L, remainingTimeNanos - elapsed);
  }

  /**
   * Returns a readable representation of this player.
   *
   * @return The player name.
   */
  @Override
  public String toString() {
    return name;
  }
}
