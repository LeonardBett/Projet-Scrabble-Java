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
 * Handles the global configuration file ~/.scrabblerc (Requirement F2).
 * Manages defaults, auto-creation, and supression by CLI options.
 */
public class ConfigLoader {
  private static final String FILE_NAME = ".scrabblerc";
  private final Map<String, String> configMap = new HashMap<>();

  /**
   * Loads the configuration from the user's HOME directory at startup (F2, F4).
   */
  public void loadConfig() {
    Path configPath = Paths.get(System.getProperty("user.dir"), FILE_NAME);

    // F2: Si le fichier n'est pas présent, en créer un minimal [cite: 86]
    if (!Files.exists(configPath)) {
      createMinimalConfig(configPath);
      return;
    }

    // F2: Lecture du fichier s'il est présent
    try (BufferedReader reader = Files.newBufferedReader(configPath)) {
      String line;
      boolean inDefaultsSection = false;

      while ((line = reader.readLine()) != null) {
        // 1. Supprimer les commentaires (tout ce qui est après #) et trimmer
        line = line.split("#")[0].trim();

        // 2. Ignorer les lignes vides
        if (line.isEmpty()) {
          continue;
        }

        // 3. Détecter la section (Maintenant ça marchera même avec un commentaire !)
        if (line.equalsIgnoreCase("[defaults]")) {
          inDefaultsSection = true;
          continue;
        }

        // 4. Parser les clés
        if (inDefaultsSection && line.contains("=")) {
          String[] parts = line.split("=", 2);
          if (parts.length == 2) {
            configMap.put(parts[0].trim(), parts[1].trim());
          }
        }
      }
    } catch (IOException e) {
      // F2: Si le fichier est présent mais invalide, afficher un avertissement
      System.err.println("Warning: .scrabblerc is present but invalid. Using standard defaults.");
    }
  }

  /**
   * Creates a minimal .scrabblerc file with default values (Requirement F2).
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
      // Échec silencieux de la création (le programme continue normalement) [cite: 86]
    }
  }

  /**
   * Returns a configured value or a fallback if the key is missing.
   */
  public String getOption(String key, String fallback) {
    return configMap.getOrDefault(key, fallback);
  }
}