package fr.ubordeaux.scrabble.view.gui;

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
      new Spinner<>(minPlayers(), maxPlayers(), defaultPlayers());

  /** Creates the player-count dialog. */
  public PlayerSetup() {
    setTitle(dialogTitle());
    setHeaderText(null);
    setResizable(false);

    playerCountSpinner.setEditable(false);
    playerCountSpinner.setPrefWidth(spinnerPrefWidth());

    Label spinnerLabel = new Label(playersLabelText());
    spinnerLabel.setFont(Font.font("Arial", FontWeight.BOLD, 13));

    HBox spinnerRow = new HBox(spinnerRowSpacing(), spinnerLabel, playerCountSpinner);
    spinnerRow.setAlignment(Pos.CENTER_LEFT);

    Label title = new Label(dialogHeaderTitle());
    title.setFont(Font.font("Arial", FontWeight.BOLD, 20));
    title.setTextFill(Color.web(dialogHeaderColor()));

    VBox content = new VBox(contentSpacing(), title, spinnerRow);
    content.setPadding(new Insets(contentPadding()));
    content.setPrefWidth(contentPrefWidth());

    getDialogPane().setContent(content);
    getDialogPane().getStyleClass().add(dialogStyleClass());

    ButtonType startType = new ButtonType(startButtonText(), ButtonBar.ButtonData.OK_DONE);
    ButtonType cancelType = new ButtonType(cancelButtonText(), ButtonBar.ButtonData.CANCEL_CLOSE);
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

  static String dialogTitle() {
    return "Scrabble U-Bordeaux";
  }

  static String playersLabelText() {
    return "Nombre de joueurs :";
  }

  static String dialogHeaderTitle() {
    return "🎮  Nouvelle Partie";
  }

  static String startButtonText() {
    return "Commencer !";
  }

  static String cancelButtonText() {
    return "Annuler";
  }

  static int minPlayers() {
    return MIN_PLAYERS;
  }

  static int maxPlayers() {
    return MAX_PLAYERS;
  }

  static int defaultPlayers() {
    return MIN_PLAYERS;
  }

  static double spinnerPrefWidth() {
    return 80;
  }

  static double spinnerRowSpacing() {
    return 12;
  }

  static double contentSpacing() {
    return 18;
  }

  static double contentPadding() {
    return 20;
  }

  static double contentPrefWidth() {
    return 320;
  }

  static String dialogHeaderColor() {
    return "#115829";
  }

  static String dialogStyleClass() {
    return "setup-dialog";
  }
}