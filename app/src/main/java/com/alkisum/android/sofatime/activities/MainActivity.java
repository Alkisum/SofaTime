package com.alkisum.android.sofatime.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.constraint.ConstraintLayout;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.format.DateUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.TextView;

import com.alkisum.android.sofatime.R;
import com.alkisum.android.sofatime.dialogs.ErrorDialog;
import com.alkisum.android.sofatime.events.ErrorEvent;
import com.alkisum.android.sofatime.events.RequestEvent;
import com.alkisum.android.sofatime.events.StatusEvent;
import com.alkisum.android.sofatime.net.VlcRequest;
import com.alkisum.android.sofatime.utils.Cmd;
import com.alkisum.android.sofatime.utils.Pref;
import com.alkisum.android.sofatime.utils.State;
import com.alkisum.android.sofatime.utils.Val;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.OnLongClick;

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
    @BindView(R.id.seekBar_time)
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

    /**
     * SeekBar for volume.
     */
    @BindView(R.id.seekBar_volume)
    SeekBar volumeSeekBar;

    @Override
    protected final void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Time SeekBar listener
        timeSeekBar.setOnSeekBarChangeListener(onTimeSeekBarChangeListener);

        // VLC volume controller is based on 256
        volumeSeekBar.setMax(256);

        // Volume SeekBar listener
        volumeSeekBar.setOnSeekBarChangeListener(onVolumeSeekBarChangeListener);
    }

    @Override
    protected final void onStart() {
        super.onStart();

        EventBus.getDefault().register(this);

        SharedPreferences sharedPref = PreferenceManager
                .getDefaultSharedPreferences(this);

        String ipAddress = sharedPref.getString(Pref.VLC_IP_ADDRESS, "");
        String port = sharedPref.getString(Pref.VLC_PORT, "");
        String password = sharedPref.getString(Pref.VLC_PASSWORD, "");

        if (ipAddress.equals("") || port.equals("")) {
            StringBuilder msg = new StringBuilder();
            msg.append(getString(R.string.error_vlc_settings_message));
            if (ipAddress.equals("")) {
                msg.append(getString(R.string.error_vlc_settings_ipaddress));
            }
            if (port.equals("")) {
                msg.append(getString(R.string.error_vlc_settings_port));
            }
            ErrorDialog.show(this, getString(R.string.error_vlc_settings_title),
                    msg.toString());
        } else {
            // start HTTP request
            vlcRequest = new VlcRequest(ipAddress, port, password);
            vlcRequest.start();
        }
    }

    @Override
    protected final void onStop() {
        super.onStop();

        if (vlcRequest != null) {
            vlcRequest.stop();
        }

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

        // Update play/pause button
        ImageButton button = findViewById(R.id.play_pause);
        switch (lastState) {
            case State.PLAYING:
                button.setImageDrawable(ContextCompat.getDrawable(this,
                        R.drawable.ic_pause_white_48dp));
                break;
            case State.PAUSED:
                button.setImageDrawable(ContextCompat.getDrawable(this,
                        R.drawable.ic_play_arrow_white_48dp));
                break;
            case State.STOPPED:
                button.setImageDrawable(ContextCompat.getDrawable(this,
                        R.drawable.ic_play_arrow_white_48dp));
                break;
            default:
                break;
        }

        // Update title
        String title = status.getTitle();
        titleTextView.setText(title);

        // Update length
        int length = status.getLength();
        endTextView.setText(DateUtils.formatElapsedTime(length));
        timeSeekBar.setMax(length);

        // Update time
        int time = status.getTime();
        startTextView.setText(DateUtils.formatElapsedTime(time));
        timeSeekBar.setProgress(time);

        // Update volume
        float volume = status.getVolume();
        volumeSeekBar.setProgress(Math.round(volume));
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
        volumeSeekBar.setProgress(0);
    }

    /**
     * Change listener for time SeekBar.
     */
    private final SeekBar.OnSeekBarChangeListener onTimeSeekBarChangeListener
            = new SeekBar.OnSeekBarChangeListener() {
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
                    String.valueOf(progress), true));

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
     * Change listener for volume SeekBar.
     */
    private final SeekBar.OnSeekBarChangeListener onVolumeSeekBarChangeListener
            = new SeekBar.OnSeekBarChangeListener() {
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
            EventBus.getDefault().post(new RequestEvent(Cmd.VOLUME,
                    String.valueOf(progress), true));
        }

        @Override
        public void onStartTrackingTouch(final SeekBar seekBar) {
        }

        @Override
        public void onStopTrackingTouch(final SeekBar seekBar) {
        }
    };

    /**
     * Called when previous/rewind button is long clicked.
     *
     * @param view View
     * @return true if the callback consumed the long click, false otherwise
     */
    @OnLongClick(R.id.previous_rewind)
    public final boolean onPreviousClick(final View view) {
        EventBus.getDefault().post(new RequestEvent(Cmd.PREVIOUS));
        return true;
    }

    /**
     * Called when previous/rewind button is clicked.
     *
     * @param view View
     */
    @OnClick(R.id.previous_rewind)
    public final void onRewindClick(final View view) {
        EventBus.getDefault().post(new RequestEvent(Cmd.SEEK, Val.REWIND));
    }

    /**
     * Called when play/pause button is clicked.
     *
     * @param view View
     */
    @OnClick(R.id.play_pause)
    public final void onPlayPauseClick(final View view) {
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
     * Called when next/forward button is clicked.
     *
     * @param view View
     */
    @OnClick(R.id.next_forward)
    public final void onForwardClick(final View view) {
        EventBus.getDefault().post(new RequestEvent(Cmd.SEEK, Val.FORWARD));
    }

    /**
     * Called when next/forward button is long clicked.
     *
     * @param view View
     * @return true if the callback consumed the long click, false otherwise
     */
    @OnLongClick(R.id.next_forward)
    public final boolean onNextClick(final View view) {
        EventBus.getDefault().post(new RequestEvent(Cmd.NEXT));
        return true;
    }

    /**
     * Called when volume up button is clicked.
     *
     * @param view View
     */
    @OnClick(R.id.volume_up)
    public final void onVolumeUpClick(final View view) {
        EventBus.getDefault().post(new RequestEvent(Cmd.VOLUME,
                Val.volume("+")));
    }

    /**
     * Called when volume down button is clicked.
     *
     * @param view View
     */
    @OnClick(R.id.volume_down)
    public final void onVolumeDownClick(final View view) {
        EventBus.getDefault().post(new RequestEvent(Cmd.VOLUME,
                Val.volume("-")));
    }
}
