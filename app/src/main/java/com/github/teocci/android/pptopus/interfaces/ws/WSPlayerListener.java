package com.github.teocci.android.pptopus.interfaces.ws;

/**
 * Created by teocci.
 *
 * @author teocci@yandex.com on 2018-Nov-05
 */
public interface WSPlayerListener
{
    void onStop(String address);

    void onStatusChanged(String address, int status);
}
