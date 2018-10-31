package com.github.teocci.android.pptopus.managers;

import android.util.Base64;

import com.github.teocci.android.pptopus.model.DeviceInfo;
import com.github.teocci.android.pptopus.model.ServiceInfo;
import com.github.teocci.android.pptopus.utils.LogHelper;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import static android.util.Base64.DEFAULT;
import static com.github.teocci.android.pptopus.utils.Config.SERVICE_NAME_SEPARATOR;
import static com.github.teocci.android.pptopus.utils.UtilHelper.getIPv4Addresses;

/**
 * Created by teocci.
 *
 * @author teocci@yandex.com on 2018-Oct-11
 */
public class ServiceListManager
{
    private static final String TAG = LogHelper.makeLogTag(ServiceListManager.class);

    private Map<String, ServiceInfo> services = new ConcurrentHashMap<>();


    public boolean add(String serviceName, javax.jmdns.ServiceInfo info)
    {
        ServiceInfo serviceInfo = parseService(info);
        if (serviceInfo == null) return false;
        if (contains(serviceName)) return false;
        services.put(serviceName, serviceInfo);

//        servicesView = FXCollections.observableArrayList(services.values());
//        listProperty.replace(FXCollections.observableArrayList(services.values()));

        LogHelper.w(TAG, "[add] " + serviceInfo.getDeviceName());
        return true;
    }

    public boolean remove(String serviceName)
    {
        if (!contains(serviceName)) return false;

        services.remove(serviceName);

//        servicesView = FXCollections.observableArrayList(services.values());
//        listProperty.replace(FXCollections.observableArrayList(services.values()));

        LogHelper.w(TAG, "[remove] " + serviceName);
        return true;
    }

    public void clear()
    {
        services.clear();

        LogHelper.w(TAG, "[clear]");
    }

    public boolean updateDeviceName(String serviceName, String newName)
    {
        if (serviceName.trim().isEmpty()) return false;
        if (!contains(serviceName)) return false;

        // When Name was modified
        ServiceInfo serviceInfo = getServiceInfo(serviceName);
        serviceInfo.setDeviceName(newName);
        services.put(serviceName, serviceInfo);

        LogHelper.w(TAG, "[updateDeviceName] " + serviceName);
        return true;
    }

    /**
     * Parse a Service Information When service table is creating (For Exception)
     *
     * @param info Service description for registering with JmDNS
     * @return ServiceInfo
     */
    private ServiceInfo parseService(javax.jmdns.ServiceInfo info)
    {
        ServiceInfo serviceInfo = null; // ServiceInfo(is not JmDNS, with model dir.)

        String serviceName = info.getName(); // Device name
        String[] split = serviceName.split(SERVICE_NAME_SEPARATOR);
        List<String> ipList = getIPv4Addresses(info.getHostAddresses());

        if (split.length != 4) return null;
        if (ipList == null) return null;

        String deviceIP = getValidIp(ipList, info.getPort());
        if (deviceIP == null) return null;

        String appName, channelName, deviceId, deviceName;
        appName = new String(Base64.decode(split[0], DEFAULT));
        channelName = new String(Base64.decode(split[1], DEFAULT));
        deviceId = split[2];
        deviceName = new String(Base64.decode(split[3], DEFAULT));

        serviceInfo = new ServiceInfo()
                .setServiceName(split)
                .setDeviceId(deviceId)
                .setDeviceName(deviceName)
                .setAddress(deviceIP) // Arrays.toString(info.getHostAddresses()))
                .setPort(info.getPort());

//        // if ServiceName hasn't a colon.
//        if (!serviceName.contains(SERVICE_NAME_SEPARATOR)) {
//            serviceInfo = new ServiceInfo()
//                    .setDeviceIP(deviceIP)//Arrays.toString(info.getHostAddresses()))
//                    .setDeviceName(serviceName)
//                    .setDeviceId(serviceName)
//                    .setServiceName(split)
//                    .setStreamingPort(info.getPort());
//        } else { // if ServiceName has a colon.
//        }

        LogHelper.w(TAG, "[parseService]");
        return serviceInfo;
    }

    public boolean containsIPAddress(String ipAddress, int port)
    {
        if (isEmpty()) return false;

        for (Map.Entry<String, ServiceInfo> entry : services.entrySet()) {
            ServiceInfo serviceInfo = entry.getValue();
            if (ipAddress.equals(serviceInfo.getAddress())) {
                if (port == serviceInfo.getPort())
                    return true;
            }
        }

        return false;
    }

    public boolean contains(String serviceName)
    {
        return !isEmpty() && services.containsKey(serviceName);
    }

    public boolean contains(ServiceInfo serviceInfo)
    {
        String serviceName = serviceInfo.getServiceName();
        return contains(serviceName);
    }

    public ServiceInfo getServiceInfo(String serviceName)
    {
        if (serviceName == null || serviceName.isEmpty()) return null;
        return contains(serviceName) ? services.get(serviceName) : null;
    }

    public String[] getServiceNames()
    {
        if (isEmpty()) return null;

        Set<String> keys = services.keySet();
        return keys.toArray(new String[keys.size()]);
    }

    public ServiceInfo[] getServiceInfos()
    {
        if (isEmpty()) return null;

        Collection<ServiceInfo> values = services.values();
        return values.toArray(new ServiceInfo[values.size()]);
    }

    public List<DeviceInfo> getDeviceList()
    {
        List<DeviceInfo> devices = new ArrayList<>();
        if (!isEmpty()) {
            for (ServiceInfo service : services.values()) {
                DeviceInfo station = new DeviceInfo(service.getDeviceName(), service.getAddress(), 0, 0);
                devices.add(station);
            }

            return devices;
        }

        return devices;
    }

    public List<ServiceInfo> getServiceList()
    {
        return new ArrayList<>(services.values());
    }

    private String getValidIp(List<String> ipList, int port)
    {
        if (ipList == null) return null;

        for (String ipv4 : ipList) {
            if (!containsIPAddress(ipv4, port)) {
                return ipv4;
            }
        }

        return null;
    }

    public InetAddress getIPAddress(String serviceName)
    {
        if (isEmpty()) return null;
        if (serviceName == null || serviceName.isEmpty()) return null;
        if (!contains(serviceName)) return null;

        ServiceInfo service = services.get(serviceName);
        if (service == null) return null;

        String address = service.getAddress();
        if (address == null || address.isEmpty()) return null;
        try {
            return InetAddress.getByName(address);
        } catch (UnknownHostException e) {
            e.printStackTrace();
            return null;
        }
    }

    public int getPort(String serviceName)
    {
        if (isEmpty()) return -1;
        if (serviceName == null || serviceName.isEmpty()) return -1;
        if (!contains(serviceName)) return -1;

        ServiceInfo service = services.get(serviceName);
        if (service == null) return -1;

        return service.getPort();
    }


    public boolean isEmpty()
    {
        return services != null && services.isEmpty();
    }
}