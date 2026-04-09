package fr.ubordeaux.scrabble.view.optionlancement;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

import fr.ubordeaux.scrabble.model.enums.GameMode;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import org.junit.jupiter.api.Test;

class CliLauncherCoverageTest {

  @Test
  void launchShouldThrowWhenSaveFileCannotBeLoaded() {
    assertThrows(IllegalArgumentException.class,
        () -> CliLauncher.launch(2, List.of(), false, 30, 2, false, false, "en",
            "missing-save.scrabble"));
  }

  @Test
  void launchShouldRunWithNullGameMode() {
    assertDoesNotThrow(() -> runWithInput("quit\nn\nquit\no\n",
        () -> CliLauncher.launch(null, 2, List.of("BLUE"), false, 30, 2, false, false,
            "en", null)));
  }

  @Test
  void launchShouldRunWithSpecificAiColorAndBlitz() {
    assertDoesNotThrow(() -> runWithInput("quit\nn\nquit\no\n",
        () -> CliLauncher.launch(GameMode.STANDARD, 3, List.of("BLUE"), true, 1, 2, true, true,
            "fr")));
  }

  @Test
  void launchShouldRunWithNullAiColors() {
    assertDoesNotThrow(() -> runWithInput("quit\nn\nquit\no\n",
        () -> CliLauncher.launch(2, null, false, 30, 2, false, false, "en")));
  }

  @Test
  void launchOverloadWithSavePathParameterShouldRunWhenNull() {
    assertDoesNotThrow(() -> runWithInput("quit\nn\nquit\no\n",
        () -> CliLauncher.launch(2, List.of(), false, 30, 2, false, false, "en", null)));
  }

  @Test
  void launchShouldLoadSaveFileSuccessfully() throws IOException {
    Path saveFile = Files.createTempFile(Path.of("target/"), "scrabble-cli-save-", ".scrabble");
    Files.writeString(saveFile,
        "[settings]\n"
            + "super-scrabble=false\n"
            + "[game]\n"
            + "[history]\n");

    assertDoesNotThrow(() -> runWithInput("quit\nn\nquit\no\n",
        () -> CliLauncher.launch(GameMode.STANDARD, 2, List.of(), false, 30, 2, false, false,
            "en", saveFile.toString())));
  }

  private static void runWithInput(String input, ThrowingRunnable action) throws Exception {
    InputStream originalIn = System.in;
    try {
      System.setIn(new ByteArrayInputStream(input.getBytes(StandardCharsets.UTF_8)));
      action.run();
    } finally {
      System.setIn(originalIn);
    }
  }

  @FunctionalInterface
  private interface ThrowingRunnable {
    void run() throws Exception;
  }
}
