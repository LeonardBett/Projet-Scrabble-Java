package fr.u_bordeaux.scrabble.view.cli;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import fr.u_bordeaux.scrabble.model.core.Move;
import fr.u_bordeaux.scrabble.model.core.Tile;
import fr.u_bordeaux.scrabble.model.enums.Direction;
import fr.u_bordeaux.scrabble.model.interfaces.Player;
import fr.u_bordeaux.scrabble.model.utils.Point;
import fr.u_bordeaux.scrabble.view.cli.Renderer.MessageRenderer;

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
            System.out.print("\nPosition de départ (format: h 8 ou 8 8) : ");
            String[] posInput = scanner.nextLine().trim().split("\\s+");
            if (posInput.length < 2) {
                throw new IllegalArgumentException("Entrez 2 valeurs: ligne colonne.");
            }

            int x;
            int y;

            // Version simple: si on envoie une lettre (ex: h), on prend son index dans a..o.
            if (posInput[0].matches("[a-oA-O]")) {
                String[] rows = {"a", "b", "c", "d", "e", "f", "g", "h", "i", "j", "k", "l", "m", "n", "o"};
                String row = posInput[0].toLowerCase();

                int rowIndex = -1;
                for (int i = 0; i < rows.length; i++) {
                    if (rows[i].equals(row)) {
                        rowIndex = i;
                        break;
                    }
                }

                if (rowIndex == -1) {
                    throw new IllegalArgumentException("Ligne invalide: " + posInput[0]);
                }

                y = rowIndex;
                x = Integer.parseInt(posInput[1]) - 1;
            } else {
                // Compatibilite: ancien format numerique x y
                x = Integer.parseInt(posInput[0]) - 1;
                y = Integer.parseInt(posInput[1]) - 1;
            }

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
                // Find the letter in the rack. Use reference equality to allow identical letters
                boolean found = false;
                for (Tile tile : rack) {
                    if (tile.getCharacter() == letter) {
                        boolean alreadyUsed = false;
                        for (Tile t : tiles) {
                            if (t == tile) { // reference equality
                                alreadyUsed = true;
                                break;
                            }
                        }
                        if (!alreadyUsed) {
                            tiles.add(tile);
                            found = true;
                            break;
                        }
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
                    if (tile.getCharacter() == letter) {
                        boolean alreadyUsed = false;
                        for (Tile t : tiles) {
                            if (t == tile) {
                                alreadyUsed = true;
                                break;
                            }
                        }
                        if (!alreadyUsed) {
                            tiles.add(tile);
                            found = true;
                            break;
                        }
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