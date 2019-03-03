package net.gliby.voicechat.client.keybindings;

import net.gliby.voicechat.client.VoiceChatClient;
import net.minecraft.client.settings.KeyBinding;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.InputEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;

import java.util.ArrayList;
import java.util.List;

@SideOnly(Side.CLIENT)
public class KeyManager {
    private final VoiceChatClient voiceChat;
    @SideOnly(Side.CLIENT)
    private final List<KeyEvent> keyEvents = new ArrayList<>();
    private boolean[] keyDown;

    public KeyManager(VoiceChatClient voiceChat) {
        this.voiceChat = voiceChat;
    }

    @SideOnly(Side.CLIENT)
    public List getKeyEvents() {
        return this.keyEvents;
    }

    public String getKeyName(EnumBinding binding) {
        for (KeyEvent key : this.keyEvents) {
            if (key.keyBind == binding)
                return Keyboard.getKeyName(key.keyID);
        }
        return null;
    }

    public void init() {
        this.keyEvents.add(new KeySpeakEvent(this.voiceChat, EnumBinding.SPEAK, 47, false));
        this.keyEvents.add(new KeyOpenOptionsEvent(this.voiceChat, EnumBinding.OPEN_GUI_OPTIONS, 52, false));
        this.registerKeyBindings();
        MinecraftForge.EVENT_BUS.register(this);
    }

    @SubscribeEvent
    public void keyEvent(InputEvent.KeyInputEvent e) {
        for (int i = 0; i < this.keyEvents.size(); ++i) {
            KeyEvent keyEvent = this.keyEvents.get(i);
            KeyBinding keyBinding = this.keyEvents.get(i).forgeKeyBinding;
            int keyCode = keyBinding.getKeyCode();
            boolean state = keyCode < 0 ? Mouse.isButtonDown(keyCode + 100) : Keyboard.isKeyDown(keyCode);

            if (state != this.keyDown[i] || state && keyEvent.repeating) {
                if (state)
                    keyEvent.keyDown(keyBinding, true, state != this.keyDown[i]);
                else
                    keyEvent.keyUp(keyBinding, true);
                this.keyDown[i] = state;
            }
        }
    }

    private KeyBinding[] registerKeyBindings() {
        KeyBinding[] keyBinding = new KeyBinding[this.keyEvents.size()];

        for (int i = 0; i < keyBinding.length; ++i) {
            KeyEvent keyEvent = this.keyEvents.get(i);
            keyBinding[i] = new KeyBinding(keyEvent.keyBind.name, keyEvent.keyID, "key.categories.multiplayer");
            this.keyDown = new boolean[keyBinding.length];
            keyEvent.forgeKeyBinding = keyBinding[i];
            ClientRegistry.registerKeyBinding(keyBinding[i]);
        }
        return keyBinding;
    }
}