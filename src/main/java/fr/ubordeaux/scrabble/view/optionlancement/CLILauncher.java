package fr.ubordeaux.scrabble.view.optionlancement;

import fr.ubordeaux.scrabble.controller.GameController;
import fr.ubordeaux.scrabble.model.core.Game;
import fr.ubordeaux.scrabble.model.core.HumanPlayer;
import fr.ubordeaux.scrabble.view.cli.CLIView;

/**
 * Lance le jeu en mode CLI.
 */
public class CLILauncher {

  private CLILauncher() {}

  /**
   * Starts the game in CLI mode with the given number of players.
   *
   * @param players the number of players (between 2 and 4)
   */
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
