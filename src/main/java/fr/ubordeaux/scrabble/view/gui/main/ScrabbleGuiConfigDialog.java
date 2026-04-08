package fr.ubordeaux.scrabble.view.gui.main;

import fr.ubordeaux.scrabble.controller.GameController;
import fr.ubordeaux.scrabble.i18n.I18n;
import fr.ubordeaux.scrabble.model.core.Game;
import java.util.Optional;
import java.util.function.Consumer;
import javafx.geometry.Insets;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Dialog;
import javafx.scene.control.DialogPane;
import javafx.scene.control.Label;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.layout.GridPane;

/**
 * Handles GUI configuration dialog and related UI updates.
 */
public final class ScrabbleGuiConfigDialog {

  /**
   * Opens the configuration dialog.
   *
   * @param controller game controller
   * @param gameInstance game instance
   * @param applyAssignments callback that applies the assignment string
   * @param recreateWithoutConfirmation callback to recreate game without confirmation
   */
  public void showDialog(GameController controller,
      Game gameInstance,
      Consumer<String> applyAssignments,
      Runnable recreateWithoutConfirmation) {
    Dialog<ButtonType> dialog = new Dialog<>();
    dialog.setTitle(I18n.translate("scrabble.config.dialog.title"));
    dialog.setHeaderText(I18n.translate("scrabble.config.dialogHeader"));

    DialogPane dialogPane = dialog.getDialogPane();
    ButtonType applyRestartButton = new ButtonType(
        I18n.translate("scrabble.config.dialog.applyRestart"),
        javafx.scene.control.ButtonBar.ButtonData.LEFT);
    dialogPane.getButtonTypes().addAll(ButtonType.OK, applyRestartButton, ButtonType.CANCEL);

    ChoiceBox<String> languageChoice = new ChoiceBox<>();
    languageChoice.getItems().addAll("en", "fr");
    languageChoice.setValue(controller.configuredLanguage());

    final Spinner<Integer> playerSpinner = new Spinner<>(
        new SpinnerValueFactory.IntegerSpinnerValueFactory(2, 8,
            Math.max(2, controller.configuredPlayerCount() > 0
                ? controller.configuredPlayerCount()
                : gameInstance.getPlayers().size())));

    CheckBox superScrabbleBox = new CheckBox();
    superScrabbleBox.setSelected(controller.configuredSuperMode());

    CheckBox blitzBox = new CheckBox();
    blitzBox.setSelected(controller.configuredBlitzMode());

    Spinner<Integer> blitzTimeoutSpinner = new Spinner<>(
        new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 120,
            controller.configuredBlitzMinutes()));
    blitzTimeoutSpinner.setEditable(true);
    blitzTimeoutSpinner.disableProperty().bind(blitzBox.selectedProperty().not());

    Spinner<Integer> aiTimeSpinner = new Spinner<>(
        new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 60,
            controller.configuredAiTime()));
    aiTimeSpinner.setEditable(true);

    CheckBox expectiminimaxBox = new CheckBox();
    expectiminimaxBox.setSelected(controller.isExpectiminimaxEnabled());

    CheckBox mlBox = new CheckBox();
    mlBox.setSelected(controller.isMlEnabled());

    CheckBox debugBox = new CheckBox();
    debugBox.setSelected(fr.ubordeaux.scrabble.model.utils.GameLogger.isDebug());

    CheckBox verboseBox = new CheckBox();
    verboseBox.setSelected(fr.ubordeaux.scrabble.model.utils.GameLogger.isVerbose());

    javafx.scene.control.TextField dictionaryField =
        new javafx.scene.control.TextField(controller.getDictionaryPathOverride());
    dictionaryField.setPrefColumnCount(28);

    GridPane grid = new GridPane();
    grid.setHgap(12);
    grid.setVgap(10);
    grid.setPadding(new Insets(12, 0, 0, 0));

    int row = 0;
    grid.addRow(row++,
        new Label(I18n.translate("scrabble.config.label.language")),
        languageChoice);
    grid.addRow(row++,
        new Label(I18n.translate("scrabble.config.label.players")),
        playerSpinner);
    grid.addRow(row++,
        new Label(I18n.translate("scrabble.config.label.super")),
        superScrabbleBox);
    grid.addRow(row++, new Label(I18n.translate("scrabble.config.label.blitz")), blitzBox);
    grid.addRow(row++,
        new Label(I18n.translate("scrabble.config.label.timeout")),
        blitzTimeoutSpinner);
    grid.addRow(row++, new Label(I18n.translate("scrabble.config.label.aitime")), aiTimeSpinner);
    grid.addRow(row++,
        new Label(I18n.translate("scrabble.config.label.expectiminimax")),
        expectiminimaxBox);
    grid.addRow(row++, new Label(I18n.translate("scrabble.config.label.ml")), mlBox);
    grid.addRow(row++,
        new Label(I18n.translate("scrabble.config.label.dictionary")),
        dictionaryField);
    grid.addRow(row++, new Label(I18n.translate("scrabble.config.label.debug")), debugBox);
    grid.addRow(row, new Label(I18n.translate("scrabble.config.label.verbose")), verboseBox);

    dialogPane.setContent(grid);

    Optional<ButtonType> result = dialog.showAndWait();
    if (result.isEmpty() || (result.get() != ButtonType.OK && result.get() != applyRestartButton)) {
      return;
    }

    applyAssignments.accept(String.join("; ",
        "language=" + languageChoice.getValue(),
        "players=" + playerSpinner.getValue(),
        "super-scrabble=" + superScrabbleBox.isSelected(),
        "blitz=" + blitzBox.isSelected(),
        "timeout=" + blitzTimeoutSpinner.getValue(),
        "ai-time=" + aiTimeSpinner.getValue(),
        "ai-exptiminimax=" + expectiminimaxBox.isSelected(),
        "ai-ml=" + mlBox.isSelected(),
        "dictionary=" + dictionaryField.getText().trim(),
        "debug=" + debugBox.isSelected(),
        "verbose=" + verboseBox.isSelected()));

    if (result.get() == applyRestartButton) {
      recreateWithoutConfirmation.run();
    }
  }

}
