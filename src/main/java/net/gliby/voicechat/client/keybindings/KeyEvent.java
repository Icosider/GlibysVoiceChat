package net.gliby.voicechat.client.keybindings;

import net.minecraft.client.settings.KeyBinding;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public abstract class KeyEvent
{
    KeyBinding forgeKeyBinding;
    EnumBinding keyBind;
    int keyID;
    boolean repeating;

    KeyEvent(EnumBinding keyBind, int keyID, boolean repeating)
    {
        this.keyBind = keyBind;
        this.keyID = keyID;
        this.repeating = repeating;
    }

    public abstract void keyDown(KeyBinding var1, boolean var2, boolean var3);
    public abstract void keyUp(KeyBinding var1, boolean var2);
}