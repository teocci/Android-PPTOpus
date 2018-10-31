package com.github.teocci.android.pptopus.audio;

import android.media.AudioFormat;

public class Configuration
{
    public static final int AUDIO_SAMPLE_RATE = 48000;
    public static final int AUDIO_CHANNEL_IN = AudioFormat.CHANNEL_IN_MONO;
    public static final int AUDIO_CHANNEL_OUT = AudioFormat.CHANNEL_OUT_MONO;
    public static final int AUDIO_FORMAT = AudioFormat.ENCODING_PCM_16BIT;

    public static final int KEEP_ALIVE = 25000; /* 25 seconds */

    public static final int FRAME_SIZE = 2880;
    public static final int BITRATE = 4 * (8 * 1024);
    public static final int CHANNELS = 1;

    public static final boolean USE_FEC = false;
}