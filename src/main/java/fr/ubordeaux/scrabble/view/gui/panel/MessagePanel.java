package fr.ubordeaux.scrabble.view.gui.panel;

import java.util.Optional;
import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;

/**
 * Utility class for displaying JavaFX alert dialogs (info, error, warning, confirmation).
 */
public class MessagePanel {

  /**
   * Default constructor.
   */
  public MessagePanel() {
  }

  /**
   * Displays an information dialog.
   *
   * @param title   the dialog title
   * @param message the message to display
   */
  public void showInfo(String title, String message) {
    Alert alert = new Alert(Alert.AlertType.INFORMATION);
    alert.setTitle(title);
    alert.setHeaderText(null);
    alert.setContentText(message);
    showNonBlocking(alert);
  }

  /**
   * Displays an error dialog.
   *
   * @param message the error message to display
   */
  public void showError(String message) {
    Alert alert = new Alert(Alert.AlertType.ERROR);
    alert.setTitle("Erreur");
    alert.setHeaderText(null);
    alert.setContentText(message);
    showNonBlocking(alert);
  }

  /**
   * Displays a warning dialog.
   *
   * @param title   the dialog title
   * @param message the warning message to display
   */
  public void showWarning(String title, String message) {
    Alert alert = new Alert(Alert.AlertType.WARNING);
    alert.setTitle(title);
    alert.setHeaderText(null);
    alert.setContentText(message);
    showNonBlocking(alert);
  }

  /**
   * Displays a confirmation dialog and returns the user's choice.
   *
   * @param message the question to ask
   * @return true if the user clicked OK, false otherwise
   */
  public boolean showConfirmation(String message) {
    Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
    alert.setTitle("Confirmation");
    alert.setHeaderText(null);
    alert.setContentText(message);
    Optional<ButtonType> result = alert.showAndWait();
    return result.isPresent() && result.get() == ButtonType.OK;
  }

  private void showNonBlocking(Alert alert) {
    if (Platform.isFxApplicationThread()) {
      alert.show();
    } else {
      Platform.runLater(alert::show);
    }
  }
}
