package com.alkisum.android.vlcremote.utils;

import java.text.SimpleDateFormat;
import java.util.Locale;

/**
 * Utility class to format values.
 *
 * @author Alkisum
 * @version 1.0
 * @since 1.0
 */
public final class Format {

    /**
     * Format for build date.
     */
    public static final SimpleDateFormat DATE_BUILD =
            new SimpleDateFormat("MMM. dd, yyyy", Locale.getDefault());

    /**
     * Format constructor.
     */
    private Format() {

    }
}
