package net.gliby.voicechat.common;

import net.gliby.voicechat.VoiceChat;
import net.gliby.voicechat.common.api.VoiceChatAPI;
import net.gliby.voicechat.common.commands.CommandChatMode;
import net.gliby.voicechat.common.commands.CommandVoiceMute;
import net.gliby.voicechat.common.networking.ServerNetwork;
import net.gliby.voicechat.common.networking.voiceservers.MinecraftVoiceServer;
import net.gliby.voicechat.common.networking.voiceservers.ServerConnectionHandler;
import net.gliby.voicechat.common.networking.voiceservers.VoiceAuthenticatedServer;
import net.gliby.voicechat.common.networking.voiceservers.VoiceServer;
import net.gliby.voicechat.common.networking.voiceservers.udp.UDPVoiceServer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.dedicated.DedicatedServer;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.event.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.net.DatagramSocket;
import java.net.ServerSocket;
import java.util.Random;

public class VoiceChatServer {
    protected static final Logger LOGGER = LogManager.getLogger("Gliby's Voice Chat Mod");
    private VoiceServer voiceServer;
    private Thread voiceServerThread;
    public ServerNetwork serverNetwork;
    public ServerSettings serverSettings;
    private File configurationDirectory;

    private static boolean available(int port) {
        if (port >= 4000 && port <= '\uffff') {
            ServerSocket ss = null;
            DatagramSocket ds = null;

            try {
                ss = new ServerSocket(port);
                ss.setReuseAddress(true);
                ds = new DatagramSocket(port);
                ds.setReuseAddress(true);
                return true;
            } catch (IOException var13) {
                var13.printStackTrace();
            } finally {
                if (ds != null) {
                    ds.close();
                }

                if (ss != null) {
                    try {
                        ss.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
            return false;
        } else {
            throw new IllegalArgumentException("Invalid start port: " + port);
        }
    }

    public static synchronized Logger getLogger() {
        return LOGGER;
    }

    public static int randInt(int min, int max) {
        return new Random().nextInt(max - min + 1) + min;
    }

    public void commonInit(final FMLPreInitializationEvent event) {
        (new VoiceChatAPI()).init();
    }

    private int getAvailablePort() throws IOException {
        int port1;

        do {
            port1 = randInt(4001, '\ufffe');
        }
        while (!available(port1));
        return port1;
    }

    private int getNearestPort(int port) {
        ++port;
        return port;
    }

    public synchronized ServerNetwork getServerNetwork() {
        return this.serverNetwork;
    }

    public ServerSettings getServerSettings() {
        return this.serverSettings;
    }

    public String getVersion() {
        return "0.7.0";
    }

    public VoiceServer getVoiceServer() {
        return this.voiceServer;
    }

    public void initMod(VoiceChat voiceChat, FMLInitializationEvent event) {
    }

    public void initServer(FMLServerStartedEvent event) {
        MinecraftServer server = FMLCommonHandler.instance().getMinecraftServerInstance();

        if (this.serverSettings.getUDPPort() == 0) {
            if (server.isDedicatedServer()) {
                int e = -1;

                if (((DedicatedServer) server).getBooleanProperty("enable-query", false)) {
                    e = ((DedicatedServer) server).getIntProperty("query.port", 0);
                }

                boolean portTaken = e == server.getServerPort();
                this.serverSettings.setUDPPort(portTaken ? this.getNearestPort(((DedicatedServer) server).getPort()) : ((DedicatedServer) server).getPort());

                if (portTaken) {
                    getLogger().warn("Hey! Over Here! It seems you are running a query on the default port. We can't run a voice server on this port, so I've found a new one just for you! I'd recommend changing the UDPPort in your configuration, if the voice server can't bind!");
                }
            } else {
                try {
                    this.serverSettings.setUDPPort(this.getAvailablePort());
                } catch (IOException e) {
                    getLogger().fatal("Couldn't start voice server.");
                    e.printStackTrace();
                    return;
                }
            }
        }
        this.voiceServerThread = this.startVoiceServer();
    }

    public void postInitMod(VoiceChat voiceChat, FMLPostInitializationEvent event) {
    }

    public void preInitClient(FMLPreInitializationEvent event) {
    }

    public void preInitServer(FMLServerStartingEvent event) {
        event.registerServerCommand(new CommandVoiceMute());
        event.registerServerCommand(new CommandChatMode());
    }

    private Thread startVoiceServer() {
        this.serverNetwork = new ServerNetwork(this);
        this.serverNetwork.init();

        switch (this.serverSettings.getAdvancedNetworkType()) {
            case 1:
                this.voiceServer = new UDPVoiceServer(this);
                break;
            case 0:
            default:
                this.voiceServer = new MinecraftVoiceServer(this);
        }
        Thread thread = new Thread(this.voiceServer, "Voice Server Process");
        thread.setDaemon(this.voiceServer instanceof VoiceAuthenticatedServer);
        thread.start();
        return thread;
    }

    public void stop() {
        this.serverNetwork.stop();

        if (this.voiceServer instanceof VoiceAuthenticatedServer) {
            ((VoiceAuthenticatedServer) this.voiceServer).waitingAuth.clear();
        }
        this.voiceServer.stop();
        this.voiceServer = null;
        this.voiceServerThread.stop();
    }

    public void aboutToStartServer(FMLServerAboutToStartEvent e) {
        MinecraftForge.EVENT_BUS.register(new ServerConnectionHandler(this));
        this.serverSettings = new ServerSettings(this);
        this.configurationDirectory = new File("config/gliby_vc");

        if (!this.configurationDirectory.exists()) {
            this.configurationDirectory.mkdir();
        }
        this.serverSettings.preInit(new File(this.configurationDirectory, "ServerSettings.ini"));
    }
}