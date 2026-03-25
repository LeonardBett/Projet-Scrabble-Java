package fr.ubordeaux.scrabble.view;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;

import fr.ubordeaux.scrabble.model.core.Rack;
import fr.ubordeaux.scrabble.model.core.Tile;
import fr.ubordeaux.scrabble.view.gui.panel.RackPanel;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Tests unitaires pour RackPanel.
 */
class RackPanelTest {

  private Rack rack;
  private RackPanel rackPanel;

  @BeforeAll
  static void initToolkit() {
    try {
      com.sun.javafx.application.PlatformImpl.startup(() -> { });
    } catch (Exception e) {
      // Toolkit already initialized or not available in this environment
    }
  }

  @BeforeEach
  void setUp() {
    rack = new Rack();
    rackPanel = new RackPanel(rack);
  }

  @Test
  void rackPanelShouldBeInstantiableWithRack() {
    assertNotNull(rackPanel);
  }

  @Test
  void rackPanelShouldBeInstantiableWithNoArg() {
    RackPanel panel = new RackPanel();
    assertNotNull(panel);
  }

  @Test
  void getRackShouldReturnInitialRack() {
    assertSame(rack, rackPanel.getRack());
  }

  @Test
  void setRackShouldUpdateRack() {
    Rack newRack = new Rack();
    newRack.addTile(new Tile('Z'));
    rackPanel.setRack(newRack);
    assertSame(newRack, rackPanel.getRack());
  }

  @Test
  void updateDisplayShouldNotThrowOnEmptyRack() {
    rackPanel.updateDisplay();
  }

  @Test
  void updateDisplayShouldNotThrowWithTiles() {
    rack.addTile(new Tile('A'));
    rack.addTile(new Tile('B'));
    rack.addTile(new Tile('C'));
    rackPanel.updateDisplay();
  }

  @Test
  void updateDisplayShouldNotThrowWithFullRack() {
    for (char c : new char[]{ 'A', 'B', 'C', 'D', 'E', 'F', 'G' }) {
      rack.addTile(new Tile(c));
    }
    rackPanel.updateDisplay();
  }

  @Test
  void setOnTileDraggedShouldNotThrow() {
    rackPanel.setOnTileDragged(tile -> {});
  }

  @Test
  void setOnTileDraggedWithNullShouldNotThrow() {
    rackPanel.setOnTileDragged(null);
  }

  @Test
  void hideTileShouldNotThrowWhenTileIsInRack() {
    Tile tile = new Tile('A');
    rack.addTile(tile);
    rackPanel.updateDisplay();
    rackPanel.hideTile(tile);
  }

  @Test
  void hideTileShouldNotThrowWhenTileIsNotInRack() {
    Tile tile = new Tile('Z');
    rackPanel.hideTile(tile); // tuile absente : ne doit pas planter
  }

  @Test
  void setRackShouldRefreshDisplay() {
    Rack newRack = new Rack();
    newRack.addTile(new Tile('X'));
    rackPanel.setRack(newRack);
    assertEquals(1, rackPanel.getRack().getTiles().size());
  }
}