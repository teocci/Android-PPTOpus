package com.github.teocci.android.pptopus.model;

/**
 * Created by teocci.
 *
 * @author teocci@yandex.com on 2017-Jul-17
 */
public class DeviceInfo
{
    public final String name;
    public final String address;
    public int transmission;
    public long ping;

    public DeviceInfo(String name, String address, int transmission, long ping)
    {
        this.name = name;
        this.address = address;
        this.transmission = transmission;
        this.ping = ping;
    }

    @Override
    public String toString()
    {
        return "[DeviceInfo] { name: '" + name +
                "', address: " + address +
                "', transmission: '" + transmission +
                "', ping: '" + ping +
                "' }";
    }
}
