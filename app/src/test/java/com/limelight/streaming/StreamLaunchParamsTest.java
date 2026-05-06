package com.limelight.streaming;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import android.content.Intent;

import com.limelight.Game;
import com.limelight.nvstream.StreamConfiguration;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

@RunWith(RobolectricTestRunner.class)
public class StreamLaunchParamsTest {
    @Test
    public void fromIntentReadsStreamExtras() {
        Intent intent = new Intent()
                .putExtra(Game.EXTRA_HOST, "192.168.1.20")
                .putExtra(Game.EXTRA_PORT, 47984)
                .putExtra(Game.EXTRA_HTTPS_PORT, 47989)
                .putExtra(Game.EXTRA_APP_NAME, "Desktop")
                .putExtra(Game.EXTRA_APP_UUID, "uuid-app")
                .putExtra(Game.EXTRA_APP_ID, 7)
                .putExtra(Game.EXTRA_UNIQUEID, "client-id")
                .putExtra(Game.EXTRA_PC_UUID, "pc-uuid")
                .putExtra(Game.EXTRA_PC_NAME, "Gaming PC")
                .putExtra(Game.EXTRA_VDISPLAY, true);

        StreamLaunchParams params = StreamLaunchParams.fromIntent(intent);

        assertTrue(params.isValid());
        assertEquals("192.168.1.20", params.getHost());
        assertEquals(47984, params.getPort());
        assertEquals(47989, params.getHttpsPort());
        assertEquals("Desktop", params.getAppName());
        assertEquals("uuid-app", params.getAppUuid());
        assertEquals(7, params.getAppId());
        assertEquals("client-id", params.getUniqueId());
        assertEquals("pc-uuid", params.getPcUuid());
        assertEquals("Gaming PC", params.getPcName());
        assertTrue(params.isVirtualDisplay());
        assertEquals("Desktop", params.createApp().getAppName());
    }

    @Test
    public void invalidWhenAppIdIsMissing() {
        Intent intent = new Intent()
                .putExtra(Game.EXTRA_HOST, "192.168.1.20")
                .putExtra(Game.EXTRA_UNIQUEID, "client-id");

        StreamLaunchParams params = StreamLaunchParams.fromIntent(intent);

        assertEquals(StreamConfiguration.INVALID_APP_ID, params.getAppId());
        assertFalse(params.isValid());
    }
}
