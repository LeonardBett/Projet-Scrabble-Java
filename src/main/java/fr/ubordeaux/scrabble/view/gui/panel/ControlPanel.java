package fr.ubordeaux.scrabble.view.gui.panel;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

/**
 * JavaFX panel containing all game action buttons (play, pass, exchange, undo, etc.).
 */
public class ControlPanel extends VBox {

  private final Button playButton;
  private final Button passButton;
  private final Button exchangeButton;
  private final Button cancelPlacementButton;
  private final Button undoButton;
  private final Button redoButton;
  private final Button hintButton;

  /**
   * Creates the ControlPanel and initializes all buttons.
   */
  public ControlPanel() {
    playButton = createButton("▶  Jouer", "#4CAF50");
    passButton = createButton("⏭  Passer", "#FF9800");
    exchangeButton = createButton("🔄 Échanger", "#2196F3");
    cancelPlacementButton = createButton("↩  Annuler placement", "#795548");
    undoButton = createButton("↶  Annuler coup", "#9E9E9E");
    redoButton = createButton("↷  Refaire coup", "#9E9E9E");
    hintButton = createButton("❓ Help", "#1E88E5");
    hintButton.setPrefWidth(96);
    initializeUi();
  }

  private void initializeUi() {
    this.setAlignment(Pos.TOP_CENTER);
    this.setSpacing(8);
    this.setPadding(new Insets(15, 10, 10, 10));
    this.setStyle("-fx-background-color: rgba(0,0,0,0.4); -fx-background-radius: 10;");
    this.setPrefWidth(250);

    Label title = new Label("ACTIONS");
    title.setFont(Font.font("Arial", FontWeight.BOLD, 16));
    title.setTextFill(Color.WHITE);

    HBox titleBar = new HBox(8);
    titleBar.setAlignment(Pos.CENTER_LEFT);
    Region spacer = new Region();
    HBox.setHgrow(spacer, Priority.ALWAYS);
    titleBar.getChildren().addAll(title, spacer, hintButton);
    titleBar.setPadding(new Insets(0, 0, 8, 0));

    this.getChildren().addAll(titleBar, playButton, passButton, exchangeButton,
        cancelPlacementButton, separator(), undoButton, redoButton);
  }

  private Button createButton(String text, String color) {
    Button btn = new Button(text);
    btn.setPrefWidth(220);
    btn.setPrefHeight(38);
    btn.setFont(Font.font("Arial", FontWeight.BOLD, 13));
    btn.setStyle("-fx-background-color: " + color + ";" + "-fx-text-fill: white;"
        + "-fx-background-radius: 5;" + "-fx-cursor: hand;");
    btn.setOnMouseEntered(e -> btn.setOpacity(0.8));
    btn.setOnMouseExited(e -> btn.setOpacity(1.0));
    return btn;
  }

  private Label separator() {
    Label sep = new Label("─────────────────────");
    sep.setTextFill(Color.GRAY);
    return sep;
  }

  /**
   * Returns the play button.
   *
   * @return the play button.
   */
  public Button getPlayButton() {
    return playButton;
  }

  /**
   * Returns the pass button.
   *
   * @return the pass button.
   */
  public Button getPassButton() {
    return passButton;
  }

  /**
   * Returns the exchange button.
   *
   * @return the exchange button.
   */
  public Button getExchangeButton() {
    return exchangeButton;
  }

  /**
   * Returns the cancel placement button.
   *
   * @return the cancel placement button.
   */
  public Button getCancelPlacementButton() {
    return cancelPlacementButton;
  }

  /**
   * Returns the undo button.
   *
   * @return the undo button.
   */
  public Button getUndoButton() {
    return undoButton;
  }

  /**
   * Returns the redo button.
   *
   * @return the redo button.
   */
  public Button getRedoButton() {
    return redoButton;
  }

  /**
   * Returns the hint button.
   *
   * @return the hint button.
   */
  public Button getHintButton() {
    return hintButton;
  }

  /**
   * Backward-compatible alias for the hint button.
   *
   * @return the hint/help button.
   */
  public Button getHelpButton() {
    return hintButton;
  }

  /**
   * Enables or disables only gameplay-related controls.
   *
   * @param disabled true to disable gameplay controls
   */
  public void setGameplayButtonsDisabled(boolean disabled) {
    playButton.setDisable(disabled);
    passButton.setDisable(disabled);
    exchangeButton.setDisable(disabled);
    cancelPlacementButton.setDisable(disabled);
    undoButton.setDisable(disabled);
    redoButton.setDisable(disabled);
  }
}
