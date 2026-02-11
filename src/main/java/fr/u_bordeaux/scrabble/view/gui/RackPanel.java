package fr.u_bordeaux.scrabble.view.gui;

import java.util.List;

import fr.u_bordeaux.scrabble.model.core.Rack;
import fr.u_bordeaux.scrabble.model.core.Tile;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

/**
 * Panel representing the player's rack.
 * Displays the tiles from the model's Rack.
 */
public class RackPanel extends VBox {
    
    private static final int MAX_TILES = Rack.MAX_SIZE; 
    private static final int TILE_SIZE = 60;
    
    private HBox tilesBox;
    private StackPane[] tileContainers;  
    private Rack rack;  
    

    public RackPanel(Rack rack) {
        this.rack = rack;
        this.tileContainers = new StackPane[MAX_TILES];
        initializeUI();
        updateDisplay();  
    }
    

    public RackPanel() {
        this(new Rack());
    }
    
    private void initializeUI() {

        Label title = new Label("CHEVALET DU JOUEUR");
        title.setFont(Font.font("Arial", FontWeight.BOLD, 14));
        title.setPadding(new Insets(0, 0, 10, 0));
        

        tilesBox = new HBox(10);
        tilesBox.setAlignment(Pos.CENTER);
        tilesBox.setPadding(new Insets(10));
        tilesBox.setStyle("-fx-background-color: #000000; -fx-background-radius: 10;");
        tilesBox.setMaxWidth(GridPane.USE_PREF_SIZE);
        tilesBox.setMaxHeight(GridPane.USE_PREF_SIZE);
        

        for (int i = 0; i < MAX_TILES; i++) {
            StackPane tileContainer = createTileContainer();
            tileContainers[i] = tileContainer;
            tilesBox.getChildren().add(tileContainer);
        }
        
        this.setAlignment(Pos.CENTER);
        this.getChildren().addAll(title, tilesBox);
    }
    

    private StackPane createTileContainer() {
        StackPane container = new StackPane();
        container.setPrefSize(TILE_SIZE, TILE_SIZE);
        container.setMaxSize(TILE_SIZE, TILE_SIZE);
        container.setMinSize(TILE_SIZE, TILE_SIZE);
        container.setStyle("-fx-background-color: #FFE4B5; " +
                          "-fx-border-color: #333333; " +
                          "-fx-border-width: 2; " +
                          "-fx-background-radius: 5; " +
                          "-fx-border-radius: 5;");
        

        container.setOnMouseEntered(e -> {
            container.setStyle(container.getStyle() + 
                "-fx-cursor: hand; " +
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.5), 10, 0, 0, 0);");
        });
        container.setOnMouseExited(e -> {
            container.setStyle(container.getStyle().replace(
                "-fx-cursor: hand; " +
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.5), 10, 0, 0, 0);", ""));
        });
        
        return container;
    }
    
    public void updateDisplay() {
        List<Tile> tiles = rack.getTiles();
        
        for (int i = 0; i < MAX_TILES; i++) {
            StackPane container = tileContainers[i];
            container.getChildren().clear();  
            
            if (i < tiles.size()) {
           
                Tile tile = tiles.get(i);
                char letter = tile.getCharacter();
                int value = tile.getValue();
                
         
                Label letterLabel = new Label(String.valueOf(letter));
                letterLabel.setFont(Font.font("Arial", FontWeight.BOLD, 28));
                letterLabel.setTextFill(Color.BLACK);
                StackPane.setAlignment(letterLabel, Pos.CENTER);
                
            
                Label valueLabel = new Label(String.valueOf(value));
                valueLabel.setFont(Font.font("Arial", FontWeight.NORMAL, 10));
                valueLabel.setTextFill(Color.DARKGRAY);
                StackPane.setAlignment(valueLabel, Pos.BOTTOM_RIGHT);
                StackPane.setMargin(valueLabel, new Insets(0, 5, 5, 0));
                
                container.getChildren().addAll(letterLabel, valueLabel);
            }
        
        }
    }
    

    public void setRack(Rack newRack) {
        this.rack = newRack;
        updateDisplay();
    }
    

    public Rack getRack() {
        return rack;
    }
}