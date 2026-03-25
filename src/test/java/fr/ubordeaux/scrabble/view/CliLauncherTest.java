package fr.ubordeaux.scrabble.view;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

import fr.ubordeaux.scrabble.view.optionlancement.CliLauncher;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import org.junit.jupiter.api.Test;

class CliLauncherTest {

  @Test
  void launchShouldRunAndQuitImmediatelyWithValidInput() {
    InputStream originalIn = System.in;
    try {
      String input = "6\no\n";
      System.setIn(new ByteArrayInputStream(input.getBytes(StandardCharsets.UTF_8)));

      assertDoesNotThrow(() ->
          CliLauncher.launch(2, List.of(), false, 30, 2, false, false, "en"));
    } finally {
      System.setIn(originalIn);
    }
  }
}
