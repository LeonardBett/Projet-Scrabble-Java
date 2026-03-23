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
   * @param players  the total number of players (between 2 and 4)
   * @param aiColors the list of colors that should be controlled by AI
   */
  public static void launch(int players, List<String> aiColors) {
    Game game = new Game();
    int humanCount = 1;

    for (int i = 1; i <= players; i++) {
      PlayerColor color = PlayerColor.fromIndex(i - 1);

      // Check if the current color is requested as an AI player
      boolean isAi = false;
      if (aiColors != null) {
        for (String aiCol : aiColors) {
          if (color.name().equalsIgnoreCase(aiCol)) {
            isAi = true;
            break;
          }
        }
      }

      // Instantiate the correct player type based on the parsed arguments
      if (isAi) {
        game.addPlayer(new AiPlayer("IA-" + color.name(), 3, 5, color));
      } else {
        game.addPlayer(new HumanPlayer("Player" + humanCount, color));
        humanCount++;
      }
    }

    CliView view = new CliView(game);
    GameController controller = new GameController(game, view);
    controller.runCli();
  }
}