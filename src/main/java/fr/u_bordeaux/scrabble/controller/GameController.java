package fr.u_bordeaux.scrabble.controller;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;

import fr.u_bordeaux.scrabble.model.ai.AIPlayer;
import fr.u_bordeaux.scrabble.model.core.Game;
import fr.u_bordeaux.scrabble.model.core.HumanPlayer;
import fr.u_bordeaux.scrabble.model.core.Move;
import fr.u_bordeaux.scrabble.model.dictionary.GADDAG;
import fr.u_bordeaux.scrabble.model.interfaces.Player;
import fr.u_bordeaux.scrabble.view.UserInterface;
import fr.u_bordeaux.scrabble.view.cli.CLIInputHandler;
import fr.u_bordeaux.scrabble.view.cli.CLIView;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import fr.u_bordeaux.scrabble.model.dictionary.GADDAG;
import fr.u_bordeaux.scrabble.model.ai.AIPlayer;
import fr.u_bordeaux.scrabble.model.core.HumanPlayer;


/**
 * Main controller (application logic).
 * Handles user input, updates the model and the view.
 * 
 * Responsibilities:
 * - Orchestrate communication between the view and the model
 * - Manage application logic (turns, validations)
 * - Notify the view of model changes
 */
public class GameController {
    private Game game;
    private UserInterface view;
    
    public GameController(Game game, UserInterface view) {
        this.game = game;
        this.view = view;
    }
    
    /**
     * Starts the game.
     */
    public void startGame() {
        if (game == null || view == null) {
            throw new IllegalStateException("Game and view must be initialized before starting.");
        }
        
        // Validate that at least 2 players are present
        if (game.getPlayers().size() < 2) {
            throw new IllegalStateException("At least 2 players must be present to start.");
        }
        
        // Initialize the game
        game.startGame();
    
    }

