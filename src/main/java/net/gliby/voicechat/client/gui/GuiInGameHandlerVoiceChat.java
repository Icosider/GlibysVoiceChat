package net.gliby.voicechat.client.gui;

import net.gliby.voicechat.VoiceChat;
import net.gliby.voicechat.client.VoiceChatClient;
import net.gliby.voicechat.client.debug.Statistics;
import net.gliby.voicechat.client.sound.ClientStream;
import net.gliby.voicechat.client.textures.IndependentGUITexture;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.player.EnumPlayerModelParts;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.client.event.RenderGameOverlayEvent.ElementType;
import net.minecraftforge.client.event.RenderGameOverlayEvent.Post;
import net.minecraftforge.client.event.RenderGameOverlayEvent.Text;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.lwjgl.Sys;
import org.lwjgl.util.vector.Vector2f;

public class GuiInGameHandlerVoiceChat extends Gui {
    private long lastFrame;
    private long lastFPS;
    private float fade = 0.0F;
    private final VoiceChatClient voiceChat;
    private ScaledResolution res;
    private final Minecraft mc;

    public GuiInGameHandlerVoiceChat(VoiceChatClient voiceChat) {
        this.voiceChat = voiceChat;
        this.mc = Minecraft.getMinecraft();
    }

    private void calcDelta() {
        if (this.getTime() - this.lastFPS > 1000L)
            this.lastFPS += 1000L;
    }

    private int getDelta() {
        long time = this.getTime();
        int delta = (int) (time - this.lastFrame);
        this.lastFrame = time;
        return delta;
    }

    private Vector2f getPosition(int width, int height, UIPosition uiPositionSpeak) {
        return uiPositionSpeak.type == 0 ? new Vector2f(uiPositionSpeak.x * (float) width, uiPositionSpeak.y * (float) height) : new Vector2f(uiPositionSpeak.x, uiPositionSpeak.y);
    }

    private long getTime() {
        return Sys.getTime() * 1000L / Sys.getTimerResolution();
    }

    @SubscribeEvent
    public void render(Text text) {
        if (text.getType() == ElementType.DEBUG && VoiceChat.getProxyInstance().getSettings().isDebug()) {
            Statistics stats = VoiceChatClient.getStatistics();

            if (stats != null) {
                int settings = 1 | ValueFormat.PRECISION(2) | 192;
                String encodedAvg = ValueFormat.format(stats.getEncodedAverageDataReceived(), settings);
                String decodedAvg = ValueFormat.format(stats.getDecodedAverageDataReceived(), settings);
                String encodedData = ValueFormat.format(stats.getEncodedDataReceived(), settings);
                String decodedData = ValueFormat.format(stats.getDecodedDataReceived(), settings);
                text.getRight().add("Voice Chat Debug Info");
                text.getRight().add("VC Data [ENC AVG]: " + encodedAvg + "");
                text.getRight().add("VC Data [DEC AVG]: " + decodedAvg + "");
                text.getRight().add("VC Data [ENC REC]: " + encodedData + "");
                text.getRight().add("VC Data [DEC REC]: " + decodedData + "");
            }
        }
    }

