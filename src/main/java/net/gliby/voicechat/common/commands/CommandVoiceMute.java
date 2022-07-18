package net.gliby.voicechat.common.commands;

import net.gliby.voicechat.VoiceChat;
import net.gliby.voicechat.common.networking.ServerNetwork;
import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.PlayerNotFoundException;
import net.minecraft.command.WrongUsageException;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraftforge.fml.common.FMLCommonHandler;

import javax.annotation.Nullable;
import java.util.List;

public class CommandVoiceMute extends CommandBase {
    @Override
    public List<String> getTabCompletions(MinecraftServer server, ICommandSender sender, String[] args, @Nullable BlockPos targetPos) {
        return args.length == 1 ? getListOfStringsMatchingLastWord(args, this.getPlayers()) : null;
    }

    @Override
    public String getName() {
        return "vmute";
    }

    @Override
    public String getUsage(ICommandSender sender) {
        return "Usage: /vmute <player>";
    }

    protected String[] getPlayers() {
        return FMLCommonHandler.instance().getMinecraftServerInstance().getOnlinePlayerNames();
    }

    @Override
    public int getRequiredPermissionLevel() {
        return 3;
    }

    @Override
    public boolean isUsernameIndex(String[] args, int index) {
        return index == 0;
    }

    @Override
    public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws WrongUsageException, PlayerNotFoundException {
        if (args.length == 1 && args[0].length() > 0) {
            ServerNetwork network = VoiceChat.getServerInstance().getServerNetwork();
            EntityPlayerMP player = getCommandSenderAsPlayer(sender);

            if (network.getDataManager().mutedPlayers.contains(player.getUniqueID())) {
                network.getDataManager().mutedPlayers.remove(player.getUniqueID());
                notifyCommandListener(sender, this, player.getDisplayName() + " has been unmuted.", args[0]);
                player.sendMessage(new TextComponentString("You have been unmuted!"));
            } else {
                notifyCommandListener(sender, this, player.getDisplayName() + " has been muted.", args[0]);
                network.getDataManager().mutedPlayers.add(player.getUniqueID());
                player.sendMessage(new TextComponentString("You have been voice muted, you cannot talk untill you have been unmuted."));
            }
        } else {
            throw new WrongUsageException(this.getUsage(sender));
        }
    }
}