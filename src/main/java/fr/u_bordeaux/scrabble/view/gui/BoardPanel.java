package fr.u_bordeaux.scrabble.view.gui;

import fr.u_bordeaux.scrabble.model.core.Board;
import fr.u_bordeaux.scrabble.model.core.Square;
import fr.u_bordeaux.scrabble.model.enums.SquareType;
import fr.u_bordeaux.scrabble.model.utils.Point;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

/**
 * Panel representing the Scrabble board (15x15 grid).
 * Displays squares with their bonuses and the letters played.
 */
public class BoardPanel extends VBox {
    
    private static final int GRID_SIZE = Board.SIZE; 
    private static final int CELL_SIZE = 40;
    
    private GridPane gridPane;
    private Label[][] cellLabels;
    private Board board;

    

    public BoardPanel(Board board) {
        this.board = board;
        this.cellLabels = new Label[GRID_SIZE][GRID_SIZE];
        initializeUI();
    }
    

    public BoardPanel() {
        this(new Board());
    }
    
    private void initializeUI() {

        Label title = new Label("PLATEAU DE JEU");
        title.setFont(Font.font("Arial", FontWeight.BOLD, 16));
        title.setPadding(new Insets(0, 0, 10, 0));
        title.setTextFill(Color.WHITE);
        

        gridPane = new GridPane();
        gridPane.setAlignment(Pos.CENTER);
        gridPane.setHgap(1);
        gridPane.setVgap(1);
        gridPane.setStyle("-fx-background-color: #333333;");
        gridPane.setMaxWidth(GridPane.USE_PREF_SIZE);
        gridPane.setMaxHeight(GridPane.USE_PREF_SIZE);
        

        for (int row = 0; row < GRID_SIZE; row++) {
            for (int col = 0; col < GRID_SIZE; col++) {
                Label cell = createCell(row, col);
                cellLabels[row][col] = cell;
                gridPane.add(cell, col, row);
            }
        }
        
        this.setAlignment(Pos.CENTER);
        this.getChildren().addAll(title, gridPane);
    }
    
    /**
        * Creates a board cell with the appropriate color according to its bonus.
     */
    private Label createCell(int row, int col) {
        Label cell = new Label();
        cell.setPrefSize(CELL_SIZE, CELL_SIZE);
        cell.setMaxSize(CELL_SIZE, CELL_SIZE);
        cell.setMinSize(CELL_SIZE, CELL_SIZE);
        cell.setAlignment(Pos.CENTER);
        cell.setFont(Font.font("Arial", FontWeight.BOLD, 18));
        

        Square square = board.getSquare(new Point(col, row)); 
        SquareType type = square.getSquareType();
        applyCellStyle(cell, type, row, col);

        cell.setOnMouseEntered(e -> cell.setStyle(cell.getStyle() + "-fx-opacity: 0.8;"));
        cell.setOnMouseExited(e -> cell.setStyle(cell.getStyle().replace("-fx-opacity: 0.8;", "")));
        
        return cell;
    }
    
    /**
        * Applies styling to a cell depending on its type.
     */
    private void applyCellStyle(Label cell, SquareType type, int row, int col) {
        String style = "-fx-border-color: #333333; -fx-border-width: 1;";
        String text = "";
        
        switch (type) {
            case TRIPLE_WORD:
                style += "-fx-background-color: #5c0099;"; 
                text = "MT";
                cell.setTextFill(Color.WHITE);
                break;
            case DOUBLE_WORD:
                style += "-fx-background-color: #fd002a;"; 
                text = "MD";
                cell.setTextFill(Color.BLACK);
                break;
            case TRIPLE_LETTER:
                style += "-fx-background-color: #0000FF;"; 
                text = "LT";
                cell.setTextFill(Color.WHITE);
                break;
            case DOUBLE_LETTER:
                style += "-fx-background-color: #87CEEB;"; 
                text = "LD";
                cell.setTextFill(Color.BLACK);
                break;
            default: 
                style += "-fx-background-color: #F5E6D3;"; 
                cell.setTextFill(Color.BLACK);
                break;
        }
        


        if (row == 7 && col == 7) {
            text = "★";
            cell.setFont(Font.font("Arial", FontWeight.BOLD, 24));
        } else if (!text.isEmpty()) {
            cell.setFont(Font.font("Arial", FontWeight.BOLD, 10));
        }
        
        cell.setText(text);
        cell.setStyle(style);
    }
    

    public void placeTile(int row, int col, char letter, int points) {
        if (row >= 0 && row < GRID_SIZE && col >= 0 && col < GRID_SIZE) {
            Label cell = cellLabels[row][col];
            cell.setText(String.valueOf(letter));
            cell.setFont(Font.font("Arial", FontWeight.BOLD, 20));
            cell.setStyle("-fx-background-color: #FFE4B5; -fx-border-color: #333333; -fx-border-width: 1;");
            cell.setTextFill(Color.BLACK);
        }
    }
    

    public void clearTile(int row, int col) {
        if (row >= 0 && row < GRID_SIZE && col >= 0 && col < GRID_SIZE) {
            Label cell = cellLabels[row][col];
            Square square = board.getSquare(new Point(col, row)); 
            SquareType type = square.getSquareType();
            applyCellStyle(cell, type, row, col);
        }
    }
}