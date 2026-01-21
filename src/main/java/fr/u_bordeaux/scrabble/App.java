package fr.u_bordeaux.scrabble;

import fr.u_bordeaux.scrabble.model.core.Game;
import fr.u_bordeaux.scrabble.model.core.Move;
import fr.u_bordeaux.scrabble.model.core.HumanPlayer;
import fr.u_bordeaux.scrabble.model.core.Tile;
import fr.u_bordeaux.scrabble.model.enums.Direction;
import fr.u_bordeaux.scrabble.model.interfaces.Player;
import fr.u_bordeaux.scrabble.model.utils.Point;

import java.util.ArrayList;
import java.util.List;

/**
 * Application entry point.
 * Handles command line arguments (CLI) and initializes the game.
 */
public class App {

    public static void main(String[] args) {
        start();
    }
    
    /**
     * Starts the game.
     */
    public static void start() {
        System.out.println("Welcome to Scrabble U-Bordeaux!");

        // Initialize the game
        Game game = new Game();

        // TODO Initialize the controller
        // TODO Initialize the view

        // Run the test scenario, will be removed
        test(game);
    }

    /**
     * Test core implementation of the Scrabble game, and show the evolution of the game in the terminal
     * Will be removed
     */
    private static void test(Game game){
        game.addPlayer(new HumanPlayer("Player1"));
        game.addPlayer(new HumanPlayer("Player2"));

        game.startGame();

        boolean showBonusSquare = false;
        game.printDebugState(showBonusSquare);

        // --- TEST SCENARIO ---
        try {
            // TURN 1: Player 1 plays 5 tiles (Horizontal)
            Player p1 = game.getCurrentPlayer();
            List<Tile> rack1 = p1.getRack().getTiles();
            List<Tile> word1 = new ArrayList<>(rack1.subList(0, Math.min(5, rack1.size())));

            System.out.println(">>> " + p1.getName() + " plays at (7,7) Horizontal");
            game.executeMove(Move.createPlay(p1, word1, new Point(7, 7), Direction.HORIZONTAL));
            game.printDebugState(showBonusSquare);

            // TURN 2: Player 2 plays 7 tiles (Vertical, crossing)
            Player p2 = game.getCurrentPlayer();
            List<Tile> rack2 = p2.getRack().getTiles();
            List<Tile> word2 = new ArrayList<>(rack2.subList(0, Math.min(7, rack2.size())));

            System.out.println(">>> " + p2.getName() + " plays at (9,6) Vertical");
            game.executeMove(Move.createPlay(p2, word2, new Point(9, 6), Direction.VERTICAL));
            game.printDebugState(showBonusSquare);

            // Test undo / redo
            System.out.println("--- TEST UNDO / REDO ---");
            game.undo();
            game.printDebugState(showBonusSquare);
            game.undo();
            game.printDebugState(showBonusSquare);
            game.redo();
            game.printDebugState(showBonusSquare);
            game.redo();
            game.printDebugState(showBonusSquare);


            // TURN 3: Player 1 exchanges 3 tiles
            p1 = game.getCurrentPlayer();
            rack1 = p1.getRack().getTiles();
            List<Tile> exchange = new ArrayList<>(rack1.subList(0, Math.min(3, rack1.size())));

            System.out.println(">>> " + p1.getName() + " exchanges 3 tiles");
            game.executeMove(Move.createExchange(p1, exchange));
            game.printDebugState(showBonusSquare);

            // TURN 4: Player 2 passes
            p2 = game.getCurrentPlayer();

            System.out.println(">>> " + p2.getName() + " passes");
            game.executeMove(Move.createPass(p2));
            game.printDebugState(showBonusSquare);

        } catch (Exception e) {
            System.err.println("Error during simulation: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
