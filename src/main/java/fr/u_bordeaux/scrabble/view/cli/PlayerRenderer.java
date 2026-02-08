package fr.u_bordeaux.scrabble.view.cli;

import java.util.List;

import fr.u_bordeaux.scrabble.model.interfaces.Player;

/**
 * Responsable de l'affichage des informations sur les joueurs.
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
        System.out.printf("║ %-30s Score: %4d pts ║%n", 
            player.getName(), player.getScore());
    }
    

    public void renderCurrentPlayer(Player player) {
        System.out.println("\n>>> Tour de : " + player.getName());
    }
}