package net.gliby.voicechat.common.networking.voiceservers.udp;

import com.google.common.io.ByteArrayDataOutput;

public class UDPClientVoiceEnd extends UDPPacket
{
    public byte id()
    {
        return (byte) 2;
    }

    public void write(ByteArrayDataOutput out) {}
}