package net.gliby.voicechat.client.gui.options;

import com.mojang.realmsclient.gui.ChatFormatting;
import net.gliby.voicechat.client.VoiceChatClient;
import net.gliby.voicechat.client.device.Device;
import net.gliby.voicechat.client.gui.GuiBoostSlider;
import net.gliby.voicechat.client.gui.GuiCustomButton;
import net.gliby.voicechat.client.gui.GuiDropDownMenu;
import net.gliby.voicechat.client.keybindings.EnumBinding;
import net.gliby.voicechat.client.sound.MicrophoneTester;
import net.gliby.voicechat.client.textures.IndependentGUITexture;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.resources.I18n;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import static org.lwjgl.opengl.GL11.*;

public class GuiScreenOptionsWizard extends GuiScreen {
    private final VoiceChatClient voiceChat;
    private final GuiScreen parent;
    private boolean dirty;
    private String[] textBatch;
    private GuiDropDownMenu dropDown;
    private final MicrophoneTester tester;
    private GuiCustomButton nextButton;
    private GuiCustomButton previousButton;
    private GuiCustomButton doneButton;
    private GuiBoostSlider boostSlider;
    private final Map<GuiButton, Integer> buttonMap = new HashMap<>();
    private int currentPage = 1;
    private int lastPage = -1;
    private String title = "Voice Chat Setup Wizard.";
    private String text = "";

    public GuiScreenOptionsWizard(VoiceChatClient voiceChat, GuiScreen parent) {
        this.voiceChat = voiceChat;
        this.parent = parent;
        this.tester = new MicrophoneTester(voiceChat);
    }

    @Override
    public void actionPerformed(GuiButton button) {
        if ((button == this.nextButton || button == this.previousButton || this.doneButton == button || this.buttonMap.get(button) != null && this.buttonMap.get(button) == this.currentPage) && !this.dropDown.dropDownMenu) {
            switch (button.id) {
                case 0:
                    if (this.currentPage < 4)
                        ++this.currentPage;
                    break;
                case 1:
                    if (this.currentPage >= 2)
                        --this.currentPage;
                    break;
                case 2:
                    if (this.currentPage == 4) {
                        this.voiceChat.getSettings().setSetupNeeded(false);
                        this.mc.displayGuiScreen(null);
                    }
                    break;
                case 3:
                    this.voiceChat.getSettings().setSetupNeeded(false);
                    this.mc.displayGuiScreen(this.parent);
            }
        }
    }

    private void drawPage(int mouseX, int mouseY, float partialTicks) {
        final int centerW = this.width / 2;
        final int centerH = this.height / 2;

        if (this.tester.recording && this.currentPage != 3)
            this.tester.stop();

        if (this.currentPage != 2 && this.dropDown.dropDownMenu)
            this.dropDown.dropDownMenu = false;

        if (!this.text.equals(this.textBatch[this.currentPage - 1]))
            this.text = this.textBatch[this.currentPage - 1];

        switch (this.currentPage) {
            case 1:
                this.title = "Gliby's Voice Chat " + I18n.format("menu.setupWizard");
                break;
            case 2:
                this.title = I18n.format("menu.selectInputDevice");
                this.dropDown.drawButton(this.mc, mouseX, mouseY, partialTicks);
                break;
            case 3:
                if (this.lastPage != this.currentPage)
                    this.tester.start();

                this.title = I18n.format("menu.adjustMicrophone");
                glPushMatrix();
                glEnable(GL_BLEND);
                OpenGlHelper.glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA, GL_ONE, GL_ZERO);
                glDisable(GL_ALPHA_TEST);
                glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
                glTranslatef((float) (centerW) - 39.75F, (float) (centerH) - 67.5F, 0.0F);
                glScalef(2.0F, 2.0F, 0.0F);
                IndependentGUITexture.GUI_WIZARD.bindTexture(this.mc);
                this.drawTexturedModalRect(0, 0, 0, 127, 35, 20);
                int progress = (int) this.tester.currentAmplitude;
                this.drawTexturedModalRect(3.35F, 0.0F, 35, 127, progress, 20);
                glEnable(GL_ALPHA_TEST);
                glPopMatrix();
                this.drawCenteredString(this.fontRenderer, I18n.format("menu.boostVoiceVolume"), centerW, centerH - 26, -1);
                break;
            case 4:
                this.title = I18n.format("menu.finishWizard");
        }
        this.lastPage = this.currentPage;
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        final int centerW = this.width / 2;
        final int centerH = this.height / 2;

