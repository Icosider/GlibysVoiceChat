package net.gliby.voicechat.client.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.renderer.OpenGlHelper;

import static org.lwjgl.opengl.GL11.*;

public class GuiCustomButton extends GuiButton {
    public boolean allowed = true;

    public GuiCustomButton(int id, int x, int y, int w, int h, String text) {
        super(id, x, y, w, h, text);
    }

    public void drawButton(Minecraft mc, int mouseX, int mouseY) {
        if (super.visible) {
            FontRenderer fontrenderer = mc.fontRenderer;
            mc.getTextureManager().bindTexture(GuiButton.BUTTON_TEXTURES);
            glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
            super.hovered = mouseX >= super.x && mouseY >= super.y && mouseX < super.x + super.width && mouseY < super.y + super.height;
            int k = this.getHoverState(super.hovered);
            glEnable(GL_BLEND);
            OpenGlHelper.glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA, GL_ONE, GL_ZERO);
            this.drawTexturedModalRect(super.x, super.y, 0, 46 + k * 20, super.width / 2, super.height);
            this.drawTexturedModalRect(super.x + super.width / 2, super.y, 200 - super.width / 2, 46 + k * 20, super.width / 2, super.height);
            this.mouseDragged(mc, mouseX, mouseY);
            int l = 14737632;

            if (this.packedFGColour != 0)
                l = this.packedFGColour;
            else if (!this.enabled)
                l = 10526880;
            else if (super.hovered && this.allowed)
                l = 16777120;

            this.drawCenteredString(fontrenderer, this.displayString, super.x + super.width / 2, super.y + (super.height - 8) / 2, l);
            glDisable(GL_BLEND);
        }
    }

    @Override
    public int getHoverState(boolean par1) {
        byte b0 = 1;

        if (!this.enabled)
            b0 = 0;
        else if (par1)
            b0 = 2;

        if (!this.allowed)
            b0 = 1;
        return b0;
    }

    public void setHeight(int height) {
        super.height = height;
    }

    @Override
    public void setWidth(int width) {
        super.width = width;
    }
}