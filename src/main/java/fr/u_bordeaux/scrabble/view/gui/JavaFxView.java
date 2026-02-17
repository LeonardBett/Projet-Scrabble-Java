package fr.u_bordeaux.scrabble.view.gui;

import fr.u_bordeaux.scrabble.model.core.Game;
import fr.u_bordeaux.scrabble.view.UserInterface;
import javafx.application.Platform;


/**
 * JavaFX view implementing UserInterface.
 *
 * ✅ MVC:
 *  - Implements UserInterface (contract with the controller)
 *  - Delegates all display work to ScrabbleGUI (the JavaFX window)
 *  - The controller only knows UserInterface, never ScrabbleGUI directly
 */
public class JavaFxView implements UserInterface {

    private final Game game;
    private ScrabbleGUI gui;  // set once the JavaFX window is ready

    public JavaFxView(Game game) {
        this.game = game;
    }

    /**
     * Called by ScrabbleGUI once the stage is ready.
     */
    public void setGUI(ScrabbleGUI gui) {
        this.gui = gui;
    }

    // ─── UserInterface ────────────────────────────────────────────────────────

    /**
     * Refreshes all GUI panels (board, rack, scores).
     * Always runs on the JavaFX thread.
     */
    @Override
    public void refresh() {
        runOnFxThread(() -> {
            if (gui != null) {
                gui.refreshBoard();
                gui.refreshRack();
                gui.refreshScores();
            }
        });
    }

    @Override
    public void displayMessage(String message) {
        runOnFxThread(() -> {
            if (gui != null) gui.showInfo("Information", message);
        });
    }

    @Override
    public void displayError(String error) {
        runOnFxThread(() -> {
            if (gui != null) gui.showError(error);
        });
    }

    @Override
    public void displaySuccess(String message) {
        runOnFxThread(() -> {
            if (gui != null) gui.showInfo("✅ Succès", message);
        });
    }

    // ─── Helpers ──────────────────────────────────────────────────────────────

    public Game getGame() {
        return game;
    }

    /**
     * Ensures the runnable executes on the JavaFX Application Thread.
     */
    private void runOnFxThread(Runnable r) {
        if (Platform.isFxApplicationThread()) {
            r.run();
        } else {
            Platform.runLater(r);
        }
    }
}




    

    

