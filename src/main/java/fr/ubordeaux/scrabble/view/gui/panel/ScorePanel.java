package fr.ubordeaux.scrabble.view.gui.panel;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

public class ScorePanel extends VBox {

  private final ListView<String> playerList;
  private final Label bagInfoLabel;
  private final Label currentPlayerLabel;

  public ScorePanel() {
    this.playerList = new ListView<>();
    this.bagInfoLabel = new Label("Lettres restantes : 102");
    this.currentPlayerLabel = new Label("Tour de : —");
    initializeUi();
  }

  private void initializeUi() {
    this.setAlignment(Pos.TOP_CENTER);
    this.setSpacing(10);
    this.setPadding(new Insets(10));
    this.setStyle("-fx-background-color: rgba(0,0,0,0.4); -fx-background-radius: 10;");
    this.setPrefWidth(250);

    Label title = new Label("SCORES");
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

    this.getChildren().addAll(title, playerList, bagInfoLabel, currentPlayerLabel);
  }

  public void updateScores(String[] playerNames, int[] scores) {
    playerList.getItems().clear();
    for (int i = 0; i < playerNames.length && i < scores.length; i++) {
      playerList.getItems().add(String.format("%-15s %4d pts", playerNames[i], scores[i]));
    }
  }

  public void updateBagInfo(int remainingTiles) {
    bagInfoLabel.setText("Lettres restantes : " + remainingTiles);
  }

  public void highlightCurrentPlayer(int playerIndex, String playerName) {
    if (playerIndex >= 0 && playerIndex < playerList.getItems().size()) {
      playerList.getSelectionModel().select(playerIndex);
    }
    currentPlayerLabel.setText("🎯 Tour de : " + playerName);
  }
}
