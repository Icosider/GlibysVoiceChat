package net.gliby.voicechat.client.gui.options;

import com.mojang.realmsclient.gui.ChatFormatting;
import net.gliby.voicechat.client.VoiceChatClient;
import net.gliby.voicechat.client.device.Device;
import net.gliby.voicechat.client.gui.GuiBoostSlider;
import net.gliby.voicechat.client.gui.GuiCustomButton;
import net.gliby.voicechat.client.gui.GuiDropDownMenu;
import net.gliby.voicechat.client.gui.GuiScreenLocalMute;
import net.gliby.voicechat.client.sound.MicrophoneTester;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.resources.I18n;
import org.lwjgl.Sys;
import org.lwjgl.opengl.GL11;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class GuiScreenVoiceChatOptions extends GuiScreen {
    private final VoiceChatClient voiceChat;
    private final MicrophoneTester tester;
    private GuiCustomButton advancedOptions;
    private GuiCustomButton mutePlayer;
    private GuiBoostSlider boostSlider;
    private GuiBoostSlider voiceVolume;
    private GuiDropDownMenu dropDown;
    private GuiButton microphoneMode;
    private List<String> warningMessages;

    public GuiScreenVoiceChatOptions(VoiceChatClient voiceChat) {
        this.voiceChat = voiceChat;
        this.tester = new MicrophoneTester(voiceChat);
    }

    @Override
    protected void actionPerformed(GuiButton button) {
        switch (button.id) {
            case 0:
                if (button instanceof GuiDropDownMenu && !this.voiceChat.getSettings().getDeviceHandler().isEmpty())
                    ((GuiDropDownMenu) button).dropDownMenu = !((GuiDropDownMenu) button).dropDownMenu;
                break;
            case 2:
                if (!this.tester.recording)
                    this.tester.start();
                else
                    this.tester.stop();
                button.displayString = this.tester.recording ? I18n.format("menu.microphoneStopTest") : I18n.format("menu.microphoneTest");
                break;
            case 3:
                this.voiceChat.getSettings().getConfiguration().save();
                this.mc.displayGuiScreen(null);
                break;
            case 4:
                this.mc.displayGuiScreen(new GuiScreenOptionsUI(this.voiceChat, this));
                break;
            case 5:
                if (!this.dropDown.dropDownMenu) {
                    this.microphoneMode.visible = true;
                    this.microphoneMode.enabled = true;
                    this.voiceChat.getSettings().setSpeakMode(this.voiceChat.getSettings().getSpeakMode() == 0 ? 1 : 0);
                    this.microphoneMode.displayString = I18n.format("menu.speakMode") + ": " + (this.voiceChat.getSettings().getSpeakMode() == 0 ? I18n.format("menu.speakModePushToTalk") : I18n.format("menu.speakModeToggleToTalk"));
                } else if (this.voiceChat.getSettings().getDeviceHandler().isEmpty()) {
                    this.microphoneMode.visible = false;
                    this.microphoneMode.enabled = false;
                }
                break;
            case 10:
                Sys.openURL("https://www.patreon.com/ivasik78");
                break;
            case 897:
                if (!this.dropDown.dropDownMenu)
                    this.mc.displayGuiScreen(new GuiScreenLocalMute(this, this.voiceChat));
                break;
            case 898:
                if (!this.dropDown.dropDownMenu)
                    this.mc.displayGuiScreen(new GuiScreenOptionsWizard(this.voiceChat, this));
                break;
            case 899:
                if (!this.dropDown.dropDownMenu)
                    this.mc.displayGuiScreen(new GuiScreenVoiceChatOptionsAdvanced(this.voiceChat, this));
        }
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        final int centerW = width / 2;

        this.drawDefaultBackground();
        GL11.glPushMatrix();
        GL11.glTranslatef((float) (centerW) - (float) (this.fontRenderer.getStringWidth("Voice Chat Options") / 2) * 1.5F, 0.0F, 0.0F);
        GL11.glScalef(1.5F, 1.5F, 0.0F);
        this.drawString(this.fontRenderer, "Voice Chat Options", 0, 6, -1);
        GL11.glPopMatrix();

        for (int i = 0; i < this.warningMessages.size(); ++i) {
            int warnY = i * this.fontRenderer.FONT_HEIGHT + this.height / 2 + 66 - this.fontRenderer.FONT_HEIGHT * this.warningMessages.size() / 2;
            this.drawCenteredString(this.fontRenderer, this.warningMessages.get(i), centerW, warnY, -1);
        }
        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    private boolean inBounds(int mouseX, int mouseY, int x, int y, int w, int h) {
        return mouseX >= x && mouseY >= y && mouseX < x + w && mouseY < y + h;
    }

    @Override
    public void initGui() {
        final List<Device> devices = this.voiceChat.getSettings().getDeviceHandler().getDevices();
        final int size = devices.size();
        final int centerW = this.width / 2;
        final int centerH = this.height / 2;

        final String[] array = new String[size];

        for (int heightOffset = 0; heightOffset < size; ++heightOffset) {
            array[heightOffset] = devices.get(heightOffset).getName();
        }

        this.dropDown = new GuiDropDownMenu(0, centerW - 151, centerH - 55, 148, 20, this.voiceChat.getSettings().getInputDevice() != null ? this.voiceChat.getSettings().getInputDevice().getName() : "None", array);
        this.microphoneMode = new GuiButton(5, centerW - 152, centerH + 25 - 55, 150, 20, I18n.format("menu.speakMode") + ": " + (this.voiceChat.getSettings().getSpeakMode() == 0 ? I18n.format("menu.speakModePushToTalk") : I18n.format("menu.speakModeToggleToTalk")));
        final GuiButton UIPosition = new GuiButton(4, centerW + 2, centerH + 25 - 55, 150, 20, I18n.format("menu.uiOptions"));
        this.voiceVolume = new GuiBoostSlider(910, centerW + 2, centerH - 25 - 55, "", I18n.format("menu.worldVolume") + ": " + (this.voiceChat.getSettings().getWorldVolume() == 0.0F ? I18n.format("options.off") : (int) (this.voiceChat.getSettings().getWorldVolume() * 100.0F) + "%"), 0.0F);
        this.voiceVolume.sliderValue = this.voiceChat.getSettings().getWorldVolume();
        this.boostSlider = new GuiBoostSlider(900, centerW + 2, centerH - 55, "", I18n.format("menu.boost") + ": " + ((int) (this.voiceChat.getSettings().getInputBoost() * 5.0F) <= 0 ? I18n.format("options.off") : "" + (int) (this.voiceChat.getSettings().getInputBoost() * 5.0F) + "db"), 0.0F);
        this.boostSlider.sliderValue = this.voiceChat.getSettings().getInputBoost();
        this.advancedOptions = new GuiCustomButton(899, centerW + 2, centerH + 49 - 55, 150, 20, I18n.format("menu.advancedOptions"));
        this.buttonList.add(new GuiButton(2, centerW - 152, centerH - 25 - 55, 150, 20, !this.tester.recording ? I18n.format("menu.microphoneTest") : I18n.format("menu.microphoneStopTest")));
        final GuiButton returnToGame = new GuiButton(3, centerW - 75, this.height - 34, 150, 20, I18n.format("menu.returnToGame"));
        this.buttonList.add(returnToGame);
        this.buttonList.add(new GuiButton(10, centerW - 75, this.height - 56, 150, 20, I18n.format("menu.gman.supportIvasik")));
        this.buttonList.add(this.advancedOptions);
        this.buttonList.add(new GuiCustomButton(898, centerW - 152, centerH + 49 - 55, 150, 20, I18n.format("menu.openOptionsWizard")));
        this.buttonList.add(UIPosition);
        this.buttonList.add(this.microphoneMode);
        this.buttonList.add(this.boostSlider);
        this.buttonList.add(this.voiceVolume);
        this.buttonList.add(this.mutePlayer = new GuiCustomButton(897, centerW - 152, centerH + 73 - 55, 304, 20, I18n.format("menu.mutePlayers")));
        this.buttonList.add(this.dropDown);

        if (this.voiceChat.getSettings().getDeviceHandler().isEmpty()) {
            this.dropDown.enabled = false;
            returnToGame.enabled = false;
            this.boostSlider.enabled = false;
            this.mutePlayer.enabled = false;
            this.microphoneMode.enabled = false;
            this.mutePlayer.enabled = false;
        }

        super.initGui();
        this.warningMessages = new ArrayList<>();

        if (this.voiceChat.getSettings().getDeviceHandler().isEmpty())
            this.warningMessages.add(ChatFormatting.DARK_RED + "No input devices found, add input device and restart Minecraft.");

        if (this.mc.isSingleplayer() || this.mc.world == null)
            this.warningMessages.add(ChatFormatting.RED + I18n.format("menu.warningSingleplayer"));

        if (!this.voiceChat.getClientNetwork().isConnected() && !this.mc.isSingleplayer())
            this.warningMessages.add(ChatFormatting.RED + I18n.format("Server doesn't support voice chat."));
    }

    @Override
    public void keyTyped(char character, int key) {
        if (key == 1) {
            this.voiceChat.getSettings().getConfiguration().save();
            this.mc.displayGuiScreen(null);
            this.mc.setIngameFocus();
        }
    }

    @Override
    public void mouseClicked(int mouseX, int mouseY, int btn) throws IOException {
        if (btn == 0 && this.dropDown.getMouseOverInteger() != -1 && this.dropDown.dropDownMenu && !this.voiceChat.getSettings().getDeviceHandler().isEmpty()) {
            final Device device = this.voiceChat.getSettings().getDeviceHandler().getDevices().get(this.dropDown.getMouseOverInteger());

            if (device == null)
                return;

            this.voiceChat.getSettings().setInputDevice(device);
            this.dropDown.setDisplayString(device.getName());
        }
        super.mouseClicked(mouseX, mouseY, btn);
    }

    @Override
    public void onGuiClosed() {
        if (this.tester.recording)
            this.tester.stop();
    }

    @Override
    public void updateScreen() {
        final float boost = this.voiceChat.getSettings().getInputBoost() * 5F;
        final float volume = this.voiceChat.getSettings().getWorldVolume();

        this.voiceChat.getSettings().setWorldVolume(this.voiceVolume.sliderValue);
        this.voiceChat.getSettings().setInputBoost(this.boostSlider.sliderValue);
        this.voiceVolume.setDisplayString(I18n.format("menu.worldVolume") + ": " + (volume == 0F ? I18n.format("options.off") : (int) (volume * 100.0F) + "%"));
        this.boostSlider.setDisplayString(I18n.format("menu.boost") + ": " + ((int) (boost) <= 0 ? I18n.format("options.off") : (int) (boost) + "db"));
        this.advancedOptions.allowed = !this.dropDown.dropDownMenu;
        this.mutePlayer.allowed = !this.dropDown.dropDownMenu;
    }
}