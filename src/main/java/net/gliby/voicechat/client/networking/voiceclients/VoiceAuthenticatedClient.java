package net.gliby.voicechat.client.networking.voiceclients;

import net.gliby.voicechat.common.networking.voiceservers.EnumVoiceNetworkType;

public abstract class VoiceAuthenticatedClient extends VoiceClient
{
    private boolean connected;
    boolean authed;
    final String hash;

    VoiceAuthenticatedClient(EnumVoiceNetworkType enumVoiceServer, String hash)
    {
        super(enumVoiceServer);
        this.hash = hash;
    }

    public abstract void autheticate();

    public final String getHash()
    {
        return this.hash;
    }

    public boolean isAuthed()
    {
        return this.authed;
    }

    public final boolean isConnected()
    {
        return this.connected;
    }

    void setAuthed(boolean authed)
    {
        this.authed = authed;
    }

    public void setConnected(boolean connected)
    {
        this.connected = connected;
    }
}