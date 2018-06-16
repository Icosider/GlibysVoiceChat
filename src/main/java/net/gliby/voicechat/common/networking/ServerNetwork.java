package net.gliby.voicechat.common.networking;

import net.gliby.voicechat.VoiceChat;
import net.gliby.voicechat.common.VoiceChatServer;
import net.gliby.voicechat.common.networking.packets.MinecraftClientEntityDataPacket;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraftforge.fml.common.FMLCommonHandler;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.List;

public class ServerNetwork
{
    private final VoiceChatServer voiceChat;
    private String externalAddress;
    public final ServerStreamManager dataManager;

    public ServerNetwork(VoiceChatServer voiceChat)
    {
        this.voiceChat = voiceChat;
        this.dataManager = new ServerStreamManager(voiceChat);
    }

    public String getAddress()
    {
        return this.externalAddress;
    }

    public ServerStreamManager getDataManager()
    {
        return this.dataManager;
    }

    public String[] getPlayerIPs()
    {
        List players = FMLCommonHandler.instance().getMinecraftServerInstance().getEntityWorld().playerEntities;
        String[] ips = new String[players.size()];

        for (int i = 0; i < players.size(); ++i)
        {
            EntityPlayerMP p = (EntityPlayerMP)players.get(i);
            ips[i] = p.getPlayerIP();
        }
        return ips;
    }

    public EntityPlayerMP[] getPlayers()
    {
        List pl = FMLCommonHandler.instance().getMinecraftServerInstance().getEntityWorld().playerEntities;
        return (EntityPlayerMP[])pl.toArray(new EntityPlayerMP[0]);
    }

    public void init()
    {
        if (this.voiceChat.getServerSettings().isUsingProxy())
        {
            (new Thread(() -> ServerNetwork.this.externalAddress = ServerNetwork.this.retrieveExternalAddress(), "Extrernal Address Retriver Process")).start();
        }
        this.dataManager.init();
    }

    private String retrieveExternalAddress()
    {
        VoiceChat.getLogger().info("Retrieving server address.");
        BufferedReader in;

        try
        {
            URL e = new URL("http://checkip.amazonaws.com");
            in = new BufferedReader(new InputStreamReader(e.openStream()));
            return in.readLine();
        }
        catch (IOException e)
        {
            e.printStackTrace();
            return "0.0.0.0";
        }
    }

    void sendEntityData(EntityPlayerMP player, int entityID, String username, double x, double y, double z)
    {
        VoiceChat.getDispatcher().sendTo(new MinecraftClientEntityDataPacket(entityID, username, x, y, z), player);
    }

    public void stop()
    {
        this.dataManager.reset();
    }
}