package net.gliby.voicechat;

import net.gliby.voicechat.client.VoiceChatClient;
import net.gliby.voicechat.common.VoiceChatServer;
import net.gliby.voicechat.common.networking.packets.*;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.*;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import net.minecraftforge.fml.relauncher.Side;
import org.apache.logging.log4j.Logger;

@Mod(modid = "gvc", name = "Gliby's Voice Chat Mod", version = "1.2.0", updateJSON = "https://github.com/Ivasik78/GlibysVoiceChat/raw/master/gvc_updates.json")
public class VoiceChat {
    @Mod.Instance
    public static VoiceChat instance;

    @SidedProxy(clientSide = "net.gliby.voicechat.client.VoiceChatClient", serverSide = "net.gliby.voicechat.common.VoiceChatServer")
    public static VoiceChatServer proxy;
    public static SimpleNetworkWrapper DISPATCH;

    public static SimpleNetworkWrapper getDispatcher() {
        return DISPATCH;
    }

    public static VoiceChat getInstance() {
        return instance;
    }

    public static Logger getLogger() {
        return VoiceChatServer.getLogger();
    }

    public static VoiceChatClient getProxyInstance() {
        return (VoiceChatClient) (proxy instanceof VoiceChatClient ? (VoiceChatClient) proxy : proxy);
    }

    public static VoiceChatServer getServerInstance() {
        return proxy;
    }

    public static synchronized VoiceChatClient getSynchronizedProxyInstance() {
        return (VoiceChatClient) proxy;
    }

    @Mod.EventHandler
    public void init(FMLInitializationEvent event) {
        proxy.initMod(this, event);
    }

    @Mod.EventHandler
    public void initServer(FMLServerStartedEvent event) {
        proxy.initServer(event);
    }

    @Mod.EventHandler
    public void postInit(FMLPostInitializationEvent event) {
        proxy.postInitMod(this, event);
    }

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        this.registerNetwork();
        proxy.commonInit(event);
        proxy.preInitClient(event);
    }

    @Mod.EventHandler
    public void preInitServer(FMLServerStartingEvent event) {
        proxy.preInitServer(event);
    }

    @Mod.EventHandler
    public void aboutToStartServer(FMLServerAboutToStartEvent event) {
        proxy.aboutToStartServer(event);
    }

    private void registerNetwork() {
        DISPATCH = NetworkRegistry.INSTANCE.newSimpleChannel("GVC");
        DISPATCH.registerMessage(MinecraftServerVoicePacket.class, MinecraftServerVoicePacket.class, 1, Side.SERVER);
        DISPATCH.registerMessage(MinecraftServerVoiceEndPacket.class, MinecraftServerVoiceEndPacket.class, 2, Side.SERVER);
        DISPATCH.registerMessage(MinecraftClientVoiceEndPacket.class, MinecraftClientVoiceEndPacket.class, 9, Side.CLIENT);
        DISPATCH.registerMessage(MinecraftClientVoicePacket.class, MinecraftClientVoicePacket.class, 3, Side.CLIENT);
        DISPATCH.registerMessage(MinecraftClientEntityDataPacket.class, MinecraftClientEntityDataPacket.class, 4, Side.CLIENT);
        DISPATCH.registerMessage(MinecraftClientEntityPositionPacket.class, MinecraftClientEntityPositionPacket.class, 5, Side.CLIENT);
        DISPATCH.registerMessage(MinecraftClientVoiceServerPacket.class, MinecraftClientVoiceServerPacket.class, 6, Side.CLIENT);
        DISPATCH.registerMessage(MinecraftClientVoiceAuthenticatedServer.class, MinecraftClientVoiceAuthenticatedServer.class, 7, Side.CLIENT);
    }

    @Mod.EventHandler
    public void stopServer(FMLServerStoppedEvent event) {
        proxy.stop();
    }
}