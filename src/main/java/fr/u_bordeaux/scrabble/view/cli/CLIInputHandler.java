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
 * Handles user input in the CLI.
 */
public class CLIInputHandler {
    
    private final Scanner scanner;
    private final MessageRenderer messageRenderer;
    
    public CLIInputHandler() {
        this.scanner = new Scanner(System.in);
        this.messageRenderer = new MessageRenderer();
    }
    
    /**
     * Asks the player which action they want to perform.
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
     * Asks the player to build a "play word" move.
     */
    public Move askPlayMove(Player player) {
        try {
            // 1. Ask for the starting position
            System.out.print("\nPosition de départ (format: x y, ex: 7 7) : ");
            String[] posInput = scanner.nextLine().trim().split("\\s+");
            int x = Integer.parseInt(posInput[0]) - 1;
            int y = Integer.parseInt(posInput[1]) - 1;
            Point startPoint = new Point(x, y);
            
            // 2. Ask for the direction
            System.out.print("Direction (H pour horizontal, V pour vertical) : ");
            String dirInput = scanner.nextLine().trim().toUpperCase();
            Direction direction = dirInput.equals("H") ? Direction.HORIZONTAL : Direction.VERTICAL;
            
            // 3. Ask for the letters to play
            System.out.print("Lettres à jouer (ex: HELLO) : ");
            String lettersInput = scanner.nextLine().trim().toUpperCase();
            
            // 4. Convert letters into Tiles
            List<Tile> tiles = new ArrayList<>();
            List<Tile> rack = player.getRack().getTiles();
            
            for (char letter : lettersInput.toCharArray()) {
                // Find the letter in the rack
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
     * Asks the player which letters to exchange.
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
     * Asks for the number of players.
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
     * Asks for a player's name.
     */
    public String askPlayerName(int playerNumber) {
        System.out.print("Nom du joueur " + playerNumber + " : ");
        return scanner.nextLine().trim();
    }
    
    /**
     * Asks for a confirmation (yes/no).
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