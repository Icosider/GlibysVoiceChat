package net.gliby.voicechat.common.api.examples;

import net.gliby.voicechat.common.api.VoiceChatAPI;
import net.gliby.voicechat.common.api.events.ServerStreamEvent;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.ChatComponentText;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.util.List;

public class ExampleStreamHandlerOnlyOP
{
    public ExampleStreamHandlerOnlyOP()
    {
        VoiceChatAPI.instance().setCustomStreamHandler(this);
    }

    @SubscribeEvent
    public void createStream(ServerStreamEvent.StreamCreated event)
    {
        if (!this.isOP(event.stream.player))
        {
            event.stream.player.addChatMessage(new ChatComponentText("Only OP\'s are allowed to talk!"));
        }
    }

    @SubscribeEvent
    public void feedStream(ServerStreamEvent.StreamFeed event)
    {
        List<EntityPlayer> players = event.stream.player.mcServer.getEntityWorld().playerEntities;
        EntityPlayerMP speaker = event.stream.player;

        if (this.isOP(speaker))
        {
            for (EntityPlayer player : players)
            {
                if (this.isOP((EntityPlayerMP) player) && player.getEntityId() != speaker.getEntityId())
                {
                    event.streamManager.feedStreamToPlayer(event.stream, event.voiceLet, (EntityPlayerMP) player, false);
                }
            }
        }

    }

    private boolean isOP(EntityPlayerMP player)
    {
        return player.mcServer.getConfigurationManager().getOppedPlayers().getEntry(player.getGameProfile()).getPermissionLevel() == 4;
    }
}