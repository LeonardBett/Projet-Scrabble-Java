package fr.ubordeaux.scrabble.i18n;

import java.text.MessageFormat;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

/**
 * Centralized internationalization helper for CLI and GUI text resources.
 */
public final class I18n {

  private static final String BUNDLE_BASE_NAME = "i18n.messages";
  private static final Locale LOCALE_FR = Locale.forLanguageTag("fr");
  private static final Locale LOCALE_EN = Locale.ENGLISH;

  private static Locale currentLocale = LOCALE_EN;
  private static ResourceBundle bundle = ResourceBundle.getBundle(BUNDLE_BASE_NAME, currentLocale);

  private I18n() {}

  /**
   * Sets the active language used by the whole application.
   *
   * @param lang language code ("fr" or "en")
   */
  public static void setLanguage(String lang) {
    currentLocale = "fr".equalsIgnoreCase(lang) ? LOCALE_FR : LOCALE_EN;
    bundle = ResourceBundle.getBundle(BUNDLE_BASE_NAME, currentLocale);
  }

  /**
   * Returns true when current language is French.
   *
   * @return true for french locale
   */
  public static boolean isFrench() {
    return "fr".equals(currentLocale.getLanguage());
  }

  /**
   * Resolves and formats a localized text.
   *
   * @param key resource key
   * @param args optional format arguments
   * @return localized and formatted text
   */
  public static String tr(String key, Object... args) {
    try {
      String pattern = bundle.getString(key);
      return args.length == 0 ? pattern : MessageFormat.format(pattern, args);
    } catch (MissingResourceException e) {
      return key;
    }
  }
}
