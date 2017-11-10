package com.alkisum.android.vlcremote.events;

/**
 * Class defining error event for EventBus.
 *
 * @author Alkisum
 * @version 1.0
 * @since 1.0
 */
public class StatusEvent {

    /**
     * State set in status.
     */
    private String state;

    /**
     * Title set in status.
     */
    private String title;

    /**
     * Time set in status.
     */
    private int time;

    /**
     * Length set in status.
     */
    private int length;

    /**
     * StatusEvent constructor.
     *
     * @param state  State set in status
     * @param title  Title set in status
     * @param time   Time set in status
     * @param length Length set in status
     */
    public StatusEvent(final String state, final String title, final int time,
                       final int length) {
        this.state = state;
        this.title = title;
        this.time = time;
        this.length = length;
    }

    /**
     * @return State set in status
     */
    public final String getState() {
        return state;
    }

    /**
     * @return Title set in status
     */
    public final String getTitle() {
        return title;
    }

    /**
     * @return Time set in status
     */
    public final int getTime() {
        return time;
    }

    /**
     * @return Length set in status
     */
    public final int getLength() {
        return length;
    }
}
