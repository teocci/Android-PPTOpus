package com.github.teocci.android.pptopus.net;

import com.github.teocci.android.pptopus.interfaces.WSListener;
import com.github.teocci.android.pptopus.utils.LogHelper;

import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;

import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by teocci.
 *
 * @author teocci@yandex.com on 2018-Aug-16
 */
public class WSServer extends WebSocketServer
{
    private final String TAG = LogHelper.makeLogTag(WSServer.class);

    private Map<String, WebSocket> clients = new ConcurrentHashMap<>();
    private volatile String currentClient;

    private WSListener wsListener;

    public WSServer(int port, WSListener wsListener)
    {
        super(new InetSocketAddress(port));
        this.wsListener = wsListener;
        setReuseAddr(true);
        setTcpNoDelay(true);
    }

    public WSServer(InetSocketAddress address, WSListener wsListener)
    {
        super(address);
        this.wsListener = wsListener;
        setReuseAddr(true);
        setTcpNoDelay(true);
    }


    @Override
    public void onOpen(WebSocket conn, ClientHandshake handshake)
    {
        if (conn == null) throw new IllegalArgumentException("WebSocket is null");

        String uniqueID = UUID.randomUUID().toString();
        clients.put(uniqueID, conn);
        currentClient = uniqueID;
        conn.send(uniqueID);

        LogHelper.e(TAG, "Client: " + conn.getRemoteSocketAddress().getAddress().getHostAddress());
        if (wsListener != null) {
            String address = conn.getRemoteSocketAddress().getAddress().getHostAddress();
            wsListener.onOpen(address);
        }
    }

    @Override
    public void onClose(WebSocket conn, int code, String reason, boolean remote)
    {
        LogHelper.e(TAG, "Client has been disconnected");
        if (wsListener != null) {
            String address = conn.getRemoteSocketAddress().getAddress().getHostAddress();
            wsListener.onClose(address);
        }
    }

    @Override
    public void onMessage(WebSocket conn, String message)
    {
        if (wsListener != null) {
            String address = conn.getRemoteSocketAddress().getAddress().getHostAddress();
            wsListener.onMessage(address, message);
        }
    }

    @Override
    public void onMessage(WebSocket conn, ByteBuffer message)
    {
        if (wsListener != null) {
            String address = conn.getRemoteSocketAddress().getAddress().getHostAddress();
            wsListener.onMessage(address, new String(message.array(), Charset.forName("UTF-8")));
        }
    }

    @Override
    public void onError(WebSocket conn, Exception ex)
    {
        ex.printStackTrace();
        if (conn != null) {
            // some errors like port binding failed may not be assignable to a specific websocket
        }
    }

    @Override
    public void onStart()
    {
        System.out.println("Server started!");
        setConnectionLostTimeout(0);
        setConnectionLostTimeout(100);
    }

    public void write(String data)
    {
        if (clients == null) return;
        if (currentClient == null || currentClient.isEmpty()) return;

        WebSocket client = clients.get(currentClient);
        if (client == null) return;

        if (!client.isClosed()) {
            client.send(data);
        }
    }
}