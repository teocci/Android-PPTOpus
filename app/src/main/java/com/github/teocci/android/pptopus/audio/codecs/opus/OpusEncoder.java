package com.github.teocci.android.pptopus.audio.codecs.opus;

import com.github.teocci.android.pptopus.audio.Configuration;

import org.bytedeco.javacpp.IntPointer;
import org.bytedeco.javacpp.Pointer;

import java.util.Arrays;

public class OpusEncoder
{
    private final int frameSize;
    private final byte[] encodeBuffer;

    private int encodedLength;

    private Pointer state;

    public OpusEncoder(int sampleRate, int channels, int frameSize, int bitrate) throws NativeAudioException
    {
        encodeBuffer = new byte[frameSize];
        this.frameSize = frameSize;

        IntPointer error = new IntPointer(1);
        error.put(0);

        state = Opus.opus_encoder_create(sampleRate, channels, Opus.OPUS_APPLICATION_VOIP, error);

        if (error.get() < 0)
            throw new NativeAudioException("Opus encoder initialization failed with error: " + error.get());

        if (Configuration.USE_FEC) {
            Opus.opus_encoder_ctl(state, Opus.OPUS_SET_INBAND_FEC_REQUEST, 1);
        }

        Opus.opus_encoder_ctl(state, Opus.OPUS_SET_VBR_REQUEST, 1);
        Opus.opus_encoder_ctl(state, Opus.OPUS_SET_BITRATE_REQUEST, bitrate);

    }

    public int encode(short[] input, int inputSize) throws NativeAudioException
    {
        if (inputSize < frameSize) {
            // If encoding is performed before frameSize is filled, fill rest of packet.
            short[] buffer = new short[frameSize];
            System.arraycopy(input, 0, buffer, 0, inputSize);
            Arrays.fill(buffer, inputSize, frameSize, (short) 0);
            input = buffer;
        }

        int result = Opus.opus_encode(state, input, frameSize, encodeBuffer, encodeBuffer.length);

        if (result < 0) {
            throw new NativeAudioException("Opus encoding failed with error: " + result);
        }

        return encodedLength = result;

    }

    public byte[] getEncodedData()
    {
        byte[] out = new byte[encodedLength];
        System.arraycopy(encodeBuffer, 0, out, 0, encodedLength);
        encodedLength = 0;

        return out;

    }

    public int getBitrate()
    {
        IntPointer ptr = new IntPointer(1);
        Opus.opus_encoder_ctl(state, Opus.OPUS_GET_BITRATE_REQUEST, ptr);

        return ptr.get();
    }

    public void destroy()
    {
        Opus.opus_encoder_destroy(state);
    }
}