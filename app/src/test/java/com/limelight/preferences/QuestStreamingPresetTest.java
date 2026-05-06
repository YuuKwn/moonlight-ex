package com.limelight.preferences;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.test.core.app.ApplicationProvider;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

@Config(sdk = {33})
@RunWith(RobolectricTestRunner.class)
public class QuestStreamingPresetTest {
    @Test
    public void balancedPresetUsesQuestPanelDefaults() {
        QuestStreamingPreset preset = QuestStreamingPreset.fromId("balanced");

        assertEquals("balanced", preset.id);
        assertEquals(PreferenceConfiguration.RES_1080P, preset.resolution);
        assertEquals("90", preset.fps);
        assertEquals(45000, preset.bitrateKbps);
        assertFalse(preset.preferLowerDelay);
    }

    @Test
    public void unknownPresetFallsBackToBalanced() {
        QuestStreamingPreset preset = QuestStreamingPreset.fromId("not-a-preset");

        assertEquals("balanced", preset.id);
        assertEquals(PreferenceConfiguration.RES_1080P, preset.resolution);
        assertEquals("90", preset.fps);
    }

    @Test
    public void applyWritesStreamingPreferences() {
        Context context = ApplicationProvider.getApplicationContext();
        SharedPreferences prefs = context.getSharedPreferences("quest-preset-test", Context.MODE_PRIVATE);
        prefs.edit().clear().commit();

        QuestStreamingPreset.apply(prefs, "low_latency");

        assertEquals("low_latency", prefs.getString(QuestStreamingPreset.PREF_KEY, ""));
        assertEquals(PreferenceConfiguration.RES_1080P,
                prefs.getString(PreferenceConfiguration.RESOLUTION_PREF_STRING, ""));
        assertEquals("120", prefs.getString(PreferenceConfiguration.FPS_PREF_STRING, ""));
        assertEquals(35000, prefs.getInt(PreferenceConfiguration.BITRATE_PREF_STRING, 0));
        assertEquals("latency", prefs.getString(PreferenceConfiguration.FRAME_PACING_PREF_STRING, ""));
        assertTrue(prefs.getBoolean(PreferenceConfiguration.LOW_LATENCY_FRAME_BALANCE_PREF_STRING, false));
    }
}
