package fr.u_bordeaux.scrabble.view;

/**
 * Common interface for views (CLI and GUI).
 */
public interface UserInterface {
    /**
     * Refreshes the complete game display.
     */
    void refresh();
    
    /**
     * Displays an informational message.
     */
    void displayMessage(String message);
    
    /**
     * Displays an error message.
     */
    void displayError(String error);
    
    /**
     * Displays a success message.
     */
    void displaySuccess(String message);
}