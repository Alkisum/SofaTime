package com.alkisum.android.vlcremote.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.constraint.ConstraintLayout;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.format.DateUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.SeekBar;
import android.widget.TextView;

import com.alkisum.android.vlcremote.R;
import com.alkisum.android.vlcremote.dialogs.ErrorDialog;
import com.alkisum.android.vlcremote.events.ErrorEvent;
import com.alkisum.android.vlcremote.events.RequestEvent;
import com.alkisum.android.vlcremote.events.StatusEvent;
import com.alkisum.android.vlcremote.net.VlcRequest;
import com.alkisum.android.vlcremote.utils.Cmd;
import com.alkisum.android.vlcremote.utils.Pref;
import com.alkisum.android.vlcremote.utils.State;
import com.alkisum.android.vlcremote.utils.Val;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Main activity showing controls for VLC.
 *
 * @author Alkisum
 * @version 1.0
 * @since 1.0
 */
public class MainActivity extends AppCompatActivity {

    /**
     * VlcRequest instance.
     */
    private VlcRequest vlcRequest;

    /**
     * Last state read from status.
     */
    private String lastState = null;

    /**
     * Flag set to true if the next status event should be skipped, false
     * otherwise. Must be used after sending a request to avoid the UI to
     * be updated due to a response sent before the request.
     */
    private boolean skipNextStatus = false;

    /**
     * Main layout.
     */
    @BindView(R.id.main_layout)
    ConstraintLayout mainLayout;

    /**
     * Title of media being played.
     */
    @BindView(R.id.title)
    TextView titleTextView;

    /**
     * SeekBar of media being played.
     */
    @BindView(R.id.seekBar)
    SeekBar timeSeekBar;

    /**
     * TextView showing the current time of media being played.
     */
    @BindView(R.id.start)
    TextView startTextView;

    /**
     * TextView showing the length of media being played.
     */
    @BindView(R.id.end)
    TextView endTextView;

