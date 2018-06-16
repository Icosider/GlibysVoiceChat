package net.gliby.voicechat.client.gui;

import net.gliby.voicechat.VoiceChat;
import net.gliby.voicechat.client.textures.IndependentGUITexture;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiButton;
import org.lwjgl.opengl.GL11;

import java.io.UnsupportedEncodingException;

public class GuiDropDownMenu extends GuiButton
{
    private String[] array;
    private boolean[] mouseOn;
    private final int prevHeight;
    private int amountOfItems;
    public boolean dropDownMenu = false;
    public int selectedInteger;

    public GuiDropDownMenu(int par1, int par2, int par3, int par4, int par5, String par6Str, String[] array)
    {
        super(par1, par2, par3, par4, par5, par6Str);
        this.prevHeight = super.height;
        this.array = array;
        this.amountOfItems = array.length;
        this.mouseOn = new boolean[this.amountOfItems];
    }

    public GuiDropDownMenu(int par1, int par2, int par3, String par4Str, String[] array)
    {
        super(par1, par2, par3, par4Str);
        this.prevHeight = super.height;
        this.array = array;
        this.amountOfItems = array.length;
        this.mouseOn = new boolean[this.amountOfItems];
    }

    @Override
    public void drawButton(Minecraft mc, int x, int y)
    {
        if (super.visible)
        {
            if (this.dropDownMenu && this.array.length != 0)
            {
                super.height = this.prevHeight * (this.amountOfItems + 1);
            }
            else {
                super.height = this.prevHeight;
            }

            FontRenderer fontrenderer = mc.fontRendererObj;
            GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
            super.hovered = x >= super.xPosition && y >= super.yPosition && x < super.xPosition + super.width && y < super.yPosition + super.height;
            this.getHoverState(super.hovered);
            this.mouseDragged(mc, x, y);
            int l = 14737632;
            drawRect(super.xPosition - 1, super.yPosition - 1, super.xPosition + super.width + 1, super.yPosition + super.height + 1, -6250336);
            drawRect(super.xPosition, super.yPosition, super.xPosition + super.width, super.yPosition + super.height, -16777216);
            drawRect(super.xPosition - 1, super.yPosition + this.prevHeight, super.xPosition + super.width + 1, super.yPosition + this.prevHeight + 1, -6250336);
            GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
            boolean u = true;
            short var9;

            if (this.dropDownMenu && this.array.length != 0)
            {
                var9 = 228;
            }
            else {
                var9 = 242;
            }

            if (!this.enabled)
            {
                l = -6250336;
            }
            String normalName = normalText(this.displayString.substring(0, Math.min(this.displayString.length(), 23)));
            this.drawCenteredString(fontrenderer, normalName, super.xPosition + super.width / 2 - 9, super.yPosition + (this.prevHeight - 8) / 2, l);
            GL11.glPushMatrix();

            if (this.dropDownMenu && this.array.length != 0)
            {
                for (int i = 0; i < this.amountOfItems; ++i)
                {
                    this.mouseOn[i] = this.inBounds(x, y, super.xPosition, super.yPosition + this.prevHeight * (i + 1), super.width, this.prevHeight);
                    String s = normalText(this.array[i].substring(0, Math.min(this.array[i].length(), 26)) + "..");
                    this.drawCenteredString(fontrenderer, s, super.xPosition + super.width / 2, super.yPosition + this.prevHeight * (i + 1) + 7, this.mouseOn[i]?16777120:14737632);
                }
            }
            GL11.glPopMatrix();
            GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
            IndependentGUITexture.TEXTURES.bindTexture(Minecraft.getMinecraft());
            this.drawTexturedModalRect(super.xPosition + super.width - 15, super.yPosition + 2, var9, 0, 14, 14);
        }
    }

    private String normalText(String text)
    {
        try
        {
            return new String(text.getBytes("Windows-1252"), "Windows-1251");
        }
        catch (UnsupportedEncodingException e)
        {
            VoiceChat.getLogger().error("Decode error: " + e.getMessage());
        }
        return "";
    }

    public int getMouseOverInteger()
    {
        for (int i = 0; i < this.mouseOn.length; ++i)
        {
            if (this.mouseOn[i])
            {
                return i;
            }
        }
        return -1;
    }

    private boolean inBounds(int x, int y, int posX, int posY, int width, int height)
    {
        return this.enabled && super.visible && x >= posX && y >= posY && x < posX + width && y < posY + height;
    }

    public void setArray(String[] array)
    {
        this.array = array;
        this.amountOfItems = array.length;
        this.mouseOn = new boolean[this.amountOfItems];
    }

    public void setDisplayString(String s)
    {
        this.displayString = s;
    }
}