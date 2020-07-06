package net.gliby.voicechat.common.networking.voiceservers.udp;

import com.google.common.io.ByteArrayDataOutput;

import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;

public class UDPClientAuthenticationPacket extends UDPPacket {
    String hash;

    public UDPClientAuthenticationPacket(String hash) {
        this.hash = hash;
    }

    public byte id() {
        return (byte) 0;
    }

    public void write(ByteArrayDataOutput out) {
        UDPByteUtilities.writeBytes(this.hash.getBytes(StandardCharsets.UTF_8), out);
    }
}