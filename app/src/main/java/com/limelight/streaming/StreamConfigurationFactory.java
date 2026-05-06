package com.limelight.streaming;

import com.limelight.binding.video.MediaCodecDecoderRenderer;
import com.limelight.nvstream.StreamConfiguration;
import com.limelight.preferences.PreferenceConfiguration;

public class StreamConfigurationFactory {
    public static class Request {
        public final StreamLaunchParams launchParams;
        public final PreferenceConfiguration preferences;
        public final MediaCodecDecoderRenderer decoderRenderer;
        public final int width;
        public final int height;
        public final float displayRefreshRate;
        public final boolean meteredNetwork;
        public final int supportedVideoFormats;
        public final int attachedGamepadMask;

        public Request(StreamLaunchParams launchParams,
                       PreferenceConfiguration preferences,
                       MediaCodecDecoderRenderer decoderRenderer,
                       int width,
                       int height,
                       float displayRefreshRate,
                       boolean meteredNetwork,
                       int supportedVideoFormats,
                       int attachedGamepadMask) {
            this.launchParams = launchParams;
            this.preferences = preferences;
            this.decoderRenderer = decoderRenderer;
            this.width = width;
            this.height = height;
            this.displayRefreshRate = displayRefreshRate;
            this.meteredNetwork = meteredNetwork;
            this.supportedVideoFormats = supportedVideoFormats;
            this.attachedGamepadMask = attachedGamepadMask;
        }
    }

    public StreamConfiguration create(Request request) {
        PreferenceConfiguration preferences = request.preferences;
        float chosenFrameRate = chooseFrameRate(preferences, request.displayRefreshRate);

        return new StreamConfiguration.Builder()
                .setResolution(request.width, request.height)
                .setLaunchRefreshRate(preferences.fps)
                .setRefreshRate(chosenFrameRate)
                .setVirtualDisplay(request.launchParams.isVirtualDisplay())
                .setResolutionScaleFactor(preferences.resolutionScaleFactor)
                .setApp(request.launchParams.createApp())
                .setEnableUltraLowLatency(preferences.enableUltraLowLatency)
                .setBitrate(request.meteredNetwork ? preferences.meteredBitrate : preferences.bitrate)
                .setEnableSops(preferences.enableSops)
                .enableLocalAudioPlayback(preferences.playHostAudio)
                .setMaxPacketSize(1392)
                .setRemoteConfiguration(StreamConfiguration.STREAM_CFG_AUTO)
                .setSupportedVideoFormats(request.supportedVideoFormats)
                .setAttachedGamepadMask(request.attachedGamepadMask)
                .setClientRefreshRateX100((int)(request.displayRefreshRate * 100))
                .setAudioConfiguration(preferences.audioConfiguration)
                .setColorSpace(request.decoderRenderer.getPreferredColorSpace())
                .setColorRange(request.decoderRenderer.getPreferredColorRange())
                .setPersistGamepadsAfterDisconnect(!preferences.multiController)
                .build();
    }

    public static float chooseFrameRate(PreferenceConfiguration preferences, float displayRefreshRate) {
        int roundedRefreshRate = Math.round(displayRefreshRate);
        float chosenFrameRate = preferences.fps;

        if (preferences.framePacing == PreferenceConfiguration.FRAME_PACING_CAP_FPS
                && preferences.fps >= roundedRefreshRate) {
            if (preferences.fps > roundedRefreshRate + 3 || roundedRefreshRate <= 49) {
                preferences.framePacing = PreferenceConfiguration.FRAME_PACING_BALANCED;
            } else {
                chosenFrameRate = roundedRefreshRate - 1;
            }
        }

        if (preferences.framePacingWarpFactor > 0) {
            chosenFrameRate *= preferences.framePacingWarpFactor;
        }

        return chosenFrameRate;
    }
}
