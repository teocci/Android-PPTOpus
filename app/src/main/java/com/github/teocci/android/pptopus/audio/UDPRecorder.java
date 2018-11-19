package com.github.teocci.android.pptopus.audio;

import android.content.Context;
import android.media.AudioRecord;
import android.media.MediaRecorder;

import com.github.teocci.android.pptopus.R;
import com.github.teocci.android.pptopus.audio.codecs.opus.NativeAudioException;
import com.github.teocci.android.pptopus.audio.codecs.opus.OpusEncoder;
import com.github.teocci.android.pptopus.utils.LogHelper;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.util.ArrayList;

public class UDPRecorder

{
    public static final String TAG = UDPRecorder.class.getSimpleName();

    private static volatile boolean _die;
    private static ArrayList<byte[]> terminationSound = new ArrayList<>();

    public static void encodeTerminationSound(Context context)
    {
        terminationSound.clear();

        InputStream is = null;
        OpusEncoder encoder = null;

        try {
            is = context.getResources().openRawResource(R.raw.over);

            encoder = new OpusEncoder(
                    Configuration.AUDIO_SAMPLE_RATE,
                    Configuration.CHANNELS,
                    Configuration.FRAME_SIZE,
                    Configuration.BITRATE
            );

            ByteArrayOutputStream buffer = new ByteArrayOutputStream(1024);
            byte[] data = new byte[1024];
            int nRead, i;

            while ((nRead = is.read(data, 0, data.length)) != -1) {
                buffer.write(data, 0, nRead);
            }

            buffer.flush();
            data = buffer.toByteArray();

            nRead = data.length / 2;
            short[] pcm = new short[nRead];
            for (i = 22; i < nRead; i++) {
                pcm[i] = (short) ((data[i * 2] & 0xff) | (data[i * 2 + 1] << 8));
            }

            nRead = pcm.length / Configuration.FRAME_SIZE;
            short[] pcm_frame_shorts = new short[Configuration.FRAME_SIZE];
            for (i = 0; i < nRead; i++) {
                System.arraycopy(pcm, i * Configuration.FRAME_SIZE, pcm_frame_shorts, 0, Configuration.FRAME_SIZE);

                if (encoder.encode(pcm_frame_shorts, Configuration.FRAME_SIZE) > 0) {
                    terminationSound.add(encoder.getEncodedData());
                }
            }

        } catch (NativeAudioException e) {
            terminationSound.clear();
            e.printStackTrace();
        } catch (IOException e) {
            terminationSound.clear();
            e.printStackTrace();
        } finally {
            if (is != null) {
                try {
                    is.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (encoder != null) {
                encoder.destroy();
            }
        }
    }

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
                    InetAddress target = UDPConnection.target;
                    int port = UDPConnection.port;
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

                                            data[2] = (byte) ((UDPPlayer.identCode >> 8) & 0xFF);
                                            data[3] = (byte) (UDPPlayer.identCode & 0xFF);

                                            System.arraycopy(opusAudioBytes, bytesSent, data, 64, byteQueue);

                                            UDPConnection.socket.send(new DatagramPacket(data, 0, data.length, target, port));

                                            LogHelper.e(TAG, "Sending: " + Integer.toString(data.length) + " bytes");

                                            bytesSent += byteQueue;
                                        }

                                    } catch (IOException e) {
                                        _die = true;
                                    }
                                    UDPConnection.setSent();
                                }
                            } catch (NativeAudioException e) {
                                e.printStackTrace();
                            }
                        }
                    }

                    /* Send *Over* */
//                    for (i = 0; i < terminationSound.size(); i++) {
//                        opus_audio_bytes = terminationSound.get(i);
//                        opus_audio_bytes_encoded = opus_audio_bytes.length;
//
//                        /* Send *terminationSound* buffer */
//                        try {
//                            bytes_sent = 0;
//                            while ((byte_queue = Math.min(444, opus_audio_bytes_encoded - bytes_sent)) > 0) {
//                                byte[] data = new byte[byte_queue + 64];
//
//                                data[0] = (byte) noOnce;
//                                noOnce = noOnce + 1 % 256;
//
//                                data[2] = (byte) ((WSAudioPlayer.identCode >> 8) & 0xFF);
//                                data[3] = (byte) (WSAudioPlayer.identCode & 0xFF);
//
//                                System.arraycopy(opus_audio_bytes, bytes_sent, data, 64, byte_queue);
//
//                                UDPConnection.server.send(new DatagramPacket(data, 0, data.length, target, port));
//
//                                LogHelper.e(TAG, "Sending (Over sound): " + Integer.toString(data.length) + " bytes");
//
//                                bytes_sent += byte_queue;
//                            }
//                        } catch (IOException e) {
//                            _die = true;
//                        }
//                    }

//                    UDPConnection.setSent();

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