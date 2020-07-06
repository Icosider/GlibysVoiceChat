package net.gliby.voicechat.common.networking.voiceservers.udp;

import net.minecraft.entity.player.EntityPlayerMP;

import java.math.BigInteger;
import java.net.InetSocketAddress;

public class UDPClient {
    public EntityPlayerMP player;
    InetSocketAddress socketAddress;
    private final String hash;
    private final int key;

    UDPClient(EntityPlayerMP player, InetSocketAddress socketAddress, String hash) {
        this.player = player;
        this.socketAddress = socketAddress;
        this.hash = hash;
        this.key = (int) (new BigInteger(hash.replaceAll("[^0-9.]", ""))).longValue();
    }

    public String toString() {
        return "Client[" + this.socketAddress + ": " + this.key + ", " + this.player + "]";
    }
}