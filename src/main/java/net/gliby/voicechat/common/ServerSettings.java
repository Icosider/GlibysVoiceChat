package net.gliby.voicechat.common;

import net.gliby.voicechat.VoiceChat;

import java.io.File;
import java.io.UnsupportedEncodingException;

public class ServerSettings
{
    private ServerConfiguration configuration;
    private int soundDist = 64;
    private int udpPort = 0;
    private int bufferSize = 128;
    private int advancedNetworkType = 0;
    public int positionUpdateRate = 40;
    private int defaultChatMode = 0;
    private int minimumQuality = 0;
    private int maximumQuality = 9;
    private boolean canShowVoiceIcons = true;
    private boolean canShowVoicePlates = true;
    private boolean behindProxy;
    private int modPackID = 1;

    ServerSettings(VoiceChatServer voiceChatServer) {}

    public boolean canShowVoiceIcons()
    {
        return this.canShowVoiceIcons;
    }

    public final boolean canShowVoicePlates()
    {
        return this.canShowVoicePlates;
    }

    final int getAdvancedNetworkType()
    {
        return this.advancedNetworkType;
    }

    public final int getBufferSize()
    {
        return this.bufferSize;
    }

    public final int getDefaultChatMode()
    {
        return this.defaultChatMode;
    }

    public final int getMaximumSoundQuality()
    {
        return this.maximumQuality;
    }

    public final int getMinimumSoundQuality()
    {
        return this.minimumQuality;
    }

    int getModPackID()
    {
        return this.modPackID;
    }

    public final int getSoundDistance()
    {
        return this.soundDist;
    }

    public final int getUDPPort()
    {
        return this.udpPort;
    }

    public final boolean isUsingProxy()
    {
        return this.behindProxy;
    }

    void preInit(File file)
    {
        this.configuration = new ServerConfiguration(this, file);
        (new Thread(() -> ServerSettings.this.configuration.init(), "Configuration Process")).start();
        (new Thread(() -> {
            ModPackSettings settings = new ModPackSettings();

            try
            {
                ModPackSettings.GVCModPackInstructions e = settings.init();
                if(e.ID != ServerSettings.this.getModPackID()) {
                    VoiceChat.getLogger().info("Modpack defaults applied, original settings overwritten.");
                    ServerSettings.this.setCanShowVoicePlates(e.SHOW_PLATES);
                    ServerSettings.this.setCanShowVoiceIcons(e.SHOW_PLAYER_ICONS);
                    ServerSettings.this.setModPackID(e.ID);
                    ServerSettings.this.configuration.save();
                }
            }
            catch (UnsupportedEncodingException e)
            {
                e.printStackTrace();
            }
        }, "Mod Pack Overwrite Process")).start();
    }

    void setAdvancedNetworkType(int type)
    {
        this.advancedNetworkType = type;
    }

    void setBufferSize(int bufferSize)
    {
        this.bufferSize = bufferSize;
    }

    final void setCanShowVoiceIcons(boolean canShowVoiceIcons)
    {
        this.canShowVoiceIcons = canShowVoiceIcons;
    }

    void setCanShowVoicePlates(boolean canShowVoicePlates)
    {
        this.canShowVoicePlates = canShowVoicePlates;
    }

    void setDefaultChatMode(int defaultChatMode)
    {
        this.defaultChatMode = defaultChatMode;
    }

    void setModPackID(int id)
    {
        this.modPackID = id;
    }

    void setQuality(int x0, int x1)
    {
        this.minimumQuality = x0;
        this.maximumQuality = x1;
    }

    void setSoundDistance(int dist)
    {
        this.soundDist = dist;
    }

    void setUDPPort(int udp)
    {
        this.udpPort = udp;
    }

    void setUsingProxy(boolean val)
    {
        this.behindProxy = val;
    }
}