package net.gliby.voicechat.common.networking.voiceservers.udp;

import com.google.common.io.ByteArrayDataOutput;

public class UDPClientVoicePacket extends UDPPacket
{
    private byte[] samples;
    private byte divider;

    public UDPClientVoicePacket(byte divider, byte[] samples)
    {
        this.samples = samples;
        this.divider = divider;
    }

    public byte id()
    {
        return (byte) 1;
    }

    public void write(ByteArrayDataOutput out)
    {
        UDPByteUtilities.writeBytes(this.samples, out);
        out.writeByte(this.divider);
    }
}