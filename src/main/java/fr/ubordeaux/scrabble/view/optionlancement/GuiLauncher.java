package fr.ubordeaux.scrabble.view.optionlancement;

import fr.ubordeaux.scrabble.model.core.Game;
import fr.ubordeaux.scrabble.model.core.HumanPlayer;
import fr.ubordeaux.scrabble.model.enums.PlayerColor;
import fr.ubordeaux.scrabble.view.gui.JavaFxView;
import fr.ubordeaux.scrabble.view.gui.ScrabbleGui;
import java.time.Duration;
import javafx.application.Application;

/**
 * Lance le jeu en mode GUI JavaFX.
 */
public class GuiLauncher {

  private GuiLauncher() {
  }

  /**
   * Starts the game in GUI mode with the given configuration.
   *
   * <p>Uses {@code players} if provided via {@code -p}, otherwise defaults to 2.
   * If {@code blitzMode} is true, enables blitz mode with {@code blitzMinutes} per player.
   *
   * @param args    the command-line arguments passed to JavaFX
   * @param players the number of players (between 2 and 4)
   * @param blitzMode true to enable blitz mode
   * @param blitzMinutes time limit per player in minutes (only used when blitzMode is true)
   */
  public static void launch(String[] args, int players, boolean blitzMode, int blitzMinutes) {
    int count = players > 0 ? players : OptionPlayer.DEFAULT;
    Game game = new Game();
    if (blitzMode) {
      game.enableBlitzMode(Duration.ofMinutes(blitzMinutes));
    }
    for (int i = 1; i <= players; i++) {
      game.addPlayer(new HumanPlayer("Player" + i, PlayerColor.fromIndex(i - 1)));
    }
    JavaFxView view = new JavaFxView(game);
    ScrabbleGui.setGame(game);
    ScrabbleGui.setView(view);
    Application.launch(ScrabbleGui.class, args);
  }

  /**
   * Starts the game in GUI mode without blitz mode.
   *
   * @param args the command-line arguments passed to JavaFX
   * @param players the number of players (0 = use default of 2)
   */
  public static void launch(String[] args, int players) {
    launch(args, players, false, 30);
  }
}