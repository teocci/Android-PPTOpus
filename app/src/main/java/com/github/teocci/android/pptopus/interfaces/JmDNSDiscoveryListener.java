package com.github.teocci.android.pptopus.interfaces;

import javax.jmdns.ServiceEvent;

/**
 * Created by teocci.
 *
 * @author teocci@yandex.com on 2018-Oct-11
 */
public interface JmDNSDiscoveryListener
{
    void onServiceResolved(ServiceEvent event);

    void onServiceRemoved(ServiceEvent event);
}
