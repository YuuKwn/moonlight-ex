package com.limelight.streaming;

import android.view.Surface;

public interface VideoSink {
    interface Callback {
        void onVideoSinkReady(VideoSink sink);
        void onVideoSinkDestroyed(VideoSink sink);
    }

    Surface getSurface();
    void setCallback(Callback callback);
    int getPreferredWidth();
    int getPreferredHeight();
    float getPreferredRefreshRate();
    boolean isReady();
}
