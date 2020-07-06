package net.gliby.voicechat.client;

import net.gliby.gman.JINIFile;
import net.gliby.voicechat.VoiceChat;
import net.gliby.voicechat.client.device.Device;
import net.gliby.voicechat.client.device.DeviceHandler;
import net.gliby.voicechat.client.gui.EnumUIPlacement;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.io.File;
import java.io.IOException;

@SideOnly(Side.CLIENT)
public class Configuration {
    private final File location;
    private JINIFile init;
    private final Settings settings;

    Configuration(Settings settings, File file) {
        this.settings = settings;
        this.location = file;
    }

    void init(DeviceHandler deviceHandler) {
        if (!this.load(deviceHandler)) {
            VoiceChat.getLogger().info("No Configuration file found, will create one with default settings.");
            this.settings.setSetupNeeded(true);

            if (this.save()) {
                VoiceChat.getLogger().info("Created Configuration file with default settings.");
            }
        }
    }

    private boolean load(DeviceHandler handler) {
        try {
            if (this.location.exists()) {
                this.init = new JINIFile(this.location);
                this.settings.setVolumeControl(this.init.ReadBool("Game", "VolumeControl", true));
                Device e = handler.getDefaultDevice();

                if (e != null) {
                    this.settings.setInputDevice(handler.getDeviceByName(this.init.ReadString("Audio", "InputDevice", e.getName())));
                }
                this.settings.setWorldVolume(this.init.ReadFloat("Audio", "WorldVolume", 1.0F));
                this.settings.setInputBoost(this.init.ReadFloat("Audio", "InputBoost", 1.0F));
                this.settings.setSpeakMode(this.init.ReadFloat("Audio", "SpeakMode", 0.0F).intValue());
                this.settings.setEncodingQuality(this.init.ReadFloat("AdvancedAudio", "EncodingQuality", 1.0F));
                this.settings.setEncodingMode(this.init.ReadFloat("AdvancedAudio", "EncodingMode", 1.0F).intValue());
                this.settings.setPerceptualEnchantment(this.init.ReadBool("AdvancedAudio", "EnhancedDecoding", true));
                this.settings.setUIOpacity(this.init.ReadFloat("Interface", "UIOpacity", 1.0F));
                String[] positionArray = this.init.ReadString("Interface", "UIPositionSpeak", this.settings.getUIPositionSpeak().x + ":" + this.settings.getUIPositionSpeak().y + ":" + this.settings.getUIPositionSpeak().type + ":" + this.settings.getUIPositionSpeak().scale).split(":");
                this.settings.setUIPosition(EnumUIPlacement.SPEAK, Float.parseFloat(positionArray[0]), Float.parseFloat(positionArray[1]), Float.parseFloat(positionArray[3]), Integer.parseInt(positionArray[2]));
                positionArray = this.init.ReadString("Interface", "UIPositionPlate", this.settings.getUIPositionPlate().x + ":" + this.settings.getUIPositionPlate().y + ":" + this.settings.getUIPositionPlate().type + ":" + this.settings.getUIPositionPlate().scale).split(":");
                this.settings.setUIPosition(EnumUIPlacement.VOICE_PLATES, Float.parseFloat(positionArray[0]), Float.parseFloat(positionArray[1]), Float.parseFloat(positionArray[3]), Integer.parseInt(positionArray[2]));
                this.settings.setSnooperAllowed(this.init.ReadBool("Miscellaneous", "GlibysSnooper", false));
                this.settings.setModPackID(this.init.ReadInteger("Miscellaneous", "ModPackID", 1));
                this.settings.setDebug(this.init.ReadBool("Miscellaneous", "Debug", false));
                return true;
            }
            return false;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean save() {
        if (this.init == null || !this.location.exists()) {
            try {
                this.init = new JINIFile(this.location);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        this.init.WriteBool("Game", "VolumeControl", this.settings.isVolumeControlled());
        this.init.WriteString("Audio", "InputDevice", this.settings.getInputDevice() != null ? this.settings.getInputDevice().getName() : "none");
        this.init.WriteFloat("Audio", "WorldVolume", this.settings.getWorldVolume());
        this.init.WriteFloat("Audio", "InputBoost", this.settings.getInputBoost());
        this.init.WriteFloat("Audio", "SpeakMode", (float) this.settings.getSpeakMode());
        this.init.WriteFloat("AdvancedAudio", "EncodingQuality", this.settings.getEncodingQuality());
        this.init.WriteFloat("AdvancedAudio", "EncodingMode", (float) this.settings.getEncodingMode());
        this.init.WriteBool("AdvancedAudio", "EnhancedDecoding", this.settings.isPerceptualEnchantmentAllowed());
        this.init.WriteFloat("Interface", "UIOpacity", this.settings.getUIOpacity());
        this.init.WriteString("Interface", "UIPositionSpeak", this.settings.getUIPositionSpeak().x + ":" + this.settings.getUIPositionSpeak().y + ":" + this.settings.getUIPositionSpeak().type + ":" + this.settings.getUIPositionSpeak().scale);
        this.init.WriteString("Interface", "UIPositionPlate", this.settings.getUIPositionPlate().x + ":" + this.settings.getUIPositionPlate().y + ":" + this.settings.getUIPositionPlate().type + ":" + this.settings.getUIPositionPlate().scale);
        this.init.WriteString("Miscellaneous", "LastVersion", VoiceChat.getProxyInstance().getVersion());
        this.init.WriteBool("Miscellaneous", "GlibysSnooper", this.settings.isSnooperAllowed());
        this.init.WriteBool("Miscellaneous", "Debug", this.settings.isDebug());
        this.init.WriteInteger("Miscellaneous", "ModPackID", this.settings.getModPackID());
        return this.init.UpdateFile();
    }
}