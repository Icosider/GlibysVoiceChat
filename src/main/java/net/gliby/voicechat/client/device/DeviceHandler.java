package net.gliby.voicechat.client.device;

import net.gliby.voicechat.VoiceChat;
import net.gliby.voicechat.client.sound.ClientStreamManager;

import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine.Info;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.Mixer;
import javax.sound.sampled.TargetDataLine;
import java.util.ArrayList;
import java.util.List;

public class DeviceHandler {
    private final List<Device> devices = new ArrayList<>();

    public Device getDefaultDevice() {
        Info info = new Info(TargetDataLine.class, ClientStreamManager.getUniversalAudioFormat());

        if (AudioSystem.isLineSupported(info)) {
            TargetDataLine line;

            try {
                line = (TargetDataLine) AudioSystem.getLine(info);
            } catch (Exception var4) {
                return null;
            }
            return line != null ? this.getDeviceByLine(line) : null;
        }
        return null;
    }

    private Device getDeviceByLine(TargetDataLine line) {
        return this.devices.stream().filter(device1 -> device1.getLine().getLineInfo().equals(line.getLineInfo())).findFirst().orElse(null);
    }

    public Device getDeviceByName(String deviceName) {
        return this.devices.stream().filter(device1 -> device1.getName().equals(deviceName)).findFirst().orElse(null);
    }

    public List<Device> getDevices() {
        return this.devices;
    }

    public boolean isEmpty() {
        return this.devices.isEmpty();
    }

    public List<Device> loadDevices() {
        this.devices.clear();
        final javax.sound.sampled.Mixer.Info[] mixers = AudioSystem.getMixerInfo();

        for (Mixer.Info info : mixers) {
            Mixer mixer = AudioSystem.getMixer(info);

            try {
                Info e = new Info(TargetDataLine.class, ClientStreamManager.getUniversalAudioFormat());
                TargetDataLine tdl = (TargetDataLine) mixer.getLine(e);

                if (info != null)
                    this.devices.add(new Device(tdl, info));
            } catch (LineUnavailableException | IllegalArgumentException e) {
                VoiceChat.getLogger().error("Line error: " + e.getMessage());
            }
        }
        return this.devices;
    }
}