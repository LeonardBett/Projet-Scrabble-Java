package fr.ubordeaux.scrabble.controller.dictionary;

import fr.ubordeaux.scrabble.model.dictionary.Gaddag;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * Loads dictionaries into a Gaddag with fallback to English when needed.
 */
public final class DictionaryLoaderController {

  private DictionaryLoaderController() {
  }

  /**
   * Loads dictionary entries for the provided language.
   *
   * @param classLoader class loader used to read dictionary resources
   * @param language language code
   * @return populated gaddag
   * @throws IOException when stream reading fails
   */
  public static Gaddag load(ClassLoader classLoader, String language) throws IOException {
    Gaddag gaddag = new Gaddag();
    String dictPath = "dictionaries/lexicon_" + language + ".txt";

    try (InputStream is = classLoader.getResourceAsStream(dictPath)) {
      if (is != null) {
        fillGaddag(gaddag, is);
      } else if (!"en".equals(language)) {
        try (InputStream fallback =
                 classLoader.getResourceAsStream("dictionaries/lexicon_en.txt")) {
          if (fallback != null) {
            fillGaddag(gaddag, fallback);
          }
        }
      }
    }

    return gaddag;
  }

  private static void fillGaddag(Gaddag gaddag, InputStream is) throws IOException {
    try (BufferedReader br = new BufferedReader(new InputStreamReader(is))) {
      String line;
      while ((line = br.readLine()) != null) {
        String entry = line.trim();
        if (!entry.isEmpty()) {
          gaddag.add(entry);
        }
      }
    }
  }
}