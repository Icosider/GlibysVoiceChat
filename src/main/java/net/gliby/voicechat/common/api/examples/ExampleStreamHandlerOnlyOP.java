package net.gliby.voicechat.common.api.examples;

import net.gliby.voicechat.common.api.VoiceChatAPI;
import net.gliby.voicechat.common.api.events.ServerStreamEvent;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.text.TextComponentString;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.util.List;

public class ExampleStreamHandlerOnlyOP {
    public ExampleStreamHandlerOnlyOP() {
        VoiceChatAPI.instance().setCustomStreamHandler(this);
    }

    @SubscribeEvent
    public void createStream(ServerStreamEvent.StreamCreated event) {
        if (!this.isOP(event.stream.player)) {
            event.stream.player.sendMessage(new TextComponentString("Only OP's are allowed to talk!"));
        }
    }

    @SubscribeEvent
    public void feedStream(ServerStreamEvent.StreamFeed event) {
        final List<EntityPlayer> players = event.stream.player.mcServer.getEntityWorld().playerEntities;
        final EntityPlayerMP speaker = event.stream.player;

        if (this.isOP(speaker)) {
            for (EntityPlayer player : players) {
                if (this.isOP((EntityPlayerMP) player) && player.getEntityId() != speaker.getEntityId())
                    event.streamManager.feedStreamToPlayer(event.stream, event.voiceLet, (EntityPlayerMP) player, false);
            }
        }

    }

    private boolean isOP(EntityPlayerMP player) {
        return player.mcServer.getPlayerList().getOppedPlayers().getEntry(player.getGameProfile()).getPermissionLevel() == 4;
    }
}