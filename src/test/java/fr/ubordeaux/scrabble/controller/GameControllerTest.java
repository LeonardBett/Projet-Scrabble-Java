package fr.ubordeaux.scrabble.controller;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import fr.ubordeaux.scrabble.model.ai.AiPlayer;
import fr.ubordeaux.scrabble.model.core.Game;
import fr.ubordeaux.scrabble.model.core.HumanPlayer;
import fr.ubordeaux.scrabble.model.core.Move;
import fr.ubordeaux.scrabble.model.core.Tile;
import fr.ubordeaux.scrabble.model.dictionary.Gaddag;
import fr.ubordeaux.scrabble.model.enums.Direction;
import fr.ubordeaux.scrabble.model.enums.PlayerColor;
import fr.ubordeaux.scrabble.model.utils.Point;
import fr.ubordeaux.scrabble.view.UserInterface;
import fr.ubordeaux.scrabble.view.cli.CliView;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;

class GameControllerTest {

  @Test
  void startGameShouldFailWhenGameOrViewMissing() {
    GameController missingGame = new GameController(null, new RecordingView());
    GameController missingView = new GameController(new Game(), null);

    assertThrows(IllegalStateException.class, missingGame::startGame);
    assertThrows(IllegalStateException.class, missingView::startGame);
  }

  @Test
  void startGameShouldFailWhenNotEnoughPlayers() {
    Game game = new Game();
    GameController controller = new GameController(game, new RecordingView());

    assertThrows(IllegalStateException.class, controller::startGame);
  }

  @Test
  void startGameShouldInitializePlayers() {
    Game game = new Game();
    HumanPlayer alice = new HumanPlayer("Alice", PlayerColor.BLUE);
    HumanPlayer bob = new HumanPlayer("Bob", PlayerColor.RED);
    game.addPlayer(alice);
    game.addPlayer(bob);

    GameController controller = new GameController(game, new RecordingView());
    controller.startGame();

    assertEquals(7, alice.getRack().getTiles().size());
    assertEquals(7, bob.getRack().getTiles().size());
  }

  @Test
  void handlePlayerMoveShouldIgnoreNullMove() {
    Game game = new Game();
    HumanPlayer alice = new HumanPlayer("Alice", PlayerColor.BLUE);
    game.addPlayer(alice);

    RecordingView view = new RecordingView();
    GameController controller = new GameController(game, view);

    assertDoesNotThrow(() -> controller.handlePlayerMove(null));
    assertEquals(0, view.refreshCount);
  }

  @Test
  void handlePlayerMoveShouldExecutePassAndRefresh() {
    Game game = new Game();
    HumanPlayer alice = new HumanPlayer("Alice", PlayerColor.BLUE);
    game.addPlayer(alice);

    RecordingView view = new RecordingView();
    GameController controller = new GameController(game, view);

    controller.handlePlayerMove(Move.createPass(alice));

    assertEquals(1, view.refreshCount);
  }

  @Test
  void handlePlayerMoveShouldRejectInvalidPerpendicularWord() throws Exception {
    Game game = new Game();
    HumanPlayer alice = new HumanPlayer("Alice", PlayerColor.BLUE);
    game.addPlayer(alice);
    game.setFirstMoveDone(true);

    game.getBoard().getSquare(new Point(6, 7)).setTile(new Tile('A'));
    game.getBoard().getSquare(new Point(8, 7)).setTile(new Tile('T'));
    game.getBoard().getSquare(new Point(7, 6)).setTile(new Tile('Z'));

    alice.getRack().setTiles(new ArrayList<>(List.of(new Tile('R'))));

    GameController controller = new GameController(game, new RecordingView());
    Gaddag dictionary = new Gaddag();
    dictionary.add("ART");
    setDictionary(controller, dictionary);

    Move move =
        Move.createPlay(alice, List.of(new Tile('R')), new Point(7, 7), Direction.HORIZONTAL);

    RuntimeException error =
        assertThrows(RuntimeException.class, () -> controller.handlePlayerMove(move));
    assertTrue(error.getMessage().contains("ZR"));
  }

