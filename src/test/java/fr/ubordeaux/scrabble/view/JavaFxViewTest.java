package fr.ubordeaux.scrabble.view;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;

import fr.ubordeaux.scrabble.model.core.Game;
import fr.ubordeaux.scrabble.model.core.HumanPlayer;
import fr.ubordeaux.scrabble.model.enums.PlayerColor;
import fr.ubordeaux.scrabble.view.gui.JavaFxView;
import javafx.application.Platform;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Tests unitaires pour JavaFxView.
 * On ne teste pas les appels à Platform.runLater (thread UI) mais les
 * comportements accessibles sans lancer de scène JavaFX.
 */
class JavaFxViewTest {

  private Game game;
  private JavaFxView view;

  @BeforeAll
  static void initJfx() {
    try {
      // Force le démarrage du Toolkit JavaFX pour éviter l'IllegalStateException
      // lors des appels à Platform.runLater() dans JavaFxView.
      Platform.startup(() -> {});
    } catch (IllegalStateException e) {
      // Le Toolkit est déjà initialisé par un autre test, on ignore l'erreur.
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
    // setGui(null) ne doit pas lever d'exception
    view.setGui(null);
  }

  @Test
  void displayMessageWithNullGuiShouldNotThrow() {
    // Sans GUI attaché, displayMessage ne doit pas planter
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