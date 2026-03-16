package fr.ubordeaux.scrabble.view.cli.Renderer;

import fr.ubordeaux.scrabble.model.interfaces.Player;
import java.util.List;

/**
 * Responsible for rendering player information.
 */
public class PlayerRenderer {

  public void renderPlayerList(List<Player> players) {
    System.out.println("╔════════════════════════════════════════════════╗");
    System.out.println("║                   JOUEURS                      ║");
    System.out.println("╠════════════════════════════════════════════════╣");

    for (Player player : players) {
      renderPlayerScore(player);
    }

    System.out.println("╚════════════════════════════════════════════════╝");
  }

  private void renderPlayerScore(Player player) {
    String timerPart = player.isBlitzClockEnabled() ? " | " + player.getRemainingTimeDisplay() : "";
    System.out.printf("║ %-18s Score: %4d pts%-11s ║%n", player.getName(), player.getScore(),
        timerPart);
  }

  public void renderCurrentPlayer(Player player) {
    if (player.isBlitzClockEnabled()) {
      System.out.println("\n>>> Current turn: " + player.getName() + " ("
          + player.getRemainingTimeDisplay() + ")");
      return;
    }
    System.out.println("\n>>> Current turn: " + player.getName());
  }
}
