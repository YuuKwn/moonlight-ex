package com.limelight.streaming;

import static org.junit.Assert.assertEquals;

import com.limelight.preferences.PreferenceConfiguration;

import org.junit.Test;

public class StreamConfigurationFactoryTest {
    @Test
    public void capsFpsJustBelowMatchingDisplayRefreshRate() {
        PreferenceConfiguration preferences = new PreferenceConfiguration();
        preferences.fps = 90;
        preferences.framePacing = PreferenceConfiguration.FRAME_PACING_CAP_FPS;

        assertEquals(89f, StreamConfigurationFactory.chooseFrameRate(preferences, 90f), 0.01f);
    }

    @Test
    public void fallsBackToBalancedWhenRequestedFpsExceedsDisplayRefreshRate() {
        PreferenceConfiguration preferences = new PreferenceConfiguration();
        preferences.fps = 120;
        preferences.framePacing = PreferenceConfiguration.FRAME_PACING_CAP_FPS;

        assertEquals(120f, StreamConfigurationFactory.chooseFrameRate(preferences, 90f), 0.01f);
        assertEquals(PreferenceConfiguration.FRAME_PACING_BALANCED, preferences.framePacing);
    }

    @Test
    public void appliesWarpFactorAfterRefreshSelection() {
        PreferenceConfiguration preferences = new PreferenceConfiguration();
        preferences.fps = 60;
        preferences.framePacing = PreferenceConfiguration.FRAME_PACING_BALANCED;
        preferences.framePacingWarpFactor = 2;

        assertEquals(120f, StreamConfigurationFactory.chooseFrameRate(preferences, 120f), 0.01f);
    }
}
