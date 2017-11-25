package com.alkisum.android.sofatime.events;

/**
 * Class defining status event for EventBus.
 *
 * @author Alkisum
 * @version 1.0
 * @since 1.0
 */
public class StatusEvent {

    /**
     * State set in status.
     */
    private final String state;

    /**
     * Title set in status.
     */
    private final String title;

    /**
     * Time set in status.
     */
    private final int time;

    /**
     * Length set in status.
     */
    private final int length;

    /**
     * Volume set in status.
     */
    private final float volume;

    /**
     * StatusEvent constructor.
     *
     * @param state  State set in status
     * @param title  Title set in status
     * @param time   Time set in status
     * @param length Length set in status
     * @param volume Volume set in status
     */
    public StatusEvent(final String state, final String title, final int time,
                       final int length, final float volume) {
        this.state = state;
        this.title = title;
        this.time = time;
        this.length = length;
        this.volume = volume;
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

    /**
     * @return Volume set in status
     */
    public final float getVolume() {
        return volume;
    }
}
