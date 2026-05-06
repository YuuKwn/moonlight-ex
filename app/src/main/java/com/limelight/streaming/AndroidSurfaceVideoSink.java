package com.limelight.streaming;

import android.view.Surface;

import com.limelight.ui.StreamContainer;

public class AndroidSurfaceVideoSink implements VideoSink {
    private final StreamContainer streamContainer;
    private final int preferredWidth;
    private final int preferredHeight;
    private final float preferredRefreshRate;
    private Callback callback;

    public AndroidSurfaceVideoSink(StreamContainer streamContainer,
                                   int preferredWidth,
                                   int preferredHeight,
                                   float preferredRefreshRate) {
        this.streamContainer = streamContainer;
        this.preferredWidth = preferredWidth;
        this.preferredHeight = preferredHeight;
        this.preferredRefreshRate = preferredRefreshRate;
        this.streamContainer.setOnSurfaceDestroyed(() -> {
            if (callback != null) {
                callback.onVideoSinkDestroyed(this);
            }
        });
    }

    @Override
    public Surface getSurface() {
        return streamContainer.getSurface();
    }

    @Override
    public void setCallback(Callback callback) {
        this.callback = callback;
        streamContainer.setOnSurfaceAvailable(() -> {
            if (this.callback != null) {
                this.callback.onVideoSinkReady(this);
            }
        });
    }

    @Override
    public int getPreferredWidth() {
        return preferredWidth;
    }

    @Override
    public int getPreferredHeight() {
        return preferredHeight;
    }

    @Override
    public float getPreferredRefreshRate() {
        return preferredRefreshRate;
    }

    @Override
    public boolean isReady() {
        Surface surface = getSurface();
        return surface != null && surface.isValid();
    }
}
