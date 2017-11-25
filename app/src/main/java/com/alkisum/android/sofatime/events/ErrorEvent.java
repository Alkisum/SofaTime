package com.alkisum.android.sofatime.events;

/**
 * Class defining error event for EventBus.
 *
 * @author Alkisum
 * @version 1.0
 * @since 1.0
 */
public class ErrorEvent {

    /**
     * Error title.
     */
    private final String title;

    /**
     * Error message.
     */
    private final String message;

    /**
     * ErrorEvent constructor.
     *
     * @param title   Title
     * @param message Message
     */
    public ErrorEvent(final String title, final String message) {
        this.title = title;
        this.message = message;
    }

    /**
     * @return Error title
     */
    public final String getTitle() {
        return title;
    }

    /**
     * @return Error message
     */
    public final String getMessage() {
        return message;
    }
}
