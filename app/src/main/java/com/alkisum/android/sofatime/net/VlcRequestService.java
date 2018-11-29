package com.alkisum.android.sofatime.net;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.v4.app.NotificationCompat;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.View;
import android.widget.RemoteViews;

import com.alkisum.android.sofatime.R;
import com.alkisum.android.sofatime.activities.MainActivity;
import com.alkisum.android.sofatime.events.ErrorEvent;
import com.alkisum.android.sofatime.events.RequestEvent;
import com.alkisum.android.sofatime.events.StatusEvent;
import com.alkisum.android.sofatime.utils.Cmd;
import com.alkisum.android.sofatime.utils.Http;
import com.alkisum.android.sofatime.utils.Pref;
import com.alkisum.android.sofatime.utils.State;
import com.alkisum.android.sofatime.utils.Val;
import com.alkisum.android.sofatime.utils.Xml;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import java.io.IOException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;

import okhttp3.Authenticator;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Credentials;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;
import okhttp3.Route;

/**
 * Service processing requests to VLC and handling responses.
 *
 * @author Alkisum
 * @version 1.1
 * @since 1.0
 */
public class VlcRequestService extends Service {

    /**
     * Log tag.
     */
    private static final String TAG = "VlcRequestService";

    /**
     * The identifier for the notification displayed for the foreground service.
     */
    private static final int NOTIFICATION_ID = 241;

    /**
     * Intent action rewind.
     */
    private static final String REWIND = "rewind";

    /**
     * Intent action play.
     */
    private static final String PLAY = "play";

    /**
     * Intent action pause.
     */
    private static final String PAUSE = "pause";

    /**
     * Intent action forward.
     */
    private static final String FORWARD = "forward";

    /**
     * Binder.
     */
    private final IBinder binder = new LocalBinder();

    /**
     * OkHttpClient instance.
     */
    private OkHttpClient client;

    /**
     * Delay to send a request to get a new status from VLC.
     */
    private long statusRefreshDelay = 1000;

    /**
     * Flag set to true if the status task is running, false otherwise.
     */
    private boolean statusTaskOn = false;

    /**
     * Handler for status task.
     */
    private final Handler statusHandler = new Handler();

    /**
     * Base URL to use when sending requests.
     */
    private String baseUrl;

    /**
     * Notification builder.
     */
    private NotificationCompat.Builder notificationBuilder;

    /**
     * Notification manager.
     */
    private NotificationManager notificationManager;

    /**
     * Flag set to true if the VLC request service is running in foreground,
     * false otherwise.
     */
    private boolean runningInForeground = false;

    /**
     * Notification layout.
     */
    private RemoteViews notifLayout;

    /**
     * Notification layout expanded.
     */
    private RemoteViews notifLayoutExpanded;

    @Override
    public final void onCreate() {

        initNotification();
        EventBus.getDefault().register(this);
        statusHandler.post(statusTask);
    }

    @Override
    public final int onStartCommand(final Intent intent, final int flags,
                                    final int startId) {
        if (intent == null || intent.getAction() == null) {
            return START_NOT_STICKY;
        }

        switch (intent.getAction()) {
            case REWIND:
                EventBus.getDefault().post(
                        new RequestEvent(Cmd.SEEK, Val.REWIND));
                break;
            case PLAY:
                EventBus.getDefault().post(new RequestEvent(Cmd.PLAY));
                break;
            case PAUSE:
                EventBus.getDefault().post(new RequestEvent(Cmd.PAUSE));
                break;
            case FORWARD:
                EventBus.getDefault().post(
                        new RequestEvent(Cmd.SEEK, Val.FORWARD));
                break;
            default:
                break;
        }
        return START_NOT_STICKY;
    }

    @Override
    public final IBinder onBind(final Intent intent) {
        // get extras from intent
        String ipAddress = intent.getStringExtra(Pref.VLC_IP_ADDRESS);
        String port = intent.getStringExtra(Pref.VLC_PORT);
        final String password = intent.getStringExtra(Pref.VLC_PASSWORD);

        // build base URL
        baseUrl = "http://" + ipAddress + ":" + port + "/requests/status.xml";

        // build client
        OkHttpClient.Builder builder = new OkHttpClient.Builder();
        builder.authenticator(new Authenticator() {
            @Override
            public Request authenticate(final Route route,
                                        @NonNull final Response response) {
                String credential = Credentials.basic("", password);
                return response.request().newBuilder().header(
                        "Authorization", credential).build();
            }
        });
        client = builder.build();
        return binder;
    }

