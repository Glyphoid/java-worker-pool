package me.scai.utilities;

import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import me.scai.utilities.clienttypes.ClientTypeMajor;
import me.scai.utilities.clienttypes.ClientTypeMinor;
import org.junit.Test;

import java.net.InetAddress;
import java.net.UnknownHostException;

import static org.junit.Assert.assertEquals;

public class TestWorkerClientTypeInfo {
    @Test
    public void TestWorkerClientTypeInfo_mobileAndroid() {
        WorkerClientInfo clientInfo = new WorkerClientInfo(null,
                "foo.bar.qux",
                ClientTypeMajor.MobileAndroid,
                ClientTypeMinor.None);

        assertEquals("foo.bar.qux", clientInfo.getClientHostName());
        assertEquals(ClientTypeMajor.MobileAndroid, clientInfo.getClientTypeMajor());
        assertEquals(ClientTypeMinor.None, clientInfo.getClientTypeMinor());

        assertEquals(null, clientInfo.getCustomClientData());
    }

    @Test
    public void TestWorkerClientTypeInfo_mobileAndroidWithVersions() {
        WorkerClientInfo clientInfo = new WorkerClientInfo(null,
                                                            "foo.bar.qux",
                                                            ClientTypeMajor.MobileAndroid,
                                                            ClientTypeMinor.None,
                                                            "4.3",
                                                            "0.2");

        assertEquals("foo.bar.qux", clientInfo.getClientHostName());
        assertEquals(ClientTypeMajor.MobileAndroid, clientInfo.getClientTypeMajor());
        assertEquals(ClientTypeMinor.None, clientInfo.getClientTypeMinor());
        assertEquals("4.3", clientInfo.getClientPlatformVersion());
        assertEquals("0.2", clientInfo.getClientAppVersion());

        assertEquals(null, clientInfo.getCustomClientData());
    }

    @Test
    public void TestWorkerClientTypeInfo_mobileAndroidWithVersionsAndCustomData() {
        JsonObject customClientData = new JsonObject();

        customClientData.add("ScreenWidth", new JsonPrimitive(800));
        customClientData.add("ScreenHeight", new JsonPrimitive(1000));

        WorkerClientInfo clientInfo = new WorkerClientInfo(null,
                                                          "foo.bar.qux",
                                                           ClientTypeMajor.MobileAndroid,
                                                           ClientTypeMinor.None,
                                                          "4.3",
                                                          "0.2",
                                                          customClientData);

        assertEquals("foo.bar.qux", clientInfo.getClientHostName());
        assertEquals(ClientTypeMajor.MobileAndroid, clientInfo.getClientTypeMajor());
        assertEquals(ClientTypeMinor.None, clientInfo.getClientTypeMinor());
        assertEquals("4.3", clientInfo.getClientPlatformVersion());
        assertEquals("0.2", clientInfo.getClientAppVersion());
        assertEquals(800, clientInfo.getCustomClientData().get("ScreenWidth").getAsInt());
        assertEquals(1000, clientInfo.getCustomClientData().get("ScreenHeight").getAsInt());
        assertEquals(null, clientInfo.getCustomClientData().get("colorDepth"));
    }

    @Test
    public void TestWorkerClientTypeInfo_desktopBrowserWithVersionsAndCustomData() {
        JsonObject customClientData = new JsonObject();

        customClientData.add("ScreenWidth", new JsonPrimitive(1024));
        customClientData.add("ScreenHeight", new JsonPrimitive(768));
        customClientData.add("colorDepth", new JsonPrimitive("24 bits"));

        WorkerClientInfo clientInfo = new WorkerClientInfo(null,
                                                           "foo.bar.qux",
                                                           ClientTypeMajor.DesktopBrowser,
                                                           ClientTypeMinor.DesktopBrowser_Chrome,
                                                           "43.0.2357.130",
                                                           "0.2b",
                                                           customClientData);

        assertEquals("foo.bar.qux", clientInfo.getClientHostName());
        assertEquals(ClientTypeMajor.DesktopBrowser, clientInfo.getClientTypeMajor());
        assertEquals(ClientTypeMinor.DesktopBrowser_Chrome, clientInfo.getClientTypeMinor());
        assertEquals("43.0.2357.130", clientInfo.getClientPlatformVersion());
        assertEquals("0.2b", clientInfo.getClientAppVersion());
        assertEquals(1024, clientInfo.getCustomClientData().get("ScreenWidth").getAsInt());
        assertEquals(768, clientInfo.getCustomClientData().get("ScreenHeight").getAsInt());
        assertEquals("24 bits", clientInfo.getCustomClientData().get("colorDepth").getAsString());
        assertEquals(null, clientInfo.getCustomClientData().get("microphoneReady"));
    }

}
