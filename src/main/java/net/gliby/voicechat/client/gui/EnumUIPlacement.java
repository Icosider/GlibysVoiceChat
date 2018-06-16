package net.gliby.voicechat.client.gui;

public enum EnumUIPlacement
{
    VOICE_PLATES(0.84F, 0.0078F, 3, 2, 0),
    SPEAK(0.86F, 0.74F, 1, 3, 0);
    int offsetX;
    int offsetY;
    public int positionType;
    public float x;
    public float y;

    EnumUIPlacement(float x, float y, int offsetX, int offsetY, int positionType)
    {
        this.x = x;
        this.y = y;
        this.offsetX = offsetX;
        this.offsetY = offsetY;
        this.positionType = positionType;
    }
}