package fr.ubordeaux.scrabble.view.gui.main;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import fr.ubordeaux.scrabble.controller.GameController;
import fr.ubordeaux.scrabble.i18n.I18n;
import fr.ubordeaux.scrabble.model.core.Game;
import fr.ubordeaux.scrabble.model.core.HumanPlayer;
import fr.ubordeaux.scrabble.model.enums.PlayerColor;
import fr.ubordeaux.scrabble.view.gui.JavaFxView;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.DialogPane;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import javafx.stage.Window;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.testfx.api.FxRobot;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.framework.junit5.Start;

@ExtendWith(ApplicationExtension.class)
class ScrabbleGuiConfigDialogTest {

  private ControllerGameContext context;

  @Start
  private void start(Stage stage) {
    stage.setScene(new Scene(new StackPane(), 240, 120));
    stage.show();
  }

  @BeforeEach
  void setUp() {
    context = createControllerWithConfig();
  }

  @Test
  void showDialogShouldApplyAssignmentsForOk(FxRobot robot) throws Exception {
    AtomicReference<String> payload = new AtomicReference<>();
    AtomicBoolean recreated = new AtomicBoolean(false);
    CountDownLatch done = new CountDownLatch(1);

    Platform.runLater(() -> {
      new ScrabbleGuiConfigDialog().showDialog(
          context.controller(),
          context.game(),
          payload::set,
          () -> recreated.set(true));
      done.countDown();
    });

    DialogPane pane = waitForDialogPane();
    robot.interact(() -> ((Button) pane.lookupButton(ButtonType.OK)).fire());

    assertTrue(done.await(3, TimeUnit.SECONDS));
    assertNotNull(payload.get());
    assertTrue(payload.get().contains("language=fr"));
    assertTrue(payload.get().contains("players=4"));
    assertTrue(payload.get().contains("super-scrabble=true"));
    assertTrue(payload.get().contains("blitz=true"));
    assertTrue(payload.get().contains("timeout=11"));
    assertTrue(payload.get().contains("ai-time=7"));
    assertTrue(payload.get().contains("ai-exptiminimax=true"));
    assertTrue(payload.get().contains("ai-ml=true"));
    assertTrue(payload.get().contains("dictionary=custom.dict"));
    assertFalse(recreated.get());
  }

  @Test
  void showDialogShouldDoNothingWhenCanceled(FxRobot robot) throws Exception {
    AtomicBoolean applied = new AtomicBoolean(false);
    AtomicBoolean recreated = new AtomicBoolean(false);
    CountDownLatch done = new CountDownLatch(1);

    Platform.runLater(() -> {
      new ScrabbleGuiConfigDialog().showDialog(
          context.controller(),
          context.game(),
          s -> applied.set(true),
          () -> recreated.set(true));
      done.countDown();
    });

    DialogPane pane = waitForDialogPane();
    robot.interact(() -> ((Button) pane.lookupButton(ButtonType.CANCEL)).fire());

    assertTrue(done.await(3, TimeUnit.SECONDS));
    assertFalse(applied.get());
    assertFalse(recreated.get());
  }

  @Test
  void showDialogShouldRecreateWhenApplyRestartIsChosen(FxRobot robot) throws Exception {
    AtomicBoolean applied = new AtomicBoolean(false);
    AtomicBoolean recreated = new AtomicBoolean(false);
    CountDownLatch done = new CountDownLatch(1);

    Platform.runLater(() -> {
      new ScrabbleGuiConfigDialog().showDialog(
          context.controller(),
          context.game(),
          s -> applied.set(true),
          () -> recreated.set(true));
      done.countDown();
    });

    DialogPane pane = waitForDialogPane();
    robot.interact(() -> {
      ButtonType applyRestart = pane.getButtonTypes().stream()
          .filter(bt -> bt.getButtonData() == ButtonBar.ButtonData.LEFT)
          .findFirst()
          .orElseThrow();
      ((Button) pane.lookupButton(applyRestart)).fire();
    });

    assertTrue(done.await(3, TimeUnit.SECONDS));
    assertTrue(applied.get());
    assertTrue(recreated.get());
  }

  private static ControllerGameContext createControllerWithConfig() {
    Game game = new Game();
    game.addPlayer(new HumanPlayer("P1", PlayerColor.BLUE));
    game.addPlayer(new HumanPlayer("P2", PlayerColor.RED));
    game.startGame();

    GameController controller = new GameController(game, new JavaFxView(game));
    controller.applyConfiguration("language", "fr");
    controller.applyConfiguration("players", "4");
    controller.applyConfiguration("super-scrabble", "true");
    controller.applyConfiguration("blitz", "true");
    controller.applyConfiguration("timeout", "11");
    controller.applyConfiguration("ai-time", "7");
    controller.applyConfiguration("ai-exptiminimax", "true");
    controller.applyConfiguration("ai-ml", "true");
    controller.applyConfiguration("dictionary", "custom.dict");

    return new ControllerGameContext(controller, game);
  }

  private record ControllerGameContext(GameController controller, Game game) {
  }

  private static DialogPane waitForDialogPane() throws Exception {
    for (int i = 0; i < 40; i++) {
      CountDownLatch latch = new CountDownLatch(1);
      AtomicReference<DialogPane> pane = new AtomicReference<>();
      Platform.runLater(() -> {
        DialogPane found = Window.getWindows().stream()
            .filter(Window::isShowing)
            .map(Window::getScene)
            .filter(scene -> scene != null && scene.getRoot() instanceof DialogPane)
            .map(scene -> (DialogPane) scene.getRoot())
            .findFirst()
            .orElse(null);
        pane.set(found);
        latch.countDown();
      });
      assertTrue(latch.await(1, TimeUnit.SECONDS));
      if (pane.get() != null) {
        return pane.get();
      }
      Thread.sleep(25);
    }
    throw new IllegalStateException("Dialog pane not found");
  }
}
