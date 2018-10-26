package com.github.teocci.android.pptopus.audio;

import java.util.Comparator;

public class AudioPacketComparator implements Comparator<AudioPacket>
{
    @Override
    public int compare(AudioPacket packetA, AudioPacket packetB)
    {
        if (packetA.num > 192 && packetB.num < 64) return (packetA.num < packetB.num) ? 1 : -1;
        if (packetA.num < 64 && packetB.num > 192) return (packetA.num < packetB.num) ? 1 : -1;

        return (packetA.num < packetB.num) ? -1 : 1;
    }
}
