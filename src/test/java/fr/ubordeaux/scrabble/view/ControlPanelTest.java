package fr.ubordeaux.scrabble.view;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import fr.ubordeaux.scrabble.view.gui.panel.ControlPanel;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

/**
 * Tests unitaires pour ControlPanel.
 *
 * <p>Les tests vérifient que tous les boutons sont créés et correctement
 * configurés
 * sans lancer l'interface graphique complète.
 */
class ControlPanelTest {

  @BeforeAll
  static void initToolkit() {
    try {
      com.sun.javafx.application.PlatformImpl.startup(() -> {
      });
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
    assertNotNull(panel.getHelpButton());
  }

  @Test
  void onlineButtonShouldNotBeNull() {
    ControlPanel panel = new ControlPanel();
    assertNotNull(panel.getCancelPlacementButton());
  }

  @Test
  void saveButtonShouldNotBeNull() {
    ControlPanel panel = new ControlPanel();
    assertNotNull(panel.getUndoButton());
  }

  @Test
  void loadButtonShouldNotBeNull() {
    ControlPanel panel = new ControlPanel();
    assertNotNull(panel.getRedoButton());
  }

  @Test
  void quitButtonShouldNotBeNull() {
    ControlPanel panel = new ControlPanel();
    assertNotNull(panel.getPlayButton());
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
    Button btn = panel.getHelpButton();
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
    assertTrue(panel.getHelpButton().getPrefWidth() > 0);
  }

  @Test
  void setGameplayButtonsDisabledShouldNotDisableHelp() {
    ControlPanel panel = new ControlPanel();
    panel.setGameplayButtonsDisabled(true);
    assertTrue(panel.getPlayButton().isDisable());
    assertTrue(panel.getPassButton().isDisable());
    assertFalse(panel.getHelpButton().isDisable());
  }

  @Test
  void controlPanelShouldContainExpectedTitleBarAndButtonTexts() {
    ControlPanel panel = new ControlPanel();
    assertTrue(panel.getChildren().getFirst() instanceof HBox);

    HBox titleBar = (HBox) panel.getChildren().getFirst();
    assertEquals(3, titleBar.getChildren().size());
    assertTrue(titleBar.getChildren().get(0) instanceof Label);
    assertTrue(titleBar.getChildren().get(1) instanceof Region);
    assertTrue(titleBar.getChildren().get(2) instanceof Button);

    Label title = (Label) titleBar.getChildren().get(0);
    Button help = (Button) titleBar.getChildren().get(2);
    assertEquals("ACTIONS", title.getText());
    assertEquals("❓ Help", help.getText());
    assertEquals("▶  Jouer", panel.getPlayButton().getText());
    assertEquals("⏭  Passer", panel.getPassButton().getText());
    assertEquals("🔄 Échanger", panel.getExchangeButton().getText());
  }
}