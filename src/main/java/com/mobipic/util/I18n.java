package com.mobipic.util;

import java.text.MessageFormat;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

/**
 * Access to the UI strings. French is the default language; English is used on English systems.
 */
public final class I18n {

    private static final ResourceBundle BUNDLE = ResourceBundle.getBundle("com.mobipic.i18n.messages");

    private I18n() {
    }

    public static ResourceBundle bundle() {
        return BUNDLE;
    }

    /** @return the string for the key, or the key itself if it is missing */
    public static String get(String key) {
        try {
            return BUNDLE.getString(key);
        } catch (MissingResourceException e) {
            return key;
        }
    }

    /** Formats a message with {@link MessageFormat} placeholders ({@code {0}}, {@code {1}}...). */
    public static String format(String key, Object... args) {
        return MessageFormat.format(get(key), args);
    }
}
