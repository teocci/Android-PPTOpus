package com.github.teocci.android.pptopus.interfaces;

import javax.jmdns.ServiceInfo;

/**
 * Created by teocci.
 *
 * @author teocci@yandex.com on 2018-Aug-13
 */
public interface JmDNSServiceListener
{
    void onServiceRegistered(ServiceInfo serviceInfo);

    void onServiceRegistrationFailed(ServiceInfo serviceInfo);
}
