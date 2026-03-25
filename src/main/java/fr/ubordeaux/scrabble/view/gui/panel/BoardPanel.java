package fr.ubordeaux.scrabble.view.gui.panel;

import fr.ubordeaux.scrabble.model.core.Board;
import fr.ubordeaux.scrabble.model.core.Square;
import fr.ubordeaux.scrabble.model.enums.SquareType;
import fr.ubordeaux.scrabble.model.utils.Point;
import java.util.function.BiConsumer;
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
 * JavaFX panel that renders the Scrabble board as a grid of draggable cells.
 */
public class BoardPanel extends VBox {

  private static final int CELL_SIZE = 40;

  private final int gridSize;
  private final GridPane gridPane;
  private final Label[][] cellLabels;
  private Board board;

  private BiConsumer<Integer, Integer> onTileDropped;

  /**
   * Creates a BoardPanel displaying the given board.
   *
   * @param board the board model to display
   */
  public BoardPanel(Board board) {
    this.board = board;
    this.gridSize = board.getSize();
    this.cellLabels = new Label[gridSize][gridSize];
    this.gridPane = new GridPane();
    initializeUi();
  }

  /**
   * Sets the callback invoked when a tile is dropped on a cell.
   *
   * @param callback a BiConsumer receiving (row, col) of the drop target
   */
  public void setOnTileDropped(BiConsumer<Integer, Integer> callback) {
    this.onTileDropped = callback;
  }

  private void initializeUi() {
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

    for (int row = 0; row < gridSize; row++) {
      for (int col = 0; col < gridSize; col++) {
        Label cell = createCell(row, col);
        cellLabels[row][col] = cell;
        gridPane.add(cell, col, row);
      }
    }

    this.setAlignment(Pos.CENTER);
    this.getChildren().addAll(title, gridPane);
  }

  private Label createCell(int row, int col) {
    Label cell = new Label();
    cell.setPrefSize(CELL_SIZE, CELL_SIZE);
    cell.setMaxSize(CELL_SIZE, CELL_SIZE);
    cell.setMinSize(CELL_SIZE, CELL_SIZE);
    cell.setAlignment(Pos.CENTER);

    Square square = board.getSquare(new Point(col, row));
    applyCellStyle(cell, square.getSquareType(), row, col);

    cell.setOnMouseEntered(e -> cell.setOpacity(0.8));
    cell.setOnMouseExited(e -> cell.setOpacity(1.0));

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

  private void applyCellStyle(Label cell, SquareType type, int row, int col) {
    String style = "-fx-border-color: #333333; -fx-border-width: 1;";
    String text = "";

    switch (type) {
      case TRIPLE_WORD -> {
        style += "-fx-background-color: #5c0099;";
        text = "MT";
        cell.setTextFill(Color.WHITE);
      }
      case DOUBLE_WORD -> {
        style += "-fx-background-color: #fd002a;";
        text = "MD";
        cell.setTextFill(Color.WHITE);
      }
      case TRIPLE_LETTER -> {
        style += "-fx-background-color: #0000FF;";
        text = "LT";
        cell.setTextFill(Color.WHITE);
      }
      case DOUBLE_LETTER -> {
        style += "-fx-background-color: #87CEEB;";
        text = "LD";
        cell.setTextFill(Color.BLACK);
      }
      default -> {
        style += "-fx-background-color: #F5E6D3;";
        cell.setTextFill(Color.BLACK);
      }
    }

    int center = gridSize / 2;
    if (row == center && col == center) {
      text = "*";
      cell.setFont(Font.font("Arial", FontWeight.BOLD, 24));
    } else if (!text.isEmpty()) {
      cell.setFont(Font.font("Arial", FontWeight.BOLD, 10));
    } else {
      cell.setFont(Font.font("Arial", FontWeight.BOLD, 18));
    }

    cell.setText(text);
    cell.setStyle(style);
  }

  /**
   * Refreshes all cells to reflect the current board state.
   */
  public void updateBoard() {
    for (int row = 0; row < gridSize; row++) {
      for (int col = 0; col < gridSize; col++) {
        Square square = board.getSquare(new Point(col, row));
        Label cell = cellLabels[row][col];
        if (!square.isEmpty()) {
          char letter = Character.toUpperCase(square.getTile().getCharacter());
          cell.setText(String.valueOf(letter));
          cell.setFont(Font.font("Arial", FontWeight.BOLD, 20));
          cell.setStyle(
              "-fx-background-color: #FFE4B5; -fx-border-color: #333333; -fx-border-width: 1;");
          cell.setTextFill(Color.BLACK);
        } else {
          applyCellStyle(cell, square.getSquareType(), row, col);
        }
      }
    }
  }

  /**
   * Places a tile visually on a cell (pending placement, not yet validated).
   *
   * @param row    the row index
   * @param col    the column index
   * @param letter the letter to display
   * @param value  the tile point value (unused visually, kept for API consistency)
   */
  public void placeTile(int row, int col, char letter, int value) {
    if (row < 0 || row >= gridSize || col < 0 || col >= gridSize) {
      return;
    }
    Label cell = cellLabels[row][col];
    cell.setText(String.valueOf(Character.toUpperCase(letter)));
    cell.setFont(Font.font("Arial", FontWeight.BOLD, 20));
    cell.setStyle("-fx-background-color: #FFE4B5; -fx-border-color: #FF8C00; -fx-border-width: 2;");
    cell.setTextFill(Color.BLACK);
  }

  /**
   * Removes a pending tile from a cell, restoring its original style.
   *
   * @param row the row index
   * @param col the column index
   */
  public void clearTile(int row, int col) {
    if (row < 0 || row >= gridSize || col < 0 || col >= gridSize) {
      return;
    }
    Square square = board.getSquare(new Point(col, row));
    applyCellStyle(cellLabels[row][col], square.getSquareType(), row, col);
  }

  /**
   * Clears all pending (unvalidated) tiles from the board display.
   */
  public void clearAllPending() {
    for (int row = 0; row < gridSize; row++) {
      for (int col = 0; col < gridSize; col++) {
        Square square = board.getSquare(new Point(col, row));
        if (square.isEmpty()) {
          applyCellStyle(cellLabels[row][col], square.getSquareType(), row, col);
        }
      }
    }
  }

  /**
   * Replaces the current board model and refreshes the display.
   *
   * @param newBoard the new board to display
   */
  public void setBoard(Board newBoard) {
    this.board = newBoard;
    updateBoard();
  }
}
