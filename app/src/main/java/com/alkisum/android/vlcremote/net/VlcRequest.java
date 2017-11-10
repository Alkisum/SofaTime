package com.alkisum.android.vlcremote.net;

import android.os.Handler;
import android.util.Log;

import com.alkisum.android.vlcremote.events.ErrorEvent;
import com.alkisum.android.vlcremote.events.RequestEvent;
import com.alkisum.android.vlcremote.events.StatusEvent;
import com.alkisum.android.vlcremote.utils.Http;
import com.alkisum.android.vlcremote.utils.Xml;

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
 * Class processing requests to VLC and handling responses.
 *
 * @author Alkisum
 * @version 1.0
 * @since 1.0
 */
public class VlcRequest {

    /**
     * Log tag.
     */
    private static final String TAG = "VlcRequest";

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
    private Handler statusHandler = new Handler();

    /**
     * Base URL to use when sending requests.
     */
    private String baseUrl;

    /**
     * VlcRequest constructor.
     *
     * @param ipAddress IP address
     * @param port      Port
     * @param password  Password
     */
    public VlcRequest(final String ipAddress, final String port,
                      final String password) {
        baseUrl = "http://" + ipAddress + ":" + port + "/requests/status.xml";

        OkHttpClient.Builder builder = new OkHttpClient.Builder();
        builder.authenticator(new Authenticator() {
            @Override
            public Request authenticate(final Route route,
                                        final Response response)
                    throws IOException {
                String credential = Credentials.basic("", password);
                return response.request().newBuilder().header(
                        "Authorization", credential).build();
            }
        });
        client = builder.build();
    }

    /**
     * Register to receive event and start status task.
     */
    public final void start() {
        EventBus.getDefault().register(this);
        statusHandler.post(statusTask);
    }

    /**
     * Stop status task and unregister to event.
     */
    public final void stop() {
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
        run("", "");
    }

    /**
     * Run the given command with the given value.
     *
     * @param command Command to run, "" for no command
     * @param val     Value to use, "" for no value
     */
    private void run(final String command, final String val) {

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
            public void onFailure(final Call call, final IOException e) {
                Log.e(TAG, e.getMessage(), e);
                EventBus.getDefault().post(new ErrorEvent(
                        "Request failed", e.getMessage()));
            }

            @Override
            public void onResponse(final Call call, final Response response) {
                if (response.isSuccessful()) {
                    statusRefreshDelay = 1000;
                    handleResponse(command, response);
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
     * @param command  Command sent in the request that triggered the given
     *                 response
     * @param response Response to handle
     */
    private void handleResponse(final String command, final Response response) {
        switch (command) {
            case "":
                // status
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
                break;
            default:
                break;
        }
    }

    /**
     * Task requesting status every n seconds.
     */
    private Runnable statusTask = new Runnable() {
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
    private static void handleStatus(final String xml) throws
            ParserConfigurationException, SAXException, IOException,
            XPathExpressionException {
        Document doc = Xml.buildDocFromString(xml);

        // State
        String state = Xml.getValueFromStatus(doc, "state", null);

        // Title
        String title = Xml.getValueFromStatus(doc, "info", "title");

        // Time
        String time = Xml.getValueFromStatus(doc, "time", null);

        // Length
        String length = Xml.getValueFromStatus(doc, "length", null);

        // Post status
        EventBus.getDefault().post(new StatusEvent(state, title,
                Integer.parseInt(time), Integer.parseInt(length)));
    }

    /**
     * @param statusRefreshDelay Status refresh delay to set
     */
    public final void setStatusRefreshDelay(final long statusRefreshDelay) {
        this.statusRefreshDelay = statusRefreshDelay;
    }

    /**
     * Triggered on Request event.
     *
     * @param request Request event
     */
    @Subscribe
    public final void onRequestEvent(final RequestEvent request) {
        run(request.getCommand(), request.getVal());
    }
}
