package fr.ubordeaux.scrabble.view;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertTrue;

import fr.ubordeaux.scrabble.view.gui.panel.MessagePanel;
import java.util.ArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import javafx.application.Platform;
import javafx.stage.Window;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class MessagePanelTest {

  @BeforeAll
  static void initToolkit() {
    try {
      com.sun.javafx.application.PlatformImpl.startup(() -> { });
    } catch (Exception e) {
      // Toolkit already initialized
    }
  }

  @AfterEach
  void closeOpenedWindows() throws InterruptedException {
    CountDownLatch latch = new CountDownLatch(1);
    Platform.runLater(() -> {
      for (Window w : new ArrayList<>(Window.getWindows())) {
        w.hide();
      }
      latch.countDown();
    });
    awaitFxOrFail(latch);
  }

  @Test
  void infoErrorWarningShouldNotThrow() throws Exception {
    MessagePanel panel = new MessagePanel();
    assertDoesNotThrow(() -> runOnFxThread(() -> {
      panel.showInfo("Info", "Message");
      panel.showError("Erreur");
      panel.showWarning("Warn", "Attention");
      return null;
    }));
  }

  private static <T> T runOnFxThread(java.util.concurrent.Callable<T> callable) throws Exception {
    CountDownLatch latch = new CountDownLatch(1);
    AtomicReference<T> result = new AtomicReference<>();
    AtomicReference<Throwable> error = new AtomicReference<>();

    Platform.runLater(() -> {
      try {
        result.set(callable.call());
      } catch (Throwable t) {
        error.set(t);
      } finally {
        latch.countDown();
      }
    });

    awaitFxOrFail(latch);
    if (error.get() != null) {
      throw new RuntimeException(error.get());
    }
    return result.get();
  }

  private static void awaitFxOrFail(CountDownLatch latch) throws InterruptedException {
    assertTrue(latch.await(2, TimeUnit.SECONDS), "Timeout waiting for JavaFX event queue");
  }
}
