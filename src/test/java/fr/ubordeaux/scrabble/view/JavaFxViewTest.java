package fr.ubordeaux.scrabble.view;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;

import fr.ubordeaux.scrabble.model.core.Game;
import fr.ubordeaux.scrabble.model.core.HumanPlayer;
import fr.ubordeaux.scrabble.model.enums.PlayerColor;
import fr.ubordeaux.scrabble.view.gui.JavaFxView;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for JavaFxView.
 * We do not test Platform.runLater calls (UI thread), only behaviors
 * accessible without launching a JavaFX scene.
 */
class JavaFxViewTest {

  private Game game;
  private JavaFxView view;

  @BeforeAll
  static void initJfx() {
    try {
      // Force JavaFX toolkit startup to avoid IllegalStateException
      // during Platform.runLater() calls in JavaFxView.
      com.sun.javafx.application.PlatformImpl.startup(() -> {
      });
    } catch (IllegalStateException e) {
      // Toolkit is already initialized by another test; ignore.
    }
  }

  @BeforeEach
  void setUp() {
    game = new Game();
    game.addPlayer(new HumanPlayer("Alice", PlayerColor.BLUE));
    game.addPlayer(new HumanPlayer("Bob", PlayerColor.RED));
    view = new JavaFxView(game);
  }

  @Test
  void javafxViewShouldBeInstantiable() {
    assertNotNull(view);
  }

  @Test
  void getGameShouldReturnSameGameInstance() {
    assertSame(game, view.getGame());
  }

  @Test
  void setGuiWithNullShouldNotThrow() {
    // setGui(null) should not throw
    view.setGui(null);
  }

  @Test
  void displayMessageWithNullGuiShouldNotThrow() {
    // Without GUI attached, displayMessage should not fail
    view.setGui(null);
    view.displayMessage("Test message");
  }

  @Test
  void displayErrorWithNullGuiShouldNotThrow() {
    view.setGui(null);
    view.displayError("Test error");
  }

  @Test
  void displaySuccessWithNullGuiShouldNotThrow() {
    view.setGui(null);
    view.displaySuccess("Test success");
  }

  @Test
  void refreshWithNullGuiShouldNotThrow() {
    view.setGui(null);
    view.refresh();
  }

  @Test
  void gameReturnedShouldContainTwoPlayers() {
    assertEquals(2, view.getGame().getPlayers().size());
  }
}