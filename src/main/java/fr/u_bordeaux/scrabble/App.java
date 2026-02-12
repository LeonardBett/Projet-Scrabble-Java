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
 * Application entry point.
 * Initializes the MVC architecture.
 */
public class App {
    
    public static void main(String[] args) {

            launchCLI();
        
    }
    


    private static void launchCLI() {
    
        Game game = new Game();
        

        CLIView view = new CLIView(game);
        
 
        GameController controller = new GameController(game, view);
        
   
        controller.runCli();
    }

    private static void launchCLI_test() {

        Game game = new Game();
        

        CLIView view = new CLIView(game);
        

        GameController controller = new GameController(game, view);

        
        controller.addPlayer(new HumanPlayer("Alice"));
        controller.addPlayer(new HumanPlayer("Bob"));
        

        controller.startGame();
        

        testGameWithController(controller);
    }
    
    /**
     * Test the game using the MVC controller.
     */
    private static void testGameWithController(GameController controller) {
        Game game = controller.getGame();
        
        try {
    
            Player p1 = game.getCurrentPlayer();
            List<Tile> rack1 = p1.getRack().getTiles();
            List<Tile> word1 = new ArrayList<>(rack1.subList(0, Math.min(5, rack1.size())));
            
           
            Move move1 = Move.createPlay(p1, word1, new Point(7, 7), Direction.HORIZONTAL);
            controller.handlePlayerMove(move1);
            
       
            Player p2 = game.getCurrentPlayer();
            List<Tile> rack2 = p2.getRack().getTiles();
            List<Tile> word2 = new ArrayList<>(rack2.subList(0, Math.min(7, rack2.size())));
            
          
            Move move2 = Move.createPlay(p2, word2, new Point(9, 6), Direction.VERTICAL);
            controller.handlePlayerMove(move2);
            
   
       
            controller.undo();
            controller.undo();
            controller.redo();
            controller.redo();
            
       
            p1 = game.getCurrentPlayer();
            rack1 = p1.getRack().getTiles();
            List<Tile> exchange = new ArrayList<>(rack1.subList(0, Math.min(3, rack1.size())));
            
            Move moveExchange = Move.createExchange(p1, exchange);
            controller.handlePlayerMove(moveExchange);
            
  
            p2 = game.getCurrentPlayer();
            Move movePass = Move.createPass(p2);
            controller.handlePlayerMove(movePass);
            
        } catch (Exception e) {
            controller.getView().displayError("Erreur durant la simulation : " + e.getMessage());
            e.printStackTrace();
        }
    }
}