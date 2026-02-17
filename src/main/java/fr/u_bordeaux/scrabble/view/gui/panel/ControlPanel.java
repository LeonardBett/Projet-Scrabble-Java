package fr.u_bordeaux.scrabble.view.gui.panel;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

/**
 * Panel containing action buttons.
 *
 * ✅ MVC: Pure display — buttons are exposed via getters so ScrabbleGUI
 * can wire them to the controller without ControlPanel knowing the controller.
 */
public class ControlPanel extends VBox {

    private final Button playButton;
    private final Button passButton;
    private final Button exchangeButton;
    private final Button undoButton;
    private final Button redoButton;
    private final Button newGameButton;
    private final Button saveButton;
    private final Button loadButton;
    private final Button quitButton;

    public ControlPanel() {
        playButton     = createButton("▶  Jouer",           "#4CAF50");
        passButton     = createButton("⏭  Passer",          "#FF9800");
        exchangeButton = createButton("🔄 Échanger",        "#2196F3");
        undoButton     = createButton("↶  Annuler",         "#9E9E9E");
        redoButton     = createButton("↷  Refaire",         "#9E9E9E");
        newGameButton  = createButton("🎮 Nouvelle partie", "#673AB7");
        saveButton     = createButton("💾 Sauvegarder",     "#00BCD4");
        loadButton     = createButton("📁 Charger",         "#00BCD4");
        quitButton     = createButton("❌ Quitter",         "#F44336");

        initializeUI();
    }

    private void initializeUI() {
        this.setAlignment(Pos.TOP_CENTER);
        this.setSpacing(8);
        this.setPadding(new Insets(15, 10, 10, 10));
        this.setStyle("-fx-background-color: rgba(0,0,0,0.4); -fx-background-radius: 10;");
        this.setPrefWidth(250);

        Label title = new Label("ACTIONS");
        title.setFont(Font.font("Arial", FontWeight.BOLD, 16));
        title.setTextFill(Color.WHITE);
        title.setPadding(new Insets(0, 0, 8, 0));

        Label sep1 = separator();
        Label sep2 = separator();

        this.getChildren().addAll(
            title,
            playButton, passButton, exchangeButton,
            sep1,
            undoButton, redoButton,
            sep2,
            newGameButton, saveButton, loadButton, quitButton
        );
    }

    private Button createButton(String text, String color) {
        Button btn = new Button(text);
        btn.setPrefWidth(220);
        btn.setPrefHeight(38);
        btn.setFont(Font.font("Arial", FontWeight.BOLD, 13));
        btn.setStyle(
            "-fx-background-color: " + color + ";" +
            "-fx-text-fill: white;" +
            "-fx-background-radius: 5;" +
            "-fx-cursor: hand;"
        );
        btn.setOnMouseEntered(e -> btn.setOpacity(0.8));
        btn.setOnMouseExited(e ->  btn.setOpacity(1.0));
        return btn;
    }

    private Label separator() {
        Label sep = new Label("─────────────────────");
        sep.setTextFill(Color.GRAY);
        return sep;
    }

    // ─── Getters (wired by ScrabbleGUI) ──────────────────────────────────────

    public Button getPlayButton()     { return playButton; }
    public Button getPassButton()     { return passButton; }
    public Button getExchangeButton() { return exchangeButton; }
    public Button getUndoButton()     { return undoButton; }
    public Button getRedoButton()     { return redoButton; }
    public Button getNewGameButton()  { return newGameButton; }
    public Button getSaveButton()     { return saveButton; }
    public Button getLoadButton()     { return loadButton; }
    public Button getQuitButton()     { return quitButton; }
}