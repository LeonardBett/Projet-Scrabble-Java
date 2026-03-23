package fr.ubordeaux.scrabble.view.optionlancement;

import fr.ubordeaux.scrabble.controller.GameController;
import fr.ubordeaux.scrabble.model.ai.AiPlayer;
import fr.ubordeaux.scrabble.model.core.Game;
import fr.ubordeaux.scrabble.model.core.HumanPlayer;
import fr.ubordeaux.scrabble.model.enums.PlayerColor;
import fr.ubordeaux.scrabble.view.cli.CliView;
import java.util.List;

/**
 * Lance le jeu en mode CLI.
 */
public class CliLauncher {

  private CliLauncher() {
  }

  /**
   * Starts the game in CLI mode with the given number of players and AI configurations.
   *
   * @param players         the total number of players (between 2 and 4)
   * @param aiColors        the list of colors that should be controlled by AI
   * @param useMl           whether to use the Machine Learning agent
   * @param useExptiminimax whether to use the Expectiminimax algorithm
   * @param aiTime          the reflection time limit for the AI
   * @param lang            the language of the game
   */
  public static void launch(int players, List<String> aiColors, boolean useMl,
                            boolean useExptiminimax, int aiTime, String lang) {

    Game game = new Game();
    CliView view = new CliView(game);
    GameController controller = new GameController(game, view);

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
    }

    controller.runCli();
  }
}