package com.github.teocci.android.pptopus.audio;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.media.AudioTrack;

import com.github.teocci.android.pptopus.audio.codecs.opus.NativeAudioException;
import com.github.teocci.android.pptopus.audio.codecs.opus.OpusDecoder;
import com.github.teocci.android.pptopus.utils.LogHelper;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.SocketTimeoutException;
import java.util.PriorityQueue;

public class UDPPlayer
{
    public static final String TAG = UDPPlayer.class.getSimpleName();

    private static volatile boolean _die;
    public static short identCode;

    private static byte[] keepAlivePacket;

    private static BroadcastReceiver receiver = new BroadcastReceiver()
    {

        @Override
        public void onReceive(Context context, Intent intent)
        {
            if (intent.getAction().equals("android.net.conn.CONNECTIVITY_CHANGE")) {
                if (UDPConnection.socket != null) {
                    new Thread(() -> {
                        try {
                            UDPConnection.socket.send(new DatagramPacket(keepAlivePacket, 4, UDPConnection.target, UDPConnection.port));
                            UDPConnection.setSent();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }).start();
                }
            }
        }

    };

    public static void start(Context context)
    {
        context.registerReceiver(receiver, new IntentFilter("android.net.conn.CONNECTIVITY_CHANGE"));

        identCode = (short) (Math.random() * 65535);

        keepAlivePacket = new byte[]{
                (byte) 100,
                (byte) 64,
                (byte) ((UDPPlayer.identCode >> 8) & 0xFF),
                (byte) (UDPPlayer.identCode & 0xFF)
        };

        _die = false;

        final int bufferSize = AudioTrack.getMinBufferSize(
                Configuration.AUDIO_SAMPLE_RATE,
                Configuration.AUDIO_CHANNEL_OUT,
                Configuration.AUDIO_FORMAT
        );

        final PriorityQueue<AudioPacket> queue = new PriorityQueue<>(10, new AudioPacketComparator());

        final int queueSize = 6; /* Wait (queueSize * (FRAME_SIZE / AUDIO_SAMPLE_RATE)) seconds */

        final Object lock = new Object();

        /* Receiving thread */
        new Thread(() -> {
            InetAddress target = UDPConnection.target;
            int targetPort = UDPConnection.port;

            byte[] buffer = new byte[512];
            DatagramPacket packet = new DatagramPacket(buffer, 512);

            int i;
            int len;
            byte[] totalData;
            byte[] data;

            while (!_die) {
                innerLoop:
                while (!_die) {
                    try {
                        if (UDPConnection.sent < System.currentTimeMillis()) {
                            UDPConnection.socket.send(new DatagramPacket(keepAlivePacket, 4, target, targetPort));
                            UDPConnection.setSent();
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    i = 0;
                    do {
                        LogHelper.e(TAG, "index " + i);
                        try {
                            UDPConnection.socket.receive(packet);
                            totalData = packet.getData();
                            len = packet.getLength() - 64;
                            if (len > -1) {
                                data = new byte[len];
                                System.arraycopy(totalData, 64, data, 0, len);

                                LogHelper.e(TAG, "Receiving: " + Integer.toString(data.length) + " bytes");

                                queue.add(new AudioPacket(totalData[0] & 0xff, ((totalData[2] & 0xFF) << 8 | totalData[3] & 0xFF), data));

                                if (i == queueSize) synchronized (lock) {
                                    lock.notify();
                                }
                            }
                        } catch (SocketTimeoutException e) {
                            break innerLoop;
                        } catch (IOException e) {
                            break innerLoop;
                        }
                    } while (++i < 500);
                }

                /* Give cpu time to rest */
                try {
                    Thread.sleep(200);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }).start();

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

                    if (queue.size() >= queueSize) {
                        try {
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

    public static void stop()
    {
        _die = true;
    }
}