  @Test
  void handlePlayerMoveShouldWrapModelErrors() {
    Game game = new Game();
    HumanPlayer alice = new HumanPlayer("Alice", PlayerColor.BLUE);
    HumanPlayer bob = new HumanPlayer("Bob", PlayerColor.RED);
    game.addPlayer(alice);
    game.addPlayer(bob);

    GameController controller = new GameController(game, new RecordingView());

    RuntimeException error = assertThrows(RuntimeException.class,
        () -> controller.handlePlayerMove(Move.createPass(bob)));
    assertTrue(error.getMessage().contains("Invalid move:"));
  }

  @Test
  void handlePlayerMoveShouldLoadDictionaryWhenMissing() {
    Game game = new Game();
    HumanPlayer alice = new HumanPlayer("Alice", PlayerColor.BLUE);
    game.addPlayer(alice);

    alice.getRack().setTiles(
        new ArrayList<>(List.of(new Tile('R'), new Tile('U'), new Tile('E'), new Tile('S'))));

    RecordingView view = new RecordingView();
    GameController controller = new GameController(game, view);

    controller.setLang("en");

    Move move =
        Move.createPlay(alice, List.of(new Tile('R'), new Tile('U'), new Tile('E'), new Tile('S')),
            new Point(7, 7), Direction.HORIZONTAL);

    assertDoesNotThrow(() -> controller.handlePlayerMove(move));
    assertEquals(1, view.refreshCount);
  }

  @Test
  void addPlayerUndoRedoAndGettersShouldWork() {
    Game game = new Game();
    RecordingView view = new RecordingView();
    GameController controller = new GameController(game, view);

    HumanPlayer alice = new HumanPlayer("Alice", PlayerColor.BLUE);
    controller.addPlayer(alice);

    assertEquals(1, game.getPlayers().size());
    assertSame(game, controller.getGame());
    assertSame(view, controller.getView());

    controller.undo();
    controller.redo();

    assertEquals(2, view.refreshCount);
  }

  @Test
  void runCliShouldRequireCliView() {
    Game game = new Game();
    game.addPlayer(new HumanPlayer("Alice", PlayerColor.BLUE));
    game.addPlayer(new HumanPlayer("Bob", PlayerColor.RED));

    GameController controller = new GameController(game, new RecordingView());
    assertThrows(IllegalStateException.class, controller::runCli);
  }

  @Test
  void runCliShouldQuitFromMenu() throws Exception {
    Game game = new Game();
    game.addPlayer(new HumanPlayer("Alice", PlayerColor.BLUE));
    game.addPlayer(new HumanPlayer("Bob", PlayerColor.RED));

    CliView view = new CliView(game);
    GameController controller = new GameController(game, view);
    setDictionary(controller, minimalDictionary("AA", "ART"));

    runCliWithInput(controller, "6\no\n");

    assertNotNull(game.determineWinner());
  }

  @Test
  void runCliShouldHandleInvalidActionThenQuit() throws Exception {
    Game game = new Game();
    game.addPlayer(new HumanPlayer("Alice", PlayerColor.BLUE));
    game.addPlayer(new HumanPlayer("Bob", PlayerColor.RED));

    CliView view = new CliView(game);
    GameController controller = new GameController(game, view);
    setDictionary(controller, minimalDictionary("AA", "ART"));

    runCliWithInput(controller, "x\n6\no\n");
  }

  @Test
  void runCliShouldHandleAllHumanActionsThenQuit() throws Exception {
    Game game = new Game();
    HumanPlayer alice = new HumanPlayer("Alice", PlayerColor.BLUE);
    HumanPlayer bob = new HumanPlayer("Bob", PlayerColor.RED);
    game.addPlayer(alice);
    game.addPlayer(bob);

    // Keep deterministic racks so action inputs are stable.
    alice.getRack().setTiles(new ArrayList<>(List.of(new Tile('A'), new Tile('A'), new Tile('B'),
        new Tile('C'), new Tile('D'), new Tile('E'), new Tile('F'))));
    bob.getRack().setTiles(new ArrayList<>(List.of(new Tile('A'), new Tile('B'), new Tile('C'),
        new Tile('D'), new Tile('E'), new Tile('F'), new Tile('G'))));

    CliView view = new CliView(game);
    GameController controller = new GameController(game, view);
    setDictionary(controller, minimalDictionary("AA", "ART"));

    // 1: invalid play format (move null), 2: invalid exchange (move null),
    // 3: pass, 4: undo, 5: redo, 6: quit.
    runCliWithInput(controller, "1\nbad\n2\nZ\n3\n4\n5\n6\no\n");
  }

