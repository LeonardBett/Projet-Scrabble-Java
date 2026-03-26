package fr.ubordeaux.scrabble.view;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import fr.ubordeaux.scrabble.view.gui.PlayerSetup;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import org.junit.jupiter.api.Test;

class PlayerSetupTest {

  @Test
  void classShouldBeReachable() {
    assertNotNull(PlayerSetup.class);
  }

  @Test
  void shouldExposeShowDialogStaticMethod() {
    assertDoesNotThrow(() -> {
      Method method = PlayerSetup.class
          .getDeclaredMethod("showDialog");
      assertNotNull(method);
    });
  }

  @Test
  void helperTextsShouldBeDefined() {
    assertFalse(((String) invokeStatic(
        "dialogTitle")).isBlank());
    assertFalse(((String) invokeStatic(
        "playersLabelText")).isBlank());
    assertFalse(((String) invokeStatic(
        "dialogHeaderTitle")).isBlank());
    assertFalse(((String) invokeStatic(
        "startButtonText")).isBlank());
    assertFalse(((String) invokeStatic(
        "cancelButtonText")).isBlank());
  }

  @Test
  void helperNumericConfigShouldBeDefined() {
    assertEquals(2, invokeStatic("minPlayers"));
    assertEquals(4, invokeStatic("maxPlayers"));
    assertEquals(2, invokeStatic("defaultPlayers"));
    assertEquals(80.0, invokeStatic("spinnerPrefWidth"));
    assertEquals(12.0, invokeStatic("spinnerRowSpacing"));
    assertEquals(18.0, invokeStatic("contentSpacing"));
    assertEquals(20.0, invokeStatic("contentPadding"));
    assertEquals(320.0, invokeStatic("contentPrefWidth"));
    assertEquals("#115829",
        invokeStatic("dialogHeaderColor"));
    assertEquals("setup-dialog",
        invokeStatic("dialogStyleClass"));
  }

  private Object invokeStatic(String methodName) {
    try {
      Method method = PlayerSetup.class
          .getDeclaredMethod(methodName);
      method.setAccessible(true);
      return method.invoke(null);
    } catch (NoSuchMethodException
        | IllegalAccessException
        | InvocationTargetException e) {
      throw new RuntimeException(e);
    }
  }
}
