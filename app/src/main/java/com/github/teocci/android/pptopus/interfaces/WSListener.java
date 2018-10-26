package com.github.teocci.android.pptopus.interfaces;

/**
 * Created by teocci.
 *
 * @author teocci@yandex.com on 2018-Aug-20
 */
public interface WSListener
{
    void onOpen();

    void onClose();

    void onMessage(String message);
}
