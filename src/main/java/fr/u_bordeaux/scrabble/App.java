package fr.u_bordeaux.scrabble;

import fr.u_bordeaux.scrabble.model.core.Board;

/**
 * Application entry point.
 * Handles command line arguments (CLI) and initializes the game.
 */
public class App {

    public static void start() {
        System.out.println("Welcome to Scrabble U-Bordeaux!");

        // Initialize the board (test)
        Board board = new Board();
        System.out.println("Board initialized successfully (" + Board.SIZE + "x" + Board.SIZE + ").");

        // TODO: Initialize players and start the game loop
    }
}
