package net.gliby.voicechat.common.networking.voiceservers;

import com.mojang.authlib.GameProfile;
import net.gliby.voicechat.VoiceChat;
import net.gliby.voicechat.common.VoiceChatServer;
import net.gliby.voicechat.common.networking.packets.MinecraftClientVoiceAuthenticatedServer;
import net.gliby.voicechat.common.networking.packets.MinecraftClientVoiceServerPacket;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.relauncher.Side;
import org.apache.commons.lang3.RandomStringUtils;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class ServerConnectionHandler
{
    private VoiceChatServer voiceChat;
    private List<GameProfile> loggedIn;

    public ServerConnectionHandler(VoiceChatServer vc)
    {
        this.voiceChat = vc;
        this.loggedIn = new ArrayList<>();
    }

    @SubscribeEvent
    public void onJoin(TickEvent.PlayerTickEvent event)
    {
        if (event.phase == TickEvent.Phase.END && event.side == Side.SERVER && !this.loggedIn.contains(event.player.getGameProfile()))
        {
            this.loggedIn.add(event.player.getGameProfile());
            this.onConnected(event.player);
        }
    }

    private void onConnected(EntityPlayer entity)
    {
        EntityPlayerMP player = (EntityPlayerMP)entity;

        if (this.voiceChat.getVoiceServer() instanceof VoiceAuthenticatedServer)
        {
            VoiceAuthenticatedServer voiceServer = (VoiceAuthenticatedServer)this.voiceChat.getVoiceServer();
            String hash = null;

            while (hash == null)
            {
                try
                {
                    hash = this.sha256(RandomStringUtils.random(32));
                }
                catch (NoSuchAlgorithmException e)
                {
                    e.printStackTrace();
                }
            }
            voiceServer.waitingAuth.put(hash, player);
            VoiceChat.getDispatcher().sendTo(new MinecraftClientVoiceAuthenticatedServer(this.voiceChat.getServerSettings().canShowVoicePlates(), this.voiceChat.getServerSettings().canShowVoiceIcons(), this.voiceChat.getServerSettings().getMinimumSoundQuality(), this.voiceChat.getServerSettings().getMaximumSoundQuality(), this.voiceChat.getServerSettings().getBufferSize(), this.voiceChat.getServerSettings().getSoundDistance(), this.voiceChat.getVoiceServer().getType().ordinal(), this.voiceChat.getServerSettings().getUDPPort(), hash, this.voiceChat.serverSettings.isUsingProxy()?this.voiceChat.serverNetwork.getAddress():""), player);
        }
        else {
            VoiceChat.getDispatcher().sendTo(new MinecraftClientVoiceServerPacket(this.voiceChat.getServerSettings().canShowVoicePlates(), this.voiceChat.getServerSettings().canShowVoiceIcons(), this.voiceChat.getServerSettings().getMinimumSoundQuality(), this.voiceChat.getServerSettings().getMaximumSoundQuality(), this.voiceChat.getServerSettings().getBufferSize(), this.voiceChat.getServerSettings().getSoundDistance(), this.voiceChat.getVoiceServer().getType().ordinal()), player);
        }
        this.voiceChat.serverNetwork.dataManager.entityHandler.connected(player);
    }

    @SubscribeEvent
    public void onDisconnect(PlayerEvent.PlayerLoggedOutEvent event)
    {
        if (FMLCommonHandler.instance().getEffectiveSide().isServer())
        {
            this.loggedIn.remove(event.player.getGameProfile());
            this.voiceChat.serverNetwork.dataManager.entityHandler.disconnected(event.player.getEntityId());
        }
    }

    private String sha256(String s) throws NoSuchAlgorithmException
    {
        MessageDigest md = MessageDigest.getInstance("SHA-256");
        byte[] hash = md.digest(s.getBytes());
        StringBuilder sb = new StringBuilder();

        for (byte aHash : Objects.requireNonNull(hash))
        {
            String hex = Integer.toHexString(aHash);

            if (hex.length() == 1)
            {
                sb.append(0);
                sb.append(hex.charAt(hex.length() - 1));
            }
            else {
                sb.append(hex.substring(hex.length() - 2));
            }
        }
        return sb.toString();
    }
}