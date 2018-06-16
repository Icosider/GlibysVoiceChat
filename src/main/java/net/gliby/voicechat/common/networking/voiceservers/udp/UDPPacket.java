package net.gliby.voicechat.common.networking.voiceservers.udp;

import com.google.common.io.ByteArrayDataOutput;

public abstract class UDPPacket
{
    public abstract byte id();
    public abstract void write(ByteArrayDataOutput var1);
}