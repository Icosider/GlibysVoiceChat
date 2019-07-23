package net.gliby.voicechat.common;

import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import org.lwjgl.util.vector.Vector3f;

public class PlayerProxy {
    private EntityPlayer player;
    private double x;
    private double y;
    private double z;
    private String entityName;
    public boolean usesEntity;

    public PlayerProxy(EntityPlayer player, int entityID, String name, double x, double y, double z) {
        this.player = player;
        this.entityName = name;
        this.x = x;
        this.y = y;
        this.z = z;
        this.usesEntity = player != null;
    }

    public String entityName() {
        return this.entityName != null?this.entityName: String.valueOf(this.player.getDisplayName());
    }

    public Entity getPlayer() {
        return this.player;
    }

    public Vector3f position() {
        return this.player != null ? (this.usesEntity ? new Vector3f((float) this.player.posX, (float) this.player.posY, (float) this.player.posZ) : new Vector3f((float) this.x, (float) this.y, (float) this.z)) : new Vector3f((float) this.x, (float) this.y, (float) this.z);
    }

    public void setName(String name) {
        this.entityName = name;
    }

    public void setPlayer(EntityPlayer entity) {
        this.player = entity;
        this.usesEntity = true;
    }

    public void setPosition(double x, double y, double z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public String toString() {
        return "PlayerProxy[" + this.entityName + ": " + this.x + ", " + this.y + "," + this.z + "]";
    }

    public void update(WorldClient world) {
        if (world != null) {
            this.player = world.getPlayerEntityByName(this.entityName);
            this.usesEntity = this.player != null;
        }
    }
}