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
   * Renders the list of all players with their score in the CLI console.
   *
   * @param players list of players to render
   */
  public void renderPlayerList(List<Player> players) {
    System.out.println("╔════════════════════════════════════════════════╗");
    System.out.printf("║ %-46s ║%n", I18n.translate("cli.player.listTitle"));
    System.out.println("╠════════════════════════════════════════════════╣");
    for (Player player : players) {
      renderPlayerScore(player);
    }
    System.out.println("╚════════════════════════════════════════════════╝");
  }

  private void renderPlayerScore(Player player) {
    String timerPart =
        player.isBlitzClockEnabled() ? " | " + player.getRemainingTimeDisplay() : "";
    String score = I18n.translate("cli.player.score", player.getScore());
    System.out.printf("║ %-18s %-16s%-11s ║%n", player.getName(), score, timerPart);
  }

  /**
   * Renders the current player's name, with remaining time if blitz mode is active.
   *
   * @param player current player
   */
  public void renderCurrentPlayer(Player player) {
    if (player.isBlitzClockEnabled()) {
      System.out.println("\n>>> " + I18n.translate(
          "cli.player.currentTurnWithTime",
          player.getName(),
          player.getRemainingTimeDisplay()));
      return;
    }
    System.out.println("\n>>> " + I18n.translate("cli.player.currentTurn", player.getName()));
  }
}