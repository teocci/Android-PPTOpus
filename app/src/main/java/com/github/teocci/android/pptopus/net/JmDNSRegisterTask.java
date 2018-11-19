package com.github.teocci.android.pptopus.net;

import android.os.AsyncTask;

import com.github.teocci.android.pptopus.interfaces.jmdns.JmDNSServiceListener;

import java.io.IOException;

import javax.jmdns.JmDNS;
import javax.jmdns.ServiceInfo;

/**
 * Created by teocci.
 *
 * @author teocci@yandex.com on 2018-Aug-13
 */
public class JmDNSRegisterTask extends AsyncTask<Void, Void, Boolean>
{
    private final JmDNS jmDNS;

    private final ServiceInfo serviceInfo;

    private final JmDNSServiceListener listener;

    public JmDNSRegisterTask(JmDNS jmDNS, ServiceInfo serviceInfo, JmDNSServiceListener listener)
    {
        this.jmDNS = jmDNS;
        this.serviceInfo = serviceInfo;
        this.listener = listener;
    }

    @Override
    protected Boolean doInBackground(Void... voids)
    {
        try {
            jmDNS.registerService(serviceInfo);
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    protected void onPostExecute(Boolean registered)
    {
        if (registered) {
            listener.onServiceRegistered(serviceInfo);
        } else {
            listener.onServiceRegistrationFailed(serviceInfo);
        }
    }
}
