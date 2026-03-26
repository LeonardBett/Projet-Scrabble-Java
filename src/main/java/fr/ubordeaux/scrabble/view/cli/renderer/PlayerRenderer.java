package fr.ubordeaux.scrabble.view.cli.renderer;

import fr.ubordeaux.scrabble.i18n.I18n;
import fr.ubordeaux.scrabble.model.interfaces.Player;
import java.util.List;

/**
 * Responsible for rendering player information.
 */
public class PlayerRenderer {

  /**
   * Default constructor for PlayerRenderer.
   */
  public PlayerRenderer() {
  }

  /**
   * Affiche la liste de tous les joueurs avec leur score dans la console CLI.
   *
   * @param players liste des joueurs à afficher
   */
  public void renderPlayerList(List<Player> players) {
    System.out.println("╔════════════════════════════════════════════════╗");
<<<<<<< HEAD
<<<<<<< HEAD
    System.out.println("║                   PLAYERS                      ║");
=======
    System.out.printf("║%1$-48s║%n", center(I18n.tr("cli.msg.playersTitle"), 48));
>>>>>>> 80eb4dd (Add internationalization support for GUI and CLI components)
=======
    System.out.printf("║%1$-48s║%n", center(I18n.tr("cli.msg.playersTitle"), 48));
>>>>>>> 80eb4dd275121a2ebeee7238584257f4cce5f53f
    System.out.println("╠════════════════════════════════════════════════╣");
    for (Player player : players) {
      renderPlayerScore(player);
    }
    System.out.println("╚════════════════════════════════════════════════╝");
  }

  private void renderPlayerScore(Player player) {
    String timerPart =
        player.isBlitzClockEnabled() ? " | " + player.getRemainingTimeDisplay() : "";
    System.out.printf("║ %-18s %s: %4d %s%-11s ║%n",
        player.getName(), I18n.tr("cli.msg.scoreLabel"), player.getScore(),
        I18n.tr("cli.msg.pointsAbbrev"), timerPart);
  }

  /**
   * Affiche le nom du joueur dont c'est le tour, avec son temps si le mode blitz est actif.
   *
   * @param player le joueur courant
   */
  public void renderCurrentPlayer(Player player) {
    if (player.isBlitzClockEnabled()) {
      System.out.println("\n" + I18n.tr("cli.msg.currentTurnWithTime", player.getName(),
          player.getRemainingTimeDisplay()));
      return;
    }
    System.out.println("\n" + I18n.tr("cli.msg.currentTurn", player.getName()));
  }

  private String center(String text, int width) {
    if (text.length() >= width) {
      return text;
    }
    int left = (width - text.length()) / 2;
    int right = width - text.length() - left;
    return " ".repeat(left) + text + " ".repeat(right);
  }
}