package com.example.sep490_mobile.call;

import android.content.Context;

import com.example.sep490_mobile.IncomingCallListener;

public final class CallManager {
    private static IncomingCallListener sListener;

    private CallManager() {}

    public static synchronized void start(Context appContext, String userId) {
        if (sListener == null && userId != null && !userId.isEmpty()) {
            sListener = new IncomingCallListener(appContext, userId);
        }
    }

    public static synchronized void stop() {
        if (sListener != null) {
            sListener.stop();
            sListener = null;
        }
    }

    public static synchronized boolean isRunning() {
        return sListener != null;
    }
}