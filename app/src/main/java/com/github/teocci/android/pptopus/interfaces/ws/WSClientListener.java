package com.github.teocci.android.pptopus.interfaces.ws;

/**
 * Created by teocci.
 *
 * @author teocci@yandex.com on 2018-Oct-30
 */
public interface WSClientListener
{
    void onAudioBuffer(byte[] data);

    void onClose();
}
