package net.gliby.voicechat.client.networking.voiceclients;

import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import net.gliby.voicechat.VoiceChat;
import net.gliby.voicechat.client.VoiceChatClient;
import net.gliby.voicechat.client.sound.ClientStreamManager;
import net.gliby.voicechat.common.PlayerProxy;
import net.gliby.voicechat.common.networking.voiceservers.EnumVoiceNetworkType;
import net.gliby.voicechat.common.networking.voiceservers.udp.UDPClientAuthenticationPacket;
import net.gliby.voicechat.common.networking.voiceservers.udp.UDPClientVoiceEnd;
import net.gliby.voicechat.common.networking.voiceservers.udp.UDPClientVoicePacket;
import net.gliby.voicechat.common.networking.voiceservers.udp.UDPPacket;

import java.io.IOException;
import java.math.BigInteger;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.net.SocketException;

public class UDPVoiceClient extends VoiceAuthenticatedClient {
    public static volatile boolean running;
    private final int port;
    private final String host;
    private final int BUFFER_SIZE = 2048;
    private final ClientStreamManager soundManager;
    private UDPVoiceClientHandler handler;
    private DatagramSocket datagramSocket;
    private InetSocketAddress address;
    private int key;
    private ByteArrayDataOutput packetBuffer = ByteStreams.newDataOutput();

    public UDPVoiceClient(EnumVoiceNetworkType enumVoiceServer, String hash, String host, int udpPort) {
        super(enumVoiceServer, hash);
        this.port = udpPort;
        this.host = host;
        this.soundManager = VoiceChatClient.getSoundManager();
        this.key = (int)(new BigInteger(hash.replaceAll("[^0-9.]", ""))).longValue();
    }

    @Override
    public void autheticate() {
        this.sendPacket(new UDPClientAuthenticationPacket(super.hash));
    }

    void handleAuth() {
        VoiceChat.getLogger().info("Successfully authenticated with voice server, client functionical.");
        this.setAuthed(true);
    }

    @Override
    public void handleEnd(int id) {
        VoiceChatClient.getSoundManager().alertEnd(id);
    }

    @Override
    public void handleEntityPosition(int entityID, double x, double y, double z) {
        final PlayerProxy proxy = this.soundManager.playerData.get(entityID);

        if (proxy != null)
            proxy.setPosition(x, y, z);
    }

    @Override
    public void handlePacket(int entityID, byte[] data, int chunkSize, boolean direct, byte volume) {
        VoiceChatClient.getSoundManager().getSoundPreProcessor().process(entityID, data, chunkSize, direct, volume);
    }

    private void sendPacket(UDPPacket packet) {
        if (!this.datagramSocket.isClosed()) {
            this.packetBuffer.writeByte(packet.id());
            packet.write(this.packetBuffer);
            byte[] data = this.packetBuffer.toByteArray();

            try {
                this.datagramSocket.send(new DatagramPacket(data, data.length, this.address));
            } catch (IOException e) {
                e.printStackTrace();
            }
            this.packetBuffer = ByteStreams.newDataOutput();
        }
    }

    @Override
    public void sendVoiceData(byte divider, byte[] samples, boolean end) {
        if (super.authed) {
            if (end)
                this.sendPacket(new UDPClientVoiceEnd());
            else
                this.sendPacket(new UDPClientVoicePacket(divider, samples));
        }
    }

    @Override
    public void start() {
        running = true;
        this.address = new InetSocketAddress(this.host, this.port);

        try {
            this.datagramSocket = new DatagramSocket();
            this.datagramSocket.setSoTimeout(0);
            this.datagramSocket.connect(this.address);
            (new Thread(this.handler = new UDPVoiceClientHandler(this), "UDP Voice Client Process")).start();
        } catch (SocketException e) {
            running = false;
            e.printStackTrace();
        }

        VoiceChat.getLogger().info("Connected to UDP[" + this.host + ":" + this.port + "] voice server, requesting authentication.");
        this.autheticate();

        while (running) {
            byte[] packetBuffer = new byte[2048];
            DatagramPacket p = new DatagramPacket(packetBuffer, 2048);

            try {
                this.datagramSocket.receive(p);
                this.handler.packetQueue.offer(p.getData());

                synchronized (this.handler) {
                    this.handler.notify();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void stop() {
        running = false;

        if (this.datagramSocket != null)
            this.datagramSocket.close();
    }
}