package net.gliby.voicechat.client;

import net.gliby.voicechat.VoiceChat;
import net.gliby.voicechat.client.device.Device;
import net.gliby.voicechat.client.device.DeviceHandler;
import net.gliby.voicechat.client.gui.EnumUIPlacement;
import net.gliby.voicechat.client.gui.UIPosition;
import net.gliby.voicechat.common.MathUtility;
import net.gliby.voicechat.common.ModPackSettings;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.io.File;
import java.io.UnsupportedEncodingException;

@SideOnly(Side.CLIENT)
public class Settings
{
    private final DeviceHandler deviceHandler = new DeviceHandler();
    private boolean debugMode;
    private Device inputDevice;
    private float worldVolume = 1.0F;
    private float inputBoost = 0.0F;
    private float uiOpacity = 1.0F;
    private int speakMode = 0;
    private int encodingMode = 0;
    private int minimumQuality = 0;
    private int maximumQuality = 10;
    private float encodingQuality = 0.6F;
    private UIPosition uiPositionSpeak;
    private UIPosition uiPositionPlate;
    private boolean perceptualEnchantment = true;
    private boolean setupNeeded;
    private boolean snooperEnabled = false;
    private boolean volumeControl = true;
    private int maxSoundDistance = 63;
    private boolean voicePlatesAllowed = true;
    private boolean voiceIconsAllowed = true;
    private int bufferSize = 144;
    private int modPackId = 1;
    private Configuration configuration;

    Settings(File file)
    {
        this.configuration = new Configuration(this, file);
        this.uiPositionSpeak = new UIPosition(EnumUIPlacement.SPEAK, EnumUIPlacement.SPEAK.x, EnumUIPlacement.SPEAK.y, EnumUIPlacement.SPEAK.positionType, 1.0F);
        this.uiPositionPlate = new UIPosition(EnumUIPlacement.VOICE_PLATES, EnumUIPlacement.VOICE_PLATES.x, EnumUIPlacement.VOICE_PLATES.y, EnumUIPlacement.VOICE_PLATES.positionType, 1.0F);
    }

    public final int getBufferSize()
    {
        return this.bufferSize;
    }

    public Configuration getConfiguration()
    {
        return this.configuration;
    }

    public DeviceHandler getDeviceHandler()
    {
        return this.deviceHandler;
    }

    public int getEncodingMode()
    {
        return (int) MathUtility.clamp((float) this.encodingMode, 0.0F, 2.0F);
    }

    public String getEncodingModeString()
    {
        String s = "";

        switch (this.encodingMode)
        {
            case 0:
                s = "Narrowband";
                break;
            case 1:
                s = "Wideband";
                break;
            case 2:
                s = "Ultrawideband";
        }
        return s;
    }

    public final float getEncodingQuality()
    {
        return MathUtility.clamp(this.encodingQuality, 0.0F, 1.0F);
    }

    public final float getInputBoost()
    {
        return this.inputBoost;
    }

    public Device getInputDevice()
    {
        if (this.inputDevice == null)
        {
            this.inputDevice = this.deviceHandler.getDefaultDevice();
        }
        return this.inputDevice;
    }

    public final int getMaximumQuality()
    {
        return this.maximumQuality;
    }

    public final int getMaximumRenderableVoiceIcons()
    {
        return 20;
    }

    public final int getMinimumQuality()
    {
        return this.minimumQuality;
    }

    final int getModPackID()
    {
        return this.modPackId;
    }

    public final int getSoundDistance()
    {
        return this.maxSoundDistance;
    }

    public final int getSpeakMode()
    {
        return this.speakMode;
    }

    public float getUIOpacity()
    {
        return this.uiOpacity;
    }

    public final UIPosition getUIPositionPlate()
    {
        return this.uiPositionPlate;
    }

    public final UIPosition getUIPositionSpeak()
    {
        return this.uiPositionSpeak;
    }

    public float getWorldVolume()
    {
        return this.worldVolume;
    }

