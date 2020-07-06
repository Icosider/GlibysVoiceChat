package net.gliby.voicechat.common.networking.voiceservers;

public enum EnumVoiceNetworkType {
    MINECRAFT("Minecraft", false),
    UDP("UDP", true);

    public boolean authRequired;
    public String name;

    EnumVoiceNetworkType(String name, boolean authRequired) {
        this.name = name;
        this.authRequired = authRequired;
    }
}