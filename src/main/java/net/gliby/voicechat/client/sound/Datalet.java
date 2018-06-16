package net.gliby.voicechat.client.sound;

public class Datalet
{
    public final int id;
    public final byte[] data;
    final boolean direct;
    public final int volume;

    Datalet(boolean direct, int id, byte[] data, byte volume)
    {
        this.direct = direct;
        this.id = id;
        this.data = data;
        this.volume = volume;
    }
}