package fr.ubordeaux.scrabble.view.optionlancement;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import fr.ubordeaux.scrabble.model.ai.AiPlayer;
import fr.ubordeaux.scrabble.model.core.Game;
import fr.ubordeaux.scrabble.model.core.HumanPlayer;
import fr.ubordeaux.scrabble.model.interfaces.Player;
import fr.ubordeaux.scrabble.view.gui.ScrabbleGui;
import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

class GuiLauncherCoverageTest {

  private String originalUserHome;

  @AfterEach
  void tearDown() {
    GuiLauncher.resetLaunchHandlerForTests();
    restoreUserHome();
  }

  @Test
  void createConfiguredGameShouldUseDefaultPlayersWhenZero() {
    Game game = GuiLauncher.createConfiguredGame(0, List.of(), false, 30, 5, false);
    assertEquals(OptionPlayer.DEFAULT, game.getPlayers().size());
    assertFalse(game.isBlitzModeEnabled());
  }

  @Test
  void createConfiguredGameShouldConfigureAiAndBlitz() {
    Game game = GuiLauncher.createConfiguredGame(3, List.of("BLUE", "YELLOW"), true, 10, 7,
        true);

    assertEquals(3, game.getPlayers().size());
    assertTrue(game.isBlitzModeEnabled());

    Player p1 = game.getPlayers().get(0);
    Player p2 = game.getPlayers().get(1);
    Player p3 = game.getPlayers().get(2);

    assertInstanceOf(AiPlayer.class, p1);
    assertInstanceOf(HumanPlayer.class, p2);
    assertInstanceOf(AiPlayer.class, p3);
  }

  @Test
  void launchShouldInvokeConfiguredLaunchHandler() {
    final boolean[] called = { false };
    GuiLauncher.setLaunchHandlerForTests((appClass, args) -> called[0] = true);

    GuiLauncher.launch(new String[] {}, 2, List.of(), false, 30, 5, false, false, "en");

    assertTrue(called[0]);
  }

  @Test
  void createConfiguredGameShouldUseStandardModeWhenNullAndAllAiFlag() {
    Game game = GuiLauncher.createConfiguredGame(null, 2, List.of("A"), false, 30, 4, false);

    assertEquals(2, game.getPlayers().size());
    assertInstanceOf(AiPlayer.class, game.getPlayers().get(0));
    assertInstanceOf(AiPlayer.class, game.getPlayers().get(1));
  }

  @Test
  void launchShouldThrowWhenSaveFileCannotBeLoaded() {
    IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
        () -> GuiLauncher.launch(new String[] {}, 2, List.of(), false, 30, 5, false, false,
            "en", "does-not-exist.scrabble"));

    assertTrue(exception.getMessage().contains("Could not load save file"));
  }

  @Test
  void launchShouldLoadSaveFileWhenProvided() throws IOException {
    Path saveFile = Files.createTempFile("scrabble-save-", ".scrabble");
    Files.writeString(saveFile,
        "[settings]\n"
            + "super-scrabble=false\n"
            + "[game]\n"
            + "[history]\n");
    final boolean[] called = { false };
    GuiLauncher.setLaunchHandlerForTests((appClass, args) -> called[0] = true);

    GuiLauncher.launch(new String[] {}, 2, List.of(), false, 30, 5, false, false, "en",
        saveFile.toString());

    assertTrue(called[0]);
  }

  @Test
  void createGameFromConfigShouldFallbackOnInvalidNumbersAndNormalizeLanguage() throws Exception {
    Path fakeHome = Files.createTempDirectory("scrabble-home-");
    Path config = fakeHome.resolve(".scrabblerc");
    Files.writeString(config,
        "[defaults]\n"
            + "language=xx\n"
            + "players-count=abc\n"
            + "blitz=true\n"
            + "timeout=not-a-number\n"
            + "ai-time=oops\n"
            + "ai-exptiminimax=true\n"
            + "super-scrabble=true\n");

    originalUserHome = System.getProperty("user.home");
    System.setProperty("user.home", fakeHome.toString());

    Game game = GuiLauncher.createGameFromConfig();

    assertNotNull(game);
    assertEquals(OptionPlayer.DEFAULT, game.getPlayers().size());
    assertTrue(game.isBlitzModeEnabled());
    assertEquals("en", configuredLanguage());
  }

  private String configuredLanguage() throws Exception {
    Field field = ScrabbleGui.class.getDeclaredField("configuredLanguage");
    field.setAccessible(true);
    return (String) field.get(null);
  }

  private void restoreUserHome() {
    if (originalUserHome != null) {
      System.setProperty("user.home", originalUserHome);
      originalUserHome = null;
    }
  }
}