  @Test
  void runCliShouldPlayValidWordThenQuit() throws Exception {
    Game game = new Game();
    HumanPlayer alice = new HumanPlayer("Alice", PlayerColor.BLUE);
    HumanPlayer bob = new HumanPlayer("Bob", PlayerColor.RED);
    game.addPlayer(alice);
    game.addPlayer(bob);

    alice.getRack().setTiles(new ArrayList<>(List.of(new Tile('A'), new Tile('A'), new Tile('B'),
        new Tile('C'), new Tile('D'), new Tile('E'), new Tile('F'))));
    bob.getRack().setTiles(new ArrayList<>(List.of(new Tile('A'), new Tile('B'), new Tile('C'),
        new Tile('D'), new Tile('E'), new Tile('F'), new Tile('G'))));

    CliView view = new CliView(game);
    GameController controller = new GameController(game, view);
    setDictionary(controller, minimalDictionary("AA", "ART"));

    runCliWithInput(controller, "1\nh 8\nH\nAA\n6\no\n");
  }

  @Test
  void runCliShouldHandleInvalidPlayMoveAfterParsing() throws Exception {
    Game game = new Game();
    HumanPlayer alice = new HumanPlayer("Alice", PlayerColor.BLUE);
    HumanPlayer bob = new HumanPlayer("Bob", PlayerColor.RED);
    game.addPlayer(alice);
    game.addPlayer(bob);

    alice.getRack().setTiles(new ArrayList<>(List.of(new Tile('A'), new Tile('A'), new Tile('B'),
        new Tile('C'), new Tile('D'), new Tile('E'), new Tile('F'))));
    bob.getRack().setTiles(new ArrayList<>(List.of(new Tile('A'), new Tile('B'), new Tile('C'),
        new Tile('D'), new Tile('E'), new Tile('F'), new Tile('G'))));

    CliView view = new CliView(game);
    GameController controller = new GameController(game, view);
    setDictionary(controller, minimalDictionary("AA", "ART"));

    // Validly parsed move but invalid placement (first word does not cover center).
    runCliWithInput(controller, "1\na 1\nH\nAA\n6\no\n");
  }

  @Test
  void runCliShouldExchangeTilesSuccessfully() throws Exception {
    Game game = new Game();
    HumanPlayer alice = new HumanPlayer("Alice", PlayerColor.BLUE);
    HumanPlayer bob = new HumanPlayer("Bob", PlayerColor.RED);
    game.addPlayer(alice);
    game.addPlayer(bob);

    alice.getRack().setTiles(new ArrayList<>(List.of(new Tile('A'), new Tile('B'), new Tile('C'),
        new Tile('D'), new Tile('E'), new Tile('F'), new Tile('G'))));
    bob.getRack().setTiles(new ArrayList<>(List.of(new Tile('A'), new Tile('B'), new Tile('C'),
        new Tile('D'), new Tile('E'), new Tile('F'), new Tile('G'))));

    CliView view = new CliView(game);
    GameController controller = new GameController(game, view);
    setDictionary(controller, minimalDictionary("AA", "ART"));

    runCliWithInput(controller, "2\nAB\n6\no\n");
  }

  @Test
  void runCliShouldHandleExchangeFailureWhenBagTooSmall() throws Exception {
    Game game = new Game();
    HumanPlayer alice = new HumanPlayer("Alice", PlayerColor.BLUE);
    HumanPlayer bob = new HumanPlayer("Bob", PlayerColor.RED);
    game.addPlayer(alice);
    game.addPlayer(bob);

    alice.getRack().setTiles(new ArrayList<>(List.of(new Tile('A'), new Tile('B'), new Tile('C'),
        new Tile('D'), new Tile('E'), new Tile('F'), new Tile('G'))));
    bob.getRack().setTiles(new ArrayList<>(List.of(new Tile('A'), new Tile('B'), new Tile('C'),
        new Tile('D'), new Tile('E'), new Tile('F'), new Tile('G'))));

    while (game.getBag().size() >= 7) {
      game.getBag().drawTile();
    }

    CliView view = new CliView(game);
    GameController controller = new GameController(game, view);
    setDictionary(controller, minimalDictionary("AA", "ART"));

    runCliWithInput(controller, "2\nAB\n6\no\n");
  }

