package fr.ubordeaux.scrabble.view;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import fr.ubordeaux.scrabble.view.gui.panel.ControlPanel;
import javafx.scene.control.Button;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

/**
 * Tests unitaires pour ControlPanel.
 *
 * <p>Les tests vérifient que tous les boutons sont créés et correctement configurés
 * sans lancer l'interface graphique complète.
 */
class ControlPanelTest {

  @BeforeAll
  static void initToolkit() {
    try {
      com.sun.javafx.application.PlatformImpl.startup(() -> { });
    } catch (Exception e) {
      // Toolkit already initialized or not available in this environment
    }
  }

  @Test
  void controlPanelShouldBeInstantiable() {
    ControlPanel panel = new ControlPanel();
    assertNotNull(panel);
  }

  @Test
  void playButtonShouldNotBeNull() {
    ControlPanel panel = new ControlPanel();
    assertNotNull(panel.getPlayButton());
  }

  @Test
  void passButtonShouldNotBeNull() {
    ControlPanel panel = new ControlPanel();
    assertNotNull(panel.getPassButton());
  }

  @Test
  void exchangeButtonShouldNotBeNull() {
    ControlPanel panel = new ControlPanel();
    assertNotNull(panel.getExchangeButton());
  }

  @Test
  void cancelPlacementButtonShouldNotBeNull() {
    ControlPanel panel = new ControlPanel();
    assertNotNull(panel.getCancelPlacementButton());
  }

  @Test
  void undoButtonShouldNotBeNull() {
    ControlPanel panel = new ControlPanel();
    assertNotNull(panel.getUndoButton());
  }

  @Test
  void redoButtonShouldNotBeNull() {
    ControlPanel panel = new ControlPanel();
    assertNotNull(panel.getRedoButton());
  }

  @Test
  void newGameButtonShouldNotBeNull() {
    ControlPanel panel = new ControlPanel();
    assertNotNull(panel.getNewGameButton());
  }

  @Test
  void onlineButtonShouldNotBeNull() {
    ControlPanel panel = new ControlPanel();
    assertNotNull(panel.getOnlineButton());
  }

  @Test
  void saveButtonShouldNotBeNull() {
    ControlPanel panel = new ControlPanel();
    assertNotNull(panel.getSaveButton());
  }

  @Test
  void loadButtonShouldNotBeNull() {
    ControlPanel panel = new ControlPanel();
    assertNotNull(panel.getLoadButton());
  }

  @Test
  void quitButtonShouldNotBeNull() {
    ControlPanel panel = new ControlPanel();
    assertNotNull(panel.getQuitButton());
  }

  @Test
  void allButtonsShouldBeEnabledByDefault() {
    ControlPanel panel = new ControlPanel();
    assertFalse(panel.getPlayButton().isDisable());
    assertFalse(panel.getPassButton().isDisable());
    assertFalse(panel.getExchangeButton().isDisable());
    assertFalse(panel.getUndoButton().isDisable());
    assertFalse(panel.getRedoButton().isDisable());
  }

  @Test
  void playButtonShouldContainPlayText() {
    ControlPanel panel = new ControlPanel();
    Button btn = panel.getPlayButton();
    assertNotNull(btn.getText());
    assertFalse(btn.getText().isBlank());
  }

  @Test
  void quitButtonShouldContainQuitText() {
    ControlPanel panel = new ControlPanel();
    Button btn = panel.getQuitButton();
    assertNotNull(btn.getText());
    assertFalse(btn.getText().isBlank());
  }

  @Test
  void undoButtonCanBeDisabled() {
    ControlPanel panel = new ControlPanel();
    panel.getUndoButton().setDisable(true);
    assertTrue(panel.getUndoButton().isDisable());
  }

  @Test
  void redoButtonCanBeDisabled() {
    ControlPanel panel = new ControlPanel();
    panel.getRedoButton().setDisable(true);
    assertTrue(panel.getRedoButton().isDisable());
  }

  @Test
  void controlPanelCanBeDisabled() {
    ControlPanel panel = new ControlPanel();
    panel.setDisable(true);
    assertTrue(panel.isDisable());
  }

  @Test
  void controlPanelCanBeReEnabled() {
    ControlPanel panel = new ControlPanel();
    panel.setDisable(true);
    panel.setDisable(false);
    assertFalse(panel.isDisable());
  }

  @Test
  void allButtonsShouldHavePreferredWidth() {
    ControlPanel panel = new ControlPanel();
    assertTrue(panel.getPlayButton().getPrefWidth() > 0);
    assertTrue(panel.getPassButton().getPrefWidth() > 0);
    assertTrue(panel.getQuitButton().getPrefWidth() > 0);
  }
}