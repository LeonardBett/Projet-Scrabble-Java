package fr.u_bordeaux.scrabble.model.interfaces;

import fr.u_bordeaux.scrabble.model.core.Rack;

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

    /**
     * Base constructor for any player.
     *
     * @param name The name of the player.
     */
    public Player(String name) {
        this.name = name;
        this.score = 0;
        this.rack = new Rack();
        this.blitzClockEnabled = false;
        this.remainingTimeNanos = 0L;
        this.activeSinceNanos = 0L;
        this.turnTimerRunning = false;
    }

    public String getName() {
        return name;
    }

    public int getScore() {
        return score;
    }

    public void addScore(int points) {
        this.score += points;
    }

    public Rack getRack() {
        return rack;
    }

    /**
     * Enables the blitz clock for this player and initializes remaining time.
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

    public boolean isBlitzClockEnabled() {
        return blitzClockEnabled;
    }

    public boolean isTurnTimerRunning() {
        return turnTimerRunning;
    }

    public long getRemainingTimeMillis() {
        long nanos = getRemainingTimeNanos();
        return nanos / 1_000_000L;
    }

    public boolean isOutOfTime() {
        return blitzClockEnabled && getRemainingTimeNanos() <= 0L;
    }

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

    @Override
    public String toString() {
        return name;
    }
}