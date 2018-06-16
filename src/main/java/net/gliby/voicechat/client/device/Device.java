package net.gliby.voicechat.client.device;

import javax.sound.sampled.Mixer.Info;
import javax.sound.sampled.TargetDataLine;

public class Device
{
    private TargetDataLine line;
    private Info info;

    public Device(TargetDataLine line, Info info)
    {
        this.line = line;
        this.info = info;
    }

    public String getDescription()
    {
        return this.info.getDescription();
    }

    public String getIdentifer()
    {
        return this.info.getName();
    }

    public Info getInfo()
    {
        return this.info;
    }

    public TargetDataLine getLine()
    {
        return this.line;
    }

    public String getName()
    {
        return this.info.getName() != null ? this.info.getName() : "none";
    }

    public String getVendor()
    {
        return this.info.getVendor();
    }

    public String getVersion()
    {
        return this.info.getVersion();
    }

    public void setDevice(Device device)
    {
        this.line = device.line;
        this.info = device.info;
    }
}