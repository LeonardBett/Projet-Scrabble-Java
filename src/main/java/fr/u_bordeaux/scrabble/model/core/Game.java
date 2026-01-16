package fr.u_bordeaux.scrabble.model.core;

import fr.u_bordeaux.scrabble.model.interfaces.Player;

import java.util.ArrayList;
import java.util.List;

/**
 * The main game engine.
 * Manages the board, players, turns, and executes moves.
 * This is where the "business logic" resides.
 */
public class Game {
    private final Board board;
    private final List<Player> players;
    private int currentPlayerIndex;
    private boolean isGameOver;

    public Game() {
        this.board = new Board();
        this.players = new ArrayList<>();
        this.currentPlayerIndex = 0;
        this.isGameOver = false;
    }

    public void addPlayer(Player player) {
        players.add(player);
    }

    /**
     * Executes a player's move.
     * This method acts as the referee: it validates and applies the changes.
     *
     * @param move The move to execute.
     * @throws IllegalArgumentException if the move is invalid (rules violation).
     */
    public void executeMove(Move move) {
        if (isGameOver) {
            throw new IllegalStateException("Game is over.");
        }

        // 1. Validate that it's the correct player's turn
        if (!move.getPlayer().equals(getCurrentPlayer())) {
            throw new IllegalArgumentException("It is not " + move.getPlayer() + "'s turn.");
        }

        // 2. Dispatch logic based on move type
        switch (move.getType()) {
            case PLAY -> handlePlayMove(move);
            case EXCHANGE -> handleExchangeMove(move);
            case PASS -> handlePassMove(move);
        }

        // 3. Prepare next turn
        nextTurn();
    }

    private void handlePlayMove(Move move) {
        // TODO: Validate word placement on 'board' (using Board methods)
        // TODO: Calculate score (using Scoring class)
        // TODO: Update 'board' with new tiles
        // TODO: Remove tiles from player's rack and refill from Bag
        System.out.println("Player " + move.getPlayer() + " played a word.");
    }

    private void handleExchangeMove(Move move) {
        // TODO: Implement tile exchange logic with the Bag
        System.out.println("Player " + move.getPlayer() + " exchanged tiles.");
    }

    private void handlePassMove(Move move) {
        System.out.println("Player " + move.getPlayer() + " passed.");
    }

    private void nextTurn() {
        if (!players.isEmpty()) {
            currentPlayerIndex = (currentPlayerIndex + 1) % players.size();
        }
    }

    public Player getCurrentPlayer() {
        return players.isEmpty() ? null : players.get(currentPlayerIndex);
    }

    public Board getBoard() {
        return board;
    }
}