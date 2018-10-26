package com.github.teocci.android.pptopus.utils;

import android.content.Context;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.util.Base64;

import com.github.teocci.android.pptopus.interfaces.JmDNSDiscoveryListener;
import com.github.teocci.android.pptopus.interfaces.JmDNSServiceListener;
import com.github.teocci.android.pptopus.interfaces.ServiceRegisteredListener;
import com.github.teocci.android.pptopus.net.JmDNSRegisterTask;

import java.io.IOException;
import java.net.InetAddress;

import javax.jmdns.JmDNS;
import javax.jmdns.ServiceEvent;
import javax.jmdns.ServiceInfo;
import javax.jmdns.ServiceListener;

import static com.github.teocci.android.pptopus.utils.Config.DEFAULT_RTSP_PORT;
import static com.github.teocci.android.pptopus.utils.Config.SERVICE_APP_NAME;
import static com.github.teocci.android.pptopus.utils.Config.SERVICE_CHANNEL_NAME;
import static com.github.teocci.android.pptopus.utils.Config.SERVICE_NAME_SEPARATOR;
import static com.github.teocci.android.pptopus.utils.Config.SERVICE_TYPE;
import static com.github.teocci.android.pptopus.utils.UtilHelper.getDeviceID;
import static com.github.teocci.android.pptopus.utils.UtilHelper.obtainIPv4Address;

/**
 * Created by teocci.
 *
 * @author teocci@yandex.com on 2018-Aug-10
 */
public class JmDNSHelper implements JmDNSServiceListener
{
    private static String TAG = LogHelper.makeLogTag(JmDNSHelper.class);

    private Context context;

    private JmDNS jmDNS;

    private boolean serviceRegistered = false;
    private boolean discoveryStarted = false;

    private String stationName;
    private String serviceName;

    private JmDNSDiscoveryListener discoveryListener;
    private ServiceRegisteredListener registeredListener;

    private ServiceListener serviceListener = new ServiceListener()
    {
        public void serviceAdded(ServiceEvent event)
        {
            LogHelper.e(TAG, "Service add request: ", String.valueOf(event.getInfo()));
        }

        public void serviceRemoved(ServiceEvent event)
        {
            LogHelper.e(TAG, "Service remove request: ", String.valueOf(event.getInfo()));
        }

        public void serviceResolved(ServiceEvent event)
        {
            LogHelper.e(TAG, "Service resolve request: ", String.valueOf(event.getInfo()));
//            int port = event.getInfo().getPort();
//            InetAddress host = event.getInfo().getInetAddresses()[0];
//            LogHelper.e(TAG, "Service resolved: (host, port) -> (" + host.getHostAddress() + ", " + port + ')');
            if (discoveryListener != null) {
                discoveryListener.onServiceResolved(event);
            }
        }
    };

//    private WifiManager.MulticastLock multicastLock;

    public JmDNSHelper(Context context, String stationName)
    {
        this(context, stationName, null);
    }

    public JmDNSHelper(Context context, String stationName, JmDNSDiscoveryListener discoveryListener)
    {
        this(context, stationName, discoveryListener, null);
    }

    public JmDNSHelper(Context context, String stationName, JmDNSDiscoveryListener discoveryListener, ServiceRegisteredListener registeredListener)
    {
        this.context = context;
        this.stationName = stationName;
        this.discoveryListener = discoveryListener;
        this.registeredListener = registeredListener;

        final String deviceID = getDeviceID(context.getContentResolver());

        // Android NSD implementation is very unstable when services
        // registers with the same name.
        // Therefore, we will use "SERVICE_CHANNEL_NAME:STATION_NAME:DEVICE_ID:".
        serviceName = Base64.encodeToString(SERVICE_APP_NAME.getBytes(), (Base64.NO_WRAP)) +
                SERVICE_NAME_SEPARATOR +
                Base64.encodeToString(SERVICE_CHANNEL_NAME.getBytes(), (Base64.NO_WRAP)) +
                SERVICE_NAME_SEPARATOR +
                deviceID +
                SERVICE_NAME_SEPARATOR +
                Base64.encodeToString(stationName.getBytes(), (Base64.NO_WRAP)) +
                SERVICE_NAME_SEPARATOR;

        WifiManager wifiManager = (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        WifiInfo info = wifiManager != null ? wifiManager.getConnectionInfo() : null;
        if (info == null) return;

        try {
            jmDNS = JmDNS.create(InetAddress.getByName(obtainIPv4Address(info)), stationName + ".local.");
        } catch (IOException e) {
            LogHelper.e(TAG, "Error in JmDNS creation: " + e);
        }
    }

    @Override
    public void onServiceRegistered(ServiceInfo serviceInfo)
    {
        serviceRegistered = true;
        LogHelper.e(TAG, "onServiceRegistered()-> " + serviceInfo.getName());

        if (registeredListener != null) {
            registeredListener.onServiceRegistered();
        }
    }

    @Override
    public void onServiceRegistrationFailed(ServiceInfo serviceInfo)
    {
        clearServiceRegistered();
        LogHelper.e(TAG, "onServiceRegistrationFailed()-> " + serviceInfo.getName());
    }


    public void registerService()
    {
        LogHelper.e(TAG, "registerService");

        unregisterService();

//            wifiLock();

        final ServiceInfo serviceInfo = ServiceInfo.create(SERVICE_TYPE, serviceName, DEFAULT_RTSP_PORT, stationName);

        new JmDNSRegisterTask(jmDNS, serviceInfo, this).execute();
    }

    public void addDiscovery()
    {
        jmDNS.addServiceListener(SERVICE_TYPE, serviceListener);
        discoveryStarted = true;
    }

    public void unregisterService()
    {
        if (jmDNS != null) {
            jmDNS.unregisterAllServices();
            clearServiceRegistered();
        }
//        if (multicastLock != null && multicastLock.isHeld()) {
//            multicastLock.release();
//        }

        LogHelper.e(TAG, "unregisterService()");
    }

    public void removeDiscovery()
    {
        if (jmDNS != null) {
            jmDNS.removeServiceListener(SERVICE_TYPE, serviceListener);
            discoveryStarted = false;
        }
    }


    private void clearServiceRegistered()
    {
        serviceRegistered = false;
    }


    public void setStationName(String stationName)
    {
        this.stationName = stationName;
    }


//    private void wifiLock()
//    {
//        WifiManager wifiManager = (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
//
//        multicastLock = wifiManager != null ? wifiManager.createMulticastLock(serviceName) : null;
//        if (multicastLock == null) return;
//
//        multicastLock.setReferenceCounted(true);
//        multicastLock.acquire();
//    }

    public boolean isServiceRegistered()
    {
        return serviceRegistered;
    }

    public boolean isDiscoveryStarted()
    {
        return discoveryStarted;
    }
}
