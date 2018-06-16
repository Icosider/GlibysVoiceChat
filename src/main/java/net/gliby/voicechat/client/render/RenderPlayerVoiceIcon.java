package net.gliby.voicechat.client.render;

import net.gliby.voicechat.client.VoiceChatClient;
import net.gliby.voicechat.client.sound.ClientStream;
import net.gliby.voicechat.client.textures.IndependentGUITexture;
import net.gliby.voicechat.common.MathUtility;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EnumPlayerModelParts;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.lwjgl.opengl.GL11;

public class RenderPlayerVoiceIcon extends Gui
{
    private final VoiceChatClient voiceChat;
    private final Minecraft mc;

    public RenderPlayerVoiceIcon(VoiceChatClient voiceChat, Minecraft mc)
    {
        this.voiceChat = voiceChat;
        this.mc = mc;
    }

    private void enableEntityLighting(Entity entity, float partialTicks)
    {
        int i1 = entity.getBrightnessForRender(partialTicks);

        if (entity.isBurning())
        {
            i1 = 15728880;
        }
        int j = i1 % 65536;
        int k = i1 / 65536;
        OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, (float)j / 1.0F, (float)k / 1.0F);
        OpenGlHelper.setActiveTexture(OpenGlHelper.lightmapTexUnit);
        GL11.glEnable(3553);
        OpenGlHelper.setActiveTexture(OpenGlHelper.defaultTexUnit);
    }

    private void disableEntityLighting()
    {
        OpenGlHelper.setActiveTexture(OpenGlHelper.lightmapTexUnit);
        GL11.glDisable(3553);
        OpenGlHelper.setActiveTexture(OpenGlHelper.defaultTexUnit);
    }

    @SubscribeEvent
    public void render(RenderWorldLastEvent event)
    {
        if (!VoiceChatClient.getSoundManager().currentStreams.isEmpty() && this.voiceChat.getSettings().isVoiceIconAllowed())
        {
            GL11.glDisable(2929);
            GL11.glEnable(3042);
            OpenGlHelper.glBlendFunc(770, 771, 1, 0);
            this.translateWorld(this.mc, event.partialTicks);

            for (int i = 0; (float)i < MathUtility.clamp((float)VoiceChatClient.getSoundManager().currentStreams.size(), 0.0F, (float)this.voiceChat.getSettings().getMaximumRenderableVoiceIcons()); ++i)
            {
                ClientStream stream = VoiceChatClient.getSoundManager().currentStreams.get(i);

                if (stream.player.getPlayer() != null && stream.player.usesEntity)
                {
                    EntityLivingBase entity = (EntityLivingBase)stream.player.getPlayer();

                    if (!entity.isInvisible() && !this.mc.gameSettings.hideGUI)
                    {
                        GL11.glPushMatrix();
                        this.enableEntityLighting(entity, event.partialTicks);
                        GL11.glNormal3f(0.0F, 1.0F, 0.0F);
                        GL11.glDepthMask(false);
                        this.translateEntity(entity, event.partialTicks);
                        GL11.glRotatef(-mc.getRenderManager().playerViewY, 0.0F, 1.0F, 0.0F);
                        GL11.glTranslatef(-0.25F, entity.height + 0.7F, 0.0F);
                        GL11.glRotatef(mc.getRenderManager().playerViewX, 1.0F, 0.0F, 0.0F);
                        GL11.glScalef(0.015F, 0.015F, 1.0F);
                        IndependentGUITexture.TEXTURES.bindTexture(this.mc);
                        GL11.glColor4f(1.0F, 1.0F, 1.0F, 0.25F);

                        if (!entity.isSneaking())
                        {
                            this.renderIcon();
                        }
                        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
                        GL11.glEnable(2929);
                        GL11.glDepthMask(true);
                        this.renderIcon();
                        IndependentGUITexture.bindPlayer(this.mc, entity);
                        GL11.glPushMatrix();
                        GL11.glTranslatef(20.0F, 30.0F, 0.0F);
                        GL11.glScalef(-1.0F, -1.0F, -1.0F);
                        GL11.glScalef(2.0F, 2.0F, 0.0F);
                        Gui.drawScaledCustomSizeModalRect(0, 0, 8.0F, 8.0F, 8, 8, 8, 8, 64.0F, 64.0F);

                        if (this.mc.thePlayer != null && this.mc.thePlayer.isWearing(EnumPlayerModelParts.HAT))
                        {
                            Gui.drawScaledCustomSizeModalRect(0, 0, 40.0F, 8.0F, 8, 8, 8, 8, 64.0F, 64.0F);
                        }
                        GL11.glPopMatrix();
                        this.disableEntityLighting();
                        GL11.glPopMatrix();
                    }
                }
            }
            GL11.glDisable(3042);
            GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        }
    }

    private void renderIcon()
    {
        this.drawTexturedModalRect(0, 0, 0, 0, 54, 46);

        switch ((int) ((float) (Minecraft.getSystemTime() % 1000L) / 350.0F))
        {
            case 0:
                this.drawTexturedModalRect(12, -3, 0, 47, 22, 49);
                break;
            case 1:
                this.drawTexturedModalRect(31, -3, 23, 47, 14, 49);
                break;
            case 2:
                this.drawTexturedModalRect(40, -3, 38, 47, 16, 49);
        }
    }

    private void translateEntity(Entity entity, float tick)
    {
        GL11.glTranslated(entity.prevPosX + (entity.posX - entity.prevPosX) * (double) tick, entity.prevPosY + (entity.posY - entity.prevPosY) * (double) tick, entity.prevPosZ + (entity.posZ - entity.prevPosZ) * (double) tick);
    }

    private void translateWorld(Minecraft mc, float tick)
    {
        GL11.glTranslated(-(mc.thePlayer.prevPosX + (mc.thePlayer.posX - mc.thePlayer.prevPosX) * (double) tick), -(mc.thePlayer.prevPosY + (mc.thePlayer.posY - mc.thePlayer.prevPosY) * (double) tick), -(mc.thePlayer.prevPosZ + (mc.thePlayer.posZ - mc.thePlayer.prevPosZ) * (double) tick));
    }
}