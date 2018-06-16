package net.gliby.voicechat.client.networking.game;

import net.gliby.voicechat.client.VoiceChatClient;
import net.gliby.voicechat.client.sound.ClientStreamManager;
import net.gliby.voicechat.common.PlayerProxy;
import net.minecraft.client.entity.EntityOtherPlayerMP;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.util.Map.Entry;

public class ClientEventHandler
{
    private VoiceChatClient voiceChat;

    public ClientEventHandler(VoiceChatClient voiceChatClient)
    {
        this.voiceChat = voiceChatClient;
    }

    @SubscribeEvent
    public void entityJoinWorld(final EntityJoinWorldEvent event)
    {
        if (event.world.isRemote)
        {
            (new Thread(() -> {
                if (event.entity instanceof EntityOtherPlayerMP)
                {
                    EntityOtherPlayerMP player = (EntityOtherPlayerMP) event.entity;

                    if (!VoiceChatClient.getSoundManager().playersMuted.contains(player.getEntityId()))
                    {
                        for (Object o : ClientStreamManager.playerMutedData.entrySet())
                        {
                            Entry entry = (Entry) o;
                            Integer key = (Integer) entry.getKey();
                            String value = (String) entry.getValue();

                            if (value.equals(player.getName()))
                            {
                                VoiceChatClient.getSoundManager().playersMuted.remove(key);
                                ClientStreamManager.playerMutedData.remove(key);
                                VoiceChatClient.getSoundManager().playersMuted.add(player.getEntityId());
                                ClientStreamManager.playerMutedData.put(player.getEntityId(), player.getName());
                                break;
                            }
                        }
                    }

                    PlayerProxy proxy1 = VoiceChatClient.getSoundManager().playerData.get(player.getEntityId());

                    if (proxy1 != null)
                    {
                        proxy1.setPlayer(player);
                        proxy1.setName(player.getName());
                        proxy1.usesEntity = true;
                    }
                }
            }, "Entity Join Process")).start();
        }
    }
}