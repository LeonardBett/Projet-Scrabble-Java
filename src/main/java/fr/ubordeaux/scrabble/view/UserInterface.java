package fr.ubordeaux.scrabble.view;

/** Common interface for views (CLI and GUI). */
public interface UserInterface {

  /** Refreshes the complete game display. */
  void refresh();

  /**
   * Displays an informational message.
   *
   * @param message the message to display
   */
  void displayMessage(String message);

  /**
   * Displays an error message.
   *
   * @param error the error message to display
   */
  void displayError(String error);

  /**
   * Displays a success message.
   *
   * @param message the success message to display
   */
  void displaySuccess(String message);
}
