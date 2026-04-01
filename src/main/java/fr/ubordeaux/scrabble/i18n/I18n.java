package fr.ubordeaux.scrabble.i18n;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

/**
 * Minimal i18n helper backed by ResourceBundle files.
 */
public final class I18n {

  private static final String BUNDLE_BASE = "i18n.messages";
  private static final String DEFAULT_LANG = "en";

  private static String currentLanguage = DEFAULT_LANG;
  private static ResourceBundle bundle = loadBundle(DEFAULT_LANG);

  private I18n() {
  }

  /**
   * Sets the active language code.
   *
   * @param lang language code ("en" or "fr")
   */
  public static synchronized void setLanguage(String lang) {
    String normalized = normalize(lang);
    currentLanguage = normalized;
    bundle = loadBundle(normalized);
  }

  /**
   * Returns the currently active language code.
   *
   * @return active language code
   */
  public static synchronized String getLanguage() {
    return currentLanguage;
  }

  /**
   * Gets a translated string by key.
   *
   * @param key translation key
   * @return translated value, or !key! when missing
   */
  public static synchronized String translate(String key) {
    try {
      return bundle.getString(key);
    } catch (MissingResourceException ex) {
      return "!" + key + "!";
    }
  }

  /**
   * Gets a translated string and formats it with arguments.
   *
   * @param key translation key
   * @param args formatting arguments
   * @return formatted translated value
   */
  public static synchronized String translate(String key, Object... args) {
    return MessageFormat.format(translate(key), args);
  }

  /**
   * Lists supported language codes based on available message bundles.
   *
   * @return supported language codes (e.g. en, fr)
   */
  public static synchronized List<String> getSupportedLanguages() {
    List<String> languages = new ArrayList<>();
    for (String code : List.of("en", "fr")) {
      String resourceName = "i18n/messages_" + code + ".properties";
      if (I18n.class.getClassLoader().getResource(resourceName) != null) {
        languages.add(code);
      }
    }
    if (!languages.contains(DEFAULT_LANG)) {
      languages.add(DEFAULT_LANG);
    }
    return languages;
  }

  private static ResourceBundle loadBundle(String lang) {
    Locale locale = Locale.forLanguageTag(lang);
    return ResourceBundle.getBundle(BUNDLE_BASE, locale);
  }

  private static String normalize(String lang) {
    if (lang == null || lang.isBlank()) {
      return DEFAULT_LANG;
    }
    String normalized = lang.trim().toLowerCase();
    if (!"fr".equals(normalized)) {
      return DEFAULT_LANG;
    }
    return "fr";
  }
}
