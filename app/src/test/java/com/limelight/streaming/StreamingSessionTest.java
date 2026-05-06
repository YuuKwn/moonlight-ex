package com.limelight.streaming;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import android.view.Surface;

import com.limelight.preferences.PreferenceConfiguration;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;

@RunWith(RobolectricTestRunner.class)
public class StreamingSessionTest {
    @Test
    public void startAndStopAdvanceLifecycleState() {
        FakeConnection connection = new FakeConnection();
        FakeDecoder decoder = new FakeDecoder();
        PreferenceConfiguration preferences = new PreferenceConfiguration();

        StreamingSession session = new StreamingSession(
                RuntimeEnvironment.getApplication(),
                preferences,
                connection,
                decoder,
                null);

        session.start(new ReadyVideoSink(), null);

        assertEquals(StreamingSession.State.STARTING, session.getState());
        assertTrue(connection.started);
        assertTrue(decoder.renderTargetSet);

        session.markStarted();
        assertEquals(StreamingSession.State.STARTED, session.getState());
        assertTrue(session.isActive());

        session.stopBlocking();

        assertEquals(StreamingSession.State.STOPPED, session.getState());
        assertTrue(connection.stopped);
        assertFalse(session.isActive());
    }

    @Test
    public void startDoesNothingWhenVideoSinkIsNotReady() {
        FakeConnection connection = new FakeConnection();
        FakeDecoder decoder = new FakeDecoder();

        StreamingSession session = new StreamingSession(
                RuntimeEnvironment.getApplication(),
                new PreferenceConfiguration(),
                connection,
                decoder,
                null);

        session.start(new NotReadyVideoSink(), null);

        assertEquals(StreamingSession.State.IDLE, session.getState());
        assertFalse(connection.started);
        assertFalse(decoder.renderTargetSet);
    }

    private static class FakeConnection implements StreamingSession.ConnectionAdapter {
        boolean started;
        boolean stopped;

        @Override
        public void start(com.limelight.binding.audio.AndroidAudioRenderer audioRenderer,
                          com.limelight.binding.video.MediaCodecDecoderRenderer decoderRenderer,
                          com.limelight.nvstream.NvConnectionListener listener) {
            started = true;
        }

        @Override
        public void stop() {
            stopped = true;
        }
    }

    private static class FakeDecoder implements StreamingSession.DecoderAdapter {
        boolean renderTargetSet;

        @Override
        public void setRenderTarget(Surface surface) {
            renderTargetSet = true;
        }

        @Override
        public void prepareForStop() {
        }
    }

    private static class ReadyVideoSink extends NotReadyVideoSink {
        @Override
        public boolean isReady() {
            return true;
        }
    }

    private static class NotReadyVideoSink implements VideoSink {
        @Override
        public Surface getSurface() {
            return null;
        }

        @Override
        public void setCallback(Callback callback) {
        }

        @Override
        public int getPreferredWidth() {
            return 0;
        }

        @Override
        public int getPreferredHeight() {
            return 0;
        }

        @Override
        public float getPreferredRefreshRate() {
            return 0;
        }

        @Override
        public boolean isReady() {
            return false;
        }
    }
}
