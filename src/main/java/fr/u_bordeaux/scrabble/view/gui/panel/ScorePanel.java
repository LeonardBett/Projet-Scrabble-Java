package fr.u_bordeaux.scrabble.view.gui.panel;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

/**
 * Panel displaying player scores and bag info.
 *
 * ✅ MVC: Pure display — receives data from ScrabbleGUI (which gets it from the model).
 */
public class ScorePanel extends VBox {

    private final ListView<String> playerList;
    private final Label bagInfoLabel;

    public ScorePanel() {
        this.playerList   = new ListView<>();
        this.bagInfoLabel = new Label("Lettres restantes : 102");
        initializeUI();
    }

    private void initializeUI() {
        this.setAlignment(Pos.TOP_CENTER);
        this.setSpacing(10);
        this.setPadding(new Insets(10));
        this.setStyle("-fx-background-color: rgba(0,0,0,0.4); -fx-background-radius: 10;");
        this.setPrefWidth(250);

        Label title = new Label("SCORES");
        title.setFont(Font.font("Arial", FontWeight.BOLD, 16));
        title.setTextFill(Color.WHITE);

        playerList.setPrefHeight(180);
        playerList.setStyle("-fx-font-size: 13px;");

        bagInfoLabel.setFont(Font.font("Arial", FontWeight.NORMAL, 12));
        bagInfoLabel.setTextFill(Color.LIGHTGRAY);
        bagInfoLabel.setPadding(new Insets(5, 0, 0, 0));

        this.getChildren().addAll(title, playerList, bagInfoLabel);
    }

    /**
     * Updates the player list with names and scores.
     */
    public void updateScores(String[] playerNames, int[] scores) {
        playerList.getItems().clear();
        for (int i = 0; i < playerNames.length && i < scores.length; i++) {
            playerList.getItems().add(String.format("%-15s %4d pts", playerNames[i], scores[i]));
        }
    }

    /**
     * Updates the remaining tiles count.
     */
    public void updateBagInfo(int remainingTiles) {
        bagInfoLabel.setText("Lettres restantes : " + remainingTiles);
    }

    /**
     * Highlights the current player row.
     */
    public void highlightCurrentPlayer(int playerIndex) {
        if (playerIndex >= 0 && playerIndex < playerList.getItems().size()) {
            playerList.getSelectionModel().select(playerIndex);
        }
    }
}