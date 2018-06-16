package net.gliby.voicechat.common.networking.packets;

import io.netty.buffer.ByteBuf;
import net.gliby.voicechat.VoiceChat;
import net.gliby.voicechat.common.networking.MinecraftPacket;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class MinecraftServerVoiceEndPacket extends MinecraftPacket implements IMessageHandler<MinecraftServerVoiceEndPacket, MinecraftServerVoiceEndPacket>
{
    public void fromBytes(ByteBuf buf) {}
    public void toBytes(ByteBuf buf) {}

    public MinecraftServerVoiceEndPacket onMessage(MinecraftServerVoiceEndPacket packet, MessageContext ctx)
    {
        VoiceChat.getServerInstance().getVoiceServer().handleVoiceData(ctx.getServerHandler().playerEntity, null, (byte)0, ctx.getServerHandler().playerEntity.getEntityId(), true);
        return null;
    }
}