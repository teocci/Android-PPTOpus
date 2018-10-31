package com.github.teocci.android.pptopus.net;

import com.github.teocci.android.pptopus.audio.Configuration;

import java.net.DatagramSocket;
import java.net.InetAddress;

public class WSConnection
{
    public static WSServer server;

    public static long sent = 0;

    public static short identCode;

    public static void setSent()
    {
        sent = System.currentTimeMillis() + Configuration.KEEP_ALIVE;
    }

    public static void setIdent(short code)
    {
        identCode = code;
    }
}
