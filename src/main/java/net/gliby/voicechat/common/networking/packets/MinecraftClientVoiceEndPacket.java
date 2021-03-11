package net.gliby.voicechat.common.networking.packets;

import io.netty.buffer.ByteBuf;
import net.gliby.voicechat.VoiceChat;
import net.gliby.voicechat.client.networking.ClientNetwork;
import net.gliby.voicechat.common.networking.MinecraftPacket;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import ru.icosider.voicechat.AsyncCatcher;

public class MinecraftClientVoiceEndPacket extends MinecraftPacket implements IMessageHandler<MinecraftClientVoiceEndPacket, MinecraftClientVoiceEndPacket> {
    private int entityID;

    public MinecraftClientVoiceEndPacket() {
    }

    public MinecraftClientVoiceEndPacket(int entityID) {
        this.entityID = entityID;
    }

    public void fromBytes(ByteBuf buf) {
        this.entityID = buf.readInt();
    }

    public void toBytes(ByteBuf buf) {
        buf.writeInt(this.entityID);
    }

    public MinecraftClientVoiceEndPacket onMessage(MinecraftClientVoiceEndPacket packet, MessageContext ctx) {
        AsyncCatcher.INSTANCE.executeClient(() -> {
            final ClientNetwork network = VoiceChat.getProxyInstance().getClientNetwork();
            if (network.isConnected())
                network.getVoiceClient().handleEnd(packet.entityID);
        });
        return null;
    }
}