package com.alkisum.android.sofatime.events;

/**
 * Class defining request event for EventBus.
 *
 * @author Alkisum
 * @version 1.0
 * @since 1.0
 */
public class RequestEvent {

    /**
     * Request command.
     */
    private final String command;

    /**
     * Request value.
     */
    private final String val;

    /**
     * Flag set to true if the request response must be ignores, false
     * otherwise.
     */
    private final boolean ignoreResponse;

    /**
     * RequestEvent constructor.
     *
     * @param command Request command
     */
    public RequestEvent(final String command) {
        this.command = command;
        this.val = "";
        this.ignoreResponse = false;
    }

    /**
     * RequestCommand constructor.
     *
     * @param command Request command
     * @param val     Request value.
     */
    public RequestEvent(final String command, final String val) {
        this.command = command;
        this.val = val;
        this.ignoreResponse = false;
    }

    /**
     * RequestCommand constructor.
     *
     * @param command        Request command
     * @param val            Request value
     * @param ignoreResponse true if the request response must be ignores, false
     *                       otherwise
     */
    public RequestEvent(final String command, final String val,
                        final boolean ignoreResponse) {
        this.command = command;
        this.val = val;
        this.ignoreResponse = ignoreResponse;
    }

    /**
     * @return Request command
     */
    public final String getCommand() {
        return command;
    }

    /**
     * @return Request value
     */
    public final String getVal() {
        return val;
    }

    /**
     * @return true if the request response must be ignores, false otherwise
     */
    public final boolean isIgnoreResponse() {
        return ignoreResponse;
    }
}
