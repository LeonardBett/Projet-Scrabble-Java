package fr.ubordeaux.scrabble.view.optionlancement;

import fr.ubordeaux.scrabble.i18n.I18n;
import fr.ubordeaux.scrabble.model.ai.AiPlayer;
import fr.ubordeaux.scrabble.model.core.Game;
import fr.ubordeaux.scrabble.model.core.HumanPlayer;
import fr.ubordeaux.scrabble.view.gui.JavaFxView;
import fr.ubordeaux.scrabble.view.gui.ScrabbleGui;
import java.time.Duration;
import java.util.Set;
import javafx.application.Application;

/**
 * Lance le jeu en mode GUI JavaFX.
 */
public class GuiLauncher {

  private GuiLauncher() {}

  /**
   * Starts the game in GUI mode with the given configuration.
   *
   * <p>Uses {@code players} if provided via {@code -p}, otherwise defaults to 2.
   * If {@code blitzMode} is true, enables blitz mode with {@code blitzMinutes} per player.
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