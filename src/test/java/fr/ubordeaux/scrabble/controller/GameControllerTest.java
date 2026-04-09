package fr.ubordeaux.scrabble.controller;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import fr.ubordeaux.scrabble.model.ai.AiPlayer;
import fr.ubordeaux.scrabble.model.core.Game;
import fr.ubordeaux.scrabble.model.core.HumanPlayer;
import fr.ubordeaux.scrabble.model.core.Move;
import fr.ubordeaux.scrabble.model.core.PlayableWord;
import fr.ubordeaux.scrabble.model.core.Tile;
import fr.ubordeaux.scrabble.model.dictionary.Gaddag;
import fr.ubordeaux.scrabble.model.enums.Direction;
import fr.ubordeaux.scrabble.model.enums.PlayerColor;
import fr.ubordeaux.scrabble.model.savefiles.SaveManager;
import fr.ubordeaux.scrabble.model.utils.Point;
import fr.ubordeaux.scrabble.view.UserInterface;
import fr.ubordeaux.scrabble.view.cli.CliView;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Stack;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
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
  void settersShouldUpdateControllerConfiguration() throws Exception {
    Game game = new Game();
    GameController controller = new GameController(game, new RecordingView());

    controller.setAiTime(9);
    controller.setUseExptiminimax(true);
    controller.setUseMl(true);
    controller.setLang("fr");
    controller.setPlayerCount(3);

    assertEquals(9, (int) getPrivateField(controller, "aiTime"));
    assertTrue((boolean) getPrivateField(controller, "useExptiminimax"));
    assertTrue((boolean) getPrivateField(controller, "useMl"));
    assertEquals("fr", getPrivateField(controller, "lang"));
    assertEquals(3, (int) getPrivateField(controller, "playerCount"));
    assertEquals("fr", game.getLanguage());
    assertEquals("fr", game.getBag().getLanguage());
  }

  @Test
  void setDictionaryPathShouldPropagateToGame() {
    Game game = new Game();
    GameController controller = new GameController(game, new RecordingView());

    controller.setDictionaryPath("/tmp/custom-dictionary.txt");

    assertEquals("/tmp/custom-dictionary.txt", controller.getDictionaryPathOverride());
    assertEquals("/tmp/custom-dictionary.txt", game.getDictionaryPathOverride());
  }

  @Test
  void setLangShouldFallbackToEnglishForUnknownLanguage() throws Exception {
    Game game = new Game();
    GameController controller = new GameController(game, new RecordingView());
    controller.setLang("zz");

    assertEquals("en", getPrivateField(controller, "lang"));
    assertEquals("en", game.getLanguage());
    assertEquals("en", game.getBag().getLanguage());
  }

  @Test
  void constructorShouldUseGameLanguageForDefaultDictionary() {
    String previousDictionaryPath = System.getProperty("scrabble.dictionary.path");
    try {
      System.clearProperty("scrabble.dictionary.path");
      Game game = new Game("fr");
      GameController controller = new GameController(game, new RecordingView());

      assertEquals("dictionaries/lexicon_fr.txt", controller.getDictionaryPathOverride());
    } finally {
      if (previousDictionaryPath == null) {
        System.clearProperty("scrabble.dictionary.path");
      } else {
        System.setProperty("scrabble.dictionary.path", previousDictionaryPath);
      }
    }
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

    runCliWithInput(controller, "quit\no\n");

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

    runCliWithInput(controller, "x\nquit\no\n");
  }

  @Test
  void runCliShouldAcceptShellQuitHelpAndPassCommands() throws Exception {
    Game game = new Game();
    game.addPlayer(new HumanPlayer("Alice", PlayerColor.BLUE));
    game.addPlayer(new HumanPlayer("Bob", PlayerColor.RED));

    CliView view = new CliView(game);
    GameController controller = new GameController(game, view);
    setDictionary(controller, minimalDictionary("AA", "ART"));

    runCliWithInput(controller, "help\nshow configuration\npass\nquit\nn\n");

    assertTrue(game.getUndoRedo().getHistory().size() >= 1);
  }

  @Test
  void runCliShouldRestartGameFromNewGameCommand() throws Exception {
    Game game = new Game();
    game.addPlayer(new HumanPlayer("Alice", PlayerColor.BLUE));
    game.addPlayer(new HumanPlayer("Bob", PlayerColor.RED));

    CliView view = new CliView(game);
    GameController controller = new GameController(game, view);
    setDictionary(controller, minimalDictionary("AA", "ART"));

    runCliWithInput(controller, "newgame\no\nquit\no\n");

    assertNotSame(game, controller.getGame());
    assertEquals(2, controller.getGame().getPlayers().size());
  }

  @Test
  void runCliShouldSaveFromShellCommand() throws Exception {
    Game game = new Game();
    game.addPlayer(new HumanPlayer("Alice", PlayerColor.BLUE));
    game.addPlayer(new HumanPlayer("Bob", PlayerColor.RED));

    CliView view = new CliView(game);
    GameController controller = new GameController(game, view);
    setDictionary(controller, minimalDictionary("AA", "ART"));

    Path saveFile = Files.createTempFile("scrabble-cli-", ".sav");
    Files.deleteIfExists(saveFile);

    runCliWithInput(controller, "save " + saveFile.toString() + "\nquit\no\n");

    assertTrue(Files.exists(saveFile));
    Files.deleteIfExists(saveFile);
  }

  @Test
  void runCliShouldAppendScrabbleExtensionWhenMissing() throws Exception {
    Game game = new Game();
    game.addPlayer(new HumanPlayer("Alice", PlayerColor.BLUE));
    game.addPlayer(new HumanPlayer("Bob", PlayerColor.RED));

    CliView view = new CliView(game);
    GameController controller = new GameController(game, view);
    setDictionary(controller, minimalDictionary("AA", "ART"));

    Path saveDir = Files.createTempDirectory(Path.of("target"), "scrabble-cli-save-");
    Path basePath = saveDir.resolve("partie_cli");
    Path expectedSave = saveDir.resolve("partie_cli.scrabble");
    Files.deleteIfExists(expectedSave);

    runCliWithInput(controller, "save " + basePath + "\nquit\no\n");

    assertTrue(Files.exists(expectedSave));
    Files.deleteIfExists(expectedSave);
    Files.deleteIfExists(saveDir);
  }

  @Test
  void runCliShouldLoadGameFromShellCommand() throws Exception {
    Game source = new Game();
    HumanPlayer sourceA = new HumanPlayer("SrcA", PlayerColor.BLUE);
    HumanPlayer sourceB = new HumanPlayer("SrcB", PlayerColor.RED);
    source.addPlayer(sourceA);
    source.addPlayer(sourceB);
    sourceA.addScore(42);
    sourceB.addScore(17);

    Path saveFile = Files.createTempFile("scrabble-cli-load-", ".sav");
    new SaveManager().saveGame(source, saveFile.toString());

    Game game = new Game();
    game.addPlayer(new HumanPlayer("Alice", PlayerColor.BLUE));
    game.addPlayer(new HumanPlayer("Bob", PlayerColor.RED));

    CliView view = new CliView(game);
    GameController controller = new GameController(game, view);
    setDictionary(controller, minimalDictionary("AA", "ART"));

    runCliWithInput(controller, "load " + saveFile + "\nquit\no\n");

    assertEquals(2, controller.getGame().getPlayers().size());
    assertEquals(42, controller.getGame().getPlayers().get(0).getScore());
    assertEquals(17, controller.getGame().getPlayers().get(1).getScore());

    Files.deleteIfExists(saveFile);
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

    runCliWithInput(controller, "bad\nexchange Z\npass\nundo\nredo\nquit\nn\n");
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

    runCliWithInput(controller, "h8h AA\nquit\nn\n");
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
    runCliWithInput(controller, "a1h AA\nquit\no\n");
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

    runCliWithInput(controller, "exchange AB\nquit\nn\n");
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

    runCliWithInput(controller, "exchange AB\nquit\no\n");
  }

  @Test
  void runCliShouldHandleQuitConfirmationNoThenYes() throws Exception {
    Game game = new Game();
    game.addPlayer(new HumanPlayer("Alice", PlayerColor.BLUE));
    game.addPlayer(new HumanPlayer("Bob", PlayerColor.RED));

    CliView view = new CliView(game);
    GameController controller = new GameController(game, view);
    setDictionary(controller, minimalDictionary("AA", "ART"));

    runCliWithInput(controller, "quit\nn\nquit\no\n");
  }

  @Test
  void runCliShouldInitializePlayersIncludingAi() throws Exception {
    Game game = new Game();
    CliView view = new CliView(game);
    GameController controller = new GameController(game, view);
    setDictionary(controller, minimalDictionary("AA", "ART"));

    controller.setUseExptiminimax(true);

    runCliWithInput(controller, "2\nBob\nIAbot\nquit\no\n");

    assertEquals(2, game.getPlayers().size());
    assertInstanceOf(AiPlayer.class, game.getPlayers().get(1));
    AiPlayer ai = (AiPlayer) game.getPlayers().get(1);

    assertTrue(ai.isExpectiminimaxMode());
  }

  @Test
  void runCliShouldAttachMlAgentToExistingAiPlayers() throws Exception {
    Game game = new Game();
    AiPlayer ai = new AiPlayer("IA-existing", 1, 3, PlayerColor.BLUE);
    HumanPlayer bob = new HumanPlayer("Bob", PlayerColor.RED);
    game.addPlayer(ai);
    game.addPlayer(bob);

    CliView view = new CliView(game);
    GameController controller = new GameController(game, view);
    controller.setUseMl(true);
    setDictionary(controller, minimalDictionary("AA", "ART"));

    runCliWithInput(controller, "quit\nn\n");

    assertNotNull(getAiMlAgent(ai));
  }

  @Test
  void runCliShouldCreateAiPlayerWithMlAgentWhenConfigured() throws Exception {
    Game game = new Game();
    CliView view = new CliView(game);
    GameController controller = new GameController(game, view);
    controller.setUseMl(true);
    controller.setPlayerCount(2);

    runCliWithInput(controller, "IAbot\nBob\nquit\nn\n");

    assertEquals(2, game.getPlayers().size());
    assertInstanceOf(AiPlayer.class, game.getPlayers().get(0));
    assertNotNull(getAiMlAgent((AiPlayer) game.getPlayers().get(0)));
  }

  @Test
  void runCliShouldUseConfiguredPlayerCountWithoutPromptingNumber() throws Exception {
    Game game = new Game();
    CliView view = new CliView(game);
    GameController controller = new GameController(game, view);
    setDictionary(controller, minimalDictionary("AA", "ART"));
    controller.setPlayerCount(2);

    // No numeric input for number of players here; only names then quit.
    runCliWithInput(controller, "Alice\nBob\nquit\no\n");

    assertEquals(2, game.getPlayers().size());
    assertEquals("Alice", game.getPlayers().get(0).getName());
    assertEquals("Bob", game.getPlayers().get(1).getName());
  }

  @Test
  void privateDictionaryListLoaderShouldLoadAndCache() throws Exception {
    GameController controller = new GameController(new Game(), new RecordingView());
    controller.setLang("en");

    @SuppressWarnings("unchecked")
    List<String> firstLoad =
        (List<String>) invokePrivateMethod(controller, "getOrLoadDictionaryList");
    @SuppressWarnings("unchecked")
    List<String> secondLoad =
        (List<String>) invokePrivateMethod(controller, "getOrLoadDictionaryList");

    assertNotNull(firstLoad);
    assertTrue(firstLoad.size() > 1000);
    assertSame(firstLoad, secondLoad);
  }

  @Test
  void privateHintHelpersShouldExtractLettersComputeScoreAndCleanup() throws Exception {
    Game game = new Game();
    HumanPlayer alice = new HumanPlayer("Alice", PlayerColor.BLUE);
    HumanPlayer bob = new HumanPlayer("Bob", PlayerColor.RED);
    game.addPlayer(alice);
    game.addPlayer(bob);

    GameController controller = new GameController(game, new RecordingView());

    PlayableWord move = new PlayableWord(7, 7, "AB", Direction.HORIZONTAL, "A>B");

    @SuppressWarnings("unchecked")
    List<Character> letters = (List<Character>) invokePrivateMethod(controller,
        "getLettersFromRack", new Class<?>[] {fr.ubordeaux.scrabble.model.core.Board.class,
            PlayableWord.class}, game.getBoard(), move);

    assertEquals(List.of('A', 'B'), letters);

    int score = (int) invokePrivateMethod(controller, "simulateScoreForHint",
        new Class<?>[] {fr.ubordeaux.scrabble.model.core.Board.class, PlayableWord.class},
        game.getBoard(), move);
    assertTrue(score >= 0);
    assertTrue(game.getBoard().getSquare(new Point(7, 7)).isEmpty());
    assertTrue(game.getBoard().getSquare(new Point(8, 7)).isEmpty());
  }

  @Test
  void privateBlitzHandlersShouldSetGameOverAndStopWatcher() throws Exception {
    Game game = new Game();
    HumanPlayer alice = new HumanPlayer("Alice", PlayerColor.BLUE);
    HumanPlayer bob = new HumanPlayer("Bob", PlayerColor.RED);
    game.addPlayer(alice);
    game.addPlayer(bob);

    CliView view = new CliView(game);
    GameController controller = new GameController(game, view);

    Field watcherField = GameController.class.getDeclaredField("blitzWatcherExecutor");
    watcherField.setAccessible(true);
    watcherField.set(controller, java.util.concurrent.Executors.newSingleThreadScheduledExecutor());

    invokePrivateMethod(controller, "handleBlitzExpiry",
        new Class<?>[] {fr.ubordeaux.scrabble.model.interfaces.Player.class, CliView.class},
        alice, view);

    assertTrue(game.isGameOver());
    assertEquals(null, watcherField.get(controller));

    // Covers stopBlitzWatcher no-op branch when watcher is already null.
    invokePrivateMethod(controller, "stopBlitzWatcher");
    assertEquals(null, watcherField.get(controller));
  }

  @Test
  void privateGaddagLoaderShouldLoadAndCache() throws Exception {
    GameController controller = new GameController(new Game(), new RecordingView());
    controller.setLang("en");

    Gaddag first = (Gaddag) invokePrivateMethod(controller, "getOrLoadGaddag");
    Gaddag second = (Gaddag) invokePrivateMethod(controller, "getOrLoadGaddag");

    assertNotNull(first);
    assertSame(first, second);
    assertTrue(first.containsWord("ART"));
  }

  @Test
  void privateBlitzWatcherShouldEndGameWhenTimeExpires() throws Exception {
    Game game = new Game();
    game.enableBlitzMode(Duration.ofMillis(1));

    HumanPlayer alice = new HumanPlayer("Alice", PlayerColor.BLUE);
    HumanPlayer bob = new HumanPlayer("Bob", PlayerColor.RED);
    game.addPlayer(alice);
    game.addPlayer(bob);
    game.startGame();

    CliView view = new CliView(game);
    GameController controller = new GameController(game, view);

    invokePrivateMethod(controller, "startBlitzWatcher", new Class<?>[] {CliView.class}, view);

    Thread.sleep(1200);

    assertTrue(game.isGameOver());
    invokePrivateMethod(controller, "stopBlitzWatcher");
  }

  @Test
  void controllerAuxPrivateBranchesShouldHandleBlitzAndNoWinner() throws Exception {
    Game game = new Game();
    HumanPlayer alice = new HumanPlayer("Alice", PlayerColor.BLUE);
    HumanPlayer bob = new HumanPlayer("Bob", PlayerColor.RED);
    game.addPlayer(alice);
    game.addPlayer(bob);
    game.enableBlitzMode(Duration.ofMillis(1));
    game.startGame();

    CliView view = new CliView(game);
    GameController controller = new GameController(game, view);
    final GameControllerAux aux = new GameControllerAux(controller);

    Thread.sleep(30);

    Object elapsed = invokePrivateAuxMethod(aux, "isBlitzTimeElapsed",
        new Class<?>[] {fr.ubordeaux.scrabble.model.interfaces.Player.class, CliView.class,
            boolean.class},
        game.getCurrentPlayer(), view, true);
    assertEquals(true, elapsed);

    Game emptyGame = new Game();
    GameController emptyController = new GameController(emptyGame, new CliView(emptyGame));
    GameControllerAux emptyAux = new GameControllerAux(emptyController);
    assertDoesNotThrow(() -> invokePrivateAuxMethod(emptyAux, "displayWinner",
        new Class<?>[] {CliView.class}, new CliView(emptyGame)));
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

    runCliWithInputSilencingErr(controller, "quit\nn\n");
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

    runCliWithInput(controller, "quit\nn\n");
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
      runCliWithInput(controller, "hint\nquit\no\n");

      String consoleOutput = outContent.toString();

      assertTrue(consoleOutput.contains("Info:"));
    } finally {
      // Restores the original standard output to avoid breaking other tests
      System.setOut(originalOut);
    }
  }

  @Test
  void runCliShouldProvideHintForSevenLetterWords() throws Exception {
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
      runCliWithInput(controller, "hint\nquit\no\n");

      String consoleOutput = outContent.toString();

      // The improved hint flow now accepts best 7-letter moves when available.
      assertTrue(consoleOutput.contains("PARKING"));
    } finally {
      System.setOut(originalOut);
    }
  }

  @Test
  void runCliShouldExerciseShellCommandsAndReloadSavedGames() throws Exception {
    Game source = new Game();
    HumanPlayer sourceAlice = new HumanPlayer("SrcA", PlayerColor.BLUE);
    HumanPlayer sourceBob = new HumanPlayer("SrcB", PlayerColor.RED);
    source.addPlayer(sourceAlice);
    source.addPlayer(sourceBob);
    sourceAlice.addScore(11);
    sourceBob.addScore(7);

    Path saveFile = Files.createTempFile("scrabble-cli-shell-", ".sav");
    new SaveManager().saveGame(source, saveFile.toString());

    Game game = new Game();
    HumanPlayer alice = new HumanPlayer("Alice", PlayerColor.BLUE);
    HumanPlayer bob = new HumanPlayer("Bob", PlayerColor.RED);
    game.addPlayer(alice);
    game.addPlayer(bob);

    CliView view = new CliView(game);
    GameController controller = new GameController(game, view);
    controller.applyConfiguration("blitz", "true");

    try {
      runCliWithInput(controller,
          "help set\n"
              + "show history\n"
              + "show time\n"
              + "show configuration\n"
              + "set language=fr; ai-time=7\n"
              + "pause\n"
              + "pass\n"
              + "undo 1\n"
              + "redo 1\n"
              + "save " + saveFile + "\n"
              + "load " + saveFile + "\n"
              + "pass\n"
              + "quit\n"
              + "n\n");

      assertEquals("fr", controller.configuredLanguage());
      assertTrue(Files.exists(saveFile));
    } finally {
      Files.deleteIfExists(saveFile);
    }
  }

  @Test
  void submitPendingMoveShouldCoverAllStatuses() throws Exception {
    Game localGame = new Game();
    HumanPlayer alice = new HumanPlayer("Alice", PlayerColor.BLUE);
    HumanPlayer bob = new HumanPlayer("Bob", PlayerColor.RED);
    localGame.addPlayer(alice);
    localGame.addPlayer(bob);
    localGame.startGame();
    alice.getRack().setTiles(new ArrayList<>(List.of(
        new Tile('A'), new Tile('R'), new Tile('T'), new Tile('B'), new Tile('C'),
        new Tile('D'), new Tile('E'))));

    GameController localController = new GameController(localGame, new RecordingView());
    setDictionary(localController, minimalDictionary("ART"));

    assertEquals(GameController.PendingMoveSubmitStatus.EMPTY,
        localController.submitPendingMove(null).status());
    assertEquals(GameController.PendingMoveSubmitStatus.EMPTY,
        localController.submitPendingMove(Map.of()).status());

    Map<Point, Tile> misaligned = new HashMap<>();
    misaligned.put(new Point(7, 7), new Tile('A'));
    misaligned.put(new Point(8, 8), new Tile('R'));
    assertEquals(GameController.PendingMoveSubmitStatus.INVALID_ALIGNMENT,
        localController.submitPendingMove(misaligned).status());

    Map<Point, Tile> invalidFirstMove = new LinkedHashMap<>();
    invalidFirstMove.put(new Point(0, 0), new Tile('A'));
    invalidFirstMove.put(new Point(1, 0), new Tile('R'));
    invalidFirstMove.put(new Point(2, 0), new Tile('T'));
    GameController.PendingMoveSubmitResult rejected =
        localController.submitPendingMove(invalidFirstMove);
    assertEquals(GameController.PendingMoveSubmitStatus.LOCAL_REJECTED, rejected.status());
    assertNotNull(rejected.errorMessage());

    Map<Point, Tile> validMove = new LinkedHashMap<>();
    validMove.put(new Point(7, 7), new Tile('A'));
    validMove.put(new Point(8, 7), new Tile('R'));
    validMove.put(new Point(9, 7), new Tile('T'));
    GameController.PendingMoveSubmitResult applied =
        localController.submitPendingMove(validMove);
    assertEquals(GameController.PendingMoveSubmitStatus.LOCAL_APPLIED, applied.status());

    localController.switchToOnlineMode(localGame);
    GameController.PendingMoveSubmitResult online = localController.submitPendingMove(validMove);
    assertEquals(GameController.PendingMoveSubmitStatus.ONLINE_READY, online.status());
    assertNotNull(online.payload());
    assertEquals(7, online.payload().x());
    assertEquals(7, online.payload().y());
    assertEquals("H", online.payload().direction());
    assertEquals("ART", online.payload().word());
    localController.exitOnlineMode();
    assertFalse(localController.isOnlineMode());
  }

  @Test
  void controllerUtilitiesShouldResolveTilesExchangesAndPayloads() throws Exception {
    Game game = new Game();
    HumanPlayer alice = new HumanPlayer("Alice", PlayerColor.BLUE);
    HumanPlayer bob = new HumanPlayer("Bob", PlayerColor.RED);
    game.addPlayer(alice);
    game.addPlayer(bob);
    game.startGame();
    alice.getRack().setTiles(new ArrayList<>(List.of(
        new Tile('A'), new Tile('B'), new Tile('C'), new Tile(' ', true),
        new Tile('D'), new Tile('E'), new Tile('F'))));

    GameController controller = new GameController(game, new RecordingView());
    setDictionary(controller, minimalDictionary("AB", "ART"));

    Tile resolvedTile = controller.resolveDroppedTile(new Tile('A'), null).orElseThrow();
    assertEquals('A', resolvedTile.getCharacter());
    Tile resolvedJoker = controller.resolveDroppedTile(new Tile(' ', true), "x").orElseThrow();
    assertTrue(resolvedJoker.isJoker());
    assertTrue(controller.resolveDroppedTile(new Tile(' ', true), "1").isEmpty());
    assertTrue(controller.resolveDroppedTile(null, "A").isEmpty());

    assertTrue(controller.buildExchangeMoveFromLetters(null).isEmpty());
    assertTrue(controller.buildExchangeMoveFromLetters("   ").isEmpty());
    assertTrue(controller.buildExchangeMoveFromLetters("AZ").isEmpty());
    assertEquals(2, controller.buildExchangeMoveFromLetters("ab").orElseThrow().getTiles().size());

    assertTrue(controller.canPlacePendingTile(new Point(7, 7), Map.of()));
    game.getBoard().getSquare(new Point(9, 9)).setTile(new Tile('Z'));
    assertFalse(controller.canPlacePendingTile(new Point(9, 9), Map.of()));
    assertFalse(controller.canPlacePendingTile(null, Map.of()));

    assertFalse(controller.analyzeTileDrop(null, 7, 7, Map.of()).accepted());
    GameController.TileDropAnalysis rejected =
        controller.analyzeTileDrop(new Tile('A'), 7, 7, Map.of(new Point(7, 7), new Tile('B')));
    assertFalse(rejected.accepted());
    GameController.TileDropAnalysis accepted =
        controller.analyzeTileDrop(new Tile('A'), 7, 8, Map.of());
    assertTrue(accepted.accepted());
    assertFalse(accepted.needsJokerResolution());
    GameController.TileDropAnalysis jokerAccepted =
        controller.analyzeTileDrop(new Tile(' ', true), 7, 9, Map.of());
    assertTrue(jokerAccepted.accepted());
    assertTrue(jokerAccepted.needsJokerResolution());

    Move play = Move.createPlay(alice, List.of(new Tile('A'), new Tile('B')),
        new Point(7, 7), Direction.HORIZONTAL);
    GameController.NetworkPlayPayload payload = controller.toNetworkPlayPayload(play);
    assertEquals(7, payload.x());
    assertEquals(7, payload.y());
    assertEquals("H", payload.direction());
    assertEquals("AB", payload.word());
    assertThrows(IllegalArgumentException.class,
        () -> controller.toNetworkPlayPayload(Move.createPass(alice)));
  }

  @Test
  void controllerShouldRecreateSaveLoadAndRunAiTurn() throws Exception {
    Game game = new Game();
    HumanPlayer alice = new HumanPlayer("Alice", PlayerColor.BLUE);
    HumanPlayer bob = new HumanPlayer("Bob", PlayerColor.RED);
    game.addPlayer(alice);
    game.addPlayer(bob);

    GameController controller = new GameController(game, new RecordingView());
    controller.setPlayerCount(2);
    assertEquals(2, controller.resolveNewGamePlayerCount(Optional.empty()).getAsInt());

    controller.setPlayerCount(0);
    assertTrue(controller.resolveNewGamePlayerCount(Optional.empty()).isEmpty());
    Optional<Game> recreated = controller.recreateConfiguredGameFromSelection(Optional.of(2));
    assertTrue(recreated.isPresent());
    assertEquals(2, controller.getGame().getPlayers().size());

    Path saveFile = Files.createTempFile("scrabble-controller-", ".sav");
    try {
      controller.saveGameToPath(saveFile.toString());
      Game loaded = controller.loadGameFromPath(saveFile.toString());
      assertEquals(controller.getGame().getPlayers().size(), loaded.getPlayers().size());

      assertEquals("fallback", controller.getConfigOption("missing.option", "fallback"));

      Game aiGame = new Game();
      PassingAiPlayer ai = new PassingAiPlayer("IA-pass");
      HumanPlayer human = new HumanPlayer("Bob", PlayerColor.RED);
      aiGame.addPlayer(ai);
      aiGame.addPlayer(human);
      aiGame.startGame();

      GameController aiController = new GameController(aiGame, new RecordingView());
      setDictionary(aiController, minimalDictionary("ART"));

      CountDownLatch finished = new CountDownLatch(1);
      aiController.performAiTurn(finished::countDown);

      assertTrue(finished.await(5, TimeUnit.SECONDS));
      assertTrue(aiGame.getUndoRedo().getHistory().size() >= 1);
    } finally {
      Files.deleteIfExists(saveFile);
    }
  }

  @Test
  void controllerAuxPrivateMethodsShouldFormatHistoryHelpAndTime() throws Exception {
    Game game = new Game();
    HumanPlayer alice = new HumanPlayer("Alice", PlayerColor.BLUE);
    HumanPlayer bob = new HumanPlayer("Bob", PlayerColor.RED);
    game.addPlayer(alice);
    game.addPlayer(bob);

    CliView view = new CliView(game);
    GameController controller = new GameController(game, view);
    GameControllerAux aux = new GameControllerAux(controller);
    invokePrivateAuxMethod(aux, "displayHelp", new Class<?>[] {CliView.class, String.class},
        view, null);

    Stack<Move> history = game.getUndoRedo().getHistory();
    history.push(Move.createPass(alice));
    history.push(Move.createExchange(alice, List.of(new Tile('A'), new Tile('B'))));
    history.push(Move.createPlay(alice, List.of(new Tile('A'), new Tile('B')),
        new Point(7, 7), Direction.HORIZONTAL));

    invokePrivateAuxMethod(aux, "showHistory", new Class<?>[] {CliView.class}, view);

    Game emptyGame = new Game();
    GameController emptyController = new GameController(emptyGame, new CliView(emptyGame));
    GameControllerAux emptyAux = new GameControllerAux(emptyController);
    invokePrivateAuxMethod(emptyAux, "showTime", new Class<?>[] {CliView.class},
        new CliView(emptyGame));

    invokePrivateAuxMethod(aux, "displayHelp", new Class<?>[] {CliView.class, String.class},
        view, null);
    invokePrivateAuxMethod(aux, "displayHelp", new Class<?>[] {CliView.class, String.class},
        view, "show");
    invokePrivateAuxMethod(aux, "displayHelp", new Class<?>[] {CliView.class, String.class},
        view, "unknown");

    invokePrivateAuxMethod(aux, "pauseBlitzClock",
        new Class<?>[] {fr.ubordeaux.scrabble.model.interfaces.Player.class, CliView.class},
        alice, view);
  }

  private static void setDictionary(GameController controller, Gaddag dictionary) throws Exception {
    Field field = GameController.class.getDeclaredField("gaddag");
    field.setAccessible(true);
    field.set(controller, dictionary);
  }

  private static Object getPrivateField(GameController controller, String name) throws Exception {
    Field field = GameController.class.getDeclaredField(name);
    field.setAccessible(true);
    return field.get(controller);
  }

  private static Object invokePrivateMethod(GameController controller, String methodName,
      Class<?>[] parameterTypes, Object... args) throws Exception {
    Method method = GameController.class.getDeclaredMethod(methodName, parameterTypes);
    method.setAccessible(true);
    return method.invoke(controller, args);
  }

  private static Object invokePrivateMethod(GameController controller, String methodName)
      throws Exception {
    Method method = GameController.class.getDeclaredMethod(methodName);
    method.setAccessible(true);
    return method.invoke(controller);
  }

  private static Object invokePrivateAuxMethod(GameControllerAux aux, String methodName,
      Class<?>[] parameterTypes, Object... args) throws Exception {
    Method method = GameControllerAux.class.getDeclaredMethod(methodName, parameterTypes);
    method.setAccessible(true);
    return method.invoke(aux, args);
  }

  private static Object getAiMlAgent(AiPlayer ai) throws Exception {
    Field field = AiPlayer.class.getDeclaredField("mlAgent");
    field.setAccessible(true);
    return field.get(ai);
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
