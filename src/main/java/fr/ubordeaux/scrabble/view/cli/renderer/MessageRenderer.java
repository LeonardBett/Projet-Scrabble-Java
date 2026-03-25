package fr.ubordeaux.scrabble.view.cli.renderer;

/**
 * Responsable de l'affichage des messages système dans la console CLI
 * (erreurs, succès, informations, avertissements, séparateurs).
 */
public class MessageRenderer {

  public void error(String message) {
    System.out.println("Error : " + message);
  }

  public void success(String message) {
    System.out.println("OK : " + message);
  }

  public void welcome() {
    System.out.println("Welcome to the Scrabble CLI!");
  }

  public void info(String message) {
    System.out.println("Info : " + message);
  }

  public void separator() {
    System.out.println("----------------------------------------------");
  }

  public void sectionTitle(String title) {
    System.out.println("\n=== " + title + " ===");
  }

  public void warning(String message) {
    System.out.println("Warning : " + message);
  }
}