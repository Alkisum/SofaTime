package com.alkisum.android.vlcremote.dialogs;

import android.app.AlertDialog;
import android.content.Context;

/**
 * Class to build a simple error dialog.
 *
 * @author Alkisum
 * @version 1.0
 * @since 1.0
 */
public final class ErrorDialog {

    /**
     * ErrorDialog constructor.
     */
    private ErrorDialog() {

    }

    /**
     * Show error dialog.
     *
     * @param context Context in which the dialog should be shown
     * @param title   Dialog title
     * @param message Dialog message
     */
    public static void show(final Context context, final String title,
                            final String message) {
        new AlertDialog.Builder(context)
                .setTitle(title)
                .setMessage(message).show();
    }
}
