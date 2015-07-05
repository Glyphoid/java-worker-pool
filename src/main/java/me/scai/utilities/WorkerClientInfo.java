package me.scai.utilities;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import me.scai.utilities.clienttypes.ClientTypeMajor;
import me.scai.utilities.clienttypes.ClientTypeMinor;

import java.net.InetAddress;

public class WorkerClientInfo {
    private ClientTypeMajor clientTypeMajor;
    private ClientTypeMinor clientTypeMinor;
    private String clientPlatformVersion;
    private String clientAppVersion;

    private JsonObject customClientData; /* Can store info such as screen size, etc. */

    private InetAddress clientIPAddress;
    private String clientHostName;

    public WorkerClientInfo(final InetAddress clientIPAddress,
                            final String clientHostName,
                            final ClientTypeMajor clientTypeMajor,
                            final ClientTypeMinor clientTypeMinor) {
        this.clientIPAddress  = clientIPAddress;
        this.clientHostName   = clientHostName;

        this.clientTypeMajor  = clientTypeMajor;
        this.clientTypeMinor  = clientTypeMinor;
    }

    public WorkerClientInfo(final InetAddress clientIPAddress,
                            final String clientHostName,
                            final ClientTypeMajor clientTypeMajor,
                            final ClientTypeMinor clientTypeMinor,
                            final String clientPlatformVersion,
                            final String clientAppVersion) {
        this(clientIPAddress, clientHostName, clientTypeMajor, clientTypeMinor);

        this.clientPlatformVersion = clientPlatformVersion;
        this.clientAppVersion = clientAppVersion;
    }

    public WorkerClientInfo(final InetAddress clientIPAddress,
                            final String clientHostName,
                            final ClientTypeMajor clientTypeMajor,
                            final ClientTypeMinor clientTypeMinor,
                            final String clientPlatformVersion,
                            final String clientAppVersion,
                            final JsonObject customClientData) {
        this(clientIPAddress, clientHostName, clientTypeMajor, clientTypeMinor,
             clientPlatformVersion, clientAppVersion);

        this.customClientData = customClientData;
    }





    /* Getters */
    public InetAddress getClientIPAddress() {
        return clientIPAddress;
    }

    public String getClientHostName() {
        return clientHostName;
    }

    public ClientTypeMajor getClientTypeMajor() {
        return clientTypeMajor;
    }

    public ClientTypeMinor getClientTypeMinor() {
        return clientTypeMinor;
    }

    public String getClientPlatformVersion() {
        return clientPlatformVersion;
    }

    public String getClientAppVersion() {
        return clientAppVersion;
    }

    public JsonObject getCustomClientData() {
        return customClientData;
    }

    public JsonElement getFromCustomClientData(final String key) {
        if (customClientData != null && customClientData.has(key)) {
            return customClientData.get(key);
        } else {
            return null;
        }
    }




}