    @Override
    public final boolean onUnbind(final Intent intent) {
        return false;
    }

    @Override
    public final void onDestroy() {
        if (statusTaskOn) {
            statusHandler.removeCallbacks(statusTask);
            statusTaskOn = false;
        }
        EventBus.getDefault().unregister(this);
    }

    /**
     * Request a new status.
     */
    private void requestStatus() {
        // No command nor value is required to request a status.
        run("", "", false);
    }

    /**
     * Run the given command with the given value.
     *
     * @param command        Command to run, "" for no command
     * @param val            Value to use, "" for no value
     * @param ignoreResponse true if the request response must be ignores, false
     *                       otherwise
     */
    private void run(final String command, final String val,
                     final boolean ignoreResponse) {

        String url = Http.buildUrl(baseUrl, command, val);
        if (url == null) {
            EventBus.getDefault().post(new ErrorEvent("Invalid URL", baseUrl));
            return;
        }

        Request request = new Request.Builder()
                .url(url)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull final Call call,
                                  @NonNull final IOException e) {
                Log.e(TAG, e.getMessage(), e);
                EventBus.getDefault().post(new ErrorEvent(
                        "Request failed", e.getMessage()));
            }

            @Override
            public void onResponse(@NonNull final Call call,
                                   @NonNull final Response response) {
                if (response.isSuccessful()) {
                    statusRefreshDelay = 1000;
                    if (!ignoreResponse) {
                        handleResponse(response);
                    }
                } else {
                    Log.e(TAG, response.toString());
                    EventBus.getDefault().post(new ErrorEvent(
                            "Error " + response.code(), response.message()));
                }
            }
        });
    }

    /**
     * Handle the given response from the given command.
     *
     * @param response Response to handle
     */
    private void handleResponse(final Response response) {
        try {
            ResponseBody body = response.body();
            if (body != null) {
                handleStatus(body.string());
            }
        } catch (IOException | XPathExpressionException | SAXException
                | ParserConfigurationException e) {
            // Ignore the status
            Log.e(TAG, e.getMessage(), e);
        }
    }

    /**
     * Task requesting status every n seconds.
     */
    private final Runnable statusTask = new Runnable() {
        @Override
        public void run() {
            statusHandler.postDelayed(this, statusRefreshDelay);
            statusTaskOn = true;
            requestStatus();
        }
    };

    /**
     * Handle status.
     *
     * @param xml XML string containing the status
     * @throws ParserConfigurationException Serious configuration error
     * @throws SAXException                 SAX error or warning
     * @throws XPathExpressionException     Error in XPath expression
     * @throws IOException                  XML document cannot be created
     *                                      from XML string
     */
    private void handleStatus(final String xml) throws
            ParserConfigurationException, SAXException, IOException,
            XPathExpressionException {
        Document doc = Xml.buildDocFromString(xml);

        // state
        String state = Xml.getValueFromStatus(doc, "state", null);

        // file name
        String title = Xml.getValueFromStatus(doc, "info", "filename");

        // time
        String time = Xml.getValueFromStatus(doc, "time", null);

        // length
        String length = Xml.getValueFromStatus(doc, "length", null);

        // volume
        String volume = Xml.getValueFromStatus(doc, "volume", null);

        // post status
        EventBus.getDefault().post(new StatusEvent(state, title,
                Integer.parseInt(time), Integer.parseInt(length),
                Float.parseFloat(volume)));
    }

    /**
     * @param statusRefreshDelay Status refresh delay to set
     */
    public final void setStatusRefreshDelay(final long statusRefreshDelay) {
        this.statusRefreshDelay = statusRefreshDelay;
    }

    /**
     * Triggered when response status is received from VLC.
     *
     * @param statusEvent Status event
     */
    @Subscribe
    public final void onStatusEvent(final StatusEvent statusEvent) {
        // get values from status event
        String title = statusEvent.getTitle();
        String content = DateUtils.formatElapsedTime(statusEvent.getTime())
                + " / "
                + DateUtils.formatElapsedTime(statusEvent.getLength());

        // update text views
        this.updateTitle(title);
        this.updateContent(content);
        notificationBuilder.setWhen(System.currentTimeMillis());

        // update buttons visibility
        switch (statusEvent.getState()) {
            case State.PLAYING:
                this.updateVisibility(R.id.notification_play, View.GONE);
                this.updateVisibility(R.id.notification_pause, View.VISIBLE);
                break;
            case State.PAUSED:
            case State.STOPPED:
                this.updateVisibility(R.id.notification_pause, View.GONE);
                this.updateVisibility(R.id.notification_play, View.VISIBLE);
                break;
            default:
                break;
        }

        if (runningInForeground) {
            // notify
            notificationManager.notify(NOTIFICATION_ID,
                    notificationBuilder.build());
        }
    }

    /**
     * Update notification layouts with the given title.
     *
     * @param title New title
     */
    private void updateTitle(final String title) {
        notifLayout.setTextViewText(R.id.notification_title, title);
        notifLayoutExpanded.setTextViewText(R.id.notification_title, title);
    }

    /**
     * Update notification layouts with the given content.
     *
     * @param content New content
     */
    private void updateContent(final String content) {
        notifLayout.setTextViewText(R.id.notification_content, content);
        notifLayoutExpanded.setTextViewText(R.id.notification_content, content);
    }

    /**
     * Update notification layouts button identified by the given id
     * with the given visibility.
     *
     * @param viewId     View id of the button to set the new visibility
     * @param visibility New visibility to set
     */
    private void updateVisibility(final int viewId, final int visibility) {
        notifLayout.setViewVisibility(viewId, visibility);
        notifLayoutExpanded.setViewVisibility(viewId, visibility);
    }

    /**
     * Triggered on Request event.
     *
     * @param request Request event
     */
    @Subscribe
    public final void onRequestEvent(final RequestEvent request) {
        run(request.getCommand(), request.getVal(), request.isIgnoreResponse());
    }

    /**
     * Initialize notification.
     */
    private void initNotification() {
        // build content intent
        PendingIntent activityPendingIntent = PendingIntent.getActivity(
                this, 0, new Intent(this, MainActivity.class),
                PendingIntent.FLAG_UPDATE_CURRENT);

        // build notification manager
        notificationManager = (NotificationManager)
                getSystemService(Context.NOTIFICATION_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel notificationChannel = new NotificationChannel(
                    "SofaTime", "SofaTime controller",
                    NotificationManager.IMPORTANCE_LOW);
            notificationManager.createNotificationChannel(notificationChannel);
        }

        // get custom layouts
        notifLayout = new RemoteViews(
                getPackageName(), R.layout.notification_small);
        notifLayoutExpanded = new RemoteViews(
                getPackageName(), R.layout.notification_large);

        // build listeners for buttons
        buildActionListeners(REWIND, R.id.notification_rewind);
        buildActionListeners(PLAY, R.id.notification_play);
        buildActionListeners(PAUSE, R.id.notification_pause);
        buildActionListeners(FORWARD, R.id.notification_forward);

        // build notification
        notificationBuilder = new NotificationCompat.Builder(this, "SofaTime")
                .setContentIntent(activityPendingIntent)
                .setSmallIcon(R.drawable.ic_theaters_white_24dp)
                .setStyle(new NotificationCompat.DecoratedCustomViewStyle())
                .setCustomContentView(notifLayout)
                .setCustomBigContentView(notifLayoutExpanded);
    }

    /**
     * Build the action listeners for the given view in notification layouts.
     *
     * @param action Intent action
     * @param viewId View id
     */
    private void buildActionListeners(final String action, final int viewId) {
        Intent i = new Intent(this, VlcRequestService.class);
        i.setAction(action);
        PendingIntent p = PendingIntent.getService(this, 0, i, 0);
        notifLayout.setOnClickPendingIntent(viewId, p);
        notifLayoutExpanded.setOnClickPendingIntent(viewId, p);
    }

    /**
     * @return true if the VLC request service is running in foreground,
     * false otherwise
     */
    public boolean isRunningInForeground() {
        return runningInForeground;
    }

    /**
     * Make the service run in the foreground.
     */
    public final void startForeground() {
        startForeground(NOTIFICATION_ID, notificationBuilder.build());
        runningInForeground = true;
    }

    /**
     * Remove the service from foreground state.
     */
    public final void stopForeground() {
        stopForeground(true);
        runningInForeground = false;
    }

    /**
     * Class used for the client Binder.
     */
    public final class LocalBinder extends Binder {

        /**
         * @return VlcRequestService
         */
        public VlcRequestService getService() {
            return VlcRequestService.this;
        }
    }
}
