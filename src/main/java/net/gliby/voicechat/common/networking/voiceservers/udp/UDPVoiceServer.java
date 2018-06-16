package net.gliby.voicechat.common.networking.voiceservers.udp;

import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import net.gliby.voicechat.common.VoiceChatServer;
import net.gliby.voicechat.common.networking.ServerStreamManager;
import net.gliby.voicechat.common.networking.voiceservers.EnumVoiceNetworkType;
import net.gliby.voicechat.common.networking.voiceservers.VoiceAuthenticatedServer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.StringUtils;
import net.minecraftforge.fml.common.FMLCommonHandler;

import java.io.IOException;
import java.net.DatagramPacket;
import java.util.HashMap;
import java.util.Map;

public class UDPVoiceServer extends VoiceAuthenticatedServer
{
    public static volatile boolean running;
    private final VoiceChatServer voiceChat;
    private final ServerStreamManager manager;
    private UDPVoiceServerHandler handler;
    Map<Integer, UDPClient> clientMap;
    private UdpServer server;
    private ByteArrayDataOutput packetBuffer = ByteStreams.newDataOutput();

    public UDPVoiceServer(VoiceChatServer voiceChat)
    {
        this.voiceChat = voiceChat;
        this.manager = voiceChat.getServerNetwork().getDataManager();
    }

    public void closeConnection(int id)
    {
        UDPClient client = this.clientMap.get(id);

        if (client != null)
        {
            this.handler.closeConnection(client.socketAddress);
        }
        this.clientMap.remove(id);
    }

    public EnumVoiceNetworkType getType()
    {
        return EnumVoiceNetworkType.UDP;
    }

    public void handleVoiceData(EntityPlayerMP player, byte[] data, byte divider, int id, boolean end)
    {
        this.manager.addQueue(player, data, divider, id, end);
    }

    public void sendChunkVoiceData(EntityPlayerMP player, int entityID, boolean direct, byte[] samples, byte chunkSize, byte volume)
    {
        UDPClient client = this.clientMap.get(player.getEntityId());
        if (client != null)
        {
            this.sendPacket(new UDPServerChunkVoicePacket(samples, entityID, direct, chunkSize, volume), client);
        }
    }

    public void sendEntityPosition(EntityPlayerMP player, int entityID, double x, double y, double z)
    {
        UDPClient client = this.clientMap.get(player.getEntityId());

        if (client != null)
        {
            this.sendPacket(new UDPServerEntityPositionPacket(entityID, x, y, z), client);
        }
    }

    void sendPacket(UDPPacket packet, UDPClient client)
    {
        this.packetBuffer.writeByte(packet.id());
        packet.write(this.packetBuffer);
        byte[] data = this.packetBuffer.toByteArray();

        try
        {
            this.server.send(new DatagramPacket(data, data.length, client.socketAddress));
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }

        this.packetBuffer = ByteStreams.newDataOutput();
    }

    public void sendVoiceData(EntityPlayerMP player, int entityID, boolean global, byte[] samples, byte volume)
    {
        UDPClient client = this.clientMap.get(player.getEntityId());

        if (client != null)
        {
            this.sendPacket(new UDPServerVoicePacket(samples, entityID, global, volume), client);
        }
    }

    public void sendVoiceEnd(EntityPlayerMP player, int entityID)
    {
        UDPClient client = this.clientMap.get(player.getEntityId());

        if (client != null)
        {
            this.sendPacket(new UDPServerVoiceEndPacket(entityID), client);
        }
    }

    public boolean start()
    {
        this.clientMap = new HashMap<>();
        this.handler = new UDPVoiceServerHandler(this);
        MinecraftServer mc = FMLCommonHandler.instance().getMinecraftServerInstance();

        if (mc.isDedicatedServer())
        {
            if (StringUtils.isNullOrEmpty(mc.getServerHostname()))
            {
                this.server = new UdpServer(VoiceChatServer.getLogger(), this.voiceChat.getServerSettings().getUDPPort());
            }
            else {
                this.server = new UdpServer(VoiceChatServer.getLogger(), mc.getServerHostname(), this.voiceChat.getServerSettings().getUDPPort());
            }
        }
        else {
            this.server = new UdpServer(VoiceChatServer.getLogger(), this.voiceChat.getServerSettings().getUDPPort());
        }

        this.server.addUdpServerListener(evt -> {
            try
            {
                UDPVoiceServer.this.handler.read(evt.getPacketAsBytes(), evt.getPacket());
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }

        });
        this.server.start();
        return true;
    }

    public void stop()
    {
        running = false;
        this.handler.close();
        this.server.clearUdpListeners();
        this.server.stop();
        this.clientMap.clear();
        this.handler = null;
        this.server = null;
    }
}