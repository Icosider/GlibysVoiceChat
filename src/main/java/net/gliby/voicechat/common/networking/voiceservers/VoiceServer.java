package net.gliby.voicechat.common.networking.voiceservers;

import net.gliby.voicechat.VoiceChat;
import net.minecraft.entity.player.EntityPlayerMP;

public abstract class VoiceServer implements Runnable
{
    public abstract EnumVoiceNetworkType getType();

    public abstract void handleVoiceData(EntityPlayerMP var1, byte[] var2, byte var3, int var4, boolean var5);

    public final void run()
    {
        VoiceChat.getLogger().info(this.start()?"Started [" + this.getType().name + "] Server.":"Failed to start [" + this.getType().name + "] Server.");
    }

    public abstract void sendChunkVoiceData(EntityPlayerMP var1, int var2, boolean var3, byte[] var4, byte var5, byte var6);

    public abstract void sendEntityPosition(EntityPlayerMP var1, int var2, double var3, double var5, double var7);

    public abstract void sendVoiceData(EntityPlayerMP var1, int var2, boolean var3, byte[] var4, byte var5);

    public abstract void sendVoiceEnd(EntityPlayerMP var1, int var2);

    public abstract boolean start();

    public abstract void stop();
}