package net.gliby.voicechat.client.gui;

import net.gliby.voicechat.client.textures.IndependentGUITexture;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.entity.player.EnumPlayerModelParts;
import org.lwjgl.opengl.GL11;

public class GuiUIPlacementSpeak extends GuiPlaceableInterface
{
    GuiUIPlacementSpeak(UIPosition position, int width, int height)
    {
        super(position, width, height);
        super.width = 56;
        super.height = 52;
    }

    public void draw(Minecraft mc, GuiScreen gui, int x, int y, float tick)
    {
        GL11.glEnable(3042);
        GL11.glBlendFunc(770, 771);
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        GL11.glScalef(super.scale, super.scale, 1.0F);
        GL11.glTranslatef(1.0F, 3.0F, 0.0F);
        IndependentGUITexture.TEXTURES.bindTexture(mc);
        gui.drawTexturedModalRect(0, 0, 0, 0, 54, 46);

        switch ((int)((float)(Minecraft.getSystemTime() % 1000L) / 350.0F))
        {
            case 0:
                gui.drawTexturedModalRect(12, -3, 0, 47, 22, 49);
                break;
            case 1:
                gui.drawTexturedModalRect(31, -3, 23, 47, 14, 49);
                break;
            case 2:
                gui.drawTexturedModalRect(40, -3, 38, 47, 16, 49);
        }
        mc.getTextureManager().bindTexture(mc.thePlayer.getLocationSkin());
        GL11.glTranslatef(0.0F, 14.0F, 0.0F);
        GL11.glScalef(2.4F, 2.4F, 0.0F);
        Gui.drawScaledCustomSizeModalRect(0, 0, 8.0F, 8.0F, 8, 8, 8, 8, 64.0F, 64.0F);

        if (mc.thePlayer != null && mc.thePlayer.isWearing(EnumPlayerModelParts.HAT))
        {
            Gui.drawScaledCustomSizeModalRect(0, 0, 40.0F, 8.0F, 8, 8, 8, 8, 64.0F, 64.0F);
        }
    }
}