package com.alkisum.android.sofatime.app;

import android.app.Application;

import com.squareup.leakcanary.LeakCanary;

/**
 * Application class.
 *
 * @author Alkisum
 * @version 1.1
 * @since 1.1
 */
public class SofaTimeApp extends Application {

    @Override
    public final void onCreate() {
        super.onCreate();
        if (LeakCanary.isInAnalyzerProcess(this)) {
            return;
        }
        LeakCanary.install(this);
    }
}
