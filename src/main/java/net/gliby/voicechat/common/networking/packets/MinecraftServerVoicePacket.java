package net.gliby.voicechat.common.networking.packets;

import io.netty.buffer.ByteBuf;
import net.gliby.voicechat.VoiceChat;
import net.gliby.voicechat.common.networking.MinecraftPacket;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class MinecraftServerVoicePacket extends MinecraftPacket implements IMessageHandler<MinecraftServerVoicePacket, MinecraftServerVoicePacket> {
    private byte[] data;
    private byte divider;

    public MinecraftServerVoicePacket() {
    }

    public MinecraftServerVoicePacket(byte divider, byte[] data) {
        this.divider = divider;
        this.data = data;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        this.divider = buf.readByte();
        this.data = new byte[buf.readableBytes()];
        buf.readBytes(this.data);
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeByte(this.divider);
        buf.writeBytes(this.data);
    }

    @Override
    public MinecraftServerVoicePacket onMessage(MinecraftServerVoicePacket packet, MessageContext ctx) {
        final EntityPlayerMP player = ctx.getServerHandler().player;
        VoiceChat.getServerInstance().getVoiceServer().handleVoiceData(player, packet.data, packet.divider, player.getEntityId(), false);
        return null;
    }
}