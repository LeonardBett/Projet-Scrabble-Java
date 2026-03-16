package fr.ubordeaux.scrabble.view.gui;

import fr.ubordeaux.scrabble.model.core.Game;
import fr.ubordeaux.scrabble.view.UserInterface;
import javafx.application.Platform;

/**
 * JavaFX view implementing UserInterface.
 *
 * <p>
 * MVC:
 * <ul>
 * <li>Implements UserInterface (contract with the controller)</li>
 * <li>Delegates all display work to ScrabbleGUI (the JavaFX window)</li>
 * <li>The controller only knows UserInterface, never ScrabbleGUI directly</li>
 * </ul>
 */
public class JavaFxView implements UserInterface {

  private final Game game;
  private ScrabbleGUI gui;

  /**
   * Creates a JavaFxView bound to the given game model.
   *
   * @param game the game model to observe
   */
  public JavaFxView(Game game) {
    this.game = game;
  }

  /**
   * Called by ScrabbleGUI once the stage is ready.
   *
   * @param gui the ScrabbleGUI instance to delegate display to
   */
  public void setGUI(ScrabbleGUI gui) {
    this.gui = gui;
  }

  /**
   * Refreshes all GUI panels (board, rack, scores) and triggers AI check. Always runs on the JavaFX
   * thread.
   */
  @Override
  public void refresh() {
    runOnFxThread(() -> {
      if (gui != null)
        gui.refreshAll();
    });
  }

  /**
   * Displays an informational message in the GUI.
   *
   * @param message the message to display
   */
  @Override
  public void displayMessage(String message) {
    runOnFxThread(() -> {
      if (gui != null)
        gui.showInfo("Information", message);
    });
  }

  /**
   * Displays an error message in the GUI.
   *
   * @param error the error message to display
   */
  @Override
  public void displayError(String error) {
    runOnFxThread(() -> {
      if (gui != null)
        gui.showError(error);
    });
  }

  /**
   * Displays a success message in the GUI.
   *
   * @param message the success message to display
   */
  @Override
  public void displaySuccess(String message) {
    runOnFxThread(() -> {
      if (gui != null)
        gui.showInfo("Succès", message);
    });
  }

  /**
   * Returns the game model.
   *
   * @return the Game instance
   */
  public Game getGame() {
    return game;
  }

  /**
   * Ensures the runnable executes on the JavaFX Application Thread.
   *
   * @param r the runnable to execute
   */
  private void runOnFxThread(Runnable r) {
    if (Platform.isFxApplicationThread()) {
      r.run();
    } else {
      Platform.runLater(r);
    }
  }
}
