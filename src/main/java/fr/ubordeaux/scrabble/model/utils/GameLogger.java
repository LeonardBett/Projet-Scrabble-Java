package fr.ubordeaux.scrabble.model.utils;

/**
 * Utility class to handle verbose and debug logging across the application.
 * Prevents cluttering standard output during normal gameplay.
 */
public class GameLogger {

  private static boolean verbose = false;
  private static boolean debug = false;

  private GameLogger() {
    // Private constructor to prevent instantiation of utility class
  }

  public static void setVerbose(boolean isVerbose) {
    verbose = isVerbose;
  }

  public static void setDebug(boolean isDebug) {
    debug = isDebug;
  }

  public static boolean isVerbose() {
    return verbose;
  }

  public static boolean isDebug() {
    return debug;
  }

  /**
   * Logs a message only if verbose or debug mode is enabled.
   *
   * @param message The detailed message to display.
   */
  public static void logVerbose(String message) {
    if (verbose) {
      System.out.println("[VERBOSE] " + message);
    }
  }

  /**
   * Logs a message only if debug mode is enabled.
   *
   * @param message The technical debug message to display.
   */
  public static void logDebug(String message) {
    if (debug) {
      System.out.println("[DEBUG] " + message);
    }
  }

  /**
   * Logs an error. Prints the stack trace only in debug mode.
   *
   * @param message The error message.
   * @param e       The exception caught (can be null).
   */
  public static void logError(String message, Exception e) {
    System.err.println("[ERROR] " + message);
    if (debug && e != null) {
      e.printStackTrace();
    }
  }

}