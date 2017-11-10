package com.alkisum.android.vlcremote.utils;

/**
 * Utility class for request commands.
 *
 * @author Alkisum
 * @version 1.0
 * @since 1.0
 */
public final class Cmd {

    /**
     * Toggle fullscreen.
     */
    public static final String FULLSCREEN = "fullscreen";

    /**
     * Jump to next item.
     */
    public static final String NEXT = "pl_next";

    /**
     * Toggle pause. If current state was 'stop', play item.
     */
    public static final String PAUSE = "pl_pause";

    /**
     * Play playlist item.
     */
    public static final String PLAY = "pl_play";

    /**
     * Jump to previous item.
     */
    public static final String PREVIOUS = "pl_previous";

    /**
     * Seek to <val>.
     * Allowed values are of the form:
     * [+ or -][<int><H or h>:][<int><M or m or '>:]
     * [<int><nothing or S or s or ">] or [+ or -]<int>%
     * (value between [ ] are optional, value between < > are mandatory)
     */
    public static final String SEEK = "seek";

    /**
     * Set volume level to <val> (can be absolute integer, percent
     * or +/- relative value).
     * Allowed values are of the form: +<int>, -<int>, <int> or <int>%
     */
    public static final String VOLUME = "volume";

    /**
     * Cmd constructor.
     */
    private Cmd() {

    }
}
