package net.gliby.voicechat.common.networking.packets;

import io.netty.buffer.ByteBuf;
import net.gliby.voicechat.VoiceChat;
import net.gliby.voicechat.client.networking.ClientNetwork;
import net.gliby.voicechat.common.networking.MinecraftPacket;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import ru.icosider.voicechat.AsyncCatcher;

public class MinecraftClientEntityDataPacket extends MinecraftPacket implements IMessageHandler<MinecraftClientEntityDataPacket, MinecraftClientEntityDataPacket> {
    private int entityID;
    private String username;
    private double x;
    private double y;
    private double z;

    public MinecraftClientEntityDataPacket() {
    }

    public MinecraftClientEntityDataPacket(int entityID, String username, double x, double y, double z) {
        this.entityID = entityID;
        this.username = username;
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public void fromBytes(ByteBuf buf) {
        this.entityID = buf.readInt();
        this.x = buf.readDouble();
        this.y = buf.readDouble();
        this.z = buf.readDouble();
        this.username = ByteBufUtils.readUTF8String(buf);
    }

    public void toBytes(ByteBuf buf) {
        buf.writeInt(this.entityID);
        buf.writeDouble(this.x);
        buf.writeDouble(this.y);
        buf.writeDouble(this.z);
        ByteBufUtils.writeUTF8String(buf, this.username);
    }

    public MinecraftClientEntityDataPacket onMessage(MinecraftClientEntityDataPacket packet, MessageContext ctx) {
        AsyncCatcher.INSTANCE.executeClient(() -> {
            final ClientNetwork network = VoiceChat.getProxyInstance().getClientNetwork();
            if (network.isConnected())
                network.handleEntityData(packet.entityID, packet.username, packet.x, packet.y, packet.z);
        });
        return null;
    }
}