package com.github.teocci.android.pptopus.net;

import com.github.teocci.android.pptopus.interfaces.ws.WSClientListener;
import com.github.teocci.android.pptopus.utils.LogHelper;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;

import java.net.URI;
import java.nio.ByteBuffer;

/**
 * Created by teocci.
 *
 * @author teocci@yandex.com on 2018-Oct-30
 */
public class WSClient extends WebSocketClient
{
    private final String TAG = LogHelper.makeLogTag(WSClient.class);

    private WSClientListener callback;

    public WSClient(URI address, WSClientListener callback)
    {
        super(address);

        this.callback = callback;
    }

    @Override
    public void onOpen(ServerHandshake handshake)
    {
        handshake.getHttpStatus();
        LogHelper.e(TAG, "Connected to Server: " + getURI());
    }

    @Override
    public void onMessage(ByteBuffer buffer)
    {
        if (buffer.hasArray()) {
            byte[] data = new byte[buffer.remaining()];
            buffer.get(data);
            callback.onAudioBuffer(data);
        }
    }

    @Override
    public void onMessage(String message)
    {
        LogHelper.e(TAG, "got: " + message);
    }

    @Override
    public void onClose(int code, String reason, boolean remote)
    {
        LogHelper.e(TAG, "Disconnected from: " + getURI() + "; Code: " + code + " " + reason);
        callback.onClose();
    }

    @Override
    public void onError(Exception ex)
    {
        LogHelper.e(TAG, "Exception occurred ...\n" + ex);
    }

//    public void sendMessage(String message)
//    {
//        if (currentMemberData == null) return;
//
//        send(WrapHelper.msgToJson(message, currentMemberData));
//    }
}