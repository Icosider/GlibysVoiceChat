package net.gliby.voicechat.common.networking.voiceservers.udp;

import com.google.common.io.ByteArrayDataOutput;

public class UDPServerVoiceEndPacket extends UDPPacket
{
    private int entityID;

    UDPServerVoiceEndPacket(int entityID)
    {
        this.entityID = entityID;
    }

    public byte id()
    {
        return (byte) 2;
    }

    public void write(ByteArrayDataOutput out)
    {
        out.writeInt(this.entityID);
    }
}