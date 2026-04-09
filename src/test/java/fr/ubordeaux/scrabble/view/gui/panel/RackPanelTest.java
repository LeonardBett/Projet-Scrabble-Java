package fr.ubordeaux.scrabble.view.gui.panel;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import fr.ubordeaux.scrabble.i18n.I18n;
import fr.ubordeaux.scrabble.model.core.Rack;
import fr.ubordeaux.scrabble.model.core.Tile;
import fr.ubordeaux.scrabble.view.gui.panel.RackPanel;
import java.lang.reflect.Field;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for RackPanel.
 */
class RackPanelTest {

  private Rack rack;
  private RackPanel rackPanel;

  @BeforeAll
  static void initToolkit() {
    try {
      com.sun.javafx.application.PlatformImpl.startup(() -> {
      });
    } catch (Exception e) {
      // Toolkit already initialized or not available in this environment
    }
  }

  @BeforeEach
  void setUp() {
    I18n.setLanguage("fr");
    rack = new Rack();
    rackPanel = new RackPanel(rack);
  }

  @AfterEach
  void tearDown() {
    I18n.setLanguage("en");
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
    for (char c : new char[] { 'A', 'B', 'C', 'D', 'E', 'F', 'G' }) {
      rack.addTile(new Tile(c));
    }
    rackPanel.updateDisplay();
  }

  @Test
  void setOnTileDraggedShouldNotThrow() {
    rackPanel.setOnTileDragged(tile -> {
    });
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
    rackPanel.hideTile(tile); // Missing tile: should not fail
  }

  @Test
  void setRackShouldRefreshDisplay() {
    Rack newRack = new Rack();
    newRack.addTile(new Tile('X'));
    rackPanel.setRack(newRack);
    assertEquals(1, rackPanel.getRack().getTiles().size());
  }

  @Test
  void rackPanelShouldContainTitleAndSevenSlotsContainer() {
    assertEquals(2, rackPanel.getChildren().size());

    Node first = rackPanel.getChildren().getFirst();
    Node second = rackPanel.getChildren().get(1);
    assertTrue(first instanceof Label);
    assertTrue(second instanceof HBox);

    Label title = (Label) first;
    assertEquals("Chevalet du joueur 1", title.getText());

    Object[] slots = (Object[]) getPrivateField(rackPanel, "tileContainers");
    assertEquals(7, slots.length);
  }

  private static Object getPrivateField(Object target, String fieldName) {
    try {
      Field field = target.getClass().getDeclaredField(fieldName);
      field.setAccessible(true);
      return field.get(target);
    } catch (NoSuchFieldException | IllegalAccessException e) {
      throw new RuntimeException(e);
    }
  }
}