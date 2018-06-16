package net.gliby.voicechat.common.commands;

import net.gliby.voicechat.VoiceChat;
import net.gliby.voicechat.common.networking.ServerStream;
import net.gliby.voicechat.common.networking.ServerStreamManager;
import net.minecraft.command.*;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.BlockPos;
import net.minecraft.util.ChatComponentText;
import java.util.List;

public class CommandChatMode extends CommandBase
{
    @Override
    public List<String> addTabCompletionOptions(ICommandSender sender, String[] args, BlockPos pos)
    {
        return args.length == 1 ? getListOfStringsMatchingLastWord(args, "distance", "global", "world") : (args.length == 2 ? getListOfStringsMatchingLastWord(args, this.getListOfPlayerUsernames()) : null);
    }

    private String getChatMode(int chatMode)
    {
        return chatMode == 0 ? "distance" : (chatMode == 2 ? "global" : (chatMode == 1 ? "world" : "distance"));
    }

    private int getChatModeFromCommand(ICommandSender par1ICommandSender, String par2Str)
    {
        return !par2Str.equalsIgnoreCase("distance") && !par2Str.startsWith("d") && !par2Str.equalsIgnoreCase("0")?(!par2Str.equalsIgnoreCase("world") && !par2Str.startsWith("w") && !par2Str.equalsIgnoreCase("1")?(!par2Str.equalsIgnoreCase("global") && !par2Str.startsWith("g") && !par2Str.equalsIgnoreCase("2")?0:2):1):0;
    }

    @Override
    public String getCommandName()
    {
        return "vchatmode";
    }

    @Override
    public String getCommandUsage(ICommandSender par1ICommandSender)
    {
        return "/vchatmode <mode> or /vchatmode <mode> [player]";
    }

    private String[] getListOfPlayerUsernames()
    {
        return MinecraftServer.getServer().getAllUsernames();
    }

    @Override
    public int getRequiredPermissionLevel()
    {
        return 3;
    }

    @Override
    public boolean isUsernameIndex(String[] par1ArrayOfStr, int par2)
    {
        return par2 == 1;
    }

    @Override
    public void processCommand(ICommandSender sender, String[] args) throws WrongUsageException, PlayerNotFoundException
    {
        if (args.length > 0)
        {
            int chatMode = this.getChatModeFromCommand(sender, args[0]);
            EntityPlayerMP player = getCommandSenderAsPlayer(sender);

            if (player != null)
            {
                ServerStreamManager dataManager = VoiceChat.getServerInstance().getServerNetwork().getDataManager();
                dataManager.chatModeMap.put(player.getPersistentID(), chatMode);
                ServerStream stream = dataManager.getStream(player.getEntityId());

                if (stream != null)
                {
                    stream.dirty = true;
                }

                if (player != sender)
                {
                    notifyOperators(sender, this, player.getName() + " set chat mode to " + this.getChatMode(chatMode).toUpperCase() + " (" + chatMode + ")", args[0]);
                }
                else {
                    player.addChatMessage(new ChatComponentText("Set own chat mode to " + this.getChatMode(chatMode).toUpperCase() + " (" + chatMode + ")"));

                    switch (chatMode)
                    {
                        case 0:
                            player.addChatMessage(new ChatComponentText("Only players near you can hear you."));
                            break;
                        case 1:
                            player.addChatMessage(new ChatComponentText("Every player in this world can hear you"));
                            break;
                        case 2:
                            player.addChatMessage(new ChatComponentText("Every player can hear you."));
                    }
                }
            }
            else {
                throw new WrongUsageException("commands.generic.player.notFound");
            }
        }
        else {
            throw new WrongUsageException(this.getCommandUsage(null));
        }
    }
}