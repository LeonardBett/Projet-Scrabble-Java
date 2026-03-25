package fr.ubordeaux.scrabble.view;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertFalse;

import fr.ubordeaux.scrabble.model.core.Game;
import fr.ubordeaux.scrabble.view.gui.JavaFxView;
import fr.ubordeaux.scrabble.view.gui.ScrabbleGui;
import org.junit.jupiter.api.Test;

class ScrabbleGuiTest {

  @Test
  void shouldInstantiateWithOfflineDefaultMode() {
    ScrabbleGui gui = new ScrabbleGui();
    assertFalse(gui.isOnlineMode());
  }

  @Test
  void staticSettersAndSafeCallbacksShouldNotThrow() {
    Game game = new Game();
    JavaFxView view = new JavaFxView(game);

    assertDoesNotThrow(() -> ScrabbleGui.setGame(game));
    assertDoesNotThrow(() -> ScrabbleGui.setView(view));

    ScrabbleGui gui = new ScrabbleGui();
    assertDoesNotThrow(() -> gui.onTileDragged(null));
    assertDoesNotThrow(() -> gui.onTileDropped(7, 7));
  }
}