    @Override
    protected final void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        timeSeekBar.setOnSeekBarChangeListener(onSeekBarChangeListener);
    }

    @Override
    protected final void onStart() {
        super.onStart();

        EventBus.getDefault().register(this);

        SharedPreferences sharedPref = PreferenceManager
                .getDefaultSharedPreferences(this);
        vlcRequest = new VlcRequest(
                sharedPref.getString(Pref.VLC_IP_ADDRESS, ""),
                sharedPref.getString(Pref.VLC_PORT, ""),
                sharedPref.getString(Pref.VLC_PASSWORD, ""));
        vlcRequest.start();
    }

    @Override
    protected final void onStop() {
        super.onStop();

        vlcRequest.stop();

        EventBus.getDefault().unregister(this);
    }

    @Override
    public final boolean onCreateOptionsMenu(final Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public final boolean onOptionsItemSelected(final MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            startActivity(new Intent(this, SettingsActivity.class));
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * Triggered on Error events.
     *
     * @param error Error event
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public final void onErrorEvent(final ErrorEvent error) {
        if (vlcRequest != null) {
            // Increase delay between each request to get the status
            vlcRequest.setStatusRefreshDelay(5000);
        }
        applyDefaultStatus();
        Snackbar.make(mainLayout, R.string.error_connect,
                Snackbar.LENGTH_LONG)
                .setAction(R.string.action_show, new View.OnClickListener() {
                    @Override
                    public void onClick(final View v) {
                        ErrorDialog.show(MainActivity.this,
                                error.getTitle(), error.getMessage());
                    }
                }).show();
    }

    /**
     * Triggered on Status events.
     *
     * @param status Status event
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public final void onStatusEvent(final StatusEvent status) {
        lastState = status.getState();
        if (skipNextStatus) {
            skipNextStatus = false;
            return;
        }
        String title = status.getTitle();
        int time = status.getTime();
        int length = status.getLength();
        if (!titleTextView.getText().equals(title)) {
            titleTextView.setText(title);
        }
        if (timeSeekBar.getMax() != length) {
            endTextView.setText(DateUtils.formatElapsedTime(length));
            timeSeekBar.setMax(length);
        }
        if (timeSeekBar.getProgress() != time) {
            startTextView.setText(DateUtils.formatElapsedTime(time));
            timeSeekBar.setProgress(time);
        }
    }

    /**
     * Apply default status values to views.
     */
    private void applyDefaultStatus() {
        lastState = null;
        titleTextView.setText(R.string.default_title);
        timeSeekBar.setProgress(0);
        startTextView.setText(R.string.default_duration);
        timeSeekBar.setMax(100);
        endTextView.setText(R.string.default_duration);
    }

    /**
     * Change listener for SeekBar.
     */
    private SeekBar.OnSeekBarChangeListener onSeekBarChangeListener =
            new SeekBar.OnSeekBarChangeListener() {
                @Override
                public void onProgressChanged(final SeekBar seekBar,
                                              final int progress,
                                              final boolean fromUser) {
                    if (!fromUser) {
                        // Ignore all changes if they are not from user
                        return;
                    }

                    // Ignore the next status update to avoid the seek bar to
                    // be updated from a response sent before this request
                    skipNextStatus = true;

                    // Send request
                    EventBus.getDefault().post(new RequestEvent(Cmd.SEEK,
                            String.valueOf(progress)));

                    // Update text view for current time
                    startTextView.setText(DateUtils.formatElapsedTime(
                            progress));
                }

                @Override
                public void onStartTrackingTouch(final SeekBar seekBar) {
                }

                @Override
                public void onStopTrackingTouch(final SeekBar seekBar) {
                }
            };

    /**
     * Called when previous button is clicked.
     *
     * @param view View
     */
    public void onPreviousClick(final View view) {
        EventBus.getDefault().post(new RequestEvent(Cmd.PREVIOUS));
    }

    /**
     * Called when rewind button is clicked.
     *
     * @param view View
     */
    public void onRewindClick(final View view) {
        EventBus.getDefault().post(new RequestEvent(Cmd.SEEK, Val.REWIND));
    }

    /**
     * Called when play / pause button is clicked.
     *
     * @param view View
     */
    public void onPlayPauseClick(final View view) {
        if (lastState == null) {
            return;
        }
        switch (lastState) {
            case State.PLAYING:
                EventBus.getDefault().post(new RequestEvent(Cmd.PAUSE));
                break;
            case State.PAUSED:
                EventBus.getDefault().post(new RequestEvent(Cmd.PLAY));
                break;
            case State.STOPPED:
                EventBus.getDefault().post(new RequestEvent(Cmd.PLAY));
                break;
            default:
                break;
        }
    }

    /**
     * Called when forward button is clicked.
     *
     * @param view View
     */
    public void onForwardClick(final View view) {
        EventBus.getDefault().post(new RequestEvent(Cmd.SEEK, Val.FORWARD));
    }

    /**
     * Called when next button is clicked.
     *
     * @param view View
     */
    public void onNextClick(final View view) {
        EventBus.getDefault().post(new RequestEvent(Cmd.NEXT));
    }

    /**
     * Called when volume up button is clicked.
     *
     * @param view View
     */
    public void onVolumeUpClick(final View view) {
        EventBus.getDefault().post(new RequestEvent(Cmd.VOLUME, Val.VOLUME_UP));
    }

    /**
     * Called when volume down button is clicked.
     *
     * @param view View
     */
    public void onVolumeDownClick(final View view) {
        EventBus.getDefault().post(new RequestEvent(Cmd.VOLUME,
                Val.VOLUME_DOWN));
    }

    /**
     * Called when fullscreen button is clicked.
     *
     * @param view View
     */
    public void onFullscreenClick(final View view) {
        EventBus.getDefault().post(new RequestEvent(Cmd.FULLSCREEN));
    }
}
