package com.github.teocci.android.pptopus.audio;

import java.net.DatagramSocket;
import java.net.InetAddress;

public class UDPConnection
{
    public static DatagramSocket socket;

    public static InetAddress target;

    public static int port;

    public static long sent = 0;

    public static void setSent()
    {
        sent = System.currentTimeMillis() + Configuration.KEEP_ALIVE;
    }

    public static void setTarget(InetAddress address)
    {
        target = address;
    }

    public static void setPort(int targetPort)
    {
        port = targetPort;
    }
}
