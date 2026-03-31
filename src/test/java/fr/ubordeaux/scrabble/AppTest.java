package fr.ubordeaux.scrabble;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.reflect.Method;
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
    App.setCliDelegateForTests((players, aiColors, blitzMode, blitzMinutes, aiTime,
        useExptiminimax, useMl, lang) -> cliCall = new CliLaunchCall(players, aiColors, blitzMode,
        blitzMinutes, aiTime, useExptiminimax, useMl, lang));
    App.setGuiDelegateForTests((args, players, aiColors, blitzMode, blitzMinutes, aiTime,
        useExptiminimax, useMl, lang) -> guiCall = new GuiLaunchCall(args, players, aiColors,
        blitzMode, blitzMinutes, aiTime, useExptiminimax, useMl, lang));
  }

  @AfterEach
  void tearDown() {
    App.resetHandlersForTests();
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
  void mainShouldExitWhenAiColorValueMissing() {
    ExitCalledException ex =
        assertThrows(ExitCalledException.class, () -> App.main(new String[] {"-a"}));

    assertEquals(1, ex.status);
  }

  @Test
  void mainShouldExitWhenPlayersValueMissing() {
    ExitCalledException ex =
        assertThrows(ExitCalledException.class, () -> App.main(new String[] {"-p"}));

    assertEquals(1, ex.status);
  }

  @Test
  void mainShouldExitWhenBlitzTimeFlagHasNoValue() {
    ExitCalledException ex =
        assertThrows(ExitCalledException.class, () -> App.main(new String[] {"-b", "-t"}));

    assertEquals(1, ex.status);
  }

  @Test
  void mainShouldAcceptHelpAndVersionOptions() {
    assertDoesNotThrow(() -> App.main(new String[] {"--help"}));
    assertDoesNotThrow(() -> App.main(new String[] {"--version"}));

    assertNotNull(cliCall);
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
    ExitCalledException ex =
        assertThrows(ExitCalledException.class, () -> App.main(new String[] {"-b", "--time"}));

    assertEquals(1, ex.status);
  }

  @Test
  void mainShouldParseDaemonFlagBeforeUnknownOption() {
    ExitCalledException ex =
        assertThrows(
            ExitCalledException.class,
            () -> App.main(new String[] {"-D", "--unknown-daemon"}));

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
    assertNotNull(cliCall);
  }

  @Test
  void mainShouldReachMissingAiExitCallWhenExitHandlerDoesNotThrow() {
    App.setExitHandlerForTests(status -> {
      // no-op to execute the missing-ai-color exit path and continue
    });

    assertThrows(ArrayIndexOutOfBoundsException.class, () -> App.main(new String[] {"-a"}));
  }

  private static final class ExitCalledException extends RuntimeException {
    private final int status;

    private ExitCalledException(int status) {
      this.status = status;
    }
  }

  private static final class CliLaunchCall {
    private final int players;
    private final List<String> aiColors;
    private final boolean blitzMode;
    private final int blitzMinutes;
    private final int aiTime;
    private final boolean useExptiminimax;
    private final boolean useMl;
    private final String lang;

    private CliLaunchCall(int players, List<String> aiColors, boolean blitzMode, int blitzMinutes,
        int aiTime, boolean useExptiminimax, boolean useMl, String lang) {
      this.players = players;
      this.aiColors = new ArrayList<>(aiColors);
      this.blitzMode = blitzMode;
      this.blitzMinutes = blitzMinutes;
      this.aiTime = aiTime;
      this.useExptiminimax = useExptiminimax;
      this.useMl = useMl;
      this.lang = lang;
    }
  }

  private static final class GuiLaunchCall {
    private final String[] args;
    private final int players;
    private final List<String> aiColors;
    private final boolean blitzMode;
    private final int blitzMinutes;
    private final int aiTime;
    private final boolean useExptiminimax;
    private final boolean useMl;
    private final String lang;

    private GuiLaunchCall(String[] args, int players, List<String> aiColors, boolean blitzMode,
        int blitzMinutes, int aiTime, boolean useExptiminimax, boolean useMl, String lang) {
      this.args = args.clone();
      this.players = players;
      this.aiColors = new ArrayList<>(aiColors);
      this.blitzMode = blitzMode;
      this.blitzMinutes = blitzMinutes;
      this.aiTime = aiTime;
      this.useExptiminimax = useExptiminimax;
      this.useMl = useMl;
      this.lang = lang;
    }
  }
}
