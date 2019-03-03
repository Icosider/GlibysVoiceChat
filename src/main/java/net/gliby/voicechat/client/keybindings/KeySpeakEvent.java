package net.gliby.voicechat.client.keybindings;

import net.gliby.voicechat.client.VoiceChatClient;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.inventory.GuiChest;
import net.minecraft.client.gui.inventory.GuiCrafting;
import net.minecraft.client.gui.inventory.GuiFurnace;
import net.minecraft.client.gui.inventory.GuiInventory;
import net.minecraft.client.settings.KeyBinding;

class KeySpeakEvent extends KeyEvent {
    private final VoiceChatClient voiceChat;
    private final boolean canSpeak;

    KeySpeakEvent(VoiceChatClient voiceChat, EnumBinding keyBind, int keyID, boolean repeating) {
        super(keyBind, keyID, repeating);
        this.voiceChat = voiceChat;
        this.canSpeak = voiceChat.getSettings().getInputDevice() != null;
    }

    @Override
    public void keyDown(KeyBinding kb, boolean tickEnd, boolean isRepeat) {
        GuiScreen screen = Minecraft.getMinecraft().currentScreen;

        if (tickEnd && this.canSpeak && (screen == null || screen instanceof GuiInventory || screen instanceof GuiCrafting || screen instanceof GuiChest || screen instanceof GuiFurnace || screen.getClass().getSimpleName().startsWith("GuiDriveableController"))) {
            this.voiceChat.recorder.set(this.voiceChat.getSettings().getSpeakMode() != 1 || !this.voiceChat.isRecorderActive());
            this.voiceChat.setRecorderActive(this.voiceChat.getSettings().getSpeakMode() != 1 || !this.voiceChat.isRecorderActive());
        }
    }

    @Override
    public void keyUp(KeyBinding kb, boolean tickEnd) {
        if (tickEnd && this.voiceChat.getSettings().getSpeakMode() == 0) {
            this.voiceChat.setRecorderActive(false);
            this.voiceChat.recorder.stop();
        }
    }
}