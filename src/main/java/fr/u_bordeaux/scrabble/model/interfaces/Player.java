package fr.u_bordeaux.scrabble.model.interfaces;

import fr.u_bordeaux.scrabble.model.core.Rack;

/**
 * Abstract class or interface representing a player (Human or AI).
 */
public abstract class Player {
    protected String name;
    protected int score;
    protected Rack rack;

    /**
     * Base constructor for any player.
     *
     * @param name The name of the player.
     */
    public Player(String name) {
        this.name = name;
        this.score = 0;
        this.rack = new Rack();
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

    @Override
    public String toString() {
        return name;
    }
}