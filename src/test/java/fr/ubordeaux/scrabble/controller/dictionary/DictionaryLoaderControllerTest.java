package fr.ubordeaux.scrabble.controller.dictionary;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import fr.ubordeaux.scrabble.model.dictionary.Gaddag;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.Test;

class DictionaryLoaderControllerTest {

  @Test
  void loadShouldReadDictionaryForRequestedLanguage() throws IOException {
    ClassLoader classLoader = new ClassLoader(getClass().getClassLoader()) {
      @Override
      public InputStream getResourceAsStream(String name) {
        if ("dictionaries/lexicon_fr.txt".equals(name)) {
          return new ByteArrayInputStream("apple\n\nbanana\n".getBytes(StandardCharsets.UTF_8));
        }
        return null;
      }
    };

    Gaddag gaddag = DictionaryLoaderController.load(classLoader, "fr");

    assertNotNull(gaddag);
    assertTrue(gaddag.containsWord("APPLE"));
    assertTrue(gaddag.containsWord("BANANA"));
    assertFalse(gaddag.containsWord("ORANGE"));
  }

  @Test
  void loadShouldFallbackToEnglishWhenRequestedDictionaryIsMissing() throws IOException {
    ClassLoader classLoader = new ClassLoader(getClass().getClassLoader()) {
      @Override
      public InputStream getResourceAsStream(String name) {
        if ("dictionaries/lexicon_en.txt".equals(name)) {
          return new ByteArrayInputStream("hello\nworld\n".getBytes(StandardCharsets.UTF_8));
        }
        return null;
      }
    };

    Gaddag gaddag = DictionaryLoaderController.load(classLoader, "fr");

    assertTrue(gaddag.containsWord("HELLO"));
    assertTrue(gaddag.containsWord("WORLD"));
    assertFalse(gaddag.containsWord("BONJOUR"));
  }

  @Test
  void loadShouldReturnEmptyDictionaryWhenNoResourceExists() throws IOException {
    ClassLoader classLoader = new ClassLoader(getClass().getClassLoader()) {
      @Override
      public InputStream getResourceAsStream(String name) {
        return null;
      }
    };

    Gaddag gaddag = DictionaryLoaderController.load(classLoader, "fr");

    assertFalse(gaddag.containsWord("APPLE"));
    assertFalse(gaddag.containsWord("HELLO"));
  }

  @Test
  void loadShouldPropagateReadErrors() {
    ClassLoader classLoader = new ClassLoader(getClass().getClassLoader()) {
      @Override
      public InputStream getResourceAsStream(String name) {
        if ("dictionaries/lexicon_en.txt".equals(name)) {
          return new InputStream() {
            @Override
            public int read() throws IOException {
              throw new IOException("boom");
            }
          };
        }
        return null;
      }
    };

    assertThrows(IOException.class, () -> DictionaryLoaderController.load(classLoader, "en"));
  }
}