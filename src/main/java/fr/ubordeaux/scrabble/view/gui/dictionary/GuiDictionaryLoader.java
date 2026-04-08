package fr.ubordeaux.scrabble.view.gui.dictionary;

import fr.ubordeaux.scrabble.controller.dictionary.DictionaryLoaderController;
import fr.ubordeaux.scrabble.model.dictionary.Gaddag;
import java.io.IOException;

/**
 * Loads GUI dictionaries into a Gaddag with fallback to English when needed.
 */
public final class GuiDictionaryLoader {

  private GuiDictionaryLoader() {
  }

  /**
   * Load dictionary entries for the provided language.
   *
   * @param classLoader classloader used to read dictionary resources
   * @param language language code ("en" or "fr")
   * @return populated gaddag
   * @throws IOException when stream reading fails
   */
  public static Gaddag load(ClassLoader classLoader, String language) throws IOException {
    return DictionaryLoaderController.load(classLoader, language);
  }
}
