package net.gliby.voicechat.client;

import net.gliby.voicechat.VoiceChat;
import net.gliby.voicechat.client.debug.Statistics;
import net.gliby.voicechat.client.gui.GuiInGameHandlerVoiceChat;
import net.gliby.voicechat.client.gui.options.GuiScreenOptionsWizard;
import net.gliby.voicechat.client.keybindings.KeyManager;
import net.gliby.voicechat.client.keybindings.KeyTickHandler;
import net.gliby.voicechat.client.networking.ClientNetwork;
import net.gliby.voicechat.client.networking.game.ClientDisconnectHandler;
import net.gliby.voicechat.client.networking.game.ClientEventHandler;
import net.gliby.voicechat.client.render.RenderPlayerVoiceIcon;
import net.gliby.voicechat.client.sound.ClientStreamManager;
import net.gliby.voicechat.client.sound.Recorder;
import net.gliby.voicechat.common.VoiceChatServer;
import net.minecraft.client.Minecraft;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.ModMetadata;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class VoiceChatClient extends VoiceChatServer {
    @SideOnly(Side.CLIENT)
    private static ClientStreamManager soundManager;

    @SideOnly(Side.CLIENT)
    private static Statistics stats;

    private static ModMetadata modMetadata;

    @SideOnly(Side.CLIENT)
    private Settings settings;

    @SideOnly(Side.CLIENT)
    public KeyManager keyManager;

    @SideOnly(Side.CLIENT)
    private ClientNetwork clientNetwork;

    @SideOnly(Side.CLIENT)
    private boolean recorderActive;

    public Recorder recorder;
    public Map<String, Integer> specialPlayers = new HashMap<>();
    private final String[] testPlayers = new String[]{"captaindogfish", "starguy1245", "SheheryaB", "arsham123", "Chris9awesome", "TechnoX_X", "bubz052", "McJackson3180", "InfamousArgyle", "jdf2", "XxNotexX0", "SirDenerim", "Frankspark", "smith70831", "killazombiecow", "CraftAeternalis", "choclaterainxx", "dragonballkid4", "TH3_CR33PER", "yetshadow", "KristinnVikarJ", "TheMCBros99", "kevinlame"};
    private boolean init;

    public static synchronized Logger getLogger() {
        return VoiceChatServer.LOGGER;
    }

    public static ModMetadata getModMetadata() {
        return modMetadata;
    }

    public static ClientStreamManager getSoundManager() {
        return soundManager;
    }

    public static Statistics getStatistics() {
        return stats;
    }

    public ClientNetwork getClientNetwork() {
        return this.clientNetwork;
    }

    public Settings getSettings() {
        return this.settings;
    }

    public String[] getTestPlayers() {
        return this.testPlayers;
    }

    @SubscribeEvent
    @SideOnly(Side.CLIENT)
    public void onJoin(TickEvent.PlayerTickEvent event) {
        if (event.phase == TickEvent.Phase.END && event.side == Side.CLIENT && !this.init) {
            Minecraft mc = Minecraft.getMinecraft();
            VoiceChatClient voiceChat = VoiceChat.getProxyInstance();

            if (voiceChat.getSettings().isSetupNeeded()) {
                mc.displayGuiScreen(new GuiScreenOptionsWizard(voiceChat, null));
            }
            this.init = true;
        }
    }

    public void initMod(VoiceChat voiceChat, FMLInitializationEvent event) {
        Minecraft mc = Minecraft.getMinecraft();
        this.recorder = new Recorder(this);
        this.keyManager.init();

        if (this.settings.isDebug()) {
            getLogger().info("Debug enabled!");
            stats = new Statistics();
        }

        getLogger().info("Started client-side on version (" + this.getVersion() + ")" + "");
        this.clientNetwork = new ClientNetwork(this);
        MinecraftForge.EVENT_BUS.register(new GuiInGameHandlerVoiceChat(this));
        MinecraftForge.EVENT_BUS.register(new RenderPlayerVoiceIcon(this, mc));
        MinecraftForge.EVENT_BUS.register(new ClientEventHandler(this));
        MinecraftForge.EVENT_BUS.register(new ClientDisconnectHandler());
        MinecraftForge.EVENT_BUS.register(new KeyTickHandler(this));
        MinecraftForge.EVENT_BUS.register(this);
    }

    public void preInitClient(FMLPreInitializationEvent event) {
        new UpdatedSoundManager();
        modMetadata = event.getModMetadata();
        File configurationDirectory = new File(event.getModConfigurationDirectory(), "gliby_vc");

        if (!configurationDirectory.exists()) {
            configurationDirectory.mkdir();
        }
        this.settings = new Settings(new File(configurationDirectory, "ClientSettings.ini"));
        this.settings.init();
        this.keyManager = new KeyManager(this);
        this.specialPlayers.put("theGliby", 1);
        this.specialPlayers.put("Rinto", 1);
        this.specialPlayers.put("DanielSturk", 1);
        this.specialPlayers.put("CraftAeternalis", 3);
        this.specialPlayers.put("YETSHADOW", 5);
        this.specialPlayers.put("McJackson3180", 6);
        this.specialPlayers.put("smith70831", 7);
        this.specialPlayers.put("XxNotexX0", 8);
        this.specialPlayers.put("TheHaxman2", 9);
        soundManager = new ClientStreamManager(Minecraft.getMinecraft(), this);
        soundManager.init();
    }

    public final boolean isRecorderActive() {
        return this.recorderActive;
    }

    public void setRecorderActive(boolean b) {
        if (this.clientNetwork.voiceClientExists()) {
            this.recorderActive = b;
        }
    }
}