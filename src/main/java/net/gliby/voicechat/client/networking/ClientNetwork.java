package net.gliby.voicechat.client.networking;

import net.gliby.voicechat.client.VoiceChatClient;
import net.gliby.voicechat.client.networking.voiceclients.MinecraftVoiceClient;
import net.gliby.voicechat.client.networking.voiceclients.UDPVoiceClient;
import net.gliby.voicechat.client.networking.voiceclients.VoiceAuthenticatedClient;
import net.gliby.voicechat.client.networking.voiceclients.VoiceClient;
import net.gliby.voicechat.common.PlayerProxy;
import net.gliby.voicechat.common.networking.voiceservers.EnumVoiceNetworkType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ServerAddress;
import net.minecraft.client.multiplayer.ServerData;

public class ClientNetwork {
    private final VoiceChatClient voiceChat;
    private VoiceClient voiceClient;
    private Thread voiceClientThread;
    public boolean connected;

    public ClientNetwork(VoiceChatClient voiceChatClient) {
        this.voiceChat = voiceChatClient;
        Runtime.getRuntime().addShutdownHook(new Thread(ClientNetwork.this::stopClientNetwork));
    }

    public VoiceClient getVoiceClient() {
        return this.voiceClient;
    }

    public void handleEntityData(int entityID, String name, double x, double y, double z) {
        PlayerProxy proxy = VoiceChatClient.getSoundManager().playerData.get(entityID);

        if (proxy != null) {
            proxy.setName(name);
            proxy.setPosition(x, y, z);
        } else {
            proxy = new PlayerProxy(null, entityID, name, x, y, z);
            VoiceChatClient.getSoundManager().playerData.put(entityID, proxy);
        }
    }

    public void handleVoiceAuthenticatedServer(boolean showVoicePlates, boolean showVoiceIcons, int minQuality, int maxQuality, int bufferSize, int soundDistance, int voiceServerType, int udpPort, String hash, String ip) {
        this.startClientNetwork(EnumVoiceNetworkType.values()[voiceServerType], hash, ip, udpPort, soundDistance, bufferSize, minQuality, maxQuality, showVoicePlates, showVoiceIcons);
    }

    public void handleVoiceServer(boolean canShowVoicePlates, boolean canShowVoiceIcons, int minQuality, int maxQuality, int bufferSize, int soundDistance, int voiceServerType) {
        this.startClientNetwork(EnumVoiceNetworkType.values()[voiceServerType], null, null, 0, soundDistance, bufferSize, minQuality, maxQuality, canShowVoicePlates, canShowVoiceIcons);
    }

    public final boolean isConnected() {
        return this.connected;
    }

    public void sendSamples(byte divider, byte[] samples, boolean end) {
        if (this.voiceClientExists()) {
            this.voiceClient.sendVoiceData(divider, samples, end);
        }
    }

    public VoiceClient startClientNetwork(EnumVoiceNetworkType type, String hash, String ip, int udpPort, int soundDist, int bufferSize, int soundQualityMin, int soundQualityMax, boolean showVoicePlates, boolean showVoiceIcons) {
        this.voiceChat.getSettings().resetQuality();
        if (this.connected) {
            this.stopClientNetwork();
        }

        VoiceChatClient.getSoundManager().reset();

        switch (type) {
            case MINECRAFT:
                this.voiceClient = new MinecraftVoiceClient(type);
                break;
            case UDP:
                String serverAddress = ip;
                if (ip.isEmpty()) {
                    ServerData serverData = Minecraft.getMinecraft().getCurrentServerData();
                    if (serverData != null) {
                        ServerAddress server = ServerAddress.fromString(serverData.serverIP);
                        serverAddress = server.getIP();
                    } else
                        serverAddress = "localhost";
                }
                this.voiceClient = new UDPVoiceClient(type, hash, serverAddress, udpPort);
                break;
        }

        this.voiceChat.getSettings().setBufferSize(bufferSize);
        this.voiceChat.getSettings().setNetworkQuality(soundQualityMin, soundQualityMax);
        this.voiceChat.getSettings().setSoundDistance(soundDist);
        this.voiceChat.getSettings().setVoiceIconsAllowed(showVoiceIcons);
        this.voiceChat.getSettings().setVoicePlatesAllowed(showVoicePlates);
        this.voiceClientThread = new Thread(this.voiceClient, "Voice Client");
        this.voiceClientThread.setDaemon(this.voiceClient instanceof VoiceAuthenticatedClient);
        this.voiceClientThread.start();
        this.connected = true;
        VoiceChatClient.getLogger().info("Connecting to [" + type.name + "] Server, settings[Buffer=" + bufferSize + ", MinQuality=" + soundQualityMin + ", MaxQuality=" + soundQualityMax + ", Distance=" + soundDist + ", Display Voice Icons: " + showVoiceIcons + ", Display Voice Plates: " + showVoicePlates + "]");
        return this.voiceClient;
    }

    public void stopClientNetwork() {
        this.connected = false;
        VoiceChatClient.getSoundManager().reset();

        if (this.voiceClient != null) {
            this.voiceClient.stop();
            VoiceChatClient.getLogger().info("Stopped Voice Client.");
        }

        if (this.voiceClientThread != null) {
            this.voiceClientThread.interrupt();
        }
        this.voiceClient = null;
        this.voiceClientThread = null;
    }

    public boolean voiceClientExists() {
        return this.voiceClient != null;
    }
}