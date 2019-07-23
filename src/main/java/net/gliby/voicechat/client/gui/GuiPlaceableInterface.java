package net.gliby.voicechat.client.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;

public abstract class GuiPlaceableInterface {
    int screenWidth;
    int screenHeight;
    UIPosition positionUI;
    float x;
    float y;
    int positionType;
    int width;
    int height;
    float scale;
    EnumUIPlacement info;

    GuiPlaceableInterface(UIPosition position, int width, int height) {
        this.positionUI = position;
        this.info = position.info;
        this.x = position.type == 0?position.x * (float)width:position.x;
        this.y = position.type == 0?position.y * (float)height:position.y;
        this.positionType = position.type;
        this.screenWidth = width;
        this.screenHeight = height;
        this.scale = position.scale;
    }

    public abstract void draw(Minecraft var1, GuiScreen var2, int var3, int var4, float var5);

    void update(int x, int y, int width, int height) {
        this.x = (float)x;
        this.y = (float)y;
        this.screenWidth = width;
        this.screenHeight = height;
    }
}