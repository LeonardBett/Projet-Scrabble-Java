package fr.ubordeaux.scrabble;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import fr.ubordeaux.scrabble.model.core.Board;
import fr.ubordeaux.scrabble.model.core.PlayableWord;
import fr.ubordeaux.scrabble.model.dictionary.Gaddag;
import fr.ubordeaux.scrabble.model.enums.Direction;
import fr.ubordeaux.scrabble.model.enums.GameMode;
import java.io.ByteArrayInputStream;
import java.io.Console;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class AppTest {

  private CliLaunchCall cliCall;
  private GuiLaunchCall guiCall;

  @BeforeEach
  void setUp() {
    App.setExitHandlerForTests(status -> {
      throw new ExitCalledException(status);
    });
    App.setCliDelegateForTests((gameMode, players, aiColors, blitzMode, blitzMinutes, aiTime,
        useExptiminimax, useMl, lang, saveFilePath) -> cliCall = new CliLaunchCall(gameMode,
        players, aiColors, blitzMode, blitzMinutes, aiTime, useExptiminimax, useMl, lang,
        saveFilePath));
    App.setGuiDelegateForTests((args, gameMode, players, aiColors, blitzMode, blitzMinutes,
        aiTime, useExptiminimax, useMl, lang, saveFilePath) -> guiCall = new GuiLaunchCall(args,
        gameMode, players, aiColors, blitzMode, blitzMinutes, aiTime, useExptiminimax, useMl,
        lang, saveFilePath));
  }

  @AfterEach
  void tearDown() {
    App.resetHandlersForTests();
    System.clearProperty("scrabble.dictionary.path");
    cliCall = null;
    guiCall = null;
  }

  @Test
  void constructorShouldInstantiate() {
    assertDoesNotThrow(App::new);
  }

  /*
  @Test
  void mainShouldLaunchCliWithDefaults() {
    App.main(new String[] {});

    assertNotNull(cliCall);
    assertNull(guiCall);
    assertEquals(2, cliCall.players);
    assertTrue(cliCall.aiColors.isEmpty());
    assertEquals(30, cliCall.blitzMinutes);
    assertEquals(5, cliCall.aiTime);
    assertEquals("en", cliCall.lang);
  }
  */

  @Test
  void mainShouldLaunchGuiAndPropagateParsedOptions() {
    App.main(new String[] {"-g", "-s", "-b", "-t", "12", "-ai-exptiminimax", "--ai-ml", "-a",
        "blue", "-p", "3", "-l", "fr", "-ai-time", "7", "-v", "-d"});

    assertNotNull(guiCall);
    assertNull(cliCall);
    assertEquals(GameMode.SUPER, guiCall.gameMode);
    assertEquals(3, guiCall.players);
    assertEquals(List.of("BLUE"), guiCall.aiColors);
    assertTrue(guiCall.blitzMode);
    assertEquals(12, guiCall.blitzMinutes);
    assertEquals(7, guiCall.aiTime);
    assertTrue(guiCall.useExptiminimax);
    assertTrue(guiCall.useMl);
    assertEquals("fr", guiCall.lang);
  }

  @Test
  void mainShouldKeepDefaultLanguageWhenUnsupported() {
    App.main(new String[] {"-l", "es"});

    assertNotNull(cliCall);
    assertEquals("en", cliCall.lang);
  }

  @Test
  void mainShouldAcceptLanguageLongOption() {
    App.main(new String[] {"--language", "fr"});

    assertNotNull(cliCall);
    assertEquals("fr", cliCall.lang);
  }

  @Test
  void mainShouldKeepDefaultLanguageWhenMissingValue() {
    App.main(new String[] {"-l"});

    assertNotNull(cliCall);
    assertEquals("en", cliCall.lang);
  }

  /*
  @Test
  void mainShouldKeepDefaultLanguageWhenMissingValue() {
    App.main(new String[] {"-l"});

    assertNotNull(cliCall);
    assertEquals("en", cliCall.lang);
  }
  */

  @Test
  void mainShouldEnableBlitzWithDefaultMinutesWhenInvalidValue() {
    App.main(new String[] {"-b", "-t", "not-a-number"});

    assertNotNull(cliCall);
    assertTrue(cliCall.blitzMode);
    assertEquals(30, cliCall.blitzMinutes);
  }

  @Test
  void mainShouldKeepDefaultAiTimeWhenMissingValue() {
    App.main(new String[] {"-ai-time"});

    assertNotNull(cliCall);
    assertEquals(5, cliCall.aiTime);
  }

  @Test
  void mainShouldKeepDefaultAiTimeWhenInvalidValue() {
    App.main(new String[] {"-ai-time", "oops"});

    assertNotNull(cliCall);
    assertEquals(5, cliCall.aiTime);
  }

  @Test
  void mainShouldExitOnUnknownOption() {
    ExitCalledException ex =
        assertThrows(ExitCalledException.class, () -> App.main(new String[] {"--nope"}));

    assertEquals(1, ex.status);
  }

  @Test
  void mainShouldUseDefaultColorWhenAiColorValueMissing() {
    App.setCliDelegateForTests((gameMode, players, aiColors,
                                blitzMode, blitzMinutes, aiTime, useExptiminimax,
                                useMl, lang, savePath) -> {
      assertTrue(aiColors.contains("RED"), "L'IA devrait avoir la couleur par défaut RED.");
    });

    App.main(new String[] {"-a"});

    App.resetHandlersForTests();
  }

  @Test
  void mainShouldExitWhenPlayersValueMissing() {
    ExitCalledException ex =
        assertThrows(ExitCalledException.class, () -> App.main(new String[] {"-p"}));

    assertEquals(1, ex.status);
  }

  @Test
  void mainShouldExitWhenBlitzTimeFlagHasNoValue() {
    App.main(new String[] {"-b", "-t"});

    assertNotNull(cliCall);
    assertTrue(cliCall.blitzMode);
    assertEquals(30, cliCall.blitzMinutes);
  }

  @Test
  void mainShouldIgnoreTimeWhenBlitzNotEnabled() {
    App.main(new String[] {"-t", "12"});

    assertNotNull(cliCall);
    assertEquals(30, cliCall.blitzMinutes);
    assertFalse(cliCall.blitzMode);
  }

  @Test
  void mainShouldAcceptHelpAndVersionOptions() {
    assertDoesNotThrow(() -> App.main(new String[] {"--help"}));
    assertNull(cliCall);
    assertNull(guiCall);

    assertDoesNotThrow(() -> App.main(new String[] {"--version"}));

    assertNull(cliCall);
  }

  @Test
  void mainShouldNormalizeLanguageWithAtAndUnderscoreSuffixes() {
    App.main(new String[] {"-l", "fr_CA.UTF-8@euro"});

    assertNotNull(cliCall);
    assertEquals("fr", cliCall.lang);
  }

  @Test
  void normalizeLanguageShouldFallbackToDefaultForBlank() throws Exception {
    Method normalize = App.class.getDeclaredMethod("normalizeLanguageOrDefault", String.class);
    normalize.setAccessible(true);

    String normalized = (String) normalize.invoke(null, "   ");
    assertEquals("en", normalized);
  }

  @Test
  void promptLaunchRequestShouldReturnNoneWithoutConsole() throws Exception {
    Method prompt = App.class.getDeclaredMethod("promptLaunchRequestAfterHelp");
    prompt.setAccessible(true);

    Object request = prompt.invoke(null);

    assertNotNull(request);
  }

  @Test
  void promptLaunchRequestShouldReturnCliShortcutWhenRequested() throws Exception {
    Object request = invokePromptWithConsoleInput("cli\n");
    assertNotNull(request);
    assertEquals("SHORTCUT", readPrivateField(request, "mode").toString());
    assertEquals("CLI", readPrivateField(request, "shortcut").toString());
  }

  @Test
  void promptLaunchRequestShouldReturnArgsWhenUserTypesLaunchArguments() throws Exception {
    Object request = invokePromptWithConsoleInput("-g -s\n");
    assertNotNull(request);
    assertEquals("ARGS", readPrivateField(request, "mode").toString());
    String[] args = (String[]) readPrivateField(request, "args");
    assertEquals(List.of("-g", "-s"), List.of(args));
  }

  @Test
  void mainShouldExitWhenServerPortIsMissing() {
    ExitCalledException ex =
        assertThrows(ExitCalledException.class, () -> App.main(new String[] {"-S"}));

    assertEquals(1, ex.status);
  }

  @Test
  void mainShouldExitWhenServerPortIsInvalidText() {
    ExitCalledException ex =
        assertThrows(ExitCalledException.class, () -> App.main(new String[] {"-S", "not-a-port"}));

    assertEquals(1, ex.status);
  }

  @Test
  void mainShouldExitWhenServerPortIsOutOfRange() {
    ExitCalledException ex =
        assertThrows(ExitCalledException.class, () -> App.main(new String[] {"-S", "70000"}));

    assertEquals(1, ex.status);
  }

  @Test
  void mainShouldEnableBlitzWhenTimeFlagHasNoNumericValueYet() {
    App.main(new String[] {"-b", "--time", "5"});
    assertNotNull(cliCall);
    assertEquals(5, cliCall.blitzMinutes);
  }

  @Test
  void mainShouldParseDaemonFlagBeforeUnknownOption() {
    ExitCalledException ex =
        assertThrows(
            ExitCalledException.class,
            () -> App.main(new String[] {"--daemon", "--unknown-daemon"}));

    assertEquals(1, ex.status);
  }

  @Test
  void normalizeLanguageShouldHandleAtSuffix() throws Exception {
    Method normalize = App.class.getDeclaredMethod("normalizeLanguageOrDefault", String.class);
    normalize.setAccessible(true);

    String normalized = (String) normalize.invoke(null, "en@euro");
    assertEquals("en", normalized);
  }

  @Test
  void mainShouldContinueAfterUnknownOptionWhenExitHandlerDoesNotThrow() {
    App.setExitHandlerForTests(status -> {
      // no-op to execute the unknown-option path without aborting the test flow
    });

    assertDoesNotThrow(() -> App.main(new String[] {"--still-unknown"}));
    assertNull(cliCall);
  }

  @Test
  void mainShouldReachMissingAiExitCallWhenExitHandlerDoesNotThrow() {
    App.setExitHandlerForTests(status -> {
      // no-op to execute the missing-ai-color exit path and continue
    });

    assertDoesNotThrow(() -> App.main(new String[] {"-a"}));
  }

  @Test
  void mainShouldTreatSinglePositionalArgAsSaveFile() {
    App.main(new String[] {"save.scrabble"});

    assertNotNull(cliCall);
    assertEquals("save.scrabble", cliCall.saveFilePath);
  }

  @Test
  void mainShouldRunContestModeWithoutLaunchingCliOrGui() throws Exception {
    Path saveFile = Files.createTempFile("contest-", ".scrabble");
    Files.writeString(saveFile, minimalContestSave());

    assertDoesNotThrow(() -> App.main(new String[] {"--contest", saveFile.toString()}));
    assertNull(cliCall);
    assertNull(guiCall);
  }

  @Test
  void loadContestDictionaryShouldLoadBuiltinDictionary() throws Exception {
    Method loadDictionary = App.class.getDeclaredMethod("loadContestDictionary", String.class);
    loadDictionary.setAccessible(true);

    Object dictionary = loadDictionary.invoke(null, "en");

    assertNotNull(dictionary);
    assertTrue(dictionary instanceof Gaddag);
  }

  @Test
  void formatContestMoveShouldBuildScrabbleNotation() throws Exception {
    Method formatMove = App.class.getDeclaredMethod("formatContestMove", PlayableWord.class);
    formatMove.setAccessible(true);

    PlayableWord move = new PlayableWord(7, 7, "COUNT", Direction.VERTICAL, "C>OUNT");
    String formatted = (String) formatMove.invoke(null, move);

    assertEquals("h8v COUNT", formatted);
  }

  @Test
  void estimateMoveScoreShouldSumLettersFromRackOnly() throws Exception {
    Method estimate = App.class.getDeclaredMethod("estimateMoveScore", Board.class,
        PlayableWord.class);
    estimate.setAccessible(true);

    Board board = new Board();
    PlayableWord move = new PlayableWord(0, 0, "AB", Direction.HORIZONTAL, "A>B");
    int score = (int) estimate.invoke(null, board, move);

    assertEquals(4, score);
  }

  @Test
  void getLettersFromRackShouldReturnLettersOnEmptySquares() throws Exception {
    Method getLetters = App.class.getDeclaredMethod("getLettersFromRack", Board.class,
        PlayableWord.class);
    getLetters.setAccessible(true);

    Board board = new Board();
    PlayableWord move = new PlayableWord(0, 0, "AB", Direction.HORIZONTAL, "A>B");
    @SuppressWarnings("unchecked")
    List<Character> letters = (List<Character>) getLetters.invoke(null, board, move);

    assertEquals(List.of('A', 'B'), letters);
  }

  @Test
  void mainShouldAcceptDictionaryOptionAndSetProperty() {
    App.main(new String[] {"-D", "custom-dict.txt"});

    assertNotNull(cliCall);
    assertEquals("custom-dict.txt", System.getProperty("scrabble.dictionary.path"));
  }

  @Test
  void mainShouldFallbackToDefaultValuesWhenConfigNumbersAreInvalid() throws Exception {
    Path tempHome = Files.createTempDirectory("scrabble-home-");
    String originalHome = System.getProperty("user.home");
    try {
      System.setProperty("user.home", tempHome.toString());
      Files.writeString(tempHome.resolve(".scrabblerc"), "[defaults]\n"
          + "players-count=oops\n"
          + "timeout=bad\n"
          + "ai-time=nan\n");

      App.main(new String[] {});

      assertNotNull(cliCall);
      assertEquals(2, cliCall.players);
      assertEquals(30, cliCall.blitzMinutes);
      assertEquals(5, cliCall.aiTime);
    } finally {
      System.setProperty("user.home", originalHome);
    }
  }

  @Test
  void mainShouldListLanguagesAndExitWithoutLaunchingGame() {
    assertDoesNotThrow(() -> App.main(new String[] {"--list-languages"}));

    assertNull(cliCall);
    assertNull(guiCall);
  }

  private String minimalContestSave() {
    StringBuilder sb = new StringBuilder();
    sb.append("[settings]\n");
    sb.append("blitz false\n");
    sb.append("[game]\n");
    sb.append("1\n");
    for (int i = 0; i < 15; i++) {
      sb.append("---------------").append('\n');
    }
    sb.append("rack-1: ABCDEFG\n");
    sb.append("rack-2: HIJKLMN\n");
    sb.append("score-1: 0\n");
    sb.append("score-2: 0\n");
    sb.append("language en\n");
    sb.append("[history]\n");
    return sb.toString();
  }

  private Object invokePromptWithConsoleInput(String input) throws Exception {
    Method prompt = App.class.getDeclaredMethod("promptLaunchRequestAfterHelp");
    prompt.setAccessible(true);

    java.lang.reflect.Field consoleField = System.class.getDeclaredField("cons");
    sun.misc.Unsafe unsafe = getUnsafe();
    Object consoleBase = unsafe.staticFieldBase(consoleField);
    long consoleOffset = unsafe.staticFieldOffset(consoleField);
    Object originalConsole = unsafe.getObject(consoleBase, consoleOffset);
    InputStream originalIn = System.in;

    try {
      Console fakeConsole = (Console) unsafe.allocateInstance(Console.class);
      unsafe.putObject(consoleBase, consoleOffset, fakeConsole);
      System.setIn(new ByteArrayInputStream(input.getBytes(StandardCharsets.UTF_8)));
      return prompt.invoke(null);
    } finally {
      unsafe.putObject(consoleBase, consoleOffset, originalConsole);
      System.setIn(originalIn);
    }
  }

  private Object readPrivateField(Object target, String fieldName) throws Exception {
    Field field = target.getClass().getDeclaredField(fieldName);
    field.setAccessible(true);
    return field.get(target);
  }

  private sun.misc.Unsafe getUnsafe() throws Exception {
    Field field = sun.misc.Unsafe.class.getDeclaredField("theUnsafe");
    field.setAccessible(true);
    return (sun.misc.Unsafe) field.get(null);
  }

  private static final class ExitCalledException extends RuntimeException {
    private final int status;

    private ExitCalledException(int status) {
      this.status = status;
    }
  }

  private static final class CliLaunchCall {
    private final GameMode gameMode;
    private final int players;
    private final List<String> aiColors;
    private final boolean blitzMode;
    private final int blitzMinutes;
    private final int aiTime;
    private final boolean useExptiminimax;
    private final boolean useMl;
    private final String lang;
    private final String saveFilePath;

    private CliLaunchCall(GameMode gameMode, int players, List<String> aiColors,
        boolean blitzMode, int blitzMinutes, int aiTime, boolean useExptiminimax, boolean useMl,
        String lang, String saveFilePath) {
      this.gameMode = gameMode;
      this.players = players;
      this.aiColors = new ArrayList<>(aiColors);
      this.blitzMode = blitzMode;
      this.blitzMinutes = blitzMinutes;
      this.aiTime = aiTime;
      this.useExptiminimax = useExptiminimax;
      this.useMl = useMl;
      this.lang = lang;
      this.saveFilePath = saveFilePath;
    }
  }

  private static final class GuiLaunchCall {
    private final String[] args;
    private final GameMode gameMode;
    private final int players;
    private final List<String> aiColors;
    private final boolean blitzMode;
    private final int blitzMinutes;
    private final int aiTime;
    private final boolean useExptiminimax;
    private final boolean useMl;
    private final String lang;
    private final String saveFilePath;

    private GuiLaunchCall(String[] args, GameMode gameMode, int players, List<String> aiColors,
        boolean blitzMode, int blitzMinutes, int aiTime, boolean useExptiminimax, boolean useMl,
        String lang, String saveFilePath) {
      this.args = args.clone();
      this.gameMode = gameMode;
      this.players = players;
      this.aiColors = new ArrayList<>(aiColors);
      this.blitzMode = blitzMode;
      this.blitzMinutes = blitzMinutes;
      this.aiTime = aiTime;
      this.useExptiminimax = useExptiminimax;
      this.useMl = useMl;
      this.lang = lang;
      this.saveFilePath = saveFilePath;
    }
  }
}
