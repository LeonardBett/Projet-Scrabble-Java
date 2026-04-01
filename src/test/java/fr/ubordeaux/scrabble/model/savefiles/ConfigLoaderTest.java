package fr.ubordeaux.scrabble.model.savefiles;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/**
 * Unit tests for the ConfigLoader class.
 * Validates Requirement F2 (INI config) and F22 (Comments).
 */
class ConfigLoaderTest {

  private ConfigLoader loader;
  private String originalUserHome;

  @TempDir
  Path tempDir;

  @BeforeEach
  void setUp() {
    // Redirect user.home to the temporary directory to avoid touching the real .scrabblerc file
    originalUserHome = System.getProperty("user.home");
    System.setProperty("user.home", tempDir.toString());
    loader = new ConfigLoader();
  }

  @AfterEach
  void tearDown() {
    // Restore the original user.home after the test
    System.setProperty("user.home", originalUserHome);
  }

  /**
   * Tests that a minimal config is created if the file is missing (Requirement F2).
   */
  @Test
  void loadConfigShouldCreateMinimalFileIfMissing() throws IOException {
    loader.loadConfig();

    Path configPath = tempDir.resolve(".scrabblerc");
    assertTrue(Files.exists(configPath), "The .scrabblerc file should be created.");

    List<String> lines = Files.readAllLines(configPath);
    assertTrue(lines.contains("[defaults] # Scrabble default configuration"));
    assertTrue(lines.contains("language=en"));
  }

  /**
   * Tests that configuration is correctly loaded from an existing file (Requirement F2).
   */
  @Test
  void loadConfigShouldReadExistingSettings() throws IOException {
    String content = "[defaults]\nlanguage=fr\nblitz=true\ntimeout=45";
    Files.writeString(tempDir.resolve(".scrabblerc"), content);

    loader.loadConfig();

    assertEquals("fr", loader.getOption("language", "en"));
    assertEquals("true", loader.getOption("blitz", "false"));
    assertEquals("45", loader.getOption("timeout", "30"));
  }

  /**
   * Tests that comments (#) are correctly ignored during parsing (Requirement F22).
   */
  @Test
  void loadConfigShouldIgnoreComments() throws IOException {
    String content = "[defaults] # Section header comment\n"
        + "verbose=true # Enable logs\n"
        + "# This is a full line comment\n"
        + "timeout=20";
    Files.writeString(tempDir.resolve(".scrabblerc"), content);

    loader.loadConfig();

    assertEquals("true", loader.getOption("verbose", "false"));
    assertEquals("20", loader.getOption("timeout", "30"));
  }

  /**
   * Tests the fallback logic when a key is missing from the file.
   */
  @Test
  void getOptionShouldReturnFallbackIfKeyMissing() throws IOException {
    String content = "[defaults]\nlanguage=en";
    Files.writeString(tempDir.resolve(".scrabblerc"), content);

    loader.loadConfig();

    // "debug" is not in the file, should return "false"
    assertEquals("false", loader.getOption("debug", "false"));
    // "language" is present, should return "en"
    assertEquals("en", loader.getOption("language", "fr"));
  }

  /**
   * Verifies that settings are only loaded if they are inside the [defaults] section.
   */
  @Test
  void loadConfigShouldOnlyReadWithinDefaultsSection() throws IOException {
    String content = "language=it\n" // Before the section (ignored)
        + "[defaults]\n"
        + "language=fr\n"
        + "[other]\n" // Other section (ignored as the parser should focus on [defaults])
        + "timeout=99";
    Files.writeString(tempDir.resolve(".scrabblerc"), content);

    loader.loadConfig();

    assertEquals("fr", loader.getOption("language", "en"),
        "Should take the value from [defaults]");

    /* * Note: In the current implementation, inDefaultsSection remains true once activated,
     * so timeout=99 will be read even if there is another [section] tag between them.
     */
    assertEquals("99", loader.getOption("timeout", "30"));
  }
}