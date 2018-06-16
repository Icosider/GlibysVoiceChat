package net.gliby.voicechat.common.networking;

import net.minecraft.entity.player.EntityPlayerMP;

import java.util.ArrayList;
import java.util.List;

public class ServerStream
{
    final int id;
    long lastUpdated;
    int tick;
    public EntityPlayerMP player;
    public List<Integer> entities;
    int chatMode;
    public boolean dirty;

    ServerStream(EntityPlayerMP player, int id, String identifier)
    {
        this.id = id;
        this.player = player;
        this.entities = new ArrayList<>();
        this.lastUpdated = System.currentTimeMillis();
    }

    final int getLastTimeUpdated()
    {
        return (int) (System.currentTimeMillis() - this.lastUpdated);
    }
}