    @SubscribeEvent
    public void renderInGameGui(Post event) {
        if (!event.isCancelable() && event.getType() == ElementType.ALL) {
            if (this.res == null) {
                this.getDelta();
                this.lastFPS = this.getTime();
            }

            this.res = new ScaledResolution(this.mc);
            int width = this.res.getScaledWidth();
            int height = this.res.getScaledHeight();
            int delta = this.getDelta();
            this.calcDelta();

            if (!VoiceChat.getProxyInstance().isRecorderActive()) {
                if (this.fade > 0.0F)
                    this.fade -= 0.01F * (float) delta;
                else
                    this.fade = 0.0F;
            } else if (this.fade < 1.0F && VoiceChat.getProxyInstance().isRecorderActive())
                this.fade += 0.01F * (float) delta;
            else
                this.fade = 1.0F;

            UIPosition positionUI;
            Vector2f position;
            if (this.fade != 0.0F) {
                positionUI = this.voiceChat.getSettings().getUIPositionSpeak();
                position = this.getPosition(width, height, positionUI);

                if (positionUI.scale != 0.0F) {
                    GlStateManager.pushMatrix();
                    GlStateManager.enableBlend();
                    GlStateManager.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
                    GlStateManager.color(1.0F, 1.0F, 1.0F, this.fade * this.voiceChat.getSettings().getUIOpacity());
                    IndependentGUITexture.TEXTURES.bindTexture(this.mc);
                    GlStateManager.translate(position.x + (float) positionUI.info.offsetX, position.y + (float) positionUI.info.offsetY, 0.0F);
                    GlStateManager.scale(positionUI.scale, positionUI.scale, 1.0F);
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
                    this.mc.getTextureManager().bindTexture(this.mc.player.getLocationSkin());
                    GlStateManager.translate(0.0F, 14.0F, 0.0F);
                    GlStateManager.scale(2.4F, 2.4F, 0.0F);
                    Gui.drawScaledCustomSizeModalRect(0, 0, 8.0F, 8.0F, 8, 8, 8, 8, 64.0F, 64.0F);

                    if (this.mc.player != null && this.mc.player.isWearing(EnumPlayerModelParts.HAT))
                        Gui.drawScaledCustomSizeModalRect(0, 0, 40.0F, 8.0F, 8, 8, 8, 8, 64.0F, 64.0F);
                    GlStateManager.disableBlend();
                    GlStateManager.popMatrix();
                }
            }

            if (!VoiceChatClient.getSoundManager().currentStreams.isEmpty() && this.voiceChat.getSettings().isVoicePlateAllowed()) {
                float scale;
                positionUI = this.voiceChat.getSettings().getUIPositionPlate();
                position = this.getPosition(width, height, positionUI);
                GlStateManager.pushMatrix();
                GlStateManager.enableBlend();
                GlStateManager.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);

                for (int i = 0; i < VoiceChatClient.getSoundManager().currentStreams.size(); ++i) {
                    ClientStream stream = VoiceChatClient.getSoundManager().currentStreams.get(i);

                    if (stream != null) {
                        String s = stream.player.entityName();
                        boolean playerExists = stream.player.getPlayer() != null;
                        int length = this.mc.fontRenderer.getStringWidth(s);
                        scale = 0.75F * positionUI.scale;
                        GlStateManager.pushMatrix();
                        GlStateManager.translate(position.x + (float) positionUI.info.offsetX, position.y + (float) positionUI.info.offsetY + (float) (i * 23) * scale, 0.0F);
                        GlStateManager.scale(scale, scale, 0.0F);
                        GlStateManager.color(1.0F, 1.0F, 1.0F, this.voiceChat.getSettings().getUIOpacity());
                        GlStateManager.translate(0.0F, 0.0F, 0.0F);
                        IndependentGUITexture.TEXTURES.bindTexture(this.mc);
                        this.drawTexturedModalRect(0, 0, 56, stream.special * 22, 109, 22);
                        GlStateManager.pushMatrix();
                        scale = MathHelper.clamp(50.5F / (float) length, 0.0F, 1.25F);
                        GlStateManager.translate(25.0F + scale / 2.0F, 11.0F - (float) (this.mc.fontRenderer.FONT_HEIGHT - 1) * scale / 2.0F, 0.0F);
                        GlStateManager.scale(scale, scale, 0.0F);
                        this.drawString(this.mc.fontRenderer, s, 0, 0, -1);
                        GlStateManager.popMatrix();
                        GlStateManager.pushMatrix();

                        if (playerExists)
                            IndependentGUITexture.bindPlayer(this.mc, stream.player.getPlayer());
                        else
                            IndependentGUITexture.bindDefaultPlayer(this.mc);

                        GlStateManager.color(1.0F, 1.0F, 1.0F, this.voiceChat.getSettings().getUIOpacity());
                        GlStateManager.translate(3.25F, 3.25F, 0.0F);
                        GlStateManager.scale(2.0F, 2.0F, 0.0F);
                        Gui.drawScaledCustomSizeModalRect(0, 0, 8.0F, 8.0F, 8, 8, 8, 8, 64.0F, 64.0F);

                        if (this.mc.player != null && this.mc.player.isWearing(EnumPlayerModelParts.HAT))
                            Gui.drawScaledCustomSizeModalRect(0, 0, 40.0F, 8.0F, 8, 8, 8, 8, 64.0F, 64.0F);
                        GlStateManager.popMatrix();
                        GlStateManager.popMatrix();
                    }
                }

                GlStateManager.popMatrix();
            }

            if (VoiceChatClient.getSoundManager().currentStreams.isEmpty())
                VoiceChatClient.getSoundManager().volumeControlStop();
            else if (this.voiceChat.getSettings().isVolumeControlled())
                VoiceChatClient.getSoundManager().volumeControlStart();
        }
    }
}