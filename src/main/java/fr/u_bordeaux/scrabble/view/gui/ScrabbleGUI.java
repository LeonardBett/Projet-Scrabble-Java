package fr.u_bordeaux.scrabble.view.gui;

import fr.u_bordeaux.scrabble.model.core.Board;
import fr.u_bordeaux.scrabble.model.core.Rack;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage; 

public class ScrabbleGUI extends Application {


    @Override
    public void start(Stage primaryStage) {
        Board board = new Board();
        BorderPane root = new BorderPane();      
        Rack playerRack = new Rack();
        root.setPadding(new Insets(10));
        Scene scene = new Scene(root, 1200, 800);
        primaryStage.setTitle("Scrabble U-Bordeaux");
        BoardPanel boardPanel = new BoardPanel(board);
        RackPanel rackPanel = new RackPanel(playerRack);
        root.setCenter(boardPanel);
        root.setBottom(rackPanel);
        root.setStyle("-fx-background-color: #115829;");
        primaryStage.setScene(scene);
        primaryStage.setFullScreen(true); 
        primaryStage.show();

    }

    public static void main(String[] args) {
        launch(args);
    }
    
}