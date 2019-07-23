package net.gliby.voicechat.common.networking.voiceservers;

import net.minecraft.entity.player.EntityPlayerMP;

import java.util.HashMap;
import java.util.Map;

public abstract class VoiceAuthenticatedServer extends VoiceServer {
    public Map<String, EntityPlayerMP> waitingAuth = new HashMap<>();

    public abstract void closeConnection(int var1);
}