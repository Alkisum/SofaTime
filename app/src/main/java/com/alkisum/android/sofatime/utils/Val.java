package com.alkisum.android.sofatime.utils;

/**
 * Utility class for request values.
 *
 * @author Alkisum
 * @version 1.0
 * @since 1.0
 */
public final class Val {

    /**
     * Seek value to rewind.
     */
    public static final String REWIND = "-5s";

    /**
     * Seek value to forward.
     */
    public static final String FORWARD = "+5s";

    /**
     * Volume value.
     */
    private static final int VOLUME = 5;

    /**
     * Val constructor.
     */
    private Val() {

    }

    /**
     * @param sign "+" for volume up, "-" for volume down
     * @return Format value for volume command
     */
    public static String volume(final String sign) {
        return sign + String.valueOf(Math.round(VOLUME * 256 / 100f));
    }
}
