package com.github.teocci.android.pptopus.audio;

import android.media.AudioFormat;

public class Configuration
{
    static final int AUDIO_SAMPLE_RATE = 48000;
    static final int AUDIO_CHANNEL_IN = AudioFormat.CHANNEL_IN_MONO;
    static final int AUDIO_CHANNEL_OUT = AudioFormat.CHANNEL_OUT_MONO;
    static final int AUDIO_FORMAT = AudioFormat.ENCODING_PCM_16BIT;

    static final int KEEP_ALIVE = 25000; /* 25 seconds */

    static final int FRAME_SIZE = 2880;
    static final int BITRATE = 4 * (8 * 1024);
    static final int CHANNELS = 1;

    public static final boolean USE_FEC = false;
}