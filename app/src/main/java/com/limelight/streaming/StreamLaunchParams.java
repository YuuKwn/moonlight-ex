package com.limelight.streaming;

import android.content.Context;
import android.content.Intent;
import android.view.Display;

import com.limelight.Game;
import com.limelight.binding.PlatformBinding;
import com.limelight.nvstream.StreamConfiguration;
import com.limelight.nvstream.http.ComputerDetails;
import com.limelight.nvstream.http.NvApp;
import com.limelight.nvstream.http.NvHTTP;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.ArrayList;

public class StreamLaunchParams {
    private final String appName;
    private final String pcName;
    private final String host;
    private final int port;
    private final int httpsPort;
    private final String appUuid;
    private final int appId;
    private final String uniqueId;
    private final String pcUuid;
    private final boolean virtualDisplay;
    private final boolean appSupportsHdr;
    private final ArrayList<String> serverCommands;
    private final X509Certificate serverCert;
    private final int displayId;

    public StreamLaunchParams(String appName,
                              String pcName,
                              String host,
                              int port,
                              int httpsPort,
                              String appUuid,
                              int appId,
                              String uniqueId,
                              String pcUuid,
                              boolean virtualDisplay,
                              boolean appSupportsHdr,
                              ArrayList<String> serverCommands,
                              X509Certificate serverCert,
                              int displayId) {
        this.appName = appName;
        this.pcName = pcName;
        this.host = host;
        this.port = port;
        this.httpsPort = httpsPort;
        this.appUuid = appUuid;
        this.appId = appId;
        this.uniqueId = uniqueId;
        this.pcUuid = pcUuid;
        this.virtualDisplay = virtualDisplay;
        this.appSupportsHdr = appSupportsHdr;
        this.serverCommands = serverCommands;
        this.serverCert = serverCert;
        this.displayId = displayId;
    }

    public static StreamLaunchParams fromIntent(Intent intent) {
        byte[] derCertData = intent.getByteArrayExtra(Game.EXTRA_SERVER_CERT);
        return new StreamLaunchParams(
                intent.getStringExtra(Game.EXTRA_APP_NAME),
                intent.getStringExtra(Game.EXTRA_PC_NAME),
                intent.getStringExtra(Game.EXTRA_HOST),
                intent.getIntExtra(Game.EXTRA_PORT, NvHTTP.DEFAULT_HTTP_PORT),
                intent.getIntExtra(Game.EXTRA_HTTPS_PORT, 0),
                intent.getStringExtra(Game.EXTRA_APP_UUID),
                intent.getIntExtra(Game.EXTRA_APP_ID, StreamConfiguration.INVALID_APP_ID),
                intent.getStringExtra(Game.EXTRA_UNIQUEID),
                intent.getStringExtra(Game.EXTRA_PC_UUID),
                intent.getBooleanExtra(Game.EXTRA_VDISPLAY, false),
                intent.getBooleanExtra(Game.EXTRA_APP_HDR, false),
                intent.getStringArrayListExtra(Game.EXTRA_SERVER_COMMANDS),
                decodeCertificate(derCertData),
                intent.getIntExtra(Game.EXTRA_DISPLAY_ID, Display.DEFAULT_DISPLAY)
        );
    }

    private static X509Certificate decodeCertificate(byte[] derCertData) {
        if (derCertData == null) {
            return null;
        }
        try {
            return (X509Certificate) CertificateFactory.getInstance("X.509")
                    .generateCertificate(new ByteArrayInputStream(derCertData));
        } catch (Exception e) {
            return null;
        }
    }

    public boolean isValid() {
        return appId != StreamConfiguration.INVALID_APP_ID && host != null && uniqueId != null;
    }

    public NvApp createApp() {
        return new NvApp(appName != null ? appName : "app", appUuid, appId, appSupportsHdr);
    }

    public NvHTTP createHttpConnection(Context context) {
        if (serverCert == null) {
            return null;
        }
        try {
            return new NvHTTP(new ComputerDetails.AddressTuple(host, port), httpsPort, uniqueId,
                    serverCert, PlatformBinding.getCryptoProvider(context));
        } catch (IOException e) {
            return null;
        }
    }

    public String getAppName() { return appName; }
    public String getPcName() { return pcName; }
    public String getHost() { return host; }
    public int getPort() { return port; }
    public int getHttpsPort() { return httpsPort; }
    public String getAppUuid() { return appUuid; }
    public int getAppId() { return appId; }
    public String getUniqueId() { return uniqueId; }
    public String getPcUuid() { return pcUuid; }
    public boolean isVirtualDisplay() { return virtualDisplay; }
    public ArrayList<String> getServerCommands() { return serverCommands; }
    public X509Certificate getServerCert() { return serverCert; }
    public int getDisplayId() { return displayId; }
}
