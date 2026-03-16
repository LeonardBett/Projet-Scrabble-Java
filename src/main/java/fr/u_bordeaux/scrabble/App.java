package fr.u_bordeaux.scrabble;

import fr.u_bordeaux.scrabble.controller.GameController;
import fr.u_bordeaux.scrabble.model.core.Game;
import fr.u_bordeaux.scrabble.model.core.HumanPlayer;
import fr.u_bordeaux.scrabble.model.core.Move;
import fr.u_bordeaux.scrabble.model.core.Tile;
import fr.u_bordeaux.scrabble.model.enums.Direction;
import fr.u_bordeaux.scrabble.model.interfaces.Player;
import fr.u_bordeaux.scrabble.model.utils.Point;
import fr.u_bordeaux.scrabble.view.cli.CLIView;
import fr.u_bordeaux.scrabble.view.gui.JavaFxView;
import fr.u_bordeaux.scrabble.view.gui.ScrabbleGUI;
import fr.u_bordeaux.scrabble.view.optionLancement.HelpPrinter;
import java.util.ArrayList;
import java.util.List;
import javafx.application.Application;

/**
 * Main entry point of the application.
 * Parses command-line arguments and launches the appropriate interface.
 */
public class App {

  public static void main(String[] args) {
    boolean guiMode = false;
    boolean blitzMode = false;
    
    // AI and Game specific configuration variables
    int aiTime = 5;
    boolean useExptiminimax = false;
    boolean useMl = false;
    String lang = "en"; // Default language

    for (int i = 0; i < args.length; i++) {
      String arg = args[i];
      if ("--gui".equals(arg) || "-g".equals(arg)) {
        guiMode = true;
      } else if ("--blitz".equals(arg) || "-b".equals(arg)) {
        blitzMode = true;
      } else if ("--help".equals(arg) || "-h".equals(arg)) {
        HelpPrinter.printHelp();
        return;
      } else if ("--version".equals(arg) || "-V".equals(arg)) {
        HelpPrinter.printVersion();
        return;
      } else if ("-l".equals(arg) || "--lang".equals(arg)) {
        if (i + 1 < args.length) {
          lang = args[++i].toLowerCase();
          if (!lang.equals("fr") && !lang.equals("en")) {
            System.err.println("Unsupported language: " + lang + ". Falling back to 'en'.");
            lang = "en";
          }
        } else {
          System.err.println("Missing value for language. Using default 'en'.");
        }
      } else if ("-ai-time".equals(arg) || "--ai-time".equals(arg)) {
        if (i + 1 < args.length) {
          try {
            aiTime = Integer.parseInt(args[++i]);
          } catch (NumberFormatException e) {
            System.err.println("Invalid AI time. Using default of 5 seconds.");
          }
        } else {
          System.err.println("Missing value for AI time. Using default of 5 seconds.");
        }
      } else if ("-ai-exptiminimax".equals(arg) || "--ai-exptiminimax".equals(arg)) {
        useExptiminimax = true;
      } else if ("--ai-ml".equals(arg)) {
        useMl = true;
      }
    }

    if (guiMode) {
      launchGUI(args, blitzMode, aiTime, useExptiminimax, useMl, lang);
    } else {
      launchCLI(blitzMode, aiTime, useExptiminimax, useMl, lang);
    }
  }

  /**
   * Launches the Command Line Interface (CLI) mode.
   *
   * @param blitzMode True if blitz mode is enabled.
   * @param aiTime The thinking time allocated for the AI in seconds.
   * @param useExptiminimax True if the AI should use the Exptiminimax algorithm.
   * @param useMl True if the AI should use Machine Learning for word search.
   * @param lang The language of the dictionary to load ("en" or "fr").
   */
  private static void launchCLI(
      boolean blitzMode, int aiTime, boolean useExptiminimax, boolean useMl, String lang) {
    Game game = new Game();
    if (blitzMode) {
      game.enableBlitzMode();
    }
    
    CLIView view = new CLIView(game);
    view.setBlitzMode(blitzMode);
    GameController controller = new GameController(game, view);
    
    // Inject configurations into the controller
    controller.setAiTime(aiTime);
    controller.setUseExptiminimax(useExptiminimax);
    controller.setUseMl(useMl);
    controller.setLang(lang);
    
    controller.runCli();
  }

  /**
   * Launches the Graphical User Interface (GUI) mode.
   */
  private static void launchGUI(
      String[] args, boolean blitzMode, int aiTime, boolean useExptiminimax, boolean useMl, String lang) {
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