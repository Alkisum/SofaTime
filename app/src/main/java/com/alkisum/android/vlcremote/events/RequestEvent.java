package com.alkisum.android.vlcremote.events;


/**
 * Class defining error event for EventBus.
 *
 * @author Alkisum
 * @version 1.0
 * @since 1.0
 */
public class RequestEvent {

    /**
     * Request command.
     */
    private String command;

    /**
     * Request value.
     */
    private String val;

    /**
     * RequestEvent constructor.
     *
     * @param command Request command
     */
    public RequestEvent(final String command) {
        this.command = command;
        this.val = "";
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
}
