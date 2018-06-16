package net.gliby.voicechat.client;

import net.gliby.voicechat.VoiceChat;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.ModContainer;
import paulscode.sound.SoundSystemConfig;
import paulscode.sound.codecs.CodecJOrbis;
import paulscode.sound.codecs.CodecWav;
import paulscode.sound.libraries.LibraryLWJGLOpenAL;

import java.util.Iterator;

class UpdatedSoundManager
{
    UpdatedSoundManager(VoiceChatClient voiceChatClient)
    {
        Iterator iter = Loader.instance().getModList().iterator();

        ModContainer mod;

        do {
            if (!iter.hasNext())
            {
                try
                {
                    SoundSystemConfig.removeLibrary(LibraryLWJGLOpenAL.class);
                    SoundSystemConfig.addLibrary(ovr.paulscode.sound.libraries.LibraryLWJGLOpenAL.class);
                    SoundSystemConfig.setCodec("ogg", CodecJOrbis.class);
                    SoundSystemConfig.setCodec("wav", CodecWav.class);
                }
                catch (Exception e)
                {
                    VoiceChat.getLogger().info("Failed to replaced sound libraries, you won\'t be hearing any voice chat.");
                    e.printStackTrace();
                }
                VoiceChat.getLogger().info("Successfully replaced sound libraries.");
                return;
            }
            mod = (ModContainer)iter.next();
        }
        while (!mod.getModId().equals("soundfilters"));
        VoiceChat.getLogger().info("Found Sound Filters mod, won\'t replace OpenAL library.");
    }
}