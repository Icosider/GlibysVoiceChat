package net.gliby.voicechat.client.keybindings;

import net.gliby.voicechat.client.VoiceChatClient;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.relauncher.Side;

public class KeyTickHandler {
    private final VoiceChatClient voiceChat;

    public KeyTickHandler(VoiceChatClient voiceChat) {
        this.voiceChat = voiceChat;
    }

    @SubscribeEvent
    public void tick(TickEvent event) {
        if (event.type == TickEvent.Type.PLAYER && event.side == Side.CLIENT)
            this.voiceChat.keyManager.keyEvent(null);
    }
}