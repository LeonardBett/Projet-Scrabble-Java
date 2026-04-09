package fr.ubordeaux.scrabble.view.optionlancement;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import fr.ubordeaux.scrabble.view.optionlancement.GuiLauncher;
import java.lang.reflect.Method;
import java.util.List;
import org.junit.jupiter.api.Test;

class GuiLauncherTest {

  @Test
  void classShouldBeReachable() {
    assertNotNull(GuiLauncher.class);
  }

  @Test
  void shouldExposeMainLaunchSignature() {
    assertDoesNotThrow(() -> {
      Method method = GuiLauncher.class.getDeclaredMethod("launch", String[].class, int.class,
          List.class, boolean.class, int.class, int.class, boolean.class, boolean.class,
          String.class);
      assertNotNull(method);
    });
  }
}
