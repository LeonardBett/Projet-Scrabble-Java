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
import fr.ubordeaux.scrabble.model.dictionary.Gaddag;
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
import java.util.Optional;
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

  @Test
  void isOccupiedOrPendingReturnsFalseWhenCellEmptyAndNotPending() {
    Map<Point, Tile> pending = new HashMap<>();
    Point origin = new Point(0, 0);
    assertFalse((boolean) invokeStatic("isOccupiedOrPending",
        new Class<?>[] { Game.class, Map.class, Point.class },
        deterministicGame, pending, origin));
  }

  @Test
  void isOccupiedOrPendingReturnsTrueWhenTileInPendingMap() {
    Map<Point, Tile> pending = new HashMap<>();
    Point origin = new Point(0, 0);
    pending.put(origin, new Tile('A'));
    assertTrue((boolean) invokeStatic("isOccupiedOrPending",
        new Class<?>[] { Game.class, Map.class, Point.class },
        deterministicGame, pending, origin));
  }

  @Test
  void normalizeExchangeLettersTrimsAndConvertsToUppercase() {
    assertEquals("HELLO", invokeStatic("normalizeExchangeLetters",
        new Class<?>[] { String.class }, "  hello  "));
    assertEquals("ABC", invokeStatic("normalizeExchangeLetters",
        new Class<?>[] { String.class }, "abc"));
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
  }

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
  void shouldPassThroughNetworkReturnsCorrectly() {
    assertTrue((boolean) invokeStatic("shouldPassThroughNetwork",
        new Class<?>[] { boolean.class }, true));
    assertFalse((boolean) invokeStatic("shouldPassThroughNetwork",
        new Class<?>[] { boolean.class }, false));
  }

  @Test
  void canUseUndoRedoMatrix() {
    assertTrue((boolean) invokeStatic("canUseUndoRedo",
        new Class<?>[] { boolean.class, boolean.class }, false, false));
    assertFalse((boolean) invokeStatic("canUseUndoRedo",
        new Class<?>[] { boolean.class, boolean.class }, true, false));
    assertFalse((boolean) invokeStatic("canUseUndoRedo",
        new Class<?>[] { boolean.class, boolean.class }, false, true));
    assertFalse((boolean) invokeStatic("canUseUndoRedo",
        new Class<?>[] { boolean.class, boolean.class }, true, true));
  }

  @Test
  void shouldIgnoreTileDropNullTile() {
    assertTrue((boolean) invokeStatic("shouldIgnoreTileDrop",
        new Class<?>[] { Tile.class, boolean.class }, null, false));
  }

  @Test
  void shouldIgnoreTileDropGameOver() {
    assertTrue((boolean) invokeStatic("shouldIgnoreTileDrop",
        new Class<?>[] { Tile.class, boolean.class }, new Tile('A'), true));
  }

  @Test
  void shouldIgnoreTileDropValidCase() {
    assertFalse((boolean) invokeStatic("shouldIgnoreTileDrop",
        new Class<?>[] { Tile.class, boolean.class }, new Tile('A'), false));
  }

  @Test
  void shouldRunAiTurnWhenAiPlayerAndNotOver() {
    Game gameWithAi = new Game();
    gameWithAi.addPlayer(new AiPlayer("AI", 1, 60, PlayerColor.BLUE));
    gameWithAi.addPlayer(new HumanPlayer("Human", PlayerColor.RED));
    gameWithAi.startGame();
    assertTrue((boolean) invokeStatic("shouldRunAiTurn",
        new Class<?>[] { fr.ubordeaux.scrabble.model.interfaces.Player.class, boolean.class },
        gameWithAi.getCurrentPlayer(), false));
  }

  @Test
  void shouldRunAiTurnFalseForHumanPlayer() {
    assertFalse((boolean) invokeStatic("shouldRunAiTurn",
        new Class<?>[] { fr.ubordeaux.scrabble.model.interfaces.Player.class, boolean.class },
        deterministicGame.getCurrentPlayer(), false));
  }

  @Test
  void shouldRunAiTurnFalseWhenGameOver() {
    Game gameWithAi = new Game();
    gameWithAi.addPlayer(new AiPlayer("AI", 1, 60, PlayerColor.BLUE));
    gameWithAi.addPlayer(new HumanPlayer("Human", PlayerColor.RED));
    gameWithAi.startGame();
    assertFalse((boolean) invokeStatic("shouldRunAiTurn",
        new Class<?>[] { fr.ubordeaux.scrabble.model.interfaces.Player.class, boolean.class },
        gameWithAi.getCurrentPlayer(), true));
  }

  @Test
  void shouldKeepGameplayDisabledAfterAi() {
    assertTrue((boolean) invokeStatic("shouldKeepGameplayDisabledAfterAi",
        new Class<?>[] { boolean.class }, true));
    assertFalse((boolean) invokeStatic("shouldKeepGameplayDisabledAfterAi",
        new Class<?>[] { boolean.class }, false));
  }

  @Test
  void shouldOpenNetworkLobbyReturnsTrueWhenNull() {
    assertTrue((boolean) invokeStatic("shouldOpenNetworkLobby",
        new Class<?>[] { NetworkLobbyView.class }, (Object) null));
  }

  @Test
  void normalizedDictionaryLineTrimsSurroundingWhitespace() {
    assertEquals("hello", invokeStatic("normalizedDictionaryLine",
        new Class<?>[] { String.class }, "  hello  "));
    assertEquals("WORD", invokeStatic("normalizedDictionaryLine",
        new Class<?>[] { String.class }, "\tWORD\n"));
  }

  @Test
  void shouldAddDictionaryEntry() {
    assertTrue((boolean) invokeStatic("shouldAddDictionaryEntry",
        new Class<?>[] { String.class }, "word"));
    assertFalse((boolean) invokeStatic("shouldAddDictionaryEntry",
        new Class<?>[] { String.class }, ""));
  }

  @Test
  void shouldLoadDictionaryForAi() {
    assertTrue((boolean) invokeStatic("shouldLoadDictionaryForAi",
        new Class<?>[] { Gaddag.class }, (Object) null));
    assertFalse((boolean) invokeStatic("shouldLoadDictionaryForAi",
        new Class<?>[] { Gaddag.class }, new Gaddag()));
  }

  @Test
  void shouldSkipScoreRefresh() {
    assertTrue((boolean) invokeStatic("shouldSkipScoreRefresh",
        new Class<?>[] { List.class }, new ArrayList<>()));
    assertFalse((boolean) invokeStatic("shouldSkipScoreRefresh",
        new Class<?>[] { List.class }, deterministicGame.getPlayers()));
  }

  @Test
  void shouldHighlightScoreIndex() {
    assertTrue((boolean) invokeStatic("shouldHighlightScoreIndex",
        new Class<?>[] { int.class }, 0));
    assertFalse((boolean) invokeStatic("shouldHighlightScoreIndex",
        new Class<?>[] { int.class }, -1));
  }

  @Test
  void shouldRejectSubmitWhenNoPending() {
    assertTrue((boolean) invokeStatic("shouldRejectSubmitWhenNoPending",
        new Class<?>[] { Map.class }, new HashMap<>()));
    Map<Point, Tile> pending = new HashMap<>();
    pending.put(new Point(7, 7), new Tile('A'));
    assertFalse((boolean) invokeStatic("shouldRejectSubmitWhenNoPending",
        new Class<?>[] { Map.class }, pending));
  }

  @Test
  void shouldRejectSubmitWhenMoveNull() {
    assertTrue((boolean) invokeStatic("shouldRejectSubmitWhenMoveNull",
        new Class<?>[] { Move.class }, (Object) null));
    assertFalse((boolean) invokeStatic("shouldRejectSubmitWhenMoveNull",
        new Class<?>[] { Move.class },
        Move.createPass(deterministicGame.getCurrentPlayer())));
  }

  @Test
  void shouldBlockExchangeWhilePending() {
    Map<Point, Tile> pending = new HashMap<>();
    pending.put(new Point(7, 7), new Tile('A'));
    assertTrue((boolean) invokeStatic("shouldBlockExchangeWhilePending",
        new Class<?>[] { Map.class }, pending));
    assertFalse((boolean) invokeStatic("shouldBlockExchangeWhilePending",
        new Class<?>[] { Map.class }, new HashMap<>()));
  }

  @Test
  void shouldCancelWhenPendingEmpty() {
    assertTrue((boolean) invokeStatic("shouldCancelWhenPendingEmpty",
        new Class<?>[] { Map.class }, new HashMap<>()));
    Map<Point, Tile> pending = new HashMap<>();
    pending.put(new Point(7, 7), new Tile('A'));
    assertFalse((boolean) invokeStatic("shouldCancelWhenPendingEmpty",
        new Class<?>[] { Map.class }, pending));
  }

  @Test
  void shouldStartBlitz() {
    assertTrue((boolean) invokeStatic("shouldStartBlitz",
        new Class<?>[] { boolean.class }, true));
    assertFalse((boolean) invokeStatic("shouldStartBlitz",
        new Class<?>[] { boolean.class }, false));
  }

  @Test
  void toPlayerNamesAndScores() {
    String[] names = (String[]) invokeStatic("toPlayerNames",
        new Class<?>[] { List.class }, deterministicGame.getPlayers());
    assertEquals(2, names.length);
    assertEquals("P1", names[0]);
    int[] scores = (int[]) invokeStatic("toPlayerScores",
        new Class<?>[] { List.class }, deterministicGame.getPlayers());
    assertEquals(2, scores.length);
  }

  @Test
  void indexOfCurrentPlayer() {
    List<fr.ubordeaux.scrabble.model.interfaces.Player> players = deterministicGame.getPlayers();
    assertEquals(0, invokeStatic("indexOfCurrentPlayer",
        new Class<?>[] { List.class, fr.ubordeaux.scrabble.model.interfaces.Player.class },
        players, deterministicGame.getCurrentPlayer()));
    assertEquals(-1, invokeStatic("indexOfCurrentPlayer",
        new Class<?>[] { List.class, fr.ubordeaux.scrabble.model.interfaces.Player.class },
        players, null));
  }

  @Test
  void buildBlitzTimeoutMessageIncludesPlayerName() {
    String msg = (String) invokeStatic("buildBlitzTimeoutMessage",
        new Class<?>[] { String.class }, "Alice");
    assertTrue(msg.contains("Alice"));
  }

  @Test
  void blitzTimeoutTitleIsNonEmpty() {
    String title = (String) invokeStatic("blitzTimeoutTitle", new Class<?>[] {});
    assertFalse(title.isEmpty());
  }

  @Test
  @SuppressWarnings("unchecked")
  void createDefaultPlayersShouldReturnCorrectCountAndNames() {
    List<HumanPlayer> two = (List<HumanPlayer>) invokeStatic("createDefaultPlayers",
        new Class<?>[] { int.class }, 2);
    assertEquals(2, two.size());
    assertEquals("Joueur1", two.get(0).getName());
    assertEquals("Joueur2", two.get(1).getName());
    List<HumanPlayer> four = (List<HumanPlayer>) invokeStatic("createDefaultPlayers",
        new Class<?>[] { int.class }, 4);
    assertEquals(4, four.size());
    assertEquals("Joueur3", four.get(2).getName());
    List<HumanPlayer> zero = (List<HumanPlayer>) invokeStatic("createDefaultPlayers",
        new Class<?>[] { int.class }, 0);
    assertTrue(zero.isEmpty());
  }

  @Test
  @SuppressWarnings("unchecked")
  void findOutOfTimePlayerNameReturnsEmptyWhenNoPlayersOutOfTime() {
    Optional<String> result = (Optional<String>) invokeStatic("findOutOfTimePlayerName",
        new Class<?>[] { List.class }, deterministicGame.getPlayers());
    assertTrue(result.isEmpty());
  }

  @Test
  @SuppressWarnings("unchecked")
  void findOutOfTimePlayerNameReturnsPlayerWhenOutOfTime() {
    HumanPlayer p = new HumanPlayer("Timeout", PlayerColor.BLUE);
    p.enableBlitzClock(java.time.Duration.ofNanos(1));
    p.startTurnTimer();
    p.pauseTurnTimer();
    Optional<String> result = (Optional<String>) invokeStatic("findOutOfTimePlayerName",
        new Class<?>[] { List.class }, List.of(p));
    assertTrue(result.isPresent());
    assertEquals("Timeout", result.get());
  }

  @Test
  void buildPlayedWordReturnsCharacterConcatenation() {
    List<Tile> tiles = List.of(new Tile('H'), new Tile('E'), new Tile('L'),
        new Tile('L'), new Tile('O'));
    Move move = Move.createPlay(deterministicGame.getCurrentPlayer(), tiles,
        new Point(7, 7), Direction.HORIZONTAL);
    assertEquals("HELLO", invokeStatic("buildPlayedWord",
        new Class<?>[] { Move.class }, move));
  }

  @Test
  void moveOriginXandY() {
    List<Tile> tiles = List.of(new Tile('A'));
    Move move = Move.createPlay(deterministicGame.getCurrentPlayer(), tiles,
        new Point(3, 5), Direction.HORIZONTAL);
    assertEquals(3, invokeStatic("moveOriginX", new Class<?>[] { Move.class }, move));
    assertEquals(5, invokeStatic("moveOriginY", new Class<?>[] { Move.class }, move));
  }

  @Test
  void moveDirectionTokenHorizontalAndVertical() {
    List<Tile> tiles = List.of(new Tile('A'));
    Move h = Move.createPlay(deterministicGame.getCurrentPlayer(), tiles,
        new Point(7, 7), Direction.HORIZONTAL);
    assertEquals("H", invokeStatic("moveDirectionToken",
        new Class<?>[] { Move.class }, h));
    Move v = Move.createPlay(deterministicGame.getCurrentPlayer(), tiles,
        new Point(7, 7), Direction.VERTICAL);
    assertEquals("V", invokeStatic("moveDirectionToken",
        new Class<?>[] { Move.class }, v));
  }

  @Test
  void shouldAbortNewGame() {
    assertTrue((boolean) invokeStatic("shouldAbortNewGame",
        new Class<?>[] { boolean.class }, false));
    assertFalse((boolean) invokeStatic("shouldAbortNewGame",
        new Class<?>[] { boolean.class }, true));
  }

  @Test
  void shouldAbortWhenMissingPlayerCount() {
    assertTrue((boolean) invokeStatic("shouldAbortWhenMissingPlayerCount",
        new Class<?>[] { Optional.class }, Optional.empty()));
    assertFalse((boolean) invokeStatic("shouldAbortWhenMissingPlayerCount",
        new Class<?>[] { Optional.class }, Optional.of(2)));
  }

  @Test
  void shouldReinitializeNetworkForNewGame() {
    assertTrue((boolean) invokeStatic("shouldReinitializeNetworkForNewGame",
        new Class<?>[] { boolean.class }, true));
    assertFalse((boolean) invokeStatic("shouldReinitializeNetworkForNewGame",
        new Class<?>[] { boolean.class }, false));
  }

  @Test
  void selectedPlayerCount() {
    assertEquals(3, invokeStatic("selectedPlayerCount",
        new Class<?>[] { Optional.class }, Optional.of(3)));
    assertEquals(0, invokeStatic("selectedPlayerCount",
        new Class<?>[] { Optional.class }, Optional.empty()));
  }

  @Test
  void shouldLoadGaddag() {
    assertTrue((boolean) invokeStatic("shouldLoadGaddag",
        new Class<?>[] { Gaddag.class }, (Object) null));
    assertFalse((boolean) invokeStatic("shouldLoadGaddag",
        new Class<?>[] { Gaddag.class }, new Gaddag()));
  }

  @Test
  void onTileDraggedBasicCall() {
    ScrabbleGui gui = new ScrabbleGui();
    gui.onTileDragged(null);
    gui.onTileDragged(new Tile('X'));
  }

  @Test
  void defaultPlayerNameFormatting() {
    assertEquals("Joueur1", invokeStatic("defaultPlayerName",
        new Class<?>[] { int.class }, 1));
    assertEquals("Joueur2", invokeStatic("defaultPlayerName",
        new Class<?>[] { int.class }, 2));
  }

  @Test
  void menuTitleText() {
    assertEquals("MENU", invokeStatic("menuTitleText", new Class<?>[] {}));
  }

  @Test
  void windowDimensionsAreValid() {
    int w = (int) invokeStatic("windowWidth", new Class<?>[] {});
    int h = (int) invokeStatic("windowHeight", new Class<?>[] {});
    assertTrue(w > 0 && h > 0);
  }

  @Test
  void dialogTextStringsAreNonEmpty() {
    assertFalse(((String) invokeStatic("helpDialogTitle", new Class<?>[] {})).isEmpty());
    assertFalse(((String) invokeStatic("helpDialogMessage", new Class<?>[] {})).isEmpty());
  }

  @Test
  void errorMessagesContainContext() {
    assertTrue(((String) invokeStatic("occupiedCellMessage",
        new Class<?>[] {})).contains("occupée"));
    assertFalse(((String) invokeStatic("placeAtLeastOneTileMessage",
        new Class<?>[] {})).isEmpty());
    assertFalse(((String) invokeStatic("invalidAlignmentMessage",
        new Class<?>[] {})).isEmpty());
  }

  @Test
  void exchangeDialogContentIsPresent() {
    assertFalse(((String) invokeStatic("exchangeDialogTitle", new Class<?>[] {})).isEmpty());
    assertFalse(((String) invokeStatic("exchangeDialogHeaderText",
        new Class<?>[] {})).isEmpty());
    assertFalse(((String) invokeStatic("exchangeDialogContentText",
        new Class<?>[] {})).isEmpty());
  }

  @Test
  void networkAndComingSoonMessages() {
    assertFalse(((String) invokeStatic("onlineStartedTitle", new Class<?>[] {})).isEmpty());
    assertFalse(((String) invokeStatic("onlineStartedMessage",
        new Class<?>[] {})).isEmpty());
    assertFalse(((String) invokeStatic("saveComingSoonMessage",
        new Class<?>[] {})).isEmpty());
    assertFalse(((String) invokeStatic("loadComingSoonMessage",
        new Class<?>[] {})).isEmpty());
    assertFalse(((String) invokeStatic("comingSoonTitle", new Class<?>[] {})).isEmpty());
  }

  @Test
  void menuButtonPropertiesAreValid() {
    assertFalse(((String) invokeStatic("appMenuButtonText", new Class<?>[] {})).isEmpty());
    assertFalse(((String) invokeStatic("newGameMenuText", new Class<?>[] {})).isEmpty());
    assertFalse(((String) invokeStatic("multiplayerMenuText", new Class<?>[] {})).isEmpty());
    assertFalse(((String) invokeStatic("saveMenuText", new Class<?>[] {})).isEmpty());
    assertFalse(((String) invokeStatic("loadMenuText", new Class<?>[] {})).isEmpty());
    assertFalse(((String) invokeStatic("quitMenuText", new Class<?>[] {})).isEmpty());
  }

  @Test
  void stylings() {
    assertFalse(((String) invokeStatic("rootBackgroundStyle", new Class<?>[] {})).isEmpty());
    assertFalse(((String) invokeStatic("appMenuButtonStyle", new Class<?>[] {})).isEmpty());
  }

  @Test
  void paddingAndSpacingValuesArePositive() {
    assertTrue((double) invokeStatic("rootPadding", new Class<?>[] {}) > 0);
    assertTrue((double) invokeStatic("rightPanelSpacing", new Class<?>[] {}) > 0);
    assertTrue((double) invokeStatic("appMenuButtonWidth", new Class<?>[] {}) > 0);
  }

  @Test
  void menuLabelProperties() {
    assertFalse(((String) invokeStatic("menuLabelFontFamily", new Class<?>[] {})).isEmpty());
    assertTrue((int) invokeStatic("menuLabelFontSize", new Class<?>[] {}) > 0);
  }

  @Test
  void dictionaryLoadErrorMessageContainsDetails() {
    assertTrue(((String) invokeStatic("dictionaryLoadErrorMessage",
        new Class<?>[] { String.class }, "detail")).contains("detail"));
  }

  @Test
  void invalidMoveMessageContainsDetails() {
    assertTrue(((String) invokeStatic("invalidMoveMessage",
        new Class<?>[] { String.class }, "bad")).contains("bad"));
  }

  @Test
  void additionalMessageStrings() {
    assertFalse(((String) invokeStatic("cancelTilesBeforeExchangeMessage",
        new Class<?>[] {})).isEmpty());
    assertFalse(((String) invokeStatic("exchangeLettersNotInRackMessage",
        new Class<?>[] {})).isEmpty());
    assertFalse(((String) invokeStatic("newGameConfirmationMessage",
        new Class<?>[] {})).isEmpty());
    assertFalse(((String) invokeStatic("quitConfirmationMessage",
        new Class<?>[] {})).isEmpty());
    assertTrue(((String) invokeStatic("missingGameErrorMessage",
        new Class<?>[] {})).contains("setGame"));
    assertTrue(((String) invokeStatic("aiErrorMessage",
        new Class<?>[] { String.class }, "err")).contains("err"));
    assertTrue(((String) invokeStatic("windowTitleText",
        new Class<?>[] {})).contains("Scrabble"));
  }

  @Test
  void rightPaddingValues() {
    assertTrue((double) invokeStatic("rightTopPadding", new Class<?>[] {}) >= 0);
    assertTrue((double) invokeStatic("rightRightPadding", new Class<?>[] {}) >= 0);
    assertTrue((double) invokeStatic("rightBottomPadding", new Class<?>[] {}) >= 0);
    assertTrue((double) invokeStatic("rightLeftPadding", new Class<?>[] {}) >= 0);
  }

  @Test
  void leftMenuPaddingValues() {
    assertTrue((double) invokeStatic("leftMenuSpacing", new Class<?>[] {}) >= 0);
    assertTrue((double) invokeStatic("leftMenuTopPadding", new Class<?>[] {}) >= 0);
    assertTrue((double) invokeStatic("leftMenuRightPadding", new Class<?>[] {}) >= 0);
    assertTrue((double) invokeStatic("leftMenuBottomPadding", new Class<?>[] {}) >= 0);
    assertTrue((double) invokeStatic("leftMenuLeftPadding", new Class<?>[] {}) >= 0);
  }

  @Test
  void toPlayerNamesAndScoresWithEmpty() {
    assertEquals(0, ((String[]) invokeStatic("toPlayerNames",
        new Class<?>[] { List.class }, new ArrayList<>())).length);
    assertEquals(0, ((int[]) invokeStatic("toPlayerScores",
        new Class<?>[] { List.class }, new ArrayList<>())).length);
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
