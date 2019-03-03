package net.gliby.voicechat.client.networking.game;

import net.gliby.voicechat.client.VoiceChatClient;
import net.gliby.voicechat.client.sound.ClientStreamManager;
import net.gliby.voicechat.common.PlayerProxy;
import net.minecraft.client.entity.EntityOtherPlayerMP;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.util.Map.Entry;

public class ClientEventHandler {
    private VoiceChatClient voiceChat;

    public ClientEventHandler(VoiceChatClient voiceChatClient) {
        this.voiceChat = voiceChatClient;
    }

    @SubscribeEvent
    public void entityJoinWorld(final EntityJoinWorldEvent event) {
        if (event.getWorld().isRemote) {
            (new Thread(() -> {
                if (event.getEntity() instanceof EntityOtherPlayerMP) {
                    EntityOtherPlayerMP player = (EntityOtherPlayerMP)event.getEntity();

                    if (!VoiceChatClient.getSoundManager().playersMuted.contains(player.getEntityId())) {
                        VoiceChatClient.getSoundManager();

                        for (Object o : ClientStreamManager.playerMutedData.entrySet()) {
                            Entry entry = (Entry) o;
                            Integer key = (Integer) entry.getKey();
                            String value = (String) entry.getValue();

                            if (value.equals(player.getName())) {
                                VoiceChatClient.getSoundManager().playersMuted.remove(key);
                                VoiceChatClient.getSoundManager();
                                ClientStreamManager.playerMutedData.remove(key);
                                VoiceChatClient.getSoundManager().playersMuted.add(player.getEntityId());
                                VoiceChatClient.getSoundManager();
                                ClientStreamManager.playerMutedData.put(player.getEntityId(), player.getName());
                                break;
                            }
                        }
                    }

                    final PlayerProxy proxy = VoiceChatClient.getSoundManager().playerData.get(player.getEntityId());

                    if (proxy != null) {
                        proxy.setPlayer(player);
                        proxy.setName(player.getName());
                        proxy.usesEntity = true;
                    }
                }
            }, "Entity Join Process")).start();
        }
    }
}