package com.github.teocci.android.pptopus.net;

import android.os.AsyncTask;

import com.github.teocci.android.pptopus.audio.UDPConnection;

import java.net.DatagramSocket;
import java.net.SocketException;

/**
 * Created by teocci.
 *
 * @author teocci@yandex.com on 2018-Oct-29
 */
public class UDPTask extends AsyncTask<Void, Void, Void>
{
//    final static int port = UtilHelper.randInt(5000, 10000);
    final static int port = 8082;

    @Override
    protected Void doInBackground(Void... params)
    {
        try {
            UDPConnection.socket = new DatagramSocket(port);
        } catch (SocketException e) {
            e.printStackTrace();
        }

        return null;
    }

    public static int getPort()
    {
        return port;
    }
}
