package net.gliby.voicechat.common.networking.voiceservers.udp;

import com.google.common.io.ByteArrayDataOutput;

public class UDPServerVoicePacket extends UDPPacket
{
    public int entityID;
    public boolean direct;
    public byte[] data;
    public byte volume;

    UDPServerVoicePacket(byte[] data, int entityId, boolean global, byte volume)
    {
        this.data = data;
        this.entityID = entityId;
        this.direct = global;
        this.volume = volume;
    }

    public byte id()
    {
        return (byte) 1;
    }

    public void write(ByteArrayDataOutput in)
    {
        in.writeByte(this.volume);
        in.writeInt(this.entityID);
        in.writeBoolean(this.direct);
        UDPByteUtilities.writeBytes(this.data, in);
    }
}