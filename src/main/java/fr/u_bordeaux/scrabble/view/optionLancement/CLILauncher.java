package fr.u_bordeaux.scrabble.view.optionLancement;

import fr.u_bordeaux.scrabble.controller.GameController;
import fr.u_bordeaux.scrabble.model.core.Game;
import fr.u_bordeaux.scrabble.model.core.HumanPlayer;
import fr.u_bordeaux.scrabble.view.cli.CLIView;

/**
 * Lance le jeu en mode CLI.
 */
public class CLILauncher {

    private CLILauncher() {}

    public static void launch(int players) {
        Game game = new Game();

         for (int i = 1; i <= players; i++) {
            game.addPlayer(new HumanPlayer("Player" + i));
        }
        CLIView view = new CLIView(game);
        GameController controller = new GameController(game, view);
        controller.runCli();
    }
}