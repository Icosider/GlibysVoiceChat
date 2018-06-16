package net.gliby.voicechat.client.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.renderer.OpenGlHelper;
import org.lwjgl.opengl.GL11;

public class GuiCustomButton extends GuiButton
{
    public boolean allowed = true;

    public GuiCustomButton(int id, int x, int y, int w, int h, String text)
    {
        super(id, x, y, w, h, text);
    }

    public void drawButton(Minecraft mc, int mouseX, int mouseY)
    {
        if (super.visible)
        {
            FontRenderer fontrenderer = mc.fontRendererObj;
            mc.getTextureManager().bindTexture(GuiButton.BUTTON_TEXTURES);
            GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
            super.hovered = mouseX >= super.xPosition && mouseY >= super.yPosition && mouseX < super.xPosition + super.width && mouseY < super.yPosition + super.height;
            int k = this.getHoverState(super.hovered);
            GL11.glEnable(3042);
            OpenGlHelper.glBlendFunc(770, 771, 1, 0);
            GL11.glBlendFunc(770, 771);
            this.drawTexturedModalRect(super.xPosition, super.yPosition, 0, 46 + k * 20, super.width / 2, super.height);
            this.drawTexturedModalRect(super.xPosition + super.width / 2, super.yPosition, 200 - super.width / 2, 46 + k * 20, super.width / 2, super.height);
            this.mouseDragged(mc, mouseX, mouseY);
            int l = 14737632;

            if (this.packedFGColour != 0)
            {
                l = this.packedFGColour;
            }
            else if (!this.enabled)
            {
                l = 10526880;
            }
            else if (super.hovered && this.allowed)
            {
                l = 16777120;
            }
            this.drawCenteredString(fontrenderer, this.displayString, super.xPosition + super.width / 2, super.yPosition + (super.height - 8) / 2, l);
            GL11.glDisable(GL11.GL_BLEND);
        }
    }

    @Override
    public int getHoverState(boolean par1)
    {
        byte b0 = 1;

        if (!this.enabled)
        {
            b0 = 0;
        }
        else if (par1)
        {
            b0 = 2;
        }

        if (!this.allowed)
        {
            b0 = 1;
        }
        return b0;
    }

    public void setHeight(int height)
    {
        super.height = height;
    }

    @Override
    public void setWidth(int width)
    {
        super.width = width;
    }
}