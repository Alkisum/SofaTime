package com.alkisum.android.vlcremote.utils;

import okhttp3.HttpUrl;

/**
 * Utility class for HTTP operations.
 *
 * @author Alkisum
 * @version 1.0
 * @since 1.0
 */
public final class Http {

    /**
     * Http constructor.
     */
    private Http() {

    }

    /**
     * Build URL.
     *
     * @param baseUrl Base URL
     * @param command Command to send
     * @param val     Value to send
     * @return Built URL
     */
    public static String buildUrl(final String baseUrl, final String command,
                                  final String val) {
        HttpUrl httpUrl = HttpUrl.parse(baseUrl);
        if (httpUrl != null) {
            HttpUrl.Builder urlBuilder = httpUrl.newBuilder();
            urlBuilder.addQueryParameter("command", command);
            urlBuilder.addQueryParameter("val", val);
            return urlBuilder.build().toString();
        }
        return null;
    }
}
