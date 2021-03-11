package ru.icosider.voicechat;

import net.minecraft.client.Minecraft;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public enum AsyncCatcher {
    INSTANCE;

    public void execute(Runnable run) {
        FMLCommonHandler.instance().getMinecraftServerInstance().addScheduledTask(run);
    }

    @SideOnly(Side.CLIENT)
    public void executeClient(Runnable run) {
        Minecraft.getMinecraft().addScheduledTask(run);
    }
}