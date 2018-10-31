package com.github.teocci.android.pptopus.managers;

import com.github.teocci.android.pptopus.net.WSClient;
import com.github.teocci.android.pptopus.net.WSPlayer;
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

    private Map<String, WSPlayer> wsPlayers = new ConcurrentHashMap<>();

    public boolean add(String location, WSPlayer wsPlayer)
    {
        if (contains(location)) return false;

        wsPlayers.put(location, wsPlayer);

        LogHelper.w(TAG, "[add]");
        return true;
    }

    public boolean remove(String location)
    {
        if (!contains(location)) return false;

        wsPlayers.remove(location);

        LogHelper.w(TAG, "[remove && close]");
        return true;
    }

    public boolean contains(String location)
    {
        return !isEmpty() && wsPlayers.containsKey(location);
    }

    public List<WSClient> getAllClients()
    {
        List<WSClient> serviceInfos = new ArrayList<>();
        if (isEmpty()) return serviceInfos;

        for (Map.Entry<String, WSPlayer> e : wsPlayers.entrySet()) {
//            String serviceName = e.getKey();
            WSPlayer wsPlayer = e.getValue();
            if (wsPlayer != null) {
                serviceInfos.add(wsPlayer.getWSClient());
            }
        }

        return serviceInfos;
    }

    public void stopAll()
    {
        if (!isEmpty()) {
            for (WSPlayer connection : wsPlayers.values()) {
                connection.stop();
            }
        }
    }

    public WSPlayer get(String location)
    {
        return contains(location) ? wsPlayers.get(location) : null;
    }

    public WSPlayer[] getAll()
    {
        return (wsPlayers != null && !wsPlayers.isEmpty()) ?
                wsPlayers.values().toArray(new WSPlayer[wsPlayers.size()]) :
                new WSPlayer[0];
    }

    public boolean isPlaying(String location)
    {
        if (!contains(location)) return false;

        WSPlayer wsPlayer = get(location);
        // Check Streaming SocketManager and Controller SocketManager
        return wsPlayer != null && !wsPlayer.isPlaying();
    }

    public boolean isEmpty()
    {
        return wsPlayers != null && wsPlayers.isEmpty();
    }
}
