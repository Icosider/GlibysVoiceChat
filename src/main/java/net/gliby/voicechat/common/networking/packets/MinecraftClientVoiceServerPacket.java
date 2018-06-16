package net.gliby.voicechat.common.networking.packets;

import io.netty.buffer.ByteBuf;
import net.gliby.voicechat.VoiceChat;
import net.gliby.voicechat.common.networking.MinecraftPacket;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class MinecraftClientVoiceServerPacket extends MinecraftPacket implements IMessageHandler<MinecraftClientVoiceServerPacket, MinecraftClientVoiceServerPacket>
{
    private boolean showVoicePlates;
    private boolean showVoiceIcons;
    private int minQuality;
    private int maxQuality;
    private int bufferSize;
    private int soundDistance;
    private int voiceServerType;

    public MinecraftClientVoiceServerPacket() {}
    public MinecraftClientVoiceServerPacket(boolean canShowVoicePlates, boolean canShowVoiceIcons, int minQuality, int maxQuality, int bufferSize, int soundDistance, int voiceServerType)
    {
        this.showVoicePlates = canShowVoicePlates;
        this.showVoiceIcons = canShowVoiceIcons;
        this.minQuality = minQuality;
        this.maxQuality = maxQuality;
        this.bufferSize = bufferSize;
        this.soundDistance = soundDistance;
        this.voiceServerType = voiceServerType;
    }

    public void fromBytes(ByteBuf buf)
    {
        this.showVoicePlates = buf.readBoolean();
        this.showVoiceIcons = buf.readBoolean();
        this.minQuality = buf.readInt();
        this.maxQuality = buf.readInt();
        this.bufferSize = buf.readInt();
        this.soundDistance = buf.readInt();
        this.voiceServerType = buf.readInt();
    }

    public void toBytes(ByteBuf buf)
    {
        buf.writeBoolean(this.showVoicePlates);
        buf.writeBoolean(this.showVoiceIcons);
        buf.writeInt(this.minQuality);
        buf.writeInt(this.maxQuality);
        buf.writeInt(this.bufferSize);
        buf.writeInt(this.soundDistance);
        buf.writeInt(this.voiceServerType);
    }

    public MinecraftClientVoiceServerPacket onMessage(MinecraftClientVoiceServerPacket packet, MessageContext ctx)
    {
        VoiceChat.getProxyInstance().getClientNetwork().handleVoiceServer(packet.showVoicePlates, packet.showVoiceIcons, packet.minQuality, packet.maxQuality, packet.bufferSize, packet.soundDistance, packet.voiceServerType);
        return null;
    }
}