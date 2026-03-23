package fr.ubordeaux.scrabble.view.optionlancement;

import fr.ubordeaux.scrabble.model.core.Game;
import fr.ubordeaux.scrabble.model.core.HumanPlayer;
import fr.ubordeaux.scrabble.model.enums.PlayerColor;
import fr.ubordeaux.scrabble.view.gui.JavaFxView;
import fr.ubordeaux.scrabble.view.gui.ScrabbleGui;
import javafx.application.Application;

/**
 * Lance le jeu en mode GUI JavaFX.
 */
public class GuiLauncher {

  private GuiLauncher() {
  }

  /**
   * Starts the game in GUI mode with the given number of players.
   *
   * @param args    the command-line arguments passed to JavaFX
   * @param players the number of players (between 2 and 4)
   */
  public static void launch(String[] args, int players) {
    Game game = new Game();
    for (int i = 1; i <= players; i++) {
      game.addPlayer(new HumanPlayer("Player" + i, PlayerColor.fromIndex(i - 1)));
    }
    JavaFxView view = new JavaFxView(game);
    ScrabbleGui.setGame(game);
    ScrabbleGui.setView(view);
    Application.launch(ScrabbleGui.class, args);
  }
}
