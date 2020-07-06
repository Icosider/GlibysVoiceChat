package net.gliby.voicechat.common.networking.voiceservers.udp;

import com.google.common.io.ByteArrayDataOutput;

public class UDPServerEntityDataPacket extends UDPPacket {
    private final int entityId;
    public String name;
    public double x;
    public double y;
    public double z;

    public UDPServerEntityDataPacket(String name, int entityId, double x, double y, double z) {
        this.name = name;
        this.entityId = entityId;
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public byte id() {
        return (byte) 3;
    }

    public void write(ByteArrayDataOutput out) {
        out.writeInt(this.entityId);
        out.writeUTF(this.name);
        out.writeDouble(this.x);
        out.writeDouble(this.y);
        out.writeDouble(this.z);
    }
}