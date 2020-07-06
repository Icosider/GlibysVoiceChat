package net.gliby.voicechat.client.gui;

import net.gliby.voicechat.client.textures.IndependentGUITexture;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.entity.player.EnumPlayerModelParts;

import static org.lwjgl.opengl.GL11.*;

public class GuiUIPlacementSpeak extends GuiPlaceableInterface {
    GuiUIPlacementSpeak(UIPosition position, int width, int height) {
        super(position, width, height);
        super.width = 56;
        super.height = 52;
    }

    @Override
    public void draw(Minecraft mc, GuiScreen gui, int x, int y, float tick) {
        glEnable(GL_BLEND);
        OpenGlHelper.glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA, GL_ONE, GL_ZERO);
        glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        glScalef(super.scale, super.scale, 1.0F);
        glTranslatef(1.0F, 3.0F, 0.0F);
        IndependentGUITexture.TEXTURES.bindTexture(mc);
        gui.drawTexturedModalRect(0, 0, 0, 0, 54, 46);

        switch ((int) ((float) (Minecraft.getSystemTime() % 1000L) / 350.0F)) {
            case 0:
                gui.drawTexturedModalRect(12, -3, 0, 47, 22, 49);
                break;
            case 1:
                gui.drawTexturedModalRect(31, -3, 23, 47, 14, 49);
                break;
            case 2:
                gui.drawTexturedModalRect(40, -3, 38, 47, 16, 49);
        }
        mc.getTextureManager().bindTexture(mc.player.getLocationSkin());
        glTranslatef(0.0F, 14.0F, 0.0F);
        glScalef(2.4F, 2.4F, 0.0F);
        Gui.drawScaledCustomSizeModalRect(0, 0, 8.0F, 8.0F, 8, 8, 8, 8, 64.0F, 64.0F);

        if (mc.player != null && mc.player.isWearing(EnumPlayerModelParts.HAT))
            Gui.drawScaledCustomSizeModalRect(0, 0, 40.0F, 8.0F, 8, 8, 8, 8, 64.0F, 64.0F);
    }
}