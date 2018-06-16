package net.gliby.voicechat.common.networking;

import net.minecraft.entity.player.EntityPlayerMP;

public class ServerDatalet
{
    public final EntityPlayerMP player;
    public final int id;
    public final byte[] data;
    public boolean end;
    byte divider;
    public byte volume;

    ServerDatalet(EntityPlayerMP player, int id, byte[] data, byte divider, boolean end, byte volume)
    {
        this.player = player;
        this.id = id;
        this.data = data;
        this.end = end;
        this.divider = divider;
        this.volume = volume;
    }
}