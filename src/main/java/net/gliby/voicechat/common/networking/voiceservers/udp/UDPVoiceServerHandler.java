package net.gliby.voicechat.common.networking.voiceservers.udp;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteStreams;
import net.gliby.voicechat.VoiceChat;
import net.gliby.voicechat.common.MathUtility;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.fml.common.FMLCommonHandler;

import java.net.DatagramPacket;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class UDPVoiceServerHandler {
    private final ExecutorService threadService;
    private final Map<InetSocketAddress, UDPClient> clientNetworkMap;
    private final UDPVoiceServer server;

    UDPVoiceServerHandler(UDPVoiceServer server) {
        this.server = server;
        this.threadService = Executors.newFixedThreadPool((int) MathHelper.clamp((float) FMLCommonHandler.instance().getMinecraftServerInstance().getMaxPlayers(), 1.0F, 10.0F));
        this.clientNetworkMap = new HashMap<>();
    }

    public void close() {
        this.clientNetworkMap.clear();
        this.threadService.shutdown();
    }

    void closeConnection(InetSocketAddress address) {
        this.clientNetworkMap.remove(address);
    }

    private void handleAuthetication(InetSocketAddress address, DatagramPacket packet, ByteArrayDataInput in) {
        final String hash = new String(UDPByteUtilities.readBytes(in), StandardCharsets.UTF_8);
        final EntityPlayerMP player = this.server.waitingAuth.get(hash);

        if (player != null) {
            UDPClient client = new UDPClient(player, address, hash);
            this.clientNetworkMap.put(client.socketAddress, client);
            this.server.clientMap.put(player.getEntityId(), client);
            this.server.waitingAuth.remove(hash);
            VoiceChat.getLogger().info(client + " has been authenticated by server.");
            this.server.sendPacket(new UDPServerAuthenticationCompletePacket(), client);
        }
    }

    private void handleVoice(UDPClient client, ByteArrayDataInput in) {
        this.server.handleVoiceData(client.player, UDPByteUtilities.readBytes(in), in.readByte(), client.player.getEntityId(), false);
    }

    private void handleVoiceEnd(UDPClient client) {
        this.server.handleVoiceData(client.player, null, (byte) 0, client.player.getEntityId(), true);
    }

    public void read(byte[] data, final DatagramPacket packet) {
        final InetSocketAddress address = (InetSocketAddress) packet.getSocketAddress();
        final UDPClient client = this.clientNetworkMap.get(address);
        final ByteArrayDataInput in = ByteStreams.newDataInput(data);
        final byte id = in.readByte();

        this.threadService.execute(() -> {
            if (id == 0)
                UDPVoiceServerHandler.this.handleAuthetication(address, packet, in);

            if (client != null) {
                switch (id) {
                    case 1:
                        UDPVoiceServerHandler.this.handleVoice(client, in);
                        break;
                    case 2:
                        UDPVoiceServerHandler.this.handleVoiceEnd(client);
                }
            }
        });
    }
}