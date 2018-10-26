package com.github.teocci.android.pptopus.model;

import android.net.nsd.NsdServiceInfo;

/**
 * Created by teocci.
 *
 * @author teocci@yandex.com on 2017-Jul-17
 */
public class ServiceInfo
{
    private NsdServiceInfo nsdServiceInfo;
    private String deviceName;
    private String serviceName;

    private String deviceId;
    public String address;

    private int port;
    private boolean resolved;
    private int nsdUpdates;
    private int state;
    private long ping;

    public ServiceInfo()
    {
        resolved = false;
        address = "not resolved";
        nsdUpdates = 0;
        state = 0;
        ping = 0;
    }

    @Override
    public String toString()
    {
        return "[ServiceInfo] { serviceName: '" + serviceName +
                "', deviceNameField: " + deviceName +
                "', address: '" + address +
                "', port: '" + port +
                "' }";
    }

    public ServiceInfo setNsdServiceInfo(NsdServiceInfo nsdServiceInfo)
    {
        this.nsdServiceInfo = nsdServiceInfo;
        return this;
    }

    public ServiceInfo setDeviceName(String deviceName)
    {
        this.deviceName = deviceName;
        return this;
    }

    public ServiceInfo setServiceName(String serviceName)
    {
        this.serviceName = serviceName;
        return this;
    }

    public ServiceInfo setServiceName(String[] serviceName)
    {
        return setServiceName(serviceName[0] + ":" + serviceName[1] + ":" + serviceName[2]);
    }

    public ServiceInfo setDeviceId(String deviceId)
    {
        this.deviceId = deviceId;
        return this;
    }

    public ServiceInfo setAddress(String address)
    {
        this.address = address;
        return this;
    }

    public ServiceInfo setPort(int port)
    {
        this.port = port;
        return this;
    }

    public ServiceInfo setResolved(boolean resolved)
    {
        this.resolved = resolved;
        return this;
    }

    public ServiceInfo setNsdUpdates(int nsdUpdates)
    {
        this.nsdUpdates = nsdUpdates;
        return this;
    }

    public ServiceInfo setState(int state)
    {
        this.state = state;
        return this;
    }

    public ServiceInfo setPing(long ping)
    {
        this.ping = ping;
        return this;
    }

    public int getPort()
    {
        return port;
    }

    public String getAddress()
    {
        return address;
    }

    public String getServiceName()
    {
        return serviceName;
    }

    public String getDeviceId()
    {
        return deviceId;
    }

    public String getDeviceName()
    {
        return deviceName;
    }
}