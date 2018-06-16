package net.gliby.voicechat.common.networking.voiceservers.udp;

import com.google.common.io.ByteArrayDataOutput;

public class UDPServerChunkVoicePacket extends UDPPacket
{
    private byte[] data;
    private boolean direct;
    private byte chunkSize;
    private int entityId;
    private byte volume;

    UDPServerChunkVoicePacket(byte[] samples, int entityID, boolean direct, byte chunkSize, byte volume)
    {
        this.data = samples;
        this.entityId = entityID;
        this.direct = direct;
        this.chunkSize = chunkSize;
        this.volume = volume;
    }

    public byte id()
    {
        return (byte) 5;
    }

    public void write(ByteArrayDataOutput out)
    {
        out.writeByte(this.volume);
        out.writeInt(this.entityId);
        out.writeByte(this.chunkSize);
        out.writeBoolean(this.direct);
        UDPByteUtilities.writeBytes(this.data, out);
    }
}