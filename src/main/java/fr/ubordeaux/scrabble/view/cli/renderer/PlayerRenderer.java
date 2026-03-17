package fr.ubordeaux.scrabble.view.cli.renderer;

import fr.ubordeaux.scrabble.model.interfaces.Player;
import java.util.List;  

/**
 * Responsible for rendering player information.
 */
public class PlayerRenderer {

  /**
   * Affiche la liste de tous les joueurs avec leur score dans la console CLI.
   *
   * @param players liste des joueurs à afficher
   */
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
    String timerPart =
        player.isBlitzClockEnabled() ? " | " + player.getRemainingTimeDisplay() : "";
    System.out.printf("║ %-18s Score: %4d pts%-11s ║%n",
        player.getName(), player.getScore(), timerPart);
  }

  /**
   * Affiche le nom du joueur dont c'est le tour, avec son temps si le mode blitz est actif. 
   *
   * @param player le joueur courant
   */
  public void renderCurrentPlayer(Player player) {
    if (player.isBlitzClockEnabled()) {
      System.out.println("\n>>> Current turn: " + player.getName()
          + " (" + player.getRemainingTimeDisplay() + ")");
      return;
    }
    System.out.println("\n>>> Current turn: " + player.getName());
  }
}