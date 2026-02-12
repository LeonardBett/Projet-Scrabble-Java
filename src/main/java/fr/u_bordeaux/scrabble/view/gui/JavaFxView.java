package fr.u_bordeaux.scrabble.view.gui;

import fr.u_bordeaux.scrabble.view.UserInterface;

/**
 * Interface graphique (GUI) utilisant JavaFX.
 */
public class JavaFxView implements UserInterface {
     private ScrabbleGUI gui;

    public JavaFxView() {
        this.gui = new ScrabbleGUI();
    }

    @Override
    public void refresh() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void displayMessage(String message) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void displayError(String error) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void displaySuccess(String message) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    
}
