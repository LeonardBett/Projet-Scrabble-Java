package fr.ubordeaux.scrabble.view.cli.renderer;

import fr.ubordeaux.scrabble.i18n.I18n;

/**
 * Responsible for rendering system messages in the CLI console
 * (errors, successes, info, warnings, separators).
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
    System.out.println(I18n.translate("cli.message.errorPrefix") + message);
  }


  /**
   * Displays a success message in green format.
   *
   * @param message The success message to display.
   */
  public void success(String message) {
    System.out.println(I18n.translate("cli.message.successPrefix") + message);
  }

  /**
   * Displays a welcome message.
   */
  public void welcome() {
    System.out.println(I18n.translate("cli.message.welcome"));
  }

  /**
   * Displays an informational message.
   *
   * @param message The information message to display.
   */
  public void info(String message) {
    System.out.println(I18n.translate("cli.message.infoPrefix") + message);
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
    System.out.println(I18n.translate("cli.message.warningPrefix") + message);
  }
}