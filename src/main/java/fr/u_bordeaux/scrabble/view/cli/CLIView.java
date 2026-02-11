package fr.u_bordeaux.scrabble.view.cli;

import fr.u_bordeaux.scrabble.model.core.Game;
import fr.u_bordeaux.scrabble.model.interfaces.Player;
import fr.u_bordeaux.scrabble.view.UserInterface;

/**
 * Full CLI view implementing UserInterface.
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
        this.showBonusSquares = true;  // By default, bonus squares are shown
        this.boardRenderer = new BoardRenderer();
        this.playerRenderer = new PlayerRenderer();
        this.rackRenderer = new RackRenderer();
        this.messageRenderer = new MessageRenderer();
    
    }
    
    /**
     * 🎯 Implementation of UserInterface.refresh()
     * Refreshes the entire display.
     */
    @Override
    public void refresh() {
        displayGameState(showBonusSquares);
    }
    
    /**
     * Displays the complete state of the game.
     */
    public void displayGameState(boolean showBonusSquares) {
        messageRenderer.separator();
        boardRenderer.render(game.getBoard(), showBonusSquares);
        playerRenderer.renderPlayerList(game.getPlayers());
        displayCurrentPlayer();

    }
    
    /**
     * Displays the current player and their rack.
     */
    public void displayCurrentPlayer() {
        Player current = game.getCurrentPlayer();
        if (current != null) {
            playerRenderer.renderCurrentPlayer(current);
            rackRenderer.render(current);
        }
    }
    
    /**
     * 🎯 Implementation of UserInterface.displayMessage()
     */
    @Override
    public void displayMessage(String message) {
        messageRenderer.info(message);
    }
    
    /**
     * 🎯 Implementation of UserInterface.displayError()
     */
    @Override
    public void displayError(String error) {
        messageRenderer.error(error);
    }
    
    /**
     * 🎯 Implementation of UserInterface.displaySuccess()
     */
    @Override
    public void displaySuccess(String message) {
        messageRenderer.success(message);
    }
    
    /**
     * Displays the welcome message.
     */
    public void displayWelcome() {
        messageRenderer.welcome();
    }
    
    /**
     * Displays the color legend.
     */

    
    public void setBlitzMode(boolean isBlitzMode) {
        this.isBlitzMode = isBlitzMode;
    }
    
    public void setShowBonusSquares(boolean show) {
        this.showBonusSquares = show;
    }
    
    // Getters for direct access to renderers if needed
    public BoardRenderer getBoardRenderer() { return boardRenderer; }
    public PlayerRenderer getPlayerRenderer() { return playerRenderer; }
    public RackRenderer getRackRenderer() { return rackRenderer; }
    public MessageRenderer getMessageRenderer() { return messageRenderer; }

}