package fr.ubordeaux.scrabble.model.savefiles;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

/**
 * Handles the global configuration file .scrabblerc (Requirement F2).
 * This class is responsible for loading default settings from the project root
 * or home directory, managing automatic file creation, and providing fallback
 * values for game parameters.
 */
public class ConfigLoader {
  private static final String FILE_NAME = ".scrabblerc";
  private final Map<String, String> configMap = new HashMap<>();

  /**
   * Initializes a new ConfigLoader instance with an empty configuration map.
   */
  public ConfigLoader() {
  }

  /**
   * Loads the configuration from the file system at startup (Requirement F2, F4).
   * It looks for the file in the project root directory. If the file is missing,
   * it triggers the creation of a minimal configuration file with standard
   * defaults.
   */
  public void loadConfig() {
    Path configPath = Paths.get(System.getProperty("user.home"), FILE_NAME);

    // If no files found, create a new one
    if (!Files.exists(configPath)) {
      createMinimalConfig(configPath);
      return;
    }

    // If file exists, read it
    try (BufferedReader reader = Files.newBufferedReader(configPath)) {
      String line;
      boolean inDefaultsSection = false;

      while ((line = reader.readLine()) != null) {
        // Requirement F22: Remove comments (content after #) and trim whitespace
        line = line.split("#")[0].trim();

        if (line.isEmpty()) {
          continue;
        }

        if (line.equalsIgnoreCase("[defaults]")) {
          inDefaultsSection = true;
          continue;
        }

        // Parse key=value pairs within the [defaults] section
        if (inDefaultsSection && line.contains("=")) {
          String[] parts = line.split("=", 2);
          if (parts.length == 2) {
            configMap.put(parts[0].trim(), parts[1].trim());
          }
        }
      }
    } catch (IOException e) {
      System.err.println("Warning: .scrabblerc is present but invalid. Using standard defaults.");
    }
  }

  /**
   * Creates a minimal .scrabblerc file with default values (Requirement F2).
   * Standard defaults include verbose=false, blitz=false, timeout=30, and language=en.
   *
   * @param path The file system path where the minimal configuration should be created.
   */
  private void createMinimalConfig(Path path) {
    try (BufferedWriter writer = Files.newBufferedWriter(path);
         PrintWriter out = new PrintWriter(writer)) {
      out.println("[defaults] # Scrabble default configuration");
      out.println("verbose=false");
      out.println("blitz=false");
      out.println("timeout=30");
      out.println("language=en");
    } catch (IOException e) {
      // Quiet error, the program continues normally
    }
  }

  /**
   * Retrieves a configuration option by its key.
   *
   * @param key The name of the configuration parameter (e.g., "language", "blitz").
   * @param fallback The value to return if the key is not found in the configuration.
   * @return The configured value as a String, or the fallback value if the key is missing.
   */
  public String getOption(String key, String fallback) {
    return configMap.getOrDefault(key, fallback);
  }
}