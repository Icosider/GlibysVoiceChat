package net.gliby.voicechat.client.gui;

import net.gliby.voicechat.client.textures.IndependentGUITexture;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.entity.player.EnumPlayerModelParts;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;

import java.util.Random;

import static org.lwjgl.opengl.GL11.*;

public class GuiUIPlacementVoicePlate extends GuiPlaceableInterface {
    private static final ResourceLocation skinDefault = new ResourceLocation("textures/entity/steve.png");
    private final String[] players;

    GuiUIPlacementVoicePlate(UIPosition position, int width, int height) {
        super(position, width, height);
        super.width = 70;
        super.height = 55;
        this.players = new String[]{"Ivasik", "theGliby", "Zundrel"};
        this.shuffleArray(this.players);
    }

    public void draw(Minecraft mc, GuiScreen gui, int x, int y, float tick) {
        for (int i = 0; i < this.players.length; ++i) {
            String stream = this.players[i];
            int length = mc.fontRenderer.getStringWidth(stream);
            float scale = 0.75F * super.scale;
            glPushMatrix();
            glTranslatef(super.positionUI.x + (float) super.positionUI.info.offsetX, super.positionUI.y + (float) super.positionUI.info.offsetY + (float) (i * 23) * scale, 0.0F);
            glScalef(scale, scale, 0.0F);
            glEnable(GL_BLEND);
            OpenGlHelper.glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA, GL_ONE, GL_ZERO);
            glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
            IndependentGUITexture.TEXTURES.bindTexture(mc);
            gui.drawTexturedModalRect(0, 0, 56, 0, 109, 22);
            glPushMatrix();
            scale = MathHelper.clamp(50.5F / (float) length, 0.0F, 1.25F);
            glTranslatef(25.0F + scale / 2.0F, 11.0F - (float) (mc.fontRenderer.FONT_HEIGHT - 1) * scale / 2.0F, 0.0F);
            glScalef(scale, scale, 0.0F);
            gui.drawString(mc.fontRenderer, stream, 0, 0, -1);
            glPopMatrix();
            glPushMatrix();
            mc.getTextureManager().bindTexture(skinDefault);
            glTranslatef(3.25F, 3.25F, 0.0F);
            glScalef(2.0F, 2.0F, 0.0F);
            Gui.drawScaledCustomSizeModalRect(0, 0, 8.0F, 8.0F, 8, 8, 8, 8, 64.0F, 64.0F);

            if (mc.player != null && mc.player.isWearing(EnumPlayerModelParts.HAT))
                Gui.drawScaledCustomSizeModalRect(0, 0, 40.0F, 8.0F, 8, 8, 8, 8, 64.0F, 64.0F);
            glPopMatrix();
            glDisable(GL_BLEND);
            glPopMatrix();
        }
    }

    private void shuffleArray(String[] ar) {
        Random rnd = new Random();

        for (int i = ar.length - 1; i > 0; --i) {
            int index = rnd.nextInt(i + 1);
            String a = ar[index];
            ar[index] = ar[i];
            ar[i] = a;
        }
    }
}