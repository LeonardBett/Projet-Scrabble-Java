package fr.ubordeaux.scrabble.view.optionlancement;

<<<<<<< HEAD
=======
import fr.ubordeaux.scrabble.i18n.I18n;
>>>>>>> 80eb4dd (Add internationalization support for GUI and CLI components)
import fr.ubordeaux.scrabble.model.ai.AiPlayer;
import fr.ubordeaux.scrabble.model.core.Game;
import fr.ubordeaux.scrabble.model.core.HumanPlayer;
import fr.ubordeaux.scrabble.model.enums.GameMode;
import fr.ubordeaux.scrabble.model.enums.PlayerColor;
import fr.ubordeaux.scrabble.view.gui.JavaFxView;
import fr.ubordeaux.scrabble.view.gui.ScrabbleGui;
import java.time.Duration;
<<<<<<< HEAD
<<<<<<< HEAD
import java.util.List;
=======
>>>>>>> c984150 (feat: Enhance game configuration and blitz mode functionality)
=======
import java.util.Set;
>>>>>>> 80eb4dd (Add internationalization support for GUI and CLI components)
import javafx.application.Application;

/**
 * Lance le jeu en mode GUI JavaFX.
 */
public class GuiLauncher {

  private GuiLauncher() {}

  /**
<<<<<<< HEAD
<<<<<<< HEAD
   * Starts the game in GUI mode with the given configuration.
   * 
   * <p>Uses {@code players} if provided via {@code -p}, otherwise defaults to 2. 
   * If {@code blitzMode} is true, enables blitz mode with {@code blitzMinutes} per player.
   *
   * @param args the command-line arguments passed to JavaFX
   * @param players the number of players (between 2 and 4)
   * @param aiColors colors controlled by AI players
   * @param blitzMode true to enable blitz mode
   * @param blitzMinutes time limit per player in minutes (only used when blitzMode is true)
   * @param aiTime AI thinking time in seconds
   * @param useExptiminimax true to enable the Expectiminimax algorithm
   * @param useMl true to enable the Machine Learning agent
   * @param lang the dictionary language ("en" or "fr")
   */
  public static void launch(String[] args, int players, List<String> aiColors, boolean blitzMode,
      int blitzMinutes, int aiTime, boolean useExptiminimax, boolean useMl, String lang) {
    int count = players > 0 ? players : OptionPlayer.DEFAULT;
    Game game = new Game();
    if (blitzMode) {
      game.enableBlitzMode(Duration.ofMinutes(blitzMinutes));
=======
   * Starts the game in GUI mode. Uses {@code players} if provided via {@code -p}, otherwise
   * defaults to 2 players.
=======
   * Starts the game in GUI mode with the given configuration.
   *
   * <p>Uses {@code players} if provided via {@code -p}, otherwise defaults to 2.
   * If {@code blitzMode} is true, enables blitz mode with {@code blitzMinutes} per player.
>>>>>>> c984150 (feat: Enhance game configuration and blitz mode functionality)
   *
   * @param args the command-line arguments passed to JavaFX
   * @param players the number of players (0 = use default of 2)
   * @param blitzMode true to enable blitz mode
   * @param blitzMinutes time limit per player in minutes (only used when blitzMode is true)
   */
  public static void launch(String[] args, int players, boolean blitzMode, int blitzMinutes) {
    launch(args, players, blitzMode, blitzMinutes, 5, false, false, "en");
  }

  /**
   * Starts the game in GUI mode with AI and language settings.
   *
   * @param args the command-line arguments passed to JavaFX
   * @param players the number of players (0 = use default of 2)
   * @param blitzMode true to enable blitz mode
   * @param blitzMinutes time limit per player in minutes (only used when blitzMode is true)
   * @param aiTime AI thinking time in seconds
   * @param useExptiminimax true to use Expectiminimax for AI players
   * @param useMl true to enable ML for AI players
   * @param lang language code ("en" or "fr")
   */
  public static void launch(String[] args, int players, boolean blitzMode, int blitzMinutes,
      int aiTime, boolean useExptiminimax, boolean useMl, String lang) {
    I18n.setLanguage(lang);
    int count = players > 0 ? players : OptionPlayer.DEFAULT;
    Game game = new Game();
    if (blitzMode) {
      game.enableBlitzMode(Duration.ofMinutes(blitzMinutes));
    }

    Set<String> aiColors = ScrabbleGui.extractAiColorSelections(args);
    for (int i = 1; i <= count; i++) {
<<<<<<< HEAD
      game.addPlayer(new HumanPlayer("Joueur" + i));
>>>>>>> 615b204 (fix bug)
    }
    int humanCount = 1;
    for (int i = 1; i <= count; i++) {
      PlayerColor color = PlayerColor.fromIndex(i - 1);
      boolean isAi =
          aiColors != null && aiColors.stream().anyMatch(c -> c.equalsIgnoreCase(color.name()));

      if (isAi) {
        AiPlayer ai = new AiPlayer("IA-" + color.name(), 3, aiTime, color);
        ai.setExpectiminimaxMode(useExptiminimax);
        // ML configuration is not wired in GUI yet.
        game.addPlayer(ai);
      } else {
        game.addPlayer(new HumanPlayer("Player" + humanCount, color));
        humanCount++;
      }
    }
=======
      String colorName = ScrabbleGui.playerColorName(i);
      String playerName = I18n.tr("player.defaultName") + i;
      if (aiColors.contains(colorName)) {
        AiPlayer ai = new AiPlayer(playerName, aiTime, aiTime);
        ai.setExpectiminimaxMode(useExptiminimax);
        game.addPlayer(ai);
      } else {
        game.addPlayer(new HumanPlayer(playerName));
      }
    }
>>>>>>> 80eb4dd (Add internationalization support for GUI and CLI components)

    JavaFxView view = new JavaFxView(game);
    ScrabbleGui.setGame(game);
    ScrabbleGui.setView(view);
    Application.launch(ScrabbleGui.class, args);
  }
<<<<<<< HEAD
<<<<<<< HEAD
=======
>>>>>>> c984150 (feat: Enhance game configuration and blitz mode functionality)

  /**
   * Starts the game in GUI mode without blitz mode.
   *
   * @param args the command-line arguments passed to JavaFX
   * @param players the number of players (0 = use default of 2)
   */
  public static void launch(String[] args, int players) {
<<<<<<< HEAD
    launch(args, players, List.of(), false, 30, 5, false, false, "en");
  }
}
=======
}
>>>>>>> 615b204 (fix bug)
=======
    launch(args, players, false, 30);
  }
}
>>>>>>> c984150 (feat: Enhance game configuration and blitz mode functionality)
