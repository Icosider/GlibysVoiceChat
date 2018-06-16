package net.gliby.voicechat.client.gui;

import net.gliby.voicechat.client.VoiceChatClient;
import net.gliby.voicechat.client.sound.ClientStreamManager;
import net.minecraft.client.entity.EntityOtherPlayerMP;
import net.minecraft.client.gui.*;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.input.Keyboard;

import java.io.IOException;
import java.util.ArrayList;

public class GuiScreenLocalMute extends GuiScreen
{
    protected GuiScreen parent;
    private GuiScreenLocalMute.List listPlayers;
    private GuiTextField playerTextField;
    private boolean playerNotFound;
    private ArrayList<String> autoCompletionNames;

    public GuiScreenLocalMute(GuiScreen parent, VoiceChatClient voiceChat)
    {
        this.parent = parent;
    }

    @Override
    protected void actionPerformed(GuiButton button)
    {
        switch(button.id)
        {
            case 0:
                this.playerNotFound = false;
                EntityPlayer entityPlayer = this.mc.theWorld.getPlayerEntityByName(this.playerTextField.getText().trim().replaceAll(" ", ""));
                if (entityPlayer != null)
                {
                    if (!entityPlayer.isServerWorld() && !VoiceChatClient.getSoundManager().playersMuted.contains(entityPlayer.getEntityId())) {
                        VoiceChatClient.getSoundManager().playersMuted.add(entityPlayer.getEntityId());
                        ClientStreamManager.playerMutedData.put(entityPlayer.getEntityId(), entityPlayer.getName());
                    }
                }
                else {
                    this.playerNotFound = true;
                }
                break;
            case 1:
                this.mc.displayGuiScreen(this.parent);
        }
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float ticks)
    {
        this.listPlayers.drawScreen(mouseX, mouseY, ticks);
        this.drawCenteredString(this.fontRendererObj, I18n.format("menu.mutedPlayers"), this.width / 2, 16, -1);

        if (this.playerNotFound)
        {
            this.drawCenteredString(this.fontRendererObj, "§c" + I18n.format("commands.generic.player.notFound"), this.width / 2, this.height - 59, -1);
        }
        this.playerTextField.drawTextBox();
        super.drawScreen(mouseX, mouseY, ticks);
    }

    @Override
    public void initGui()
    {
        Keyboard.enableRepeatEvents(true);
        this.autoCompletionNames = new ArrayList<>();
        this.playerTextField = new GuiTextField(0, this.fontRendererObj, this.width / 2 - 100, this.height - 57 - -9, 130, 20);
        this.playerTextField.setFocused(true);
        this.buttonList.add(new GuiOptionButton(0, this.width / 2 + 32, this.height - 57 - -9, 98, 20, I18n.format("menu.add")));
        this.buttonList.add(new GuiOptionButton(1, this.width / 2 - 75, this.height - 32 - -9, I18n.format("gui.done")));
        this.listPlayers = new GuiScreenLocalMute.List();
        this.listPlayers.registerScrollButtons(7, 8);
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException
    {
        super.mouseClicked(mouseX, mouseY, mouseButton);
        playerTextField.mouseClicked(mouseX, mouseY, mouseButton);
    }

    @Override
    public void handleMouseInput() throws IOException
    {
        super.handleMouseInput();
        listPlayers.handleMouseInput();
    }

    @Override
    protected void keyTyped(char character, int key) throws IOException
    {
        this.playerNotFound = false;
        this.playerTextField.textboxKeyTyped(character, key);
        super.keyTyped(character, key);

        switch (key)
        {
            case 15:
                if (this.autoCompletionNames.size() > 0)
                {
                    this.shuffleCompleition();
                }
                else {
                    this.autoCompletionNames.clear();
                    java.util.List<EntityPlayer> players = this.mc.theWorld.playerEntities;

                    for (EntityPlayer player : players)
                    {
                        if (player instanceof EntityOtherPlayerMP)
                        {
                            String name = player.getName();

                            if (name.toLowerCase().startsWith(this.playerTextField.getText().toLowerCase().trim().replaceAll(" ", "")))
                            {
                                this.autoCompletionNames.add(name);
                            }
                        }
                    }
                    this.shuffleCompleition();
                }
                break;
            case 28:
                this.actionPerformed(null);
                break;
            default:
                this.autoCompletionNames.clear();
        }

    }

    @Override
    public void onGuiClosed()
    {
        super.onGuiClosed();
        Keyboard.enableRepeatEvents(false);
    }

    private void shuffleCompleition()
    {
        if (this.autoCompletionNames.iterator().hasNext())
        {
            String name = this.autoCompletionNames.iterator().next();
            this.autoCompletionNames.add(name);
            this.playerTextField.setText(name);
            this.autoCompletionNames.remove(name);
        }
    }

    @Override
    public void updateScreen()
    {
        this.playerTextField.updateCursorCounter();
    }

    @SideOnly(Side.CLIENT)
    class List extends GuiSlot
    {
        public List()
        {
            super(GuiScreenLocalMute.this.mc, GuiScreenLocalMute.this.width, GuiScreenLocalMute.this.height, 32, GuiScreenLocalMute.this.height - 65 + 4, 18);
        }

        @Override
        protected void drawBackground()
        {
            GuiScreenLocalMute.this.drawDefaultBackground();
        }

        @Override
        protected void drawSlot(int entryID, int insideLeft, int yPos, int insideSlotHeight, int mouseXIn, int mouseYIn)
        {
            GuiScreenLocalMute guiScreenLocalMute = GuiScreenLocalMute.this;
            FontRenderer fr = GuiScreenLocalMute.this.fontRendererObj;
            guiScreenLocalMute.drawCenteredString(fr, ClientStreamManager.playerMutedData.get(VoiceChatClient.getSoundManager().playersMuted.get(entryID)), super.width / 2, yPos + 1, 16777215);
            GuiScreenLocalMute.this.drawCenteredString(GuiScreenLocalMute.this.fontRendererObj, "§lX", super.width / 2 + 88, yPos + 3, 16711680);
        }

        @Override
        protected void elementClicked(int index, boolean doubleClick, int x, int y)
        {
            VoiceChatClient.getSoundManager().playersMuted.remove(index);
            ClientStreamManager.playerMutedData.remove(index);
        }

        @Override
        protected int getContentHeight()
        {
            return this.getSize() * 18;
        }

        @Override
        protected int getSize()
        {
            return VoiceChatClient.getSoundManager().playersMuted.size();
        }

        @Override
        protected boolean isSelected(int p_148131_1_)
        {
            return true;
        }
    }
}