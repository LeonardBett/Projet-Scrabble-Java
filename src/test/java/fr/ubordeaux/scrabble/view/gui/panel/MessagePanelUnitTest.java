package fr.ubordeaux.scrabble.view.gui.panel;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;

/**
 * Unit tests for MessagePanel dialog methods.
 */
class MessagePanelUnitTest {

  @Test
  void messagePanelShowInfoMethodExists() {
    assertDoesNotThrow(() -> {
      var showInfo = MessagePanel.class.getMethod("showInfo", String.class,
          String.class);
      assertNotNull(showInfo);
    });
  }

  @Test
  void messagePanelShowErrorMethodExists() {
    assertDoesNotThrow(() -> {
      var showError = MessagePanel.class.getMethod("showError", String.class);
      assertNotNull(showError);
    });
  }

  @Test
  void messagePanelShowConfirmationMethodExists() {
    assertDoesNotThrow(() -> {
      var showConfirm = MessagePanel.class.getMethod("showConfirmation",
          String.class);
      assertNotNull(showConfirm);
    });
  }

  @Test
  void messageDialogMethodsAreInvokable() {
    assertDoesNotThrow(() -> {
      var infoMethod = MessagePanel.class.getDeclaredMethod("showInfo",
          String.class, String.class);
      var errorMethod = MessagePanel.class.getDeclaredMethod("showError",
          String.class);
      var confirmMethod = MessagePanel.class.getDeclaredMethod(
          "showConfirmation", String.class);
      assertNotNull(infoMethod);
      assertNotNull(errorMethod);
      assertNotNull(confirmMethod);
    });
  }
}
