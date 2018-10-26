package com.github.teocci.android.pptopus.audio;

/**
 * Created by Jasper on 18-2-2017.
 */

public class AudioPacket
{
    public AudioPacket(int num, int ident, byte[] data)
    {
        this.num = num;
        this.ident = ident;
        this.data = data;
    }

    public byte[] data;
    public int num;
    public int ident;
}
