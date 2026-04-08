package fr.ubordeaux.scrabble.controller.config;

import fr.ubordeaux.scrabble.controller.GameController;
import fr.ubordeaux.scrabble.model.utils.GameLogger;

/**
 * Immutable snapshot of controller configuration used to rebuild a new game with the same
 * settings.
 */
public final class ControllerConfigSnapshot {
  private final String language;
  private final int playerCount;
  private final boolean superScrabbleMode;
  private final boolean blitzMode;
  private final int blitzMinutes;
  private final int aiTime;
  private final boolean expectiminimax;
  private final boolean ml;
  private final String dictionaryPath;
  private final boolean debug;
  private final boolean verbose;

  private ControllerConfigSnapshot(String language, int playerCount, boolean superScrabbleMode,
      boolean blitzMode, int blitzMinutes, int aiTime, boolean expectiminimax, boolean ml,
      String dictionaryPath, boolean debug, boolean verbose) {
    this.language = language;
    this.playerCount = playerCount;
    this.superScrabbleMode = superScrabbleMode;
    this.blitzMode = blitzMode;
    this.blitzMinutes = blitzMinutes;
    this.aiTime = aiTime;
    this.expectiminimax = expectiminimax;
    this.ml = ml;
    this.dictionaryPath = dictionaryPath;
    this.debug = debug;
    this.verbose = verbose;
  }

  /**
   * Capture the current controller configuration and logger flags.
   *
   * @param controller game controller
   * @return immutable snapshot
   */
  public static ControllerConfigSnapshot capture(GameController controller) {
    return new ControllerConfigSnapshot(
        controller.configuredLanguage(),
        controller.configuredPlayerCount(),
        controller.configuredSuperMode(),
        controller.configuredBlitzMode(),
        controller.configuredBlitzMinutes(),
        controller.configuredAiTime(),
        controller.isExpectiminimaxEnabled(),
        controller.isMlEnabled(),
        controller.getDictionaryPathOverride(),
        GameLogger.isDebug(),
        GameLogger.isVerbose());
  }

  /**
   * Re-apply snapshot values to a controller.
   *
   * @param controller game controller
   */
  public void applyTo(GameController controller) {
    controller.applyConfiguration("language", language);
    if (playerCount > 0) {
      controller.applyConfiguration("players", String.valueOf(playerCount));
    }
    controller.applyConfiguration("super-scrabble", String.valueOf(superScrabbleMode));
    controller.applyConfiguration("blitz", String.valueOf(blitzMode));
    controller.applyConfiguration("timeout", String.valueOf(blitzMinutes));
    controller.applyConfiguration("ai-time", String.valueOf(aiTime));
    controller.applyConfiguration("ai-exptiminimax", String.valueOf(expectiminimax));
    controller.applyConfiguration("ai-ml", String.valueOf(ml));
    controller.applyConfiguration("dictionary", dictionaryPath);
    controller.applyConfiguration("debug", String.valueOf(debug));
    controller.applyConfiguration("verbose", String.valueOf(verbose));
  }

  /**
   * Returns the captured language code.
   *
   * @return the language code
   */
  public String language() {
    return language;
  }

  /**
   * Returns the captured player count.
   *
   * @return the player count
   */
  public int playerCount() {
    return playerCount;
  }

  /**
   * Indicates whether super Scrabble mode was captured.
   *
   * @return true when super Scrabble mode is enabled
   */
  public boolean superScrabbleMode() {
    return superScrabbleMode;
  }

  /**
   * Indicates whether blitz mode was captured.
   *
   * @return true when blitz mode is enabled
   */
  public boolean blitzMode() {
    return blitzMode;
  }

  /**
   * Returns the captured blitz timeout in minutes.
   *
   * @return the blitz timeout in minutes
   */
  public int blitzMinutes() {
    return blitzMinutes;
  }
}