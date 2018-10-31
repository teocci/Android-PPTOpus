package com.github.teocci.android.pptopus.net;

import android.media.AudioRecord;
import android.media.MediaRecorder;

import com.github.teocci.android.pptopus.audio.Configuration;
import com.github.teocci.android.pptopus.audio.codecs.opus.NativeAudioException;
import com.github.teocci.android.pptopus.audio.codecs.opus.OpusEncoder;
import com.github.teocci.android.pptopus.utils.LogHelper;

public class WSRecorder

{
    public static final String TAG = WSRecorder.class.getSimpleName();

    private static volatile boolean _die;

    public static void start()
    {
        _die = false;

        final int bufferSize = Configuration.FRAME_SIZE * (
                AudioRecord.getMinBufferSize(
                        Configuration.AUDIO_SAMPLE_RATE,
                        Configuration.AUDIO_CHANNEL_IN,
                        Configuration.AUDIO_FORMAT
                ) / Configuration.FRAME_SIZE + 1
        );

        final AudioRecord audioRecord = new AudioRecord(
                MediaRecorder.AudioSource.MIC,
                Configuration.AUDIO_SAMPLE_RATE,
                Configuration.AUDIO_CHANNEL_IN,
                Configuration.AUDIO_FORMAT,
                bufferSize
        );

        if (audioRecord.getState() == AudioRecord.STATE_INITIALIZED) {
            audioRecord.startRecording();
            new Thread(new Runnable()
            {
                OpusEncoder encoder;

                @Override
                public void run()
                {
                    if (encoder == null) {
                        try {
                            encoder = new OpusEncoder(
                                    Configuration.AUDIO_SAMPLE_RATE,
                                    Configuration.CHANNELS,
                                    Configuration.FRAME_SIZE,
                                    Configuration.BITRATE
                            );
                        } catch (NativeAudioException e) {
                            e.printStackTrace();
                            _die = true;
                        }
                    }

                    short[] pcmAudioShorts = new short[bufferSize];
                    short[] pcmFrameShorts = new short[Configuration.FRAME_SIZE];

                    byte[] opusAudioBytes;
                    int opusAudioBytesEncoded;

                    int i;

                    int noOnce = 0;

                    int bytesRead;
                    int byteQueue;
                    int bytesSent;

                    while (!_die) {
                        /* Fill audio buffer */
                        bytesRead = audioRecord.read(pcmAudioShorts, 0, bufferSize);

                        for (i = 0; i < bytesRead / Configuration.FRAME_SIZE; i++) {
                            System.arraycopy(pcmAudioShorts, i * Configuration.FRAME_SIZE, pcmFrameShorts, 0, Configuration.FRAME_SIZE);
                            try {
                                if ((opusAudioBytesEncoded = encoder.encode(pcmFrameShorts, Configuration.FRAME_SIZE)) > 0) {
                                    opusAudioBytes = encoder.getEncodedData();
                                    /* Send audio buffer */
                                    try {
                                        bytesSent = 0;
                                        while ((byteQueue = Math.min(444, opusAudioBytesEncoded - bytesSent)) > 0) {
                                            byte[] data = new byte[byteQueue + 64];

                                            data[0] = (byte) noOnce;
                                            noOnce = noOnce + 1 % 256;

                                            data[2] = (byte) ((WSConnection.identCode >> 8) & 0xFF);
                                            data[3] = (byte) (WSConnection.identCode & 0xFF);

                                            System.arraycopy(opusAudioBytes, bytesSent, data, 64, byteQueue);

                                            WSConnection.server.broadcast(data);

                                            LogHelper.e(TAG, "Sending: " + Integer.toString(data.length) + " bytes");

                                            bytesSent += byteQueue;
                                        }

                                    } catch (Exception e) {
                                        _die = true;
                                    }
                                    WSConnection.setSent();
                                }
                            } catch (NativeAudioException e) {
                                e.printStackTrace();
                            }
                        }
                    }

                    //encoder.destroy();

                    audioRecord.stop();
                    audioRecord.release();
                }
            }).start();
        }
    }

    public static void stop()
    {
        new Thread(() -> {
            try {
                Thread.sleep(120);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            _die = true;
        }).start();
    }
}