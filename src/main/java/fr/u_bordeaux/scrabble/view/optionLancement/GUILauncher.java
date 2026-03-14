package fr.u_bordeaux.scrabble.view.optionLancement;

import fr.u_bordeaux.scrabble.model.core.Game;
import fr.u_bordeaux.scrabble.model.core.HumanPlayer;
import fr.u_bordeaux.scrabble.view.gui.JavaFxView;
import fr.u_bordeaux.scrabble.view.gui.ScrabbleGUI;
import javafx.application.Application;

/**
 * Lance le jeu en mode GUI JavaFX.
 */
public class GUILauncher {

    private GUILauncher() {}

    /**
     * Starts the game in GUI mode with the given number of players.
     *
     * @param args    the command-line arguments passed to JavaFX
     * @param players the number of players (between 2 and 4)
     */
    public static void launch(String[] args, int players) {
        Game game = new Game();
        for (int i = 1; i <= players; i++) {
            game.addPlayer(new HumanPlayer("Player" + i));
        }
        JavaFxView view = new JavaFxView(game);
        ScrabbleGUI.setGame(game);
        ScrabbleGUI.setView(view);
        Application.launch(ScrabbleGUI.class, args);
    }
}