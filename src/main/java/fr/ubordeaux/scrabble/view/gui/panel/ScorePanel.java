package fr.ubordeaux.scrabble.view.gui.panel;

import fr.ubordeaux.scrabble.i18n.I18n;
import fr.ubordeaux.scrabble.model.interfaces.Player;
import java.util.List;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.util.Duration;

/**
 * Panel displaying player scores, bag info, current player and blitz timers.
 *
 * <p>When blitz mode is active, a JavaFX Timeline refreshes the timer display
 * every second for each player.
 */
public class ScorePanel extends VBox {

  private final ListView<String> playerList;
  private final Label bagInfoLabel;
  private final Label currentPlayerLabel;
  private final Label blitzLabel;

  /** Holds the last known player list for the live timer refresh. */
  private List<Player> livePlayers;

  /** Timeline that ticks every second to refresh blitz timers. */
  private Timeline blitzTimeline;

  /** Callback invoked when a player runs out of time (blitz mode). */
  private Runnable onTimeExpired;

  /** Creates the score panel. */
  public ScorePanel() {
    this.playerList = new ListView<>();
    this.bagInfoLabel = new Label(I18n.tr("gui.score.initialRemaining"));
    this.currentPlayerLabel = new Label(I18n.tr("gui.score.initialTurn"));
    this.blitzLabel = new Label();
    initializeUi();
  }

  private void initializeUi() {
    this.setAlignment(Pos.TOP_CENTER);
    this.setSpacing(10);
    this.setPadding(new Insets(10));
    this.setStyle("-fx-background-color: rgba(0,0,0,0.4); -fx-background-radius: 10;");
    this.setPrefWidth(250);

    Label title = new Label(I18n.tr("gui.score.scores"));
    title.setFont(Font.font("Arial", FontWeight.BOLD, 16));
    title.setTextFill(Color.WHITE);

    playerList.setPrefHeight(150);
    playerList.setStyle("-fx-font-size: 13px;");

    bagInfoLabel.setFont(Font.font("Arial", FontWeight.NORMAL, 12));
    bagInfoLabel.setTextFill(Color.LIGHTGRAY);
    bagInfoLabel.setPadding(new Insets(2, 0, 0, 0));

    currentPlayerLabel.setFont(Font.font("Arial", FontWeight.BOLD, 13));
    currentPlayerLabel.setTextFill(Color.LIGHTGREEN);
    currentPlayerLabel.setPadding(new Insets(4, 0, 0, 0));
    currentPlayerLabel.setWrapText(true);

    blitzLabel.setFont(Font.font("Arial", FontWeight.BOLD, 12));
    blitzLabel.setTextFill(Color.ORANGE);
    blitzLabel.setPadding(new Insets(2, 0, 0, 0));
    blitzLabel.setWrapText(true);
    blitzLabel.setVisible(false);

    this.getChildren().addAll(title, playerList, bagInfoLabel, currentPlayerLabel, blitzLabel);
  }

  /**
   * Updates the score list with the given player names and scores.
   *
   * @param playerNames the player display names
   * @param scores the corresponding scores
   */
  public void updateScores(String[] playerNames, int[] scores) {
    playerList.getItems().clear();
    for (int i = 0; i < playerNames.length && i < scores.length; i++) {
      playerList.getItems().add(String.format("%-15s %4d pts", playerNames[i], scores[i]));
    }
  }

  /**
   * Updates the bag remaining tile count.
   *
   * @param remainingTiles number of tiles left in the bag
   */
  public void updateBagInfo(int remainingTiles) {
    bagInfoLabel.setText(I18n.tr("gui.score.remainingTiles", remainingTiles));
  }

  /**
   * Highlights the current player in the list and updates the turn label.
   *
   * @param playerIndex the index of the current player in the list
   * @param playerName the name of the current player
   */
  public void highlightCurrentPlayer(int playerIndex, String playerName) {
    if (playerIndex >= 0 && playerIndex < playerList.getItems().size()) {
      playerList.getSelectionModel().select(playerIndex);
    }
    currentPlayerLabel.setText(I18n.tr("gui.score.turn", playerName));
  }

  /**
   * Starts the blitz timer display. Refreshes every second and shows remaining time
   * for each player. Calls {@code onTimeExpired} when any player reaches zero.
   *
   * @param players the list of players with blitz clocks enabled
   * @param timeExpiredCallback called on the JavaFX thread when a player's time runs out
   */
  public void startBlitzTimers(List<Player> players, Runnable timeExpiredCallback) {
    stopBlitzTimers();

    this.livePlayers = players;
    this.onTimeExpired = timeExpiredCallback;
    blitzLabel.setVisible(true);

    blitzTimeline = new Timeline(new KeyFrame(Duration.seconds(1), e -> refreshBlitzDisplay()));
    blitzTimeline.setCycleCount(Timeline.INDEFINITE);
    blitzTimeline.play();

    refreshBlitzDisplay();
  }

  /**
   * Stops the blitz timer display and hides the blitz label.
   */
  public void stopBlitzTimers() {
    if (blitzTimeline != null) {
      blitzTimeline.stop();
      blitzTimeline = null;
    }
    blitzLabel.setVisible(false);
    livePlayers = null;
  }

  private void refreshBlitzDisplay() {
    if (livePlayers == null || livePlayers.isEmpty()) {
      return;
    }

    StringBuilder sb = new StringBuilder(I18n.tr("gui.score.timeRemaining") + "\n");
    boolean anyExpired = false;

    for (Player p : livePlayers) {
      if (!p.isBlitzClockEnabled()) {
        continue;
      }
      String time = p.getRemainingTimeDisplay();
      sb.append(p.getName()).append(" : ").append(time).append("\n");
      if (p.isOutOfTime()) {
        anyExpired = true;
      }
    }

    blitzLabel.setText(sb.toString().trim());

    // Couleur rouge si un joueur est à moins de 60 secondes
    boolean urgent = livePlayers.stream()
        .filter(Player::isBlitzClockEnabled)
        .anyMatch(p -> p.getRemainingTimeMillis() < 60_000 && !p.isOutOfTime());
    blitzLabel.setTextFill(urgent ? Color.RED : Color.ORANGE);

    if (anyExpired) {
      stopBlitzTimers();
      if (onTimeExpired != null) {
        onTimeExpired.run();
      }
    }
  }
}