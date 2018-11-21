package com.alkisum.android.sofatime.utils;

import java.text.SimpleDateFormat;
import java.util.Locale;

/**
 * Utility class to format values.
 *
 * @author Alkisum
 * @version 1.1
 * @since 1.0
 */
public final class Format {

    /**
     * Format constructor.
     */
    private Format() {

    }

    /**
     * @return format for build date.
     */
    public static SimpleDateFormat getDateBuild() {
        return new SimpleDateFormat("MMM. dd, yyyy", Locale.getDefault());
    }
}