    public void init()
    {
        (new Thread(() -> {
            Settings.this.deviceHandler.loadDevices();
            Settings.this.configuration.init(Settings.this.deviceHandler);
            ModPackSettings settings = new ModPackSettings();

            try
            {
                ModPackSettings.GVCModPackInstructions e = settings.init();
                if (e.ID != Settings.this.getModPackID())
                {
                    VoiceChat.getLogger().info("Modpack defaults applied, original settings overwritten.");
                    Settings.this.uiPositionSpeak = new UIPosition(EnumUIPlacement.SPEAK, e.SPEAK_ICON.X, e.SPEAK_ICON.Y, e.SPEAK_ICON.TYPE, e.SPEAK_ICON.SCALE);
                    Settings.this.uiPositionPlate = new UIPosition(EnumUIPlacement.VOICE_PLATES, e.VOICE_PLATE.X, e.VOICE_PLATE.Y, e.VOICE_PLATE.TYPE, e.VOICE_PLATE.SCALE);
                    Settings.this.setWorldVolume(e.WORLD_VOLUME);
                    Settings.this.setUIOpacity(e.UI_OPACITY);
                    Settings.this.setVolumeControl(e.VOLUME_CONTROL);
                    Settings.this.setVoicePlatesAllowed(e.SHOW_PLATES);
                    Settings.this.setVoiceIconsAllowed(e.SHOW_PLAYER_ICONS);
                    Settings.this.setModPackID(e.ID);
                    Settings.this.configuration.save();
                }
            }
            catch (UnsupportedEncodingException e)
            {
                e.printStackTrace();
            }
        }, "Settings Process")).start();
    }

    public final boolean isDebug()
    {
        return this.debugMode;
    }

    public final boolean isPerceptualEnchantmentAllowed()
    {
        return this.perceptualEnchantment;
    }

    final boolean isSetupNeeded()
    {
        return this.setupNeeded;
    }

    public final boolean isSnooperAllowed()
    {
        return this.snooperEnabled;
    }

    public final boolean isVoiceIconAllowed()
    {
        return this.voiceIconsAllowed;
    }

    public final boolean isVoicePlateAllowed()
    {
        return this.voicePlatesAllowed;
    }

    public final boolean isVolumeControlled()
    {
        return this.volumeControl;
    }

    public void resetQuality()
    {
        this.minimumQuality = 0;
        this.maximumQuality = 10;
    }

    public void resetUI(int width, int height)
    {
        this.uiPositionSpeak.type = this.uiPositionSpeak.info.positionType;
        this.uiPositionSpeak.x = this.uiPositionSpeak.info.x;
        this.uiPositionSpeak.y = this.uiPositionSpeak.info.y;
        this.uiPositionSpeak.scale = 1.0F;
        this.uiPositionPlate.type = this.uiPositionPlate.info.positionType;
        this.uiPositionPlate.x = this.uiPositionPlate.info.x;
        this.uiPositionPlate.y = this.uiPositionPlate.info.y;
        this.uiPositionPlate.scale = 1.0F;
    }

    public void setBufferSize(int bufferSize)
    {
        this.bufferSize = bufferSize;
    }

    void setDebug(boolean debugMode)
    {
        this.debugMode = debugMode;
    }

    public void setEncodingMode(int encodingMode)
    {
        this.encodingMode = encodingMode;
    }

    public void setEncodingQuality(float encodingQuality)
    {
        this.encodingQuality = encodingQuality;
    }

    public void setInputBoost(float inputBoost)
    {
        this.inputBoost = inputBoost;
    }

    public void setInputDevice(Device loadedDevice)
    {
        this.inputDevice = loadedDevice;
    }

    void setModPackID(int modPackId)
    {
        this.modPackId = modPackId;
    }

    public void setNetworkQuality(int soundQualityMin, int soundQualityMax)
    {
        this.minimumQuality = soundQualityMin;
        this.maximumQuality = soundQualityMax;
    }

    public void setPerceptualEnchantment(boolean perceptualEnchantment)
    {
        this.perceptualEnchantment = perceptualEnchantment;
    }

    public void setSetupNeeded(boolean setupNeeded)
    {
        this.setupNeeded = setupNeeded;
    }

    public void setSnooperAllowed(boolean b)
    {
        this.snooperEnabled = b;
    }

    public void setSoundDistance(int soundDist)
    {
        this.maxSoundDistance = soundDist;
    }

    public void setSpeakMode(int speakMode)
    {
        this.speakMode = speakMode;
    }

    public void setUIOpacity(float chatIconOpacity)
    {
        this.uiOpacity = chatIconOpacity;
    }

    void setUIPosition(EnumUIPlacement placement, float x, float y, float scale, int type)
    {
        if (placement == EnumUIPlacement.SPEAK)
        {
            this.uiPositionSpeak = new UIPosition(placement, x, y, type, scale);
        }

        if (placement == EnumUIPlacement.VOICE_PLATES)
        {
            this.uiPositionPlate = new UIPosition(placement, x, y, type, scale);
        }
    }

    public final void setVoiceIconsAllowed(boolean voiceIconsAllowed)
    {
        this.voiceIconsAllowed = voiceIconsAllowed;
    }

    public final void setVoicePlatesAllowed(boolean voicePlatesAllowed)
    {
        this.voicePlatesAllowed = voicePlatesAllowed;
    }

    public void setVolumeControl(boolean volumeControl)
    {
        this.volumeControl = volumeControl;
    }

    public void setWorldVolume(float worldVolume)
    {
        this.worldVolume = worldVolume;
    }
}