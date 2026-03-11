package fr.u_bordeaux.scrabble;

import fr.u_bordeaux.scrabble.view.optionLancement.CLILauncher;
import fr.u_bordeaux.scrabble.view.optionLancement.GUILauncher;
import fr.u_bordeaux.scrabble.view.optionLancement.HelpPrinter;
import fr.u_bordeaux.scrabble.view.optionLancement.OptionPlayer;

/**
 * Point d'entrée de l'application.
 * Switch direct sur l'argument → appelle le fichier correspondant.
 */
public class App {
    
     public static void main(String[] args) {
        boolean guiMode = false;
        boolean blitzMode = false;

        for (String arg : args) {
            if ("--gui".equals(arg)) {
                guiMode = true;
            } else if ("--blitz".equals(arg)) {
                blitzMode = true;
            }
        }

        if (guiMode) {
            launchGUI(args, blitzMode);
        } else {
            launchCLI(blitzMode);
        }
    }
    


    private static void launchCLI(boolean blitzMode) {
        Game game = new Game();
        if (blitzMode) {
            game.enableBlitzMode();
        }
        CLIView view = new CLIView(game);
        view.setBlitzMode(blitzMode);
        GameController controller = new GameController(game, view);
        controller.runCli();
    }

    private static void launchGUI(String[] args, boolean blitzMode) {
        Game game = new Game();
        JavaFxView view = new JavaFxView(game);
        ScrabbleGUI.setGame(game);
        ScrabbleGUI.setView(view);
        Application.launch(ScrabbleGUI.class, args);
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