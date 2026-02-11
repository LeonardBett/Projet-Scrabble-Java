package fr.u_bordeaux.scrabble.view;

/**
 * Interface commune pour les vues (CLI et GUI).
 */
public interface UserInterface {
    /**
     * Rafraîchit l'affichage complet du jeu.
     */
    void refresh();
    
    /**
     * Affiche un message d'information.
     */
    void displayMessage(String message);
    
    /**
     * Affiche un message d'erreur.
     */
    void displayError(String error);
    
    /**
     * Affiche un message de succès.
     */
    void displaySuccess(String message);
}