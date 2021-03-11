package net.gliby.voicechat.common.networking.packets;

import io.netty.buffer.ByteBuf;
import net.gliby.voicechat.VoiceChat;
import net.gliby.voicechat.client.networking.ClientNetwork;
import net.gliby.voicechat.common.networking.MinecraftPacket;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import ru.icosider.voicechat.AsyncCatcher;

public class MinecraftClientVoicePacket extends MinecraftPacket implements IMessageHandler<MinecraftClientVoicePacket, MinecraftClientVoicePacket> {
    private byte divider;
    private byte[] samples;
    private int entityID;
    private boolean direct;
    private byte volume;

    public MinecraftClientVoicePacket() {
    }

    public MinecraftClientVoicePacket(byte divider, byte[] samples, int entityID, boolean direct, byte volume) {
        this.divider = divider;
        this.samples = samples;
        this.entityID = entityID;
        this.direct = direct;
        this.volume = volume;
    }

    public void fromBytes(ByteBuf buf) {
        this.volume = buf.readByte();
        this.divider = buf.readByte();
        this.entityID = buf.readInt();
        this.direct = buf.readBoolean();
        this.samples = new byte[buf.readableBytes()];
        buf.readBytes(this.samples);
    }

    public void toBytes(ByteBuf buf) {
        buf.writeByte(this.volume);
        buf.writeByte(this.divider);
        buf.writeInt(this.entityID);
        buf.writeBoolean(this.direct);
        buf.writeBytes(this.samples);
    }

    public MinecraftClientVoicePacket onMessage(MinecraftClientVoicePacket packet, MessageContext ctx) {
        AsyncCatcher.INSTANCE.executeClient(() -> {
            final ClientNetwork network = VoiceChat.getProxyInstance().getClientNetwork();
            if (network.isConnected())
                network.getVoiceClient().handlePacket(packet.entityID, packet.samples, packet.divider, packet.direct, packet.volume);
        });
        return null;
    }
}