    /**
     * Runs a CLI game loop if the provided view is a CLIView.
     * This will prompt for players (if missing), start the game and process
     * player actions until the game ends or the user quits.
     */
/**
     * Runs a CLI game loop if the provided view is a CLIView.
     * This will prompt for players (if missing), start the game and process
     * player actions until the game ends or the user quits.
     */
    public void runCli() {
        if (!(view instanceof CLIView)) {
            throw new IllegalStateException("CLI loop requires a CLIView instance as view.");
        }

        CLIInputHandler input = new CLIInputHandler();
        CLIView cliView = (CLIView) view;

        cliView.displayWelcome();

        // 1. Initialisation des joueurs (Détection de l'IA)
        if (game.getPlayers().size() < 2) {
            int num = input.askNumberOfPlayers();
            for (int i = 1; i <= num; i++) {
                String name = input.askPlayerName(i);
                
                // Si le nom commence par "IA", on crée un bot (niveau 1 de profondeur par défaut)
                if (name.toUpperCase().startsWith("IA") || name.toUpperCase().startsWith("AI")) {
                    AIPlayer bot = new AIPlayer(name, 1); 
                    
                    // On demande si on veut activer l'Expectiminimax
                    if (input.askConfirmation("Activer le mode Expectiminimax (avancé) pour " + name + " ? (o/n)")) {
                        bot.setExpectiminimaxMode(true);
                    }
                    addPlayer(bot);
                } else {
                    addPlayer(new HumanPlayer(name));
                }
            }
        }

        startGame();

        // 2. Chargement du dictionnaire GADDAG depuis le fichier texte
        GADDAG gaddag = new GADDAG();
        System.out.println("\nChargement du dictionnaire GADDAG en cours (cela peut prendre quelques secondes)...");
        try {
            // Lecture sécurisée depuis le dossier resources (fonctionne même dans un .jar compilé)
            InputStream is = getClass().getClassLoader().getResourceAsStream("dictionaries/lexicon_en.txt");
            
            if (is == null) {
                System.err.println("ERREUR : Fichier lexicon_en.txt introuvable dans src/main/resources/dictionaries/");
            } else {
                BufferedReader br = new BufferedReader(new InputStreamReader(is));
                String line;
                int wordCount = 0;
                
                while ((line = br.readLine()) != null) {
                    String cleanWord = line.trim();
                    if (!cleanWord.isEmpty()) {
                        gaddag.add(cleanWord);
                        wordCount++;
                    }
                }
                br.close();
                System.out.println("Dictionnaire chargé avec succès ! (" + wordCount + " mots ajoutés).\n");
                System.out.println("PICKETE est dans le GADDAG ? " + gaddag.containsWord("PICKETE"));
                System.out.println("BICKERE est dans le GADDAG ? " + gaddag.containsWord("BICKERE"));
            }
        } catch (Exception e) {
            System.err.println("Erreur lors de la lecture du dictionnaire : " + e.getMessage());
            e.printStackTrace();
        }
        // 3. Boucle principale du jeu
        // 2. Chargement du dictionnaire GADDAG depuis le fichier texte
        GADDAG gaddag = new GADDAG();
        System.out.println("\nChargement du dictionnaire GADDAG en cours (cela peut prendre quelques secondes)...");
        try {
            // Lecture sécurisée depuis le dossier resources (fonctionne même dans un .jar compilé)
            InputStream is = getClass().getClassLoader().getResourceAsStream("dictionaries/lexicon_en.txt");
            
            if (is == null) {
                System.err.println("ERREUR : Fichier lexicon_en.txt introuvable dans src/main/resources/dictionaries/");
            } else {
                BufferedReader br = new BufferedReader(new InputStreamReader(is));
                String line;
                int wordCount = 0;
                
                while ((line = br.readLine()) != null) {
                    String cleanWord = line.trim();
                    if (!cleanWord.isEmpty()) {
                        gaddag.add(cleanWord);
                        wordCount++;
                    }
                }
                br.close();
                System.out.println("Dictionnaire chargé avec succès ! (" + wordCount + " mots ajoutés).\n");
                System.out.println("PICKETE est dans le GADDAG ? " + gaddag.containsWord("PICKETE"));
                System.out.println("BICKERE est dans le GADDAG ? " + gaddag.containsWord("BICKERE"));
            }
        } catch (Exception e) {
            System.err.println("Erreur lors de la lecture du dictionnaire : " + e.getMessage());
            e.printStackTrace();
        }
        // 3. Boucle principale du jeu
        boolean running = true;
        while (running && !game.isGameOver()) {
            view.refresh();
            Player current = game.getCurrentPlayer();

            // --- GESTION DU TOUR DE L'IA ---
            if (current instanceof AIPlayer) {
                view.displayMessage("\n--- C'est au tour de l'IA (" + current.getName() + ") ---");
                AIPlayer ai = (AIPlayer) current;
                
                try {
                    // L'IA calcule et joue son coup
                    ai.playTurn(game, gaddag);
                    
                    // Petite pause de 2 secondes pour que l'humain ait le temps de lire l'action de l'IA
                    Thread.sleep(2000); 
                } catch (Exception e) {
                    view.displayError("Erreur pendant le tour de l'IA : " + e.getMessage());
                    e.printStackTrace();
                    // En cas de crash inattendu, on force l'IA à passer pour ne pas bloquer le jeu
                    handlePlayerMove(Move.createPass(current));
                }
                
                continue; // On repart au début de la boucle sans demander d'input humain
            }

            // --- GESTION DU TOUR D'UN JOUEUR HUMAIN ---
            String action = input.askAction();
            switch (action) {
                case "1": // Play a word
                {
                    Move move = input.askPlayMove(current);
                    if (move != null) {
                        try {
                            handlePlayerMove(move);
                            view.displaySuccess("Coup joué.");
                        } catch (RuntimeException e) {
                            view.displayError(e.getMessage());
                        }
                    }
                    break;
                }
                case "2": // Exchange
                {
                    Move move = input.askExchangeMove(current);
                    if (move != null) {
                        try {
                            handlePlayerMove(move);
                            view.displaySuccess("Lettres échangées.");
                        } catch (RuntimeException e) {
                            view.displayError(e.getMessage());
                        }
                    }
                    break;
                }
                case "3": // Pass
                {
                    try {
                        handlePlayerMove(Move.createPass(current));
                        view.displayMessage(current.getName() + " a passé son tour.");
                    } catch (RuntimeException e) {
                        view.displayError(e.getMessage());
                    }
                    break;
                }
                case "4": // Undo
                {
                    undo();
                    break;
                }
                case "5": // Redo
                {
                    redo();
                    break;
                }
                case "6": // Quit
                {
                    if (input.askConfirmation("Voulez-vous vraiment quitter ?")) {
                        running = false;
                    }
                    break;
                }
                default:
                    view.displayError("Choix invalide.");
            }
        }

        // Fin de la partie
        // Fin de la partie
        Player winner = game.determineWinner();
        if (winner != null) {
            view.displaySuccess("Partie terminée. Vainqueur: " + winner.getName());
        }

        input.close();
    }
    
    /**
     * Executes a player's move.
     * @param move The move to execute
     */
    public void handlePlayerMove(Move move) {
        try {
            if (move == null) {
                return;
            }
            
            // Execute the move in the model
            game.executeMove(move);
            
            // Notify the view
            view.refresh();

            
        } catch (IllegalArgumentException | IllegalStateException e) {
            throw new RuntimeException("Invalid move: " + e.getMessage(), e);
        }
    }
    
    /**
     * Adds a player to the game.
     * @param player The player to add
     */
    public void addPlayer(Player player) {
        game.addPlayer(player);
    }
    
    /**
     * Undoes the last move.
     */
    public void undo() {
        game.undo();
        view.refresh();
    }
    
    /**
     * Redoes the undone move.
     */
    public void redo() {
        game.redo();
        view.refresh();
    }
    
    /**
     * Gets the game.
     * @return The Game model
     */
    public Game getGame() {
        return game;
    }
    
    /**
     * Gets the view.
     * @return The user interface
     */
    public UserInterface getView() {
        return view;
    }
}
