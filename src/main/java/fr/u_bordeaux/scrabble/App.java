package fr.u_bordeaux.scrabble;

import java.util.ArrayList;
import java.util.List;

import fr.u_bordeaux.scrabble.controller.GameController;
import fr.u_bordeaux.scrabble.model.core.Game;
import fr.u_bordeaux.scrabble.model.core.HumanPlayer;
import fr.u_bordeaux.scrabble.model.core.Move;
import fr.u_bordeaux.scrabble.model.core.Tile;
import fr.u_bordeaux.scrabble.model.enums.Direction;
import fr.u_bordeaux.scrabble.model.interfaces.Player;
import fr.u_bordeaux.scrabble.model.utils.Point;
import fr.u_bordeaux.scrabble.view.cli.CLIView;

/**
 * Point d'entrée de l'application.
 * Initialise l'architecture MVC.
 */
public class App {
    
    public static void main(String[] args) {

            launchCLI();
        
    }
    
    /**
     * Lance l'interface graphique (GUI).
     */

    
    /**
     * Lance l'interface en ligne de commande (CLI).
     */
    private static void launchCLI() {
        // 🎯 1. Créer le MODÈLE
        Game game = new Game();
        
        // 🎯 2. Créer la VUE
        CLIView view = new CLIView(game);
        
        // 🎯 3. Créer le CONTRÔLEUR
        GameController controller = new GameController(game, view);

        
        controller.addPlayer(new HumanPlayer("Alice"));
        controller.addPlayer(new HumanPlayer("Bob"));
        
        // 🎯 5. Démarrer le jeu
        controller.startGame();
        
        // 🎯 6. Simuler quelques coups (test)
        testGameWithController(controller);
    }
    
    /**
     * Test du jeu avec le contrôleur MVC.
     */
    private static void testGameWithController(GameController controller) {
        Game game = controller.getGame();
        
        try {
            // TOUR 1 : Alice joue
            Player p1 = game.getCurrentPlayer();
            List<Tile> rack1 = p1.getRack().getTiles();
            List<Tile> word1 = new ArrayList<>(rack1.subList(0, Math.min(5, rack1.size())));
            
           
            Move move1 = Move.createPlay(p1, word1, new Point(7, 7), Direction.HORIZONTAL);
            controller.handlePlayerMove(move1);
            
            // TOUR 2 : Bob joue
            Player p2 = game.getCurrentPlayer();
            List<Tile> rack2 = p2.getRack().getTiles();
            List<Tile> word2 = new ArrayList<>(rack2.subList(0, Math.min(7, rack2.size())));
            
          
            Move move2 = Move.createPlay(p2, word2, new Point(9, 6), Direction.VERTICAL);
            controller.handlePlayerMove(move2);
            
            // Test UNDO
       
            controller.undo();
            controller.undo();
            controller.redo();
            controller.redo();
            
            // TOUR 3 : Alice échange
            p1 = game.getCurrentPlayer();
            rack1 = p1.getRack().getTiles();
            List<Tile> exchange = new ArrayList<>(rack1.subList(0, Math.min(3, rack1.size())));
            
            Move moveExchange = Move.createExchange(p1, exchange);
            controller.handlePlayerMove(moveExchange);
            
            // TOUR 4 : Bob passe
            p2 = game.getCurrentPlayer();
            Move movePass = Move.createPass(p2);
            controller.handlePlayerMove(movePass);
            
        } catch (Exception e) {
            controller.getView().displayError("Erreur durant la simulation : " + e.getMessage());
            e.printStackTrace();
        }
    }
}