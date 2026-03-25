package fr.ubordeaux.scrabble.view.optionlancement;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;

import fr.ubordeaux.scrabble.model.ai.AiPlayer;
import fr.ubordeaux.scrabble.model.core.Game;
import fr.ubordeaux.scrabble.model.core.HumanPlayer;
import fr.ubordeaux.scrabble.model.interfaces.Player;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

class GuiLauncherCoverageTest {

  @AfterEach
  void tearDown() {
    GuiLauncher.resetLaunchHandlerForTests();
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
    final boolean[] called = {false};
    GuiLauncher.setLaunchHandlerForTests((appClass, args) -> called[0] = true);

    GuiLauncher.launch(new String[] {}, 2, List.of(), false, 30, 5, false, false, "en");

    assertTrue(called[0]);
  }
}
