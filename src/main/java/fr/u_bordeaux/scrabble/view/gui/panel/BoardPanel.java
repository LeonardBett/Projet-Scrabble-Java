package fr.u_bordeaux.scrabble.view.gui.panel;

import java.util.function.BiConsumer;

import fr.u_bordeaux.scrabble.model.core.Board;
import fr.u_bordeaux.scrabble.model.core.Square;
import fr.u_bordeaux.scrabble.model.enums.SquareType;
import fr.u_bordeaux.scrabble.model.utils.Point;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

/**
 * Panel representing the Scrabble board (15x15 grid).
 *
 * ✅ MVC: Only displays the Board from the model.
 * User interactions (drop) are forwarded via a callback to ScrabbleGUI,
 * which delegates to the controller.
 */
public class BoardPanel extends VBox {

    private static final int GRID_SIZE = Board.SIZE;
    private static final int CELL_SIZE = 40;

    private final GridPane gridPane;
    private final Label[][] cellLabels;
    private final Board board;

    /**
     * Callback called when a tile is dropped on a cell: (row, col) → ScrabbleGUI.
     */
    private BiConsumer<Integer, Integer> onTileDropped;

    public BoardPanel(Board board) {
        this.board = board;
        this.cellLabels = new Label[GRID_SIZE][GRID_SIZE];
        this.gridPane = new GridPane();
        initializeUI();
    }

    // ─── Callback setter ──────────────────────────────────────────────────────

    public void setOnTileDropped(BiConsumer<Integer, Integer> callback) {
        this.onTileDropped = callback;
    }

    // ─── UI initialisation ────────────────────────────────────────────────────

    private void initializeUI() {
        Label title = new Label("PLATEAU DE JEU");
        title.setFont(Font.font("Arial", FontWeight.BOLD, 16));
        title.setPadding(new Insets(0, 0, 10, 0));
        title.setTextFill(Color.WHITE);

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

    // ─── Cell creation ────────────────────────────────────────────────────────

    private Label createCell(int row, int col) {
        Label cell = new Label();
        cell.setPrefSize(CELL_SIZE, CELL_SIZE);
        cell.setMaxSize(CELL_SIZE, CELL_SIZE);
        cell.setMinSize(CELL_SIZE, CELL_SIZE);
        cell.setAlignment(Pos.CENTER);
        cell.setFont(Font.font("Arial", FontWeight.BOLD, 18));

        Square square = board.getSquare(new Point(col, row));
        applyCellStyle(cell, square.getSquareType(), row, col);

        // Hover effect
        cell.setOnMouseEntered(e -> cell.setOpacity(0.8));
        cell.setOnMouseExited(e -> cell.setOpacity(1.0));

        // ✅ Drop target: forward to ScrabbleGUI via callback
        setupDropTarget(cell, row, col);

        return cell;
    }

    private void setupDropTarget(Label cell, int row, int col) {
        cell.setOnDragOver(event -> {
            if (event.getDragboard().hasString()) {
                event.acceptTransferModes(TransferMode.MOVE);
            }
            event.consume();
        });

        cell.setOnDragEntered(event -> {
            if (event.getDragboard().hasString()) {
                cell.setOpacity(0.6);
            }
            event.consume();
        });

        cell.setOnDragExited(event -> {
            cell.setOpacity(1.0);
            event.consume();
        });

        cell.setOnDragDropped(event -> {
            boolean success = event.getDragboard().hasString();
            if (success && onTileDropped != null) {
                onTileDropped.accept(row, col);
            }
            event.setDropCompleted(success);
            event.consume();
        });
    }

    // ─── Style helpers ────────────────────────────────────────────────────────

    private void applyCellStyle(Label cell, SquareType type, int row, int col) {
        String style = "-fx-border-color: #333333; -fx-border-width: 1;";
        String text  = "";

        switch (type) {
            case TRIPLE_WORD   -> { style += "-fx-background-color: #5c0099;"; text = "MT"; cell.setTextFill(Color.WHITE); }
            case DOUBLE_WORD   -> { style += "-fx-background-color: #fd002a;"; text = "MD"; cell.setTextFill(Color.WHITE); }
            case TRIPLE_LETTER -> { style += "-fx-background-color: #0000FF;"; text = "LT"; cell.setTextFill(Color.WHITE); }
            case DOUBLE_LETTER -> { style += "-fx-background-color: #87CEEB;"; text = "LD"; cell.setTextFill(Color.BLACK); }
            default            -> { style += "-fx-background-color: #F5E6D3;";               cell.setTextFill(Color.BLACK); }
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

    // ─── Public update methods (called by ScrabbleGUI after refresh) ──────────

    /**
     * Re-reads the entire Board model and redraws every cell.
     */
    public void updateBoard() {
        for (int row = 0; row < GRID_SIZE; row++) {
            for (int col = 0; col < GRID_SIZE; col++) {
                Square square = board.getSquare(new Point(col, row));
                Label cell = cellLabels[row][col];

                if (!square.isEmpty()) {
                    char letter = Character.toUpperCase(square.getTile().getCharacter());
                    cell.setText(String.valueOf(letter));
                    cell.setFont(Font.font("Arial", FontWeight.BOLD, 20));
                    cell.setStyle("-fx-background-color: #FFE4B5; -fx-border-color: #333333; -fx-border-width: 1;");
                    cell.setTextFill(Color.BLACK);
                } else {
                    applyCellStyle(cell, square.getSquareType(), row, col);
                }
            }
        }
    }

    /**
     * Shows a tile temporarily (pending, not yet validated).
     */
    public void placeTile(int row, int col, char letter, int value) {
        if (row < 0 || row >= GRID_SIZE || col < 0 || col >= GRID_SIZE) return;
        Label cell = cellLabels[row][col];
        cell.setText(String.valueOf(Character.toUpperCase(letter)));
        cell.setFont(Font.font("Arial", FontWeight.BOLD, 20));
        cell.setStyle("-fx-background-color: #FFE4B5; -fx-border-color: #FF8C00; -fx-border-width: 2;");
        cell.setTextFill(Color.BLACK);
    }

    /**
     * Restores a cell to its original appearance.
     */
    public void clearTile(int row, int col) {
        if (row < 0 || row >= GRID_SIZE || col < 0 || col >= GRID_SIZE) return;
        Square square = board.getSquare(new Point(col, row));
        applyCellStyle(cellLabels[row][col], square.getSquareType(), row, col);
    }

    public void clearAllPending() {
        for (int row = 0; row < GRID_SIZE; row++) {
            for (int col = 0; col < GRID_SIZE; col++) {
                Square square = board.getSquare(new Point(col, row));
                if (square.isEmpty()) {
                    applyCellStyle(cellLabels[row][col], square.getSquareType(), row, col);
                }
            }
        }
    }

      public void setBoard(Board newBoard) {
      
        updateBoard();
    }
}