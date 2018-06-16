package net.gliby.voicechat.client.sound.thread;

import net.gliby.voicechat.client.VoiceChatClient;
import net.gliby.voicechat.client.sound.ClientStream;
import net.gliby.voicechat.client.sound.ClientStreamManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.SoundManager.SoundSystemStarterThread;
import org.lwjgl.util.vector.Vector3f;

public class ThreadUpdateStream implements Runnable
{
    private final Minecraft mc;
    private final VoiceChatClient voiceChat;
    private final ClientStreamManager manager;

    public ThreadUpdateStream(ClientStreamManager manager, VoiceChatClient voiceChatClient)
    {
        this.manager = manager;
        this.mc = Minecraft.getMinecraft();
        this.voiceChat = voiceChatClient;
    }

    public void run()
    {
        while (true)
        {
            if (VoiceChatClient.getSoundManager().currentStreams.isEmpty())
            {
                try
                {
                    synchronized (this)
                    {
                        this.wait(2L);
                    }
                }
                catch (InterruptedException e)
                {
                    e.printStackTrace();
                }
            }
            else {
                for (int e1 = 0; e1 < VoiceChatClient.getSoundManager().currentStreams.size(); ++e1)
                {
                    SoundSystemStarterThread sndSystem = this.mc.getSoundHandler().sndManager.sndSystem;
                    final ClientStream stream = (ClientStream)VoiceChatClient.getSoundManager().currentStreams.get(e1);
                    String source = stream.generateSource();

                    if ((stream.needsEnd || stream.getLastTimeUpdatedMS() > 325) && !sndSystem.playing(source))
                    {
                        this.manager.killStream(stream);
                    }

                    if (stream.dirty)
                    {
                        if (stream.volume >= 0)
                        {
                            sndSystem.setVolume(source, this.voiceChat.getSettings().getWorldVolume() * (float)stream.volume * 0.01F);
                        }
                        else {
                            sndSystem.setVolume(source, this.voiceChat.getSettings().getWorldVolume());
                        }

                        sndSystem.setAttenuation(source, 2);
                        sndSystem.setDistOrRoll(source, (float)this.voiceChat.getSettings().getSoundDistance());
                        stream.dirty = false;
                    }

                    if (stream.direct)
                    {
                        Vector3f vector = stream.player.position();
                        sndSystem.setPosition(source, vector.x, vector.y, vector.z);
                    }
                    else {
                        sndSystem.setPosition(source, (float)this.mc.thePlayer.posX, (float)this.mc.thePlayer.posY, (float)this.mc.thePlayer.posZ);
                    }

                    if (stream.volume >= 0)
                    {
                        sndSystem.setVolume(source, this.voiceChat.getSettings().getWorldVolume() * (float)stream.volume * 0.01F);
                    }
                    Minecraft.getMinecraft().addScheduledTask(() -> stream.player.update(ThreadUpdateStream.this.mc.theWorld));
                }

                try
                {
                    synchronized (this)
                    {
                        this.wait(34L);
                    }
                }
                catch (InterruptedException e)
                {
                    e.printStackTrace();
                }
            }
        }
    }
}