package fr.ubordeaux.scrabble.view.gui.setup;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import fr.ubordeaux.scrabble.view.gui.PlayerSetup;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import javafx.application.Platform;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

/**
 * Additional coverage tests for PlayerSetup that require
 * the JavaFX toolkit to be running.
 */
class PlayerSetupFxTest {

  @BeforeAll
  static void initToolkit() {
    try {
      com.sun.javafx.application.PlatformImpl.startup(() -> {
      });
    } catch (Exception e) {
      // Already initialized
    }
  }

  @Test
  void constructorShouldCreateDialogOnFxThread() throws Exception {
    CountDownLatch latch = new CountDownLatch(1);
    AtomicReference<PlayerSetup> ref = new AtomicReference<>();
    Platform.runLater(() -> {
      try {
        ref.set(new PlayerSetup());
      } finally {
        latch.countDown();
      }
    });
    assertTrue(latch.await(5, TimeUnit.SECONDS));
    assertNotNull(ref.get());
  }

  @Test
  void constructorShouldSetTitle() throws Exception {
    CountDownLatch latch = new CountDownLatch(1);
    AtomicReference<PlayerSetup> ref = new AtomicReference<>();
    Platform.runLater(() -> {
      try {
        ref.set(new PlayerSetup());
      } finally {
        latch.countDown();
      }
    });
    assertTrue(latch.await(5, TimeUnit.SECONDS));
    assertNotNull(ref.get());
    assertEquals((String) invokeStatic("dialogTitle"), ref.get().getTitle());
  }

  @Test
  void constructorShouldMakeDialogNotResizable() throws Exception {
    CountDownLatch latch = new CountDownLatch(1);
    AtomicReference<PlayerSetup> ref = new AtomicReference<>();
    Platform.runLater(() -> {
      try {
        ref.set(new PlayerSetup());
      } finally {
        latch.countDown();
      }
    });
    assertTrue(latch.await(5, TimeUnit.SECONDS));
    assertFalse(ref.get().isResizable());
  }

  @Test
  void dialogPaneShouldHaveContent() throws Exception {
    CountDownLatch latch = new CountDownLatch(1);
    AtomicReference<PlayerSetup> ref = new AtomicReference<>();
    Platform.runLater(() -> {
      try {
        ref.set(new PlayerSetup());
      } finally {
        latch.countDown();
      }
    });
    assertTrue(latch.await(5, TimeUnit.SECONDS));
    assertNotNull(ref.get().getDialogPane().getContent());
  }

  @Test
  void dialogPaneShouldHaveButtonTypes() throws Exception {
    CountDownLatch latch = new CountDownLatch(1);
    AtomicReference<PlayerSetup> ref = new AtomicReference<>();
    Platform.runLater(() -> {
      try {
        ref.set(new PlayerSetup());
      } finally {
        latch.countDown();
      }
    });
    assertTrue(latch.await(5, TimeUnit.SECONDS));
    assertEquals(2, ref.get().getDialogPane().getButtonTypes().size());
  }

  @Test
  void dialogPaneShouldHaveSetupDialogStyleClass() throws Exception {
    CountDownLatch latch = new CountDownLatch(1);
    AtomicReference<PlayerSetup> ref = new AtomicReference<>();
    Platform.runLater(() -> {
      try {
        ref.set(new PlayerSetup());
      } finally {
        latch.countDown();
      }
    });
    assertTrue(latch.await(5, TimeUnit.SECONDS));
    assertTrue(ref.get().getDialogPane().getStyleClass().contains(
        (String) invokeStatic("dialogStyleClass")));
  }

  @Test
  void resultConverterReturnsNullForCancelButton() throws Exception {
    CountDownLatch latch = new CountDownLatch(1);
    AtomicReference<Object> ref = new AtomicReference<>();
    Platform.runLater(() -> {
      try {
        PlayerSetup dialog = new PlayerSetup();
        var cancelType = dialog.getDialogPane().getButtonTypes().get(1);
        ref.set(dialog.getResultConverter().call(cancelType));
      } finally {
        latch.countDown();
      }
    });
    assertTrue(latch.await(5, TimeUnit.SECONDS));
    assertEquals(null, ref.get());
  }

  @Test
  void allStaticHelpersShouldReturnExpectedValues() {
    assertFalse(((String) invokeStatic("dialogTitle")).isEmpty());
    assertFalse(((String) invokeStatic("playersLabelText")).isEmpty());
    assertFalse(((String) invokeStatic("dialogHeaderTitle")).isEmpty());
    assertFalse(((String) invokeStatic("startButtonText")).isEmpty());
    assertFalse(((String) invokeStatic("cancelButtonText")).isEmpty());
    assertEquals(2, invokeStatic("minPlayers"));
    assertEquals(4, invokeStatic("maxPlayers"));
    assertEquals(2, invokeStatic("defaultPlayers"));
    assertEquals(80.0, invokeStatic("spinnerPrefWidth"));
    assertEquals(12.0, invokeStatic("spinnerRowSpacing"));
    assertEquals(18.0, invokeStatic("contentSpacing"));
    assertEquals(20.0, invokeStatic("contentPadding"));
    assertEquals(320.0, invokeStatic("contentPrefWidth"));
    assertEquals("#115829", invokeStatic("dialogHeaderColor"));
    assertEquals("setup-dialog", invokeStatic("dialogStyleClass"));
  }

  @Test
  void showDialogMethodShouldExist() {
    assertDoesNotThrow(() -> {
      Method m = PlayerSetup.class.getDeclaredMethod("showDialog");
      assertNotNull(m);
    });
  }

  private Object invokeStatic(String methodName) {
    try {
      Method method = PlayerSetup.class.getDeclaredMethod(methodName);
      method.setAccessible(true);
      return method.invoke(null);
    } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
      throw new RuntimeException(e);
    }
  }
}
