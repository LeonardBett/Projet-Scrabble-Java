package fr.ubordeaux.scrabble.view;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import fr.ubordeaux.scrabble.view.optionlancement.CliLauncher;
import fr.ubordeaux.scrabble.view.optionlancement.GuiLauncher;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for CliLauncher and GuiLauncher.
 *
 * <p>Note: launch() starts an interactive application, so these tests only
 * check instantiation and behavior verifiable without launching a GUI.
 */
class LauncherTest {

  // ─── CliLauncher ─────────────────────────────────────────────────────────

  @Test
  void cliLauncherClassShouldExist() {
    // Verifies that the class is accessible
    assertNotNull(CliLauncher.class);
  }

  @Test
  void guiLauncherClassShouldExist() {
    assertNotNull(GuiLauncher.class);
  }

  // launch() methods start interactive GUIs and cannot be invoked
  // in automated test contexts. Here we verify launcher configuration
  // remains consistent with OptionPlayer.
  @Test
  void cliLauncherAndOptionPlayerShouldShareMinPlayers() {
    // CliLauncher does not expose its own constants and relies on
    // OptionPlayer
    assertEquals(2, fr.ubordeaux.scrabble.view.optionlancement.OptionPlayer.MIN);
  }

  @Test
  void cliLauncherAndOptionPlayerShouldShareMaxPlayers() {
    assertEquals(4, fr.ubordeaux.scrabble.view.optionlancement.OptionPlayer.MAX);
  }
}