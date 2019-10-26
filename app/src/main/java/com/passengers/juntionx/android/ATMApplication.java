package com.passengers.juntionx.android;

import android.app.Application;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import timber.log.Timber;

public class ATMApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        initLogging();
    }

    private void initLogging() {
        if (BuildConfig.DEBUG) {
            Timber.plant(new Timber.DebugTree());
        } else {
            Timber.plant(new Timber.Tree() {
                @Override
                protected void log(int priority, @Nullable String tag, @NotNull String message, @Nullable Throwable t) {
                    // don't log in not DEBUG build
                }
            });
        }
    }
}
