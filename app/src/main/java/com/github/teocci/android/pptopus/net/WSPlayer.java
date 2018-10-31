package com.github.teocci.android.pptopus.net;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.media.AudioTrack;

import com.github.teocci.android.pptopus.audio.AudioPacket;
import com.github.teocci.android.pptopus.audio.AudioPacketComparator;
import com.github.teocci.android.pptopus.audio.Configuration;
import com.github.teocci.android.pptopus.audio.codecs.opus.NativeAudioException;
import com.github.teocci.android.pptopus.audio.codecs.opus.OpusDecoder;
import com.github.teocci.android.pptopus.interfaces.WSClientListener;
import com.github.teocci.android.pptopus.utils.LogHelper;

import java.net.URI;
import java.util.concurrent.PriorityBlockingQueue;

public class WSPlayer
{
    public final String TAG = WSPlayer.class.getSimpleName();

    private volatile boolean _die;

    private WSClient wsClient;

    private int i = 0;
    private int len;
    private byte[] data;

    private final PriorityBlockingQueue<AudioPacket> queue = new PriorityBlockingQueue<>(10, new AudioPacketComparator());

    private final int queueSize = 6; /* Wait (queueSize * (FRAME_SIZE / AUDIO_SAMPLE_RATE)) seconds */

    private final Object lock = new Object();

    private BroadcastReceiver receiver = new BroadcastReceiver()
    {

        @Override
        public void onReceive(Context context, Intent intent)
        {
            if (intent.getAction().equals("android.net.conn.CONNECTIVITY_CHANGE")) {
                if (wsClient != null) {
                    new Thread(() -> {
                        try {
                            wsClient.sendPing();
                            WSConnection.setSent();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }).start();
                }
            }
        }

    };

    private WSClientListener clientListener = new WSClientListener()
    {
        @Override
        public void onAudioBuffer(byte[] totalData)
        {
            try {
                if (WSConnection.sent < System.currentTimeMillis()) {
                    if (wsClient != null && !wsClient.isClosed()) {
                        wsClient.sendPing();
                        WSConnection.setSent();
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            len = totalData.length - 64;
            if (len > -1) {
                data = new byte[len];
                System.arraycopy(totalData, 64, data, 0, len);

                LogHelper.e(TAG, "Receiving: " + Integer.toString(data.length) + " bytes");
                queue.add(new AudioPacket(totalData[0] & 0xff, ((totalData[2] & 0xFF) << 8 | totalData[3] & 0xFF), data));

                if (i == queueSize) synchronized (lock) {
                    lock.notify();
                }
            }
            if (i++ > 500) i = 0;
        }

        @Override
        public void onClose()
        {
            stop();
        }
    };

    public void start(Context context, URI uri)
    {
        wsClient = new WSClient(uri, clientListener);
        wsClient.connect();

        context.registerReceiver(receiver, new IntentFilter("android.net.conn.CONNECTIVITY_CHANGE"));

        WSConnection.setIdent((short) (Math.random() * 65535));

        _die = false;

        final int bufferSize = AudioTrack.getMinBufferSize(
                Configuration.AUDIO_SAMPLE_RATE,
                Configuration.AUDIO_CHANNEL_OUT,
                Configuration.AUDIO_FORMAT
        );

        /* Playback thread */
        new Thread(() -> {
            try {
                OpusDecoder decoder = new OpusDecoder(Configuration.AUDIO_SAMPLE_RATE, Configuration.CHANNELS);

                AudioTrack track = new AudioTrack(
                        AudioManager.STREAM_MUSIC,
                        Configuration.AUDIO_SAMPLE_RATE,
                        Configuration.AUDIO_CHANNEL_OUT,
                        Configuration.AUDIO_FORMAT,
                        bufferSize,
                        AudioTrack.MODE_STREAM
                );

                AudioPacket handlePack;

                short[] pcmOut = new short[Configuration.FRAME_SIZE];
                int pcmOutLength;

                while (!_die) {
                    try {
                        synchronized (lock) {
                            lock.wait(200);
                        }
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                    if (queue.size() >= queueSize) synchronized (lock) {
                        try {
                            LogHelper.e(TAG, "decoding D: (queue.size , queueSize) -> (" + queue.size() + ", " + queueSize + ')');
                            handlePack = queue.remove();
                            pcmOutLength = decoder.decodeShort(handlePack.data, handlePack.data.length, pcmOut, Configuration.FRAME_SIZE);
                            track.write(pcmOut, 0, pcmOutLength);
                            LogHelper.e(TAG, Integer.toString(handlePack.ident));
                            track.play();
                        } catch (NativeAudioException e) {
                            e.printStackTrace();
                            _die = true;
                        }

                        if (!_die) do {
                            try {
                                LogHelper.e(TAG, "decoding G: (queue.size , queueSize) -> (" + queue.size() + ", " + queueSize + ')');
                                handlePack = queue.remove();
                                pcmOutLength = decoder.decodeShort(handlePack.data, handlePack.data.length, pcmOut, Configuration.FRAME_SIZE);
                                track.write(pcmOut, 0, pcmOutLength);
                                LogHelper.e(TAG, Integer.toString(handlePack.ident));
                            } catch (NativeAudioException e) {
                                e.printStackTrace();
                                _die = true;
                                break;
                            }
                        } while (!queue.isEmpty());

                        track.write(new short[bufferSize], 0, bufferSize);
                        track.stop();
                        track.flush();
                    }
                }

                track.stop();
                track.flush();
                track.release();

                decoder.destroy();
            } catch (NativeAudioException e) {
                e.printStackTrace();
                _die = true;
            }
        }).start();
    }

    public void stop()
    {
        _die = true;
    }

    public WSClient getWSClient()
    {
        return wsClient;
    }

    public boolean isPlaying()
    {
        return !hasStopped();
    }

    public boolean hasStopped()
    {
        return _die;
    }
}