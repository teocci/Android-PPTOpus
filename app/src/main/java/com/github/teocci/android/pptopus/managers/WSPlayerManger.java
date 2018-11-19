package com.github.teocci.android.pptopus.managers;

import com.github.teocci.android.pptopus.model.DeviceInfo;
import com.github.teocci.android.pptopus.net.WSAudioPlayer;
import com.github.teocci.android.pptopus.net.WSClient;
import com.github.teocci.android.pptopus.utils.LogHelper;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by teocci.
 *
 * @author teocci@yandex.com on 2018-Oct-31
 */
public class WSPlayerManger
{
    private static final String TAG = LogHelper.makeLogTag(WSPlayerManger.class);

    private Map<String, WSAudioPlayer> wsPlayers = new ConcurrentHashMap<>();

    public boolean add(String address, WSAudioPlayer wsAudioPlayer)
    {
        if (contains(address)) return false;

        wsPlayers.put(address, wsAudioPlayer);

        LogHelper.w(TAG, "[add]");
        return true;
    }

    public boolean remove(String address)
    {
        if (!contains(address)) return false;

        wsPlayers.remove(address);

        LogHelper.w(TAG, "[remove && close]");
        return true;
    }

    public boolean contains(String address)
    {
        return !isEmpty() && wsPlayers.containsKey(address);
    }

    public List<WSClient> getAllClients()
    {
        List<WSClient> serviceInfos = new ArrayList<>();
        if (isEmpty()) return serviceInfos;

        for (Map.Entry<String, WSAudioPlayer> e : wsPlayers.entrySet()) {
//            String serviceName = e.getKey();
            WSAudioPlayer wsAudioPlayer = e.getValue();
            if (wsAudioPlayer != null) {
                serviceInfos.add(wsAudioPlayer.getWSClient());
            }
        }

        return serviceInfos;
    }

    public void stopAll()
    {
        if (!isEmpty()) {
            for (WSAudioPlayer connection : wsPlayers.values()) {
                connection.stop();
            }
        }
    }

    public WSAudioPlayer get(String address)
    {
        return contains(address) ? wsPlayers.get(address) : null;
    }

    public WSAudioPlayer[] getAll()
    {
        return (wsPlayers != null && !wsPlayers.isEmpty()) ?
                wsPlayers.values().toArray(new WSAudioPlayer[0]) : new WSAudioPlayer[0];
    }

    public List<DeviceInfo> getDeviceList()
    {
        List<DeviceInfo> devices = new ArrayList<>();
        if (!isEmpty()) {
            for (WSAudioPlayer wsAudioPlayer : wsPlayers.values()) {
                DeviceInfo station = new DeviceInfo(wsAudioPlayer.getDeviceName(), wsAudioPlayer.getAddress(), 0, 0);
                devices.add(station);
            }

            return devices;
        }

        return devices;
    }

    public boolean isPlaying(String location)
    {
        if (!contains(location)) return false;

        WSAudioPlayer wsAudioPlayer = get(location);
        // Check Streaming SocketManager and Controller SocketManager
        return wsAudioPlayer != null && !wsAudioPlayer.isPlaying();
    }

    public boolean isEmpty()
    {
        return wsPlayers != null && wsPlayers.isEmpty();
    }
}
