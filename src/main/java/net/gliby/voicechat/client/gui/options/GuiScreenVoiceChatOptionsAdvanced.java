package net.gliby.voicechat.client.gui.options;

import net.gliby.voicechat.client.VoiceChatClient;
import net.gliby.voicechat.client.gui.GuiBoostSlider;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.resources.I18n;
import org.lwjgl.opengl.GL11;

public class GuiScreenVoiceChatOptionsAdvanced extends GuiScreen {
    private final VoiceChatClient voiceChat;
    private GuiButton encodingMode;
    private GuiButton enhancedDecoding;
    private GuiButton serverConnection;
    private GuiButton volumeControlButton;
    private GuiBoostSlider qualitySlider;
    private final GuiScreen parent;

    GuiScreenVoiceChatOptionsAdvanced(VoiceChatClient voiceChat, GuiScreen parent) {
        this.voiceChat = voiceChat;
        this.parent = parent;
    }

    @Override
    protected void actionPerformed(GuiButton button) {
        switch (button.id) {
            case 0:
                this.voiceChat.getSettings().getConfiguration().save();
                this.mc.displayGuiScreen(this.parent);
                break;
            case 1:
                this.resetAdvancedOptions();
            case 2:
            case 3:
            case 4:
            default:
                break;
            case 5:
                int mode = this.voiceChat.getSettings().getEncodingMode();

                if( mode < 2)
                    ++mode;
                else
                    mode = 0;
                this.voiceChat.getSettings().setEncodingMode(mode);
                this.encodingMode.displayString = I18n.format("menu.encodingMode") + ": " + this.voiceChat.getSettings().getEncodingModeString();
                break;
            case 6:
                this.voiceChat.getSettings().setPerceptualEnchantment(!this.voiceChat.getSettings().isPerceptualEnchantmentAllowed());
                this.enhancedDecoding.displayString = I18n.format("menu.enhancedDecoding") + ": " + (this.voiceChat.getSettings().isPerceptualEnchantmentAllowed()?I18n.format("options.on"):I18n.format("options.off"));
                break;
            case 7:
                this.voiceChat.getSettings().setSnooperAllowed(false);
                this.serverConnection.displayString = I18n.format("menu.allowSnooper") + ": " + (this.voiceChat.getSettings().isSnooperAllowed()?I18n.format("options.on"):I18n.format("options.off"));
                break;
            case 8:
                this.voiceChat.getSettings().setVolumeControl(!this.voiceChat.getSettings().isVolumeControlled());
                this.volumeControlButton.displayString = I18n.format("menu.volumeControl") + ": " + (this.voiceChat.getSettings().isVolumeControlled()?I18n.format("options.on"):I18n.format("options.off"));
                VoiceChatClient.getSoundManager().volumeControlStop();
        }
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        final int centerW = this.width / 2;

        this.drawDefaultBackground();
        GL11.glPushMatrix();
        GL11.glTranslatef((float) (centerW) - (float)(this.fontRenderer.getStringWidth("Gliby\'s Voice Chat Options") / 2) * 1.5F, 0.0F, 0.0F);
        GL11.glScalef(1.5F, 1.5F, 0.0F);
        this.drawString(this.mc.fontRenderer, "Gliby\'s Voice Chat Options", 0, 6, -1);
        GL11.glPopMatrix();
        GL11.glPushMatrix();
        GL11.glTranslatef((float) (centerW - this.fontRenderer.getStringWidth(I18n.format("menu.advancedOptions")) / 2), 12.0F, 0.0F);
        this.drawString(this.mc.fontRenderer, I18n.format("menu.advancedOptions"), 0, 12, -1);
        GL11.glPopMatrix();

        if ((int)(this.voiceChat.getSettings().getEncodingQuality() * 10.0F) <= 2)
            this.drawCenteredString(this.mc.fontRenderer, I18n.format("menu.encodingMessage"), centerW, this.height - 50, -255);
        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    @Override
    public void initGui() {
        final int centerW = this.width / 2;

        this.buttonList.add(new GuiButton(0, centerW - 75, this.height - 34, 150, 20, I18n.format("gui.back")));
        this.buttonList.add(new GuiButton(1, centerW + 77, this.height - 34, 75, 20, I18n.format("controls.reset")));
        this.qualitySlider = new GuiBoostSlider(4, centerW + 2, 74, "", I18n.format("menu.encodingQuality") + ": " + (this.voiceChat.getSettings().getEncodingQuality() == 0.0F?"0":String.valueOf((int)(this.voiceChat.getSettings().getEncodingQuality() * 10.0F))), 0.0F);
        this.qualitySlider.sliderValue = this.voiceChat.getSettings().getEncodingQuality();
        this.encodingMode = new GuiButton(5, centerW - 152, 98, 150, 20, I18n.format("menu.encodingMode") + ": " + this.voiceChat.getSettings().getEncodingModeString());
        this.buttonList.add(this.enhancedDecoding = new GuiButton(6, centerW - 152, 50, 150, 20, I18n.format("menu.enhancedDecoding") + ": " + (this.voiceChat.getSettings().isPerceptualEnchantmentAllowed()?I18n.format("options.on"):I18n.format("options.off"))));
        this.buttonList.add(this.serverConnection = new GuiButton(7, centerW + 2, 50, 150, 20, I18n.format("menu.allowSnooper") + ": " + (this.voiceChat.getSettings().isSnooperAllowed()?I18n.format("options.on"):I18n.format("options.off"))));
        this.buttonList.add(this.volumeControlButton = new GuiButton(8, centerW - 152, 74, 150, 20, I18n.format("menu.volumeControl") + ": " + (this.voiceChat.getSettings().isVolumeControlled()?I18n.format("options.on"):I18n.format("options.off"))));
        this.buttonList.add(this.qualitySlider);
        this.buttonList.add(this.encodingMode);
        this.serverConnection.enabled = false;
        this.encodingMode.enabled = false;
    }

    @Override
    public void onGuiClosed() {
        super.onGuiClosed();
        this.voiceChat.getSettings().getConfiguration().save();
    }

    private void resetAdvancedOptions() {
        this.qualitySlider.sliderValue = 0.6F;
        this.voiceChat.getSettings().setEncodingQuality(this.qualitySlider.sliderValue);
        this.qualitySlider.idValue = I18n.format("menu.encodingQuality") + ": " + (this.voiceChat.getSettings().getEncodingQuality() == 0.0F?"0":String.valueOf((int)(this.voiceChat.getSettings().getEncodingQuality() * 10.0F)));
        this.qualitySlider.displayString = this.qualitySlider.idValue;
        this.voiceChat.getSettings().setEncodingMode(1);
        this.encodingMode.displayString = I18n.format("menu.encodingMode") + ": " + this.voiceChat.getSettings().getEncodingModeString();
        this.voiceChat.getSettings().setPerceptualEnchantment(true);
        this.enhancedDecoding.displayString = I18n.format("menu.enhancedDecoding") + ": " + (this.voiceChat.getSettings().isPerceptualEnchantmentAllowed()?I18n.format("options.on"):I18n.format("options.off"));
        this.voiceChat.getSettings().setSnooperAllowed(false);
        this.serverConnection.displayString = I18n.format("menu.allowSnooper") + ": " + (this.voiceChat.getSettings().isSnooperAllowed()?I18n.format("options.on"):I18n.format("options.off"));
        this.voiceChat.getSettings().setVolumeControl(true);
        this.volumeControlButton.displayString = I18n.format("menu.volumeControl") + ": " + (this.voiceChat.getSettings().isVolumeControlled()?I18n.format("options.on"):I18n.format("options.off"));
    }

    @Override
    public void updateScreen() {
        final float encodingQuality = this.voiceChat.getSettings().getEncodingQuality();

        this.voiceChat.getSettings().setEncodingQuality(this.qualitySlider.sliderValue);
        this.qualitySlider.setDisplayString(I18n.format("menu.encodingQuality") + ": " + (encodingQuality == 0F ? "0" : String.valueOf((int) (encodingQuality * 10.0F))));
    }
}