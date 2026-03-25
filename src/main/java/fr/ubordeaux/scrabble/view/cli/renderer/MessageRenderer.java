package fr.ubordeaux.scrabble.view.cli.renderer;

/**
 * Responsable de l'affichage des messages système dans la console CLI
 * (erreurs, succès, informations, avertissements, séparateurs).
 */
public class MessageRenderer {

  /**
   * Default constructor for MessageRenderer.
   */
  public MessageRenderer() {
  }

  /**
   * Displays an error message in red format.
   *
   * @param message The error message to display.
   */
  public void error(String message) {
    System.out.println("Error : " + message);
  }


  /**
   * Displays a success message in green format.
   *
   * @param message The success message to display.
   */
  public void success(String message) {
    System.out.println("OK : " + message);
  }

  /**
   * Displays a welcome message.
   */
  public void welcome() {
    System.out.println("Welcome to the Scrabble CLI!");
  }

  /**
   * Displays an informational message.
   *
   * @param message The information message to display.
   */
  public void info(String message) {
    System.out.println("Info : " + message);
  }

  /**
   * Displays a visual separator line.
   */
  public void separator() {
    System.out.println("----------------------------------------------");
  }

  /**
   * Displays a formatted section title.
   *
   * @param title The title text to display.
   */
  public void sectionTitle(String title) {
    System.out.println("\n=== " + title + " ===");
  }

  /**
   * Displays a warning message.
   *
   * @param message the warning message to display
   */
  public void warning(String message) {
    System.out.println("Warning : " + message);
  }
}