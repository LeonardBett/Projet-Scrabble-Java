package fr.u_bordeaux.scrabble.view.gui;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.Spinner;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

/**
 * Dialog to configure players before the game starts.
 * Equivalent to CLIInputHandler.askNumberOfPlayers() / askPlayerName().
 *
 * ✅ MVC: Pure view — returns a list of names, knows nothing about Game or controller.
 */
public class PlayerSetup extends Dialog<List<String>> {

    private static final int MIN_PLAYERS = 2;
    private static final int MAX_PLAYERS = 4;

    // Name fields, one per possible player
    private final List<TextField> nameFields = new ArrayList<>();
    private final VBox nameFieldsBox = new VBox(10);
    private final Spinner<Integer> playerCountSpinner = new Spinner<>(MIN_PLAYERS, MAX_PLAYERS, 2);

    public PlayerSetup() {
        setTitle("Scrabble U-Bordeaux");
        setHeaderText(null);
        setResizable(false);

        // ── Spinner : number of players ──────────────────────────────────────
        playerCountSpinner.setEditable(false);
        playerCountSpinner.setPrefWidth(80);
        playerCountSpinner.valueProperty().addListener((obs, oldVal, newVal) -> rebuildNameFields(newVal));

        Label spinnerLabel = new Label("Nombre de joueurs :");
        spinnerLabel.setFont(Font.font("Arial", FontWeight.BOLD, 13));

        HBox spinnerRow = new HBox(12, spinnerLabel, playerCountSpinner);
        spinnerRow.setAlignment(Pos.CENTER_LEFT);

        // ── Name fields ───────────────────────────────────────────────────────
        nameFieldsBox.setAlignment(Pos.CENTER_LEFT);
        rebuildNameFields(2);   // start with 2 players

        // ── Title label ───────────────────────────────────────────────────────
        Label title = new Label("🎮  Nouvelle Partie");
        title.setFont(Font.font("Arial", FontWeight.BOLD, 20));
        title.setTextFill(Color.web("#115829"));

        // ── Layout ────────────────────────────────────────────────────────────
        VBox content = new VBox(18, title, spinnerRow, nameFieldsBox);
        content.setPadding(new Insets(20));
        content.setPrefWidth(380);

        getDialogPane().setContent(content);
        getDialogPane().getStyleClass().add("setup-dialog");

        // ── Buttons ───────────────────────────────────────────────────────────
        ButtonType startType = new ButtonType("Commencer !", ButtonBar.ButtonData.OK_DONE);
        ButtonType quitType  = new ButtonType("Quitter",     ButtonBar.ButtonData.CANCEL_CLOSE);
        getDialogPane().getButtonTypes().addAll(startType, quitType);

        // ── Result converter ──────────────────────────────────────────────────
        setResultConverter(buttonType -> {
            if (buttonType == startType) {
                return collectNames();
            }
            return null;  // null → user cancelled
        });

        // Validate before closing: all names must be non-empty
        final Button startButton = (Button) getDialogPane().lookupButton(startType);
        startButton.addEventFilter(javafx.event.ActionEvent.ACTION, event -> {
            List<String> names = collectNames();
            if (names.contains("")) {
                showValidationError("Veuillez entrer un nom pour chaque joueur.");
                event.consume(); // prevent dialog from closing
            }
        });
    }

    // ─── Helpers ──────────────────────────────────────────────────────────────

    /**
     * Rebuilds the name input fields when the player count changes.
     */
    private void rebuildNameFields(int count) {
        nameFields.clear();
        nameFieldsBox.getChildren().clear();

        for (int i = 1; i <= count; i++) {
            Label label = new Label("Nom du joueur " + i + " :");
            label.setFont(Font.font("Arial", FontWeight.NORMAL, 12));

            TextField field = new TextField();
            field.setPromptText("Joueur " + i);
            field.setPrefWidth(280);

            nameFields.add(field);
            nameFieldsBox.getChildren().addAll(label, field);
        }
    }

    /**
     * Collects the names typed in the fields (trimmed).
     */
    private List<String> collectNames() {
        List<String> names = new ArrayList<>();
        for (TextField f : nameFields) {
            names.add(f.getText().trim());
        }
        return names;
    }

    private void showValidationError(String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("Champs manquants");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    // ─── Static factory ───────────────────────────────────────────────────────

    /**
     * Shows the dialog and returns the list of player names,
     * or empty Optional if the user cancelled.
     */
 
    public static Optional<List<String>> showDialog() {
        PlayerSetup dialog = new PlayerSetup();
        return dialog.showAndWait();
    }
}