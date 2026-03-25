package fr.ubordeaux.scrabble.view;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import fr.ubordeaux.scrabble.model.ai.AiPlayer;
import fr.ubordeaux.scrabble.model.core.Game;
import fr.ubordeaux.scrabble.model.core.HumanPlayer;
import fr.ubordeaux.scrabble.model.core.Move;
import fr.ubordeaux.scrabble.model.core.Tile;
import fr.ubordeaux.scrabble.model.enums.Direction;
import fr.ubordeaux.scrabble.model.enums.PlayerColor;
import fr.ubordeaux.scrabble.model.utils.Point;
import fr.ubordeaux.scrabble.view.gui.JavaFxView;
import fr.ubordeaux.scrabble.view.gui.NetworkLobbyView;
import fr.ubordeaux.scrabble.view.gui.ScrabbleGui;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ScrabbleGuiTest {

  private Game deterministicGame;

  @BeforeEach
  void setDeterministicStatics() {
    deterministicGame = new Game();
    deterministicGame.addPlayer(new HumanPlayer("P1", PlayerColor.BLUE));
    deterministicGame.addPlayer(new HumanPlayer("P2", PlayerColor.RED));
    deterministicGame.startGame();
    ScrabbleGui.setGame(deterministicGame);
    ScrabbleGui.setView(new JavaFxView(deterministicGame));
  }

  @AfterEach
  void resetDeterministicStatics() {
    Game resetGame = new Game();
    resetGame.addPlayer(new HumanPlayer("R1", PlayerColor.BLUE));
    resetGame.addPlayer(new HumanPlayer("R2", PlayerColor.RED));
    resetGame.startGame();
    ScrabbleGui.setGame(resetGame);
    ScrabbleGui.setView(new JavaFxView(resetGame));
  }

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
    assertDoesNotThrow(() -> gui.onTileDropped(0, 0));
  }

  // ===== Domain logic tests: occupancy and board state =====
  @Test
  void isOccupiedOrPendingReturnsFalseWhenCellEmptyAndNotPending() {
    Map<Point, Tile> pending = new HashMap<>();
    Point origin = new Point(0, 0);

    assertFalse((boolean) invokeStatic("isOccupiedOrPending",
        new Class<?>[] { Game.class, Map.class, Point.class }, deterministicGame, pending,
        origin));
  }

  @Test
  void isOccupiedOrPendingReturnsTrueWhenTileInPendingMap() {
    Map<Point, Tile> pending = new HashMap<>();
    Point origin = new Point(0, 0);
    pending.put(origin, new Tile('A'));

    assertTrue((boolean) invokeStatic("isOccupiedOrPending",
        new Class<?>[] { Game.class, Map.class, Point.class }, deterministicGame, pending,
        origin));
  }

  // ===== Move and exchange handling =====
  @Test
  void normalizeExchangeLettersTrimsAndConvertsToUppercase() {
    assertEquals("HELLO", invokeStatic("normalizeExchangeLetters",
        new Class<?>[] { String.class }, "  hello  "));
    assertEquals("ABC", invokeStatic("normalizeExchangeLetters",
        new Class<?>[] { String.class }, "abc"));
    assertEquals("XYZ", invokeStatic("normalizeExchangeLetters",
        new Class<?>[] { String.class }, "  XYZ  "));
  }

  @Test
  void shouldSkipExchangeReturnsTrueForEmptyLetters() {
    assertTrue((boolean) invokeStatic("shouldSkipExchange",
        new Class<?>[] { String.class }, ""));
  }

  @Test
  void shouldSkipExchangeReturnsFalseForNonEmptyLetters() {
    assertFalse((boolean) invokeStatic("shouldSkipExchange",
        new Class<?>[] { String.class }, "ABC"));
    assertFalse((boolean) invokeStatic("shouldSkipExchange",
        new Class<?>[] { String.class }, "Z"));
  }

  // ===== Gameplay action control =====
  @Test
  void shouldIgnoreGameplayActionReturnsTrueWhenGameOver() {
    assertTrue((boolean) invokeStatic("shouldIgnoreGameplayAction",
        new Class<?>[] { boolean.class }, true));
  }

  @Test
  void shouldIgnoreGameplayActionReturnsFalseWhenGameNotOver() {
    assertFalse((boolean) invokeStatic("shouldIgnoreGameplayAction",
        new Class<?>[] { boolean.class }, false));
  }

  @Test
  void shouldPassThroughNetworkReturnsTrueInOnlineMode() {
    assertTrue((boolean) invokeStatic("shouldPassThroughNetwork",
        new Class<?>[] { boolean.class }, true));
  }

  @Test
  void shouldPassThroughNetworkReturnsFalseInOfflineMode() {
    assertFalse((boolean) invokeStatic("shouldPassThroughNetwork",
        new Class<?>[] { boolean.class }, false));
  }

  // ===== Undo/redo control =====
  @Test
  void canUseUndoRedoReturnsTrueWhenOfflineAndGameNotOver() {
    assertTrue((boolean) invokeStatic("canUseUndoRedo",
        new Class<?>[] { boolean.class, boolean.class }, false, false));
  }

  @Test
  void canUseUndoRedoReturnsFalseWhenOnline() {
    assertFalse((boolean) invokeStatic("canUseUndoRedo",
        new Class<?>[] { boolean.class, boolean.class }, true, false));
  }

  @Test
  void canUseUndoRedoReturnsFalseWhenGameOver() {
    assertFalse((boolean) invokeStatic("canUseUndoRedo",
        new Class<?>[] { boolean.class, boolean.class }, false, true));
  }

  // ===== Tile drop validation =====
  @Test
  void shouldIgnoreTileDropReturnsTrueWhenDraggedTileNull() {
    assertTrue((boolean) invokeStatic("shouldIgnoreTileDrop",
        new Class<?>[] { Tile.class, boolean.class }, null, false));
  }

  @Test
  void shouldIgnoreTileDropReturnsTrueWhenGameOver() {
    Tile tile = new Tile('A');
    assertTrue((boolean) invokeStatic("shouldIgnoreTileDrop",
        new Class<?>[] { Tile.class, boolean.class }, tile, true));
  }

  @Test
  void shouldIgnoreTileDropReturnsFalseWhenValidTileAndGameNotOver() {
    Tile tile = new Tile('A');
    assertFalse((boolean) invokeStatic("shouldIgnoreTileDrop",
        new Class<?>[] { Tile.class, boolean.class }, tile, false));
  }

  // ===== AI turn logic =====
  @Test
  void shouldRunAiTurnReturnsTrueWhenCurrentIsAiPlayerAndGameNotOver() {
    Game gameWithAi = new Game();
    gameWithAi.addPlayer(new AiPlayer("AI", 1, 60, PlayerColor.BLUE));
    gameWithAi.addPlayer(new HumanPlayer("Human", PlayerColor.RED));
    gameWithAi.startGame();

    assertTrue((boolean) invokeStatic("shouldRunAiTurn",
        new Class<?>[] { fr.ubordeaux.scrabble.model.interfaces.Player.class, boolean.class },
        gameWithAi.getCurrentPlayer(), false));
  }

  @Test
  void shouldRunAiTurnReturnsFalseWhenCurrentIsHumanPlayer() {
    assertFalse((boolean) invokeStatic("shouldRunAiTurn",
        new Class<?>[] { fr.ubordeaux.scrabble.model.interfaces.Player.class, boolean.class },
        deterministicGame.getCurrentPlayer(), false));
  }

  @Test
  void shouldRunAiTurnReturnsFalseWhenGameOver() {
    Game gameWithAi = new Game();
    gameWithAi.addPlayer(new AiPlayer("AI", 1, 60, PlayerColor.BLUE));
    gameWithAi.addPlayer(new HumanPlayer("Human", PlayerColor.RED));
    gameWithAi.startGame();

    assertFalse((boolean) invokeStatic("shouldRunAiTurn",
        new Class<?>[] { fr.ubordeaux.scrabble.model.interfaces.Player.class, boolean.class },
        gameWithAi.getCurrentPlayer(), true));
  }

  @Test
  void shouldKeepGameplayDisabledAfterAiReturnsTrueWhenGameOver() {
    assertTrue((boolean) invokeStatic("shouldKeepGameplayDisabledAfterAi",
        new Class<?>[] { boolean.class }, true));
  }

  @Test
  void shouldKeepGameplayDisabledAfterAiReturnsFalseWhenGameNotOver() {
    assertFalse((boolean) invokeStatic("shouldKeepGameplayDisabledAfterAi",
        new Class<?>[] { boolean.class }, false));
  }

  // ===== Network lobby control =====
  @Test
  void shouldOpenNetworkLobbyReturnsTrueWhenLobbyViewNull() {
    assertTrue((boolean) invokeStatic("shouldOpenNetworkLobby",
        new Class<?>[] { NetworkLobbyView.class }, (Object) null));
  }

  // ===== Dictionary and score management =====
  @Test
  void normalizedDictionaryLineTrimsSurroundingWhitespace() {
    assertEquals("hello", invokeStatic("normalizedDictionaryLine",
        new Class<?>[] { String.class }, "  hello  "));
    assertEquals("WORD", invokeStatic("normalizedDictionaryLine",
        new Class<?>[] { String.class }, "\tWORD\n"));
  }

  @Test
  void shouldAddDictionaryEntryReturnsTrueForNonEmptyLine() {
    assertTrue((boolean) invokeStatic("shouldAddDictionaryEntry",
        new Class<?>[] { String.class }, "word"));
  }

  @Test
  void shouldAddDictionaryEntryReturnsFalseForEmptyLine() {
    assertFalse((boolean) invokeStatic("shouldAddDictionaryEntry",
        new Class<?>[] { String.class }, ""));
  }

  @Test
  void shouldLoadDictionaryForAiReturnsTrueWhenGaddagNull() {
    assertTrue((boolean) invokeStatic("shouldLoadDictionaryForAi",
        new Class<?>[] { fr.ubordeaux.scrabble.model.dictionary.Gaddag.class }, (Object) null));
  }

  @Test
  void shouldSkipScoreRefreshReturnsTrueWhenPlayersEmpty() {
    List<fr.ubordeaux.scrabble.model.interfaces.Player> emptyList = new ArrayList<>();
    assertTrue((boolean) invokeStatic("shouldSkipScoreRefresh",
        new Class<?>[] { List.class }, emptyList));
  }

  @Test
  void shouldSkipScoreRefreshReturnsFalseWhenPlayersNotEmpty() {
    List<fr.ubordeaux.scrabble.model.interfaces.Player> players = new ArrayList<>(
        deterministicGame.getPlayers());
    assertFalse((boolean) invokeStatic("shouldSkipScoreRefresh",
        new Class<?>[] { List.class }, players));
  }

  @Test
  void shouldHighlightScoreIndexReturnsTrueWhenIndexNonNegative() {
    assertTrue((boolean) invokeStatic("shouldHighlightScoreIndex",
        new Class<?>[] { int.class }, 0));
    assertTrue((boolean) invokeStatic("shouldHighlightScoreIndex",
        new Class<?>[] { int.class }, 5));
  }

  @Test
  void shouldHighlightScoreIndexReturnsFalseWhenIndexNegative() {
    assertFalse((boolean) invokeStatic("shouldHighlightScoreIndex",
        new Class<?>[] { int.class }, -1));
  }

  // ===== Move submission validation =====
  @Test
  void shouldRejectSubmitWhenNoPendingReturnsTrueWhenMapEmpty() {
    Map<Point, Tile> empty = new HashMap<>();
    assertTrue((boolean) invokeStatic("shouldRejectSubmitWhenNoPending",
        new Class<?>[] { Map.class }, empty));
  }

  @Test
  void shouldRejectSubmitWhenNoPendingReturnsFalseWhenMapHasTiles() {
    Map<Point, Tile> pending = new HashMap<>();
    pending.put(new Point(7, 7), new Tile('A'));
    assertFalse((boolean) invokeStatic("shouldRejectSubmitWhenNoPending",
        new Class<?>[] { Map.class }, pending));
  }

  @Test
  void shouldRejectSubmitWhenMoveNullReturnsTrueWhenMoveNull() {
    assertTrue((boolean) invokeStatic("shouldRejectSubmitWhenMoveNull",
        new Class<?>[] { Move.class }, (Object) null));
  }

  @Test
  void shouldRejectSubmitWhenMoveNullReturnsFalseWhenMoveExists() {
    Move move = Move.createPass(deterministicGame.getCurrentPlayer());
    assertFalse((boolean) invokeStatic("shouldRejectSubmitWhenMoveNull",
        new Class<?>[] { Move.class }, move));
  }

  // ===== Exchange dialog control =====
  @Test
  void shouldBlockExchangeWhilePendingReturnsTrueWhenMapNotEmpty() {
    Map<Point, Tile> pending = new HashMap<>();
    pending.put(new Point(7, 7), new Tile('A'));
    assertTrue((boolean) invokeStatic("shouldBlockExchangeWhilePending",
        new Class<?>[] { Map.class }, pending));
  }

  @Test
  void shouldBlockExchangeWhilePendingReturnsFalseWhenMapEmpty() {
    Map<Point, Tile> empty = new HashMap<>();
    assertFalse((boolean) invokeStatic("shouldBlockExchangeWhilePending",
        new Class<?>[] { Map.class }, empty));
  }

  @Test
  void shouldCancelWhenPendingEmptyReturnsTrueWhenMapEmpty() {
    Map<Point, Tile> empty = new HashMap<>();
    assertTrue((boolean) invokeStatic("shouldCancelWhenPendingEmpty",
        new Class<?>[] { Map.class }, empty));
  }

  @Test
  void shouldCancelWhenPendingEmptyReturnsFalseWhenMapNotEmpty() {
    Map<Point, Tile> pending = new HashMap<>();
    pending.put(new Point(7, 7), new Tile('A'));
    assertFalse((boolean) invokeStatic("shouldCancelWhenPendingEmpty",
        new Class<?>[] { Map.class }, pending));
  }

  // ===== Blitz mode =====
  @Test
  void shouldStartBlitzReturnsTrueWhenBlitzModeEnabled() {
    assertTrue((boolean) invokeStatic("shouldStartBlitz",
        new Class<?>[] { boolean.class }, true));
  }

  @Test
  void shouldStartBlitzReturnsFalseWhenBlitzModeDisabled() {
    assertFalse((boolean) invokeStatic("shouldStartBlitz",
        new Class<?>[] { boolean.class }, false));
  }

  // ===== Player data transformations =====
  @Test
  void toPlayerNamesReturnsArrayOfPlayerNames() {
    String[] names = (String[]) invokeStatic("toPlayerNames",
        new Class<?>[] { List.class }, deterministicGame.getPlayers());
    assertEquals(2, names.length);
    assertEquals("P1", names[0]);
    assertEquals("P2", names[1]);
  }

  @Test
  void toPlayerScoresReturnsArrayOfScores() {
    int[] scores = (int[]) invokeStatic("toPlayerScores",
        new Class<?>[] { List.class }, deterministicGame.getPlayers());
    assertEquals(2, scores.length);
    assertTrue(scores[0] >= 0);
    assertTrue(scores[1] >= 0);
  }

  @Test
  void indexOfCurrentPlayerReturnsCorrectIndex() {
    List<fr.ubordeaux.scrabble.model.interfaces.Player> players = deterministicGame.getPlayers();
    int idx = (int) invokeStatic("indexOfCurrentPlayer",
        new Class<?>[] { List.class, fr.ubordeaux.scrabble.model.interfaces.Player.class },
        players, deterministicGame.getCurrentPlayer());
    assertEquals(0, idx);
  }

  @Test
  void indexOfCurrentPlayerReturnsNegativeWhenPlayerNull() {
    List<fr.ubordeaux.scrabble.model.interfaces.Player> players = deterministicGame.getPlayers();
    int idx = (int) invokeStatic("indexOfCurrentPlayer",
        new Class<?>[] { List.class, fr.ubordeaux.scrabble.model.interfaces.Player.class },
        players, null);
    assertEquals(-1, idx);
  }

  // ===== Blitz timeout =====
  @Test
  void buildBlitzTimeoutMessageIncludesPlayerName() {
    String msg = (String) invokeStatic("buildBlitzTimeoutMessage",
        new Class<?>[] { String.class }, "Alice");
    assertTrue(msg.contains("Alice"));
    assertTrue(msg.contains("épuisé"));
  }

  // ===== INTEGRATION TESTS - Static helpers & constants =====
  
  /**
   * Teste onTileDragged ne crash pas même si aucune UI.
   */
  @Test
  void onTileDraggedBasicCall() {
    ScrabbleGui gui = new ScrabbleGui();
    // Juste appel basique - vérifier qu'il y a pas de crash NPE
    gui.onTileDragged(null);
    gui.onTileDragged(new Tile('X'));
  }

  @Test
  void onlineModeFalseByDefault() {
    ScrabbleGui gui = new ScrabbleGui();
    assertFalse(gui.isOnlineMode());
  }

  // ===== Static utility chains =====
  @Test
  void defaultPlayerNameFormatting() {
    String name1 = (String) invokeStatic("defaultPlayerName",
        new Class<?>[] { int.class }, 1);
    String name2 = (String) invokeStatic("defaultPlayerName",
        new Class<?>[] { int.class }, 2);
    assertEquals("Joueur1", name1);
    assertEquals("Joueur2", name2);
  }

  @Test
  void menuTitleText() {
    String title = (String) invokeStatic("menuTitleText", new Class<?>[] {});
    assertEquals("MENU", title);
  }

  @Test
  void windowDimensionsAreValid() {
    int width = (int) invokeStatic("windowWidth", new Class<?>[] {});
    int height = (int) invokeStatic("windowHeight", new Class<?>[] {});
    assertTrue(width > 0 && width < 5000);
    assertTrue(height > 0 && height < 3000);
  }

  @Test
  void dialogTextStringsAreNonEmpty() {
    String helpTitle = (String) invokeStatic("helpDialogTitle", new Class<?>[] {});
    String helpMsg = (String) invokeStatic("helpDialogMessage", new Class<?>[] {});
    assertTrue(!helpTitle.isEmpty());
    assertTrue(!helpMsg.isEmpty());
  }

  @Test
  void errorMessagesContainContext() {
    String msg1 = (String) invokeStatic("occupiedCellMessage", new Class<?>[] {});
    String msg2 = (String) invokeStatic("placeAtLeastOneTileMessage", new Class<?>[] {});
    String msg3 = (String) invokeStatic("invalidAlignmentMessage", new Class<?>[] {});
    assertTrue(msg1.toLowerCase().contains("occupée") || msg1.toLowerCase().contains("occupée"));
    assertTrue(!msg2.isEmpty());
    assertTrue(!msg3.isEmpty());
  }

  @Test
  void exchangeDialogContentIsPresent() {
    String title = (String) invokeStatic("exchangeDialogTitle", new Class<?>[] {});
    String header = (String) invokeStatic("exchangeDialogHeaderText", new Class<?>[] {});
    String content = (String) invokeStatic("exchangeDialogContentText", new Class<?>[] {});
    assertTrue(!title.isEmpty());
    assertTrue(!header.isEmpty());
    assertTrue(!content.isEmpty());
  }

  @Test
  void networkMessagesAreValid() {
    String onlineTitle = (String) invokeStatic("onlineStartedTitle", new Class<?>[] {});
    String onlineMsg = (String) invokeStatic("onlineStartedMessage", new Class<?>[] {});
    assertTrue(!onlineTitle.isEmpty());
    assertTrue(!onlineMsg.isEmpty());
  }

  @Test
  void comingSoonMessagesAreValid() {
    String saveMsg = (String) invokeStatic("saveComingSoonMessage", new Class<?>[] {});
    String loadMsg = (String) invokeStatic("loadComingSoonMessage", new Class<?>[] {});
    assertTrue(!saveMsg.isEmpty());
    assertTrue(!loadMsg.isEmpty());
  }

  @Test
  void menuButtonPropertiesAreValid() {
    String appBtnText = (String) invokeStatic("appMenuButtonText", new Class<?>[] {});
    String newGameTxt = (String) invokeStatic("newGameMenuText", new Class<?>[] {});
    String multiplayerTxt = (String) invokeStatic("multiplayerMenuText", new Class<?>[] {});
    assertTrue(!appBtnText.isEmpty());
    assertTrue(!newGameTxt.isEmpty());
    assertTrue(!multiplayerTxt.isEmpty());
  }

  @Test
  void stylingsAreNonEmpty() {
    String rootStyle = (String) invokeStatic("rootBackgroundStyle", new Class<?>[] {});
    String btnStyle = (String) invokeStatic("appMenuButtonStyle", new Class<?>[] {});
    assertTrue(!rootStyle.isEmpty());
    assertTrue(!btnStyle.isEmpty());
  }

  @Test
  void paddingAndSpacingValuesArePositive() {
    double root = (double) invokeStatic("rootPadding", new Class<?>[] {});
    double rightSpacing = (double) invokeStatic("rightPanelSpacing", new Class<?>[] {});
    double appBtnWidth = (double) invokeStatic("appMenuButtonWidth", new Class<?>[] {});
    assertTrue(root > 0);
    assertTrue(rightSpacing > 0);
    assertTrue(appBtnWidth > 0);
  }

  @Test
  void menuLabelPropertiesAreValid() {
    String fontFamily = (String) invokeStatic("menuLabelFontFamily", new Class<?>[] {});
    int fontSize = (int) invokeStatic("menuLabelFontSize", new Class<?>[] {});
    assertTrue(!fontFamily.isEmpty());
    assertTrue(fontSize > 0 && fontSize < 100);
  }

  private Object invokeStatic(String methodName, Class<?>[] argTypes, Object... args) {
    try {
      Method method = ScrabbleGui.class.getDeclaredMethod(methodName, argTypes);
      method.setAccessible(true);
      return method.invoke(null, args);
    } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
      throw new RuntimeException(e);
    }
  }
}
