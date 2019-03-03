package net.gliby.voicechat.common.networking.packets;

import io.netty.buffer.ByteBuf;
import net.gliby.voicechat.VoiceChat;
import net.gliby.voicechat.common.networking.MinecraftPacket;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class MinecraftServerVoiceEndPacket extends MinecraftPacket implements IMessageHandler<MinecraftServerVoiceEndPacket, MinecraftServerVoiceEndPacket> {
    @Override
    public void fromBytes(ByteBuf buf) {}
    @Override
    public void toBytes(ByteBuf buf) {}

    @Override
    public MinecraftServerVoiceEndPacket onMessage(MinecraftServerVoiceEndPacket packet, MessageContext ctx) {
        final EntityPlayerMP player = ctx.getServerHandler().player;
        VoiceChat.getServerInstance().getVoiceServer().handleVoiceData(player, null, (byte) 0, player.getEntityId(), true);
        return null;
    }
}