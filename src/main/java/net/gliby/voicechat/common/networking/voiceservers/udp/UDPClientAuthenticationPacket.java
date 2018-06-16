package net.gliby.voicechat.common.networking.voiceservers.udp;

import com.google.common.io.ByteArrayDataOutput;

import java.io.UnsupportedEncodingException;

public class UDPClientAuthenticationPacket extends UDPPacket
{
    String hash;

    public UDPClientAuthenticationPacket(String hash)
    {
        this.hash = hash;
    }

    public byte id()
    {
        return (byte) 0;
    }

    public void write(ByteArrayDataOutput out)
    {
        try
        {
            UDPByteUtilities.writeBytes(this.hash.getBytes("UTF-8"), out);
        }
        catch (UnsupportedEncodingException e)
        {
            e.printStackTrace();
        }
    }
}