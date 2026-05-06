package com.limelight.preferences;

import android.content.SharedPreferences;

public final class QuestStreamingPreset {
    public static final String PREF_KEY = "list_quest_streaming_preset";

    public final String id;
    public final String resolution;
    public final String fps;
    public final int bitrateKbps;
    public final String videoFormat;
    public final String framePacing;
    public final boolean preferLowerDelay;

    private QuestStreamingPreset(String id, String resolution, String fps, int bitrateKbps,
                                 String videoFormat, String framePacing, boolean preferLowerDelay) {
        this.id = id;
        this.resolution = resolution;
        this.fps = fps;
        this.bitrateKbps = bitrateKbps;
        this.videoFormat = videoFormat;
        this.framePacing = framePacing;
        this.preferLowerDelay = preferLowerDelay;
    }

    public static QuestStreamingPreset fromId(String id) {
        if ("quality".equals(id)) {
            return new QuestStreamingPreset("quality", PreferenceConfiguration.RES_1440P, "90",
                    70000, "auto", "balanced", false);
        }
        if ("low_latency".equals(id)) {
            return new QuestStreamingPreset("low_latency", PreferenceConfiguration.RES_1080P, "120",
                    35000, "auto", "latency", true);
        }
        if ("desktop_clarity".equals(id)) {
            return new QuestStreamingPreset("desktop_clarity", PreferenceConfiguration.RES_1440P, "60",
                    50000, "auto", "balanced", false);
        }
        return new QuestStreamingPreset("balanced", PreferenceConfiguration.RES_1080P, "90",
                45000, "auto", "latency", false);
    }

    public static void apply(SharedPreferences prefs, String id) {
        QuestStreamingPreset preset = fromId(id);
        prefs.edit()
                .putString(PREF_KEY, preset.id)
                .putString(PreferenceConfiguration.RESOLUTION_PREF_STRING, preset.resolution)
                .putString(PreferenceConfiguration.FPS_PREF_STRING, preset.fps)
                .putInt(PreferenceConfiguration.BITRATE_PREF_STRING, preset.bitrateKbps)
                .putString(PreferenceConfiguration.VIDEO_FORMAT_PREF_STRING, preset.videoFormat)
                .putString(PreferenceConfiguration.FRAME_PACING_PREF_STRING, preset.framePacing)
                .putBoolean(PreferenceConfiguration.LOW_LATENCY_FRAME_BALANCE_PREF_STRING, preset.preferLowerDelay)
                .apply();
    }
}