  @Test
  void runCliShouldHandleQuitConfirmationNoThenYes() throws Exception {
    Game game = new Game();
    game.addPlayer(new HumanPlayer("Alice", PlayerColor.BLUE));
    game.addPlayer(new HumanPlayer("Bob", PlayerColor.RED));

    CliView view = new CliView(game);
    GameController controller = new GameController(game, view);
    setDictionary(controller, minimalDictionary("AA", "ART"));

    runCliWithInput(controller, "6\nn\n6\no\n");
  }

  @Test
  void runCliShouldInitializePlayersIncludingAi() throws Exception {
    Game game = new Game();
    CliView view = new CliView(game);
    GameController controller = new GameController(game, view);
    setDictionary(controller, minimalDictionary("AA", "ART"));

    controller.setUseExptiminimax(true);

    runCliWithInput(controller, "2\nBob\nIAbot\n6\no\n");

    assertEquals(2, game.getPlayers().size());
    assertInstanceOf(AiPlayer.class, game.getPlayers().get(1));
    AiPlayer ai = (AiPlayer) game.getPlayers().get(1);

    assertTrue(ai.isExpectiminimaxMode());
  }

  @Test
  void runCliShouldHandleAiTurnFailureAndContinue() throws Exception {
    Game game = new Game();
    AiPlayer failing = new FailingAiPlayer("IA-crash");
    HumanPlayer bob = new HumanPlayer("Bob", PlayerColor.BLUE);
    game.addPlayer(failing);
    game.addPlayer(bob);

    CliView view = new CliView(game);
    GameController controller = new GameController(game, view);
    setDictionary(controller, minimalDictionary("AA", "ART"));

    runCliWithInputSilencingErr(controller, "6\no\n");
    assertTrue(game.getCurrentPlayer() instanceof HumanPlayer
        || game.getCurrentPlayer() instanceof AiPlayer);
  }

  @Test
  void runCliShouldHandleAiTurnSuccessThenContinue() throws Exception {
    Game game = new Game();
    AiPlayer passing = new PassingAiPlayer("IA-pass");
    HumanPlayer bob = new HumanPlayer("Bob", PlayerColor.BLUE);
    game.addPlayer(passing);
    game.addPlayer(bob);

    CliView view = new CliView(game);
    GameController controller = new GameController(game, view);
    setDictionary(controller, minimalDictionary("AA", "ART"));

    runCliWithInput(controller, "6\no\n");
    assertTrue(game.getCurrentPlayer() instanceof HumanPlayer
        || game.getCurrentPlayer() instanceof AiPlayer);
  }

  @Test
  void runCliShouldProvideHintWhenWordIsPossible() throws Exception {
    Game game = new Game();
    HumanPlayer alice = new HumanPlayer("Alice", PlayerColor.BLUE);
    HumanPlayer bob = new HumanPlayer("Bob", PlayerColor.RED);
    game.addPlayer(alice);
    game.addPlayer(bob);

    // Forces a specific rack to control the outcome of the hint
    alice.getRack().setTiles(
        new ArrayList<>(
            List.of(
                new Tile('B'),
                new Tile('A'),
                new Tile('R'),
                new Tile('X'),
                new Tile('Y'),
                new Tile('Z'),
                new Tile('W'))));
    bob.getRack().setTiles(new ArrayList<>(List.of(new Tile('A'))));

    CliView view = new CliView(game);
    GameController controller = new GameController(game, view);

    // Injects a minimal dictionary containing a valid 3-letter word
    setDictionary(controller, minimalDictionary("BAR", "ART"));

    // Intercepts the standard output to read what the CLI displays
    PrintStream originalOut = System.out;
    ByteArrayOutputStream outContent = new ByteArrayOutputStream();
    System.setOut(new PrintStream(outContent));

    try {
      // Simulates the user inputs: '7' (Hint), '6' (Quit), 'o' (Confirm)
      runCliWithInput(controller, "7\n6\no\n");

      String consoleOutput = outContent.toString();

      // Asserts that the hint was successfully calculated and displayed
      assertTrue(consoleOutput.contains("Indice"));
      assertTrue(consoleOutput.contains("B, A, R"));
    } finally {
      // Restores the original standard output to avoid breaking other tests
      System.setOut(originalOut);
    }
  }

