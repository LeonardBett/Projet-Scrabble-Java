package fr.u_bordeaux.scrabble.view.cli;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import fr.u_bordeaux.scrabble.model.core.Move;
import fr.u_bordeaux.scrabble.model.core.Tile;
import fr.u_bordeaux.scrabble.model.enums.Direction;
import fr.u_bordeaux.scrabble.model.interfaces.Player;
import fr.u_bordeaux.scrabble.model.utils.Point;

/**
 * Gère les entrées utilisateur en CLI.
 */
public class CLIInputHandler {
    
    private final Scanner scanner;
    private final MessageRenderer messageRenderer;
    
    public CLIInputHandler() {
        this.scanner = new Scanner(System.in);
        this.messageRenderer = new MessageRenderer();
    }
    
    /**
     * Demande au joueur quelle action il veut effectuer.
     */
    public String askAction() {
        messageRenderer.sectionTitle("CHOISISSEZ UNE ACTION");
        System.out.println("1. Jouer un mot");
        System.out.println("2. Échanger des lettres");
        System.out.println("3. Passer le tour");
        System.out.println("4. Annuler le coup précédent");
        System.out.println("5. Refaire le coup annulé");
        System.out.println("6. Quitter");
        System.out.print("\nVotre choix (1-6) : ");
        
        return scanner.nextLine().trim();
    }
    
    /**
     * Demande au joueur de créer un coup "jouer un mot".
     */
    public Move askPlayMove(Player player) {
        try {
            // 1. Demander la position de départ
            System.out.print("\nPosition de départ (format: x y, ex: 7 7) : ");
            String[] posInput = scanner.nextLine().trim().split("\\s+");
            int x = Integer.parseInt(posInput[0]);
            int y = Integer.parseInt(posInput[1]);
            Point startPoint = new Point(x, y);
            
            // 2. Demander la direction
            System.out.print("Direction (H pour horizontal, V pour vertical) : ");
            String dirInput = scanner.nextLine().trim().toUpperCase();
            Direction direction = dirInput.equals("H") ? Direction.HORIZONTAL : Direction.VERTICAL;
            
            // 3. Demander les lettres à jouer
            System.out.print("Lettres à jouer (ex: HELLO) : ");
            String lettersInput = scanner.nextLine().trim().toUpperCase();
            
            // 4. Convertir les lettres en Tiles
            List<Tile> tiles = new ArrayList<>();
            List<Tile> rack = player.getRack().getTiles();
            
            for (char letter : lettersInput.toCharArray()) {
                // Chercher la lettre dans le chevalet
                boolean found = false;
                for (Tile tile : rack) {
                    if (tile.getCharacter() == letter && !tiles.contains(tile)) {
                        tiles.add(tile);
                        found = true;
                        break;
                    }
                }
                
                if (!found) {
                    messageRenderer.error("La lettre '" + letter + "' n'est pas dans votre chevalet !");
                    return null;
                }
            }
            
            return Move.createPlay(player, tiles, startPoint, direction);
            
        } catch (Exception e) {
            messageRenderer.error("Format invalide ! " + e.getMessage());
            return null;
        }
    }
    
    /**
     * Demande au joueur quelles lettres échanger.
     */
    public Move askExchangeMove(Player player) {
        try {
            System.out.print("\nLettres à échanger (ex: ABC) : ");
            String lettersInput = scanner.nextLine().trim().toUpperCase();
            
            List<Tile> tiles = new ArrayList<>();
            List<Tile> rack = player.getRack().getTiles();
            
            for (char letter : lettersInput.toCharArray()) {
                boolean found = false;
                for (Tile tile : rack) {
                    if (tile.getCharacter() == letter && !tiles.contains(tile)) {
                        tiles.add(tile);
                        found = true;
                        break;
                    }
                }
                
                if (!found) {
                    messageRenderer.error("La lettre '" + letter + "' n'est pas dans votre chevalet !");
                    return null;
                }
            }
            
            return Move.createExchange(player, tiles);
            
        } catch (Exception e) {
            messageRenderer.error("Format invalide ! " + e.getMessage());
            return null;
        }
    }
    
    /**
     * Demande le nombre de joueurs.
     */
    public int askNumberOfPlayers() {
        while (true) {
            System.out.print("\nNombre de joueurs (2-4) : ");
            try {
                int num = Integer.parseInt(scanner.nextLine().trim());
                if (num >= 2 && num <= 4) {
                    return num;
                }
                messageRenderer.warning("Le nombre de joueurs doit être entre 2 et 4.");
            } catch (NumberFormatException e) {
                messageRenderer.error("Veuillez entrer un nombre valide.");
            }
        }
    }
    
    /**
     * Demande le nom d'un joueur.
     */
    public String askPlayerName(int playerNumber) {
        System.out.print("Nom du joueur " + playerNumber + " : ");
        return scanner.nextLine().trim();
    }
    
    /**
     * Demande une confirmation (oui/non).
     */
    public boolean askConfirmation(String question) {
        System.out.print(question + " (o/n) : ");
        String response = scanner.nextLine().trim().toLowerCase();
        return response.equals("o") || response.equals("oui") || response.equals("y") || response.equals("yes");
    }
    
    /**
     * Ferme le scanner.
     */
    public void close() {
        scanner.close();
    }
}