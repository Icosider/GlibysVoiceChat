package net.gliby.voicechat.client.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.opengl.GL11;

@SideOnly(Side.CLIENT)
public class GuiBoostSlider extends GuiButton {
    public float sliderValue;
    private boolean dragging;
    public String idValue;

    public GuiBoostSlider(int id, int x, int y, String idValue, String text, float value) {
        super(id, x, y, 150, 20, text);
        this.idValue = idValue;
        this.sliderValue = value;
    }

    @Override
    public int getHoverState(boolean par1)
    {
        return 0;
    }

    @Override
    protected void mouseDragged(Minecraft mc, int mouseX, int mouseY) {
        if (super.visible) {
            if (this.dragging) {
                this.sliderValue = (float)(mouseX - (super.x + 4)) / (float)(super.width - 8);

                if (this.sliderValue < 0.0F)
                    this.sliderValue = 0.0F;

                if (this.sliderValue > 1.0F)
                    this.sliderValue = 1.0F;
                this.displayString = this.idValue;
            }

            GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
            this.drawTexturedModalRect(super.x + (int)(this.sliderValue * (float)(super.width - 8)), super.y, 0, 66, 4, 20);
            this.drawTexturedModalRect(super.x + (int)(this.sliderValue * (float)(super.width - 8)) + 4, super.y, 196, 66, 4, 20);
        }
    }

    @Override
    public boolean mousePressed(Minecraft mc, int mouseX, int mouseY) {
        if (super.mousePressed(mc, mouseX, mouseY)) {
            this.sliderValue = (float)(mouseX - (super.x + 4)) / (float)(super.width - 8);

            if (this.sliderValue < 0.0F)
                this.sliderValue = 0.0F;

            if (this.sliderValue > 1.0F)
                this.sliderValue = 1.0F;
            this.dragging = true;
            return true;
        }
        return false;
    }

    @Override
    public void mouseReleased(int mouseX, int mouseY) {
        this.dragging = false;
    }

    public void setDisplayString(String display) {
        this.idValue = display;
        this.displayString = display;
    }
}