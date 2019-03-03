package net.gliby.voicechat.client.sound;

import net.gliby.voicechat.client.VoiceChatClient;
import net.gliby.voicechat.client.debug.Statistics;
import net.minecraft.client.Minecraft;
import org.xiph.speex.SpeexDecoder;

import java.io.StreamCorruptedException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class SoundPreProcessor
{
    private VoiceChatClient voiceChat;
    private Statistics stats;
    private SpeexDecoder decoder;
    private byte[] buffer;

    private static List<byte[]> divideArray(byte[] source, int chunksize)
    {
        List<byte[]> result = new ArrayList<>();

        for (int start = 0; start < source.length; start += chunksize)
        {
            int end = Math.min(source.length, start + chunksize);
            result.add(Arrays.copyOfRange(source, start, end));
        }
        return result;
    }

    SoundPreProcessor(VoiceChatClient voiceChat, Minecraft mc)
    {
        this.voiceChat = voiceChat;
        this.stats = VoiceChatClient.getStatistics();
    }

    public boolean process(int id, byte[] encodedSamples, int chunkSize, boolean direct, byte volume)
    {
        if (chunkSize > encodedSamples.length)
        {
            VoiceChatClient.getLogger().fatal("Sound Pre-Processor has been given incorrect data from network, sample pieces cannot be bigger than whole sample. ");
            return false;
        }
        else {
            if (this.decoder == null)
            {
                this.decoder = new SpeexDecoder();
                this.decoder.init(0, (int)ClientStreamManager.getUniversalAudioFormat().getSampleRate(), ClientStreamManager.getUniversalAudioFormat().getChannels(), this.voiceChat.getSettings().isPerceptualEnchantmentAllowed());
            }

            byte[] buf;

            if (encodedSamples.length <= chunkSize)
            {
                try
                {
                    this.decoder.processData(encodedSamples, 0, encodedSamples.length);
                }
                catch (StreamCorruptedException e)
                {
                    e.printStackTrace();
                    return false;
                }

                buf = new byte[this.decoder.getProcessedDataByteSize()];
                this.decoder.getProcessedData(buf, 0);
            }
            else {
                List<byte[]> samplesList = divideArray(encodedSamples, chunkSize);
                this.buffer = new byte[0];

                for (byte[] sample : samplesList)
                {
                    SpeexDecoder tempDecoder = new SpeexDecoder();
                    tempDecoder.init(0, (int) ClientStreamManager.getUniversalAudioFormat().getSampleRate(), ClientStreamManager.getUniversalAudioFormat().getChannels(), this.voiceChat.getSettings().isPerceptualEnchantmentAllowed());

                    try
                    {
                        this.decoder.processData(sample, 0, sample.length);
                    }
                    catch (StreamCorruptedException e)
                    {
                        e.printStackTrace();
                        return false;
                    }

                    byte[] sampleBuffer = new byte[this.decoder.getProcessedDataByteSize()];
                    this.decoder.getProcessedData(sampleBuffer, 0);
                    this.write(sampleBuffer);
                }
                buf = this.buffer;
            }

            if (buf != null) {
                VoiceChatClient.getSoundManager().addQueue(buf, direct, id, volume);

                if (this.stats != null)
                {
                    this.stats.addEncodedSamples(encodedSamples.length);
                    this.stats.addDecodedSamples(buf.length);
                }
                this.buffer = new byte[0];
                return true;
            }
            return false;
        }
    }

    private void write(byte[] write)
    {
        byte[] result = new byte[this.buffer.length + write.length];
        System.arraycopy(this.buffer, 0, result, 0, this.buffer.length);
        System.arraycopy(write, 0, result, this.buffer.length, write.length);
        this.buffer = result;
    }
}