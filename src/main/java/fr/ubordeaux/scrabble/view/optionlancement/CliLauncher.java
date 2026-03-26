package fr.ubordeaux.scrabble.view.optionlancement;

import fr.ubordeaux.scrabble.controller.GameController;
<<<<<<< HEAD
<<<<<<< HEAD
import fr.ubordeaux.scrabble.model.ai.AiPlayer;
=======
import fr.ubordeaux.scrabble.i18n.I18n;
>>>>>>> 80eb4dd (Add internationalization support for GUI and CLI components)
=======
import fr.ubordeaux.scrabble.i18n.I18n;
>>>>>>> 80eb4dd275121a2ebeee7238584257f4cce5f53f
import fr.ubordeaux.scrabble.model.core.Game;
import fr.ubordeaux.scrabble.model.core.HumanPlayer;
import fr.ubordeaux.scrabble.model.enums.GameMode;
import fr.ubordeaux.scrabble.model.enums.PlayerColor;
import fr.ubordeaux.scrabble.view.cli.CliView;
import java.time.Duration;
<<<<<<< HEAD
<<<<<<< HEAD
import java.util.List;
=======
>>>>>>> c984150 (feat: Enhance game configuration and blitz mode functionality)
=======
>>>>>>> 80eb4dd275121a2ebeee7238584257f4cce5f53f

/**
 * Lance le jeu en mode CLI.
 */
public class CliLauncher {

  private CliLauncher() {}

  /**
   * Starts the game in CLI mode with the given configuration.
<<<<<<< HEAD
<<<<<<< HEAD
   * 
   * <p>If {@code players} is 0, the number of players is asked interactively. 
   * If {@code blitzMode} is true, enables blitz mode with {@code blitzMinutes} per player.
   *
   * @param players the total number of players (0 = ask, 2-4 = use directly)
   * @param aiColors the list of colors that should be controlled by AI
=======
   *
=======
   *
>>>>>>> 80eb4dd275121a2ebeee7238584257f4cce5f53f
   * <p>If {@code players} is 0, the number of players is asked interactively.
   * If {@code blitzMode} is true, enables blitz mode with {@code blitzMinutes} per player.
   *
   * @param players the number of players (0 = ask, 2-4 = use directly)
<<<<<<< HEAD
>>>>>>> c984150 (feat: Enhance game configuration and blitz mode functionality)
=======
>>>>>>> 80eb4dd275121a2ebeee7238584257f4cce5f53f
   * @param blitzMode true to enable blitz mode
   * @param blitzMinutes time limit per player in minutes (only used when blitzMode is true)
   * @param aiTime AI thinking time in seconds
   * @param useExptiminimax true to enable the Expectiminimax algorithm
   * @param useMl true to enable the Machine Learning agent
   * @param lang the dictionary language ("en" or "fr")
   */
<<<<<<< HEAD
<<<<<<< HEAD
  public static void launch(int players, List<String> aiColors, boolean blitzMode, int blitzMinutes,
      int aiTime, boolean useExptiminimax, boolean useMl, String lang) {
=======
  public static void launch(int players, boolean blitzMode, int blitzMinutes, int aiTime,
      boolean useExptiminimax, boolean useMl, String lang) {
<<<<<<< HEAD
>>>>>>> c984150 (feat: Enhance game configuration and blitz mode functionality)
=======
    I18n.setLanguage(lang);
>>>>>>> 80eb4dd (Add internationalization support for GUI and CLI components)
=======
  public static void launch(int players, boolean blitzMode, int blitzMinutes, int aiTime,
      boolean useExptiminimax, boolean useMl, String lang) {
    I18n.setLanguage(lang);
>>>>>>> 80eb4dd275121a2ebeee7238584257f4cce5f53f
    Game game = new Game();
    if (blitzMode) {
      game.enableBlitzMode(Duration.ofMinutes(blitzMinutes));
    }

    CliView view = new CliView(game);
    view.setBlitzMode(blitzMode);

    GameController controller = new GameController(game, view);
<<<<<<< HEAD
<<<<<<< HEAD

    controller.setUseMl(useMl);
    controller.setUseExptiminimax(useExptiminimax);
    controller.setAiTime(aiTime);
    controller.setLang(lang);
    int humanCount = 1;

    for (int i = 1; i <= players; i++) {
      PlayerColor color = PlayerColor.fromIndex(i - 1);

      boolean isAi = false;
      if (aiColors != null) {
        for (String aiCol : aiColors) {
          if (color.name().equalsIgnoreCase(aiCol)) {
            isAi = true;
            break;
          }
        }
      }

      if (isAi) {
        // Le temps de réflexion 'aiTime' est maintenant correctement injecté au lieu du 5
        game.addPlayer(new AiPlayer("IA-" + color.name(), 3, aiTime, color));
      } else {
        game.addPlayer(new HumanPlayer("Player" + humanCount, color));
        humanCount++;
      }
=======
=======
>>>>>>> 80eb4dd275121a2ebeee7238584257f4cce5f53f
    controller.setAiTime(aiTime);
    controller.setUseExptiminimax(useExptiminimax);
    controller.setUseMl(useMl);
    controller.setLang(lang);

    final int count = players > 0 ? players : OptionPlayer.DEFAULT;
    for (int i = 1; i <= count; i++) {
<<<<<<< HEAD
<<<<<<< HEAD
      game.addPlayer(new HumanPlayer("Joueur" + i));
>>>>>>> c984150 (feat: Enhance game configuration and blitz mode functionality)
=======
      game.addPlayer(new HumanPlayer(I18n.tr("player.defaultName") + i));
>>>>>>> 80eb4dd (Add internationalization support for GUI and CLI components)
=======
      game.addPlayer(new HumanPlayer(I18n.tr("player.defaultName") + i));
>>>>>>> 80eb4dd275121a2ebeee7238584257f4cce5f53f
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