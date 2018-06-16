package net.gliby.voicechat.client.sound;

import net.gliby.voicechat.VoiceChat;
import net.gliby.voicechat.client.VoiceChatClient;
import net.gliby.voicechat.common.MathUtility;
import org.xiph.speex.SpeexEncoder;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.TargetDataLine;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class Recorder implements Runnable
{
    private boolean recording;
    private Thread thread;
    private final VoiceChatClient voiceChat;
    private byte[] buffer;

    public Recorder(VoiceChatClient voiceChat)
    {
        this.voiceChat = voiceChat;
    }

    private byte[] boostVolume(byte[] data)
    {
        ByteBuffer buf = ByteBuffer.wrap(data).order(ByteOrder.LITTLE_ENDIAN);
        ByteBuffer newBuf = ByteBuffer.allocate(data.length).order(ByteOrder.LITTLE_ENDIAN);

        while (buf.hasRemaining())
        {
            int sample = buf.getShort();
            sample *= (int)(this.voiceChat.getSettings().getInputBoost() * 5.0F) + 1;
            newBuf.putShort((short)(sample));
        }
        return newBuf.array();
    }

    public void run()
    {
        AudioFormat format = ClientStreamManager.getUniversalAudioFormat();
        TargetDataLine recordingLine = this.voiceChat.getSettings().getInputDevice().getLine();

        if (recordingLine == null)
        {
            VoiceChat.getLogger().fatal("Attempted to record input device, but failed! Java Sound System hasn\'t found any microphones, check your input devices and restart Minecraft.");
        }
        else if (!this.startLine(recordingLine))
        {
            this.voiceChat.setRecorderActive(false);
            this.stop();
        }
        else {
            SpeexEncoder encoder = new SpeexEncoder();
            encoder.init(0, (int)MathUtility.clamp(MathUtility.clamp((float)((int)(this.voiceChat.getSettings().getEncodingQuality() * 10.0F)), 1.0F, 9.0F), (float)this.voiceChat.getSettings().getMinimumQuality(), (float)this.voiceChat.getSettings().getMaximumQuality()), (int)format.getSampleRate(), format.getChannels());
            int blockSize = encoder.getFrameSize() * format.getChannels() * 2;
            byte[] normBuffer = new byte[blockSize * 2];
            recordingLine.start();
            this.buffer = new byte[0];
            byte pieceSize = 0;

            while (this.recording && this.voiceChat.getClientNetwork().isConnected())
            {
                int read = recordingLine.read(normBuffer, 0, blockSize);

                if (read == -1)
                {
                    break;
                }

                byte[] boostedBuffer = this.boostVolume(normBuffer);

                if (!encoder.processData(boostedBuffer, 0, blockSize))
                {
                    break;
                }
                int encoded = encoder.getProcessedData(boostedBuffer, 0);
                byte[] encoded_data = new byte[encoded];
                System.arraycopy(boostedBuffer, 0, encoded_data, 0, encoded);
                pieceSize = (byte)encoded;
                this.write(encoded_data);

                if (this.buffer.length >= this.voiceChat.getSettings().getBufferSize())
                {
                    this.voiceChat.getClientNetwork().sendSamples(pieceSize, this.buffer, false);
                    this.buffer = new byte[0];
                }
            }

            if (this.buffer.length > 0)
            {
                this.voiceChat.getClientNetwork().sendSamples(pieceSize, this.buffer, false);
            }
            this.voiceChat.getClientNetwork().sendSamples((byte)0, null, true);
            recordingLine.stop();
            recordingLine.close();
        }
    }

    public void set(boolean toggle)
    {
        if (toggle)
        {
            this.start();
        }
        else {
            this.stop();
        }
    }

    public void start()
    {
        this.thread = new Thread(this, "Input Device Recorder");
        this.recording = true;
        this.thread.start();
    }

    private boolean startLine(TargetDataLine recordingLine)
    {
        try
        {
            recordingLine.open();
            return true;
        }
        catch (LineUnavailableException e)
        {
            e.printStackTrace();
            VoiceChat.getLogger().fatal("Failed to open recording line! " + recordingLine.getFormat());
            return false;
        }
    }

    public void stop()
    {
        this.recording = false;
        this.thread = null;
    }

    private void write(byte[] write)
    {
        byte[] result = new byte[this.buffer.length + write.length];
        System.arraycopy(this.buffer, 0, result, 0, this.buffer.length);
        System.arraycopy(write, 0, result, this.buffer.length, write.length);
        this.buffer = result;
    }
}