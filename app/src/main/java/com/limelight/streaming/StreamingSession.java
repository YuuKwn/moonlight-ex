package com.limelight.streaming;

import android.content.Context;

import com.limelight.binding.audio.AndroidAudioRenderer;
import com.limelight.binding.video.MediaCodecDecoderRenderer;
import com.limelight.nvstream.NvConnection;
import com.limelight.nvstream.NvConnectionListener;
import com.limelight.preferences.PreferenceConfiguration;

public class StreamingSession {
    public enum State {
        IDLE,
        STARTING,
        STARTED,
        STOPPING,
        STOPPED
    }

    interface ConnectionAdapter {
        void start(AndroidAudioRenderer audioRenderer,
                   MediaCodecDecoderRenderer decoderRenderer,
                   NvConnectionListener listener);
        void stop();
    }

    interface DecoderAdapter {
        void setRenderTarget(android.view.Surface surface);
        void prepareForStop();
    }

    private final Context context;
    private final PreferenceConfiguration preferences;
    private final ConnectionAdapter connection;
    private final DecoderAdapter decoder;
    private final MediaCodecDecoderRenderer decoderRenderer;
    private State state = State.IDLE;

    public StreamingSession(Context context,
                            PreferenceConfiguration preferences,
                            NvConnection connection,
                            MediaCodecDecoderRenderer decoderRenderer) {
        this(context, preferences, new ConnectionAdapter() {
            @Override
            public void start(AndroidAudioRenderer audioRenderer,
                              MediaCodecDecoderRenderer decoderRenderer,
                              NvConnectionListener listener) {
                connection.start(audioRenderer, decoderRenderer, listener);
            }

            @Override
            public void stop() {
                connection.stop();
            }
        }, new DecoderAdapter() {
            @Override
            public void setRenderTarget(android.view.Surface surface) {
                decoderRenderer.setRenderTarget(surface);
            }

            @Override
            public void prepareForStop() {
                decoderRenderer.prepareForStop();
            }
        }, decoderRenderer);
    }

    StreamingSession(Context context,
                     PreferenceConfiguration preferences,
                     ConnectionAdapter connection,
                     DecoderAdapter decoder,
                     MediaCodecDecoderRenderer decoderRenderer) {
        this.context = context;
        this.preferences = preferences;
        this.connection = connection;
        this.decoder = decoder;
        this.decoderRenderer = decoderRenderer;
    }

    public synchronized State getState() {
        return state;
    }

    public synchronized boolean hasStarted() {
        return state == State.STARTING || state == State.STARTED || state == State.STOPPING;
    }

    public synchronized boolean isActive() {
        return state == State.STARTING || state == State.STARTED;
    }

    public void start(VideoSink videoSink, NvConnectionListener listener) {
        synchronized (this) {
            if (state != State.IDLE && state != State.STOPPED) {
                return;
            }
            if (!videoSink.isReady()) {
                return;
            }
            state = State.STARTING;
        }

        decoder.setRenderTarget(videoSink.getSurface());
        connection.start(new AndroidAudioRenderer(context, preferences.playHostAudio),
                decoderRenderer, listener);
    }

    public synchronized void markStarted() {
        if (state == State.STARTING) {
            state = State.STARTED;
        }
    }

    public void prepareForVideoSinkDestroyed() {
        if (hasStarted()) {
            decoder.prepareForStop();
        }
    }

    public void stopAsync() {
        if (!beginStop()) {
            return;
        }
        new Thread(new Runnable() {
            @Override
            public void run() {
                stopConnectionAndMarkStopped();
            }
        }, "StreamingSessionStop").start();
    }

    public void stopBlocking() {
        if (!beginStop()) {
            return;
        }
        stopConnectionAndMarkStopped();
    }

    private void stopConnectionAndMarkStopped() {
        try {
            connection.stop();
        } finally {
            synchronized (this) {
                state = State.STOPPED;
            }
        }
    }

    private synchronized boolean beginStop() {
        if (state != State.STARTING && state != State.STARTED) {
            return false;
        }
        state = State.STOPPING;
        return true;
    }
}
