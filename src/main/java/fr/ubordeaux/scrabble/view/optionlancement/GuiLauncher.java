package fr.ubordeaux.scrabble.view.optionlancement;

import fr.ubordeaux.scrabble.model.core.Game;
import fr.ubordeaux.scrabble.model.core.HumanPlayer;
import fr.ubordeaux.scrabble.view.gui.JavaFxView;
import fr.ubordeaux.scrabble.view.gui.ScrabbleGui;
import javafx.application.Application;

/**
 * Lance le jeu en mode GUI JavaFX.
 */
public class GuiLauncher {

  private GuiLauncher() {}

  /**
   * Starts the game in GUI mode. Uses {@code players} if provided via {@code -p}, otherwise
   * defaults to 2 players.
   *
   * @param args the command-line arguments passed to JavaFX
   * @param players the number of players (0 = use default of 2)
   */
  public static void launch(String[] args, int players) {
    int count = players > 0 ? players : OptionPlayer.DEFAULT;
    Game game = new Game();
    for (int i = 1; i <= count; i++) {
      game.addPlayer(new HumanPlayer("Joueur" + i));
    }
    JavaFxView view = new JavaFxView(game);
    ScrabbleGui.setGame(game);
    ScrabbleGui.setView(view);
    Application.launch(ScrabbleGui.class, args);
  }
}