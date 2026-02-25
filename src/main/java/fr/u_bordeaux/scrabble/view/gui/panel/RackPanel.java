package fr.u_bordeaux.scrabble.view.gui.panel;

import java.util.List;
import java.util.function.Consumer;

import fr.u_bordeaux.scrabble.model.core.Rack;
import fr.u_bordeaux.scrabble.model.core.Tile;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

/**
 * Panel representing the player's rack (7 tiles).
 *
 * ✅ MVC: Only displays the Rack from the model.
 * Drag events are forwarded via a callback to ScrabbleGUI,
 * which stores the dragged tile and delegates to the controller when dropped.
 */
public class RackPanel extends VBox {

    private static final int MAX_TILES = Rack.MAX_SIZE;
    private static final int TILE_SIZE = 60;

    private final HBox tilesBox;
    private final StackPane[] tileContainers;
    private Rack rack;

    /**
     * Callback called when the user starts dragging a tile: (tile) → ScrabbleGUI.
     */
    private Consumer<Tile> onTileDragged;

    public RackPanel(Rack rack) {
        this.rack = rack;
        this.tileContainers = new StackPane[MAX_TILES];
        this.tilesBox = new HBox(10);
        initializeUI();
        updateDisplay();
    }

    public RackPanel() {
        this(new Rack());
    }

    // ─── Callback setter ──────────────────────────────────────────────────────

    public void setOnTileDragged(Consumer<Tile> callback) {
        this.onTileDragged = callback;
        // Re-apply so existing containers use the new callback
        updateDisplay();
    }

    // ─── UI initialisation ────────────────────────────────────────────────────

    private void initializeUI() {
        Label title = new Label("CHEVALET DU JOUEUR");
        title.setFont(Font.font("Arial", FontWeight.BOLD, 14));
        title.setTextFill(Color.WHITE);
        title.setPadding(new Insets(0, 0, 10, 0));

        tilesBox.setAlignment(Pos.CENTER);
        tilesBox.setPadding(new Insets(10));
        tilesBox.setStyle("-fx-background-color: #000000; -fx-background-radius: 10;");
        tilesBox.setMaxWidth(GridPane.USE_PREF_SIZE);
        tilesBox.setMaxHeight(GridPane.USE_PREF_SIZE);

        for (int i = 0; i < MAX_TILES; i++) {
            StackPane container = createEmptySlot();
            tileContainers[i] = container;
            tilesBox.getChildren().add(container);
        }

        this.setAlignment(Pos.CENTER);
        this.getChildren().addAll(title, tilesBox);
    }

    private StackPane createEmptySlot() {
        StackPane slot = new StackPane();
        slot.setPrefSize(TILE_SIZE, TILE_SIZE);
        slot.setMaxSize(TILE_SIZE, TILE_SIZE);
        slot.setMinSize(TILE_SIZE, TILE_SIZE);
        slot.setStyle("-fx-background-color: #8B6914; " +
                      "-fx-border-color: #333333; " +
                      "-fx-border-width: 2; " +
                      "-fx-background-radius: 5; " +
                      "-fx-border-radius: 5;");
        return slot;
    }

    // ─── Display update ───────────────────────────────────────────────────────

    /**
     * Re-reads the Rack model and redraws every slot.
     */
    public void updateDisplay() {
        List<Tile> tiles = rack.getTiles();

        for (int i = 0; i < MAX_TILES; i++) {
            StackPane slot = tileContainers[i];
            slot.getChildren().clear();
            slot.setVisible(true);

            if (i < tiles.size()) {
                Tile tile = tiles.get(i);
                fillSlot(slot, tile);
            } else {
                // Empty slot
                slot.setStyle("-fx-background-color: #8B6914; " +
                              "-fx-border-color: #333333; " +
                              "-fx-border-width: 2; " +
                              "-fx-background-radius: 5; " +
                              "-fx-border-radius: 5;");
            }
        }
    }

    /**
     * Fills a slot with the tile's letter and value, and activates drag.
     */
    private void fillSlot(StackPane slot, Tile tile) {
        // Style
        slot.setStyle("-fx-background-color: #FFE4B5; " +
                      "-fx-border-color: #333333; " +
                      "-fx-border-width: 2; " +
                      "-fx-background-radius: 5; " +
                      "-fx-border-radius: 5;");

        // Letter label
        Label letterLabel = new Label(String.valueOf(tile.getCharacter()));
        letterLabel.setFont(Font.font("Arial", FontWeight.BOLD, 28));
        letterLabel.setTextFill(Color.BLACK);
        StackPane.setAlignment(letterLabel, Pos.CENTER);

        // Value label (bottom-right)
        Label valueLabel = new Label(String.valueOf(tile.getValue()));
        valueLabel.setFont(Font.font("Arial", FontWeight.NORMAL, 10));
        valueLabel.setTextFill(Color.DARKGRAY);
        StackPane.setAlignment(valueLabel, Pos.BOTTOM_RIGHT);
        StackPane.setMargin(valueLabel, new Insets(0, 4, 4, 0));

        slot.getChildren().addAll(letterLabel, valueLabel);

        // Hover
        slot.setOnMouseEntered(e -> slot.setStyle(slot.getStyle() + "-fx-cursor: hand;"));
        slot.setOnMouseExited(e -> slot.setStyle(slot.getStyle().replace("-fx-cursor: hand;", "")));

        // ✅ Drag source: notify ScrabbleGUI which tile is being dragged
        setupDragSource(slot, tile);
    }

    /**
     * Activates drag-and-drop on a slot.
     */
    private void setupDragSource(StackPane slot, Tile tile) {
        slot.setOnDragDetected(event -> {
            // Notify the ScrabbleGUI of the tile being dragged
            if (onTileDragged != null) {
                onTileDragged.accept(tile);
            }

            Dragboard db = slot.startDragAndDrop(TransferMode.MOVE);
            ClipboardContent content = new ClipboardContent();
            // We put a non-empty string so the board cell can accept the drop
            content.putString(tile.getCharacter() + ":" + tile.getValue());
            db.setContent(content);

            event.consume();
        });
    }

    // ─── Public helpers ───────────────────────────────────────────────────────

    /**
     * Hides a tile visually (it has been placed on the board, pending validation).
     */
    public void hideTile(Tile tile) {
        List<Tile> tiles = rack.getTiles();
        for (int i = 0; i < tiles.size(); i++) {
            if (tiles.get(i).equals(tile)) {
                tileContainers[i].setVisible(false);
                break;
            }
        }
    }

    /**
     * Changes the displayed rack (e.g. when the turn changes).
     */
    public void setRack(Rack newRack) {
        this.rack = newRack;
        updateDisplay();
    }

    public Rack getRack() {
        return rack;
    }
}