        this.drawDefaultBackground();
        IndependentGUITexture.GUI_WIZARD.bindTexture(this.mc);
        glPushMatrix();
        glTranslatef((float) (centerW) - 142.5F, (float) (centerH) - 94.5F, 0.0F);
        glScalef(1.5F, 1.5F, 0.0F);
        this.drawTexturedModalRect(0, 0, 0, 0, 190, 127);
        glPopMatrix();
        this.drawString(this.mc.fontRenderer, this.currentPage + "/" + 4, centerW + 108, centerH + 67, -1);

        if (this.title != null)
            this.drawString(this.mc.fontRenderer, ChatFormatting.BOLD + this.title, centerW - this.mc.fontRenderer.getStringWidth(this.title) / 2 - 12, centerH - 80, -1);

        if (this.text != null) {
            this.fontRenderer.drawSplitString(ChatFormatting.stripFormatting(this.text), centerW - 107 - 1 + 1, centerH - 65 + 1, 230, 0);
            this.fontRenderer.drawSplitString(this.text, centerW - 107 - 1, centerH - 65, 230, -1);
        }

        for (final GuiButton button : this.buttonList) {
            if (button == this.nextButton || button == this.previousButton || button == this.doneButton || this.buttonMap.get(button) != null && this.buttonMap.get(button) == this.currentPage)
                button.drawButton(this.mc, mouseX, mouseY, partialTicks);
        }
        this.drawPage(mouseX, mouseY, partialTicks);
    }

    @Override
    public void initGui() {
        final List<Device> devices = this.voiceChat.getSettings().getDeviceHandler().getDevices();
        final int size = devices.size();
        final String[] array = new String[size];

        final int centerW = this.width / 2;
        final int centerH = this.height / 2;

        for (int i = 0; i < size; ++i) {
            array[i] = devices.get(i).getName();
        }

        this.dropDown = new GuiDropDownMenu(-1, centerW - 75, centerH - 55, 150, 20, this.voiceChat.getSettings().getInputDevice() != null ? this.voiceChat.getSettings().getInputDevice().getName() : "None", array);
        this.buttonList.add(this.nextButton = new GuiCustomButton(0, centerW - 90, centerH + 60, 180, 20, I18n.format("menu.next") + " ->"));
        this.buttonList.add(this.previousButton = new GuiCustomButton(1, centerW - 90, centerH, 180, 20, "<- " + I18n.format("menu.previous")));
        this.buttonList.add(this.doneButton = new GuiCustomButton(2, centerW - 90, centerH, 180, 20, I18n.format("gui.done")));
        GuiCustomButton backButton;
        this.buttonList.add(backButton = new GuiCustomButton(3, centerW - 90, centerH + 18, 180, 20, I18n.format("gui.back")));
        this.buttonList.add(this.boostSlider = new GuiBoostSlider(900, centerW - 75, centerH - 15, "", I18n.format("menu.boost") + ": " + ((int) (this.voiceChat.getSettings().getInputBoost() * 5.0F) <= 0 ? I18n.format("options.off") : "" + (int) (this.voiceChat.getSettings().getInputBoost() * 5.0F) + "db"), 0.0F));
        this.boostSlider.sliderValue = this.voiceChat.getSettings().getInputBoost();
        this.doneButton.visible = false;
        this.buttonMap.put(backButton, 1);
        this.buttonMap.put(this.boostSlider, 3);
        this.dirty = true;
        this.textBatch = new String[]{I18n.format("menu.setupWizardPageOne").replaceAll(Pattern.quote("$n"), "\n").replaceAll(Pattern.quote("$a"), this.voiceChat.keyManager.getKeyName(EnumBinding.OPEN_GUI_OPTIONS)), I18n.format("menu.setupWizardPageTwo").replaceAll(Pattern.quote("$n"), "\n"), I18n.format("menu.setupWizardPageThree").replaceAll(Pattern.quote("$n"), "\n"), I18n.format("menu.setupWizardPageFour").replaceAll(Pattern.quote("$n"), "\n").replaceAll(Pattern.quote("$a"), this.voiceChat.keyManager.getKeyName(EnumBinding.OPEN_GUI_OPTIONS)).replaceAll(Pattern.quote("$b"), this.voiceChat.keyManager.getKeyName(EnumBinding.SPEAK))};
    }

    @Override
    public void mouseClicked(int x, int y, int b) throws IOException {
        if (this.currentPage == 2) {
            if (this.dropDown.getMouseOverInteger() != -1 && this.dropDown.dropDownMenu && !this.voiceChat.getSettings().getDeviceHandler().isEmpty()) {
                final Device mic = this.voiceChat.getSettings().getDeviceHandler().getDevices().get(this.dropDown.getMouseOverInteger());

                if (mic != null) {
                    this.voiceChat.getSettings().setInputDevice(mic);
                    this.dropDown.setDisplayString(mic.getName());
                }
            }

            if (this.dropDown.mousePressed(this.mc, x, y) && b == 0) {
                this.dropDown.playPressSound(this.mc.getSoundHandler());
                this.dropDown.dropDownMenu = !this.dropDown.dropDownMenu;
            }
        }

        if (b == 0) {
            for (final GuiButton button : this.buttonList) {
                if ((button == this.nextButton || button == this.previousButton || this.doneButton == button || this.buttonMap.get(button) != null && this.buttonMap.get(button) == this.currentPage) && button.mousePressed(this.mc, x, y))
                    super.mouseClicked(x, y, b);
            }
        }
    }

    @Override
    public void onGuiClosed() {
        if (this.tester.recording)
            this.tester.stop();
        this.voiceChat.getSettings().getConfiguration().save();
    }

    @Override
    public void updateScreen() {
        final float boost = this.voiceChat.getSettings().getInputBoost() * 5F;

        this.boostSlider.setDisplayString(I18n.format("menu.boost") + ": " + ((int) (boost) <= 0 ? I18n.format("options.off") : "" + (int) (boost) + "db"));
        this.voiceChat.getSettings().setInputBoost(this.boostSlider.sliderValue);

        if (this.lastPage != this.currentPage || this.dirty) {
            switch (this.currentPage) {
                case 1:
                case 2:
                    this.previousButton.visible = false;
                    this.doneButton.visible = false;
                    this.nextButton.x = this.width / 2 - 90;
                    this.nextButton.y = this.height / 2 + 60;
                    this.nextButton.setWidth(180);
                    this.nextButton.setHeight(20);
                    break;
                case 4:
                    this.nextButton.visible = false;
                    this.doneButton.visible = true;
                    this.doneButton.x = this.width / 2;
                    this.doneButton.y = this.height / 2 + 60;
                    this.doneButton.setWidth(95);
                    this.doneButton.setHeight(20);
                    this.previousButton.x = this.width / 2 - 95;
                    this.previousButton.y = this.height / 2 + 60;
                    this.previousButton.setWidth(95);
                    this.previousButton.setHeight(20);
                    break;
                default:
                    this.previousButton.visible = true;
                    this.nextButton.visible = true;
                    this.doneButton.visible = false;
                    this.nextButton.x = this.width / 2;
                    this.nextButton.y = this.height / 2 + 60;
                    this.nextButton.setWidth(95);
                    this.nextButton.setHeight(20);
                    this.previousButton.x = this.width / 2 - 95;
                    this.previousButton.y = this.height / 2 + 60;
                    this.previousButton.setWidth(95);
                    this.previousButton.setHeight(20);
                    break;
            }
            this.dirty = false;
        }
    }
}