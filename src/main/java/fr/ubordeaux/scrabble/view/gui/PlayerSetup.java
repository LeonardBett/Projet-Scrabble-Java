package fr.ubordeaux.scrabble.view.gui;

import fr.ubordeaux.scrabble.i18n.I18n;
import java.util.Optional;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.Spinner;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

/**
 * Dialog to configure the number of players before starting a new game.
 *
 * <p>Returns the chosen player count as an Integer. MVC: pure view, no knowledge of Game.
 */
public class PlayerSetup extends Dialog<Integer> {

  private static final int MIN_PLAYERS = 2;
  private static final int MAX_PLAYERS = 4;

  private final Spinner<Integer> playerCountSpinner =
      new Spinner<>(MIN_PLAYERS, MAX_PLAYERS, MIN_PLAYERS);

  /** Creates the player-count dialog. */
  public PlayerSetup() {
    setTitle(I18n.tr("gui.setup.title"));
    setHeaderText(null);
    setResizable(false);

    playerCountSpinner.setEditable(false);
    playerCountSpinner.setPrefWidth(80);

    Label spinnerLabel = new Label(I18n.tr("gui.setup.playersLabel"));
    spinnerLabel.setFont(Font.font("Arial", FontWeight.BOLD, 13));

    HBox spinnerRow = new HBox(12, spinnerLabel, playerCountSpinner);
    spinnerRow.setAlignment(Pos.CENTER_LEFT);

    Label title = new Label(I18n.tr("gui.setup.newGameTitle"));
    title.setFont(Font.font("Arial", FontWeight.BOLD, 20));
    title.setTextFill(Color.web("#115829"));

    VBox content = new VBox(18, title, spinnerRow);
    content.setPadding(new Insets(20));
    content.setPrefWidth(320);

    getDialogPane().setContent(content);
    getDialogPane().getStyleClass().add("setup-dialog");

    ButtonType startType = new ButtonType(I18n.tr("gui.setup.start"), ButtonBar.ButtonData.OK_DONE);
    ButtonType cancelType =
        new ButtonType(I18n.tr("gui.setup.cancel"), ButtonBar.ButtonData.CANCEL_CLOSE);
    getDialogPane().getButtonTypes().addAll(startType, cancelType);

    setResultConverter(buttonType -> {
      if (buttonType == startType) {
        return playerCountSpinner.getValue();
      }
      return null;
    });
  }

  /**
   * Shows the dialog and returns the chosen number of players, or empty if cancelled.
   *
   * @return an Optional containing the player count, or empty if cancelled
   */
  public static Optional<Integer> showDialog() {
    PlayerSetup dialog = new PlayerSetup();
    return dialog.showAndWait();
  }
}