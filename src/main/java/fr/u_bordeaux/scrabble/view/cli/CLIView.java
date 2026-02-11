package fr.u_bordeaux.scrabble.view.cli;

import fr.u_bordeaux.scrabble.model.core.Game;
import fr.u_bordeaux.scrabble.model.interfaces.Player;
import fr.u_bordeaux.scrabble.view.UserInterface;

/**
 * Vue CLI complète avec implémentation de UserInterface.
 */
public class CLIView implements UserInterface {
    
    private final Game game;
    private final BoardRenderer boardRenderer;
    private final PlayerRenderer playerRenderer;
    private final RackRenderer rackRenderer;
    private final MessageRenderer messageRenderer;

    
    private boolean isBlitzMode;
    private boolean showBonusSquares;
    
    public CLIView(Game game) {
        this(game, false);
    }
    
    public CLIView(Game game, boolean isBlitzMode) {
        this.game = game;
        this.isBlitzMode = isBlitzMode;
        this.showBonusSquares = true;  // Par défaut, on affiche les couleurs
        this.boardRenderer = new BoardRenderer();
        this.playerRenderer = new PlayerRenderer();
        this.rackRenderer = new RackRenderer();
        this.messageRenderer = new MessageRenderer();
    
    }
    
    /**
     * 🎯 Implémentation de UserInterface.refresh()
     * Rafraîchit tout l'affichage.
     */
    @Override
    public void refresh() {
        displayGameState(showBonusSquares);
    }
    
    /**
     * Affiche l'état complet du jeu.
     */
    public void displayGameState(boolean showBonusSquares) {
        messageRenderer.separator();
        boardRenderer.render(game.getBoard(), showBonusSquares);
        playerRenderer.renderPlayerList(game.getPlayers());
        displayCurrentPlayer();

    }
    
    /**
     * Affiche le joueur actuel et son chevalet.
     */
    public void displayCurrentPlayer() {
        Player current = game.getCurrentPlayer();
        if (current != null) {
            playerRenderer.renderCurrentPlayer(current);
            rackRenderer.render(current);
        }
    }
    
    /**
     * 🎯 Implémentation de UserInterface.displayMessage()
     */
    @Override
    public void displayMessage(String message) {
        messageRenderer.info(message);
    }
    
    /**
     * 🎯 Implémentation de UserInterface.displayError()
     */
    @Override
    public void displayError(String error) {
        messageRenderer.error(error);
    }
    
    /**
     * 🎯 Implémentation de UserInterface.displaySuccess()
     */
    @Override
    public void displaySuccess(String message) {
        messageRenderer.success(message);
    }
    
    /**
     * Affiche le message de bienvenue.
     */
    public void displayWelcome() {
        messageRenderer.welcome();
    }
    
    /**
     * Affiche la légende des couleurs.
     */

    
    public void setBlitzMode(boolean isBlitzMode) {
        this.isBlitzMode = isBlitzMode;
    }
    
    public void setShowBonusSquares(boolean show) {
        this.showBonusSquares = show;
    }
    
    // Getters pour accès direct aux renderers si nécessaire
    public BoardRenderer getBoardRenderer() { return boardRenderer; }
    public PlayerRenderer getPlayerRenderer() { return playerRenderer; }
    public RackRenderer getRackRenderer() { return rackRenderer; }
    public MessageRenderer getMessageRenderer() { return messageRenderer; }

}