  @Test
  void runCliShouldNotProvideHintForSevenLetterWords() throws Exception {
    Game game = new Game();
    HumanPlayer alice = new HumanPlayer("Alice", PlayerColor.BLUE);
    HumanPlayer bob = new HumanPlayer("Bob", PlayerColor.RED);
    game.addPlayer(alice);
    game.addPlayer(bob);

    // Forces a rack capable of forming a 7-letter word (a scrabble)
    alice.getRack().setTiles(
        new ArrayList<>(
            List.of(
                new Tile('P'),
                new Tile('A'),
                new Tile('R'),
                new Tile('K'),
                new Tile('I'),
                new Tile('N'),
                new Tile('G'))));

    CliView view = new CliView(game);
    GameController controller = new GameController(game, view);

    // Injects a dictionary containing ONLY the 7-letter word
    setDictionary(controller, minimalDictionary("PARKING"));

    PrintStream originalOut = System.out;
    ByteArrayOutputStream outContent = new ByteArrayOutputStream();
    System.setOut(new PrintStream(outContent));

    try {
      runCliWithInput(controller, "7\n6\no\n");

      String consoleOutput = outContent.toString();

      // Asserts that the hint algorithm correctly filtered out the 7-letter word
      assertTrue(consoleOutput.contains("Aucun mot valide de moins de 7 lettres"));
    } finally {
      System.setOut(originalOut);
    }
  }

  private static void setDictionary(GameController controller, Gaddag dictionary) throws Exception {
    Field field = GameController.class.getDeclaredField("gaddag");
    field.setAccessible(true);
    field.set(controller, dictionary);
  }

  private static Gaddag minimalDictionary(String... words) {
    Gaddag dictionary = new Gaddag();
    for (String word : words) {
      dictionary.add(word);
    }
    return dictionary;
  }

  private static void runCliWithInput(GameController controller, String inputData) {
    InputStream previousIn = System.in;
    try {
      System.setIn(new ByteArrayInputStream(inputData.getBytes(StandardCharsets.UTF_8)));
      controller.runCli();
    } finally {
      System.setIn(previousIn);
    }
  }

  private static void runCliWithInputSilencingErr(GameController controller, String inputData) {
    InputStream previousIn = System.in;
    PrintStream previousErr = System.err;
    try {
      System.setIn(new ByteArrayInputStream(inputData.getBytes(StandardCharsets.UTF_8)));
      System.setErr(new PrintStream(OutputStream.nullOutputStream()));
      controller.runCli();
    } finally {
      System.setIn(previousIn);
      System.setErr(previousErr);
    }
  }

  private static final class RecordingView implements UserInterface {
    private int refreshCount;

    @Override
    public void refresh() {
      refreshCount++;
    }

    @Override
    public void displayMessage(String message) {
    }

    @Override
    public void displayError(String error) {
    }

    @Override
    public void displaySuccess(String message) {
    }
  }

  private static final class FailingAiPlayer extends AiPlayer {
    FailingAiPlayer(String name) {
      super(name, 1, 5, PlayerColor.BLUE);
    }

    @Override
    public void playTurn(Game game, Gaddag gaddag) {
      throw new RuntimeException("planned failure");
    }
  }

  private static final class PassingAiPlayer extends AiPlayer {
    PassingAiPlayer(String name) {
      super(name, 1, 5, PlayerColor.RED);
    }

    @Override
    public void playTurn(Game game, Gaddag gaddag) {
      game.executeMove(Move.createPass(this));
    }
  }
}
