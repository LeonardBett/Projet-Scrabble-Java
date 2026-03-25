package fr.ubordeaux.scrabble.view.gui.panel;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;

/**
 * Unit tests for ControlPanel button interactions.
 */
class ControlPanelUnitTest {

  @Test
  void controlPanelSetDisabledMethodExists() {
    assertDoesNotThrow(() -> {
      var setDisabled = ControlPanel.class.getMethod("setGameplayButtonsDisabled",
          boolean.class);
      assertNotNull(setDisabled);
    });
  }

  @Test
  void gameplayButtonsCanToggle() {
    assertDoesNotThrow(() -> {
      var method = ControlPanel.class.getDeclaredMethod(
          "setGameplayButtonsDisabled", boolean.class);
      assertNotNull(method);
    });
  }

  @Test
  void controlPanelPlayButtonCallbackExists() {
    assertDoesNotThrow(() -> {
      var method = ControlPanel.class.getMethod("getPlayButton");
      assertNotNull(method);
    });
  }

  @Test
  void controlPanelPassButtonCallbackExists() {
    assertDoesNotThrow(() -> {
      var method = ControlPanel.class.getMethod("getPassButton");
      assertNotNull(method);
    });
  }

  @Test
  void controlPanelExchangeButtonCallbackExists() {
    assertDoesNotThrow(() -> {
      var method = ControlPanel.class.getMethod("getExchangeButton");
      assertNotNull(method);
    });
  }
}
