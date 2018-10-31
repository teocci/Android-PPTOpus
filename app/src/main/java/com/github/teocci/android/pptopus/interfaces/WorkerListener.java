package com.github.teocci.android.pptopus.interfaces;

/**
 * Created by teocci.
 *
 * @author teocci@yandex.com on 2017-Jul-26
 */
public interface WorkerListener
{
    /**
     * After a UDPConnection Succeed
     */
    void onWorkerConnected();

    /**
     * When a Worker is disconnected
     */
    void onWorkerDisconnected();

    void onReceiveCommand(String str);

    /**
     * When a Worker has an error
     *
     * @param e Exception connection exception
     */
    void onWorkerError(Exception e);
}
