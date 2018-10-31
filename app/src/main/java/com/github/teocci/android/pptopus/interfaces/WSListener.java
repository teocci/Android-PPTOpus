package com.github.teocci.android.pptopus.interfaces;
/**
 * Created by teocci.
 *
 * @author teocci@yandex.com on 2018-Aug-20
 */
public interface WSListener
{
    void onOpen(String address);

    void onClose(String address);

    void onMessage(String address, String message);
}
