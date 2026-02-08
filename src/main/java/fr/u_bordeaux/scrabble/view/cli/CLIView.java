package fr.u_bordeaux.scrabble.view.cli;

import fr.u_bordeaux.scrabble.model.core.Game;
import fr.u_bordeaux.scrabble.model.interfaces.Player;
import fr.u_bordeaux.scrabble.view.UserInterface;

/**
 * Vue principale en ligne de commande.
 */
public class CLIView implements UserInterface {
    
    private final Game game;
    

    private final BoardRenderer boardRenderer;
    private final PlayerRenderer playerRenderer;
    private final RackRenderer rackRenderer;
    // private final MessageRenderer messageRenderer;

    
    public CLIView(Game game) {
        this.game = game;
        this.boardRenderer = new BoardRenderer();
        this.playerRenderer = new PlayerRenderer();
        this.rackRenderer = new RackRenderer();
        // this.messageRenderer = new MessageRenderer();

    }
    

    public void displayGameState(boolean showBonusSquares) {
        // messageRenderer.separator();
        boardRenderer.render(game.getBoard(), showBonusSquares);
        playerRenderer.renderPlayerList(game.getPlayers());

    }
    

    public void displayCurrentPlayer() {
         Player current = game.getCurrentPlayer();
         playerRenderer.renderCurrentPlayer(current);
          rackRenderer.render(current);
     }
    

    

}