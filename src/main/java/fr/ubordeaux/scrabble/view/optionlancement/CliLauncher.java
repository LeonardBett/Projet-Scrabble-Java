package fr.ubordeaux.scrabble.view.optionlancement;

import fr.ubordeaux.scrabble.controller.GameController;
import fr.ubordeaux.scrabble.model.core.Game;
import fr.ubordeaux.scrabble.model.core.HumanPlayer;
import fr.ubordeaux.scrabble.view.cli.CliView;
import java.time.Duration;

/**
 * Lance le jeu en mode CLI.
 */
public class CliLauncher {

  private CliLauncher() {}

  /**
   * Starts the game in CLI mode with the given configuration.
   *
   * <p>If {@code players} is 0, the number of players is asked interactively.
   * If {@code blitzMode} is true, enables blitz mode with {@code blitzMinutes} per player.
   *
   * @param players the number of players (0 = ask, 2-4 = use directly)
   * @param blitzMode true to enable blitz mode
   * @param blitzMinutes time limit per player in minutes (only used when blitzMode is true)
   * @param aiTime AI thinking time in seconds
   * @param useExptiminimax true to enable the Expectiminimax algorithm
   * @param useMl true to enable the Machine Learning agent
   * @param lang the dictionary language ("en" or "fr")
   */
  public static void launch(int players, boolean blitzMode, int blitzMinutes, int aiTime,
      boolean useExptiminimax, boolean useMl, String lang) {
    Game game = new Game();
    if (blitzMode) {
      game.enableBlitzMode(Duration.ofMinutes(blitzMinutes));
    }

    CliView view = new CliView(game);
    view.setBlitzMode(blitzMode);

    GameController controller = new GameController(game, view);
    controller.setAiTime(aiTime);
    controller.setUseExptiminimax(useExptiminimax);
    controller.setUseMl(useMl);
    controller.setLang(lang);

    final int count = players > 0 ? players : OptionPlayer.DEFAULT;
    for (int i = 1; i <= count; i++) {
      game.addPlayer(new HumanPlayer("Joueur" + i));
    }

    controller.runCli();
  }

  /**
   * Starts the game in CLI mode with default configuration and the given player count.
   *
   * @param players the number of players (0 = ask interactively)
   */
  public static void launch(int players) {
    launch(players, false, 30, 5, false, false, "en");
  }
}