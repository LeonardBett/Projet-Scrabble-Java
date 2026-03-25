package fr.ubordeaux.scrabble.view;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import fr.ubordeaux.scrabble.view.gui.PlayerSetup;
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
      Method method = PlayerSetup.class.getDeclaredMethod("showDialog");
      assertNotNull(method);
    });
  }
}
