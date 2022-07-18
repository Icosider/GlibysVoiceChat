package net.gliby.voicechat.client.render;

import net.gliby.voicechat.client.VoiceChatClient;
import net.gliby.voicechat.client.sound.ClientStream;
import net.gliby.voicechat.client.textures.IndependentGUITexture;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EnumPlayerModelParts;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class RenderPlayerVoiceIcon extends Gui {
    private final VoiceChatClient voiceChat;
    private final Minecraft mc;

    public RenderPlayerVoiceIcon(VoiceChatClient voiceChat, Minecraft mc) {
        this.voiceChat = voiceChat;
        this.mc = mc;
    }

    private void enableEntityLighting(Entity entity, float partialTicks) {
        int i1 = entity.getBrightnessForRender();

        if (entity.isBurning()) {
            i1 = 15728880;
        }
        int j = i1 % 65536;
        int k = i1 / 65536;
        OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, (float) j, (float) k);
        OpenGlHelper.setActiveTexture(OpenGlHelper.lightmapTexUnit);
        GlStateManager.enableTexture2D();
        OpenGlHelper.setActiveTexture(OpenGlHelper.defaultTexUnit);
    }

    private void disableEntityLighting() {
        OpenGlHelper.setActiveTexture(OpenGlHelper.lightmapTexUnit);
        GlStateManager.disableTexture2D();
        OpenGlHelper.setActiveTexture(OpenGlHelper.defaultTexUnit);
    }

    @SubscribeEvent
    public void render(RenderWorldLastEvent event) {
        if (!VoiceChatClient.getSoundManager().currentStreams.isEmpty() && this.voiceChat.getSettings().isVoiceIconAllowed()) {
            GlStateManager.disableDepth();
            GlStateManager.enableBlend();
            GlStateManager.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
            this.translateWorld(this.mc, event.getPartialTicks());

            for (int i = 0; (float) i < MathHelper.clamp((float) VoiceChatClient.getSoundManager().currentStreams.size(), 0.0F, (float) this.voiceChat.getSettings().getMaximumRenderableVoiceIcons()); ++i) {
                ClientStream stream = VoiceChatClient.getSoundManager().currentStreams.get(i);

                if (stream.player.getPlayer() != null && stream.player.usesEntity) {
                    EntityLivingBase entity = (EntityLivingBase) stream.player.getPlayer();

                    if (!entity.isInvisible() && !this.mc.gameSettings.hideGUI) {
                        GlStateManager.pushMatrix();
                        this.enableEntityLighting(entity, event.getPartialTicks());
                        GlStateManager.glNormal3f(0.0F, 1.0F, 0.0F);
                        GlStateManager.depthMask(false);
                        this.translateEntity(entity, event.getPartialTicks());
                        GlStateManager.rotate(-mc.getRenderManager().playerViewY, 0.0F, 1.0F, 0.0F);
                        GlStateManager.translate(-0.25F, entity.height + 0.7F, 0.0F);
                        GlStateManager.rotate(mc.getRenderManager().playerViewX, 1.0F, 0.0F, 0.0F);
                        GlStateManager.scale(0.015F, 0.015F, 1.0F);
                        IndependentGUITexture.TEXTURES.bindTexture(this.mc);
                        GlStateManager.color(1.0F, 1.0F, 1.0F, 0.25F);

                        if (!entity.isSneaking())
                            this.renderIcon();

                        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
                        GlStateManager.enableDepth();
                        GlStateManager.depthMask(true);
                        this.renderIcon();
                        IndependentGUITexture.bindPlayer(this.mc, entity);
                        GlStateManager.pushMatrix();
                        GlStateManager.translate(20.0F, 30.0F, 0.0F);
                        GlStateManager.scale(-1.0F, -1.0F, -1.0F);
                        GlStateManager.scale(2.0F, 2.0F, 0.0F);
                        Gui.drawScaledCustomSizeModalRect(0, 0, 8.0F, 8.0F, 8, 8, 8, 8, 64.0F, 64.0F);

                        if (this.mc.player != null && this.mc.player.isWearing(EnumPlayerModelParts.HAT))
                            Gui.drawScaledCustomSizeModalRect(0, 0, 40.0F, 8.0F, 8, 8, 8, 8, 64.0F, 64.0F);

                        GlStateManager.popMatrix();
                        this.disableEntityLighting();
                        GlStateManager.popMatrix();
                    }
                }
            }
            GlStateManager.disableBlend();
            GlStateManager.enableBlend();
            GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        }
    }

    private void renderIcon() {
        this.drawTexturedModalRect(0, 0, 0, 0, 54, 46);

        switch ((int) ((float) (Minecraft.getSystemTime() % 1000L) / 350.0F)) {
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

    private void translateEntity(Entity entity, float tick) {
        GlStateManager.translate(entity.prevPosX + (entity.posX - entity.prevPosX) * (double) tick, entity.prevPosY + (entity.posY - entity.prevPosY) * (double) tick, entity.prevPosZ + (entity.posZ - entity.prevPosZ) * (double) tick);
    }

    private void translateWorld(Minecraft mc, float tick) {
        GlStateManager.translate(-(mc.player.prevPosX + (mc.player.posX - mc.player.prevPosX) * (double) tick), -(mc.player.prevPosY + (mc.player.posY - mc.player.prevPosY) * (double) tick), -(mc.player.prevPosZ + (mc.player.posZ - mc.player.prevPosZ) * (double) tick));
    }
}